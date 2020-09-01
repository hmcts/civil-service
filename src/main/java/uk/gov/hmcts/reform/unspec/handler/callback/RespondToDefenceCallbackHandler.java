package uk.gov.hmcts.reform.unspec.handler.callback;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.Callback;
import uk.gov.hmcts.reform.unspec.callback.CallbackHandler;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.callback.CallbackType;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.enums.YesOrNo;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.CLAIMANT_RESPONSE;

@Service
@RequiredArgsConstructor
public class RespondToDefenceCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(CLAIMANT_RESPONSE);
    public static final String APPLICANT_1_PROCEEDING = "applicant1ProceedWithClaim";

    private final ObjectMapper mapper;

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return Map.of(
            CallbackType.SUBMITTED, this::buildConfirmation
        );
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        Map<String, Object> data = callbackParams.getRequest().getCaseDetails().getData();
        YesOrNo proceeding = mapper.convertValue(data.get(APPLICANT_1_PROCEEDING), YesOrNo.class);

        String claimNumber = "TBC";
        String dqLink = "http://www.google.com";

        String body = getBody(proceeding);
        String title = getTitle(proceeding);

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format(title, claimNumber))
            .confirmationBody(format(body, dqLink))
            .build();
    }

    private String getTitle(YesOrNo proceeding) {
        if (proceeding == YesOrNo.YES) {
            return "# You've decided to proceed with the claim%n## Claim number: %s";
        }
        return "# You've decided not to proceed with the claim%n## Claim number: %s";
    }

    private String getBody(YesOrNo proceeding) {
        if (proceeding == YesOrNo.YES) {
            return "<br />We'll review the case. We'll contact you to tell you what to do next.%n%n"
                    + "[Download directions questionnaire](%s)";
        }
        return "CONTENT TBC";
    }
}
