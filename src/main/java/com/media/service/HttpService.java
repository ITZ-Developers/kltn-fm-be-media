package com.media.service;

import com.media.exception.UnauthorizationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class HttpService {
    @Value("${app.api-key}")
    private String apiSecretKey;

    public void validateInternalRequest(HttpServletRequest request) {
        try {
            String apiKey = request.getHeader("X-Api-Key");
            if (apiKey == null || apiKey.isEmpty() || !Objects.equals(apiKey, apiSecretKey)) {
                throw new UnauthorizationException("Full authentication is required to access this resource");
            }
        } catch (Exception e) {
            throw new UnauthorizationException("Full authentication is required to access this resource");
        }
    }

    public MultiValueMap<String, String> buildParams(Object criteria) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        Stream.of(criteria.getClass().getDeclaredFields())
                .forEach(field -> {
                    field.setAccessible(true);
                    try {
                        Object value = field.get(criteria);
                        if (Objects.nonNull(value)) {
                            params.add(field.getName(), value.toString());
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                });

        return params;
    }
}
