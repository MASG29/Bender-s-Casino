package com.bendercasino.controller;

import com.bendercasino.service.SlotsService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.net.http.HttpResponse;

@RestController
@RequestMapping("/api/slots")
public class SlotsController {

    private SlotsService slotsService;

    @RequestMapping(method = RequestMethod.GET, path = "/roll")
    public HttpResponse<> roll() {
        slotsService.roll();

    }

}
