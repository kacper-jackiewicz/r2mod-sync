package dev.kj.r2mod_sync.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class LoginRateLimitFilterTest {

    @Test
    void deniedLoginRedirectsToFailurePageAndStopsFilterChain() throws Exception {
        RateLimiterRegistry registry = mock(RateLimiterRegistry.class);
        RateLimiter limiter = mock(RateLimiter.class);
        when(registry.rateLimiter("login:192.0.2.10", "loginConfig")).thenReturn(limiter);
        when(limiter.acquirePermission()).thenReturn(false);

        LoginRateLimitFilter filter = new LoginRateLimitFilter(registry);
        MockHttpServletRequest request = loginRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals("/?page=login&error", response.getRedirectedUrl());
        assertNull(chain.getRequest());
        verify(limiter).acquirePermission();
    }

    @Test
    void permittedLoginContinuesFilterChain() throws Exception {
        RateLimiterRegistry registry = mock(RateLimiterRegistry.class);
        RateLimiter limiter = mock(RateLimiter.class);
        when(registry.rateLimiter("login:192.0.2.10", "loginConfig")).thenReturn(limiter);
        when(limiter.acquirePermission()).thenReturn(true);

        MockHttpServletRequest request = loginRequest();
        MockFilterChain chain = new MockFilterChain();
        new LoginRateLimitFilter(registry).doFilter(request, new MockHttpServletResponse(), chain);

        assertEquals(request, chain.getRequest());
        verify(limiter).acquirePermission();
    }

    @Test
    void nonLoginRequestSkipsRateLimiter() throws Exception {
        RateLimiterRegistry registry = mock(RateLimiterRegistry.class);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        MockFilterChain chain = new MockFilterChain();

        new LoginRateLimitFilter(registry).doFilter(request, new MockHttpServletResponse(), chain);

        assertEquals(request, chain.getRequest());
        verify(registry, never()).rateLimiter("login:192.0.2.10", "loginConfig");
    }

    private MockHttpServletRequest loginRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
        request.setServletPath("/login");
        request.setRemoteAddr("192.0.2.10");
        return request;
    }
}
