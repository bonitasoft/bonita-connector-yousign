# Connector Generation Report

## Summary
- **Connector name:** yousign
- **Display name:** Yousign eSignature
- **Generated:** 2026-03-26
- **Version:** 1.0.0-beta.1
- **Operations:** 8
- **Package:** com.bonitasoft.connectors.yousign

## Operations Generated

| Operation | Def ID | .def | .impl | Unit Tests | Property Tests | Integration Test |
|-----------|--------|------|-------|------------|----------------|------------------|
| Create From Template | yousign-create-from-template | Yes | Yes | 12 | 10 | Yes (skippable) |
| Activate | yousign-activate | Yes | Yes | 8 | 10 | Yes (skippable) |
| Get Status | yousign-get-status | Yes | Yes | 8 | 10 | Yes (skippable) |
| Cancel | yousign-cancel | Yes | Yes | 8 | 10 | Yes (skippable) |
| Download Document | yousign-download-document | Yes | Yes | 8 | 10 | Yes (skippable) |
| Download Audit Trail | yousign-download-audit-trail | Yes | Yes | 8 | 10 | Yes (skippable) |
| List | yousign-list | Yes | Yes | 8 | 10 | Yes (skippable) |
| Register Webhook | yousign-register-webhook | Yes | Yes | 8 | 10 | Yes (skippable) |

## Files Created

### Source (src/main/java/com/bonitasoft/connectors/yousign/)
- `AbstractYousignConnector.java` - Base class with lifecycle and error handling
- `YousignConfiguration.java` - Lombok @Data/@Builder config holder
- `YousignException.java` - Typed exception with status code and retryable flag
- `YousignClient.java` - OkHttp + Jackson API facade (8 operation methods + 7 result records)
- `RetryPolicy.java` - Exponential backoff with jitter
- `CreateFromTemplateConnector.java`
- `ActivateConnector.java`
- `GetStatusConnector.java`
- `CancelConnector.java`
- `DownloadDocumentConnector.java`
- `DownloadAuditTrailConnector.java`
- `ListConnector.java`
- `RegisterWebhookConnector.java`

### Resources
- 8 `.def` files (src/main/resources-filtered/)
- 8 `.impl` files (src/main/resources-filtered/)
- 8 `.properties` files (src/main/resources/)
- `yousign-icon.png` (placeholder — 16x16 PNG required)

### Assembly
- `all-assembly.xml` - Bundles all operations
- 8 per-operation assembly files

### Tests (src/test/java/com/bonitasoft/connectors/yousign/)
- 8 unit test classes (~68 tests total)
- 8 property test classes (~80 jqwik properties total)
- 8 integration test classes (skippable)
- `RetryPolicyTest.java`
- `YousignExceptionTest.java`
- `YousignConfigurationTest.java`
- `ConnectorTestToolkit.java`
- `YousignConnectorIT.java`

### Other
- `beta-status.json`
- `BETA_STATUS.md`
- `.github/ISSUE_TEMPLATE/beta-feedback.yml`

## Dependencies
| Dependency | Version | Scope |
|-----------|---------|-------|
| bonita-common | 10.2.0 | provided |
| lombok | 1.18.30 | provided |
| okhttp | 4.12.0 | provided |
| jackson-databind | 2.17.2 | provided |
| jackson-core | 2.17.2 | provided |
| jackson-annotations | 2.17.2 | provided |

## Assumptions Made
- Yousign API v3 uses Bearer token authentication (API key in Authorization header)
- Template-based signature request creation is the primary use case
- Documents are returned as Base64-encoded content for Bonita variable compatibility
- Audit trail is downloaded as a single PDF file
- Webhook events use the Yousign standard event format
- Rate limit retry follows 3 retries with exponential backoff (1s, 2s, 4s base)

## TODOs Requiring Human Input
- [ ] Replace placeholder icon (yousign-icon.png) with actual 16x16 PNG Yousign logo
- [ ] Set YOUSIGN_API_KEY environment variable for integration tests
- [ ] Set YOUSIGN_TEMPLATE_ID for create-from-template integration test
- [ ] Validate all operations against a real Yousign sandbox account
- [ ] Update beta-status.json as operations are validated
