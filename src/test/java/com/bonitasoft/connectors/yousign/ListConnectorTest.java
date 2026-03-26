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
class ListConnectorTest {

    @Mock
    private YousignClient mockClient;

    private ListConnector connector;

    @BeforeEach
    void setUp() {
        connector = new ListConnector();
    }

    private Map<String, Object> validInputs() {
        var inputs = new HashMap<String, Object>();
        inputs.put("apiKey", "test-api-key");
        return inputs;
    }

    private void injectMockClient() throws Exception {
        var clientField = AbstractYousignConnector.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(connector, mockClient);
    }

    @Test
    void should_execute_successfully_with_results() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.list(any())).thenReturn(
                new YousignClient.ListResult("[{\"id\":\"req-1\"}]", "cursor-abc", true));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("signatureRequestsJson")).isEqualTo("[{\"id\":\"req-1\"}]");
        assertThat(connector.getOutputs().get("nextCursor")).isEqualTo("cursor-abc");
        assertThat(connector.getOutputs().get("hasMore")).isEqualTo(true);
        assertThat(connector.getOutputs().get("success")).isEqualTo(true);
    }

    @Test
    void should_execute_successfully_with_no_more_results() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.list(any())).thenReturn(
                new YousignClient.ListResult("[]", null, false));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("hasMore")).isEqualTo(false);
        assertThat(connector.getOutputs().get("success")).isEqualTo(true);
    }

    @Test
    void should_fail_validation_when_apiKey_missing() {
        var inputs = new HashMap<String, Object>();
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class);
    }

    @Test
    void should_fail_immediately_on_auth_error() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.list(any())).thenThrow(
                new YousignException("Auth error", 401, false));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("success")).isEqualTo(false);
    }

    @Test
    void should_handle_network_timeout() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.list(any())).thenThrow(
                new YousignException("Network error", new java.net.SocketTimeoutException()));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("success")).isEqualTo(false);
    }

    @Test
    void should_apply_default_page_size() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        // Default pageSize should be 20
        var configField = AbstractYousignConnector.class.getDeclaredField("configuration");
        configField.setAccessible(true);
        var config = (YousignConfiguration) configField.get(connector);
        assertThat(config.getPageSize()).isEqualTo(20);
    }

    @Test
    void should_populate_all_output_fields() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.list(any())).thenReturn(
                new YousignClient.ListResult("[]", null, false));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("signatureRequestsJson")).isNotNull();
        assertThat(connector.getOutputs().get("hasMore")).isNotNull();
    }

    @Test
    void should_set_error_outputs_on_failure() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        injectMockClient();
        when(mockClient.list(any())).thenThrow(
                new YousignException("Server error", 500, true));

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("success")).isEqualTo(false);
        assertThat(connector.getOutputs().get("errorMessage")).isNotNull();
    }
}
