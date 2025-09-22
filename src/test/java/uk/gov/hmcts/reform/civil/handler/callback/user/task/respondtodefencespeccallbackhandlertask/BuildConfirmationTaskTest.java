package uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtodefencespeccallbackhandlertask;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGenerator;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationTextGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class BuildConfirmationTaskTest {

    @Mock
    private List<RespondToResponseConfirmationHeaderGenerator> confirmationHeaderGenerators;

    @Mock
    private List<RespondToResponseConfirmationTextGenerator> confirmationTextGenerators;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private BuildConfirmationTask buildConfirmationTask;

    public static final String DOWNLOAD_URL_CLAIM_DOCUMENTS = "/cases/case-details/%s#Claim documents";

    @Test
    void shouldGenerateConfirmationTextAndHeader() {
        CaseData caseData = CaseDataBuilder.builder()
            .setIntermediateTrackClaim()
            .build();

        SubmittedCallbackResponse response = buildConfirmationTask.execute(callbackParams(caseData), featureToggleService);

        String expectedConfirmationText = "<h2 class=\"govuk-heading-m\">What happens next</h2>"
            + "You've decided not to proceed and the case will end.<br>"
            + format("%n%n<a href=\"%s\" target=\"_blank\">View Directions questionnaire</a>",
                     format(BuildConfirmationTask.DOWNLOAD_URL_CLAIM_DOCUMENTS, caseData.getCcdCaseReference()));

        String expectedConfirmationHeader = format(
            "# You have decided not to proceed with the claim%n## Claim number: %s",
            caseData.getLegacyCaseReference());

        assertNotNull(response);
        assertEquals(expectedConfirmationText, response.getConfirmationBody());
        assertEquals(expectedConfirmationHeader, response.getConfirmationHeader());
    }

    @Test
    void shouldGenerateConfirmationTextAndHeaderWhenApplicantProceededWithClaim() {

        CaseData caseData = CaseDataBuilder.builder()
            .setIntermediateTrackClaim()
            .defenceAdmitPartPaymentTimeRouteRequired(IMMEDIATELY)
            .respondent1ClaimResponseTypeForSpec(FULL_ADMISSION)
            .applicant1ProceedWithClaim(YES)
            .build();

        SubmittedCallbackResponse response = buildConfirmationTask.execute(callbackParams(caseData), featureToggleService);

        String expectedConfirmationText = "<h2 class=\"govuk-heading-m\">What happens next</h2>"
            + "We'll review the case and contact you about what to do next.<br>"
            + format(
            "%n%n<a href=\"%s\" target=\"_blank\">View Directions questionnaire</a>",
            format(DOWNLOAD_URL_CLAIM_DOCUMENTS, caseData.getCcdCaseReference()));

        String expectedConfirmationHeader = format(
            "# You have decided to proceed with the claim%n## Claim number: %s",
            caseData.getLegacyCaseReference());

        assertNotNull(response);
        assertEquals(expectedConfirmationText, response.getConfirmationBody());
        assertEquals(expectedConfirmationHeader, response.getConfirmationHeader());
    }

    @Test
    void shouldGenerateConfirmationTextAndHeaderWhenAllFinalOrderIssued() {
        CaseData caseData = CaseDataBuilder.builder()
            .buildJudmentOnlineCaseDataWithPaymentByInstalment().toBuilder()
            .applicant1ProceedWithClaim(YesOrNo.NO)
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE)
            .build();

        SubmittedCallbackResponse response = buildConfirmationTask.execute(callbackParams(caseData), featureToggleService);

        String expectedConfirmationText = format(
            "<br />%n%n<a href=\"%s\" target=\"_blank\">Download county court judgment</a>"
                + "<br><br>The defendant will be served the county court judgment<br><br>",
            format(DOWNLOAD_URL_CLAIM_DOCUMENTS, caseData.getCcdCaseReference()));

        String expectedConfirmationHeader =  format(
            "# Judgment Submitted %n## A county court judgment(ccj) has been submitted for case %s",
            caseData.getLegacyCaseReference());

        assertNotNull(response);
        assertEquals(expectedConfirmationText, response.getConfirmationBody());
        assertEquals(expectedConfirmationHeader, response.getConfirmationHeader());
    }

    @Test
    void shouldGenerateConfirmationTextHeaderWhenClaimantAgreedToFreeMediation() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateMediationSuccessful(MultiPartyScenario.ONE_V_ONE)
            .build();

        SubmittedCallbackResponse response = buildConfirmationTask.execute(callbackParams(caseData), featureToggleService);

        String expectedConfirmationHeader =  format(
            "# You have rejected their response %n## Your Claim Number : %s",
            caseData.getLegacyCaseReference());

        assertNotNull(response);
        assertEquals(expectedConfirmationHeader, response.getConfirmationHeader());
    }

    private CallbackParams callbackParams(CaseData caseData) {

        return CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, BEARER_TOKEN))
            .build();
    }
}
