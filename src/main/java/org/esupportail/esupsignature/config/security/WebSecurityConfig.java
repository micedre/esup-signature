package org.esupportail.esupsignature.config.security;

import jakarta.annotation.Resource;
import org.esupportail.esupsignature.config.GlobalProperties;
import org.esupportail.esupsignature.config.security.cas.CasProperties;
import org.esupportail.esupsignature.config.security.otp.OtpAuthenticationProvider;
import org.esupportail.esupsignature.config.security.shib.DevClientRequestFilter;
import org.esupportail.esupsignature.config.security.shib.DevShibProperties;
import org.esupportail.esupsignature.config.security.shib.ShibProperties;
import org.esupportail.esupsignature.service.security.*;
import org.esupportail.esupsignature.service.security.cas.CasSecurityServiceImpl;
import org.esupportail.esupsignature.service.security.oauth.CustomAuthorizationRequestResolver;
import org.esupportail.esupsignature.service.security.oauth.OAuthSecurityServiceImpl;
import org.esupportail.esupsignature.service.security.oauth.ValidatingOAuth2UserService;
import org.esupportail.esupsignature.service.security.shib.ShibSecurityServiceImpl;
import org.esupportail.esupsignature.service.security.su.SuAuthenticationSuccessHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.client.ClientsConfiguredCondition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity(debug = false)
@EnableMethodSecurity(
		securedEnabled = true,
		jsr250Enabled = true)
@EnableConfigurationProperties({WebSecurityProperties.class, ShibProperties.class, CasProperties.class, DevShibProperties.class})
public class WebSecurityConfig {

	private static final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);

	private final String apiKey = "SomeKey1234567890";

	private LdapContextSource ldapContextSource;

	@Autowired(required = false)
	public void setLdapContextSource(LdapContextSource ldapContextSource) {
		this.ldapContextSource = ldapContextSource;
	}

	@Resource
	private GlobalProperties globalProperties;

	@Resource
	private WebSecurityProperties webSecurityProperties;

	private final ClientRegistrationRepository clientRegistrationRepository;

	private final List<SecurityService> securityServices = new ArrayList<>();

	private final List<DevSecurityFilter> devSecurityFilters = new ArrayList<>();

	public WebSecurityConfig(@Autowired(required = false) ClientRegistrationRepository clientRegistrationRepository) {
		this.clientRegistrationRepository = clientRegistrationRepository;
	}

	@Bean
	@Order(1)
	@ConditionalOnProperty({"spring.ldap.base", "ldap.search-base", "security.cas.service"})
	public CasSecurityServiceImpl casSecurityServiceImpl() {
		if(ldapContextSource!= null && ldapContextSource.getUserDn() != null) {
			CasSecurityServiceImpl casSecurityService = new CasSecurityServiceImpl();
			securityServices.add(casSecurityService);
			return casSecurityService;
		} else {
			logger.error("cas config found without needed ldap config, cas security will be disabled");
			return null;
		}
	}

	@Bean
	@Order(2)
	@ConditionalOnProperty(prefix = "security.shib", name = "principal-request-header")
	public ShibSecurityServiceImpl shibSecurityServiceImpl() {
		ShibSecurityServiceImpl shibSecurityService = new ShibSecurityServiceImpl();
		securityServices.add(shibSecurityService);
		return shibSecurityService;
	}

	@Bean
	@Order(3)
	@Conditional(ClientsConfiguredCondition.class)
	public OAuthSecurityServiceImpl oAuthSecurityService() {
		OAuthSecurityServiceImpl oAuthSecurityService = new OAuthSecurityServiceImpl();
		securityServices.add(oAuthSecurityService);
		return oAuthSecurityService;
	}

	@Bean
	@Conditional(ClientsConfiguredCondition.class)
	JwtDecoder jwtDecoder() {
		ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("franceconnect");
		SecretKeySpec key = new SecretKeySpec(registration.getClientSecret().getBytes(StandardCharsets.UTF_8), "HS256");
		return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
	}

	@Bean
	@Conditional(ClientsConfiguredCondition.class)
	public JwtDecoderFactory<ClientRegistration> jwtDecoderFactory() {
		final JwtDecoder decoder = jwtDecoder();
		return new JwtDecoderFactory<ClientRegistration>() {
			@Override
			public JwtDecoder createDecoder(ClientRegistration context) {
				return decoder;
			}
		};
	}

	@Bean
	@Order(4)
	@ConditionalOnProperty(prefix = "security.shib.dev", name = "enable", havingValue = "true")
	public DevClientRequestFilter devClientRequestFilter() {
		DevClientRequestFilter devClientRequestFilter = new DevClientRequestFilter();
		devSecurityFilters.add(devClientRequestFilter);
		return devClientRequestFilter;
	}

