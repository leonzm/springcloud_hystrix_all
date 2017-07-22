package com.example.springcloud_hystrix.rest;

import com.example.springcloud_hystrix.command.HelloCommand;
import com.example.springcloud_hystrix.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import rx.Observable;

/**
 * @Author: Leon
 * @CreateDate: 2017/7/15
 * @Description:
 * @Version: 1.0.0
 */
@RestController
public class ConsumerController {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private HelloService helloService;

    // 注解配置的方式
    @RequestMapping(value = "ribbon-consumer", method = RequestMethod.GET)
    public String helloConsumer() {
        return helloService.helloService();
    }

    // 传统继承方式
    @RequestMapping(value = "ribbon-consumer2", method = RequestMethod.GET)
    public String helloConsumer2() {
        //java.util.concurrent.Future<String> future = helloCommand.queue(); // 异步调用
        return new HelloCommand().execute(); // 同步调用

        // 除了传统的同步执行与异步执行之外，还可以将 HystrixCommand 通过 Observable 来实现响应式执行方式
        //Observable<String> ho = new HelloCommand(restTemplate).observe(); // 返回一个 Hot Observable，该命令会在 observe() 调用时立即执行
        //Observable<String> co = new HelloCommand(restTemplate).toObservable(); // 返回一个 Cold Observable，命令不会被立即执行，只有当所有订阅都订阅它之后才会执行
     }

}
