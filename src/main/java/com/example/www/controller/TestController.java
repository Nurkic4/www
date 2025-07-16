package com.example.www.controller;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestController {

    @GetMapping("/hello")
    public Map<String, Object> hello() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello World");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    @GetMapping("/articles")
    public Map<String, Object> getArticles() {
        Map<String, Object> response = new HashMap<>();
        response.put("total", 0);
        response.put("pages", 0);
        response.put("current", 1);
        response.put("size", 10);
        response.put("records", new Object[0]);
        return response;
    }
} 