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
class DownloadDocumentConnectorTest {

    @Mock
    private YousignClient mockClient;

    private DownloadDocumentConnector connector;

    @BeforeEach
    void setUp() {
        connector = new DownloadDocumentConnector();
    }

    private Map<String, Object> validInputs() {
        var inputs = new HashMap<String, Object>();
        inputs.put("apiKey", "test-api-key");
        inputs.put("signatureRequestId", "req-123");
        inputs.put("documentId", "doc-456");
        return inputs;
    }

    private void injectMockClient() throws Exception {
        var clientField = AbstractYousignConnector.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(connector, mockClient);
    }

    @Test
    void should_execute_successfully_when_downloading_single_document() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.downloadDocument("req-123", "doc-456")).thenReturn(
                new YousignClient.DownloadDocumentResult("base64content", "contract.pdf", "application/pdf", 12345L));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("documentContent")).isEqualTo("base64content");
        assertThat(connector.getOutputs().get("documentName")).isEqualTo("contract.pdf");
        assertThat(connector.getOutputs().get("contentType")).isEqualTo("application/pdf");
        assertThat(connector.getOutputs().get("fileSizeBytes")).isEqualTo(12345L);
        assertThat(connector.getOutputs().get("success")).isEqualTo(true);
    }

    @Test
    void should_download_all_documents_as_zip_when_no_documentId() throws Exception {
        var inputs = validInputs();
        inputs.remove("documentId");
        connector.setInputParameters(inputs);
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.downloadDocument("req-123", null)).thenReturn(
                new YousignClient.DownloadDocumentResult("zipbase64", "documents.zip", "application/zip", 99999L));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("contentType")).isEqualTo("application/zip");
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
    void should_fail_on_access_denied() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.downloadDocument(anyString(), anyString())).thenThrow(
                new YousignException("Access denied: document not ready", 403, false));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("success")).isEqualTo(false);
    }

    @Test
    void should_handle_network_timeout() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.downloadDocument(anyString(), anyString())).thenThrow(
                new YousignException("Network error", new java.net.SocketTimeoutException()));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("success")).isEqualTo(false);
    }

    @Test
    void should_populate_all_output_fields() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.downloadDocument("req-123", "doc-456")).thenReturn(
                new YousignClient.DownloadDocumentResult("content", "file.pdf", "application/pdf", 100L));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("documentContent")).isNotNull();
        assertThat(connector.getOutputs().get("documentName")).isNotNull();
        assertThat(connector.getOutputs().get("contentType")).isNotNull();
        assertThat(connector.getOutputs().get("fileSizeBytes")).isNotNull();
        assertThat(connector.getOutputs().get("success")).isEqualTo(true);
    }

    @Test
    void should_set_error_outputs_on_failure() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.downloadDocument(anyString(), anyString())).thenThrow(
                new YousignException("Not found", 404, false));

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
