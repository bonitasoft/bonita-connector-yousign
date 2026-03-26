package com.bonitasoft.connectors.yousign;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * API client facade for Yousign connector.
 * Uses OkHttp 4.12.0 + Jackson 2.17.2 for REST API communication.
 */
@Slf4j
public class YousignClient {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final YousignConfiguration configuration;
    private final RetryPolicy retryPolicy;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public YousignClient(YousignConfiguration configuration) throws YousignException {
        this.configuration = configuration;
        this.retryPolicy = new RetryPolicy(configuration.getMaxRetries());
        this.objectMapper = new ObjectMapper();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(configuration.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(configuration.getReadTimeout(), TimeUnit.MILLISECONDS)
                .build();
        log.debug("YousignClient initialized with baseUrl={}", configuration.getBaseUrl());
    }

    /**
     * Create a signature request from a template.
     * POST /signature_requests
     */
    public CreateFromTemplateResult createFromTemplate(YousignConfiguration config) throws YousignException {
        return retryPolicy.execute(() -> {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("template_id", config.getTemplateId());
            body.put("name", config.getRequestName());
            if (config.getExternalId() != null && !config.getExternalId().isBlank()) {
                body.put("external_id", config.getExternalId());
            }
            if (config.getDeliveryMode() != null && !config.getDeliveryMode().isBlank()) {
                body.put("delivery_mode", config.getDeliveryMode());
            }
            if (config.getOrderedSigners() != null) {
                body.put("ordered_signers", config.getOrderedSigners());
            }
            if (config.getExpirationDate() != null && !config.getExpirationDate().isBlank()) {
                body.put("expiration_date", config.getExpirationDate());
            }

            // Signers
            ArrayNode signers = objectMapper.createArrayNode();
            ObjectNode signer = objectMapper.createObjectNode();
            ObjectNode info = objectMapper.createObjectNode();
            info.put("first_name", config.getSignerFirstName());
            info.put("last_name", config.getSignerLastName());
            info.put("email", config.getSignerEmail());
            if (config.getSignerPhoneNumber() != null && !config.getSignerPhoneNumber().isBlank()) {
                info.put("phone_number", config.getSignerPhoneNumber());
            }
            if (config.getSignerLocale() != null && !config.getSignerLocale().isBlank()) {
                info.put("locale", config.getSignerLocale());
            }
            signer.set("info", info);
            signer.put("signature_level", "electronic_signature");
            if (config.getSignerLabel() != null && !config.getSignerLabel().isBlank()) {
                signer.put("label", config.getSignerLabel());
            }
            signers.add(signer);

            // Additional signers
            if (config.getAdditionalSignersJson() != null && !config.getAdditionalSignersJson().isBlank()) {
                try {
                    JsonNode additionalSigners = objectMapper.readTree(config.getAdditionalSignersJson());
                    if (additionalSigners.isArray()) {
                        for (JsonNode additionalSigner : additionalSigners) {
                            signers.add(additionalSigner);
                        }
                    }
                } catch (JsonProcessingException e) {
                    throw new YousignException("Invalid additionalSignersJson format: " + e.getMessage());
                }
            }
            body.set("signers", signers);

            // Template text fields
            if (config.getTemplateTextFieldsJson() != null && !config.getTemplateTextFieldsJson().isBlank()) {
                try {
                    JsonNode textFields = objectMapper.readTree(config.getTemplateTextFieldsJson());
                    body.set("template_placeholders", textFields);
                } catch (JsonProcessingException e) {
                    throw new YousignException("Invalid templateTextFieldsJson format: " + e.getMessage());
                }
            }

            String jsonBody = objectMapper.writeValueAsString(body);
            Request request = buildPostRequest("/signature_requests", jsonBody);
            return executeRequest(request, response -> {
                JsonNode node = objectMapper.readTree(response.body().string());
                return new CreateFromTemplateResult(
                        node.path("id").asText(),
                        node.path("status").asText());
            });
        });
    }

    /**
     * Activate a signature request.
     * POST /signature_requests/{id}/activate
     */
    public ActivateResult activate(String signatureRequestId) throws YousignException {
        return retryPolicy.execute(() -> {
            Request request = buildPostRequest(
                    "/signature_requests/" + signatureRequestId + "/activate", "{}");
            return executeRequest(request, response -> {
                JsonNode node = objectMapper.readTree(response.body().string());
                String status = node.path("status").asText();
                // Extract signer links
                JsonNode signersNode = node.path("signers");
                String signerLinksJson = signersNode.isMissingNode() ? "[]" : objectMapper.writeValueAsString(signersNode);
                return new ActivateResult(status, signerLinksJson);
            });
        });
    }

    /**
     * Get the status of a signature request.
     * GET /signature_requests/{id}
     */
    public GetStatusResult getStatus(String signatureRequestId) throws YousignException {
        return retryPolicy.execute(() -> {
            Request request = buildGetRequest("/signature_requests/" + signatureRequestId);
            return executeRequest(request, response -> {
                JsonNode node = objectMapper.readTree(response.body().string());
                String status = node.path("status").asText();
                JsonNode signersNode = node.path("signers");
                String signerStatusesJson = signersNode.isMissingNode() ? "[]" : objectMapper.writeValueAsString(signersNode);
                boolean isTerminal = "done".equals(status) || "expired".equals(status)
                        || "canceled".equals(status) || "declined".equals(status) || "deleted".equals(status);
                JsonNode documentsNode = node.path("documents");
                String signedDocumentIdsJson = documentsNode.isMissingNode() ? "[]" : objectMapper.writeValueAsString(documentsNode);
                return new GetStatusResult(status, signerStatusesJson, isTerminal, signedDocumentIdsJson);
            });
        });
    }

    /**
     * Cancel a signature request.
     * POST /signature_requests/{id}/cancel
     */
    public void cancel(String signatureRequestId, String reason) throws YousignException {
        retryPolicy.execute(() -> {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("reason", reason != null ? reason : "Cancelled by Bonita process");
            String jsonBody = objectMapper.writeValueAsString(body);
            Request request = buildPostRequest(
                    "/signature_requests/" + signatureRequestId + "/cancel", jsonBody);
            return executeRequest(request, response -> null);
        });
    }

    /**
     * Download a document or all documents (ZIP).
     * GET /signature_requests/{id}/documents/{docId}/download (single)
     * GET /signature_requests/{id}/documents/download (all)
     */
    public DownloadDocumentResult downloadDocument(String signatureRequestId, String documentId)
            throws YousignException {
        return retryPolicy.execute(() -> {
            String path;
            if (documentId != null && !documentId.isBlank()) {
                path = "/signature_requests/" + signatureRequestId + "/documents/" + documentId + "/download";
            } else {
                path = "/signature_requests/" + signatureRequestId + "/documents/download";
            }
            Request request = buildGetRequest(path);
            return executeRequestRaw(request, response -> {
                byte[] bytes = response.body().bytes();
                String contentType = response.header("Content-Type", "application/octet-stream");
                String disposition = response.header("Content-Disposition", "");
                String fileName = extractFileName(disposition,
                        documentId != null ? "document-" + documentId : "documents.zip");
                String base64Content = Base64.getEncoder().encodeToString(bytes);
                return new DownloadDocumentResult(base64Content, fileName, contentType, (long) bytes.length);
            });
        });
    }

    /**
     * Download audit trail.
     * GET /signature_requests/{id}/audit_trails/download
     */
    public DownloadAuditTrailResult downloadAuditTrail(String signatureRequestId, String signerIdsJson)
            throws YousignException {
        return retryPolicy.execute(() -> {
            String path = "/signature_requests/" + signatureRequestId + "/audit_trails/download";
            Request request = buildGetRequest(path);
            return executeRequestRaw(request, response -> {
                byte[] bytes = response.body().bytes();
                String disposition = response.header("Content-Disposition", "");
                String fileName = extractFileName(disposition, "audit-trail.pdf");
                String base64Content = Base64.getEncoder().encodeToString(bytes);
                return new DownloadAuditTrailResult(base64Content, fileName, (long) bytes.length);
            });
        });
    }

    /**
     * List signature requests.
     * GET /signature_requests
     */
    public ListResult list(YousignConfiguration config) throws YousignException {
        return retryPolicy.execute(() -> {
            StringBuilder path = new StringBuilder("/signature_requests?");
            path.append("limit=").append(config.getPageSize());
            if (config.getStatusFilter() != null && !config.getStatusFilter().isBlank()) {
                path.append("&status=").append(config.getStatusFilter());
            }
            if (config.getExternalIdFilter() != null && !config.getExternalIdFilter().isBlank()) {
                path.append("&external_id=").append(config.getExternalIdFilter());
            }
            if (config.getAfterCursor() != null && !config.getAfterCursor().isBlank()) {
                path.append("&after=").append(config.getAfterCursor());
            }
            if (config.getSortOrder() != null && !config.getSortOrder().isBlank()) {
                path.append("&order[created_at]=").append(config.getSortOrder());
            }
            Request request = buildGetRequest(path.toString());
            return executeRequest(request, response -> {
                JsonNode node = objectMapper.readTree(response.body().string());
                JsonNode dataNode = node.path("data");
                String signatureRequestsJson = dataNode.isMissingNode() ? "[]" : objectMapper.writeValueAsString(dataNode);
                JsonNode metaNode = node.path("meta");
                String nextCursor = metaNode.path("next_cursor").asText(null);
                boolean hasMore = nextCursor != null && !nextCursor.isEmpty();
                return new ListResult(signatureRequestsJson, nextCursor, hasMore);
            });
        });
    }

    /**
     * Register a webhook.
     * POST /webhooks
     */
    public RegisterWebhookResult registerWebhook(YousignConfiguration config) throws YousignException {
        return retryPolicy.execute(() -> {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("endpoint", config.getWebhookEndpointUrl());
            if (config.getSandbox() != null) {
                body.put("sandbox", config.getSandbox());
            }
            if (config.getWebhookSecret() != null && !config.getWebhookSecret().isBlank()) {
                body.put("secret", config.getWebhookSecret());
            }
            if (config.getAutoRetry() != null) {
                body.put("auto_retry", config.getAutoRetry());
            }
            if (config.getEnabled() != null) {
                body.put("enabled", config.getEnabled());
            }
            // Subscribed events
            if (config.getSubscribedEventsJson() != null && !config.getSubscribedEventsJson().isBlank()) {
                try {
                    JsonNode events = objectMapper.readTree(config.getSubscribedEventsJson());
                    body.set("subscribed_events", events);
                } catch (JsonProcessingException e) {
                    throw new YousignException("Invalid subscribedEventsJson format: " + e.getMessage());
                }
            }
            String jsonBody = objectMapper.writeValueAsString(body);
            Request request = buildPostRequest("/webhooks", jsonBody);
            return executeRequest(request, response -> {
                JsonNode node = objectMapper.readTree(response.body().string());
                return new RegisterWebhookResult(node.path("id").asText());
            });
        });
    }

    // ============ Private helpers ============

    private Request buildGetRequest(String path) {
        return new Request.Builder()
                .url(configuration.getBaseUrl() + path)
                .header("Authorization", "Bearer " + configuration.getApiKey())
                .header("Accept", "application/json")
                .get()
                .build();
    }

    private Request buildPostRequest(String path, String jsonBody) {
        return new Request.Builder()
                .url(configuration.getBaseUrl() + path)
                .header("Authorization", "Bearer " + configuration.getApiKey())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .post(RequestBody.create(jsonBody, JSON))
                .build();
    }

    private <T> T executeRequest(Request request, ResponseParser<T> parser) throws YousignException {
        try (Response response = httpClient.newCall(request).execute()) {
            handleErrorResponse(response);
            return parser.parse(response);
        } catch (YousignException e) {
            throw e;
        } catch (IOException e) {
            throw new YousignException("Network error: " + e.getMessage(), e);
        }
    }

    private <T> T executeRequestRaw(Request request, ResponseParser<T> parser) throws YousignException {
        try (Response response = httpClient.newCall(request).execute()) {
            handleErrorResponse(response);
            return parser.parse(response);
        } catch (YousignException e) {
            throw e;
        } catch (IOException e) {
            throw new YousignException("Network error: " + e.getMessage(), e);
        }
    }

    private void handleErrorResponse(Response response) throws YousignException, IOException {
        if (response.isSuccessful()) {
            return;
        }
        int code = response.code();
        String responseBody = response.body() != null ? response.body().string() : "";
        boolean retryable = RetryPolicy.isRetryableStatusCode(code);

        String message;
        switch (code) {
            case 400 -> message = "Bad request: " + responseBody;
            case 401 -> message = "Authentication error: invalid API key";
            case 403 -> message = "Access denied: " + responseBody;
            case 404 -> message = "Resource not found: " + responseBody;
            case 429 -> message = "Rate limit exceeded";
            default -> message = "HTTP " + code + ": " + responseBody;
        }
        throw new YousignException(message, code, retryable);
    }

    private String extractFileName(String contentDisposition, String defaultName) {
        if (contentDisposition != null && contentDisposition.contains("filename=")) {
            String[] parts = contentDisposition.split("filename=");
            if (parts.length > 1) {
                return parts[1].replace("\"", "").trim();
            }
        }
        return defaultName;
    }

    @FunctionalInterface
    interface ResponseParser<T> {
        T parse(Response response) throws IOException, YousignException;
    }

    // ============ Result records ============

    public record CreateFromTemplateResult(String signatureRequestId, String status) {}
    public record ActivateResult(String status, String signerLinksJson) {}
    public record GetStatusResult(String status, String signerStatusesJson, boolean isTerminal, String signedDocumentIdsJson) {}
    public record DownloadDocumentResult(String documentContent, String documentName, String contentType, Long fileSizeBytes) {}
    public record DownloadAuditTrailResult(String auditTrailContent, String auditTrailName, Long fileSizeBytes) {}
    public record ListResult(String signatureRequestsJson, String nextCursor, boolean hasMore) {}
    public record RegisterWebhookResult(String webhookId) {}
}
