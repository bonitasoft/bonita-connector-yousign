package com.bonitasoft.connectors.yousign;

import lombok.Builder;
import lombok.Data;

/**
 * Configuration for Yousign connector.
 * Holds all connection and operation parameters.
 */
@Data
@Builder
public class YousignConfiguration {

    // === Connection / Auth parameters ===
    private String apiKey;

    @Builder.Default
    private String baseUrl = "https://api-sandbox.yousign.app/v3";

    @Builder.Default
    private int connectTimeout = 30000;

    @Builder.Default
    private int readTimeout = 60000;

    // === Create From Template parameters ===
    private String templateId;
    private String requestName;
    private String externalId;
    private String deliveryMode;
    private Boolean orderedSigners;
    private String expirationDate;
    private String signerLabel;
    private String signerFirstName;
    private String signerLastName;
    private String signerEmail;
    private String signerPhoneNumber;
    private String signerLocale;
    private String templateTextFieldsJson;
    private String additionalSignersJson;

    // === Activate / Get Status / Cancel / Download parameters ===
    private String signatureRequestId;
    private String cancellationReason;
    private String documentId;
    private String signerIdsJson;

    // === List parameters ===
    private String statusFilter;
    private String externalIdFilter;
    private String afterCursor;
    @Builder.Default
    private int pageSize = 20;
    private String sortOrder;

    // === Register Webhook parameters ===
    private Boolean sandbox;
    private String webhookSecret;
    private String webhookEndpointUrl;
    private String subscribedEventsJson;
    private Boolean autoRetry;
    private Boolean enabled;

    // === Retry settings ===
    @Builder.Default
    private int maxRetries = 3;
}
