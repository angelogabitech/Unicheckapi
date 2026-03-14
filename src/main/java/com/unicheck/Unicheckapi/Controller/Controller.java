package com.unicheck.Unicheckapi.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class Controller {
    @GetMapping("/teste")
    public String teste() {
        return "Backend funcionando 🚀";
    }
}
