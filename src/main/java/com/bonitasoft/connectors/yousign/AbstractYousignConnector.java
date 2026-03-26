package com.bonitasoft.connectors.yousign;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;

/**
 * Abstract base connector for Yousign eSignature.
 * Handles connection lifecycle, validation, and standard error handling.
 */
@Slf4j
public abstract class AbstractYousignConnector extends AbstractConnector {

    // Output parameter constants shared across all operations
    protected static final String OUTPUT_SUCCESS = "success";
    protected static final String OUTPUT_ERROR_MESSAGE = "errorMessage";

    protected YousignConfiguration configuration;
    protected YousignClient client;

    @Override
    public void validateInputParameters() throws ConnectorValidationException {
        try {
            this.configuration = buildConfiguration();
            validateConfiguration(this.configuration);
        } catch (IllegalArgumentException e) {
            throw new ConnectorValidationException(this, e.getMessage());
        }
    }

    @Override
    public void connect() throws ConnectorException {
        try {
            this.client = new YousignClient(this.configuration);
            log.info("Yousign connector connected successfully");
        } catch (YousignException e) {
            throw new ConnectorException("Failed to connect: " + e.getMessage(), e);
        }
    }

    @Override
    public void disconnect() throws ConnectorException {
        this.client = null;
    }

    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        try {
            doExecute();
            setOutputParameter(OUTPUT_SUCCESS, true);
        } catch (YousignException e) {
            log.error("Yousign connector execution failed: {}", e.getMessage(), e);
            setOutputParameter(OUTPUT_SUCCESS, false);
            setOutputParameter(OUTPUT_ERROR_MESSAGE, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error in Yousign connector: {}", e.getMessage(), e);
            setOutputParameter(OUTPUT_SUCCESS, false);
            setOutputParameter(OUTPUT_ERROR_MESSAGE, "Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Subclasses implement this to perform their specific operation.
     */
    protected abstract void doExecute() throws YousignException;

    /**
     * Subclasses implement this to build their configuration from input parameters.
     */
    protected abstract YousignConfiguration buildConfiguration();

    /**
     * Validates shared connection parameters.
     */
    protected void validateConfiguration(YousignConfiguration config) {
        if (config.getApiKey() == null || config.getApiKey().isBlank()) {
            throw new IllegalArgumentException("apiKey is mandatory");
        }
        if (config.getBaseUrl() == null || config.getBaseUrl().isBlank()) {
            throw new IllegalArgumentException("baseUrl is mandatory");
        }
    }

    /** Helper: read a String input, returning null if not set. */
    protected String readStringInput(String name) {
        Object value = getInputParameter(name);
        return value != null ? value.toString() : null;
    }

    /** Helper: read a String input with a default value. */
    protected String readStringInput(String name, String defaultValue) {
        String value = readStringInput(name);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }

    /** Helper: read a Boolean input with a default value. */
    protected Boolean readBooleanInput(String name, boolean defaultValue) {
        Object value = getInputParameter(name);
        return value != null ? (Boolean) value : defaultValue;
    }

    /** Helper: read an Integer input with a default value. */
    protected Integer readIntegerInput(String name, int defaultValue) {
        Object value = getInputParameter(name);
        return value != null ? ((Number) value).intValue() : defaultValue;
    }

    /** Helper: read a Long input with a default value. */
    protected Long readLongInput(String name, long defaultValue) {
        Object value = getInputParameter(name);
        return value != null ? ((Number) value).longValue() : defaultValue;
    }

    /**
     * Returns output parameters map. Package-private for test access
     * since Bonita's AbstractConnector.getOutputParameters() is protected.
     */
    java.util.Map<String, Object> getOutputs() {
        return getOutputParameters();
    }
}
