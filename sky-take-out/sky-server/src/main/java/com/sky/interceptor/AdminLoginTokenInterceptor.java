package com.sky.interceptor;

import com.sky.properties.JwtProperties;
import com.sky.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 校验令牌的拦截器
 */
@Slf4j
@Component
public class AdminLoginTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("拦截到了请求 , {}" , request.getRequestURL().toString());

        //1. 获取请求头中的令牌
        String jwt = request.getHeader(jwtProperties.getAdminTokenName());

        //2. 判断令牌是否存在, 如果不存在, 则不放行 - 401
        if(!StringUtils.hasLength(jwt)){
            log.info("令牌为空, 响应401");
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            return false;
        }

        //3. 校验令牌 , 如果令牌校验失败 , 则不放行 - 401
        try {
            JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), jwt);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("令牌非法, 响应401");
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            return false;
        }

        //4. 放行
        return true;
    }
}
