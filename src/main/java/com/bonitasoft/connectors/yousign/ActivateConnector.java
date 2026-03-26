package com.bonitasoft.connectors.yousign;

import lombok.extern.slf4j.Slf4j;

/**
 * Activates a signature request.
 * API: POST /signature_requests/{id}/activate
 */
@Slf4j
public class ActivateConnector extends AbstractYousignConnector {

    static final String INPUT_API_KEY = "apiKey";
    static final String INPUT_BASE_URL = "baseUrl";
    static final String INPUT_CONNECT_TIMEOUT = "connectTimeout";
    static final String INPUT_READ_TIMEOUT = "readTimeout";
    static final String INPUT_SIGNATURE_REQUEST_ID = "signatureRequestId";

    static final String OUTPUT_STATUS = "status";
    static final String OUTPUT_SIGNER_LINKS_JSON = "signerLinksJson";

    @Override
    protected YousignConfiguration buildConfiguration() {
        return YousignConfiguration.builder()
                .apiKey(readStringInput(INPUT_API_KEY))
                .baseUrl(readStringInput(INPUT_BASE_URL, "https://api-sandbox.yousign.app/v3"))
                .connectTimeout(readIntegerInput(INPUT_CONNECT_TIMEOUT, 30000))
                .readTimeout(readIntegerInput(INPUT_READ_TIMEOUT, 60000))
                .signatureRequestId(readStringInput(INPUT_SIGNATURE_REQUEST_ID))
                .build();
    }

    @Override
    protected void validateConfiguration(YousignConfiguration config) {
        super.validateConfiguration(config);
        if (config.getSignatureRequestId() == null || config.getSignatureRequestId().isBlank()) {
            throw new IllegalArgumentException("signatureRequestId is mandatory");
        }
    }

    @Override
    protected void doExecute() throws YousignException {
        log.info("Executing Activate connector for requestId={}", configuration.getSignatureRequestId());
        YousignClient.ActivateResult result = client.activate(configuration.getSignatureRequestId());
        setOutputParameter(OUTPUT_STATUS, result.status());
        setOutputParameter(OUTPUT_SIGNER_LINKS_JSON, result.signerLinksJson());
        log.info("Activate connector executed successfully, status={}", result.status());
    }
}
