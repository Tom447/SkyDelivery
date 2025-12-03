package com.sky.interceptor;


import com.aliyuncs.utils.StringUtils;
import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 校验令牌的拦截器
 */
@Slf4j
@Component
public class AdminLoginTokenInterceptor implements HandlerInterceptor{

    @Autowired
    private JwtProperties jwtProperties;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("拦截到了请求，{}",request.getRequestURI().toString());

        //1. 获取令牌
        String jwt = request.getHeader(jwtProperties.getAdminTokenName());
        //2. 判断令牌是否存在。如果不存在，则不放行 - 401
        if (StringUtils.isEmpty(jwt)){
            log.info("令牌为空，直接响应401");
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            return false;
        }
        //3. 校验令牌，如果令牌校验失败， 则不放行 - 401
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), jwt);
            log.info("解析完令牌，{}",claims);
            Object empIdObj = claims.get(JwtClaimsConstant.EMP_ID);
            Long empId = null;
            if (empIdObj instanceof Number){
                empId = ((Number) empIdObj).longValue();
            }
            //存入当前登录员工id
            BaseContext.setCurrentId(empId);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("令牌非法，响应401");
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            return false;
        }
        // 4.放行
        return true;
    }

    @Override
     public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                            @Nullable ModelAndView modelAndView) throws Exception {
        BaseContext.removeCurrentId();

    }

}