//	@Bean
//	public WebSecurityCustomizer webSecurityCustomizer() {
//		return (web) -> web.ignoring().requestMatchers("/resources/**", "/webjars/**")
//				.requestMatchers("/logged-out")
//				.requestMatchers("/webjars", "/webjars/**")
//				.requestMatchers("/css", "/css/**")
//				.requestMatchers("/images", "/images/**")
//				.requestMatchers("/js", "/js/**")
//				.requestMatchers("/fonts", "/fonts/**");
//	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests.requestMatchers("/").permitAll());
		devSecurityFilters.forEach(devSecurityFilter -> http.addFilterBefore(devSecurityFilter, OAuth2AuthorizationRequestRedirectFilter.class));
		http.exceptionHandling(exceptionHandling -> exceptionHandling.defaultAuthenticationEntryPointFor(new IndexEntryPoint("/"), new AntPathRequestMatcher("/")));
		for(SecurityService securityService : securityServices) {
			http.authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests.requestMatchers(securityService.getLoginUrl()).authenticated());
			http.exceptionHandling(exceptionHandling -> exceptionHandling.defaultAuthenticationEntryPointFor(securityService.getAuthenticationEntryPoint(), new AntPathRequestMatcher(securityService.getLoginUrl())));
			if(securityService.getClass().equals(OAuthSecurityServiceImpl.class)) {
				http.oauth2Login(oauth2Login -> oauth2Login.loginPage("/"))
						.oauth2Login(oauth2Login -> oauth2Login.successHandler(((OAuthSecurityServiceImpl) securityService).getoAuthAuthenticationSuccessHandler())
								.userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
										.userService(new ValidatingOAuth2UserService(jwtDecoder()))))
						.oauth2Login(oauth2Login -> oauth2Login
								.authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint
										.authorizationRequestResolver(new CustomAuthorizationRequestResolver(clientRegistrationRepository, webSecurityProperties.getFranceConnectAcr()))));

			} else {
				http.addFilterBefore(securityService.getAuthenticationProcessingFilter(), OAuth2AuthorizationRequestRedirectFilter.class);
			}
		}
		if(globalProperties.getEnableSu()) {
			for (SecurityService securityService : securityServices) {
				if (securityService instanceof CasSecurityServiceImpl) {
					switchUserFilter().setUserDetailsService(securityService.getUserDetailsService());
				} else if (securityService instanceof ShibSecurityServiceImpl) {
					switchUserFilter().setUserDetailsService(securityService.getUserDetailsService());
					break;
				}
			}
		}
		http.logout(logout -> logout.invalidateHttpSession(true)
						.logoutRequestMatcher(
								new AntPathRequestMatcher("/logout")
						));
		http.logout(logout -> logout.addLogoutHandler(logoutHandler())
				.logoutSuccessUrl("/login").permitAll());
		http.csrf(csrf -> csrf.ignoringRequestMatchers("/resources/**")
				.ignoringRequestMatchers("/webjars/**")
				.ignoringRequestMatchers("/ws/**")
				.ignoringRequestMatchers("/user/nexu-sign/**")
				.ignoringRequestMatchers("/otp-access/**")
				.ignoringRequestMatchers("/log/**")
				.ignoringRequestMatchers("/actuator/**")
				.ignoringRequestMatchers("/h2-console/**"))
				;
		http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
		setAuthorizeRequests(http);
		return http.build();
	}

