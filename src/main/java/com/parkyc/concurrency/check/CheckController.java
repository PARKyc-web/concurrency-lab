package com.parkyc.concurrency.check;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequiredArgsConstructor
@RestController
public class CheckController {

    private final CheckService checkService;

    @GetMapping("/redis")
    public String redis(String key){
        return checkService.get(key);
    }

    @PostMapping("/redis")
    public void insRedis(@RequestBody Map<String, String> map){
        checkService.set(map.get("key"), map.get("value"));
    }

    @GetMapping("/redis/ping")
    public String ping(){
        return checkService.get("ping");
    }

}
