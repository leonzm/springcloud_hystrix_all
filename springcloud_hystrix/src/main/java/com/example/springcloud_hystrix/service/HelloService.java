package com.example.springcloud_hystrix.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * @Author: Leon
 * @CreateDate: 2017/7/15
 * @Description: 注解配置的方式
 * @Version: 1.0.0
 */
@Service
public class HelloService {

    private static final Logger LOGGER = Logger.getLogger(HelloService.class);

    @Autowired
    private RestTemplate restTemplate;

    @HystrixCommand(fallbackMethod = "helloFallback")
    public String helloService() {
        LOGGER.info("HelloService -> helloService");
        return restTemplate.getForEntity("http://HELLO-SERVICE/hello", String.class).getBody();
    }

    // 服务降级处理
    public String helloFallback() {
        LOGGER.info("HelloService -> helloFallback");
        return "error";
    }

}
