package com.phoenixhell.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.phoenixhell.auth.feign.MemberFeignService;
import com.phoenixhell.auth.feign.SendCodeService;
import com.phoenixhell.auth.vo.UserLoginVo;
import com.phoenixhell.auth.vo.UserRegistVo;
import com.phoenixhell.common.constant.AuthConstant;
import com.phoenixhell.common.exception.BizCodeEnume;
import com.phoenixhell.common.utils.R;
import com.phoenixhell.common.vo.MemberVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class LoginController {
    //   webconfig 直接定义简单路径controller

    @Autowired
    private SendCodeService sendCodeService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private MemberFeignService memberFeignService;

    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone) {
        //验证码的再次校验
        String codeString = redisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(codeString)) {
            long codeTime = Long.parseLong(codeString.split("_")[1]);
            if (System.currentTimeMillis() - codeTime < 60000) {
                //验证码未过60秒
                return R.error(BizCodeEnume.SMS_CODe_EXCEPTION.getCode(), BizCodeEnume.SMS_CODe_EXCEPTION.getMsg());
            }
        }

        //验证码的再次校验 存到redis 设置过期时间
        String code = String.format("%040d", new BigInteger(UUID.randomUUID().toString().replace("-", ""), 16)).substring(1, 7);
        String redisCode = code + "_" + System.currentTimeMillis();
        redisTemplate.opsForValue().set(AuthConstant.SMS_CODE_CACHE_PREFIX + phone, redisCode, 30, TimeUnit.MINUTES);
        sendCodeService.sendCode(phone, code);
        return R.ok();
    }

    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo userRegistVo, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            //toMap 个参数  键  值
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(fieldError -> {
                return fieldError.getField();
            }, fieldError -> {
                return fieldError.getDefaultMessage();
            }));
            redirectAttributes.addFlashAttribute("errors", errors);
            //forward:sign  用户注册->regist[Post]-->转发sign(默认路径映射都是get)
            //解决直接渲染thymeleaf return "sign"
            // 新问题 防止表单重复提交
            // RedirectAttributes 重定向携带数据  session
            return "redirect:http://auth."+AuthConstant.WEBNAME+"/sign.html";
        }
        //真正注册 校验验证码
        String codeString = redisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + userRegistVo.getPhone());
        Map<String, String> errors = new HashMap<>();
        if (StringUtils.isEmpty(codeString)) {
            errors.put("code", "验证码过期");
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth."+AuthConstant.WEBNAME+"/sign.html";
        } else if (!codeString.split("_")[0].equals(userRegistVo.getCode())) {
            errors.put("code", "验证码不正确");
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth."+AuthConstant.WEBNAME+"/sign.html";
        }
        //验证码验证成功 删除验证码
        redisTemplate.delete(AuthConstant.SMS_CODE_CACHE_PREFIX + userRegistVo.getPhone());
        //远程注册
        R r = memberFeignService.regist(userRegistVo);
        if (r.getCode() != 0) {
            errors.put("msg", (String) r.get("msg"));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth."+AuthConstant.WEBNAME+"/sign.html";
        }
        return "redirect:http://auth."+AuthConstant.WEBNAME+"/login.html";
    }

    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session) {
        R login = memberFeignService.login(vo);
        if (login.getCode() != 0) {
            HashMap<String, String> errors = new HashMap<>();
            errors.put("msg", login.get("msg").toString());
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth."+AuthConstant.WEBNAME+"/login.html";
        }
        //只要登录都要存入spring session
        //session.setMaxInactiveInterval(60*60*24*30);
        session.setAttribute(AuthConstant.LOGIN_USER,login.getData(new TypeReference<MemberVo>(){}));
        return "redirect:http://"+AuthConstant.WEBNAME;
    }

    @GetMapping({"/login.html","/"})
    public String loginPage(HttpSession session){
        if(session.getAttribute(AuthConstant.LOGIN_USER)!=null){
            return "redirect:http://"+AuthConstant.WEBNAME;
        }
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session){
        //不起作用 只是移除了用户 session 还在
        //session.removeAttribute(AuthConstant.LOGIN_USER);

        //使Session变成无效，及用户退出
        session.invalidate();
        //清理redis 当前登录用户的session信息
        return "redirect:http://"+AuthConstant.WEBNAME;
    }
}
