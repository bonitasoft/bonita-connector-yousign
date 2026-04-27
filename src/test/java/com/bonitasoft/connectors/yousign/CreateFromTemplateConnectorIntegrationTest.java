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
        // Signer data goes inside template_placeholders.signers (Yousign v3 contract).
        String signerLabel = System.getenv().getOrDefault("YOUSIGN_SIGNER_LABEL", "signer1");
        String signerEmail = System.getenv().getOrDefault("YOUSIGN_SIGNER_EMAIL", "test@example.com");
        inputs.put("templatePlaceholdersJson",
                "{\"signers\":[{\"label\":\"" + signerLabel + "\",\"info\":{\"first_name\":\"Test\",\"last_name\":\"User\",\"email\":\"" + signerEmail + "\",\"locale\":\"en\"}}],\"read_only_text_fields\":[]}");
        connector.setInputParameters(inputs);
        connector.validateInputParameters();
        connector.connect();
        connector.executeBusinessLogic();
        connector.disconnect();
    }
}
