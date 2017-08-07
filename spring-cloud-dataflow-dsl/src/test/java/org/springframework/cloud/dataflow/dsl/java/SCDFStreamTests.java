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

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import org.springframework.cloud.dataflow.dsl.java.SCDFStream.DefinableStream;
import org.springframework.cloud.dataflow.dsl.java.SCDFStream.DeployableStream;
import org.springframework.cloud.dataflow.dsl.support.LocalServer;


import static org.assertj.core.api.Assertions.assertThat;
/**
 *
 * @author Oleg Zhurakousky
 *
 */
public class SCDFStreamTests {

//	@Rule
//	public BrokerRunning brokerRunning = BrokerRunning.isRunning();

	@Test
	public void demo(){

		//LocalServer.start();
		SCDFStream.name("TICKTOCK")
		 .definition("time | log")
		 .deploy("app.log.log.expression='TICKTOCK - TIMESTAMP: '.concat(payload)");

//		"deployer.time-processor.debugPort=4000,"
		//+ "deployer.time-processor.debugSuspend=true,"

//		java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=4000,suspend=n myapp
	}



	@Test
	public void testNoExceptionWhenSCDFServerNotPresent() throws Exception {
		SCDFStream scdfStream = SCDFStream.name("TICKTOCK")
				  	                  .definition("time | log")
				 	                  .deploy("app.log.log.expression", "'TICKTOCK - TIMESTAMP: '.concat(payload)");
		assertThat(scdfStream).isNotNull();
		scdfStream = scdfStream.undeploy();
		//destroy. Basicay expose ablity of shell dsl
		// do it in core package with the oyher dsl. java.dsl
		assertThat(scdfStream).isNotNull();
	}

	@Test
	public void testIndividualBitsAndPieces() throws Exception {
		LocalServer.start();

		DefinableStream definableStream = SCDFStream.name("my-stream");
		assertThat(definableStream).isNotNull();
		assertThat(new JSONObject(definableStream.toString()).get("name")).isEqualTo("my-stream");

		DeployableStream deployableStream = definableStream.definition("foo | bar");
		assertThat(deployableStream).isNotNull();
		assertThat(new JSONObject(deployableStream.toString()).get("definition")).isEqualTo("foo | bar");

		SCDFStream deployedStream1 = deployableStream.deploy();
		assertThat(deployedStream1).isNotNull();
		JSONArray deploymentProperties = (JSONArray) new JSONObject(deployedStream1.toString()).get("deployment.properties");
		assertThat(deploymentProperties).isNotNull();
		assertThat(deploymentProperties.length()).isEqualTo(0);
		deployedStream1.undeploy();

		SCDFStream deployedStream2 = deployableStream.deploy("name=john", "id=123");
		assertThat(deployedStream2).isNotNull();
		deploymentProperties = (JSONArray) new JSONObject(deployedStream2.toString()).get("deployment.properties");
		assertThat(deploymentProperties).isNotNull();
		assertThat(deploymentProperties.length()).isEqualTo(2);
		assertThat(deploymentProperties.get(0)).isEqualTo("name=john");
		assertThat(deploymentProperties.get(1)).isEqualTo("id=123");
		deployedStream2.undeploy();
		LocalServer.stop();
	}
}
