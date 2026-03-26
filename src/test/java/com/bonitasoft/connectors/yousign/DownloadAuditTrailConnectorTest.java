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
class DownloadAuditTrailConnectorTest {

    @Mock
    private YousignClient mockClient;

    private DownloadAuditTrailConnector connector;

    @BeforeEach
    void setUp() {
        connector = new DownloadAuditTrailConnector();
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
    void should_execute_successfully() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.downloadAuditTrail("req-123", null)).thenReturn(
                new YousignClient.DownloadAuditTrailResult("pdfbase64", "audit-trail.pdf", 5000L));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("auditTrailContent")).isEqualTo("pdfbase64");
        assertThat(connector.getOutputs().get("auditTrailName")).isEqualTo("audit-trail.pdf");
        assertThat(connector.getOutputs().get("fileSizeBytes")).isEqualTo(5000L);
        assertThat(connector.getOutputs().get("success")).isEqualTo(true);
    }

    @Test
    void should_pass_signer_ids_when_provided() throws Exception {
        var inputs = validInputs();
        inputs.put("signerIdsJson", "[\"signer-1\",\"signer-2\"]");
        connector.setInputParameters(inputs);
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.downloadAuditTrail("req-123", "[\"signer-1\",\"signer-2\"]")).thenReturn(
                new YousignClient.DownloadAuditTrailResult("content", "audit.pdf", 3000L));

        connector.executeBusinessLogic();

        verify(mockClient).downloadAuditTrail("req-123", "[\"signer-1\",\"signer-2\"]");
        assertThat(connector.getOutputs().get("success")).isEqualTo(true);
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
        when(mockClient.downloadAuditTrail(anyString(), any())).thenThrow(
                new YousignException("Auth error", 401, false));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("success")).isEqualTo(false);
    }

    @Test
    void should_handle_network_timeout() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.downloadAuditTrail(anyString(), any())).thenThrow(
                new YousignException("Network error", new java.net.SocketTimeoutException()));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("success")).isEqualTo(false);
    }

    @Test
    void should_populate_all_output_fields() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.downloadAuditTrail("req-123", null)).thenReturn(
                new YousignClient.DownloadAuditTrailResult("c", "a.pdf", 1L));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("auditTrailContent")).isNotNull();
        assertThat(connector.getOutputs().get("auditTrailName")).isNotNull();
        assertThat(connector.getOutputs().get("fileSizeBytes")).isNotNull();
    }

    @Test
    void should_set_error_outputs_on_failure() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.downloadAuditTrail(anyString(), any())).thenThrow(
                new YousignException("Not found", 404, false));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("success")).isEqualTo(false);
        assertThat(connector.getOutputs().get("errorMessage")).isNotNull();
    }
}
