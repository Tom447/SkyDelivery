package com.sky.service.impl;




import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.BusinessException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;
    @Override
    public User login(UserLoginDTO userLoginDTO) {
        //1.调用微信接口（httpClient），实现登录操作
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appid", weChatProperties.getAppid());
        paramMap.put("secret", weChatProperties.getSecret());
        paramMap.put("js_code", userLoginDTO.getCode());
        paramMap.put("grant_type", "authorization_code");


        String result = HttpClientUtil.doGet(WX_LOGIN_URL, paramMap);
        log.info("微信登录，结果{}",result);

        if (!StringUtils.hasLength(result)){
            throw new BusinessException(MessageConstant.LOGIN_FAILED);
        }

        JSONObject jsonObject = JSON.parseObject(result);
        String openid = jsonObject.getString("openid");//微信用户的唯一标识
        if (!StringUtils.hasLength(openid)){
            throw new BusinessException(MessageConstant.LOGIN_FAILED);
        }
        //2.如果用户是第一次访问小程序，则需要完成自动注册(insert)功能
        User user = userMapper.selectByOpenid(openid);
        if (user == null){
            user = User.builder().openid(openid).createTime(LocalDateTime.now()).build();
            userMapper.insert(user);
        }

        //3.返回数据
        return user;
    }
}
