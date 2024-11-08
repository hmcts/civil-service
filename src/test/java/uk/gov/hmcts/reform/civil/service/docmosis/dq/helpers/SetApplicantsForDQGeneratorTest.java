package uk.gov.hmcts.reform.civil.service.docmosis.dq.helpers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DirectionsQuestionnaireForm;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.docmosis.RepresentativeService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class SetApplicantsForDQGeneratorTest {

    @Mock
    private RepresentativeService representativeService;

    @InjectMocks
    private SetApplicantsForDQGenerator setApplicantsForDQGenerator;

    @Test
    void shouldSetApplicantforDQFormBuilder() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoApplicants()
            .applicant1(PartyBuilder.builder().individual().build())
            .build();

        DirectionsQuestionnaireForm.DirectionsQuestionnaireFormBuilder builder = DirectionsQuestionnaireForm.builder();

        setApplicantsForDQGenerator.setApplicants(builder, caseData);

        DirectionsQuestionnaireForm form = builder.build();

        assertNotNull(form.getApplicant());
        assertNotNull(form.getApplicant2());
        verify(representativeService, times(2)).getApplicantRepresentative(caseData);
    }

    @Test
    void shouldSetSingleApplicantForDQFormBuilder() {
        CaseData caseData = CaseDataBuilder.builder()
            .applicant1(PartyBuilder.builder().individual().build())
            .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YES)
            .build();

        DirectionsQuestionnaireForm.DirectionsQuestionnaireFormBuilder builder = DirectionsQuestionnaireForm.builder();

        setApplicantsForDQGenerator.setApplicants(builder, caseData);

        DirectionsQuestionnaireForm form = builder.build();

        assertNotNull(form.getApplicant());
        assertNull(form.getApplicant2());
        verify(representativeService, times(1)).getApplicantRepresentative(caseData);
    }
}
