package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "sky") // 绑定到 sky. 下的所有属性
public class SkyProperties {

    private Shop shop = new Shop();
    private Baidu baidu = new Baidu();
    private Distance distance = new Distance();
    @Data
    public static class Shop {
        private String address;
    }

    @Data
    public static class Baidu {
        private String ak;
        private String geoCoderUrl;
        private String drivingUrl;
    }

    @Data
    public static class Distance {
        private Integer distanceByM;
    }


}