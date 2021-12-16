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
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_DEFENDANT_LITIGATION_FRIEND;

@Service
@RequiredArgsConstructor
public class AddDefendantLitigationFriendCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(ADD_DEFENDANT_LITIGATION_FRIEND);

    private final Time time;
    private final ObjectMapper objectMapper;
    private final ExitSurveyContentService exitSurveyContentService;
    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(V_1, ABOUT_TO_START), this::prepareDefendantSolicitorOptions,
            callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit,
            callbackKey(V_1, ABOUT_TO_SUBMIT), this::aboutToSubmit_multiparty,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseDataUpdated = callbackParams.getCaseData().toBuilder()
            .businessProcess(BusinessProcess.ready(ADD_DEFENDANT_LITIGATION_FRIEND))
            .genericLitigationFriendDate(LocalDateTime.now())
            .genericLitigationFriendCreatedDate(
                ofNullable(callbackParams.getCaseData().getGenericLitigationFriendCreatedDate())
                    .orElse(LocalDateTime.now()))
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated.toMap(objectMapper))
            .build();
    }

    private CallbackResponse aboutToSubmit_multiparty(CallbackParams callbackParams) {
        if (!featureToggleService.isMultipartyEnabled()) {
            return aboutToSubmit(callbackParams);
        }

        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime currentDateTime = time.now();

        CaseData.CaseDataBuilder caseDataUpdated = caseData.toBuilder()
            .businessProcess(BusinessProcess.ready(ADD_DEFENDANT_LITIGATION_FRIEND));

        if (caseData.getSelectLitigationFriend() == null) {
            caseDataUpdated
                .respondent1LitigationFriendDate(currentDateTime)
                .respondent1LitigationFriendCreatedDate(
                    ofNullable(callbackParams.getCaseData().getRespondent1LitigationFriendCreatedDate())
                        .orElse(currentDateTime))
                .respondent1LitigationFriend(caseData.getGenericLitigationFriend())
                .genericLitigationFriend(null);
        } else if (caseData.getSelectLitigationFriend().getValue().getLabel() != "Both") {
            if (caseData.getSelectLitigationFriend().getValue().getLabel().contains("Respondent Two")) {
                caseDataUpdated
                    .respondent2LitigationFriendDate(currentDateTime)
                    .respondent2LitigationFriendCreatedDate(
                        ofNullable(callbackParams.getCaseData().getRespondent2LitigationFriendCreatedDate())
                            .orElse(currentDateTime))
                    .respondent2LitigationFriend(caseData.getGenericLitigationFriend())
                    .genericLitigationFriend(null);
            } else if (caseData.getSelectLitigationFriend().getValue().getLabel().contains("Respondent One")) {
                caseDataUpdated
                    .respondent1LitigationFriendDate(currentDateTime)
                    .respondent1LitigationFriendCreatedDate(
                        ofNullable(callbackParams.getCaseData().getRespondent1LitigationFriendCreatedDate())
                            .orElse(currentDateTime))
                    .respondent1LitigationFriend(caseData.getGenericLitigationFriend())
                    .genericLitigationFriend(null);
            }
        } else {
            caseDataUpdated
                .respondent1LitigationFriendDate(currentDateTime)
                .respondent1LitigationFriendCreatedDate(
                    ofNullable(callbackParams.getCaseData().getRespondent1LitigationFriendCreatedDate())
                        .orElse(currentDateTime))
                .respondent1LitigationFriend(caseData.getGenericLitigationFriend())
                .respondent2LitigationFriendDate(currentDateTime)
                .respondent2LitigationFriendCreatedDate(
                    ofNullable(callbackParams.getCaseData().getRespondent2LitigationFriendCreatedDate())
                        .orElse(currentDateTime))
                .respondent2LitigationFriend(caseData.getGenericLitigationFriend())
                .genericLitigationFriend(null);
        }

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

        List<String> dynamicListOptions = new ArrayList<>();
        dynamicListOptions.add("Both");
        dynamicListOptions.add("Respondent One: " + caseData.getApplicant1().getPartyName());

        if (nonNull(caseData.getRespondent2())) {
            dynamicListOptions.add("Respondent Two: " + caseData.getRespondent2().getPartyName());
        }

        isLitigationFriendForToBothParty(caseData);
        //build options for field (Default Value & List Options), add to case data
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.selectLitigationFriend(DynamicList.fromList(dynamicListOptions));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    protected boolean isLitigationFriendForToBothParty(CaseData caseData) {
        return Optional.ofNullable(caseData.getSelectLitigationFriend())
            .map(DynamicList::getValue)
            .map(DynamicListElement::getLabel)
            .orElse("")
            .equalsIgnoreCase("Both");
    }
}
