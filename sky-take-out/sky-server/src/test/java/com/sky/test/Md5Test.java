package com.sky.test;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.util.DigestUtils;

public class Md5Test {

    @Test
    public void testMd5(){
        System.out.println(DigestUtils.md5DigestAsHex("123456".getBytes()));
    }


    @Test
    public void testRandomString(){
        for (int i = 0; i < 20; i++) {
            System.out.println(RandomStringUtils.randomAlphabetic(5));
        }
    }

}
