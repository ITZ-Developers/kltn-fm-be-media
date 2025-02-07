package com.media.controller;

import com.media.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

public class ABasicController {
    @Autowired
    UserServiceImpl userService;
}
