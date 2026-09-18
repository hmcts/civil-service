package uk.gov.hmcts.reform.civil.service.camunda;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaIncident;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaMessageCorrelation;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaProcessInstance;
import uk.gov.hmcts.reform.civil.model.camunda.CamundaVariableValue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Backs {@link CamundaVariableWireFormatTest} with a real HTTP implementation of
 * {@link CamundaRuntimeApi}, so the production mapping in {@link CamundaRuntimeClient}
 * is exercised against a live engine rather than a mock.
 */
class CamundaProbeSupport {

    private static final String PROBE_PROCESS_KEY = "varProbe";

    private final String baseUrl;
    private final ObjectMapper mapper;
    private final HttpClient http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10)).build();

    CamundaProbeSupport(String baseUrl, ObjectMapper mapper) {
        this.baseUrl = baseUrl.replaceAll("/$", "");
        this.mapper = mapper;
    }

    String startProcessInstance() {
        ensureDeployed();
        Map<String, Object> started = post("/process-definition/key/" + PROBE_PROCESS_KEY + "/start", Map.of());
        return (String) started.get("id");
    }

    Map<String, Object> rawVariable(String processInstanceId, String name) {
        return get("/process-instance/" + processInstanceId + "/variables/" + name + "?deserializeValues=false");
    }

    CamundaRuntimeApi api() {
        return new ProbeApi();
    }

    private void ensureDeployed() {
        Map<String, Object> count = get("/process-definition/count?key=" + PROBE_PROCESS_KEY);
        if (((Number) count.get("count")).intValue() > 0) {
            return;
        }
        String bpmn = """
            <?xml version="1.0" encoding="UTF-8"?>
            <bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                              xmlns:camunda="http://camunda.org/schema/1.0/bpmn"
                              id="Definitions_probe" targetNamespace="http://bpmn.io/schema/bpmn">
              <bpmn:process id="varProbe" isExecutable="true" camunda:historyTimeToLive="1">
                <bpmn:startEvent id="start"><bpmn:outgoing>f1</bpmn:outgoing></bpmn:startEvent>
                <bpmn:sequenceFlow id="f1" sourceRef="start" targetRef="wait"/>
                <bpmn:userTask id="wait"><bpmn:incoming>f1</bpmn:incoming></bpmn:userTask>
              </bpmn:process>
            </bpmn:definitions>
            """;
        String boundary = "----probe" + System.nanoTime();
        String payload = "--" + boundary + "\r\n"
            + "Content-Disposition: form-data; name=\"deployment-name\"\r\n\r\nvar-probe\r\n"
            + "--" + boundary + "\r\n"
            + "Content-Disposition: form-data; name=\"probe.bpmn\"; filename=\"probe.bpmn\"\r\n"
            + "Content-Type: text/xml\r\n\r\n" + bpmn + "\r\n"
            + "--" + boundary + "--\r\n";
        send(HttpRequest.newBuilder(URI.create(baseUrl + "/deployment/create"))
            .header("Content-Type", "multipart/form-data; boundary=" + boundary)
            .POST(HttpRequest.BodyPublishers.ofString(payload)).build());
    }

    private Map<String, Object> get(String path) {
        return read(send(HttpRequest.newBuilder(URI.create(baseUrl + path)).GET().build()));
    }

    private Map<String, Object> post(String path, Object body) {
        return read(send(HttpRequest.newBuilder(URI.create(baseUrl + path))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(write(body))).build()));
    }

    private String send(HttpRequest request) {
        try {
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                throw new IllegalStateException(
                    "Camunda probe call failed: " + response.statusCode() + " " + response.body());
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Camunda probe call failed", e);
        }
    }

    private Map<String, Object> read(String body) {
        try {
            return body == null || body.isBlank()
                ? Map.of() : mapper.readValue(body, new TypeReference<>() {});
        } catch (IOException e) {
            throw new IllegalStateException("Could not read Camunda response", e);
        }
    }

    private String write(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (IOException e) {
            throw new IllegalStateException("Could not write Camunda request", e);
        }
    }

    /** Only the methods the wire-format test exercises are implemented. */
    private class ProbeApi implements CamundaRuntimeApi {

        @Override
        public HashMap<String, CamundaVariableValue> getProcessVariables(String processInstanceId, String auth) {
            try {
                return mapper.readValue(
                    send(HttpRequest.newBuilder(
                        URI.create(baseUrl + "/process-instance/" + processInstanceId + "/variables")).GET().build()),
                    new TypeReference<>() {});
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void setProcessVariables(String auth, String processInstanceId, Map<String, Object> modifications) {
            post("/process-instance/" + processInstanceId + "/variables", modifications);
        }

        @Override
        public void setProcessVariable(String auth, String processInstanceId, String name, CamundaVariableValue value) {
            send(HttpRequest.newBuilder(
                    URI.create(baseUrl + "/process-instance/" + processInstanceId + "/variables/" + name))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(write(value))).build());
        }

        @Override
        public void correlateMessage(String auth, CamundaMessageCorrelation correlation) {
            post("/message", correlation);
        }

        @Override
        public List<Map<String, Object>> evaluateDecision(String k, String t, Map<String, Object> b) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<CamundaProcessInstance> getUnfinishedProcessInstancesWithIncidents(
            String a, boolean u, boolean w, String sa, String sb, Integer fr, Integer mr, String sby, String so, String is) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<CamundaProcessInstance> queryProcessInstances(
            String a, Integer fr, Integer mr, String sb, String so, Map<String, Object> f) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<String, Object> fetchErrorDetails(String incidentId, String auth) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<CamundaIncident> getLatestOpenIncidentForProcessInstance(
            String a, boolean o, String p, String sb, String so, int mr) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setJobRetries(String a, String jobId, Map<String, Object> body) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void modifyProcessInstance(String a, String p, Map<String, Object> r) {
            throw new UnsupportedOperationException();
        }
    }
}
