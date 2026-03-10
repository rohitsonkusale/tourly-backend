package com.tourly.auth.controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/secure")
    public String secureEndpoint() {
        return "JWT authentication working!";
    }
}