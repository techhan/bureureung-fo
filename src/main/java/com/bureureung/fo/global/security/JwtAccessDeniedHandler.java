package com.bureureung.fo.global.security;

import com.bureureung.fo.global.common.ApiResponse;
import com.bureureung.fo.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
  private final ObjectMapper objectMapper;

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws IOException, ServletException {
    response.setStatus(HttpStatus.FORBIDDEN.value()); // 401
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write(objectMapper.writeValueAsString(
        ApiResponse.failBody(ErrorCode.ACCESS_DENIED)
    ));
  }
}
