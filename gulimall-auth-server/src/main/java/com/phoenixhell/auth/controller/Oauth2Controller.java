package com.phoenixhell.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.phoenixhell.auth.config.Oauth2Properties;
import com.phoenixhell.auth.feign.MemberFeignService;
import com.phoenixhell.auth.vo.GithubRegistVo;
import com.phoenixhell.auth.vo.GithubTokenVo;
import com.phoenixhell.auth.vo.GithubUserVo;
import com.phoenixhell.common.constant.AuthConstant;
import com.phoenixhell.common.utils.HttpUtils;
import com.phoenixhell.common.utils.R;
import com.phoenixhell.common.vo.MemberVo;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
public class Oauth2Controller {
    @Autowired
    private Oauth2Properties properties;
    @Autowired
    private MemberFeignService memberFeignService;

    /*
   将请求重定向到GitHub授权服务器
   redirect:https://github.com/login/oauth/authorize?client_id=clientID&redirect_uri=callbackUrl”
   */
    @GetMapping("/oauth2/login")
    public String oauth2Login() {
        String clientID = properties.getClientID();
        String authorizeUrl = properties.getAuthorizeUrl();
        String callbackUrl = properties.getCallbackUrl();
        String url = authorizeUrl + "?client_id=" + clientID + "&redirect_uri=" + callbackUrl;
        return "redirect:" + url;
    }

    //用户同意授权后会重定向到此路径 携带授权码code  只能使用一次
    @GetMapping("/oauth2/callback")
    public String callback(@RequestParam("code") String code, RedirectAttributes redirectAttributes, HttpSession session) {
        HashMap<String, String> errors = new HashMap<>();
        String accessToken = getAccessToken(code);
        if (StringUtils.isEmpty(accessToken)) {
            errors.put("msg", "登录失败");
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth."+AuthConstant.WEBNAME+"/login.html";
        }
        //根据token 获取user信息 第一次登录需要注册
        GithubRegistVo githubRegistVo = getUserInfo(accessToken);
        R r = memberFeignService.oauth2Login(githubRegistVo);
        if (r.getCode() != 0) {
            errors.put("msg", (String) r.get("msg"));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth."+AuthConstant.WEBNAME+"/login.html";
        }
        MemberVo memberVo = r.getData(new TypeReference<MemberVo>() {
        });
        // 重定向到不同服务的页面是带不了数据的
        //第一次使用session 命令浏览器保存jsessionid 这个cookie
        //以后浏览器访问这个网站都会带上这个cookie
        //即使请求的是这个网站的子域 携带的cookie的作用域都指定为父级网站

        //整合spring session
        //memberVo 以json方式存入redis 好点 配置
        //session.setMaxInactiveInterval(60*60*24*30);
        session.setAttribute(AuthConstant.LOGIN_USER,memberVo);
        return "redirect:http://"+AuthConstant.WEBNAME;
    }

    //根据拿到的授权码 向服务器申请令牌
    /*
    POST accessTokenUrl?client_id=clientID&client_secret=clientSecrets&code=code&grant_type=authorization_code
     */
    public String getAccessToken(String code) {
        try {
            String host = properties.getAccessTokenUrl();
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Accept", "application/json");
            Map<String, String> bodys = new HashMap<String, String>();
            bodys.put("client_id", properties.getClientID());
            bodys.put("client_secret", properties.getClientSecrets());
            bodys.put("code", code);
            //header不能少 随便塞点啥进去  不然爆空指针异常
            HttpResponse response = HttpUtils.doPost(host, null, "post", headers, null, bodys);
            if (response.getStatusLine().getStatusCode() == 200) {
                //EntityUtils 提取加密响应内容 entity变成accessTokenJson
                String tokenJson = EntityUtils.toString(response.getEntity());
                GithubTokenVo tokenVo = JSON.parseObject(tokenJson, new TypeReference<GithubTokenVo>() {
                });
                return tokenVo.getAccessToken();
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private GithubRegistVo getUserInfo(String accessToken) {
        try {
            String host = properties.getUserInfoUrl();
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Authorization", "token " + accessToken);
            HttpResponse response = HttpUtils.doGet(host, null, "get", headers, null);
            String userJson = EntityUtils.toString(response.getEntity());
            GithubUserVo userVo = JSON.parseObject(userJson, new TypeReference<GithubUserVo>() {});
            GithubRegistVo githubRegistVo = new GithubRegistVo();
            githubRegistVo.setUsername(userVo.getLogin());
            githubRegistVo.setGithubId(userVo.getId());
            githubRegistVo.setHeader(userVo.getAvatarUrl());
            return githubRegistVo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
