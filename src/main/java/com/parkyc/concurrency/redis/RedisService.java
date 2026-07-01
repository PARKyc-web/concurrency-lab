package com.parkyc.concurrency.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RedisService {

    private final StringRedisTemplate template;

    public void set(String key, String value){
        template.opsForValue().set(key, value);
    }

    public String get(String key){
        return template.opsForValue().get(key);
    }
}
