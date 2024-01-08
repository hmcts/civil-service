package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.ReasonForReconsideration;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.REQUEST_FOR_RECONSIDERATION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorOne;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isApplicantSolicitor;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorTwo;

@Service
@RequiredArgsConstructor
public class RequestForReconsiderationCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(REQUEST_FOR_RECONSIDERATION);
    protected final ObjectMapper objectMapper;
    private final UserService userService;
    private final CoreCaseUserService coreCaseUserService;
    private static final String CONFIRMATION_HEADER = "# Your request has been submitted";
    private static final String CONFIRMATION_BODY = "### What happens next \n" +
        "You should receive an update on your request for determination after 10 days, please monitor" +
        " your notifications/dashboard for an update.";

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::getPartyDetails)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::saveRequestForReconsiderationReason)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    private CallbackResponse getPartyDetails(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();
        List<String> roles = getUserRole(callbackParams);
        if (isApplicantSolicitor(roles)) {
            updatedData.casePartyRequestForReconsideration("Applicant");
        }
        else if (isRespondentSolicitorOne(roles)) {
            updatedData.casePartyRequestForReconsideration("Respondent1");
        }
        else if (isRespondentSolicitorTwo(roles)) {
            updatedData.casePartyRequestForReconsideration("Respondent2");
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }

    private boolean applicant2Present(CaseData caseData) {
        return caseData.getAddApplicant2() != null && caseData.getAddApplicant2() == YES;
    }

    private boolean respondent2Present(CaseData caseData) {
        return caseData.getAddRespondent2() != null
            && caseData.getAddRespondent2() == YES;
    }

    private boolean respondent2HasSameLegalRep(CaseData caseData) {
        return caseData.getRespondent2SameLegalRepresentative() != null
            && caseData.getRespondent2SameLegalRepresentative() == YES;
    }
    private CallbackResponse saveRequestForReconsiderationReason(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> updatedData = caseData.toBuilder();
        List<String> roles = getUserRole(callbackParams);
        StringBuilder partyName = new StringBuilder();
        if (isApplicantSolicitor(roles)) {
            ReasonForReconsideration reasonForReconsideration = caseData.getReasonForReconsiderationApplicant();
            partyName.append("Applicant - ");
            partyName.append(caseData.getApplicant1().getPartyName());
            partyName.append(applicant2Present(caseData) ?
            " and " + caseData.getApplicant2().getPartyName() : "");
            reasonForReconsideration.setRequestor(partyName.toString());
            updatedData.reasonForReconsiderationApplicant(reasonForReconsideration);
        }
        else if (isRespondentSolicitorOne(roles)) {
            ReasonForReconsideration reasonForReconsideration = caseData.getReasonForReconsiderationRespondent1();
            partyName.append("Defendant - ");
            partyName.append(caseData.getRespondent1().getPartyName());
            partyName.append(respondent2Present(caseData) && respondent2HasSameLegalRep(caseData) ?
                        " and " + caseData.getRespondent2().getPartyName() : "");

            reasonForReconsideration.setRequestor(partyName.toString());
            updatedData.reasonForReconsiderationRespondent1(reasonForReconsideration);
        }
        else if (isRespondentSolicitorTwo(roles)) {
            partyName.append("Defendant - ");
            ReasonForReconsideration reasonForReconsideration = caseData.getReasonForReconsiderationRespondent2();
            partyName.append(respondent2Present(caseData) ? caseData.getRespondent2().getPartyName() : "");
            reasonForReconsideration.setRequestor(partyName.toString());
            updatedData.reasonForReconsiderationRespondent2(reasonForReconsideration);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();
    }

    private List<String> getUserRole(CallbackParams callbackParams) {
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        return coreCaseUserService.getUserCaseRoles(
            callbackParams.getCaseData().getCcdCaseReference().toString(),
            userInfo.getUid()
        );
    }

    private CallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(CONFIRMATION_HEADER)
            .confirmationBody(CONFIRMATION_BODY)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
