package com.example.cart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Component
public class MappingPrinter implements ApplicationRunner {
    @Autowired
    private RequestMappingHandlerMapping mapping;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var map = mapping.getHandlerMethods();
        System.out.println("---- Registered request mappings ----");
        map.forEach((k, v) -> System.out.println(k + " -> " + v));
        System.out.println("------------------------------------");
    }
}
