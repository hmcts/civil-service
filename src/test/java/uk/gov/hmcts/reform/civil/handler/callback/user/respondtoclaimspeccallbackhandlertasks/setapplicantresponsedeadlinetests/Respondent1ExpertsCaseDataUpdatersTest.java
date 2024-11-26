package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinetests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec.Respondent1ExpertsCaseDataUpdaters;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.dq.ExpertDetails;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class Respondent1ExpertsCaseDataUpdatersTest {

    @InjectMocks
    private Respondent1ExpertsCaseDataUpdaters updater;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder()
                .responseClaimExpertSpecRequired(YES)
                .respondent1DQ(Respondent1DQ.builder()
                        .respondToClaimExperts(ExpertDetails.builder()
                                .expertName("Expert Name")
                                .fieldofExpertise("Field")
                                .build())
                        .build())
                .build();
    }

    @Test
    void shouldUpdateCaseDataWhenExpertRequired() {
        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder()
                .respondent1DQ(caseData.getRespondent1DQ());

        updater.update(caseData, updatedData);

        Respondent1DQ updatedRespondent1DQ = updatedData.build().getRespondent1DQ();
        assertThat(updatedRespondent1DQ).isNotNull();

        Experts updatedExperts = updatedRespondent1DQ.getRespondent1DQExperts();
        assertThat(updatedExperts).isNotNull();
        assertThat(updatedExperts.getExpertRequired()).isEqualTo(YES);
        assertThat(updatedExperts.getDetails()).isNotEmpty();
        assertThat(updatedExperts.getDetails().get(0).getValue().getName()).isEqualTo("Expert Name");
    }

    @Test
    void shouldUpdateCaseDataWhenExpertNotRequired() {
        caseData = caseData.toBuilder()
                .responseClaimExpertSpecRequired(NO)
                .respondent1DQ(Respondent1DQ.builder()
                        .respondToClaimExperts(null)
                        .build())
                .build();

        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder()
                .respondent1DQ(caseData.getRespondent1DQ());

        updater.update(caseData, updatedData);

        Respondent1DQ updatedRespondent1DQ = updatedData.build().getRespondent1DQ();
        assertThat(updatedRespondent1DQ).isNotNull();

        Experts updatedExperts = updatedRespondent1DQ.getRespondent1DQExperts();
        assertThat(updatedExperts).isNotNull();
        assertThat(updatedExperts.getExpertRequired()).isEqualTo(NO);
    }

    @Test
    void shouldHandleNullRespondent1DQ() {
        caseData = caseData.toBuilder()
                .respondent1DQ(null)
                .build();

        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();

        updater.update(caseData, updatedData);

        CaseData result = updatedData.build();
        assertThat(result.getRespondent1DQ()).isNull();
    }
}
