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
package org.springframework.cloud.dataflow.dsl.java;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.dataflow.dsl.support.ConditionalOnDataFlowServer;
import org.springframework.cloud.dataflow.rest.client.DataFlowTemplate;
import org.springframework.cloud.dataflow.rest.client.StreamOperations;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 *
 * @author Oleg Zhurakousky
 *
 */
@SpringBootApplication
@EnableConfigurationProperties(SCDFDSLConfiguration.class)
@ConditionalOnDataFlowServer
class SimpleSCDFStream implements SCDFStream, ApplicationContextAware {

	private Logger logger = LoggerFactory.getLogger(SimpleSCDFStream.class);

	private ApplicationContext applicationContext; // TODO needed only for undeploy. Revisit

	private final String streamDefinition;

	private final StreamOperations streamOperations;

	@Autowired
	private StreamDeployer streamDeployer;


	SimpleSCDFStream(@Value("${streamDefinition}")String streamDefinition,
			SCDFDSLConfiguration environmentConfiguration, DataFlowTemplate dataFlowOperationsTemplate) {
		Assert.hasText(streamDefinition, "'streamDefinition' must not be null or empty.");
		Assert.notNull(dataFlowOperationsTemplate, "'dataFlowOperationsTemplate' must not be null");
		this.streamDefinition = streamDefinition;

		this.streamOperations = dataFlowOperationsTemplate.streamOperations();
	}


	public SCDFStream deploy() {
		logger.info("Deploying STREAM:\n" + this.streamDefinition);
		streamDeployer.deploy(this);
		return this;
	}

	@Override
	public SCDFStream undeploy() {
		SpringApplication.exit(this.applicationContext);
		logger.info("Undeploying stream:\n" + this.streamDefinition);
		return this;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public String toString(){
		return DSLUtils.streamDefinitionToJsonString(streamDefinition);
	}
}
