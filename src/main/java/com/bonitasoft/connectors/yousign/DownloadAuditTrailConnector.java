package com.bonitasoft.connectors.yousign;

import lombok.extern.slf4j.Slf4j;

/**
 * Downloads the audit trail for a signature request.
 * API: GET /signature_requests/{id}/audit_trails/download
 */
@Slf4j
public class DownloadAuditTrailConnector extends AbstractYousignConnector {

    static final String INPUT_API_KEY = "apiKey";
    static final String INPUT_BASE_URL = "baseUrl";
    static final String INPUT_CONNECT_TIMEOUT = "connectTimeout";
    static final String INPUT_READ_TIMEOUT = "readTimeout";
    static final String INPUT_SIGNATURE_REQUEST_ID = "signatureRequestId";
    static final String INPUT_SIGNER_IDS_JSON = "signerIdsJson";

    static final String OUTPUT_AUDIT_TRAIL_CONTENT = "auditTrailContent";
    static final String OUTPUT_AUDIT_TRAIL_NAME = "auditTrailName";
    static final String OUTPUT_FILE_SIZE_BYTES = "fileSizeBytes";

    @Override
    protected YousignConfiguration buildConfiguration() {
        return YousignConfiguration.builder()
                .apiKey(readStringInput(INPUT_API_KEY))
                .baseUrl(readStringInput(INPUT_BASE_URL, "https://api-sandbox.yousign.app/v3"))
                .connectTimeout(readIntegerInput(INPUT_CONNECT_TIMEOUT, 30000))
                .readTimeout(readIntegerInput(INPUT_READ_TIMEOUT, 60000))
                .signatureRequestId(readStringInput(INPUT_SIGNATURE_REQUEST_ID))
                .signerIdsJson(readStringInput(INPUT_SIGNER_IDS_JSON))
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
        log.info("Executing DownloadAuditTrail connector for requestId={}", configuration.getSignatureRequestId());
        YousignClient.DownloadAuditTrailResult result = client.downloadAuditTrail(
                configuration.getSignatureRequestId(), configuration.getSignerIdsJson());
        setOutputParameter(OUTPUT_AUDIT_TRAIL_CONTENT, result.auditTrailContent());
        setOutputParameter(OUTPUT_AUDIT_TRAIL_NAME, result.auditTrailName());
        setOutputParameter(OUTPUT_FILE_SIZE_BYTES, result.fileSizeBytes());
        log.info("DownloadAuditTrail connector executed successfully");
    }
}
