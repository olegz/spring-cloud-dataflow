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

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;
import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport.ConditionAndOutcomes;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.dataflow.dsl.java.SCDFStream.DefinableStream;
import org.springframework.cloud.dataflow.dsl.java.SCDFStream.DeployableStream;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author Oleg Zhurakousky
 */
class SCDFStreamBuilder implements DefinableStream {
	private final Logger logger = LoggerFactory.getLogger(SCDFStreamBuilder.class);

	private final JSONObject streamDefinition = new JSONObject();

	SCDFStreamBuilder(String name) {
		Assert.hasText(name, "'name' must not be null or empty");
		this.addToJsonObject(DSLUtils.STREAM_NAME, name);
	}

	/**
	 * Returns String representation of the current state of the stream.
	 */
	@Override
	public String toString(){
		return DSLUtils.streamDefinitionToJsonString(this.streamDefinition);
	}

	/**
	 *
	 */
	@Override
	public DeployableStream definition(String streamDefinitionDsl) {
		this.addToJsonObject(DSLUtils.STREAM_DEFINITION, streamDefinitionDsl);
		return new DeployableStream() {
			@Override
			public SCDFStream deploy(String... kvStrings) {
				Map<String, String> appDeploymentProperties = SCDFStreamBuilder.this.buildAppDeploymentProperties(kvStrings);
				SCDFStreamBuilder.this.addToJsonObject(DSLUtils.STREAM_DEPLOYMENT_PROPERIES, appDeploymentProperties);

				String jsonStreamDefinition = DSLUtils.streamDefinitionToJsonString(SCDFStreamBuilder.this.streamDefinition);

				String[] dslProperties = SCDFStreamBuilder.this.concat(Stream.of(kvStrings).map(s -> "--" + s).toArray(String[]::new),
						"--streamDefinition=" + jsonStreamDefinition, "--spring.jmx.default-domain=scdf.dsl");
				ApplicationContext context = new SpringApplicationBuilder(SimpleSCDFStream.class)
						.web(false)
						.run(dslProperties);
				ConditionAndOutcomes conditionAndOutcomes = context.getBean(ConditionEvaluationReport.class)
						.getConditionAndOutcomesBySource().get(SimpleSCDFStream.class.getName());

				String noConnectableServerErrorMessage = checkScdfServer(conditionAndOutcomes);
				if (StringUtils.hasText(noConnectableServerErrorMessage)){
					logger.error("Stream: \n" + jsonStreamDefinition
							+ "\ncan not be deployed due to the following reasons:\n\t" + noConnectableServerErrorMessage);
					return new SCDFStream() {
						@Override
						public SCDFStream undeploy() {
							return this;
						}
					};
				}
				else {
					SimpleSCDFStream stream = context.getBean(SimpleSCDFStream.class);
					return stream.deploy();
				}
			}

			@Override
			public SCDFStream deploy(Map<String, String> properties) {
				String[] propertyString = properties.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).toArray(String[]::new);
				return this.deploy(propertyString);
			}

			/**
			 * Returns String representation of the current state of the stream.
			 */
			@Override
			public String toString(){
				return DSLUtils.streamDefinitionToJsonString(SCDFStreamBuilder.this.streamDefinition);
			}
		};
	}

	private String checkScdfServer(ConditionAndOutcomes conditionAndOutcomes){
		return StreamSupport.stream(conditionAndOutcomes.spliterator(), false)
				.filter(co -> co.getCondition().getClass().getSimpleName().equals("OnDataFlowServerCondition"))
				.filter(co -> !co.getOutcome().isMatch())
				.map(co -> co.getOutcome().getMessage())
				.reduce(null, (a, b) -> a == null ? b : a + ",\n" + b);
	}

	/**
	 *
	 */
	private void addToJsonObject(String key, Object value) {
		try {
			if (value.getClass().isArray()) {
				value = new JSONArray(value);
			}
			else if (value instanceof Map){
				value = new JSONObject((Map<?,?>) value);
			}
			this.streamDefinition.put(key, value);
			System.out.println("####### STREAM DEF: " + this.streamDefinition);
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to add key=" + key + "; value=" + value + " to json object", e);
		}
	}

	private <T> T[] concat(T[] first, @SuppressWarnings("unchecked") T... second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	private Map<String, String> buildAppDeploymentProperties(String... kvStrings){
		// filter out dsl related properties loading them into map
		Map<String, String> appDeployProperties = Stream.of(kvStrings)
				.filter(s -> !s.startsWith("scdf.dsl"))
				.map(SCDFStreamBuilder.this::stringToEntry)
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

		// add inheritLogging if necessary
//		if (!appDeployProperties.keySet().stream().filter(s -> s.endsWith(".local.inheritLogging")).findFirst().isPresent()){
//			appDeployProperties.put("deployer.*.local.inheritLogging", "true");
//		}
		return appDeployProperties;
	}

	private Entry<String, String> stringToEntry(String string) {
		String[] kv = string.split("=");
		return new Entry<String, String>() {
			@Override
			public String getKey() {
				return kv[0];
			}

			@Override
			public String getValue() {
				return kv[1];
			}

			@Override
			public String setValue(String value) {
				throw new UnsupportedOperationException();
			}
		};
	}
}
