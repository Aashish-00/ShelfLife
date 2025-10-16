package com.shelflife.shelflife.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/hello")
    public String home(){
        return "Welcome to shelflife";
    }

    @GetMapping("/")
    public String index(){
        return "redirect:/login";
    }
}
