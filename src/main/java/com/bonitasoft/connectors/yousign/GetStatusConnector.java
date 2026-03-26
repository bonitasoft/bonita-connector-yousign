package com.bonitasoft.connectors.yousign;

import lombok.extern.slf4j.Slf4j;

/**
 * Gets the status of a signature request.
 * API: GET /signature_requests/{id}
 */
@Slf4j
public class GetStatusConnector extends AbstractYousignConnector {

    static final String INPUT_API_KEY = "apiKey";
    static final String INPUT_BASE_URL = "baseUrl";
    static final String INPUT_CONNECT_TIMEOUT = "connectTimeout";
    static final String INPUT_READ_TIMEOUT = "readTimeout";
    static final String INPUT_SIGNATURE_REQUEST_ID = "signatureRequestId";

    static final String OUTPUT_STATUS = "status";
    static final String OUTPUT_SIGNER_STATUSES_JSON = "signerStatusesJson";
    static final String OUTPUT_IS_TERMINAL = "isTerminal";
    static final String OUTPUT_SIGNED_DOCUMENT_IDS_JSON = "signedDocumentIdsJson";

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
        log.info("Executing GetStatus connector for requestId={}", configuration.getSignatureRequestId());
        YousignClient.GetStatusResult result = client.getStatus(configuration.getSignatureRequestId());
        setOutputParameter(OUTPUT_STATUS, result.status());
        setOutputParameter(OUTPUT_SIGNER_STATUSES_JSON, result.signerStatusesJson());
        setOutputParameter(OUTPUT_IS_TERMINAL, result.isTerminal());
        setOutputParameter(OUTPUT_SIGNED_DOCUMENT_IDS_JSON, result.signedDocumentIdsJson());
        log.info("GetStatus connector executed successfully, status={}", result.status());
    }
}
