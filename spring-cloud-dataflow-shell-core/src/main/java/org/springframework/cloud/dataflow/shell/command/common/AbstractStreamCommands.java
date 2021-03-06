/*
 * Copyright 2015-2018 the original author or authors.
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

package org.springframework.cloud.dataflow.shell.command.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.cloud.dataflow.rest.client.StreamOperations;
import org.springframework.cloud.dataflow.rest.resource.StreamDefinitionResource;
import org.springframework.cloud.dataflow.rest.resource.StreamDeploymentResource;
import org.springframework.cloud.dataflow.rest.util.DeploymentPropertiesUtils;
import org.springframework.cloud.dataflow.shell.command.support.OpsType;
import org.springframework.cloud.dataflow.shell.command.support.RoleType;
import org.springframework.cloud.dataflow.shell.command.support.ShellUtils;
import org.springframework.cloud.dataflow.shell.config.DataFlowShell;
import org.springframework.core.io.FileSystemResource;
import org.springframework.hateoas.PagedResources;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.shell.table.AbsoluteWidthSizeConstraints;
import org.springframework.shell.table.BeanListTableModel;
import org.springframework.shell.table.CellMatchers;
import org.springframework.shell.table.Table;
import org.springframework.shell.table.TableBuilder;
import org.springframework.shell.table.TableModelBuilder;
import org.springframework.util.StringUtils;

/**
 * Stream commands.
 *
 * @author Ilayaperumal Gopinathan
 * @author Mark Fisher
 * @author Gunnar Hillert
 * @author Glenn Renfro
 * @author Christian Tzolov
 */
public abstract class AbstractStreamCommands implements CommandMarker {

	private static final String INFO_STREAM = "stream info";

	private static final String LIST_STREAM = "stream list";

	private static final String UNDEPLOY_STREAM = "stream undeploy";

	private static final String UNDEPLOY_STREAM_ALL = "stream all undeploy";

	private static final String DESTROY_STREAM = "stream destroy";

	private static final String DESTROY_STREAM_ALL = "stream all destroy";

	protected static final String PROPERTIES_OPTION = "properties";

	protected static final String PROPERTIES_FILE_OPTION = "propertiesFile";

	protected DataFlowShell dataFlowShell;

	protected UserInput userInput;

	@CliAvailabilityIndicator({ LIST_STREAM, INFO_STREAM })
	public boolean availableWithViewRole() {
		return dataFlowShell.hasAccess(RoleType.VIEW, OpsType.STREAM);
	}

	@CliAvailabilityIndicator({ UNDEPLOY_STREAM, UNDEPLOY_STREAM_ALL, DESTROY_STREAM,
			DESTROY_STREAM_ALL })
	public boolean availableWithCreateRole() {
		return dataFlowShell.hasAccess(RoleType.CREATE, OpsType.STREAM);
	}

	@CliCommand(value = LIST_STREAM, help = "List created streams")
	public Table listStreams() {
		final PagedResources<StreamDefinitionResource> streams = streamOperations().list();
		LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
		headers.put("name", "Stream Name");
		headers.put("dslText", "Stream Definition");
		headers.put("statusDescription", "Status");
		BeanListTableModel<StreamDefinitionResource> model = new BeanListTableModel<>(streams, headers);
		return DataFlowTables.applyStyle(new TableBuilder(model)).build();
	}

	@CliCommand(value = INFO_STREAM, help = "Show information about a specific stream")
	public List<Object> streamInfo(@CliOption(key = { "",
			"name" }, help = "the name of the stream to show", mandatory = true, optionContext = "existing-stream disable-string-converter") String name) {
		List<Object> result = new ArrayList<>();
		final StreamDeploymentResource stream = streamOperations().info(name);
		TableModelBuilder<Object> modelBuilder = new TableModelBuilder<>();
		modelBuilder.addRow().addValue("Name").addValue("DSL").addValue("Status");
		modelBuilder.addRow().addValue(stream.getStreamName())
				.addValue(stream.getDslText())
				.addValue(stream.getStatus());
		TableBuilder builder = DataFlowTables.applyStyle(new TableBuilder(modelBuilder.build()))
				.on(CellMatchers.table()).addSizer(new AbsoluteWidthSizeConstraints(30)).and();
		result.add(builder.build());
		if (StringUtils.hasText(stream.getDeploymentProperties())) {
			//TODO: rename Deployment properties for Skipper as it includes apps' info (app:version) as well
			result.add(String.format("Stream Deployment properties: %s", ShellUtils.prettyPrintIfJson(stream.getDeploymentProperties())));
		}
		return result;
	}

