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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

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
        caseData = CaseDataBuilder.builder().build();
        caseData.setResponseClaimExpertSpecRequired(YES);
        ExpertDetails expertDetails = new ExpertDetails();
        expertDetails.setExpertName("Expert Name");
        expertDetails.setFieldofExpertise("Field");
        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setRespondToClaimExperts(expertDetails);
        caseData.setRespondent1DQ(respondent1DQ);
    }

    @Test
    void shouldUpdateCaseDataWhenExpertRequired() {
        CaseData updatedData = updater.update(caseData);

        assertThat(updatedData).isNotNull();

        Respondent1DQ updatedRespondent1DQ = updatedData.getRespondent1DQ();
        assertThat(updatedRespondent1DQ).isNotNull();

        Experts updatedExperts = updatedRespondent1DQ.getRespondent1DQExperts();
        assertThat(updatedExperts).isNotNull();
        assertThat(updatedExperts.getExpertRequired()).isEqualTo(YES);
        assertThat(updatedExperts.getDetails()).isNotEmpty();
        assertThat(updatedExperts.getDetails().get(0).getValue().getName()).isEqualTo("Expert Name");
    }

    @Test
    void shouldUpdateCaseDataWhenExpertNotRequired() {
        caseData.setResponseClaimExpertSpecRequired(NO);
        Respondent1DQ respondent1DQ = new Respondent1DQ();
        respondent1DQ.setRespondToClaimExperts(null);
        caseData.setRespondent1DQ(respondent1DQ);

        CaseData updatedData = updater.update(caseData);

        assertThat(updatedData).isNotNull();

        Respondent1DQ updatedRespondent1DQ = updatedData.getRespondent1DQ();
        assertThat(updatedRespondent1DQ).isNotNull();

        Experts updatedExperts = updatedRespondent1DQ.getRespondent1DQExperts();
        assertThat(updatedExperts).isNotNull();
        assertThat(updatedExperts.getExpertRequired()).isEqualTo(NO);
    }

    @Test
    void shouldHandleNullRespondent1DQ() {
        caseData.setRespondent1DQ(null);

        CaseData updatedData = updater.update(caseData);

        assertThat(updatedData).isNotNull();
        assertThat(updatedData.getRespondent1DQ()).isNull();
    }
}
