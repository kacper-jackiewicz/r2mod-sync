package dev.kacperjackiewicz.r2mod_sync.config;

import io.micrometer.common.util.StringUtils;
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

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/", "/login.html", "/login", "/css/**", "/js/**", "/i18n/**", "/images/**").permitAll()
						.anyRequest().authenticated())
				.formLogin(form -> form
						.loginPage("/login.html")
						.loginProcessingUrl("/login")
						.usernameParameter("text")
						.passwordParameter("password")
						.defaultSuccessUrl("/server.html", true)
						.failureUrl("/login.html?error"))
				.logout(logout -> logout
						.logoutSuccessUrl("/login.html?logout"))
				.build();
	}

	@Bean
	UserDetailsService userDetailsService(
			@Value("${app.security.email:}") String username,
			@Value("${app.security.password:}") String password,
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
