package com.maomao2.simplespringmvc.controller;

import com.maomao2.simplespringmvc.annotation.Controller;
import com.maomao2.simplespringmvc.annotation.Qualifier;
import com.maomao2.simplespringmvc.annotation.RequestMapping;
import com.maomao2.simplespringmvc.service.impl.UserService;

@Controller("userController")
@RequestMapping("/user")
public class UserController {
    @Qualifier("userServiceImpl")
    private UserService userService;

    @RequestMapping("/insert")
    public void insert() {
        userService.insert();
    }
}
