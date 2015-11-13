package org.slurp;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.camel.spring.boot.CamelSpringBootApplicationController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	/**
	 * Main method, used to run the application.
	 */
	public static void main(String[] args) throws UnknownHostException {
		SpringApplication app = new SpringApplication(Application.class);
		app.setShowBanner(false);
		ConfigurableApplicationContext applicationContext = app.run(args);

		Environment env = applicationContext.getEnvironment();
		log.info("Access URLs:\n----------------------------------------------------------\n\t" + "Local: \t\thttp://127.0.0.1:{}\n\t"
				+ "External: \thttp://{}:{}\n----------------------------------------------------------", env.getProperty("server.port"), InetAddress
				.getLocalHost().getHostAddress(), env.getProperty("server.port"));

		CamelSpringBootApplicationController applicationController = applicationContext.getBean(CamelSpringBootApplicationController.class);
		applicationController.blockMainThread();

	}

}
