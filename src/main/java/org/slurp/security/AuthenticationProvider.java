package org.slurp.security;

import static java.util.Arrays.asList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

public class AuthenticationProvider implements
		org.springframework.security.authentication.AuthenticationProvider {

	private final Logger log = LoggerFactory
			.getLogger(AuthenticationProvider.class);

	private UserDetailsService userDetailsService = new InMemoryUserDetailsManager(
			asList(new User("admin", "admin",
					asList(new SimpleGrantedAuthority("ROLE_ADMIN")))));

	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;

		String login = token.getName();
		UserDetails user = userDetailsService.loadUserByUsername(login);
		if (user == null) {
			throw new UsernameNotFoundException("User does not exists");
		}
		String password = user.getPassword();
		String tokenPassword = (String) token.getCredentials();
		if (tokenPassword == null
				|| (tokenPassword != null && !tokenPassword.equals(password))) {
			throw new BadCredentialsException("Invalid username/password");
		}
		return new UsernamePasswordAuthenticationToken(user, password,
				user.getAuthorities());
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.equals(authentication);
	}
}
