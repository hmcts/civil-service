package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinetests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec.Respondent2ExpertsCaseDataUpdater;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.dq.ExpertDetails;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class Respondent2ExpertsCaseDataUpdaterTest {

    @InjectMocks
    private Respondent2ExpertsCaseDataUpdater updater;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        Respondent2DQ respondent2DQ = Respondent2DQ.builder()
                .respondToClaimExperts2(ExpertDetails.builder()
                        .expertName("Expert Name")
                        .fieldofExpertise("Field")
                        .build())
                .build();

        caseData = CaseData.builder()
                .respondent2DQ(respondent2DQ)
                .responseClaimExpertSpecRequired2(YesOrNo.YES)
                .build();
    }

    @Test
    void shouldUpdateCaseDataWithExperts() {
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();
        updater.update(caseData, updatedData);

        Respondent2DQ updatedRespondent2DQ = updatedData.build().getRespondent2DQ();
        assertThat(updatedRespondent2DQ).isNotNull();

        Experts updatedExperts = updatedRespondent2DQ.getRespondent2DQExperts();
        assertThat(updatedExperts).isNotNull();
        assertThat(updatedExperts.getExpertRequired()).isEqualTo(YesOrNo.YES);

        assertThat(updatedExperts.getDetails()).isNotNull();
        assertThat(updatedExperts.getDetails()).hasSize(1);
        assertThat(updatedExperts.getDetails().get(0).getValue().getName()).isEqualTo("Expert Name");
        assertThat(updatedExperts.getDetails().get(0).getValue().getFieldOfExpertise()).isEqualTo("Field");
    }

    @Test
    void shouldHandleNoExpertsRequired() {
        Respondent2DQ respondent2DQ = Respondent2DQ.builder().build();
        caseData = caseData.toBuilder()
                .respondent2DQ(respondent2DQ)
                .responseClaimExpertSpecRequired2(YesOrNo.NO)
                .build();

        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();
        updater.update(caseData, updatedData);

        Respondent2DQ updatedRespondent2DQ = updatedData.build().getRespondent2DQ();
        assertThat(updatedRespondent2DQ).isNotNull();

        Experts updatedExperts = updatedRespondent2DQ.getRespondent2DQExperts();
        assertThat(updatedExperts).isNotNull();
        assertThat(updatedExperts.getExpertRequired()).isEqualTo(YesOrNo.NO);

        assertThat(updatedExperts.getDetails()).isNull();
    }
}
