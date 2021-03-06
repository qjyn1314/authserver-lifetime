package com.authserver.life.security;

import com.authserver.common.filter.JwtAuthenticationFilter;
import com.authserver.life.security.handler.sso.SsoFailureHandler;
import com.authserver.life.security.handler.sso.SsoSuccessHandler;
import com.authserver.life.security.sso.CaptchaAuthenticationDetailsSource;
import com.authserver.life.security.sso.UsernamePasswordAuthenticationProvider;
import com.authserver.life.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class DefaultSecurityConfig {

    @Autowired
    private RedisTemplate<String, Object> redisHelper;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserService userService;
    @Autowired
    private OAuth2AuthorizationService authorizationService;
    @Autowired
    private RegisteredClientRepository registeredClientService;

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http,
                                                          CaptchaAuthenticationDetailsSource authenticationDetailsSource,
                                                          JwtAuthenticationFilter jwtAuthenticationFilter,
                                                          AuthenticationProvider usernamePasswordProvider)
            throws Exception {
        http
                // ??????csrf-??????csrf??????-?????????https://blog.csdn.net/yjclsx/article/details/80349906
                .csrf().disable()
                // ??????session
                /*
                Spring Security????????????SessionCreationPolicy,??????session???????????????
                ALWAYS
                    ????????????HttpSession
                IF_REQUIRED
                    Spring Security??????????????????????????????HttpSession
                NEVER
                    Spring Security????????????HttpSession?????????????????????????????????????????????HttpSession
                STATELESS
                    Spring Security??????????????????HttpSession??????????????????HttpSession?????????SecurityContext
                 */
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)

                .and()
                .authorizeRequests()
                // ????????????????????????
                // swagger??????
                .antMatchers("/v2/api-docs").permitAll()
                // ??????public??????
                .antMatchers("/public/**").permitAll()
                .antMatchers("/actuator/**").permitAll()
                .antMatchers("/auth/**").permitAll()
                .antMatchers("/druid/**").permitAll()
                .antMatchers("/login/**").permitAll()

                // ???????????????????????????????????????????????????
                .anyRequest().authenticated()

                .and()
                .logout()
                .logoutUrl("/auth/logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
//                .addLogoutHandler(new SsoLogoutHandle(authorizationService, redisHelper))

                .and()
                .formLogin()
                .loginProcessingUrl(SecurityConstant.SSO_LOGIN)
                .authenticationDetailsSource(authenticationDetailsSource)
                .successHandler(new SsoSuccessHandler())
                .failureHandler(new SsoFailureHandler());
//        http.apply()//?????????????????????????????????????????????
        // ???????????????????????????????????????
        http.authenticationProvider(usernamePasswordProvider);
        // ??????jwtfilter  ?????????????????? jwtAuthenticationFilter -> UsernamePasswordFilter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * ????????????????????????????????????????????????
     */
    @Bean
    public CaptchaAuthenticationDetailsSource authenticationDetailsSource() {
        return new CaptchaAuthenticationDetailsSource();
    }

    /**
     * ????????????????????????????????????
     */
    @Bean
    public AuthenticationProvider usernamePasswordProvider() {
        return new UsernamePasswordAuthenticationProvider(userDetailsService, passwordEncoder,
                redisHelper, userService, registeredClientService);
    }

//    @Bean
//    public AuthenticationConverter authenticationConverter() {
//        return new OAuth2ImplicitAuthenticationConverter();
//    }

}
