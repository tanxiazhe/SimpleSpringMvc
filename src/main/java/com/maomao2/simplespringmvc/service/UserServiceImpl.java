package com.maomao2.simplespringmvc.service;

import com.maomao2.simplespringmvc.annotation.Qualifier;
import com.maomao2.simplespringmvc.annotation.Service;
import com.maomao2.simplespringmvc.dao.UserDao;
import com.maomao2.simplespringmvc.service.impl.UserService;

@Service(value = "userServiceImpl")
public class UserServiceImpl implements UserService {
    @Qualifier("userDaoImpl")
    private UserDao userDao;

    public void insert() {
        userDao.insert();
    }
}
