package com.medicology.dictionary.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;
import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@Controller
public class RootController {
    @GetMapping("/")
    public RedirectView redirectRootToSwagger() {
        return new RedirectView("/swagger-ui/index.html");
    }
}
