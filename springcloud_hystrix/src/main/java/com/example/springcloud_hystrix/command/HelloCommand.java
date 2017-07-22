package com.example.springcloud_hystrix.command;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * @Author: Leon
 * @CreateDate: 2017/7/20
 * @Description: 传统继承方式
 * @Version: 1.0.0
 */
@Service
public class HelloCommand extends HystrixCommand<String> {

    private static final Logger LOGGER = Logger.getLogger(HelloCommand.class);

    @Autowired
    private RestTemplate restTemplate;

    public HelloCommand() {
        super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("HelloGroup"))
                .andCommandKey(HystrixCommandKey.Factory.asKey("Hello")));
    }

    @Override
    protected String run() throws Exception {
        LOGGER.info("HelloCommand -> run");
        return restTemplate.getForEntity("http://HELLO-SERVICE/hello", String.class).getBody();
    }

    @Override
    protected String getFallback() {
        LOGGER.info("HelloCommand -> getFallback");
        return "error";
    }
}