//	@Bean
//	public APIKeyFilter apiKeyFilter() {
//		APIKeyFilter filter = new APIKeyFilter();
//		filter.setAuthenticationManager(authentication -> {
//			if(authentication.getPrincipal() == null) {
//				throw new BadCredentialsException("Access Denied.");
//			}
//			String apiKey = (String) authentication.getPrincipal();
//			if (authentication.getPrincipal() != null && this.apiKey.equals(apiKey)) {
//				Collection<SimpleGrantedAuthority> oldAuthorities = (Collection<SimpleGrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities();
//				SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_WS");
//				List<SimpleGrantedAuthority> updatedAuthorities = new ArrayList<>();
//				updatedAuthorities.add(authority);
//				updatedAuthorities.addAll(oldAuthorities);
//				SecurityContextHolder.getContext().setAuthentication(
//						new UsernamePasswordAuthenticationToken(
//								SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
//								SecurityContextHolder.getContext().getAuthentication().getCredentials(),
//								updatedAuthorities));
//				return SecurityContextHolder.getContext().getAuthentication();
//			} else {
//				throw new BadCredentialsException("Access Denied.");
//			}
//		});
//		return filter;
//	}

	private void setAuthorizeRequests(HttpSecurity http) throws Exception {
		http.logout(logout -> logout.logoutSuccessUrl("/").permitAll());
		AccessDeniedHandlerImpl accessDeniedHandlerImpl = new AccessDeniedHandlerImpl();
		accessDeniedHandlerImpl.setErrorPage("/denied");
		http.exceptionHandling(exceptionHandling -> exceptionHandling.accessDeniedHandler(accessDeniedHandlerImpl));
		String hasIpAddresses = "";
		int nbIps = 0;
		if(webSecurityProperties.getWsAccessAuthorizeIps() != null) {
			for (String ip : webSecurityProperties.getWsAccessAuthorizeIps()) {
				nbIps++;
				hasIpAddresses += "hasIpAddress('"+ ip +"')";
				if(nbIps < webSecurityProperties.getWsAccessAuthorizeIps().length) {
					hasIpAddresses += " or ";
				}
			}
			String finalHasIpAddresses = hasIpAddresses;
			http.authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests.requestMatchers("/ws/**").access(new WebExpressionAuthorizationManager(finalHasIpAddresses)));
			http.authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests.requestMatchers("/actuator/**").access(new WebExpressionAuthorizationManager(finalHasIpAddresses)));
//			http.authorizeRequests().requestMatchers("/ws/**").access("hasRole('WS')").and().addFilter(apiKeyFilter());
		} else {
			http.authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests.requestMatchers("/ws/**").denyAll());
			http.authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests.requestMatchers("/actuator/**").denyAll());
		}
		http.authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
				.requestMatchers("/api-docs/", "/api-docs/**").hasAnyRole("ADMIN")
				.requestMatchers("/swagger-ui.html", "/swagger-ui/", "/swagger-ui/**").hasAnyRole("ADMIN")
				.requestMatchers("/admin/", "/admin/**").hasAnyRole("ADMIN", "MANAGER")
				.requestMatchers("/user/", "/user/**").hasAnyRole("USER")
				.requestMatchers("/otp/", "/otp/**").hasAnyRole("OTP", "FRANCECONNECT")
				.requestMatchers("/ws-secure/", "/ws-secure/**").hasAnyRole("USER", "OTP", "FRANCECONNECT")
				.anyRequest().permitAll());
	}

	@Bean
	public LogoutHandlerImpl logoutHandler() {
		return new LogoutHandlerImpl();
	}

	@Bean
	public SessionRegistryImpl sessionRegistry() {
		return new SessionRegistryImpl();
	}

	@Bean
	@ConditionalOnProperty(value="global.enable-su",havingValue = "true")
	public SwitchUserFilter switchUserFilter() {
		SwitchUserFilter switchUserFilter = new SwitchUserFilter();
		switchUserFilter.setUserDetailsService(userDetailsService());
		switchUserFilter.setSwitchUserUrl("/admin/su-login");
		switchUserFilter.setExitUserUrl("/su-logout");
		switchUserFilter.setSuccessHandler(new SuAuthenticationSuccessHandler());
		switchUserFilter.setFailureHandler(new ExceptionMappingAuthenticationFailureHandler());
		return switchUserFilter;
	}

	@Bean
	public AuthenticationManager authenticationManagerBean() {
		return new ProviderManager(List.of(new OtpAuthenticationProvider()));
	}

	@Bean
	public UserDetailsService userDetailsService() {
		return new InMemoryUserDetailsManager();
	}

	@Bean
	public SpelGroupService spelGroupService() {
		SpelGroupService spelGroupService = new SpelGroupService(globalProperties);
		Map<String, String> groups4eppnSpel = new HashMap<>();
		if (webSecurityProperties.getGroupMappingSpel() != null) {
			for (String groupName : webSecurityProperties.getGroupMappingSpel().keySet()) {
				String spelRule = webSecurityProperties.getGroupMappingSpel().get(groupName);
				groups4eppnSpel.put(groupName, spelRule);
			}
		}
		spelGroupService.setGroups4eppnSpel(groups4eppnSpel);
		return spelGroupService;
	}

}
