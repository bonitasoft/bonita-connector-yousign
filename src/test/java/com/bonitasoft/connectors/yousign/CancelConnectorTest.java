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
class CancelConnectorTest {

    @Mock
    private YousignClient mockClient;

    private CancelConnector connector;

    @BeforeEach
    void setUp() {
        connector = new CancelConnector();
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
        doNothing().when(mockClient).cancel(eq("req-123"), anyString());

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("success")).isEqualTo(true);
    }

    @Test
    void should_use_custom_cancellation_reason() throws Exception {
        var inputs = validInputs();
        inputs.put("cancellationReason", "Custom reason");
        connector.setInputParameters(inputs);
        connector.validateInputParameters();
        injectMockClient();
        doNothing().when(mockClient).cancel("req-123", "Custom reason");

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("success")).isEqualTo(true);
        verify(mockClient).cancel("req-123", "Custom reason");
    }

    @Test
    void should_use_default_cancellation_reason_when_not_set() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        doNothing().when(mockClient).cancel(eq("req-123"), eq("Cancelled by Bonita process"));

        connector.executeBusinessLogic();

        verify(mockClient).cancel("req-123", "Cancelled by Bonita process");
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
        doThrow(new YousignException("Auth error", 401, false)).when(mockClient).cancel(anyString(), anyString());

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("success")).isEqualTo(false);
    }

    @Test
    void should_handle_status_transition_error() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        doThrow(new YousignException("Cannot cancel a done request", 400, false))
                .when(mockClient).cancel(anyString(), anyString());

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("success")).isEqualTo(false);
        assertThat((String) connector.getOutputs().get("errorMessage")).contains("Cannot cancel");
    }

    @Test
    void should_set_error_outputs_on_failure() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        doThrow(new YousignException("Server error", 500, true)).when(mockClient).cancel(anyString(), anyString());

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
