package com.bendercasino.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
class SpaForwardController {

    @RequestMapping({"/lobby", "/blackjack", "/profile"})
    String forward() {
        return "forward:/index.html";
    }
}
