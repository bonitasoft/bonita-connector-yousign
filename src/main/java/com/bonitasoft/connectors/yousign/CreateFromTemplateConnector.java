package com.bonitasoft.connectors.yousign;

import lombok.extern.slf4j.Slf4j;

/**
 * Creates a signature request from a Yousign template.
 * API: POST /signature_requests (with template_id)
 */
@Slf4j
public class CreateFromTemplateConnector extends AbstractYousignConnector {

    // Input parameter name constants
    static final String INPUT_API_KEY = "apiKey";
    static final String INPUT_BASE_URL = "baseUrl";
    static final String INPUT_CONNECT_TIMEOUT = "connectTimeout";
    static final String INPUT_READ_TIMEOUT = "readTimeout";
    static final String INPUT_TEMPLATE_ID = "templateId";
    static final String INPUT_REQUEST_NAME = "requestName";
    static final String INPUT_EXTERNAL_ID = "externalId";
    static final String INPUT_DELIVERY_MODE = "deliveryMode";
    static final String INPUT_ORDERED_SIGNERS = "orderedSigners";
    static final String INPUT_EXPIRATION_DATE = "expirationDate";
    static final String INPUT_TEMPLATE_PLACEHOLDERS_JSON = "templatePlaceholdersJson";

    // Output parameter name constants
    static final String OUTPUT_SIGNATURE_REQUEST_ID = "signatureRequestId";
    static final String OUTPUT_STATUS = "status";

    @Override
    protected YousignConfiguration buildConfiguration() {
        return YousignConfiguration.builder()
                .apiKey(readStringInput(INPUT_API_KEY))
                .baseUrl(readStringInput(INPUT_BASE_URL, "https://api-sandbox.yousign.app/v3"))
                .connectTimeout(readIntegerInput(INPUT_CONNECT_TIMEOUT, 30000))
                .readTimeout(readIntegerInput(INPUT_READ_TIMEOUT, 60000))
                .templateId(readStringInput(INPUT_TEMPLATE_ID))
                .requestName(readStringInput(INPUT_REQUEST_NAME))
                .externalId(readStringInput(INPUT_EXTERNAL_ID))
                .deliveryMode(readStringInput(INPUT_DELIVERY_MODE, "email"))
                .orderedSigners(readBooleanInput(INPUT_ORDERED_SIGNERS, false))
                .expirationDate(readStringInput(INPUT_EXPIRATION_DATE))
                .templatePlaceholdersJson(readStringInput(INPUT_TEMPLATE_PLACEHOLDERS_JSON))
                .build();
    }

    @Override
    protected void validateConfiguration(YousignConfiguration config) {
        super.validateConfiguration(config);
        if (config.getTemplateId() == null || config.getTemplateId().isBlank()) {
            throw new IllegalArgumentException("templateId is mandatory");
        }
        if (config.getRequestName() == null || config.getRequestName().isBlank()) {
            throw new IllegalArgumentException("requestName is mandatory");
        }
        if (config.getTemplatePlaceholdersJson() == null
                || config.getTemplatePlaceholdersJson().isBlank()) {
            throw new IllegalArgumentException(
                    "templatePlaceholdersJson is mandatory. Pass a JSON object with a non-empty 'signers' array, "
                    + "e.g. {\"signers\":[{\"placeholder_name\":\"signer_1\",\"info\":{...}}]}");
        }
    }

    @Override
    protected void doExecute() throws YousignException {
        log.info("Executing CreateFromTemplate connector");
        YousignClient.CreateFromTemplateResult result = client.createFromTemplate(configuration);
        setOutputParameter(OUTPUT_SIGNATURE_REQUEST_ID, result.signatureRequestId());
        setOutputParameter(OUTPUT_STATUS, result.status());
        log.info("CreateFromTemplate connector executed successfully, requestId={}", result.signatureRequestId());
    }
}
