package uk.gov.hmcts.reform.civil.handler.callback.user.createclaim;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ClaimIssueConfiguration;
import uk.gov.hmcts.reform.civil.handler.callback.user.CreateClaimCallbackHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.CreateClaimSpecCallbackHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;

/**
 * getSpecBody is because CreateClaimSpecCallbackHandler.CONFIRMATION_SUMMARY is different from
 * CreateClaimCallbackHandler's, and the same with LIP_CONFIRMATION_BODY. It's possible that
 * both should be the same, though, which would make this class simpler.
 *
 * <p>A similar thing happens with the choice between respondent represented and registered. When both
 * spec and unspec support MP, their probably going to use the same condition
 */
@Component
@RequiredArgsConstructor
public class CreateClaimConfirmationBuilder {

    private final ClaimIssueConfiguration claimIssueConfiguration;
    private final ExitSurveyContentService exitSurveyContentService;

    public SubmittedCallbackResponse buildUnspecConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(getUnspecHeader(caseData))
            .confirmationBody(getUnspecBody(
                caseData
            ))
            .build();
    }

    public SubmittedCallbackResponse buildSpecConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if ("CREATE_CLAIM_SPEC".equals(callbackParams.getRequest().getEventId())) {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(getSpecHeader(caseData))
                .confirmationBody(getSpecBody(
                    caseData,
                    CreateClaimSpecCallbackHandler.SPEC_CONFIRMATION_SUMMARY,
                    CreateClaimSpecCallbackHandler.SPEC_LIP_CONFIRMATION_BODY
                ))
                .build();
        } else {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(getSpecHeader(caseData))
                .confirmationBody(getSpecBody(
                    caseData,
                    CreateClaimSpecCallbackHandler.CONFIRMATION_SUMMARY,
                    CreateClaimSpecCallbackHandler.LIP_CONFIRMATION_BODY
                ))
                .build();
        }
    }

    /**
     * Returns confirmation's body for Spec.
     *
     * @param caseData                            the case's data.
     * @param respondent1RepresentedAndRegistered template to use when the respondent1 is represented and their
     *                                            representative is registered.
     * @param otherwise                           template to use otherwise.
     * @return confirmation body.
     */
    private String getSpecBody(CaseData caseData,
                               String respondent1RepresentedAndRegistered,
                               String otherwise) {
        LocalDateTime serviceDeadline = LocalDate.now().plusDays(112).atTime(23, 59);
        String formattedServiceDeadline = formatLocalDateTime(serviceDeadline, DATE_TIME_AT);

        return format(
            isRespondent1RepresentedAndRegistered(caseData)
                ? respondent1RepresentedAndRegistered
                : otherwise,
            format("/cases/case-details/%s#CaseDocuments", caseData.getCcdCaseReference()),
            claimIssueConfiguration.getResponsePackLink(),
            formattedServiceDeadline
        ) + exitSurveyContentService.applicantSurvey();
    }

    private boolean isRespondent1RepresentedAndRegistered(CaseData caseData) {
        return caseData.getRespondent1Represented() != NO && caseData.getRespondent1OrgRegistered() != NO;
    }

    /**
     * Returns confirmation's body for Spec.
     *
     * @param caseData the case's data.
     * @return confirmation body.
     */
    private String getUnspecBody(CaseData caseData) {
        LocalDateTime serviceDeadline = LocalDate.now().plusDays(112).atTime(23, 59);
        String formattedServiceDeadline = formatLocalDateTime(serviceDeadline, DATE_TIME_AT);

        return String.format(
            areRespondentsRepresentedAndRegistered(caseData)
                ? CreateClaimCallbackHandler.CONFIRMATION_SUMMARY
                : CreateClaimCallbackHandler.LIP_CONFIRMATION_BODY,
            format("/cases/case-details/%s#CaseDocuments", caseData.getCcdCaseReference()),
            claimIssueConfiguration.getResponsePackLink(),
            formattedServiceDeadline
        ) + exitSurveyContentService.applicantSurvey();
    }

    private String getSpecHeader(CaseData caseData) {
        if (isRespondent1RepresentedAndRegistered(caseData)) {
            return format("# Your claim has been received%n## Claim number: %s", caseData.getLegacyCaseReference());
        } else {
            return format(
                "# Your claim has been received and will progress offline%n## Claim number: %s",
                caseData.getLegacyCaseReference()
            );
        }
    }

    private String getUnspecHeader(CaseData caseData) {
        if (areRespondentsRepresentedAndRegistered(caseData)) {
            return format("# Your claim has been received%n## Claim number: %s", caseData.getLegacyCaseReference());
        }

        return format(
            "# Your claim has been received and will progress offline%n## Claim number: %s",
            caseData.getLegacyCaseReference()
        );
    }

    private boolean areRespondentsRepresentedAndRegistered(CaseData caseData) {
        return !(caseData.getRespondent1Represented() == NO
            || caseData.getRespondent1OrgRegistered() == NO
            || caseData.getRespondent2Represented() == NO
            || caseData.getRespondent2OrgRegistered() == NO);
    }
}
