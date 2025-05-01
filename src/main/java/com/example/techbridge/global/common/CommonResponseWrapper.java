package com.example.techbridge.global.common;

import com.example.techbridge.global.exception.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Slf4j
@ControllerAdvice
public class CommonResponseWrapper implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType,
        Class<? extends HttpMessageConverter<?>> converterType) {
        return !returnType.getParameterType().equals(ErrorResponse.class)
            && !returnType.getParameterType().equals(ResponseEntity.class);
    }

    @Override
    public Object beforeBodyWrite(Object body,
        MethodParameter returnType,
        MediaType selectedContentType,
        Class<? extends HttpMessageConverter<?>> selectedConverterType,
        org.springframework.http.server.ServerHttpRequest request,
        org.springframework.http.server.ServerHttpResponse response) {

        if (body instanceof CommonResponse<?> common) {
            return common;
        }

        return CommonResponse.success(body);
    }
}
