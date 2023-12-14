package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.validation.ValidateEmailService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.AMEND_PARTY_DETAILS;

@Service
@RequiredArgsConstructor
public class AmendPartyDetailsCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(AMEND_PARTY_DETAILS);

    private final ValidateEmailService validateEmailService;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(ABOUT_TO_SUBMIT), this::validateUpdatedDetails,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse validateUpdatedDetails(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        List<String> errors = new ArrayList<>();
        errors.addAll(validateEmailService.validate(caseData.getApplicantSolicitor1UserDetails().getEmail()));
        errors.addAll(validateEmailService.validate(caseData.getRespondentSolicitor1EmailAddress()));

        // set organisation policy after removing it in claim issue
        // workaround for hiding cases in CAA list before case notify
        setOrganisationPolicy(caseData, caseDataBuilder);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .errors(!errors.isEmpty() ? errors : null)
            .build();
    }

    private void setOrganisationPolicy(CaseData caseData, CaseData.CaseDataBuilder caseDataBuilder) {
        if (caseData.getRespondent1OrganisationIDCopy() != null) {
            caseDataBuilder.respondent1OrganisationPolicy(
                caseData.getRespondent1OrganisationPolicy().toBuilder()
                    .organisation(Organisation.builder()
                                      .organisationID(caseData.getRespondent1OrganisationIDCopy())
                                      .build())
                    .build()
            );
        }

        if (caseData.getRespondent2OrganisationIDCopy() != null) {
            caseDataBuilder.respondent2OrganisationPolicy(
                caseData.getRespondent2OrganisationPolicy().toBuilder()
                    .organisation(Organisation.builder()
                                      .organisationID(caseData.getRespondent2OrganisationIDCopy())
                                      .build())
                    .build()
            );
        }
    }

    private CallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# You have updated a legal representative's information")
            .confirmationBody("<br />")
            .build();
    }
}
