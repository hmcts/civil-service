package uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtodefencespeccallbackhandlertask;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.CaseDataToTextGenerator;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationHeaderGenerator;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToResponseConfirmationTextGenerator;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;

@Component
@RequiredArgsConstructor
@Slf4j
public class BuildConfirmationTask implements CaseTask {

    public static final String DOWNLOAD_URL_CLAIM_DOCUMENTS = "/cases/case-details/%s#Claim documents";
    private final List<RespondToResponseConfirmationHeaderGenerator> confirmationHeaderGenerators;
    private final List<RespondToResponseConfirmationTextGenerator> confirmationTextGenerators;

    public SubmittedCallbackResponse execute(CallbackParams callbackParams, FeatureToggleService featureToggleService) {

        CaseData caseData = callbackParams.getCaseData();
        log.info("Executing SubmittedCallbackResponse for Case : {} ", caseData.getCcdCaseReference());

        if (!AllocatedTrack.MULTI_CLAIM.equals(caseData.getAllocatedTrack())) {
            caseData.toBuilder().ccdState(CaseState.JUDICIAL_REFERRAL).build();
        } else if (isDefendantFullAdmitPayImmediately(caseData)) {
            caseData.toBuilder().ccdState(CaseState.PROCEEDS_IN_HERITAGE_SYSTEM).build();
        }

        SubmittedCallbackResponse.SubmittedCallbackResponseBuilder responseBuilder =
            SubmittedCallbackResponse.builder();

        log.debug("Setting confirmation Body & Header for Case : {} ", caseData.getCcdCaseReference());
        responseBuilder.confirmationBody(
                CaseDataToTextGenerator.getTextFor(
                    confirmationTextGenerators.stream(),
                    () -> getDefaultConfirmationText(caseData),
                    caseData,
                    featureToggleService
                ))
            .confirmationHeader(
                CaseDataToTextGenerator.getTextFor(
                    confirmationHeaderGenerators.stream(),
                    () -> getDefaultConfirmationHeader(caseData),
                    caseData,
                    featureToggleService
                ));

        return responseBuilder.build();
    }

    private boolean isDefendantFullAdmitPayImmediately(CaseData caseData) {
        return caseData.getDefenceAdmitPartPaymentTimeRouteRequired() != null
            && caseData.getDefenceAdmitPartPaymentTimeRouteRequired() == IMMEDIATELY
            && (FULL_ADMISSION.equals(caseData.getRespondent1ClaimResponseTypeForSpec()));
    }

    private String getDefaultConfirmationText(CaseData caseData) {
        if (caseData.hasApplicantProceededWithClaim()) {
            return "<h2 class=\"govuk-heading-m\">What happens next</h2>"
                + "We'll review the case and contact you about what to do next.<br>"
                + format(
                "%n%n<a href=\"%s\" target=\"_blank\">View Directions questionnaire</a>",
                format(DOWNLOAD_URL_CLAIM_DOCUMENTS, caseData.getCcdCaseReference())
            );
        }  else if (CaseState.All_FINAL_ORDERS_ISSUED == caseData.getCcdState()
            && (caseData.isPayBySetDate() || caseData.isPayByInstallment())) {
            return format(
                "<br />%n%n<a href=\"%s\" target=\"_blank\">Download county court judgment</a>"
                    + "<br><br>The defendant will be served the county court judgment<br><br>",
                format(DOWNLOAD_URL_CLAIM_DOCUMENTS, caseData.getCcdCaseReference())
            );
        } else {
            return "<h2 class=\"govuk-heading-m\">What happens next</h2>"
                + "You've decided not to proceed and the case will end.<br>"
                + format(
                "%n%n<a href=\"%s\" target=\"_blank\">View Directions questionnaire</a>",
                format(DOWNLOAD_URL_CLAIM_DOCUMENTS, caseData.getCcdCaseReference())
            );
        }
    }

    private String getDefaultConfirmationHeader(CaseData caseData) {
        String claimNumber = caseData.getLegacyCaseReference();
        if (caseData.hasApplicantProceededWithClaim() && !caseData.hasClaimantAgreedToFreeMediation()) {
            return format(
                "# You have decided to proceed with the claim%n## Claim number: %s",
                claimNumber
            );
        } else if (caseData.hasClaimantAgreedToFreeMediation()) {
            return format(
                "# You have rejected their response %n## Your Claim Number : %s",
                caseData.getLegacyCaseReference()
            );
        } else if (CaseState.All_FINAL_ORDERS_ISSUED == caseData.getCcdState()
            && (caseData.isPayBySetDate() || caseData.isPayByInstallment())) {
            return format(
                "# Judgment Submitted %n## A county court judgment(ccj) has been submitted for case %s",
                claimNumber
            );
        } else {
            return format(
                "# You have decided not to proceed with the claim%n## Claim number: %s",
                claimNumber
            );
        }
    }
}
