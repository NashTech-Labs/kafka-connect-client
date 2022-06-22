/**
 * Copyright 2018, 2019, 2020, 2021 SourceLab.org https://github.com/SourceLabOrg/kafka-connect-client
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.sourcelab.kafka.connect.apiclient;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sourcelab.kafka.connect.apiclient.exception.ResponseParseException;
import org.sourcelab.kafka.connect.apiclient.request.JacksonFactory;
import org.sourcelab.kafka.connect.apiclient.request.Request;
import org.sourcelab.kafka.connect.apiclient.request.RequestErrorResponse;
import org.sourcelab.kafka.connect.apiclient.request.delete.DeleteConnector;
import org.sourcelab.kafka.connect.apiclient.request.dto.ConnectServerVersion;
import org.sourcelab.kafka.connect.apiclient.request.dto.ConnectorDefinition;
import org.sourcelab.kafka.connect.apiclient.request.dto.ConnectorPlugin;
import org.sourcelab.kafka.connect.apiclient.request.dto.ConnectorPluginConfigDefinition;
import org.sourcelab.kafka.connect.apiclient.request.dto.ConnectorPluginConfigValidationResults;
import org.sourcelab.kafka.connect.apiclient.request.dto.ConnectorStatus;
import org.sourcelab.kafka.connect.apiclient.request.dto.ConnectorTopics;
import org.sourcelab.kafka.connect.apiclient.request.dto.ConnectorsWithExpandedInfo;
import org.sourcelab.kafka.connect.apiclient.request.dto.ConnectorsWithExpandedMetadata;
import org.sourcelab.kafka.connect.apiclient.request.dto.ConnectorsWithExpandedStatus;
import org.sourcelab.kafka.connect.apiclient.request.dto.NewConnectorDefinition;
import org.sourcelab.kafka.connect.apiclient.request.dto.Task;
import org.sourcelab.kafka.connect.apiclient.request.dto.TaskStatus;
import org.sourcelab.kafka.connect.apiclient.request.get.GetConnectServerVersion;
import org.sourcelab.kafka.connect.apiclient.request.get.GetConnector;
import org.sourcelab.kafka.connect.apiclient.request.get.GetConnectorConfig;
import org.sourcelab.kafka.connect.apiclient.request.get.GetConnectorPlugins;
import org.sourcelab.kafka.connect.apiclient.request.get.GetConnectorStatus;
import org.sourcelab.kafka.connect.apiclient.request.get.GetConnectorTaskStatus;
import org.sourcelab.kafka.connect.apiclient.request.get.GetConnectorTasks;
import org.sourcelab.kafka.connect.apiclient.request.get.GetConnectorTopics;
import org.sourcelab.kafka.connect.apiclient.request.get.GetConnectors;
import org.sourcelab.kafka.connect.apiclient.request.get.GetConnectorsExpandAllDetails;
import org.sourcelab.kafka.connect.apiclient.request.get.GetConnectorsExpandInfo;
import org.sourcelab.kafka.connect.apiclient.request.get.GetConnectorsExpandStatus;
import org.sourcelab.kafka.connect.apiclient.request.post.PostConnector;
import org.sourcelab.kafka.connect.apiclient.request.post.PostConnectorRestart;
import org.sourcelab.kafka.connect.apiclient.request.post.PostConnectorTaskRestart;
import org.sourcelab.kafka.connect.apiclient.request.put.PutConnectorConfig;
import org.sourcelab.kafka.connect.apiclient.request.put.PutConnectorPause;
import org.sourcelab.kafka.connect.apiclient.request.put.PutConnectorPluginConfigValidate;
import org.sourcelab.kafka.connect.apiclient.request.put.PutConnectorResume;
import org.sourcelab.kafka.connect.apiclient.request.put.PutConnectorTopicsReset;
import org.sourcelab.kafka.connect.apiclient.rest.HttpClientRestClient;
import org.sourcelab.kafka.connect.apiclient.rest.RestClient;
import org.sourcelab.kafka.connect.apiclient.rest.RestResponse;
import org.sourcelab.kafka.connect.apiclient.rest.exceptions.InvalidRequestException;
import org.sourcelab.kafka.connect.apiclient.rest.exceptions.UnauthorizedRequestException;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * API Client for interacting with the Kafka-Connect Rest Endpoint.
 * Official Rest Endpoint documentation can be found here:
 *   https://docs.confluent.io/current/connect/references/restapi.html
 */
