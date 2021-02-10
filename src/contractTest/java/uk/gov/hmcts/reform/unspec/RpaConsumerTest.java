package uk.gov.hmcts.reform.unspec;

import au.com.dius.pact.consumer.PactVerificationResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.service.flowstate.FlowState;
import uk.gov.hmcts.reform.unspec.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.unspec.service.robotics.mapper.EventHistoryMapper;
import uk.gov.hmcts.reform.unspec.service.robotics.mapper.RoboticsAddressMapper;
import uk.gov.hmcts.reform.unspec.service.robotics.mapper.RoboticsDataMapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.unspec.enums.RespondentResponseType.COUNTER_CLAIM;
import static uk.gov.hmcts.reform.unspec.enums.RespondentResponseType.FULL_ADMISSION;
import static uk.gov.hmcts.reform.unspec.enums.RespondentResponseType.PART_ADMISSION;
import static uk.gov.hmcts.reform.unspec.matcher.IsValidJson.validateJson;

@Slf4j
@SpringBootTest(classes = {
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class,
    StateFlowEngine.class,
    EventHistoryMapper.class,
    RoboticsDataMapper.class,
    RoboticsAddressMapper.class
})
class RpaConsumerTest extends BaseRpaTest {

    @Autowired
    RoboticsDataMapper roboticsDataMapper;

    @Nested
    class UnrepresentedDefendant {

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenClaimAgainstUnrepresentedDefendant() {
            CaseData caseData = CaseDataBuilder.builder()
                .atState(FlowState.Main.PROCEEDS_OFFLINE_UNREPRESENTED_DEFENDANT)
                .build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for claim against unrepresented defendant";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }
    }

    @Nested
    class AdmissionsOrCounterClaim {

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenDefendantRespondedWithPartAdmission() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondedToClaim(PART_ADMISSION).build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for defendant responded with part admission";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenDefendantRespondedWithFullAdmission() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondedToClaim(FULL_ADMISSION).build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for defendant responded with full admission";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }

        @Test
        @SneakyThrows
        void shouldGeneratePact_whenDefendantRespondedWithCounterClaim() {
            CaseData caseData = CaseDataBuilder.builder().atStateRespondedToClaim(COUNTER_CLAIM).build();
            String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

            assertThat(payload, validateJson());

            String description = "Robotics case data for defendant responded with counter claim";
            PactVerificationResult result = getPactVerificationResult(payload, description);

            assertEquals(PactVerificationResult.Ok.INSTANCE, result);
        }
    }
}
