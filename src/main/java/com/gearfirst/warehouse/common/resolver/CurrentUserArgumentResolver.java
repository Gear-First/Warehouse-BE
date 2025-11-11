package com.gearfirst.warehouse.common.resolver;

import com.gearfirst.warehouse.common.annotation.CurrentUser;
import com.gearfirst.warehouse.common.context.UserContext;
import com.gearfirst.warehouse.common.context.UserContextHolder;
import com.gearfirst.warehouse.common.exception.UnAuthorizedException;
import com.gearfirst.warehouse.common.response.ErrorStatus;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && parameter.getParameterType().equals(UserContext.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        UserContext user = UserContextHolder.get();
        if (user == null) {
            throw new UnAuthorizedException(ErrorStatus.USER_UNAUTHORIZED);
        }
        return user;
    }
}
