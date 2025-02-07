package com.media.service.impl;

import com.media.service.AuthenticationFacadeService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFacadeServiceImpl implements AuthenticationFacadeService {
    @Override
    public Authentication getAuthentication() {
        if (SecurityContextHolder.getContext().getAuthentication() == null){
            throw new BadCredentialsException("Invalid token");
        }
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
