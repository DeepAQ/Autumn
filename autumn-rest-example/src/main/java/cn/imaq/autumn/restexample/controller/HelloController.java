package cn.imaq.autumn.restexample.controller;

import cn.imaq.autumn.rest.annotation.RequestMapping;
import cn.imaq.autumn.rest.annotation.RestController;
import cn.imaq.autumn.rest.annotation.param.RequestParam;
import cn.imaq.autumn.rest.core.RequestMethod;

@RestController
@RequestMapping("/")
public class HelloController {
    @RequestMapping(value = "/", method = RequestMethod.GET)
    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String hello(@RequestParam("name") String name) {
        if (name.isEmpty()) {
            name = "AutumnREST";
        }
        return "<h1>Hello " + name + "!</h1>";
    }
}
