# Bonita Connector - Yousign eSignature

[![Build](https://github.com/bonitasoft/bonita-connector-yousign/actions/workflows/build.yml/badge.svg)](https://github.com/bonitasoft/bonita-connector-yousign/actions/workflows/build.yml)

Official Bonita connector for [Yousign](https://yousign.com/) eSignature API v3. Enables Bonita processes to create, manage, and track electronic signature requests.

**Status:** BETA (1.0.0-beta.2)

## Operations

| Operation | Description | API Endpoint |
|-----------|-------------|-------------|
| **Create From Template** | Creates a signature request from a Yousign template | `POST /signature_requests` |
| **Activate** | Activates a signature request (sends invitations) | `POST /signature_requests/{id}/activate` |
| **Get Status** | Gets the current status of a signature request | `GET /signature_requests/{id}` |
| **Cancel** | Cancels an active signature request | `POST /signature_requests/{id}/cancel` |
| **Download Document** | Downloads signed documents (single or ZIP) | `GET /signature_requests/{id}/documents/...` |
| **Download Audit Trail** | Downloads the audit trail PDF | `GET /signature_requests/{id}/audit_trails/download` |
| **List** | Lists signature requests with filters and pagination | `GET /signature_requests` |
| **Register Webhook** | Registers a webhook for signature events | `POST /webhooks` |

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Bonita Studio 10.2.0+
- Yousign API key ([get one here](https://yousign.com/))

### Build

```bash
# Build and run tests
./mvnw clean verify

# Install to local Maven repo (required before Bonita Studio import)
./mvnw install -DskipTests
```

### Import into Bonita Studio

1. Build the project: `./mvnw install -DskipTests`
2. In Bonita Studio, go to **Development > Connectors > Import connector...**
3. Select the JAR file: `target/bonita-connector-yousign-1.0.0-beta.2.jar`
4. The 8 Yousign operations will appear under the **Yousign** category

### Maven Coordinates

```xml
<dependency>
    <groupId>org.bonitasoft.connectors</groupId>
    <artifactId>bonita-connector-yousign</artifactId>
    <version>1.0.0-beta.2</version>
</dependency>
```

## Create From Template - Template placeholders

Yousign API v3 requires that when `template_id` is used, all signer data and read-only text field values live inside a `template_placeholders` object. This connector exposes that object through the **mandatory** `templatePlaceholdersJson` input. The shape is validated before sending, so a malformed payload fails fast with a Bonita error instead of an opaque 4xx from Yousign.

Required shape:

```json
{
  "signers": [
    {
      "label": "policyHolder",
      "info": {
        "first_name": "Jane",
        "last_name": "Doe",
        "email": "jane.doe@example.com",
        "locale": "en"
      }
    }
  ],
  "read_only_text_fields": [
    { "label": "claim_id", "text": "SIN-2026-42" },
    { "label": "amount",   "text": "1800" }
  ]
}
```

The `label` in each signer / `read_only_text_field` must match the placeholder label defined on the Yousign template. The connector enforces:

- The input is a JSON **object** (not an array, not a primitive).
- The object contains a key named `signers`.
- `signers` is an **array**.
- The array is **non-empty**.

Minimal Groovy expression for the input (single signer, no read-only fields):

```groovy
import groovy.json.JsonOutput

return JsonOutput.toJson([
    signers: [
        [ label: "policyHolder",
          info : [ first_name: "Jane", last_name: "Doe", email: "jane.doe@example.com", locale: "en" ] ]
    ],
    read_only_text_fields: []
])
```

`POST /signature_requests` returns a signature request in `draft` status. Chain the **Activate** operation (passing the `signatureRequestId` output) on the same task or a downstream task to transition it to `ongoing` and trigger the signer email.

## Breaking changes from 1.0.0-beta.1

> Read this if you already use the connector.

- **`templateTextFieldsJson` renamed to `templatePlaceholdersJson`**. The old name implied "text fields" but the input always carried the full placeholders object including signers. The new name reflects that. Update any `.proc` mapping the old input.
- **`templatePlaceholdersJson` is now mandatory** and its shape is validated (object with non-empty `signers` array). The `1.0.0-beta.1` version accepted blank values silently and produced opaque API 4xx errors at runtime.
- **Removed inputs (silently ignored in 1.0.0-beta.1)**: `additionalSignersJson`, `signerLabel`, `signerFirstName`, `signerLastName`, `signerEmail`, `signerPhoneNumber`, `signerLocale`. None of these were forwarded to the API after the v3 migration. Pass signer data via `templatePlaceholdersJson.signers[]` instead.
- **`yousign-activate` BETA status downgraded to `untested`**: there is no automated integration test exercising activate end-to-end yet. The operation works (manually validated against sandbox) but does not meet the bar for `validated`.

## Configuration

### Connection Parameters (all operations)

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| apiKey | String | Yes | - | Yousign API key (Bearer token) |
| baseUrl | String | No | `https://api-sandbox.yousign.app/v3` | API base URL |
| connectTimeout | Integer | No | 30000 | Connection timeout (ms) |
| readTimeout | Integer | No | 60000 | Read timeout (ms) |

### Environment URLs
- **Sandbox:** `https://api-sandbox.yousign.app/v3`
- **Production:** `https://api.yousign.app/v3`

## Error Handling

| HTTP Code | Behavior |
|-----------|----------|
| 400 | Fail immediately (validation error) |
| 401 | Fail immediately (auth error) |
| 403 | Fail immediately (access denied) |
| 404 | Fail immediately (not found) |
| 429/5xx | Retry 3x with exponential backoff (1s, 2s, 4s) |

All operations always set `success` (Boolean) and `errorMessage` (String) outputs.

## Integration Tests

Set these environment variables to run against the real Yousign API:

```bash
export YOUSIGN_API_KEY="your-sandbox-api-key"
export YOUSIGN_BASE_URL="https://api-sandbox.yousign.app/v3"
export YOUSIGN_TEMPLATE_ID="your-template-uuid"
export YOUSIGN_SIGNER_LABEL="Signer"
export YOUSIGN_SIGNER_EMAIL="test@example.com"
export YOUSIGN_SIGNATURE_REQUEST_ID="existing-request-uuid"
export YOUSIGN_WEBHOOK_ENDPOINT_URL="https://your-app.example.com/webhook"
```

Then run:
```bash
./mvnw verify
```

## Build Artifacts

After building, the following artifacts are available in `target/`:

| Artifact | Description |
|----------|-------------|
| `bonita-connector-yousign-1.0.0-beta.2.jar` | Main JAR for Bonita Studio import |
| `bonita-connector-yousign-1.0.0-beta.2-all.zip` | All operations bundled |
| `bonita-connector-yousign-1.0.0-beta.2-{operation}-impl.zip` | Individual operation ZIPs |

## Technology Stack

- **Java 17** (records, sealed classes, pattern matching)
- **OkHttp 4.12.0** (HTTP client)
- **Jackson 2.17.2** (JSON serialization)
- **Bonita 10.2.0** (connector API)
- **JUnit 5** + **Mockito** (unit tests)
- **jqwik** (property-based tests)
- **Testcontainers** (Docker-based integration tests)

## License

[GPL-2.0](LICENSE)
