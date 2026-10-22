package com.outsera.awards.adapter.in.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SwaggerUiController {

    @GetMapping("/")
    public String redirectToSwaggerUi() {
        return "redirect:/swagger-ui/index.html";
    }
}
