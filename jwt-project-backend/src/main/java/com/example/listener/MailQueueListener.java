package com.example.listener;

import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
//@RabbitListener(queues = "mail", messageConverter = "jacksonConverter")
public class MailQueueListener {
    @Resource
    MailSender sender;

    @Value("${spring.mail.username}")
    String username;

//    @RabbitHandler
    @RabbitListener(queues = "mail", messageConverter = "jacksonConverter")
    public void sendMailMessage(Map<String, Object> data){
        String email = (String) data.get("email");
        Integer code = (Integer) data.get("code");
        String type = (String) data.get("type");
        SimpleMailMessage message = switch (type) {
            case "register" -> createMessage("欢迎注册网站", "验证码:" + code + "有效时间3分钟", email);
            case "reset" -> createMessage("密码重置邮件", "验证码:" + code + "有效时间3分钟", email);
            default -> null;
        };
        if(message == null) return;
        sender.send(message);
    }

    private SimpleMailMessage createMessage(String title, String content, String email){
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setSubject(title);
        simpleMailMessage.setText(content);
        simpleMailMessage.setTo(email);
        simpleMailMessage.setFrom(username);
        return simpleMailMessage;
    }
}
