package com.parkyc.concurrency.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

// @Configuration
public class RedisConfig {

    // 추후, Redis를 사용할때 별도의 설정을 하고 싶을때 사용

    private final String host;
    private final int port;

    public RedisConfig(@Value("${spring.data.redis.host}") String host,
                       @Value("${spring.data.redis.port}") int port){
        this.host = host;
        this.port = port;
    }

}
