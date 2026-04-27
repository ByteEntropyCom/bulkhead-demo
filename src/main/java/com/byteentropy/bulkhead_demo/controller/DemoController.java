package com.byteentropy.bulkhead_demo.controller;

import com.byteentropy.bulkhead_demo.service.ExternalApiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.CompletableFuture;

@RestController
public class DemoController {

    private final ExternalApiService apiService;

    public DemoController(ExternalApiService apiService) {
        this.apiService = apiService;
    }

    @GetMapping("/semaphore")
    public String semaphore() {
        return apiService.slowSemaphoreCall();
    }

    @GetMapping("/threadpool")
    public CompletableFuture<String> threadpool() {
        return apiService.isolatedThreadCall();
    }
}