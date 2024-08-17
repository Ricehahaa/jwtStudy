package com.example.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
//@PreAuthorize("hasRole('rrrr')")
public class TestController {

    @GetMapping("/hello")
    public String test(){
        return "hello";
    }
}
