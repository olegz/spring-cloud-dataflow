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

import org.json.JSONObject;

class DSLUtils {

	public static final String STREAM_NAME = "name";

	public static final String STREAM_DEFINITION = "definition";

	public static final String STREAM_DEPLOYMENT_PROPERIES = "deployment.properties";

	static String streamDefinitionToJsonString(String streamDefinition) {
		try {
			return streamDefinitionToJsonString(new JSONObject(streamDefinition));
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static String streamDefinitionToJsonString(JSONObject streamDefinition) {
		try {
			return streamDefinition.toString(2);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
