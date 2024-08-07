package uk.gov.hmcts.reform.civil.handler.callback.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.CaseDataToTextGenerator;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationHeaderSpecGenerator;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.RespondToClaimConfirmationTextSpecGenerator;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDateTime;
import java.util.List;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;

@Component
@RequiredArgsConstructor
public class RespondToClaimSpecConfirmationBuilder {

    private final List<RespondToClaimConfirmationTextSpecGenerator> confirmationTextSpecGenerators;
    private final List<RespondToClaimConfirmationHeaderSpecGenerator> confirmationHeaderGenerators;

    SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        String claimNumber = caseData.getLegacyCaseReference();

        String body = generateConfirmationBody(caseData);
        String header = generateConfirmationHeader(caseData, claimNumber);

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(header)
            .confirmationBody(body)
            .build();
    }

    private String generateConfirmationBody(CaseData caseData) {
        return CaseDataToTextGenerator.getTextFor(
            confirmationTextSpecGenerators.stream(),
            () -> getDefaultConfirmationBody(caseData),
            caseData
        );
    }

    private String generateConfirmationHeader(CaseData caseData, String claimNumber) {
        return CaseDataToTextGenerator.getTextFor(
            confirmationHeaderGenerators.stream(),
            () -> format("# You have submitted your response%n## Claim number: %s", claimNumber),
            caseData
        );
    }

    private String getDefaultConfirmationBody(CaseData caseData) {
        LocalDateTime responseDeadline = caseData.getApplicant1ResponseDeadline();
        String nextStepsMessage = getNextStepsMessage(responseDeadline);
        return format(
            "<h2 class=\"govuk-heading-m\">What happens next</h2>%n%n%s%n%n<a href=\"%s\" target=\"_blank\">Download questionnaire (opens in a new tab)</a>",
            nextStepsMessage,
            format("/cases/case-details/%s#Claim documents", caseData.getCcdCaseReference())
        );
    }

    private String getNextStepsMessage(LocalDateTime responseDeadline) {
        if (responseDeadline == null) {
            return "After the other solicitor has responded and/or the time for responding has passed the claimant will be notified.";
        } else {
            return format("The claimant has until 4pm on %s to respond to your claim. We will let you know when they respond.",
                          formatLocalDateTime(responseDeadline, DATE));
        }
    }
}
