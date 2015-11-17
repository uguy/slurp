package org.slurp.config;

import static org.apache.camel.model.rest.RestParamType.body;
import static org.apache.camel.model.rest.RestParamType.path;

import java.util.Collections;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.spring.security.SpringSecurityAuthorizationPolicy;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slurp.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.boot.actuate.endpoint.Endpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.util.StringUtils;

@Configuration
@AutoConfigureAfter({ EndpointAutoConfiguration.class })
@EnableConfigurationProperties
public class RestConfig extends RouteBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(RestConfig.class);

	@Autowired
	private List<Endpoint<?>> endpoints = Collections.emptyList();

	@Autowired
	private ManagementServerProperties management;

	@Autowired
	SpringSecurityAuthorizationPolicy adminPolicy;

	@Override
	public void configure() throws Exception {

		LOG.info("Configure rest server");

		// configure we want to use netty as the component for the rest DSL
		// and we enable json binding mode
		restConfiguration().component("netty4-http").bindingMode(RestBindingMode.json)
				.enableCORS(true)
				// and output using pretty print
				.dataFormatProperty("prettyPrint", "true")
				// setup context path and port number that netty will use
				.contextPath("/").port(8080).host("0.0.0.0")
				// add swagger api-doc out of the box
				.apiContextPath("/api-doc").apiProperty("api.title", "Slurp API")
				.apiProperty("api.version", "1.0.0")
				// and enable CORS
				.apiProperty("cors", "true");

		LOG.info("Configure User rest routes");

		// this REST service is json only
		RestDefinition restDefinition = rest().description("User rest service")
				.consumes("application/json").produces("application/json");

		configureUserRoutes(restDefinition);
		// configureGoodsRoutes(restDefinition);
		configureManagementRoutes(restDefinition);

		onException(AccessDeniedException.class).handled(true).process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 401);
			}
		});
		onException(BadCredentialsException.class).handled(true).process(new Processor() {
			@Override
			public void process(Exchange exchange) throws Exception {
				exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 403);
			}
		});

	}

	private RestDefinition configureManagementRoutes(RestDefinition restDefinition) {
		LOG.info("Configure Management rest routes");
		String prefix = management.getContextPath();
		if (StringUtils.hasText(prefix)) {
			prefix = prefix.endsWith("/") ? prefix : prefix + "/";
		}
		else {
			prefix = "/";
		}

		for (Endpoint<?> endpoint : endpoints) {
			if (endpoint.isEnabled()) {
				LOG.debug("Register {} endpoint", prefix + endpoint.getId());
				restDefinition.description(endpoint.getId() + "endpoint")
						.get(prefix + endpoint.getId()).route()
						.routeId(endpoint.getId() + "-get")
						.process("authenticationService").policy(adminPolicy)
						.process(new Processor() {
							@Override
							public void process(Exchange exchange) throws Exception {
								exchange.getIn().setBody(endpoint.invoke());
							}
						}).endRest();
			}
		}
		return restDefinition;
	}

	// private RestDefinition configureGoodsRoutes(RestDefinition
	// restDefinition) {
	//
	// LOG.info("Configure Goods rest routes");
	// restDefinition.get("/{id}").description("Find Goods by id").outType(String.class).param().name("id").type(path)
	// .description("The id of the Goods to get").dataType("int").endParam().route().routeId("goods-get")
	// .to("bean:goodsService?method=get(${header.id})").endRest()
	//
	// .post().description("Creates a Goods").type(String.class).param().name("body").type(body).description("The Goods to create")
	// .endParam().route().routeId("goods-post").to("bean:goodsService?method=create").endRest()
	//
	// .put().description("Updates a Goods").type(String.class).param().name("body").type(body).description("The Goods to update")
	// .endParam().route().routeId("goods-put").to("bean:goodsService?method=update").endRest()
	//
	// .get("/goods").description("Find all Goods").outTypeList(String.class).route().routeId("goods-list")
	// .to("bean:goodsService?method=list").endRest();
	// return restDefinition;
	// }

	private RestDefinition configureUserRoutes(RestDefinition restDefinition) {
		LOG.info("Configure User rest routes");
		restDefinition.get("/users/{id}").description("Find user by id")
				.outType(User.class).param().name("id").type(path)
				.description("The id of the user to get").dataType("int").endParam()
				.route().routeId("user-get")
				.to("bean:userService?method=getUser(${header.id})").endRest()

				.post("/users").description("Creates a user").type(User.class).param()
				.name("body").type(body).description("The user to create").endParam()
				.route().routeId("user-post").to("bean:userService?method=createUser")
				.endRest()

				.put("/users").description("Updates a user").type(User.class).param()
				.name("body").type(body).description("The user to update").endParam()
				.route().routeId("user-put").to("bean:userService?method=updateUser")
				.endRest()

				.get("/users/").description("Find all users").outTypeList(User.class)
				.route().routeId("user-list").to("bean:userService?method=listUsers")
				.endRest();

		return restDefinition;
	}
}
