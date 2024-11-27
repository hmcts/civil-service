package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.fasttracktests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fasttrack.FastTrackJudgesRecitalFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgesRecital;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class FastTrackJudgesRecitalFieldBuilderTest {

    @InjectMocks
    private FastTrackJudgesRecitalFieldBuilder fastTrackJudgesRecitalFieldBuilder;

    @Test
    void shouldBuildFastTrackJudgesRecitalFields() {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        fastTrackJudgesRecitalFieldBuilder.build(caseDataBuilder);

        CaseData caseData = caseDataBuilder.build();
        FastTrackJudgesRecital judgesRecital = caseData.getFastTrackJudgesRecital();
        assertThat(judgesRecital).isNotNull();
        assertThat(judgesRecital.getInput()).isEqualTo("Upon considering the statements of case and the information provided by the parties,");
    }
}