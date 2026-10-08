package uk.gov.hmcts.reform.civil.service.camunda;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.camunda.community.rest.client.model.VariableValueDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.HearingDay;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Proves a revert of DTSCCI-6393 is safe, which is the one thing the forward probe does not cover.
 *
 * <p>{@link CamundaVariableWireFormatTest} verifies the deploy direction: values written by the old
 * code, which the engine stored as {@code application/x-java-serialized-object} because Holunda sent
 * them untyped, are read correctly by the new code. The reverse was never tested, and that gap is
 * why the PR carries a "do not revert, roll forward with a fix" instruction. The hearing notice
 * scheduler is long-lived, so a revert even a day after deploy meets values written in the new
 * format and cannot simply be waited out.</p>
 *
 * <p>This test closes that gap. It writes variables through the new client, so the engine stores
 * them as JSON, then reads them back through
 * {@code org.camunda.community.rest.client.model.VariableValueDto}, the Holunda model that a revert
 * restores in {@link CamundaRuntimeApi}. It then applies the same value extraction
 * {@link CamundaRuntimeClient#getProcessVariables} performs, so what is asserted is the reverted
 * read path rather than an approximation of it.</p>
 *
 * <p>Disabled unless {@code -Dcamunda.probe.url} is set, because it needs a real engine. Run against
 * 7.21.0, the version in AAT, preview and production:</p>
 *
 * <pre>
 *   docker run -d -p 18080:8080 camunda/camunda-bpm-platform:run-7.21.0
 *   ./gradlew test --tests "*CamundaVariableRevertCompatibilityTest*" \
 *       -Dcamunda.probe.url=http://localhost:18080/engine-rest
 * </pre>
 *
 * <p>If this passes, the revert restriction can be lifted. If it fails, the restriction is load
 * bearing and the failure names what breaks.</p>
 */
@ExtendWith(MockitoExtension.class)
@EnabledIfSystemProperty(named = "camunda.probe.url", matches = ".+")
class CamundaVariableRevertCompatibilityTest {

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

    /**
     * The whole point: JSON written now, read through the model a revert brings back.
     */
    @Test
    void jsonWrittenByTheNewCodeShouldBeReadableThroughHolundasModel() {
        String processInstanceId = probe.startProcessInstance();
        HearingNoticeVariables written = hearingNoticeVariables();

        client.setProcessVariables(processInstanceId, written.toMap(objectMapper));

        Map<String, Object> asRevertedCodeWouldSee = readThroughHolundaModel(processInstanceId);

        HearingNoticeVariables readBack =
            objectMapper.convertValue(asRevertedCodeWouldSee, HearingNoticeVariables.class);

        assertThat(readBack)
            .as("a revert must still read variables the new code wrote, or rolling back loses them")
            .isEqualTo(written);
        assertThat(readBack.getDays())
            .as("the collection is the case that falls back to Java serialisation when untyped")
            .hasSize(1);
        assertThat(readBack.getDays().get(0).getHearingStartDateTime())
            .isEqualTo(LocalDateTime.of(2026, 2, 2, 10, 0));
    }

    /**
     * Guards the test itself. If the engine happened to store these as Java serialisation, the test
     * above would pass while proving nothing about the revert case, because that is the format the
     * old code already read happily.
     */
    @Test
    @SuppressWarnings("unchecked")
    void theVariableUnderTestShouldActuallyBeStoredAsJson() {
        String processInstanceId = probe.startProcessInstance();

        client.setProcessVariables(processInstanceId, hearingNoticeVariables().toMap(objectMapper));

        Map<String, Object> raw = probe.rawVariable(processInstanceId, "days");
        assertThat(raw).containsEntry("type", "Object");
        assertThat((Map<String, Object>) raw.get("valueInfo"))
            .containsEntry("serializationDataFormat", "application/json");
        assertThat(raw.get("value").toString())
            .as("rO0 is base64 Java serialisation; seeing it here means this test is vacuous")
            .doesNotStartWith("rO0");
    }

    /**
     * A scalar as well as the collection, since the two take different paths through the engine's
     * type handling and a revert meets both.
     */
    @Test
    void scalarsWrittenByTheNewCodeShouldAlsoBeReadableThroughHolundasModel() {
        String processInstanceId = probe.startProcessInstance();

        client.setProcessVariable(processInstanceId, "welshEnabled", true);

        assertThat(readThroughHolundaModel(processInstanceId)).containsEntry("welshEnabled", true);
    }

    /**
     * Reads the variables exactly as a reverted {@code CamundaRuntimeApi} would: deserialised into
     * Holunda's {@code VariableValueDto}, then flattened to name to value the way
     * {@code CamundaRuntimeClient.getProcessVariables} does.
     */
    private Map<String, Object> readThroughHolundaModel(String processInstanceId) {
        String body = probe.rawJson("/process-instance/" + processInstanceId + "/variables");
        HashMap<String, VariableValueDto> holundaView;
        try {
            holundaView = objectMapper.readValue(body, new TypeReference<>() {});
        } catch (Exception e) {
            throw new AssertionError(
                "Holunda's VariableValueDto could not deserialise the engine response, so a revert "
                    + "would fail to read variables written by the new code. Body: " + body, e);
        }
        Map<String, Object> flattened = new HashMap<>();
        holundaView.forEach((name, dto) -> flattened.put(name, dto.getValue()));
        return flattened;
    }

    private HearingNoticeVariables hearingNoticeVariables() {
        return new HearingNoticeVariables()
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
    }
}
