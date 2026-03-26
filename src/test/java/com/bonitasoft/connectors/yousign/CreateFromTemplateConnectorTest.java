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
class CreateFromTemplateConnectorTest {

    @Mock
    private YousignClient mockClient;

    private CreateFromTemplateConnector connector;

    @BeforeEach
    void setUp() {
        connector = new CreateFromTemplateConnector();
    }

    private Map<String, Object> validInputs() {
        var inputs = new HashMap<String, Object>();
        inputs.put("apiKey", "test-api-key");
        inputs.put("baseUrl", "https://api-sandbox.yousign.app/v3");
        inputs.put("templateId", "tmpl-123");
        inputs.put("requestName", "Test Request");
        inputs.put("signerLabel", "Signer 1");
        inputs.put("signerFirstName", "John");
        inputs.put("signerLastName", "Doe");
        inputs.put("signerEmail", "john@example.com");
        return inputs;
    }

    private void injectMockClient() throws Exception {
        var clientField = AbstractYousignConnector.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(connector, mockClient);
    }

    @Test
    void should_execute_successfully_when_all_mandatory_inputs_set() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.createFromTemplate(any())).thenReturn(
                new YousignClient.CreateFromTemplateResult("req-456", "draft"));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("signatureRequestId")).isEqualTo("req-456");
        assertThat(connector.getOutputs().get("status")).isEqualTo("draft");
        assertThat(connector.getOutputs().get("success")).isEqualTo(true);
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
    void should_fail_validation_when_templateId_missing() {
        var inputs = validInputs();
        inputs.remove("templateId");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class);
    }

    @Test
    void should_fail_validation_when_requestName_missing() {
        var inputs = validInputs();
        inputs.remove("requestName");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class);
    }

    @Test
    void should_fail_validation_when_signerLabel_missing() {
        var inputs = validInputs();
        inputs.remove("signerLabel");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class);
    }

    @Test
    void should_fail_validation_when_signerFirstName_missing() {
        var inputs = validInputs();
        inputs.remove("signerFirstName");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class);
    }

    @Test
    void should_fail_validation_when_signerLastName_missing() {
        var inputs = validInputs();
        inputs.remove("signerLastName");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class);
    }

    @Test
    void should_fail_validation_when_signerEmail_missing() {
        var inputs = validInputs();
        inputs.remove("signerEmail");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class);
    }

    @Test
    void should_fail_immediately_on_auth_error() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.createFromTemplate(any())).thenThrow(
                new YousignException("Authentication error: invalid API key", 401, false));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("success")).isEqualTo(false);
        assertThat((String) connector.getOutputs().get("errorMessage")).contains("Authentication error");
    }

    @Test
    void should_handle_network_timeout() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.createFromTemplate(any())).thenThrow(
                new YousignException("Network error: timeout", new java.net.SocketTimeoutException("timeout")));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("success")).isEqualTo(false);
        assertThat((String) connector.getOutputs().get("errorMessage")).contains("Network error");
    }

    @Test
    void should_apply_defaults_for_null_optional_inputs() throws Exception {
        var inputs = validInputs();
        // Don't set optional inputs
        connector.setInputParameters(inputs);
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.createFromTemplate(any())).thenReturn(
                new YousignClient.CreateFromTemplateResult("req-789", "draft"));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("success")).isEqualTo(true);
    }

    @Test
    void should_populate_all_output_fields_on_success() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.createFromTemplate(any())).thenReturn(
                new YousignClient.CreateFromTemplateResult("req-111", "draft"));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("signatureRequestId")).isNotNull();
        assertThat(connector.getOutputs().get("status")).isNotNull();
        assertThat(connector.getOutputs().get("success")).isEqualTo(true);
    }

    @Test
    void should_set_error_outputs_on_failure() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.createFromTemplate(any())).thenThrow(
                new YousignException("Bad request: invalid template", 400, false));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("success")).isEqualTo(false);
        assertThat(connector.getOutputs().get("errorMessage")).isNotNull();
    }

    @Test
    void should_fail_validation_when_apiKey_is_blank() {
        var inputs = validInputs();
        inputs.put("apiKey", "   ");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class);
    }

    @Test
    void should_fail_validation_when_templateId_is_blank() {
        var inputs = validInputs();
        inputs.put("templateId", "   ");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class)
                .hasMessageContaining("templateId is mandatory");
    }

    @Test
    void should_fail_validation_when_requestName_is_blank() {
        var inputs = validInputs();
        inputs.put("requestName", "   ");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class)
                .hasMessageContaining("requestName is mandatory");
    }

    @Test
    void should_fail_validation_when_signerLabel_is_blank() {
        var inputs = validInputs();
        inputs.put("signerLabel", "   ");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class)
                .hasMessageContaining("signerLabel is mandatory");
    }

    @Test
    void should_fail_validation_when_signerFirstName_is_blank() {
        var inputs = validInputs();
        inputs.put("signerFirstName", "   ");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class)
                .hasMessageContaining("signerFirstName is mandatory");
    }

    @Test
    void should_fail_validation_when_signerLastName_is_blank() {
        var inputs = validInputs();
        inputs.put("signerLastName", "   ");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class)
                .hasMessageContaining("signerLastName is mandatory");
    }

    @Test
    void should_fail_validation_when_signerEmail_is_blank() {
        var inputs = validInputs();
        inputs.put("signerEmail", "   ");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class)
                .hasMessageContaining("signerEmail is mandatory");
    }
}
