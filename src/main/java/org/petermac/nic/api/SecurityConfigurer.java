package org.petermac.nic.api;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;

/**
 * Created by Nick Kravchenko on 25/06/2018.
 */
//@Configuration
@EnableWebSecurity
//@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true, proxyTargetClass = true)
//@EnableWebMVC
public class SecurityConfigurer extends WebSecurityConfigurerAdapter
{
    private Logger log = org.slf4j.LoggerFactory.getLogger(getClass());

    protected void configure(HttpSecurity http) throws Exception
    {
        log.info("SecurityConfigurer configure called http={}", http);
        http.csrf().disable();

        http.authorizeRequests().anyRequest().authenticated();
        http.authorizeRequests().antMatchers("/v1/**").hasRole("USER").and().formLogin();
//        http.authorizeRequests().antMatchers("/**").authenticated().and().httpBasic();

        http.httpBasic().init(http);
//        http.httpBasic().disable();
//        http.formLogin().disable();
//TODO Disable for PROD?
        http.requestMatcher(EndpointRequest.toAnyEndpoint()).authorizeRequests().anyRequest().permitAll();
    }


    @Autowired
    public void configureGlobalSecurity(AuthenticationManagerBuilder auth) throws Exception
    {
        auth.inMemoryAuthentication().withUser("user").password(passwordEncoder().encode("password")).roles("USER").and()
                .withUser("admin").password(passwordEncoder().encode("password")).roles("USER", "ADMIN");
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }
//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception
//    {
    //TO use  jpa
//        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoderProvider.getPasswordEncoder());
//OR BYO
//        auth.authenticationProvider(new MyCustomAuthProvider());

    // enable in memory based authentication with a user named
    // "user" and "admin"
//        auth.inMemoryAuthentication().withUser("user").password("password").roles("USER").and()
//                .withUser("admin").password("password").roles("USER", "ADMIN");
//        userDetailsService = auth.getDefaultUserDetailsService();
//        auth.inMemoryAuthentication().withUser("user").password(passwordEncoder().encode("password")).roles("USER").and()
//                .withUser("admin").password(passwordEncoder().encode("password")).roles("USER", "ADMIN");
//    }

    public void configure(WebSecurity web) throws Exception
    {
        log.info("SecurityConfigurer WebSecurity called web={}", web);

        web.ignoring().antMatchers("/VAADIN/**"
                ,"/error"
//                ,"/resources/**"// Spring Security should completely ignore URLs starting with /resources/
                ,"/v1/annotate/**"// Unauthorized issues ?
                , "/api", "/api/", "/api/**");
        final StrictHttpFirewall strictHttpFirewall = new StrictHttpFirewall();
        strictHttpFirewall.setAllowSemicolon(true);
        strictHttpFirewall.setAllowUrlEncodedPeriod(true);
//        final HttpFirewall httpFirewall = new DefaultHttpFirewall();
        web.httpFirewall(strictHttpFirewall);
    }

    @Configuration
    public class WebConfig implements WebMvcConfigurer
    {

        @Override
        public void configurePathMatch(PathMatchConfigurer configurer)
        {
            log.info("SecurityConfigurer configurePathMatch called configurer={}", configurer);

            UrlPathHelper urlPathHelper = new UrlPathHelper();
            urlPathHelper.setRemoveSemicolonContent(false);//For MatrixVariables
            configurer.setUrlPathHelper(urlPathHelper);
        }
    }

}
