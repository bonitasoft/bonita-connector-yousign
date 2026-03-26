package com.bonitasoft.connectors.yousign;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * Integration test for CreateFromTemplate connector.
 * Skipped unless YOUSIGN_API_KEY environment variable is set.
 */
@EnabledIfEnvironmentVariable(named = "YOUSIGN_API_KEY", matches = ".+")
class CreateFromTemplateConnectorIntegrationTest {

    @Test
    void should_create_signature_request_from_template_against_real_api() throws Exception {
        var connector = new CreateFromTemplateConnector();
        var inputs = new java.util.HashMap<String, Object>();
        inputs.put("apiKey", System.getenv("YOUSIGN_API_KEY"));
        inputs.put("baseUrl", System.getenv().getOrDefault("YOUSIGN_BASE_URL", "https://api-sandbox.yousign.app/v3"));
        inputs.put("templateId", System.getenv("YOUSIGN_TEMPLATE_ID"));
        inputs.put("requestName", "Integration Test - " + System.currentTimeMillis());
        inputs.put("signerLabel", System.getenv().getOrDefault("YOUSIGN_SIGNER_LABEL", "Signer"));
        inputs.put("signerFirstName", "Test");
        inputs.put("signerLastName", "User");
        inputs.put("signerEmail", System.getenv().getOrDefault("YOUSIGN_SIGNER_EMAIL", "test@example.com"));
        connector.setInputParameters(inputs);
        connector.validateInputParameters();
        connector.connect();
        connector.executeBusinessLogic();
        connector.disconnect();
    }
}
