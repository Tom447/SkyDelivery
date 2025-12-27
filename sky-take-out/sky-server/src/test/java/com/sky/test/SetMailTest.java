package com.sky.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.internet.MimeMessage;

@SpringBootTest
public class SetMailTest {

    @Autowired
    private JavaMailSender javaMailSender;

    @Test
    public void testSendSimpleMail(){
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom("1142322503@qq.com");
        simpleMailMessage.setCc("3546872365@qq.com");
        simpleMailMessage.setSubject("test");
        simpleMailMessage.setText("hello world");
        javaMailSender.send(simpleMailMessage);
    }
}
