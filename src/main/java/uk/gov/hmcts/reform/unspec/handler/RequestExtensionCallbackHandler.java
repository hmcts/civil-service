package uk.gov.hmcts.reform.unspec.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.Callback;
import uk.gov.hmcts.reform.unspec.callback.CallbackHandler;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CallbackType;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.enums.YesOrNo;
import uk.gov.hmcts.reform.unspec.validation.RequestExtensionValidator;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.REQUEST_EXTENSION;
import static uk.gov.hmcts.reform.unspec.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDate;

@Service
public class RequestExtensionCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(REQUEST_EXTENSION);
    public static final String ALREADY_AGREED = "You told us you've already agreed this with the claimant's legal "
        + "representative. We'll contact them and email you to confirm the deadline.</p>";
    public static final String NOT_AGREED = "We'll email you to tell you if the claimant's legal representative "
        + "accepts or rejects your request.</p>";

    public static final String PROPOSED_DEADLINE = "respondentSolicitor1claimResponseExtensionProposedDeadline";
    public static final String RESPONSE_DEADLINE = "responseDeadline";
    public static final String EXTENSION_ALREADY_AGREED = "respondentSolicitor1claimResponseExtensionAlreadyAgreed";

    private final ObjectMapper mapper;
    private final RequestExtensionValidator validator;

    public RequestExtensionCallbackHandler(ObjectMapper mapper, RequestExtensionValidator validator) {
        this.mapper = mapper;
        this.validator = validator;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return Map.of(
            CallbackType.ABOUT_TO_START, this::aboutToStart,
            CallbackType.MID, this::validateRequestedDeadline,
            CallbackType.SUBMITTED, this::buildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse aboutToStart(CallbackParams callbackParams) {
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(validator.validateAlreadyRequested(caseDetails))
            .build();
    }

    private CallbackResponse validateRequestedDeadline(CallbackParams callbackParams) {

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(validator.validateProposedDeadline(callbackParams.getRequest().getCaseDetails()))
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        Map<String, Object> data = callbackParams.getRequest().getCaseDetails().getData();
        LocalDate proposedDeadline = mapper.convertValue(
            data.get(PROPOSED_DEADLINE),
            LocalDate.class
        );
        YesOrNo extensionAlreadyAgreed = mapper.convertValue(data.get(EXTENSION_ALREADY_AGREED), YesOrNo.class);
        String claimNumber = "TBC";

        LocalDate responseDeadline = mapper.convertValue(
            data.get(RESPONSE_DEADLINE),
            LocalDate.class
        );
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
