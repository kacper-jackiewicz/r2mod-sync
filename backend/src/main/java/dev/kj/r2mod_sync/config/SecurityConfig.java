package dev.kj.r2mod_sync.config;

import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import java.util.function.Supplier;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, RateLimiterRegistry registry) {
        LoginRateLimitFilter loginFilter =
                new LoginRateLimitFilter(registry);
        return http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new EagerCsrfTokenRequestHandler()))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/", "/login", "/assets/**", "/favicon.ico").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/")
                        .loginProcessingUrl("/login")
                        .usernameParameter("text")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/?page=server", true)
                        .failureUrl("/?page=login&error"))
                .addFilterBefore(
                        loginFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/?page=login&logout"))

                .build();
    }

    private static final class EagerCsrfTokenRequestHandler extends CsrfTokenRequestAttributeHandler {
        @Override
        public void handle(
                @NonNull HttpServletRequest request,
                @NonNull HttpServletResponse response,
                Supplier<CsrfToken> deferredCsrfToken) {
            deferredCsrfToken.get();
            super.handle(request, response, deferredCsrfToken);
        }
    }

    @Bean
    UserDetailsService userDetailsService(
            @Value("${security.email:}") String username,
            @Value("${security.password:}") String password,
            PasswordEncoder passwordEncoder) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new IllegalStateException("Set app.security.email and app.security.password");
        }

        return new InMemoryUserDetailsManager(User.withUsername(username)
                .password(passwordEncoder.encode(password))
                .roles("USER")
                .build());
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
