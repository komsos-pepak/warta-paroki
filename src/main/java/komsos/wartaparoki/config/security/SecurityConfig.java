package komsos.wartaparoki.config.security;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import komsos.wartaparoki.config.security.filter.CustomAuthorizationFilter;
import komsos.wartaparoki.config.security.filter.DatabaseLoginSuccessHandler;
import komsos.wartaparoki.config.security.oauth.CustomOAuth2UserService;
import komsos.wartaparoki.config.security.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import komsos.wartaparoki.config.security.oauth.OAuthLoginSuccessHandler;
import komsos.wartaparoki.exception.AccessDeniedHandlerFilter;
import komsos.wartaparoki.exception.AuthenticationLoginException;
import komsos.wartaparoki.exception.CustomAuthenticationFailureHandler;
import komsos.wartaparoki.exception.CustomInternalServerErrorException;
import komsos.wartaparoki.feature.pengguna.PenggunaRepository;
import komsos.wartaparoki.feature.pengguna.PenggunaService;
import komsos.wartaparoki.feature.token.TokenProvider;
import komsos.wartaparoki.utils.CookieUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	@Value("${application.token-secret}")
	private String tokenSecret;

	@Value("${application.allowed-cors}")
	private String allowedCors;

	@Value("${server.servlet.context-path}")
	private String prefix;

	@Value("${application.access-token-cookie-name}")
	private String accessTokenCookieName;

	@Value("${application.security-cipher-key}")
	private String securityCipherKey;

	@Value("${application.cookie-domain}")
	private String cookieDomain;

	@Value("${application.authorized-redirect-uris}")
	private String authorizedRedirectUris;

	private final CustomOAuth2UserService oauth2UserService;

	private final TokenProvider tokenProvider;
	private final PenggunaService penggunaService;
	private final PenggunaRepository penggunaRepository;
	private final CookieUtil cookieUtil;
	
	@Bean
    public HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }

	@Bean
	public CustomAuthorizationFilter customAuthorizationFilter() {
		return new CustomAuthorizationFilter(penggunaService, cookieUtil, tokenSecret, accessTokenCookieName, prefix);
	}

	@Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler();
    }

	@Bean
	public DatabaseLoginSuccessHandler databaseLoginSuccessHandler() {
		return new DatabaseLoginSuccessHandler(securityCipherKey, cookieDomain, tokenProvider, cookieUtil);
	}

	@Bean
	public OAuthLoginSuccessHandler oAuthLoginSuccessHandler() {
		return new OAuthLoginSuccessHandler(securityCipherKey, authorizedRedirectUris, penggunaService,
				penggunaRepository,
				tokenProvider, cookieUtil, cookieAuthorizationRequestRepository());
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		RequestMatcher csrfRequestMatcher = new RequestMatcher() {
			private AntPathRequestMatcher[] disableCsrfMatchers = {
					new AntPathRequestMatcher("/**")
			};

			@Override
			public boolean matches(HttpServletRequest request) {

				for (AntPathRequestMatcher rm : disableCsrfMatchers) {
					if (rm.matches(request)) {
						return false;
					}
				}
				return true;
			}
		};
        http.authorizeHttpRequests().requestMatchers(getWhiteList()).permitAll()
                .and()
                .cors(withDefaults()).csrf(csrf -> csrf.requireCsrfProtectionMatcher(csrfRequestMatcher))
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests().anyRequest().authenticated().and()
                .addFilterBefore(customAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class)
                .formLogin(login -> {
					try {
						login.permitAll()
						.loginPage("/v1/login")
						        .usernameParameter("username")
						        .passwordParameter("password")
						        .successHandler(databaseLoginSuccessHandler())
								.failureHandler(authenticationFailureHandler())
								.and()
								.exceptionHandling(handling -> handling.authenticationEntryPoint(new AuthenticationLoginException()));
					} catch (Exception e) {
						throw new CustomInternalServerErrorException("Failed generate Login", "Internal server error");
					}
				})
                .oauth2Login(login -> {
					try {
						login
							.authorizationEndpoint()
								.baseUri("/oauth2/authorization")
								.authorizationRequestRepository(cookieAuthorizationRequestRepository())
								.and()
							.userInfoEndpoint()
							.userService(oauth2UserService)
							.and()
							.successHandler(oAuthLoginSuccessHandler()).failureHandler(authenticationFailureHandler()).and()
							.exceptionHandling(handling -> handling.authenticationEntryPoint(new AuthenticationLoginException()));
					} catch (Exception e) {
						throw new CustomInternalServerErrorException("Failed generate Login", "Internal server error");
					}
				})
				.exceptionHandling(handling -> handling.accessDeniedHandler(new AccessDeniedHandlerFilter()));
		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList(getAllowedCors()));
		configuration.setAllowCredentials(true);
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
		configuration.setExposedHeaders(Arrays.asList("x-auth-token"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	private String[] getAllowedCors() {
		String[] allowedCorsString = allowedCors.split(",");
		System.out.println("CORS ALLOWED : \n");
		for (String url : allowedCorsString) {
			System.out.println("-" + url + "\n");
		}
		return allowedCorsString;
	}

	private String[] getWhiteList() {
		String[] allowedCorsString = {
				"/v1/pengguna/login",
				"/v1/token/refresh",
				"/v1/pengguna/logout",
				// -- Swagger UI v2
				"/v2/api-docs",
				"/swagger-resources",
				"/swagger-resources/**",
				"/configuration/ui",
				"/configuration/security",
				"/swagger-ui.html",
				"/webjars/**",
				// -- Swagger UI v3 (OpenAPI)
				"/v3/api-docs/**",
				"/swagger-ui/**",
				// -- Actuator
				"/actuator/**",
				// other public endpoints of your API may be appended to this array
				"/v1/pengguna/register",
				"/v1/init"
		};
		return allowedCorsString;
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring().requestMatchers("/v2/api-docs",
				"/configuration/ui",
				"/configuration/**",
				"/swagger-resources/**",
				"/configuration/security",
				"/swagger-ui.html",
				"/webjars/**",
				"/swagger-ui/**");
	}
}
