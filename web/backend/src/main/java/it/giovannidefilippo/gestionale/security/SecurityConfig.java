package it.giovannidefilippo.gestionale.security;

import it.giovannidefilippo.gestionale.common.ApiErrorCode;
import it.giovannidefilippo.gestionale.user.SessionCookies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableMethodSecurity
class SecurityConfig {
    private final SessionAuthenticationFilter sessionAuthenticationFilter;
    private final SessionCookies sessionCookies;
    private final SecurityErrorWriter errorWriter;

    SecurityConfig(SessionAuthenticationFilter sessionAuthenticationFilter, SessionCookies sessionCookies, SecurityErrorWriter errorWriter) {
        this.sessionAuthenticationFilter = sessionAuthenticationFilter;
        this.sessionCookies = sessionCookies;
        this.errorWriter = errorWriter;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("Autenticazione gestita tramite token di sessione.");
        };
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(sessionCookies.csrfTokenRepository())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        // Il token CSRF ruota su login e rinnovo (SessionCookies), non a ogni richiesta autenticata dal filtro.
                        .sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy())
                        .ignoringRequestMatchers("/api/accounts/login", "/api/accounts/register", "/api/accounts/password-strength", "/actuator/**"))
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.deny()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) -> errorWriter.write(request, response, HttpStatus.UNAUTHORIZED, ApiErrorCode.AUTH_UNAUTHORIZED, "Sessione mancante o non valida."))
                        .accessDeniedHandler((request, response, exception) -> {
                            if (exception instanceof CsrfException) {
                                errorWriter.write(request, response, HttpStatus.FORBIDDEN, ApiErrorCode.AUTH_CSRF_INVALID, "Protezione della richiesta mancante o non valida. Ricarica la pagina e riprova.");
                                return;
                            }
                            errorWriter.write(request, response, HttpStatus.FORBIDDEN, ApiErrorCode.AUTH_FORBIDDEN, "Permessi insufficienti per questa operazione.");
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/accounts/login", "/api/accounts/register", "/api/accounts/password-strength").permitAll()
                        .requestMatchers("/actuator/health/liveness", "/actuator/health/readiness").permitAll()
                        .requestMatchers("/actuator/prometheus").permitAll()
                        .requestMatchers("/actuator/**").denyAll()
                        .requestMatchers(HttpMethod.GET, "/api/accounts/session").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/accounts/logout").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/accounts/session/renew").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/accounts/me/password").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/customer/catalog/**").hasAuthority("VIEW_CATALOG")
                        .requestMatchers(HttpMethod.GET, "/api/customer/dashboard").hasAuthority("VIEW_ORDERS")
                        .requestMatchers(HttpMethod.GET, "/api/vat-rates").hasAuthority("MANAGE_INVENTORY")
                        .requestMatchers("/api/vat-rates/**").denyAll()
                        .requestMatchers(HttpMethod.GET, "/api/product-categories").hasAuthority("VIEW_CATALOG")
                        .requestMatchers(HttpMethod.POST, "/api/product-categories").hasAuthority("MANAGE_PRODUCTS")
                        .requestMatchers(HttpMethod.PUT, "/api/product-categories/*").hasAuthority("MANAGE_PRODUCTS")
                        .requestMatchers("/api/product-categories/**").denyAll()
                        .requestMatchers(HttpMethod.GET, "/api/product-attributes").hasAuthority("VIEW_CATALOG")
                        .requestMatchers(HttpMethod.POST, "/api/product-attributes").hasAuthority("MANAGE_PRODUCTS")
                        .requestMatchers(HttpMethod.PUT, "/api/product-attributes/*").hasAuthority("MANAGE_PRODUCTS")
                        .requestMatchers("/api/product-attributes/**").denyAll()
                        .requestMatchers(HttpMethod.GET, "/api/units-of-measure").hasAuthority("VIEW_CATALOG")
                        .requestMatchers(HttpMethod.POST, "/api/units-of-measure").hasAuthority("MANAGE_PRODUCTS")
                        .requestMatchers(HttpMethod.PUT, "/api/units-of-measure/*").hasAuthority("MANAGE_PRODUCTS")
                        .requestMatchers("/api/units-of-measure/**").denyAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**").hasAuthority("MANAGE_INVENTORY")
                        .requestMatchers(HttpMethod.GET, "/api/dashboard").hasAuthority("VIEW_REPORTS")
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasAuthority("MANAGE_PRODUCTS")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAuthority("MANAGE_PRODUCTS")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasAuthority("MANAGE_PRODUCTS")
                        .requestMatchers(HttpMethod.GET, "/api/partners/**").hasAuthority("VIEW_PARTNERS")
                        .requestMatchers(HttpMethod.POST, "/api/partners/**").hasAuthority("MANAGE_PARTNERS")
                        .requestMatchers(HttpMethod.PUT, "/api/partners/**").hasAuthority("MANAGE_PARTNERS")
                        .requestMatchers(HttpMethod.DELETE, "/api/partners/**").hasAuthority("MANAGE_PARTNERS")
                        .requestMatchers("/api/inventory/**").hasAuthority("MANAGE_INVENTORY")
                        .requestMatchers(HttpMethod.GET, "/api/orders/**").hasAuthority("VIEW_ORDERS")
                        .requestMatchers(HttpMethod.POST, "/api/orders/*/confirm").hasAuthority("CONFIRM_ORDERS")
                        .requestMatchers(HttpMethod.POST, "/api/orders/*/fulfill").hasAuthority("FULFILL_ORDERS")
                        .requestMatchers(HttpMethod.POST, "/api/orders/*/cancel").hasAuthority("CANCEL_ORDERS")
                        .requestMatchers(HttpMethod.POST, "/api/orders").hasAuthority("CREATE_ORDERS")
                        .requestMatchers("/api/documents/**").hasAuthority("MANAGE_DOCUMENTS")
                        .requestMatchers(HttpMethod.GET, "/api/reports/**").hasAuthority("VIEW_REPORTS")
                        .requestMatchers("/api/accounts/**").hasAuthority("MANAGE_ACCOUNTS")
                        .requestMatchers("/api/audit/**").hasAuthority("VIEW_AUDIT")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(sessionAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
