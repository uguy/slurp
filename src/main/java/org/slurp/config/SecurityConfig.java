package org.slurp.config;
import static java.util.Arrays.asList;

import java.util.List;

import org.apache.camel.component.spring.security.SpringSecurityAccessPolicy;
import org.apache.camel.component.spring.security.SpringSecurityAuthorizationPolicy;
import org.slurp.security.SimpleAuthenticationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;

@Configuration
public class SecurityConfig {

	@Bean
	public AffirmativeBased accessDecisionManager() {
		AffirmativeBased accessDecisionManager = new AffirmativeBased(asList(new RoleVoter()));
		accessDecisionManager.setAllowIfAllAbstainDecisions(true);
		return accessDecisionManager;
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		return new org.slurp.security.AuthenticationProvider();
	}

	@Bean
	public AuthenticationManager authenticationManager() {
		List<AuthenticationProvider> providers = asList(authenticationProvider());
		return new ProviderManager(providers);
	}

	@Bean
	public SpringSecurityAuthorizationPolicy adminPolicy() {
		SpringSecurityAuthorizationPolicy policy = new SpringSecurityAuthorizationPolicy();
		policy.setId("admin");
		policy.setSpringSecurityAccessPolicy(new SpringSecurityAccessPolicy("ROLE_ADMIN"));
		policy.setAccessDecisionManager(accessDecisionManager());
		policy.setAuthenticationManager(authenticationManager());
		return policy;
	}

	@Bean
	public SimpleAuthenticationService authenticationService() {
		return new SimpleAuthenticationService();
	}
}
