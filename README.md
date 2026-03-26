# Bonita Connector - Yousign eSignature

[![Build](https://github.com/bonitasoft/bonita-connector-yousign/actions/workflows/build.yml/badge.svg)](https://github.com/bonitasoft/bonita-connector-yousign/actions/workflows/build.yml)

Official Bonita connector for [Yousign](https://yousign.com/) eSignature API v3. Enables Bonita processes to create, manage, and track electronic signature requests.

**Status:** BETA (1.0.0-beta.1)

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
3. Select the JAR file: `target/bonita-connector-yousign-1.0.0-beta.1.jar`
4. The 8 Yousign operations will appear under the **Yousign** category

### Maven Coordinates

```xml
<dependency>
    <groupId>org.bonitasoft.connectors</groupId>
    <artifactId>bonita-connector-yousign</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```

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
| `bonita-connector-yousign-1.0.0-beta.1.jar` | Main JAR for Bonita Studio import |
| `bonita-connector-yousign-1.0.0-beta.1-all.zip` | All operations bundled |
| `bonita-connector-yousign-1.0.0-beta.1-{operation}-impl.zip` | Individual operation ZIPs |

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
