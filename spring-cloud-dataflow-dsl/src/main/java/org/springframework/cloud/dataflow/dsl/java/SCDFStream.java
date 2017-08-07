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

import java.util.Map;

import org.springframework.util.Assert;

/**
 * <p>
 * SCDF Shell/DSL-based builder and deployer of SCDF streams.
 * </p>
 * <p>
 * For more info about SCDF DSL please see <a href="http://docs.spring.io/spring-cloud-dataflow/docs/1.2.3.RELEASE/reference/htmlsingle/#spring-cloud-dataflow-stream-intro-dsl">SCDF DSL</a>
 * </p>
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * {@code
 * SCDFStream stream = SCDFStream.name("TICKTOCK")
 * 	                       .definition("time | log")
 * 	                       .deploy("app.log.log.expression", "'TICKTOCK - TIMESTAMP: '.concat(payload)");
 * // stream is deployed and running
 *
 * stream.undeploy();
 * // stream is stopped and un-deployed
 * }
 * </pre>
 *
 * @author Oleg Zhurakousky
 */
public interface SCDFStream {

	/**
	 * Creates an instance of the builder as {@link DefinableStream} for a named stream.
	 * @return an instance of the {@link DefinableStream} that supports providing a
	 * definition of the stream (e.g., <code>builder.definition("time | log")</code>).
	 * Once the definition is provided the subsequent builder will provide deploy operation
	 * where deployment properties may optionally be provided.
	 */
	public static DefinableStream name(String name) {
		Assert.hasText(name, "'name' must not be null or empty");
		return new SCDFStreamBuilder(name);
	}

	public SCDFStream undeploy();

	/**
	 * Defines an intermediate strategy for {@link SCDFStream} that <i>can</i> be deployed.
	 *
	 * @author Oleg Zhurakousky
	 */
	public interface DeployableStream {
		/**
		 * <p>
		 * Deploys this stream.
		 * </p>
		 * <p>
		 * This operation supports optionally passing deployment properties
		 * as an array of "key=value" strings.
		 * </p>
		 * @param deploymentPropertiesString array of "key=value" strings
		 *        defining deployment properties
		 * @return an instance of fully deployed {@link SCDFStream}
		 */
		SCDFStream deploy(String... deploymentPropertiesString);

		/**
		 * <p>
		 * Deploys this stream.
		 * </p>
		 * <p>
		 * This operation supports passing deployment properties as {@link Map}
		 * </p>
		 * @param deploymentProperties instance of {@link Map}
		 *        defining deployment properties
		 * @return an instance of fully deployed {@link SCDFStream}
		 */
		SCDFStream deploy(Map<String, String> deploymentProperties);
	}

	/**
	 * Defines an intermediate (initial) strategy for {@link SCDFStream}
	 * that <i>can</i> accept stream definition (e.g., "foo | bar");
	 *
	 * @author Oleg Zhurakousky
	 */
	public interface DefinableStream {
		/**
		 * Provides string-based dsl expression defining the stream (e.g., "http | log").
		 *
		 * @param streamDefinitionDsl dsl defining stream (e.g., "http | log")
		 * @return an instance of {@link DeployableStream}
		 */
		public DeployableStream definition(String streamDefinitionDsl);
	}
}
