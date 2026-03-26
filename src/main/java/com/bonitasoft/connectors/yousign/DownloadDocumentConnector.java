package com.bonitasoft.connectors.yousign;

import lombok.extern.slf4j.Slf4j;

/**
 * Downloads a signed document or all documents as ZIP.
 * API: GET /signature_requests/{id}/documents/{docId}/download (single)
 *      GET /signature_requests/{id}/documents/download (all=ZIP)
 */
@Slf4j
public class DownloadDocumentConnector extends AbstractYousignConnector {

    static final String INPUT_API_KEY = "apiKey";
    static final String INPUT_BASE_URL = "baseUrl";
    static final String INPUT_CONNECT_TIMEOUT = "connectTimeout";
    static final String INPUT_READ_TIMEOUT = "readTimeout";
    static final String INPUT_SIGNATURE_REQUEST_ID = "signatureRequestId";
    static final String INPUT_DOCUMENT_ID = "documentId";

    static final String OUTPUT_DOCUMENT_CONTENT = "documentContent";
    static final String OUTPUT_DOCUMENT_NAME = "documentName";
    static final String OUTPUT_CONTENT_TYPE = "contentType";
    static final String OUTPUT_FILE_SIZE_BYTES = "fileSizeBytes";

    @Override
    protected YousignConfiguration buildConfiguration() {
        return YousignConfiguration.builder()
                .apiKey(readStringInput(INPUT_API_KEY))
                .baseUrl(readStringInput(INPUT_BASE_URL, "https://api-sandbox.yousign.app/v3"))
                .connectTimeout(readIntegerInput(INPUT_CONNECT_TIMEOUT, 30000))
                .readTimeout(readIntegerInput(INPUT_READ_TIMEOUT, 60000))
                .signatureRequestId(readStringInput(INPUT_SIGNATURE_REQUEST_ID))
                .documentId(readStringInput(INPUT_DOCUMENT_ID))
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
        log.info("Executing DownloadDocument connector for requestId={}", configuration.getSignatureRequestId());
        YousignClient.DownloadDocumentResult result = client.downloadDocument(
                configuration.getSignatureRequestId(), configuration.getDocumentId());
        setOutputParameter(OUTPUT_DOCUMENT_CONTENT, result.documentContent());
        setOutputParameter(OUTPUT_DOCUMENT_NAME, result.documentName());
        setOutputParameter(OUTPUT_CONTENT_TYPE, result.contentType());
        setOutputParameter(OUTPUT_FILE_SIZE_BYTES, result.fileSizeBytes());
        log.info("DownloadDocument connector executed successfully, fileName={}", result.documentName());
    }
}
