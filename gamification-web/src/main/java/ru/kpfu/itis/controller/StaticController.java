package ru.kpfu.itis.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by timur on 25.06.15.
 */
@Controller
public class StaticController {

    @RequestMapping("/")
    public String indexPage() {
        return "forward:/index.html";
    }

    @RequestMapping("/api")
    public String swaggerPage() {
        return "forward:/swagger.html";
    }

}
