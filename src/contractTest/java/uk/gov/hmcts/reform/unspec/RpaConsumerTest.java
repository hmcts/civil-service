package uk.gov.hmcts.reform.unspec;

import au.com.dius.pact.consumer.PactVerificationResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.unspec.service.flowstate.FlowState;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.unspec.matcher.IsValidJson.validateJson;

@Slf4j
class RpaConsumerTest extends BaseRpaTest {

    @Test
    @SneakyThrows
    void shouldGeneratePact_whenRoboticsCaseDataIsAtCaseStayed() {
        CaseData caseData = CaseDataBuilder.builder().atState(FlowState.Main.CLAIM_STAYED).build();
        String payload = roboticsDataMapper.toRoboticsCaseData(caseData).toJsonString();

        assertThat(payload, validateJson());

        String description = "Robotics case data for civil unspecified at case stayed";
        PactVerificationResult result = getPactVerificationResult(payload, description);

        assertEquals(PactVerificationResult.Ok.INSTANCE, result);
    }
}
