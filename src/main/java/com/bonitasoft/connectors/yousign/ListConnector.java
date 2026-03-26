package com.bonitasoft.connectors.yousign;

import lombok.extern.slf4j.Slf4j;

/**
 * Lists signature requests with optional filters.
 * API: GET /signature_requests
 */
@Slf4j
public class ListConnector extends AbstractYousignConnector {

    static final String INPUT_API_KEY = "apiKey";
    static final String INPUT_BASE_URL = "baseUrl";
    static final String INPUT_CONNECT_TIMEOUT = "connectTimeout";
    static final String INPUT_READ_TIMEOUT = "readTimeout";
    static final String INPUT_STATUS_FILTER = "statusFilter";
    static final String INPUT_EXTERNAL_ID_FILTER = "externalIdFilter";
    static final String INPUT_AFTER_CURSOR = "afterCursor";
    static final String INPUT_PAGE_SIZE = "pageSize";
    static final String INPUT_SORT_ORDER = "sortOrder";

    static final String OUTPUT_SIGNATURE_REQUESTS_JSON = "signatureRequestsJson";
    static final String OUTPUT_NEXT_CURSOR = "nextCursor";
    static final String OUTPUT_HAS_MORE = "hasMore";

    @Override
    protected YousignConfiguration buildConfiguration() {
        return YousignConfiguration.builder()
                .apiKey(readStringInput(INPUT_API_KEY))
                .baseUrl(readStringInput(INPUT_BASE_URL, "https://api-sandbox.yousign.app/v3"))
                .connectTimeout(readIntegerInput(INPUT_CONNECT_TIMEOUT, 30000))
                .readTimeout(readIntegerInput(INPUT_READ_TIMEOUT, 60000))
                .statusFilter(readStringInput(INPUT_STATUS_FILTER))
                .externalIdFilter(readStringInput(INPUT_EXTERNAL_ID_FILTER))
                .afterCursor(readStringInput(INPUT_AFTER_CURSOR))
                .pageSize(readIntegerInput(INPUT_PAGE_SIZE, 20))
                .sortOrder(readStringInput(INPUT_SORT_ORDER, "desc"))
                .build();
    }

    @Override
    protected void doExecute() throws YousignException {
        log.info("Executing List connector");
        YousignClient.ListResult result = client.list(configuration);
        setOutputParameter(OUTPUT_SIGNATURE_REQUESTS_JSON, result.signatureRequestsJson());
        setOutputParameter(OUTPUT_NEXT_CURSOR, result.nextCursor());
        setOutputParameter(OUTPUT_HAS_MORE, result.hasMore());
        log.info("List connector executed successfully, hasMore={}", result.hasMore());
    }
}
