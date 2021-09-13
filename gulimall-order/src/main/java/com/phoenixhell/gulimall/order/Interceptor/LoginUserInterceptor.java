package com.phoenixhell.gulimall.order.Interceptor;

import com.phoenixhell.common.constant.AuthConstant;
import com.phoenixhell.common.vo.MemberVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//拦截器要起作用 需要陪在到mvconfig里面
@Component
public class LoginUserInterceptor implements HandlerInterceptor {
    //线程共享memberVo 其他人都能访问
    public static ThreadLocal<MemberVo> threadLocal=new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //服务器之间的调用无需登录
        //url全部路径  uri 只有path内容就是com后面的
        String requestURI = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/order/order/status/**", requestURI);
        if(match){
            return true;
        }
        HttpSession session = request.getSession();
        MemberVo memberVo = (MemberVo) session.getAttribute(AuthConstant.LOGIN_USER);
        if(memberVo!=null){
            threadLocal.set(memberVo);
            session.removeAttribute("message");
            return true;
        }
        //时间到了就自动消失
        session.setMaxInactiveInterval(20);
        session.setAttribute("message","请先登录");
        response.sendRedirect("http://auth."+AuthConstant.WEBNAME);
        return  false;
    }
}
