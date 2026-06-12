package com.study.neighbortrade.config;

import com.study.neighbortrade.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final ObjectProvider<CustomOAuth2UserService> customOAuth2UserService;
    private final ObjectProvider<ClientRegistrationRepository> clientRegistrationRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**", "/h2-console/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/ws/**").authenticated()
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                .requestMatchers("/", "/member/join", "/member/login", "/market/list", "/market/detail/**", "/api/neighborhoods", "/browse/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated());
        http.formLogin(login -> login
                .loginPage("/member/login")
                .loginProcessingUrl("/member/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/market/list", true)
                .failureUrl("/member/login?error=true"));
        http.logout(logout -> logout
                .logoutUrl("/member/logout")
                .logoutSuccessUrl("/market/list")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID"));
        if (clientRegistrationRepository.getIfAvailable() != null) {
            http.oauth2Login(oauth -> oauth
                    .loginPage("/member/login")
                    .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService.getObject()))
                    .defaultSuccessUrl("/market/list", true)
                    .failureUrl("/member/login?error=true"));
        }
        http.exceptionHandling(ex -> ex
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    if (request.getUserPrincipal() != null) {
                        response.sendRedirect(request.getContextPath() + "/market/list");
                        return;
                    }
                    response.sendRedirect(request.getContextPath() + "/member/login?denied");
                }));
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/ws/**"));
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }
}
