package com.bonitasoft.connectors.yousign;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = "YOUSIGN_API_KEY", matches = ".+")
class DownloadAuditTrailConnectorIntegrationTest {

    @Test
    void should_download_audit_trail_against_real_api() throws Exception {
        var connector = new DownloadAuditTrailConnector();
        var inputs = new java.util.HashMap<String, Object>();
        inputs.put("apiKey", System.getenv("YOUSIGN_API_KEY"));
        inputs.put("baseUrl", System.getenv().getOrDefault("YOUSIGN_BASE_URL", "https://api-sandbox.yousign.app/v3"));
        inputs.put("signatureRequestId", System.getenv("YOUSIGN_SIGNATURE_REQUEST_ID"));
        connector.setInputParameters(inputs);
        connector.validateInputParameters();
        connector.connect();
        connector.executeBusinessLogic();
        connector.disconnect();
    }
}
