package uk.gov.hmcts.reform.civil.service.docmosis.dq.helpers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.docmosis.dq.DirectionsQuestionnaireForm;
import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.DocmosisTemplateDataUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class RespondentTemplateForDQGeneratorTest {

    @InjectMocks
    private RespondentTemplateForDQGenerator respondentTemplateForDQGenerator;

    @Mock
    private SetApplicantsForDQGenerator setApplicantsForDQGenerator;

    @Mock
    private GetRespondentsForDQGenerator getRespondentsForDQGenerator;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private LocationReferenceDataService locationRefDataService;

    @Mock
    private CaseData caseData;

    @Mock
    private DQ dq;

    private static final String BEARER_TOKEN = "Bearer Token";

    @Test
    void shouldReturnRespondent1TemplateData() {

        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .build()
            .toBuilder()
            .businessProcess(BusinessProcess.builder()
                                 .camundaEvent("CLAIMANT_RESPONSE").build())
            .build();

        DirectionsQuestionnaireForm result =
            respondentTemplateForDQGenerator.getRespondent1TemplateData(caseData, "ONE", BEARER_TOKEN);

        assertNotNull(result);
        assertNotNull(result.getSolicitorReferences());
        assertNotNull(result.getExperts());
        assertNotNull(result.getWitnesses());
        assertEquals(caseData.getLegacyCaseReference(), result.getReferenceNumber());
        assertEquals(DocmosisTemplateDataUtils.toCaseName.apply(caseData), result.getCaseName());
    }

    @Test
    void shouldReturnRespondent2TemplateData() {

        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .respondent2DQ()
            .respondent2SameLegalRepresentative(YES)
            .build()
            .toBuilder()
            .businessProcess(BusinessProcess.builder()
                                 .camundaEvent("CLAIMANT_RESPONSE").build())
            .build();

        DirectionsQuestionnaireForm result =
            respondentTemplateForDQGenerator.getRespondent2TemplateData(caseData, "TWO", BEARER_TOKEN);

        assertNotNull(result);
        assertNotNull(result.getSolicitorReferences());
        assertNotNull(result.getExperts());
        assertEquals(caseData.getLegacyCaseReference(), result.getReferenceNumber());
        assertEquals(DocmosisTemplateDataUtils.toCaseName.apply(caseData), result.getCaseName());
    }

    @Test
    void shouldReturnRespondent2TemplateDataWithFastClaim() {

        CaseData caseData = CaseDataBuilder.builder()
            .atStateApplicantRespondToDefenceAndProceed()
            .respondent2DQ()
            .respondent2SameLegalRepresentative(YES)
            .responseClaimTrack(FAST_CLAIM.name())
            .build()
            .toBuilder()
            .businessProcess(BusinessProcess.builder()
                                 .camundaEvent("CLAIMANT_RESPONSE").build())
            .build();

        DirectionsQuestionnaireForm result =
            respondentTemplateForDQGenerator.getRespondent2TemplateData(caseData, "TWO", BEARER_TOKEN);

        assertNotNull(result);
        assertNotNull(result.getExperts());
        assertEquals(NO, result.getExperts().getExpertRequired());
    }

    @Test
    void shouldDefaultExpertCostToZeroWhenEstimatedCostMissing() {
        Expert expertWithNullCost = Expert.builder()
            .firstName("John")
            .lastName("Doe")
            .estimatedCost(null)
            .build();

        Respondent1DQ dq = Respondent1DQ.builder()
            .respondent1DQExperts(Experts.builder()
                                       .expertRequired(YES)
                                       .details(List.of(element(expertWithNullCost)))
                                       .build())
            .build();

        List<uk.gov.hmcts.reform.civil.model.docmosis.dq.Expert> experts = respondentTemplateForDQGenerator.getExpertsDetails(dq);

        assertEquals("£0.00", experts.get(0).getFormattedCost());
    }
}
