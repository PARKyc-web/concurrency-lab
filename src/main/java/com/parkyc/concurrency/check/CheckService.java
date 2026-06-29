package com.parkyc.concurrency.check;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CheckService {

    private final StringRedisTemplate template;

    public String get(String key){
        return template.opsForValue().get(key);
    }

    public void set(String key, String value){
        template.opsForValue().set(key, value);
    }

}
