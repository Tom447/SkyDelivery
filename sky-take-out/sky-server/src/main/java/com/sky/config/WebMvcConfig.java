package com.sky.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.interceptor.AdminLoginTokenInterceptor;
import com.sky.interceptor.UserLoginTokenInterceptor;
import com.sky.json.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 配置类
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AdminLoginTokenInterceptor adminLoginTokenInterceptor;
    @Autowired
    private UserLoginTokenInterceptor userLoginTokenInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //管理端的令牌校验拦截器
        registry.addInterceptor(adminLoginTokenInterceptor).addPathPatterns("/admin/**").excludePathPatterns("/admin/employee/login");
        //小程序端的令牌校验拦截器
        registry.addInterceptor(userLoginTokenInterceptor).addPathPatterns("/user/**").excludePathPatterns("/user/user/login","/user/shop/status");
    }


    //扩展的全局的日期格式转换器
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展spring框架的消息转换器，在转化json格式的数据时，要使用自定义的转换器");
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setObjectMapper(new JacksonObjectMapper());
        //将转换器插入到第0为，保证转换
        converters.add(0, mappingJackson2HttpMessageConverter);
    }
}
