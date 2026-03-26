package com.bonitasoft.connectors.yousign;

import lombok.extern.slf4j.Slf4j;

/**
 * Registers a webhook for Yousign events.
 * API: POST /webhooks
 */
@Slf4j
public class RegisterWebhookConnector extends AbstractYousignConnector {

    static final String INPUT_API_KEY = "apiKey";
    static final String INPUT_BASE_URL = "baseUrl";
    static final String INPUT_CONNECT_TIMEOUT = "connectTimeout";
    static final String INPUT_READ_TIMEOUT = "readTimeout";
    static final String INPUT_SANDBOX = "sandbox";
    static final String INPUT_WEBHOOK_SECRET = "webhookSecret";
    static final String INPUT_WEBHOOK_ENDPOINT_URL = "webhookEndpointUrl";
    static final String INPUT_SUBSCRIBED_EVENTS_JSON = "subscribedEventsJson";
    static final String INPUT_AUTO_RETRY = "autoRetry";
    static final String INPUT_ENABLED = "enabled";

    static final String OUTPUT_WEBHOOK_ID = "webhookId";

    @Override
    protected YousignConfiguration buildConfiguration() {
        return YousignConfiguration.builder()
                .apiKey(readStringInput(INPUT_API_KEY))
                .baseUrl(readStringInput(INPUT_BASE_URL, "https://api-sandbox.yousign.app/v3"))
                .connectTimeout(readIntegerInput(INPUT_CONNECT_TIMEOUT, 30000))
                .readTimeout(readIntegerInput(INPUT_READ_TIMEOUT, 60000))
                .sandbox(readBooleanInput(INPUT_SANDBOX, true))
                .webhookSecret(readStringInput(INPUT_WEBHOOK_SECRET))
                .webhookEndpointUrl(readStringInput(INPUT_WEBHOOK_ENDPOINT_URL))
                .subscribedEventsJson(readStringInput(INPUT_SUBSCRIBED_EVENTS_JSON,
                        "[\"signature_request.done\",\"signature_request.canceled\",\"signature_request.declined\",\"signer.done\"]"))
                .autoRetry(readBooleanInput(INPUT_AUTO_RETRY, true))
                .enabled(readBooleanInput(INPUT_ENABLED, true))
                .build();
    }

    @Override
    protected void validateConfiguration(YousignConfiguration config) {
        super.validateConfiguration(config);
        if (config.getWebhookEndpointUrl() == null || config.getWebhookEndpointUrl().isBlank()) {
            throw new IllegalArgumentException("webhookEndpointUrl is mandatory");
        }
    }

    @Override
    protected void doExecute() throws YousignException {
        log.info("Executing RegisterWebhook connector");
        YousignClient.RegisterWebhookResult result = client.registerWebhook(configuration);
        setOutputParameter(OUTPUT_WEBHOOK_ID, result.webhookId());
        log.info("RegisterWebhook connector executed successfully, webhookId={}", result.webhookId());
    }
}
