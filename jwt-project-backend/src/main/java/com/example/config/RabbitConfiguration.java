package com.example.config;


import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@Configuration
public class RabbitConfiguration {
    @Bean
    BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    //邮件的消息队列
    @Bean("emailQueue")
    public Queue emailQueue(){
        return QueueBuilder
                .durable("mail")
                .build();
    }
    //需要配置用于JSON转换的Bean
    //就是消息队列可能发的消息是对象,需要配置这个才行
    @Bean("jacksonConverter")
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