public class KafkaConnectClient {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConnectClient.class);

    /**
     * Our API Configuration.
     */
    private final Configuration configuration;

    /**
     * Underlying RestClient to use.
     */
    private final RestClient restClient;

    /**
     * Internal State flag.
     */
    private boolean isInitialized = false;


    /**
     * Default Constructor.
     * @param configuration Api Client Configuration.
     */
    public KafkaConnectClient(final Configuration configuration) {
        this.configuration = configuration;
        this.restClient = new HttpClientRestClient();
    }

    /**
     * Constructor for injecting a RestClient implementation.
     * Typically only used in testing.
     * @param configuration Api Client Configuration.
     * @param restClient RestClient implementation to use.
     */
    public KafkaConnectClient(final Configuration configuration, final RestClient restClient) {
        this.configuration = configuration;
        this.restClient = restClient;
    }

    /**
     * Retrieve details about the Kafka-Connect service itself.
     * https://docs.confluent.io/current/connect/references/restapi.html#get--
     * @return ConnectServerVersion
     */
    public ConnectServerVersion getConnectServerVersion() {
        return submitRequest(new GetConnectServerVersion());
    }

    /**
     * Get a list of deployed connectors.
     * https://docs.confluent.io/current/connect/references/restapi.html#get--connectors
     *
     * @return Collection of connector names currently deployed.
     */
    public Collection<String> getConnectors() {
        return submitRequest(new GetConnectors());
    }

    /**
     * Get a list of deployed connectors, including the status for each connector.
     * https://docs.confluent.io/current/connect/references/restapi.html#get--connectors
     *
     * Requires Kafka-Connect 2.3.0+
     *
     * @return All deployed connectors, and their respective statuses.
     */
    public ConnectorsWithExpandedStatus getConnectorsWithExpandedStatus() {
        return submitRequest(new GetConnectorsExpandStatus());
    }

    /**
     * Get a list of deployed connectors, including the definition for each connector.
     * https://docs.confluent.io/current/connect/references/restapi.html#get--connectors
     *
     * Requires Kafka-Connect 2.3.0+
     *
     * @return All deployed connectors, and their respective definition.
     */
    public ConnectorsWithExpandedInfo getConnectorsWithExpandedInfo() {
        return submitRequest(new GetConnectorsExpandInfo());
    }

    /**
     * Get a list of deployed connectors, including all metadata available.
     * Currently includes both 'info' and 'status'
     * metadata.
     * https://docs.confluent.io/current/connect/references/restapi.html#get--connectors
     *
     * Requires Kafka-Connect 2.3.0+
     *
     * @return All deployed connectors, and their respective metadata.
     */
    public ConnectorsWithExpandedMetadata getConnectorsWithAllExpandedMetadata() {
        return submitRequest(new GetConnectorsExpandAllDetails());
    }

    /**
     * Get information about the connector.
     * https://docs.confluent.io/current/connect/references/restapi.html#get--connectors-(string-name)
     * @param connectorName Name of connector.
     * @return Connector details.
     */
    public ConnectorDefinition getConnector(final String connectorName) {
        return submitRequest(new GetConnector(connectorName));
    }

    /**
     * Get the configuration for the connector.
     * https://docs.confluent.io/current/connect/references/restapi.html#get--connectors-(string-name)-config
     * @param connectorName Name of connector.
     * @return Configuration for connector.
     */
    public Map<String, String> getConnectorConfig(final String connectorName) {
        return submitRequest(new GetConnectorConfig(connectorName));
    }

    /**
     * Get the status of specified connector by name.
     * https://docs.confluent.io/current/connect/references/restapi.html#get--connectors-(string-name)-config
     *
     * @param connectorName Name of connector.
     * @return Status details of the connector.
     */
    public ConnectorStatus getConnectorStatus(final String connectorName) {
        return submitRequest(new GetConnectorStatus(connectorName));
    }

    /**
     * Get the set of topics that a specific connector is using since the connector was created or since a request
     * to reset its set of active topics was issued.
     * https://docs.confluent.io/current/connect/references/restapi.html#get--connectors-(string-name)-topics
     *
     * Requires Kafka-Connect 2.5.0+
     *
     * @param connectorName Name of connector.
     * @return Connector Topics response.
     */
    public ConnectorTopics getConnectorTopics(final String connectorName) {
        return submitRequest(new GetConnectorTopics(connectorName));
    }

    /**
     * Send a request to empty the set of active topics of a connector.
     * https://docs.confluent.io/current/connect/references/restapi.html#put--connectors-(string-name)-topics-reset
     * Requires Kafka-Connect 2.5.0+
     *
     * @param connectorName Name of connector.
     * @return true on success.
     */
    public boolean resetConnectorTopics(final String connectorName) {
        return submitRequest(new PutConnectorTopicsReset(connectorName));
    }

    /**
     * Create a new connector, returning the current connector info if successful.
     * https://docs.confluent.io/current/connect/references/restapi.html#post--connectors
     *
     * @param connectorDefinition Defines the new connector to deploy
     * @return connector info.
     */
    public ConnectorDefinition addConnector(final NewConnectorDefinition connectorDefinition) {
        return submitRequest(new PostConnector(connectorDefinition));
    }

    /**
     * Update a connector's configuration.
     * https://docs.confluent.io/current/connect/references/restapi.html#put--connectors-(string-name)-config
     *
     * @param connectorName Name of connector to update.
     * @param config Configuration values to set.
     * @return ConnectorDefinition describing the connectors configuration.
     */
    public ConnectorDefinition updateConnectorConfig(final String connectorName, final Map<String, String> config) {
        return submitRequest(new PutConnectorConfig(connectorName, config));
    }

    /**
     * Restart a connector.
     * https://docs.confluent.io/current/connect/references/restapi.html#post--connectors-(string-name)-restart
     *
     * @param connectorName Name of connector to restart.
     * @return Boolean true if success.
     */
    public Boolean restartConnector(final String connectorName) {
        return submitRequest(new PostConnectorRestart(connectorName));
    }

    /**
     * Pause a connector.
     * https://docs.confluent.io/current/connect/references/restapi.html#put--connectors-(string-name)-pause
     *
     * @param connectorName Name of connector to pause.
     * @return Boolean true if success.
     */
    public Boolean pauseConnector(final String connectorName) {
        return submitRequest(new PutConnectorPause(connectorName));
    }

    /**
     * Resume a connector.
     * https://docs.confluent.io/current/connect/references/restapi.html#put--connectors-(string-name)-resume
     *
     * @param connectorName Name of connector to resume.
     * @return Boolean true if success.
     */
    public Boolean resumeConnector(final String connectorName) {
        return submitRequest(new PutConnectorResume(connectorName));
    }

    /**
     * Resume a connector.
     * https://docs.confluent.io/current/connect/references/restapi.html#put--connectors-(string-name)-resume
     *
     * @param connectorName Name of connector to resume.
     * @return Boolean true if success.
     */
    public Boolean deleteConnector(final String connectorName) {
        return submitRequest(new DeleteConnector(connectorName));
    }

    /**
     * Get a list of tasks currently running for the connector.
     * https://docs.confluent.io/current/connect/references/restapi.html#get--connectors-(string-name)-tasks
     *
     * @param connectorName Name of connector to retrieve tasks for.
     * @return Collection of details about each task.
     */
    public Collection<Task> getConnectorTasks(final String connectorName) {
        return submitRequest(new GetConnectorTasks(connectorName));
    }

    /**
     * Get a task’s status.
     * https://docs.confluent.io/current/connect/references/restapi.html#get--connectors-(string-name)-tasks-(int-taskid)-status
     *
     * @param connectorName Name of connector to retrieve tasks for.
     * @param taskId Id of task to get status for.
     * @return Details about task.
     */
    public TaskStatus getConnectorTaskStatus(final String connectorName, final int taskId) {
        return submitRequest(new GetConnectorTaskStatus(connectorName, taskId));
    }

    /**
     * Restart an individual task.
     * https://docs.confluent.io/current/connect/references/restapi.html#post--connectors-(string-name)-tasks-(int-taskid)-restart
     *
     * @param connectorName Name of connector to restart tasks for.
     * @param taskId Id of task to restart
     * @return True if a success.
     */
    public Boolean restartConnectorTask(final String connectorName, final int taskId) {
        return submitRequest(new PostConnectorTaskRestart(connectorName, taskId));
    }

    /**
     * Return a list of connector plugins installed in the Kafka Connect cluster.
     * https://docs.confluent.io/current/connect/references/restapi.html#get--connector-plugins-
     *
     * @return Collection of available connector plugins.
     */
    public Collection<ConnectorPlugin> getConnectorPlugins() {
        return submitRequest(new GetConnectorPlugins());
    }

    /**
     * Validate the provided configuration values against the configuration definition. This API performs per config
     * validation, returns suggested values and error messages during validation.
     * https://docs.confluent.io/current/connect/references/restapi.html#put--connector-plugins-(string-name)-config-validate
     *
     * @param configDefinition Defines the configuration to validate.
     * @return Results of the validation.
     */
    public ConnectorPluginConfigValidationResults validateConnectorPluginConfig(final ConnectorPluginConfigDefinition configDefinition) {
        return submitRequest(
            new PutConnectorPluginConfigValidate(configDefinition.getName(), configDefinition.getConfig())
        );
    }

    private <T> T submitRequest(final Request<T> request) {
        // Submit request
        final RestResponse restResponse = getRestClient().submitRequest(request);
        final int responseCode = restResponse.getHttpCode();
        String responseStr = restResponse.getResponseStr();

        // If we have a valid response
        logger.debug("Response: {}", restResponse);

        // Check for invalid http status codes
        if (responseCode >= 200 && responseCode < 300) {
            // These response codes have no values
            if ((responseCode == 204 || responseCode == 205) && responseStr == null) {
                // Avoid NPE
                responseStr = "";
            }

            try {
                return request.parseResponse(responseStr);
            } catch (final MismatchedInputException exception) {
                throw new ResponseParseException(exception.getMessage(), exception);
            } catch (final IOException exception) {
                throw new RuntimeException(exception.getMessage(), exception);
            }
        }

        // Server reject's client's authentication.
        if (responseCode == HttpStatus.SC_UNAUTHORIZED) {
            // Throw contextual error msg based on if credentials are configured or not.
            String errorMsg;
            if (configuration.getBasicAuthUsername() == null) {
                errorMsg = "Server required authentication credentials but none were provided in client configuration.";
            } else {
                errorMsg = "Client authentication credentials (username=" + configuration.getBasicAuthUsername() + ") was rejected by server.";
            }
            errorMsg = errorMsg + " Server responded with: \"" + responseStr + "\"";
            throw new UnauthorizedRequestException(errorMsg, responseCode);
        }

        // Attempt to parse error response
        try {
            final RequestErrorResponse errorResponse = JacksonFactory.newInstance().readValue(responseStr, RequestErrorResponse.class);
            throw InvalidRequestException.factory(errorResponse);
        } catch (final IOException e) {
            // swallow
        }
        throw new InvalidRequestException("Invalid response from server: " + responseStr, restResponse.getHttpCode());
    }

    private RestClient getRestClient() {
        // If we haven't initialized.
        if (!isInitialized) {
            // Call Init.
            restClient.init(getConfiguration());

            // Flip state flag
            isInitialized = true;
        }

        // return our rest client.
        return restClient;
    }

    private Configuration getConfiguration() {
        return configuration;
    }
}
