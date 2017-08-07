/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.dataflow.dsl.support;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.dataflow.dsl.java.SCDFDSLConfiguration;
import org.springframework.cloud.dataflow.rest.client.AppRegistryOperations;
import org.springframework.cloud.dataflow.rest.client.DataFlowTemplate;
import org.springframework.cloud.dataflow.server.EnableDataFlowServer;
import org.springframework.context.ApplicationContext;

/**
 * A simple convenience utility to start Local server
 *
 *  LocalServer.start()
 *
 *
 * @author Oleg Zhurakousky
 *
 */
@SpringBootApplication
@EnableDataFlowServer
@EnableConfigurationProperties(SCDFDSLConfiguration.class)
public class LocalServer  {

	private static Logger logger = LoggerFactory.getLogger(LocalServer.class);

	public final static AtomicBoolean running = new AtomicBoolean();

	private static ApplicationContext context;

	public static void start(String... args) {
		try {
			Class.forName("org.springframework.cloud.dataflow.autoconfigure.local.LocalDataFlowServerAutoConfiguration");
			context = SpringApplication.run(LocalServer.class, args);
			SCDFDSLConfiguration configuration = context.getBean(SCDFDSLConfiguration.class);
//			prepareServer(configuration);
		}
		catch (Exception e) {
			String message = "\nCan not start local server since LocalDataFlowServerAutoConfiguration is not available on classpath."
					+ "\nConsider adding 'org.springframework.cloud:spring-cloud-starter-dataflow-server-local' to your classpath";
			logger.error(message);
			throw new RuntimeException(message);
		}
	}


	public static void stop(){
		SpringApplication.exit(context);
		running.set(false);
	}

	private static void prepareServer(SCDFDSLConfiguration configuration) {
		DataFlowTemplate dataFlowOperationsTemplate = SCDFDSLUtils.createDataFlowTemplate(configuration.getServerUri());
		AppRegistryOperations appRegistryOperations = dataFlowOperationsTemplate.appRegistryOperations();
		appRegistryOperations.importFromResource(configuration.getStreamRegistrationResource(), true);
		appRegistryOperations.importFromResource(configuration.getTaskRegistrationResource(), true);
	}
}
