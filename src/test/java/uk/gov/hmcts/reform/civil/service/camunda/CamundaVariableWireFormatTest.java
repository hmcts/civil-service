package uk.gov.hmcts.reform.civil.service.camunda;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.HearingDay;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Round-trips process variables through a real Camunda engine to prove the wire format
 * the client produces is one the engine accepts and returns intact.
 *
 * <p>Disabled unless {@code -Dcamunda.probe.url} is set, because it needs a running
 * engine. Verified against 7.21.0, the version deployed in AAT, preview and production:</p>
 *
 * <pre>
 *   docker run -d -p 18080:8080 camunda/camunda-bpm-platform:run-7.21.0
 *   ./gradlew test --tests "*CamundaVariableWireFormatTest*" \
 *       -Dcamunda.probe.url=http://localhost:18080/engine-rest
 * </pre>
 *
 * <p>The case that matters is the collection. Sending a complex value untyped makes the
 * engine fall back to Java serialisation; sending it as an {@code Object} with
 * {@code application/json} keeps it readable as JSON, and both forms deserialise to the
 * same thing on read, so instances written either way stay compatible.</p>
 */
@ExtendWith(MockitoExtension.class)
@EnabledIfSystemProperty(named = "camunda.probe.url", matches = ".+")
class CamundaVariableWireFormatTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    private ObjectMapper objectMapper;
    private CamundaRuntimeClient client;
    private CamundaProbeSupport probe;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        probe = new CamundaProbeSupport(System.getProperty("camunda.probe.url"), objectMapper);
        client = new CamundaRuntimeClient(authTokenGenerator, probe.api(), objectMapper);
        when(authTokenGenerator.generate()).thenReturn("s2s");
    }

    @Test
    void shouldRoundTripHearingNoticeVariablesThroughTheEngine() {
        String processInstanceId = probe.startProcessInstance();

        HearingNoticeVariables written = new HearingNoticeVariables()
            .setHearingId("2000018496")
            .setCaseId(1789552221666600L)
            .setHearingStartDateTime(LocalDateTime.of(2026, 2, 2, 10, 0))
            .setHearingLocationEpims("196538")
            .setCaseState("HEARING_READINESS")
            .setRequestVersion(1L)
            .setHearingType("AAA7-TRI")
            .setDays(List.of(new HearingDay(
                LocalDateTime.of(2026, 2, 2, 10, 0),
                LocalDateTime.of(2026, 2, 2, 16, 0))));

        client.setProcessVariables(processInstanceId, written.toMap(objectMapper));

        Map<String, Object> read = client.getProcessVariables(processInstanceId);
        HearingNoticeVariables readBack = objectMapper.convertValue(read, HearingNoticeVariables.class);

        assertThat(readBack).isEqualTo(written);
        assertThat(readBack.getDays()).hasSize(1);
        assertThat(readBack.getDays().get(0).getHearingStartDateTime())
            .isEqualTo(LocalDateTime.of(2026, 2, 2, 10, 0));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldStoreCollectionsAsJsonRatherThanJavaSerialisation() {
        String processInstanceId = probe.startProcessInstance();

        client.setProcessVariables(processInstanceId, Map.of("dispatchedHearingIds", List.of("h1", "h2")));

        Map<String, Object> raw = probe.rawVariable(processInstanceId, "dispatchedHearingIds");
        assertThat(raw).containsEntry("type", "Object");
        assertThat((Map<String, Object>) raw.get("valueInfo"))
            .containsEntry("serializationDataFormat", "application/json");
        assertThat(raw.get("value").toString()).doesNotStartWith("rO0");
    }

    @Test
    void shouldRoundTripASingleScalarVariable() {
        String processInstanceId = probe.startProcessInstance();

        client.setProcessVariable(processInstanceId, "welshEnabled", true);

        assertThat(client.getProcessVariables(processInstanceId)).containsEntry("welshEnabled", true);
    }
}
