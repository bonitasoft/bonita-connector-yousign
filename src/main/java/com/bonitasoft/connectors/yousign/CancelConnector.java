package com.bonitasoft.connectors.yousign;

import lombok.extern.slf4j.Slf4j;

/**
 * Cancels a signature request.
 * API: POST /signature_requests/{id}/cancel
 */
@Slf4j
public class CancelConnector extends AbstractYousignConnector {

    static final String INPUT_API_KEY = "apiKey";
    static final String INPUT_BASE_URL = "baseUrl";
    static final String INPUT_CONNECT_TIMEOUT = "connectTimeout";
    static final String INPUT_READ_TIMEOUT = "readTimeout";
    static final String INPUT_SIGNATURE_REQUEST_ID = "signatureRequestId";
    static final String INPUT_CANCELLATION_REASON = "cancellationReason";

    @Override
    protected YousignConfiguration buildConfiguration() {
        return YousignConfiguration.builder()
                .apiKey(readStringInput(INPUT_API_KEY))
                .baseUrl(readStringInput(INPUT_BASE_URL, "https://api-sandbox.yousign.app/v3"))
                .connectTimeout(readIntegerInput(INPUT_CONNECT_TIMEOUT, 30000))
                .readTimeout(readIntegerInput(INPUT_READ_TIMEOUT, 60000))
                .signatureRequestId(readStringInput(INPUT_SIGNATURE_REQUEST_ID))
                .cancellationReason(readStringInput(INPUT_CANCELLATION_REASON, "Cancelled by Bonita process"))
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
        log.info("Executing Cancel connector for requestId={}", configuration.getSignatureRequestId());
        client.cancel(configuration.getSignatureRequestId(), configuration.getCancellationReason());
        log.info("Cancel connector executed successfully");
    }
}
