package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.flowstate.StateFlowEngine;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_DEFENDANT_LITIGATION_FRIEND;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

@Service
@RequiredArgsConstructor
public class AddDefendantLitigationFriendCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(ADD_DEFENDANT_LITIGATION_FRIEND);

    private final Time time;
    private final ObjectMapper objectMapper;
    private final ExitSurveyContentService exitSurveyContentService;
    private final UserService userService;
    private final StateFlowEngine stateFlowEngine;
    private final CoreCaseUserService coreCaseUserService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::prepareDefendantSolicitorOptions,
            callbackKey(MID, "get-litigation-friend"), this::getLitigationFriend,
            callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    private CallbackResponse getLitigationFriend(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataUpdated = caseData.toBuilder();
        String selectedOption = Optional.of(caseData).map(CaseData::getSelectLitigationFriend)
            .map(DynamicList::getValue).map(DynamicListElement::getLabel).orElse("").split(":")[0]
            .toUpperCase();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated.litigantFriendSelection(selectedOption).build().toMap(objectMapper))
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime currentDateTime = time.now();

        CaseData.CaseDataBuilder caseDataUpdated = caseData.toBuilder()
            .businessProcess(BusinessProcess.ready(ADD_DEFENDANT_LITIGATION_FRIEND));

        boolean diffSolicitors = caseData.getRespondent2SameLegalRepresentative() == null
            || caseData.getRespondent2SameLegalRepresentative() == NO;

        String selectedOption = diffSolicitors
            ? solicitorRepresentsOnlyRespondent2(callbackParams) ? "Defendant Two" : "Defendant One"
            : Optional.of(caseData).map(CaseData::getSelectLitigationFriend)
            .map(DynamicList::getValue).map(DynamicListElement::getLabel).orElse("").split(":")[0];

        if ("Both".equals(selectedOption)) {
            caseDataUpdated
                .respondent1LitigationFriend(caseData.getGenericLitigationFriend())
                .respondent1LitigationFriendDate(currentDateTime)
                .respondent1LitigationFriendCreatedDate(
                    ofNullable(callbackParams.getCaseData().getRespondent1LitigationFriendCreatedDate())
                        .orElse(currentDateTime))
                .respondent2LitigationFriend(caseData.getGenericLitigationFriend())
                .respondent2LitigationFriendDate(currentDateTime)
                .respondent2LitigationFriendCreatedDate(
                    ofNullable(callbackParams.getCaseData().getRespondent2LitigationFriendCreatedDate())
                        .orElse(currentDateTime));

        } else if ("Defendant Two".equals(selectedOption)) {
            caseDataUpdated
                .respondent2LitigationFriendDate(currentDateTime)
                .respondent2LitigationFriendCreatedDate(
                    ofNullable(callbackParams.getCaseData().getRespondent2LitigationFriendCreatedDate())
                        .orElse(currentDateTime));

        } else if ("Defendant One".equals(selectedOption)) {
            caseDataUpdated
                .respondent1LitigationFriendDate(currentDateTime)
                .respondent1LitigationFriendCreatedDate(
                    ofNullable(callbackParams.getCaseData().getRespondent1LitigationFriendCreatedDate())
                        .orElse(currentDateTime));
        }

        caseDataUpdated.isRespondent1(null);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated.build().toMap(objectMapper))
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# You have added litigation friend details")
            .confirmationBody(exitSurveyContentService.respondentSurvey())
            .build();
    }

    private CallbackResponse prepareDefendantSolicitorOptions(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        if (caseData.getRespondent2SameLegalRepresentative() == YES) {
            caseDataBuilder.selectLitigationFriend(DynamicList.fromList(buildDefendantOptions(caseData)));
        } else {
            caseDataBuilder.isRespondent1(solicitorRepresentsOnlyRespondent2(callbackParams) ? NO : YES);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private List<String> buildDefendantOptions(final CaseData caseData) {
        List<String> dynamicListOptions = new ArrayList<>();
        dynamicListOptions.add("Both");
        dynamicListOptions.add("Defendant One: " + caseData.getRespondent1().getPartyName());

        if (nonNull(caseData.getRespondent2())) {
            dynamicListOptions.add("Defendant Two: " + caseData.getRespondent2().getPartyName());
        }

        return dynamicListOptions;
    }

    private boolean solicitorRepresentsOnlyRespondent2(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());

        return stateFlowEngine.evaluate(caseData).isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)
            && coreCaseUserService.userHasCaseRole(
            caseData.getCcdCaseReference().toString(),
            userInfo.getUid(),
            RESPONDENTSOLICITORTWO
        );
    }
}
