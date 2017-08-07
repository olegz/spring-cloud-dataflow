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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.cloud.dataflow.dsl.java.SCDFDSLConfiguration;
import org.springframework.cloud.dataflow.dsl.java.StreamDeployer;
import org.springframework.cloud.dataflow.rest.client.DataFlowTemplate;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.Assert;

/**
 * Implementation of {@link ConditionalOnDataFlowServer} which will check if SDFS server
 * provided via 'scdf.dsl.server-uri' is up and running defaulting to local server http://localhost:9393
 *
 * It will also register instance of StreamDeplopyerdiscovered via spring.factories with AC to
 * be used with the current deployment.
 *
 * @author Oleg Zhurakousky
 *
 */
class OnDataFlowServerCondition extends SpringBootCondition {

	private Logger logger = LoggerFactory.getLogger(OnDataFlowServerCondition.class);

	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {

		DefaultListableBeanFactory bf = (DefaultListableBeanFactory) context.getBeanFactory();

		String serverUri = context.getEnvironment().getProperty("scdf.dsl.server-uri", SCDFDSLConfiguration.DEFAULT_SERVER_URI);

		if (!bf.containsBean("dataFlowOperationsTemplate")){
			List<StreamDeployer> streamDeployers = SpringFactoriesLoader.loadFactories(StreamDeployer.class, this.getClass().getClassLoader());
			Assert.notEmpty(streamDeployers, "Failed to locate StreamDeplopyer");
			//Assert.isTrue(streamDeployers.size() == 1, "found more then one deployer");
			StreamDeployer streamDeployer = streamDeployers.get(0);
			bf.registerSingleton("streamDeployer", streamDeployer);
			logger.info("Using " + streamDeployer.getClass().getName());

	        try {
	        	DataFlowTemplate dataFlowOperationsTemplate = SCDFDSLUtils.createDataFlowTemplate(serverUri);
	        	bf.registerSingleton("dataFlowOperationsTemplate", dataFlowOperationsTemplate);
			}
			catch (Exception e) {
				return ConditionOutcome.noMatch(e.getMessage());
			}
		}

		return ConditionOutcome.match();
	}
}
