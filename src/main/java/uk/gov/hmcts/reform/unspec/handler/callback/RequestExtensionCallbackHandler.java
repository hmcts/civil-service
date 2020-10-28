package uk.gov.hmcts.reform.unspec.handler.callback;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.Callback;
import uk.gov.hmcts.reform.unspec.callback.CallbackHandler;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.enums.YesOrNo;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.service.BusinessProcessService;
import uk.gov.hmcts.reform.unspec.validation.RequestExtensionValidator;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.REQUEST_EXTENSION;
import static uk.gov.hmcts.reform.unspec.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.unspec.service.DeadlinesCalculator.MID_NIGHT;

@Service
@RequiredArgsConstructor
public class RequestExtensionCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(REQUEST_EXTENSION);
    public static final String ALREADY_AGREED = "You told us you've already agreed this with the claimant's legal "
        + "representative. We'll contact them and email you to confirm the deadline.</p>";
    public static final String NOT_AGREED = "We'll email you to tell you if the claimant's legal representative "
        + "accepts or rejects your request.</p>";

    public static final String PROPOSED_DEADLINE = "respondentSolicitor1claimResponseExtensionProposedDeadline";
    public static final String RESPONSE_DEADLINE = "respondentSolicitor1ResponseDeadline";
    public static final String LEGACY_CASE_REFERENCE = "legacyCaseReference";

    private final RequestExtensionValidator validator;
    private final BusinessProcessService businessProcessService;
    private final CaseDetailsConverter caseDetailsConverter;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(MID, "propose-deadline"), this::validateRequestedDeadline,
            callbackKey(ABOUT_TO_SUBMIT), this::updateResponseDeadline,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    private CallbackResponse updateResponseDeadline(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDate proposedDeadline = caseData.getRespondentSolicitor1claimResponseExtensionProposedDeadline();
        YesOrNo extensionAlreadyAgreed = caseData.getRespondentSolicitor1claimResponseExtensionAlreadyAgreed();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        if (extensionAlreadyAgreed == YES) {
            caseDataBuilder.respondentSolicitor1ResponseDeadline(proposedDeadline.atTime(MID_NIGHT));
        }
        CaseData caseDataUpdated = businessProcessService.updateBusinessProcess(
            caseDataBuilder.build(),
            REQUEST_EXTENSION
        );

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDetailsConverter.toMap(caseDataUpdated))
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse validateRequestedDeadline(CallbackParams callbackParams) {

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(validator.validateProposedDeadline(callbackParams.getRequest().getCaseDetails()))
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDate proposedDeadline = caseData.getRespondentSolicitor1claimResponseExtensionProposedDeadline();
        YesOrNo extensionAlreadyAgreed = caseData.getRespondentSolicitor1claimResponseExtensionAlreadyAgreed();
        String claimNumber = caseData.getLegacyCaseReference();
        LocalDate responseDeadline = caseData.getRespondentSolicitor1ResponseDeadline().toLocalDate();

        String body = format(
            "<br /><p>You asked if you can respond before 4pm on %s %s"
                + "<p>They can choose not to respond to your request, so if you don't get an email from us, "
                + "assume you need to respond before 4pm on %s.</p>",
            formatLocalDate(proposedDeadline, DATE),
            extensionAlreadyAgreed == YES ? ALREADY_AGREED : NOT_AGREED,
            formatLocalDate(responseDeadline, DATE)
        );

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# You asked for extra time to respond%n## Claim number: %s", claimNumber))
            .confirmationBody(body)
            .build();
    }
}
