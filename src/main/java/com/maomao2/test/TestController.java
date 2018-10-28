package com.maomao2.test;

import com.maomao2.simplespringmvc.annotation.Controller;
import com.maomao2.simplespringmvc.annotation.Qualifier;
import com.maomao2.simplespringmvc.annotation.RequestMapping;
import com.maomao2.simplespringmvc.service.impl.UserService;

@RequestMapping("/user")
@Controller(value = "testController")
public class TestController {

    @Qualifier("userServiceImpl")
    private UserService userService;

    @RequestMapping("/insert2")
    public void insert2() {
        System.out.println("testcontroller insert2");
        userService.insert();
    }

    @RequestMapping("/insert3")
    public void insert3() {
        System.out.println("testcontroller insert3");
        userService.insert();
    }
}
