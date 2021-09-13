package com.phoenixhell.gulimall.cart.interceptor;

import com.phoenixhell.common.constant.AuthConstant;
import com.phoenixhell.common.constant.CartConstant;
import com.phoenixhell.common.vo.MemberVo;
import com.phoenixhell.gulimall.cart.vo.UserInfoTo;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

//判断用户的状态 封装传递给controller
public class CartInterceptor implements HandlerInterceptor {
    //同一线程共享数据
    public static ThreadLocal<UserInfoTo> userInfoToThreadLocal=new ThreadLocal<>();

    //目标方法执行之前拦截

    /**
     * 调用时间：Controller方法处理之前
     *
     * 执行顺序：链式Intercepter情况下，Intercepter按照声明的顺序一个接一个执行
     *
     * 若返回false，则中断执行，注意：不会进入afterCompletion
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        MemberVo memberVo = (MemberVo) session.getAttribute(AuthConstant.LOGIN_USER);
        UserInfoTo userInfoTo = new UserInfoTo();
        if (memberVo != null) {
            //获取登录后的购物车
            userInfoTo.setUserId(memberVo.getId());
        }
        //无论有没登录 都需要一个user-key用来识别用户
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (CartConstant.TEMP_USER_COOKIE_NAME.equalsIgnoreCase(cookie.getName())) {
                    userInfoTo.setUserKey(cookie.getValue());
                    //说明请求带来了临时 user-key  不需要重新创建
                    userInfoTo.setTempKey(true);
                    break;
                }
            }
        }
        //第一次登录 啥都没 手动分配user-key 给临时用户
        if(StringUtils.isEmpty(userInfoTo.getUserKey())){
            userInfoTo.setUserKey(UUID.randomUUID().toString());
        }

        //数据放入threadlocal 同一线程共享数据  controller方法执行前 可以获取到
        userInfoToThreadLocal.set(userInfoTo);
        return true;
    }

    //业务执行之后  不管是登录后得到的user key（合并没登录的购物车用） 还是创建出来的  都要返回给浏览器 让他下次访问带上识别用户

    /**
     * 调用前提：preHandle返回true
     *
     * 调用时间：Controller方法处理完之后，DispatcherServlet进行视图的渲染之前，也就是说在这个方法中你可以对ModelAndView进行操作
     *
     * 执行顺序：链式Intercepter情况下，Intercepter按照声明的顺序倒着执行。
     *
     * 备注：postHandle虽然post打头，但post、get方法都能处理
     *
     * 给浏览器cookie中放入我们创建的user-key
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = userInfoToThreadLocal.get();
        //如果带了user-key就不需要设置cookie
        if(!userInfoTo.getTempKey()){
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME,userInfoTo.getUserKey());
            cookie.setDomain("gulimall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_EXPIRE_TIME); //一个月过期
            response.addCookie(cookie);
        }
    }

    /**
     * 调用前提：preHandle返回true
     *
     * 调用时间：DispatcherServlet进行视图的渲染之后
     *
     * 多用于清理资源
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
