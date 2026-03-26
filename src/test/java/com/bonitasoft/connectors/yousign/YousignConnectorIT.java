package com.bonitasoft.connectors.yousign;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.bonitasoft.web.client.BonitaClient;
import org.bonitasoft.web.client.api.ArchivedProcessInstanceApi;
import org.bonitasoft.web.client.api.ProcessInstanceApi;
import org.bonitasoft.web.client.exception.NotFoundException;
import org.bonitasoft.web.client.model.ArchivedProcessInstance;
import org.bonitasoft.web.client.services.policies.OrganizationImportPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Process-based integration tests for Yousign connectors.
 * Deploys connectors into a real Bonita process engine running in Docker.
 */
@Testcontainers
@org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable(named = "YOUSIGN_API_KEY", matches = ".+")
class YousignConnectorIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(YousignConnectorIT.class);

    private static final String LIST_DEF_ID = "yousign-list";
    private static final String LIST_DEF_VERSION = "1.0.0";

    @Container
    static GenericContainer<?> BONITA_CONTAINER = new GenericContainer<>(
            DockerImageName.parse("bonita:10.2.0"))
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/bonita"))
            .withLogConsumer(new Slf4jLogConsumer(LOGGER));

    private BonitaClient client;

    @BeforeAll
    static void installOrganization() {
        var client = BonitaClient
                .builder(String.format("http://%s:%s/bonita",
                        BONITA_CONTAINER.getHost(),
                        BONITA_CONTAINER.getFirstMappedPort()))
                .build();
        client.login("install", "install");
        client.users().importOrganization(
                new File(YousignConnectorIT.class.getResource("/ACME.xml").getFile()),
                OrganizationImportPolicy.IGNORE_DUPLICATES);
        client.logout();
    }

    @BeforeEach
    void login() {
        client = BonitaClient
                .builder(String.format("http://%s:%s/bonita",
                        BONITA_CONTAINER.getHost(),
                        BONITA_CONTAINER.getFirstMappedPort()))
                .build();
        client.login("install", "install");
    }

    @AfterEach
    void logout() {
        client.logout();
    }

    @Test
    void should_list_signature_requests_via_process() throws Exception {
        var inputs = commonInputs();

        var outputs = Map.of(
                "resultSuccess", ConnectorTestToolkit.Output.create("success", Boolean.class.getName()),
                "resultRequests", ConnectorTestToolkit.Output.create("signatureRequestsJson", String.class.getName()));

        var barFile = ConnectorTestToolkit.buildConnectorToTest(
                LIST_DEF_ID, LIST_DEF_VERSION, inputs, outputs);
        var processResponse = ConnectorTestToolkit.importAndLaunchProcess(barFile, client);

        await().until(pollInstanceState(processResponse.getCaseId()), "started"::equals);

        var success = ConnectorTestToolkit.getProcessVariableValue(client,
                processResponse.getCaseId(), "resultSuccess");
        assertThat(success).isEqualTo("true");
    }

    private Map<String, String> commonInputs() {
        var inputs = new HashMap<String, String>();
        inputs.put("apiKey", System.getenv("YOUSIGN_API_KEY"));
        inputs.put("baseUrl", System.getenv().getOrDefault("YOUSIGN_BASE_URL", "https://api-sandbox.yousign.app/v3"));
        return inputs;
    }

    private Callable<String> pollInstanceState(String id) {
        return () -> {
            try {
                var instance = client.get(ProcessInstanceApi.class)
                        .getProcessInstanceById(id, (String) null);
                return instance.getState().name().toLowerCase();
            } catch (NotFoundException e) {
                var archived = getCompletedProcess(id);
                return archived != null ? archived.getState().name().toLowerCase() : "unknown";
            }
        };
    }

    private ArchivedProcessInstance getCompletedProcess(String id) {
        var archivedInstances = client.get(ArchivedProcessInstanceApi.class)
                .searchArchivedProcessInstances(
                        new ArchivedProcessInstanceApi.SearchArchivedProcessInstancesQueryParams()
                                .c(1)
                                .p(0)
                                .f(List.of("caller=any", "sourceObjectId=" + id)));
        if (!archivedInstances.isEmpty()) {
            return archivedInstances.get(0);
        }
        return null;
    }
}