	protected Map<String, String> getDeploymentProperties(@CliOption(key = {
			PROPERTIES_OPTION }, help = "the properties for this deployment") String deploymentProperties,
			@CliOption(key = {
					PROPERTIES_FILE_OPTION }, help = "the properties for this deployment (as a File)") File propertiesFile,
			int which) throws IOException {
		Map<String, String> propertiesToUse;
		switch (which) {
		case 0:
			propertiesToUse = DeploymentPropertiesUtils.parse(deploymentProperties);
			break;
		case 1:
			String extension = FilenameUtils.getExtension(propertiesFile.getName());
			Properties props = null;
			if (extension.equals("yaml") || extension.equals("yml")) {
				YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean();
				yamlPropertiesFactoryBean.setResources(new FileSystemResource(propertiesFile));
				yamlPropertiesFactoryBean.afterPropertiesSet();
				props = yamlPropertiesFactoryBean.getObject();
			}
			else {
				props = new Properties();
				try (FileInputStream fis = new FileInputStream(propertiesFile)) {
					props.load(fis);
				}
			}
			propertiesToUse = DeploymentPropertiesUtils.convert(props);
			break;
		case -1: // Neither option specified
			propertiesToUse = new HashMap<>(1);
			break;
		default:
			throw new AssertionError();
		}
		return propertiesToUse;
	}

	@CliCommand(value = UNDEPLOY_STREAM, help = "Un-deploy a previously deployed stream")
	public String undeployStream(@CliOption(key = { "",
			"name" }, help = "the name of the stream to un-deploy", mandatory = true, optionContext = "existing-stream disable-string-converter") String name) {
		streamOperations().undeploy(name);
		return String.format("Un-deployed stream '%s'", name);
	}

	@CliCommand(value = UNDEPLOY_STREAM_ALL, help = "Un-deploy all previously deployed stream")
	public String undeployAllStreams(
			@CliOption(key = "force", help = "bypass confirmation prompt", unspecifiedDefaultValue = "false", specifiedDefaultValue = "true") boolean force) {
		if (force || "y".equalsIgnoreCase(userInput.promptWithOptions("Really undeploy all streams?", "n", "y", "n"))) {
			streamOperations().undeployAll();
			return String.format("Un-deployed all the streams");
		}
		else {
			return "";
		}
	}

	@CliCommand(value = DESTROY_STREAM, help = "Destroy an existing stream")
	public String destroyStream(@CliOption(key = { "",
			"name" }, help = "the name of the stream to destroy", mandatory = true, optionContext = "existing-stream disable-string-converter") String name) {
		streamOperations().destroy(name);
		return String.format("Destroyed stream '%s'", name);
	}

	@CliCommand(value = DESTROY_STREAM_ALL, help = "Destroy all existing streams")
	public String destroyAllStreams(
			@CliOption(key = "force", help = "bypass confirmation prompt", unspecifiedDefaultValue = "false", specifiedDefaultValue = "true") boolean force) {
		if (force || "y".equalsIgnoreCase(userInput.promptWithOptions("Really destroy all streams?", "n", "y", "n"))) {
			streamOperations().destroyAll();
			return "Destroyed all streams";
		}
		else {
			return "";
		}
	}

	protected StreamOperations streamOperations() {
		return dataFlowShell.getDataFlowOperations().streamOperations();
	}
}
