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
class GetStatusConnectorTest {

    @Mock
    private YousignClient mockClient;

    private GetStatusConnector connector;

    @BeforeEach
    void setUp() {
        connector = new GetStatusConnector();
    }

    private Map<String, Object> validInputs() {
        var inputs = new HashMap<String, Object>();
        inputs.put("apiKey", "test-api-key");
        inputs.put("signatureRequestId", "req-123");
        return inputs;
    }

    private void injectMockClient() throws Exception {
        var clientField = AbstractYousignConnector.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(connector, mockClient);
    }

    @Test
    void should_execute_successfully_when_valid_request_id() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.getStatus("req-123")).thenReturn(
                new YousignClient.GetStatusResult("ongoing", "[]", false, "[]"));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("status")).isEqualTo("ongoing");
        assertThat(connector.getOutputs().get("isTerminal")).isEqualTo(false);
        assertThat(connector.getOutputs().get("success")).isEqualTo(true);
    }

    @Test
    void should_return_terminal_true_when_status_done() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.getStatus("req-123")).thenReturn(
                new YousignClient.GetStatusResult("done", "[]", true, "[\"doc-1\"]"));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("isTerminal")).isEqualTo(true);
    }

    @Test
    void should_fail_validation_when_signatureRequestId_missing() {
        var inputs = validInputs();
        inputs.remove("signatureRequestId");
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
        when(mockClient.getStatus("req-123")).thenThrow(
                new YousignException("Authentication error", 401, false));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("success")).isEqualTo(false);
    }

    @Test
    void should_handle_network_timeout() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.getStatus("req-123")).thenThrow(
                new YousignException("Network error", new java.net.SocketTimeoutException()));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("success")).isEqualTo(false);
    }

    @Test
    void should_populate_all_output_fields() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.getStatus("req-123")).thenReturn(
                new YousignClient.GetStatusResult("ongoing", "[{\"id\":\"s1\"}]", false, "[]"));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("status")).isNotNull();
        assertThat(connector.getOutputs().get("signerStatusesJson")).isNotNull();
        assertThat(connector.getOutputs().get("isTerminal")).isNotNull();
        assertThat(connector.getOutputs().get("signedDocumentIdsJson")).isNotNull();
        assertThat(connector.getOutputs().get("success")).isEqualTo(true);
    }

    @Test
    void should_set_error_outputs_on_failure() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.getStatus("req-123")).thenThrow(
                new YousignException("Resource not found", 404, false));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("success")).isEqualTo(false);
        assertThat(connector.getOutputs().get("errorMessage")).isNotNull();
    }

    @Test
    void should_fail_validation_when_signatureRequestId_is_blank() {
        var inputs = validInputs();
        inputs.put("signatureRequestId", "  ");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class)
                .hasMessageContaining("signatureRequestId is mandatory");
    }
}
