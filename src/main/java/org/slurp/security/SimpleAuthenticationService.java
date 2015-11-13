package org.slurp.security;

import javax.security.auth.Subject;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.codec.Base64;

public class SimpleAuthenticationService implements Processor {

	public void process(Exchange exchange) throws Exception {

		String header = exchange.getIn().getHeader("Authorization",
				String.class);
		if (header != null && !header.isEmpty()) {
			header = header.replace("Basic ", "");

			byte[] headerBytes = header.getBytes("UTF-8");
			String userpass = new String(Base64.decode(headerBytes), "UTF-8");
			String[] tokens = userpass.split(":");

			// create an Authentication object
			UsernamePasswordAuthenticationToken authToken = null;
			if (tokens.length == 2) {
				authToken = new UsernamePasswordAuthenticationToken(tokens[0],
						tokens[1]);
			} else {
				authToken = new UsernamePasswordAuthenticationToken(tokens[0],
						null);
			}

			// wrap it in a Subject
			Subject subject = new Subject();
			subject.getPrincipals().add(authToken);

			// place the Subject in the In message
			exchange.getIn().setHeader(Exchange.AUTHENTICATION, subject);

			// you could also do this if useThreadSecurityContext is set to true
			SecurityContextHolder.getContext().setAuthentication(authToken);
		}else{
			throw new AccessDeniedException("Authentatication is required");
		}
	}

}
