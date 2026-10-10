package dev.kj.r2mod_sync.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaticPageController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/server.html")
    public String index() {
        return "server";
    }

    @GetMapping("/login.html")
    public String loginPage() {
        return "login";
    }
}
