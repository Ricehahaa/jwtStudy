package com.example.utils;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class FlowUtils {
    @Resource
    StringRedisTemplate stringRedisTemplate;
    //只要请求了就放到redis里60s做限流
    public boolean limitOnceCheck(String key, int blockTime){
        if(Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))){
            //已经在redis了,返回false
            return false;
        } else {
            //不在redis里,说明第一次,作限流操作60s
            stringRedisTemplate.opsForValue()
                    .set(key, "", blockTime, TimeUnit.SECONDS);
            return true;
        }
    }
}
