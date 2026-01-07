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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class Respondent2ExpertsCaseDataUpdaterTest {

    @InjectMocks
    private Respondent2ExpertsCaseDataUpdater updater;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        Respondent2DQ respondent2DQ = new Respondent2DQ();
        ExpertDetails  expertDetails = new ExpertDetails();
        expertDetails.setExpertName("Expert Name");
        expertDetails.setFieldofExpertise("Field");
        respondent2DQ.setRespondToClaimExperts2(expertDetails);

        caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent2DQ(respondent2DQ);
        caseData.setResponseClaimExpertSpecRequired2(YesOrNo.YES);
    }

    @Test
    void shouldUpdateCaseDataWithExperts() {
        CaseData result = updater.update(caseData);

        Respondent2DQ updatedRespondent2DQ = result.getRespondent2DQ();
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
        Respondent2DQ respondent2DQ = new Respondent2DQ();
        CaseData testCaseData = CaseDataBuilder.builder().build();
        testCaseData.setRespondent2DQ(respondent2DQ);
        testCaseData.setResponseClaimExpertSpecRequired2(YesOrNo.NO);

        CaseData result = updater.update(testCaseData);

        Respondent2DQ updatedRespondent2DQ = result.getRespondent2DQ();
        assertThat(updatedRespondent2DQ).isNotNull();

        Experts updatedExperts = updatedRespondent2DQ.getRespondent2DQExperts();
        assertThat(updatedExperts).isNotNull();
        assertThat(updatedExperts.getExpertRequired()).isEqualTo(YesOrNo.NO);

        assertThat(updatedExperts.getDetails()).isNull();
    }
}
