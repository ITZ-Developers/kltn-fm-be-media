package com.media.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.media.constant.MediaConstant;
import com.media.dto.ApiMessageDto;
import com.media.service.impl.UserServiceImpl;
import com.media.service.LoggingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class LogInterceptor implements HandlerInterceptor {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    LoggingService loggingService;
    @Autowired
    UserServiceImpl userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws IOException {
        if (DispatcherType.REQUEST.name().equals(request.getDispatcherType().name())
                && request.getMethod().equals(HttpMethod.GET.name())) {
        }
        String tenantName = request.getHeader("X-tenant");
        if (StringUtils.isNotBlank(tenantName)) {
            String tenantInfo = userService.getTenantInfo();
            Integer userKind = userService.getUserKind();
            if (tenantInfo != null && !tenantInfo.isEmpty()) {
                String[] tenantContextList = tenantInfo.split(":");
                for (String tenantContext : tenantContextList) {
                    if (tenantContext.split("&")[0].equals(tenantName)) {
                        String tenantId = tenantContext.split("&")[0];
                        if (tenantId != null) {
                            userService.tenantId.set(tenantId);
                            return true;
                        }
                    }
                }
            } else if (userKind != null && (userKind.equals(MediaConstant.USER_KIND_ADMIN) || userKind.equals(MediaConstant.USER_KIND_CUSTOMER))) {
                userService.tenantId.set(tenantName);
                return true;
            } else {
                ApiMessageDto<String> apiMessageDto = new ApiMessageDto<>();
                apiMessageDto.setResult(false);
                apiMessageDto.setMessage("Invalid tenant");
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                response.getOutputStream().write(objectMapper.writeValueAsBytes(apiMessageDto));
                response.flushBuffer();
                return false;
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

    /**
     * get full url request
     *
     * @param req
     * @return
     */
    private static String getUrl(HttpServletRequest req) {
        String reqUrl = req.getRequestURL().toString();
        String queryString = req.getQueryString();   // d=789
        if (StringUtils.isNotBlank(queryString)) {
            reqUrl += "?" + queryString;
        }
        return reqUrl;
    }
}
