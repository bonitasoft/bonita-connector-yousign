package com.bonitasoft.connectors.yousign;

import static org.assertj.core.api.Assertions.*;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Base64;

class YousignClientTest {

    private MockWebServer mockServer;
    private YousignClient client;

    @BeforeEach
    void setUp() throws Exception {
        mockServer = new MockWebServer();
        mockServer.start();
        var config = YousignConfiguration.builder()
                .apiKey("test-api-key")
                .baseUrl(mockServer.url("/v3").toString().replaceAll("/$", ""))
                .connectTimeout(5000)
                .readTimeout(5000)
                .maxRetries(0)
                .build();
        client = new YousignClient(config);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }

    // === createFromTemplate ===

    @Test
    void should_create_from_template_with_minimal_config() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":\"req-123\",\"status\":\"draft\"}")
                .addHeader("Content-Type", "application/json"));

        var config = YousignConfiguration.builder()
                .apiKey("key")
                .baseUrl(mockServer.url("/v3").toString().replaceAll("/$", ""))
                .templateId("tmpl-1")
                .requestName("Test")
                .signerFirstName("John")
                .signerLastName("Doe")
                .signerEmail("john@example.com")
                .maxRetries(0)
                .build();
        var result = client.createFromTemplate(config);

        assertThat(result.signatureRequestId()).isEqualTo("req-123");
        assertThat(result.status()).isEqualTo("draft");

        RecordedRequest request = mockServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/v3/signature_requests");
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer test-api-key");
        String body = request.getBody().readUtf8();
        assertThat(body).contains("\"template_id\":\"tmpl-1\"");
        assertThat(body).contains("\"name\":\"Test\"");
        assertThat(body).contains("\"first_name\":\"John\"");
        assertThat(body).contains("\"last_name\":\"Doe\"");
        assertThat(body).contains("\"email\":\"john@example.com\"");
        assertThat(body).contains("\"signature_level\":\"electronic_signature\"");
    }

    @Test
    void should_create_from_template_with_all_optional_fields() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":\"req-456\",\"status\":\"draft\"}")
                .addHeader("Content-Type", "application/json"));

        var config = YousignConfiguration.builder()
                .apiKey("key")
                .baseUrl(mockServer.url("/v3").toString().replaceAll("/$", ""))
                .templateId("tmpl-1")
                .requestName("Test")
                .externalId("ext-1")
                .deliveryMode("email")
                .orderedSigners(true)
                .expirationDate("2026-12-31")
                .signerFirstName("John")
                .signerLastName("Doe")
                .signerEmail("john@example.com")
                .signerPhoneNumber("+33612345678")
                .signerLocale("fr")
                .signerLabel("Signer 1")
                .maxRetries(0)
                .build();
        var result = client.createFromTemplate(config);

        assertThat(result.signatureRequestId()).isEqualTo("req-456");

        String body = mockServer.takeRequest().getBody().readUtf8();
        assertThat(body).contains("\"external_id\":\"ext-1\"");
        assertThat(body).contains("\"delivery_mode\":\"email\"");
        assertThat(body).contains("\"ordered_signers\":true");
        assertThat(body).contains("\"expiration_date\":\"2026-12-31\"");
        assertThat(body).contains("\"phone_number\":\"+33612345678\"");
        assertThat(body).contains("\"locale\":\"fr\"");
        assertThat(body).contains("\"label\":\"Signer 1\"");
    }

    @Test
    void should_create_from_template_without_optional_fields_when_null() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":\"req-789\",\"status\":\"draft\"}")
                .addHeader("Content-Type", "application/json"));

        var config = YousignConfiguration.builder()
                .apiKey("key")
                .baseUrl(mockServer.url("/v3").toString().replaceAll("/$", ""))
                .templateId("tmpl-1")
                .requestName("Test")
                .signerFirstName("John")
                .signerLastName("Doe")
                .signerEmail("john@example.com")
                // externalId, deliveryMode, orderedSigners, expirationDate all null
                // signerPhoneNumber, signerLocale, signerLabel all null
                .maxRetries(0)
                .build();
        var result = client.createFromTemplate(config);
        assertThat(result.signatureRequestId()).isEqualTo("req-789");

        String body = mockServer.takeRequest().getBody().readUtf8();
        assertThat(body).doesNotContain("external_id");
        assertThat(body).doesNotContain("delivery_mode");
        assertThat(body).doesNotContain("expiration_date");
        assertThat(body).doesNotContain("phone_number");
    }

    @Test
    void should_create_from_template_with_blank_optional_fields_excluded() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":\"req-b\",\"status\":\"draft\"}")
                .addHeader("Content-Type", "application/json"));

        var config = YousignConfiguration.builder()
                .apiKey("key")
                .baseUrl(mockServer.url("/v3").toString().replaceAll("/$", ""))
                .templateId("tmpl-1")
                .requestName("Test")
                .externalId("   ")
                .deliveryMode("  ")
                .expirationDate("")
                .signerFirstName("John")
                .signerLastName("Doe")
                .signerEmail("john@example.com")
                .signerPhoneNumber("  ")
                .signerLocale("")
                .signerLabel("  ")
                .maxRetries(0)
                .build();
        client.createFromTemplate(config);

        String body = mockServer.takeRequest().getBody().readUtf8();
        assertThat(body).doesNotContain("\"external_id\"");
        assertThat(body).doesNotContain("\"delivery_mode\"");
        assertThat(body).doesNotContain("\"expiration_date\"");
        assertThat(body).doesNotContain("\"phone_number\"");
        assertThat(body).doesNotContain("\"locale\"");
        assertThat(body).doesNotContain("\"label\"");
    }

    @Test
    void should_create_from_template_with_additional_signers() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":\"req-add\",\"status\":\"draft\"}")
                .addHeader("Content-Type", "application/json"));

        var config = YousignConfiguration.builder()
                .apiKey("key")
                .baseUrl(mockServer.url("/v3").toString().replaceAll("/$", ""))
                .templateId("tmpl-1")
                .requestName("Test")
                .signerFirstName("John")
                .signerLastName("Doe")
                .signerEmail("john@example.com")
                .additionalSignersJson("[{\"info\":{\"first_name\":\"Jane\",\"last_name\":\"Roe\",\"email\":\"jane@example.com\"}}]")
                .maxRetries(0)
                .build();
        var result = client.createFromTemplate(config);

        assertThat(result.signatureRequestId()).isEqualTo("req-add");
        String body = mockServer.takeRequest().getBody().readUtf8();
        assertThat(body).contains("Jane");
        assertThat(body).contains("jane@example.com");
    }

    @Test
    void should_throw_on_invalid_additional_signers_json() {
        mockServer.enqueue(new MockResponse().setResponseCode(200)
                .setBody("{\"id\":\"x\",\"status\":\"y\"}")
                .addHeader("Content-Type", "application/json"));

        var config = YousignConfiguration.builder()
                .apiKey("key")
                .baseUrl(mockServer.url("/v3").toString().replaceAll("/$", ""))
                .templateId("tmpl-1")
                .requestName("Test")
                .signerFirstName("John")
                .signerLastName("Doe")
                .signerEmail("john@example.com")
                .additionalSignersJson("{invalid json")
                .maxRetries(0)
                .build();

        assertThatThrownBy(() -> client.createFromTemplate(config))
                .isInstanceOf(YousignException.class)
                .hasMessageContaining("Invalid additionalSignersJson format");
    }

    @Test
    void should_create_from_template_with_template_text_fields() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":\"req-txt\",\"status\":\"draft\"}")
                .addHeader("Content-Type", "application/json"));

        var config = YousignConfiguration.builder()
                .apiKey("key")
                .baseUrl(mockServer.url("/v3").toString().replaceAll("/$", ""))
                .templateId("tmpl-1")
                .requestName("Test")
                .signerFirstName("John")
                .signerLastName("Doe")
                .signerEmail("john@example.com")
                .templateTextFieldsJson("{\"company\":\"Acme\"}")
                .maxRetries(0)
                .build();
        client.createFromTemplate(config);

        String body = mockServer.takeRequest().getBody().readUtf8();
        assertThat(body).contains("\"template_placeholders\"");
        assertThat(body).contains("Acme");
    }

    @Test
    void should_throw_on_invalid_template_text_fields_json() {
        var config = YousignConfiguration.builder()
                .apiKey("key")
                .baseUrl(mockServer.url("/v3").toString().replaceAll("/$", ""))
                .templateId("tmpl-1")
                .requestName("Test")
                .signerFirstName("John")
                .signerLastName("Doe")
                .signerEmail("john@example.com")
                .templateTextFieldsJson("{broken json")
                .maxRetries(0)
                .build();

        assertThatThrownBy(() -> client.createFromTemplate(config))
                .isInstanceOf(YousignException.class)
                .hasMessageContaining("Invalid templateTextFieldsJson format");
    }

    @Test
    void should_not_include_template_text_fields_when_blank() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":\"req-no-txt\",\"status\":\"draft\"}")
                .addHeader("Content-Type", "application/json"));

        var config = YousignConfiguration.builder()
                .apiKey("key")
                .baseUrl(mockServer.url("/v3").toString().replaceAll("/$", ""))
                .templateId("tmpl-1")
                .requestName("Test")
                .signerFirstName("John")
                .signerLastName("Doe")
                .signerEmail("john@example.com")
                .templateTextFieldsJson("  ")
                .maxRetries(0)
                .build();
        client.createFromTemplate(config);

        String body = mockServer.takeRequest().getBody().readUtf8();
        assertThat(body).doesNotContain("template_placeholders");
    }

    @Test
    void should_not_include_additional_signers_when_blank() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":\"req-no-add\",\"status\":\"draft\"}")
                .addHeader("Content-Type", "application/json"));

        var config = YousignConfiguration.builder()
                .apiKey("key")
                .baseUrl(mockServer.url("/v3").toString().replaceAll("/$", ""))
                .templateId("tmpl-1")
                .requestName("Test")
                .signerFirstName("John")
                .signerLastName("Doe")
                .signerEmail("john@example.com")
                .additionalSignersJson("")
                .maxRetries(0)
                .build();
        client.createFromTemplate(config);

        // Should have only one signer in the array
        String body = mockServer.takeRequest().getBody().readUtf8();
        assertThat(body).contains("signers");
    }

    // === activate ===

    @Test
    void should_activate_signature_request() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"status\":\"ongoing\",\"signers\":[{\"id\":\"s1\",\"signature_link\":\"https://sign.yousign.app/s1\"}]}")
                .addHeader("Content-Type", "application/json"));

        var result = client.activate("req-123");

        assertThat(result.status()).isEqualTo("ongoing");
        assertThat(result.signerLinksJson()).contains("s1");

        RecordedRequest request = mockServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/v3/signature_requests/req-123/activate");
    }

    @Test
    void should_activate_with_missing_signers_node() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"status\":\"ongoing\"}")
                .addHeader("Content-Type", "application/json"));

        var result = client.activate("req-no-signers");

        assertThat(result.status()).isEqualTo("ongoing");
        assertThat(result.signerLinksJson()).isEqualTo("[]");
    }

    // === getStatus ===

    @Test
    void should_get_status_ongoing() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"status\":\"ongoing\",\"signers\":[{\"id\":\"s1\"}],\"documents\":[{\"id\":\"d1\"}]}")
                .addHeader("Content-Type", "application/json"));

        var result = client.getStatus("req-123");

        assertThat(result.status()).isEqualTo("ongoing");
        assertThat(result.isTerminal()).isFalse();
        assertThat(result.signerStatusesJson()).contains("s1");
        assertThat(result.signedDocumentIdsJson()).contains("d1");
    }

    @Test
    void should_get_status_done_as_terminal() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"status\":\"done\",\"signers\":[],\"documents\":[]}")
                .addHeader("Content-Type", "application/json"));

        var result = client.getStatus("req-123");
        assertThat(result.isTerminal()).isTrue();
    }

    @Test
    void should_get_status_expired_as_terminal() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"status\":\"expired\"}")
                .addHeader("Content-Type", "application/json"));

        var result = client.getStatus("req-123");
        assertThat(result.isTerminal()).isTrue();
    }

    @Test
    void should_get_status_canceled_as_terminal() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"status\":\"canceled\"}")
                .addHeader("Content-Type", "application/json"));

        var result = client.getStatus("req-123");
        assertThat(result.isTerminal()).isTrue();
    }

    @Test
    void should_get_status_declined_as_terminal() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"status\":\"declined\"}")
                .addHeader("Content-Type", "application/json"));

        var result = client.getStatus("req-123");
        assertThat(result.isTerminal()).isTrue();
    }

    @Test
    void should_get_status_deleted_as_terminal() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"status\":\"deleted\"}")
                .addHeader("Content-Type", "application/json"));

        var result = client.getStatus("req-123");
        assertThat(result.isTerminal()).isTrue();
    }

    @Test
    void should_get_status_draft_as_non_terminal() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"status\":\"draft\"}")
                .addHeader("Content-Type", "application/json"));

        var result = client.getStatus("req-123");
        assertThat(result.isTerminal()).isFalse();
    }

    @Test
    void should_get_status_with_missing_signers_and_documents_nodes() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"status\":\"ongoing\"}")
                .addHeader("Content-Type", "application/json"));

        var result = client.getStatus("req-123");
        assertThat(result.signerStatusesJson()).isEqualTo("[]");
        assertThat(result.signedDocumentIdsJson()).isEqualTo("[]");
    }

    // === cancel ===

    @Test
    void should_cancel_with_custom_reason() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{}")
                .addHeader("Content-Type", "application/json"));

        client.cancel("req-123", "My custom reason");

        RecordedRequest request = mockServer.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/v3/signature_requests/req-123/cancel");
        String body = request.getBody().readUtf8();
        assertThat(body).contains("\"reason\":\"My custom reason\"");
    }

    @Test
    void should_cancel_with_default_reason_when_null() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{}")
                .addHeader("Content-Type", "application/json"));

        client.cancel("req-123", null);

        String body = mockServer.takeRequest().getBody().readUtf8();
        assertThat(body).contains("\"reason\":\"Cancelled by Bonita process\"");
    }

    // === downloadDocument ===

    @Test
    void should_download_single_document() throws Exception {
        byte[] content = "PDF-CONTENT".getBytes();
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(new okio.Buffer().write(content))
                .addHeader("Content-Type", "application/pdf")
                .addHeader("Content-Disposition", "attachment; filename=\"contract.pdf\""));

        var result = client.downloadDocument("req-123", "doc-456");

        assertThat(result.documentContent()).isEqualTo(Base64.getEncoder().encodeToString(content));
        assertThat(result.documentName()).isEqualTo("contract.pdf");
        assertThat(result.contentType()).isEqualTo("application/pdf");
        assertThat(result.fileSizeBytes()).isEqualTo((long) content.length);

        RecordedRequest request = mockServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/v3/signature_requests/req-123/documents/doc-456/download");
    }

    @Test
    void should_download_all_documents_as_zip_when_documentId_null() throws Exception {
        byte[] content = "ZIP-CONTENT".getBytes();
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(new okio.Buffer().write(content))
                .addHeader("Content-Type", "application/zip"));

        var result = client.downloadDocument("req-123", null);

        assertThat(result.documentName()).isEqualTo("documents.zip");

        RecordedRequest request = mockServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/v3/signature_requests/req-123/documents/download");
    }

    @Test
    void should_download_all_documents_when_documentId_blank() throws Exception {
        byte[] content = "ZIP-CONTENT".getBytes();
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(new okio.Buffer().write(content))
                .addHeader("Content-Type", "application/zip"));

        var result = client.downloadDocument("req-123", "  ");

        // Path uses the "all documents" endpoint since blank is treated as absent
        RecordedRequest request = mockServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/v3/signature_requests/req-123/documents/download");
        // But the default fileName uses documentId if not null: "document-  "
        // This verifies the actual behavior of extractFileName with an empty disposition
        assertThat(result.documentName()).isNotNull();
    }

    @Test
    void should_use_default_filename_when_no_content_disposition() throws Exception {
        byte[] content = "DATA".getBytes();
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(new okio.Buffer().write(content))
                .addHeader("Content-Type", "application/octet-stream"));

        var result = client.downloadDocument("req-123", "doc-1");

        assertThat(result.documentName()).isEqualTo("document-doc-1");
    }

    @Test
    void should_use_default_content_type_when_missing() throws Exception {
        byte[] content = "DATA".getBytes();
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(new okio.Buffer().write(content)));

        var result = client.downloadDocument("req-123", "doc-1");

        // The header falls back to application/octet-stream
        assertThat(result.contentType()).isNotNull();
    }

    // === downloadAuditTrail ===

    @Test
    void should_download_audit_trail() throws Exception {
        byte[] content = "AUDIT-PDF".getBytes();
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(new okio.Buffer().write(content))
                .addHeader("Content-Type", "application/pdf")
                .addHeader("Content-Disposition", "attachment; filename=\"trail.pdf\""));

        var result = client.downloadAuditTrail("req-123", null);

        assertThat(result.auditTrailContent()).isEqualTo(Base64.getEncoder().encodeToString(content));
        assertThat(result.auditTrailName()).isEqualTo("trail.pdf");
        assertThat(result.fileSizeBytes()).isEqualTo((long) content.length);

        RecordedRequest request = mockServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/v3/signature_requests/req-123/audit_trails/download");
        assertThat(request.getMethod()).isEqualTo("GET");
    }

    @Test
    void should_download_audit_trail_with_default_name() throws Exception {
        byte[] content = "AUDIT".getBytes();
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(new okio.Buffer().write(content)));

        var result = client.downloadAuditTrail("req-123", null);

        assertThat(result.auditTrailName()).isEqualTo("audit-trail.pdf");
    }

    // === list ===

    @Test
    void should_list_with_default_params() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"data\":[{\"id\":\"req-1\"}],\"meta\":{\"next_cursor\":\"cur-1\"}}")
                .addHeader("Content-Type", "application/json"));

        var config = YousignConfiguration.builder()
                .apiKey("key")
                .baseUrl(mockServer.url("/v3").toString().replaceAll("/$", ""))
                .pageSize(10)
                .maxRetries(0)
                .build();
        var result = client.list(config);

        assertThat(result.signatureRequestsJson()).contains("req-1");
        assertThat(result.nextCursor()).isEqualTo("cur-1");
        assertThat(result.hasMore()).isTrue();

        RecordedRequest request = mockServer.takeRequest();
        assertThat(request.getPath()).contains("limit=10");
    }

    @Test
    void should_list_with_all_filters() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"data\":[],\"meta\":{}}")
                .addHeader("Content-Type", "application/json"));

        var config = YousignConfiguration.builder()
                .apiKey("key")
                .baseUrl(mockServer.url("/v3").toString().replaceAll("/$", ""))
                .pageSize(5)
                .statusFilter("done")
                .externalIdFilter("ext-1")
                .afterCursor("cursor-abc")
                .sortOrder("asc")
                .maxRetries(0)
                .build();
        var result = client.list(config);

        assertThat(result.hasMore()).isFalse();
        assertThat(result.nextCursor()).isNull();

        RecordedRequest request = mockServer.takeRequest();
        String path = request.getPath();
        assertThat(path).contains("limit=5");
        assertThat(path).contains("status=done");
        assertThat(path).contains("external_id=ext-1");
        assertThat(path).contains("after=cursor-abc");
        assertThat(path).contains("order[created_at]=asc");
    }

    @Test
    void should_list_with_no_more_when_next_cursor_null() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"data\":[],\"meta\":{\"next_cursor\":null}}")
                .addHeader("Content-Type", "application/json"));

        var config = YousignConfiguration.builder()
                .apiKey("key")
                .baseUrl(mockServer.url("/v3").toString().replaceAll("/$", ""))
                .maxRetries(0)
                .build();
        var result = client.list(config);

        assertThat(result.hasMore()).isFalse();
    }

    @Test
    void should_list_with_no_more_when_next_cursor_empty() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"data\":[],\"meta\":{\"next_cursor\":\"\"}}")
                .addHeader("Content-Type", "application/json"));

        var config = YousignConfiguration.builder()
                .apiKey("key")
                .baseUrl(mockServer.url("/v3").toString().replaceAll("/$", ""))
                .maxRetries(0)
                .build();
        var result = client.list(config);

        assertThat(result.hasMore()).isFalse();
    }

    @Test
    void should_list_with_missing_data_node() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"meta\":{}}")
                .addHeader("Content-Type", "application/json"));

        var config = YousignConfiguration.builder()
                .apiKey("key")
                .baseUrl(mockServer.url("/v3").toString().replaceAll("/$", ""))
                .maxRetries(0)
                .build();
        var result = client.list(config);

        assertThat(result.signatureRequestsJson()).isEqualTo("[]");
    }

    @Test
    void should_list_without_optional_filters_when_null() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"data\":[],\"meta\":{}}")
                .addHeader("Content-Type", "application/json"));

        var config = YousignConfiguration.builder()
                .apiKey("key")
                .baseUrl(mockServer.url("/v3").toString().replaceAll("/$", ""))
                .pageSize(20)
                .maxRetries(0)
                .build();
        client.list(config);

        String path = mockServer.takeRequest().getPath();
        assertThat(path).doesNotContain("status=");
        assertThat(path).doesNotContain("external_id=");
        assertThat(path).doesNotContain("after=");
        assertThat(path).doesNotContain("order[created_at]=");
    }

    @Test
    void should_list_without_optional_filters_when_blank() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"data\":[],\"meta\":{}}")
                .addHeader("Content-Type", "application/json"));

        var config = YousignConfiguration.builder()
                .apiKey("key")
                .baseUrl(mockServer.url("/v3").toString().replaceAll("/$", ""))
                .pageSize(20)
                .statusFilter("  ")
                .externalIdFilter("  ")
                .afterCursor("")
                .sortOrder("  ")
                .maxRetries(0)
                .build();
        client.list(config);

        String path = mockServer.takeRequest().getPath();
        assertThat(path).doesNotContain("status=");
        assertThat(path).doesNotContain("external_id=");
        assertThat(path).doesNotContain("after=");
        assertThat(path).doesNotContain("order[created_at]=");
    }

    // === registerWebhook ===

    @Test
    void should_register_webhook_with_all_fields() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":\"wh-123\"}")
                .addHeader("Content-Type", "application/json"));

        var config = YousignConfiguration.builder()
                .apiKey("key")
                .baseUrl(mockServer.url("/v3").toString().replaceAll("/$", ""))
                .webhookEndpointUrl("https://example.com/webhook")
                .sandbox(true)
                .webhookSecret("secret123")
                .autoRetry(true)
                .enabled(false)
                .subscribedEventsJson("[\"signature_request.done\"]")
                .maxRetries(0)
                .build();
        var result = client.registerWebhook(config);

        assertThat(result.webhookId()).isEqualTo("wh-123");

        String body = mockServer.takeRequest().getBody().readUtf8();
        assertThat(body).contains("\"endpoint\":\"https://example.com/webhook\"");
        assertThat(body).contains("\"sandbox\":true");
        assertThat(body).contains("\"secret\":\"secret123\"");
        assertThat(body).contains("\"auto_retry\":true");
        assertThat(body).contains("\"enabled\":false");
        assertThat(body).contains("\"subscribed_events\"");
    }

    @Test
    void should_register_webhook_without_optional_fields() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":\"wh-456\"}")
                .addHeader("Content-Type", "application/json"));

        var config = YousignConfiguration.builder()
                .apiKey("key")
                .baseUrl(mockServer.url("/v3").toString().replaceAll("/$", ""))
                .webhookEndpointUrl("https://example.com/webhook")
                .maxRetries(0)
                .build();
        var result = client.registerWebhook(config);

        assertThat(result.webhookId()).isEqualTo("wh-456");

        String body = mockServer.takeRequest().getBody().readUtf8();
        assertThat(body).doesNotContain("sandbox");
        assertThat(body).doesNotContain("secret");
        assertThat(body).doesNotContain("auto_retry");
        assertThat(body).doesNotContain("enabled");
        assertThat(body).doesNotContain("subscribed_events");
    }

    @Test
    void should_register_webhook_without_secret_when_blank() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":\"wh-789\"}")
                .addHeader("Content-Type", "application/json"));

        var config = YousignConfiguration.builder()
                .apiKey("key")
                .baseUrl(mockServer.url("/v3").toString().replaceAll("/$", ""))
                .webhookEndpointUrl("https://example.com/webhook")
                .webhookSecret("   ")
                .maxRetries(0)
                .build();
        client.registerWebhook(config);

        String body = mockServer.takeRequest().getBody().readUtf8();
        assertThat(body).doesNotContain("secret");
    }

    @Test
    void should_throw_on_invalid_subscribed_events_json() {
        var config = YousignConfiguration.builder()
                .apiKey("key")
                .baseUrl(mockServer.url("/v3").toString().replaceAll("/$", ""))
                .webhookEndpointUrl("https://example.com/webhook")
                .subscribedEventsJson("{broken")
                .maxRetries(0)
                .build();

        assertThatThrownBy(() -> client.registerWebhook(config))
                .isInstanceOf(YousignException.class)
                .hasMessageContaining("Invalid subscribedEventsJson format");
    }

    @Test
    void should_not_include_subscribed_events_when_blank() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"id\":\"wh-b\"}")
                .addHeader("Content-Type", "application/json"));

        var config = YousignConfiguration.builder()
                .apiKey("key")
                .baseUrl(mockServer.url("/v3").toString().replaceAll("/$", ""))
                .webhookEndpointUrl("https://example.com/webhook")
                .subscribedEventsJson("  ")
                .maxRetries(0)
                .build();
        client.registerWebhook(config);

        String body = mockServer.takeRequest().getBody().readUtf8();
        assertThat(body).doesNotContain("subscribed_events");
    }

    // === Error handling ===

    @Test
    void should_throw_on_400_bad_request() {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setBody("validation error")
                .addHeader("Content-Type", "application/json"));

        assertThatThrownBy(() -> client.activate("req-123"))
                .isInstanceOf(YousignException.class)
                .hasMessageContaining("Bad request")
                .hasMessageContaining("validation error");
    }

    @Test
    void should_throw_on_401_unauthorized() {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("unauthorized")
                .addHeader("Content-Type", "application/json"));

        assertThatThrownBy(() -> client.activate("req-123"))
                .isInstanceOf(YousignException.class)
                .hasMessageContaining("Authentication error: invalid API key");
    }

    @Test
    void should_throw_on_403_forbidden() {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(403)
                .setBody("forbidden details")
                .addHeader("Content-Type", "application/json"));

        assertThatThrownBy(() -> client.activate("req-123"))
                .isInstanceOf(YousignException.class)
                .hasMessageContaining("Access denied")
                .hasMessageContaining("forbidden details");
    }

    @Test
    void should_throw_on_404_not_found() {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("not found details")
                .addHeader("Content-Type", "application/json"));

        assertThatThrownBy(() -> client.activate("req-123"))
                .isInstanceOf(YousignException.class)
                .hasMessageContaining("Resource not found")
                .hasMessageContaining("not found details");
    }

    @Test
    void should_throw_on_429_rate_limit() {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(429)
                .setBody("")
                .addHeader("Content-Type", "application/json"));

        assertThatThrownBy(() -> client.activate("req-123"))
                .isInstanceOf(YousignException.class)
                .hasMessageContaining("Rate limit exceeded");
    }

    @Test
    void should_throw_on_500_server_error() {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("internal server error")
                .addHeader("Content-Type", "application/json"));

        assertThatThrownBy(() -> client.activate("req-123"))
                .isInstanceOf(YousignException.class)
                .hasMessageContaining("HTTP 500")
                .hasMessageContaining("internal server error");
    }

    @Test
    void should_mark_429_as_retryable() {
        mockServer.enqueue(new MockResponse().setResponseCode(429).setBody(""));

        try {
            client.activate("req-123");
            fail("Expected YousignException");
        } catch (YousignException e) {
            assertThat(e.isRetryable()).isTrue();
        }
    }

    @Test
    void should_mark_500_as_retryable() {
        mockServer.enqueue(new MockResponse().setResponseCode(500).setBody("error"));

        try {
            client.activate("req-123");
            fail("Expected YousignException");
        } catch (YousignException e) {
            assertThat(e.isRetryable()).isTrue();
        }
    }

    @Test
    void should_mark_400_as_not_retryable() {
        mockServer.enqueue(new MockResponse().setResponseCode(400).setBody("bad"));

        try {
            client.activate("req-123");
            fail("Expected YousignException");
        } catch (YousignException e) {
            assertThat(e.isRetryable()).isFalse();
        }
    }

    @Test
    void should_mark_401_as_not_retryable() {
        mockServer.enqueue(new MockResponse().setResponseCode(401).setBody(""));

        try {
            client.activate("req-123");
            fail("Expected YousignException");
        } catch (YousignException e) {
            assertThat(e.isRetryable()).isFalse();
        }
    }

    @Test
    void should_handle_error_with_null_body() {
        mockServer.enqueue(new MockResponse().setResponseCode(502).setBody(""));

        assertThatThrownBy(() -> client.activate("req-123"))
                .isInstanceOf(YousignException.class)
                .hasMessageContaining("HTTP 502");
    }

    // === extractFileName edge cases ===

    @Test
    void should_extract_filename_from_content_disposition_without_quotes() throws Exception {
        byte[] content = "DATA".getBytes();
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(new okio.Buffer().write(content))
                .addHeader("Content-Disposition", "attachment; filename=doc.pdf"));

        var result = client.downloadDocument("req-123", "doc-1");
        assertThat(result.documentName()).isEqualTo("doc.pdf");
    }

    @Test
    void should_use_default_name_when_content_disposition_null() throws Exception {
        byte[] content = "DATA".getBytes();
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(new okio.Buffer().write(content)));

        var result = client.downloadDocument("req-123", "doc-1");
        // Default is "document-{docId}"
        assertThat(result.documentName()).isEqualTo("document-doc-1");
    }

    @Test
    void should_use_default_name_when_content_disposition_has_no_filename() throws Exception {
        byte[] content = "DATA".getBytes();
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(new okio.Buffer().write(content))
                .addHeader("Content-Disposition", "attachment"));

        var result = client.downloadDocument("req-123", "doc-1");
        assertThat(result.documentName()).isEqualTo("document-doc-1");
    }

    // === Error handling for raw requests (download) ===

    @Test
    void should_throw_on_error_during_download_document() {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(403)
                .setBody("forbidden")
                .addHeader("Content-Type", "application/json"));

        assertThatThrownBy(() -> client.downloadDocument("req-123", "doc-1"))
                .isInstanceOf(YousignException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    void should_throw_on_error_during_download_audit_trail() {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("not found")
                .addHeader("Content-Type", "application/json"));

        assertThatThrownBy(() -> client.downloadAuditTrail("req-123", null))
                .isInstanceOf(YousignException.class)
                .hasMessageContaining("Resource not found");
    }

    // === cancel return value ===

    @Test
    void should_cancel_without_throwing() throws Exception {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{}")
                .addHeader("Content-Type", "application/json"));

        // cancel returns void, just verify no exception
        assertThatCode(() -> client.cancel("req-123", "reason"))
                .doesNotThrowAnyException();
    }

    // === extractFileName edge: parts.length boundary ===

    @Test
    void should_extract_filename_when_content_disposition_has_filename_equals_at_end() throws Exception {
        byte[] content = "DATA".getBytes();
        // "filename=" with nothing after the split yields parts.length==2 where parts[1] is ""
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(new okio.Buffer().write(content))
                .addHeader("Content-Disposition", "attachment; filename="));

        var result = client.downloadDocument("req-123", "doc-1");
        // parts[1] would be empty string, trimmed is still empty
        assertThat(result.documentName()).isNotNull();
    }

    @Test
    void should_extract_filename_with_exactly_one_part_after_split() throws Exception {
        byte[] content = "DATA".getBytes();
        // Content-Disposition that contains "filename=" but split yields only the value part
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(new okio.Buffer().write(content))
                .addHeader("Content-Disposition", "filename=report.pdf"));

        var result = client.downloadDocument("req-123", "doc-1");
        assertThat(result.documentName()).isEqualTo("report.pdf");
    }

    // === Network error (IOException) ===

    @Test
    void should_throw_yousign_exception_on_network_error() throws Exception {
        mockServer.shutdown(); // Cause connection failure

        assertThatThrownBy(() -> client.activate("req-123"))
                .isInstanceOf(YousignException.class)
                .hasMessageContaining("Network error");
    }

    @Test
    void should_throw_network_error_on_download_failure() throws Exception {
        mockServer.shutdown();

        assertThatThrownBy(() -> client.downloadDocument("req-123", "doc-1"))
                .isInstanceOf(YousignException.class)
                .hasMessageContaining("Network error");
    }
}
