package org.slurp.config;

import org.apache.camel.CamelContext;
import org.apache.camel.component.metrics.routepolicy.MetricsRoutePolicyFactory;
import org.apache.camel.impl.ExplicitCamelContextNameStrategy;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.codahale.metrics.MetricRegistry;

@Configuration
public class CamelConfig {

	private static final Logger LOG = LoggerFactory.getLogger(CamelConfig.class);

	@Autowired
	private MetricRegistry metricRegistry;

	@Bean
	CamelContextConfiguration contextConfiguration() {
		return new CamelContextConfiguration() {

			@Override
			public void beforeApplicationStart(CamelContext context) {

				context.setNameStrategy(new ExplicitCamelContextNameStrategy(
						"slurp-context"));

				LOG.info("Configuring camel metrics on all routes");
				MetricsRoutePolicyFactory fac = new MetricsRoutePolicyFactory();
				fac.setMetricsRegistry(metricRegistry);
				context.addRoutePolicyFactory(fac);
			}
		};
	}

}
