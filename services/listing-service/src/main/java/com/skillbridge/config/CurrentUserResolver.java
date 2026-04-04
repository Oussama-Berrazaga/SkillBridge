package com.skillbridge.config;

import org.springframework.core.MethodParameter;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.skillbridge.exception.AccessDeniedException;

@Component
public class CurrentUserResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(CurrentUser.class)
        && parameter.getParameterType().equals(AuthUser.class);
  }

  @Override
  public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws AccessDeniedException {
    String userId = webRequest.getHeader("X-User-Id");
    String role = webRequest.getHeader("X-User-Role");
    if (userId == null || role == null) {
      throw new AccessDeniedException("Missing auth headers");
    }

    try {
      return new AuthUser(Long.parseLong(userId), UserRole.valueOf(role));
    } catch (IllegalArgumentException e) {
      throw new AccessDeniedException("Invalid auth headers");
    }

  }
}