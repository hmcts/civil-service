package uk.gov.hmcts.reform.unspec.handler.callback;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.Callback;
import uk.gov.hmcts.reform.unspec.callback.CallbackHandler;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CallbackType;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.enums.ClaimType;
import uk.gov.hmcts.reform.unspec.model.ClaimValue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.CREATE_CASE;
import static uk.gov.hmcts.reform.unspec.enums.AllocatedTrack.getAllocatedTrack;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.unspec.helpers.DateFormatHelper.formatLocalDateTime;

@Service
public class CreateClaimCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CREATE_CASE);

    private final ObjectMapper mapper;
    private final String responsePackLink;

    public CreateClaimCallbackHandler(
        ObjectMapper mapper,
        @Value("${unspecified.response-pack-url}") String responsePackLink
    ) {
        this.mapper = mapper;
        this.responsePackLink = responsePackLink;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return Map.of(
            CallbackType.MID, this::validateClaimValues,
            CallbackType.ABOUT_TO_SUBMIT, this::addIssuedDate,
            CallbackType.SUBMITTED, this::buildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse validateClaimValues(CallbackParams callbackParams) {
        Map<String, Object> data = callbackParams.getRequest().getCaseDetails().getData();
        ClaimValue claimValue = mapper.convertValue(data.get("claimValue"), ClaimValue.class);
        List<String> errors = new ArrayList<>();

        if (claimValue.hasLargerLowerValue()) {
            errors.add("CONTENT TBC: Higher value must not be lower than the lower value.");
        }

        if (errors.isEmpty()) {
            ClaimType claimType = mapper.convertValue(data.get("claimType"), ClaimType.class);

            data.put("allocatedTrack", getAllocatedTrack(claimValue, claimType));
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
                   .data(data)
                   .errors(errors)
                   .build();
    }

    private AboutToStartOrSubmitCallbackResponse addIssuedDate(CallbackParams callbackParams) {
        Map<String, Object> data = callbackParams.getRequest().getCaseDetails().getData();
        data.put("claimIssuedDate", LocalDate.now());

        return AboutToStartOrSubmitCallbackResponse.builder()
                   .data(data)
                   .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        String documentLink = "https://www.google.com";
        LocalDateTime serviceDeadline = LocalDate.now().plusDays(112).atTime(23, 59);
        String formattedServiceDeadline = formatLocalDateTime(serviceDeadline, DATE_TIME_AT);
        String claimNumber = "TBC";

        String body = format(
            "<br />Follow these steps to serve a claim:"
                + "\n* [Download the sealed claim form](%s) (PDF, 123KB)"
                + "\n* Send the form, particulars of claim and "
                + "<a href=\"%s\" target=\"_blank\">a response pack</a> (PDF, 266 KB) to the defendant by %s"
                + "\n* Confirm service online within 21 days of sending the form, particulars and response pack, before"
                + " 4pm if you're doing this on the due day", documentLink, responsePackLink, formattedServiceDeadline);

        return SubmittedCallbackResponse.builder()
                   .confirmationHeader(format("# Your claim has been issued\n## Claim number: %s", claimNumber))
                   .confirmationBody(body)
                   .build();
    }
}
