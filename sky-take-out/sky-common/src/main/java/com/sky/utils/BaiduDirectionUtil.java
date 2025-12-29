package com.sky.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.entity.BaiduLocation;
import com.sky.properties.SkyProperties;
import com.sky.properties.WeChatProperties;
import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * 微信支付工具类
 */
@Component
@Slf4j
public class BaiduDirectionUtil {

    @Autowired
    private SkyProperties skyProperties;


    /**得到地址address的经纬度
     * "location": {
     *       "lat": 31.208645,  //纬度
     *       "lng": 121.603218  // 经度
     *     }
     * @param address
     */
    public BaiduLocation getAddrOfLatAndLng(String address){

        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> params = new LinkedHashMap<>();
        params.put("address", address);
        params.put("output", "json");
        params.put("ak", skyProperties.getBaidu().getAk());
        params.put("callback", "showLocation");

        ResponseEntity<String> response = restTemplate.getForEntity(
                skyProperties.getBaidu().getGeoCoderUrl() + "?address={address}&output={output}&ak={ak}&callback={callback}",
                String.class,
                params
        );

        System.out.println("状态码: " + response.getStatusCode());
        System.out.println("响应体: " + response.getBody());

        String body = response.getBody();
        String jsonStr = body;
        if (body.contains("(") && body.endsWith(")")) {
            int start = body.indexOf('(');
            int end = body.lastIndexOf(')');
            jsonStr = body.substring(start + 1, end);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode root = objectMapper.readTree(jsonStr);

            double lng = root.path("result").path("location").get("lng").asDouble();
            double lat = root.path("result").path("location").get("lat").asDouble();

            // 格式化为6位小数
            String formattedLat = String.format("%.6f", lat);
            String formattedLng = String.format("%.6f", lng);
            String locationStr = formattedLat + "," + formattedLng;

            BaiduLocation baiduLocation = BaiduLocation.builder()
                    .lng(lng)
                    .lat(lat)
                    .loation(locationStr) // 注意：建议字段名改为 location（拼写）
                    .build();

            System.out.println("lng: " + lng);
            System.out.println("lat: " + lat);
            System.out.println("location: " + baiduLocation.getLoation());
            return baiduLocation;

        } catch (Exception e) {
            throw new RuntimeException("百度地图地理编码解析失败", e);
        }
    }

    /** 参数格式
     *   原地 origin:40.01116,116.339303
     *   目的地 destination:"39.936404,116.452562
     * @param origin
     * @param destination
     */
    public Integer getDirecirectionLite(String origin, String destination){
        RestTemplate restTemplate = new RestTemplate();

        // 构建请求参数
        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("origin", origin);
        uriVariables.put("destination", destination);
        uriVariables.put("ak", skyProperties.getBaidu().getAk());

        // 发送 GET 请求
        ResponseEntity<String> response = restTemplate.getForEntity(
                skyProperties.getBaidu().getDrivingUrl() + "?origin={origin}&destination={destination}&ak={ak}",
                String.class,
                uriVariables
        );

//        // 打印结果
//        System.out.println("状态码: " + response.getStatusCode());
//        System.out.println("响应体: " + response.getBody());
        //两地之间的距离
        Integer totalDistance = 0;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode route = root.path("result").path("routes").get(0);
            totalDistance = route.get("distance").asInt();
            return totalDistance;
        }catch (Exception e){
            throw new RuntimeException("百度地图两地计算配置存在错误");
        }
    }
}
