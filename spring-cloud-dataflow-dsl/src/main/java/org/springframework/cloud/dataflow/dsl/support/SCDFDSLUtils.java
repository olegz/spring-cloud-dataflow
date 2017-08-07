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

import java.net.URI;

import org.springframework.cloud.dataflow.rest.client.DataFlowTemplate;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class SCDFDSLUtils {

	public static DataFlowTemplate createDataFlowTemplate(String serverUri) {
		int timeout = 1000 * 60;
		HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(timeout);
        httpRequestFactory.setConnectTimeout(timeout);
        httpRequestFactory.setReadTimeout(timeout);
        RestTemplate rt = new RestTemplate(httpRequestFactory);
        try {
        	return new DataFlowTemplate(new URI(serverUri), rt);
		}
		catch (Exception e) {
			String message = "Failed to connect to DataFlow server on: " + serverUri + ". "
						+ "Possible reasons/solutions:\n\t\t - server may not be running on the provided address/make "
						+ "sure that the server is running and/or the address is correct."
						+ " You can start the local server via 'LocalServer.start();'";
			throw new IllegalStateException(message, e);
		}
	}
}
