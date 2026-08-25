package com.bendercasino.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
class SpaForwardController {

    // Catch-all para rotas do SPA: exclui "api" (rotas REST) e qualquer segmento com ponto
    // (assets estáticos como .css, .js, .png, favicon.ico).
    @RequestMapping("/{path:^(?!api$)[^.]*}")
    String forward() {
        return "forward:/index.html";
    }

    @RequestMapping("/{path:^(?!api$)[^.]*}/{subpath:[^.]*}")
    String forwardNested() {
        return "forward:/index.html";
    }
}
