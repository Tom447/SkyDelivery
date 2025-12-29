package com.sky.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.entity.BaiduLocation;
import com.sky.properties.SkyProperties;
import org.apache.http.HttpRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@SpringBootTest
public class DirectionliteTest {
    @Autowired
    private SkyProperties skyProperties;


    //获得地址的百度地图的接口
    public static final String URL = "https://api.map.baidu.com/geocoding/v3";
    //规划的百度地图接口
    private static final String BAIDU_DIRECTION_LITE_URL = "https://api.map.baidu.com/directionlite/v1/driving";
    private static final String AK = "h4fI0nWOuUT9Nq7PcE32Yg7MNxOaFbNI"; // ← 替换为你的 AK

    @Test
    public void testBaiduDirectionLiteApi() {
        RestTemplate restTemplate = new RestTemplate();

        // 构建请求参数
        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("origin", "40.01116,116.339303");
        uriVariables.put("destination", "39.936404,116.452562");
        uriVariables.put("ak", AK);

        // 发送 GET 请求
        ResponseEntity<String> response = restTemplate.getForEntity(
                BAIDU_DIRECTION_LITE_URL + "?origin={origin}&destination={destination}&ak={ak}",
                String.class,
                uriVariables
        );

        // 打印结果
        System.out.println("状态码: " + response.getStatusCode());
        System.out.println("响应体: " + response.getBody());

        Integer totalDistance = 0;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode route = root.path("result").path("routes").get(0);
            totalDistance = route.get("distance").asInt();
        } catch (Exception e) {
            throw new RuntimeException("百度地图两地计算配置存在错误");
        }
        System.out.println("totalDistance: " + totalDistance);
    }


    @Test
    public void getAddressTest() {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> params = new LinkedHashMap<>();
        params.put("address", "北京市海淀区上地十街10号");
        params.put("output", "json");
        params.put("ak", AK);
        params.put("callback", "showLocation");

        ResponseEntity<String> response = restTemplate.getForEntity(
                URL + "?address={address}&output={output}&ak={ak}&callback={callback}",
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
            System.out.println(skyProperties.getDistance().getDistanceByM());

        } catch (Exception e) {
            throw new RuntimeException("百度地图地理编码解析失败", e);
        }
    }

}
