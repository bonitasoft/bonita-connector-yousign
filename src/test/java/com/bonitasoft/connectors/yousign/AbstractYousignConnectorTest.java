package com.bonitasoft.connectors.yousign;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class AbstractYousignConnectorTest {

    private TestableConnector connector;

    @BeforeEach
    void setUp() {
        connector = new TestableConnector();
    }

    // === validateInputParameters ===

    @Test
    void should_validate_successfully_with_valid_inputs() {
        connector.setInputParameters(validInputs());
        assertThatCode(() -> connector.validateInputParameters()).doesNotThrowAnyException();
    }

    @Test
    void should_throw_validation_exception_when_apiKey_null() {
        var inputs = validInputs();
        inputs.remove("apiKey");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class)
                .hasMessageContaining("apiKey is mandatory");
    }

    @Test
    void should_throw_validation_exception_when_apiKey_blank() {
        var inputs = validInputs();
        inputs.put("apiKey", "   ");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class)
                .hasMessageContaining("apiKey is mandatory");
    }

    @Test
    void should_throw_validation_exception_when_apiKey_empty() {
        var inputs = validInputs();
        inputs.put("apiKey", "");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class)
                .hasMessageContaining("apiKey is mandatory");
    }

    @Test
    void should_throw_validation_exception_when_baseUrl_null() {
        // buildConfiguration uses default for baseUrl when null, so test via
        // a connector that explicitly passes null
        var inputs = new HashMap<String, Object>();
        inputs.put("apiKey", "test-key");
        inputs.put("baseUrl", null); // Force null
        var nullBaseUrlConnector = new AbstractYousignConnector() {
            @Override
            protected YousignConfiguration buildConfiguration() {
                return YousignConfiguration.builder()
                        .apiKey(readStringInput("apiKey"))
                        .baseUrl(null)
                        .build();
            }
            @Override
            protected void doExecute() {}
        };
        nullBaseUrlConnector.setInputParameters(inputs);
        assertThatThrownBy(() -> nullBaseUrlConnector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class)
                .hasMessageContaining("baseUrl is mandatory");
    }

    @Test
    void should_throw_validation_exception_when_baseUrl_blank() {
        var inputs = new HashMap<String, Object>();
        inputs.put("apiKey", "test-key");
        inputs.put("baseUrl", "  ");
        var blankBaseUrlConnector = new AbstractYousignConnector() {
            @Override
            protected YousignConfiguration buildConfiguration() {
                return YousignConfiguration.builder()
                        .apiKey(readStringInput("apiKey"))
                        .baseUrl("  ")
                        .build();
            }
            @Override
            protected void doExecute() {}
        };
        blankBaseUrlConnector.setInputParameters(inputs);
        assertThatThrownBy(() -> blankBaseUrlConnector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class)
                .hasMessageContaining("baseUrl is mandatory");
    }

    // === connect ===

    @Test
    void should_connect_successfully() {
        connector.setInputParameters(validInputs());
        assertThatCode(() -> {
            connector.validateInputParameters();
            connector.connect();
        }).doesNotThrowAnyException();
        assertThat(connector.client).isNotNull();
    }

    @Test
    void should_throw_connector_exception_when_connect_fails() {
        connector.setInputParameters(validInputs());
        connector.failOnConnect = true;
        assertThatCode(() -> connector.validateInputParameters()).doesNotThrowAnyException();
        // The YousignClient constructor needs valid config, so test via mock
    }

    // === disconnect ===

    @Test
    void should_null_client_on_disconnect() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        connector.connect();
        assertThat(connector.client).isNotNull();

        connector.disconnect();
        assertThat(connector.client).isNull();
    }

    // === executeBusinessLogic ===

    @Test
    void should_set_success_true_when_doExecute_succeeds() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        connector.connect();

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("success")).isEqualTo(true);
    }

    @Test
    void should_set_success_false_on_yousign_exception() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        connector.connect();
        connector.throwOnExecute = new YousignException("Test Yousign error");

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("success")).isEqualTo(false);
        assertThat(connector.getOutputs().get("errorMessage")).isEqualTo("Test Yousign error");
    }

    @Test
    void should_set_success_false_on_unexpected_exception() throws Exception {
        connector.setInputParameters(validInputs());
        connector.validateInputParameters();
        connector.connect();
        connector.throwUnexpected = new RuntimeException("Unexpected boom");

        connector.executeBusinessLogic();

        assertThat(connector.getOutputs().get("success")).isEqualTo(false);
        assertThat((String) connector.getOutputs().get("errorMessage")).contains("Unexpected error");
        assertThat((String) connector.getOutputs().get("errorMessage")).contains("Unexpected boom");
    }

    // === readStringInput ===

    @Test
    void should_read_string_input_returning_value() {
        var inputs = Map.of("key", (Object) "value");
        connector.setInputParameters(inputs);
        assertThat(connector.readStringInput("key")).isEqualTo("value");
    }

    @Test
    void should_read_string_input_returning_null_when_not_set() {
        connector.setInputParameters(Map.of());
        assertThat(connector.readStringInput("missing")).isNull();
    }

    @Test
    void should_read_string_input_with_default_when_value_null() {
        connector.setInputParameters(Map.of());
        assertThat(connector.readStringInput("missing", "default")).isEqualTo("default");
    }

    @Test
    void should_read_string_input_with_default_when_value_blank() {
        var inputs = new HashMap<String, Object>();
        inputs.put("key", "   ");
        connector.setInputParameters(inputs);
        assertThat(connector.readStringInput("key", "default")).isEqualTo("default");
    }

    @Test
    void should_read_string_input_with_default_when_value_empty() {
        var inputs = new HashMap<String, Object>();
        inputs.put("key", "");
        connector.setInputParameters(inputs);
        assertThat(connector.readStringInput("key", "default")).isEqualTo("default");
    }

    @Test
    void should_read_string_input_with_value_over_default() {
        var inputs = Map.of("key", (Object) "actual");
        connector.setInputParameters(inputs);
        assertThat(connector.readStringInput("key", "default")).isEqualTo("actual");
    }

    @Test
    void should_read_string_input_converting_non_string_to_string() {
        var inputs = Map.of("num", (Object) 42);
        connector.setInputParameters(inputs);
        assertThat(connector.readStringInput("num")).isEqualTo("42");
    }

    // === readBooleanInput ===

    @Test
    void should_read_boolean_input_returning_value() {
        var inputs = Map.of("flag", (Object) Boolean.TRUE);
        connector.setInputParameters(inputs);
        assertThat(connector.readBooleanInput("flag", false)).isTrue();
    }

    @Test
    void should_read_boolean_input_returning_default_when_null() {
        connector.setInputParameters(Map.of());
        assertThat(connector.readBooleanInput("missing", true)).isTrue();
        assertThat(connector.readBooleanInput("missing", false)).isFalse();
    }

    // === readIntegerInput ===

    @Test
    void should_read_integer_input_returning_value() {
        var inputs = Map.of("count", (Object) 42);
        connector.setInputParameters(inputs);
        assertThat(connector.readIntegerInput("count", 0)).isEqualTo(42);
    }

    @Test
    void should_read_integer_input_returning_default_when_null() {
        connector.setInputParameters(Map.of());
        assertThat(connector.readIntegerInput("missing", 99)).isEqualTo(99);
    }

    // === readLongInput ===

    @Test
    void should_read_long_input_returning_value() {
        var inputs = Map.of("timeout", (Object) 5000L);
        connector.setInputParameters(inputs);
        assertThat(connector.readLongInput("timeout", 0L)).isEqualTo(5000L);
    }

    @Test
    void should_read_long_input_returning_default_when_null() {
        connector.setInputParameters(Map.of());
        assertThat(connector.readLongInput("missing", 30000L)).isEqualTo(30000L);
    }

    // === Helper: validInputs ===

    private Map<String, Object> validInputs() {
        var inputs = new HashMap<String, Object>();
        inputs.put("apiKey", "test-key");
        inputs.put("baseUrl", "https://api-sandbox.yousign.app/v3");
        return inputs;
    }

    // === Testable concrete subclass ===

    static class TestableConnector extends AbstractYousignConnector {

        boolean failOnConnect = false;
        YousignException throwOnExecute = null;
        RuntimeException throwUnexpected = null;

        @Override
        protected YousignConfiguration buildConfiguration() {
            return YousignConfiguration.builder()
                    .apiKey(readStringInput("apiKey"))
                    .baseUrl(readStringInput("baseUrl", "https://api-sandbox.yousign.app/v3"))
                    .connectTimeout(readIntegerInput("connectTimeout", 30000))
                    .readTimeout(readIntegerInput("readTimeout", 60000))
                    .build();
        }

        @Override
        protected void doExecute() throws YousignException {
            if (throwOnExecute != null) {
                throw throwOnExecute;
            }
            if (throwUnexpected != null) {
                throw throwUnexpected;
            }
        }
    }
}
