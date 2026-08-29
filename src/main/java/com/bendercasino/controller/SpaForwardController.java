package com.bendercasino.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
class SpaForwardController {

    // excludes "api" and any segment with a dot, so static assets (.css, .js, favicon.ico) fall through
    @RequestMapping("/{path:^(?!api$)[^.]*}")
    String forward() {
        return "forward:/index.html";
    }

    @RequestMapping("/{path:^(?!api$)[^.]*}/{subpath:[^.]*}")
    String forwardNested() {
        return "forward:/index.html";
    }
}
