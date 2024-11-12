package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests.disposalhearingtests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.disposalhearing.JudgesRecitalFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingJudgesRecital;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.UPON_CONSIDERING;

@ExtendWith(MockitoExtension.class)
class JudgesRecitalFieldBuilderTest {

    @InjectMocks
    private JudgesRecitalFieldBuilder judgesRecitalFieldBuilder;

    @Test
    void shouldSetJudgesRecital() {
        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();

        judgesRecitalFieldBuilder.build(updatedData);

        CaseData result = updatedData.build();
        DisposalHearingJudgesRecital judgesRecital = result.getDisposalHearingJudgesRecital();
        assertThat(judgesRecital).isNotNull();
        assertThat(judgesRecital.getInput()).isEqualTo(UPON_CONSIDERING);
    }
}