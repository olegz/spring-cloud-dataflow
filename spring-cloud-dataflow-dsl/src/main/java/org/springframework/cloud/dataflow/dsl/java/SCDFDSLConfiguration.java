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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Oleg Zhurakousky
 *
 */
@ConfigurationProperties("scdf.dsl")
public class SCDFDSLConfiguration {
	public final static String DEFAULT_SERVER_URI = "http://localhost:9393";

	private String serverUri = DEFAULT_SERVER_URI;

	private String streamRegistrationResource = "http://bit.ly/Bacon-BUILD-SNAPSHOT-stream-applications-rabbit-maven";

	private String taskRegistrationResource = "http://bit.ly/Belmont-BUILD-SNAPSHOT-task-applications-maven";

	private int debugPort;

	private boolean debugSuspend;

	public SCDFDSLConfiguration(){
		System.out.println();
	}

	public int getDebugPort() {
		return debugPort;
	}

	public void setDebugPort(int debugPort) {
		this.debugPort = debugPort;
	}

	public boolean isDebugSuspend() {
		return debugSuspend;
	}

	public void setDebugSuspend(boolean debugSuspend) {
		this.debugSuspend = debugSuspend;
	}

	public String getServerUri() {
		return serverUri;
	}

	public void setServerUri(String serverUri) {
		this.serverUri = serverUri;
	}

	public String getStreamRegistrationResource() {
		return streamRegistrationResource;
	}

	public void setStreamRegistrationResource(String streamRegistrationResource) {
		this.streamRegistrationResource = streamRegistrationResource;
	}

	public String getTaskRegistrationResource() {
		return taskRegistrationResource;
	}

	public void setTaskRegistrationResource(String taskRegistrationResource) {
		this.taskRegistrationResource = taskRegistrationResource;
	}
}
