package com.bonitasoft.connectors.yousign;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class RegisterWebhookConnectorTest {

    @Mock
    private YousignClient mockClient;

    private RegisterWebhookConnector connector;

    @BeforeEach
    void setUp() {
        connector = new RegisterWebhookConnector();
    }

    private Map<String, Object> validInputs() {
        var inputs = new HashMap<String, Object>();
        inputs.put("apiKey", "test-api-key");
        inputs.put("webhookEndpointUrl", "https://my-app.example.com/webhook");
        return inputs;
    }

    private void injectMockClient() throws Exception {
        var clientField = AbstractYousignConnector.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(connector, mockClient);
    }

    @Test
    void should_execute_successfully() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.registerWebhook(any())).thenReturn(
                new YousignClient.RegisterWebhookResult("wh-123"));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("webhookId")).isEqualTo("wh-123");
        assertThat(connector.getOutputs().get("success")).isEqualTo(true);
    }

    @Test
    void should_fail_validation_when_webhookEndpointUrl_missing() {
        var inputs = validInputs();
        inputs.remove("webhookEndpointUrl");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class);
    }

    @Test
    void should_fail_validation_when_apiKey_missing() {
        var inputs = validInputs();
        inputs.remove("apiKey");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class);
    }

    @Test
    void should_fail_immediately_on_auth_error() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.registerWebhook(any())).thenThrow(
                new YousignException("Auth error", 401, false));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("success")).isEqualTo(false);
    }

    @Test
    void should_handle_network_timeout() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.registerWebhook(any())).thenThrow(
                new YousignException("Network error", new java.net.SocketTimeoutException()));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("success")).isEqualTo(false);
    }

    @Test
    void should_apply_default_subscribed_events() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        var configField = AbstractYousignConnector.class.getDeclaredField("configuration");
        configField.setAccessible(true);
        var config = (YousignConfiguration) configField.get(connector);
        assertThat(config.getSubscribedEventsJson()).contains("signature_request.done");
    }

    @Test
    void should_populate_all_output_fields() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.registerWebhook(any())).thenReturn(
                new YousignClient.RegisterWebhookResult("wh-456"));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("webhookId")).isNotNull();
        assertThat(connector.getOutputs().get("success")).isEqualTo(true);
    }

    @Test
    void should_set_error_outputs_on_failure() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.registerWebhook(any())).thenThrow(
                new YousignException("Bad request", 400, false));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("success")).isEqualTo(false);
        assertThat(connector.getOutputs().get("errorMessage")).isNotNull();
    }
}
