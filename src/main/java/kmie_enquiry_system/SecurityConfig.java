package kmie_enquiry_system;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .csrf(csrf -> csrf.ignoringRequestMatchers(
                "/h2-console/**", "/api/enquiries", "/api/bookings"))
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/enquiry", "/css/**",
                        "/js/*", "/images/*",
                        "/counsellor/login", "/login").permitAll()
                    .requestMatchers(HttpMethod.GET,
                        "/api/enquiries/list",
                        "/api/bookings/list").permitAll()
                    .requestMatchers(HttpMethod.POST,
                        "/api/enquiries",
                        "/api/bookings").permitAll()
                    .requestMatchers("/admin/*", "/api/*")
                        .hasRole("COUNSELLOR")
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/counsellor/login")
                .loginProcessingUrl("/login")
                .failureUrl("/counsellor/login?error")
                .defaultSuccessUrl("/admin/enquiries", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(
            PasswordEncoder encoder,
            @Value("${ADMIN_PASSWORD}") String adminPassword) {

        return new InMemoryUserDetailsManager(
            User.withUsername("counsellor")
                .password(encoder.encode(adminPassword))
                .roles("COUNSELLOR")
                .build()
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}