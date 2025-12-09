package com.sky.test;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;

public class HttpClientTest {


    //Post: http://localhost:8080/admin/employee/login  --参数：username、password
    @Test
    public void testPost() throws Exception {
        //1.构造一个httpclient对象
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        //2.构造一个http请求对象 - HttpPost
        HttpPost httpPost = new HttpPost("http://localhost:8080/admin/employee/login");
        HttpEntity httpEntity = new StringEntity("{\n" +
                "    \"username\": \"admin\",\n" +
                "    \"password\": \"123456\"\n" +
                "}");
        httpPost.setEntity(httpEntity); //  请求体
        httpPost.setHeader("Content-Type","application/json");//请求头
        //3.发送请求
        CloseableHttpResponse response = closeableHttpClient.execute(httpPost);
        //4.获取响应结果
        System.out.println(response.getStatusLine());//状态码
        HttpEntity entity = response.getEntity();//响应体
        String result = EntityUtils.toString(entity);
        System.out.println(result);
        //5.释放资源
        response.close();
        closeableHttpClient.close();
    }



    //Get: http://localhost:8080/admin/employee/page
    @Test
    public void testGet() throws Exception {
        //1.构造一个httpclient对象
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();

        //2.构造一个http请求对象 - HttpPost
        HttpGet httpGet = new HttpGet("http://localhost:8080/admin/employee/page?page=1&pageSize=5");
        httpGet.setHeader("token","eyJhbGciOiJIUzI1NiJ9.eyJlbXBJZCI6MSwiZXhwIjoxNzY0ODQxMzEyfQ.uJAWzjBIEnpDsb3MslnUnGNPgOgGYPGcr2r0CJTrZ5U");

        //3.发送请求
        CloseableHttpResponse response = closeableHttpClient.execute(httpGet);
        //4.获取响应结果
        System.out.println(response.getStatusLine());//状态码
        HttpEntity entity = response.getEntity();//响应体
        String result = EntityUtils.toString(entity);
        System.out.println(result);
        //5.释放资源
        response.close();
        closeableHttpClient.close();

    }
}
