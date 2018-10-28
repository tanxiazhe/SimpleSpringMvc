package com.maomao2.simplespringmvc.dao.impl;

import com.maomao2.simplespringmvc.annotation.Repository;
import com.maomao2.simplespringmvc.dao.UserDao;

@Repository(value = "userDaoImpl")
public class UserDaoImpl implements UserDao {
    public void insert() {
        System.out.println("dao insert");
    }
}
