package com.bigmoji.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthSessionInterceptor implements HandlerInterceptor {
    public static final String REQUEST_ATTRIBUTE = "bigmoji.authSession";

    private final SignedCookieSessionService sessionService;

    public AuthSessionInterceptor(SignedCookieSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        AuthSession session =
                sessionService
                        .read(request)
                        .orElseThrow(() -> new AuthenticationRequiredException("Authentication required"));
        request.setAttribute(REQUEST_ATTRIBUTE, session);
        return true;
    }
}
