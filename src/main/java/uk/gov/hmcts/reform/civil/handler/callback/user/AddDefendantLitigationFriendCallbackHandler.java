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
import static uk.gov.hmcts.reform.civil.callback.CallbackType.*;
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
            //callbackKey(MID, "select-litigant-friend"), this::selectLitigantFriend,
            callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit,
            callbackKey(V_1, ABOUT_TO_SUBMIT), this::aboutToSubmitMultiparty,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    //production uses this callback until CCD change is merged
    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseDataUpdated = callbackParams.getCaseData().toBuilder()
            .businessProcess(BusinessProcess.ready(ADD_DEFENDANT_LITIGATION_FRIEND))
            .respondent1LitigationFriendDate(LocalDateTime.now())
            .respondent1LitigationFriendCreatedDate(
                ofNullable(callbackParams.getCaseData().getRespondent1LitigationFriendCreatedDate())
                    .orElse(LocalDateTime.now()))
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated.toMap(objectMapper))
            .build();
    }

    /**
    private CallbackResponse selectLitigantFriend(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toBuilder()
                      .selectedLitigationFriend(caseData.getSelectLitigationFriend().getValue().getLabel()
                                                    .split(":")[0])
                      .build().toMap(objectMapper))
            .build();
    }
     if (caseData.getSelectLitigationFriend().getValue().getLabel().equals("Respondent Two")) {
     caseDataUpdated
     .respondent2LitigationFriend(caseData.getGenericLitigationFriend())
     .respondent2LitigationFriendDate(currentDateTime)
     .respondent2LitigationFriendCreatedDate(
     ofNullable(callbackParams.getCaseData().getRespondent2LitigationFriendCreatedDate())
     .orElse(currentDateTime))
     .genericLitigationFriend(null);
     } else if (caseData.getSelectLitigationFriend().getValue().getLabel().equals("Respondent One")) {
     caseDataUpdated
     .respondent1LitigationFriend(caseData.getGenericLitigationFriend())
     .respondent1LitigationFriendDate(currentDateTime)
     .respondent1LitigationFriendCreatedDate(
     ofNullable(callbackParams.getCaseData().getRespondent1LitigationFriendCreatedDate())
     .orElse(currentDateTime))
     .genericLitigationFriend(null);
     }
     **/

    private CallbackResponse aboutToSubmitMultiparty(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        LocalDateTime currentDateTime = time.now();

        //catches production
        if (!featureToggleService.isMultipartyEnabled()) {
            return aboutToSubmit(callbackParams);
            //when CCD changes are merged
        } else {
            CaseData.CaseDataBuilder caseDataUpdated = caseData.toBuilder()
                .businessProcess(BusinessProcess.ready(ADD_DEFENDANT_LITIGATION_FRIEND));

            if ("Both".equals(caseData.getSelectLitigationFriend().getValue().getLabel())) {
                caseDataUpdated
                    .respondent1LitigationFriendDate(currentDateTime)
                    .respondent1LitigationFriendCreatedDate(
                        ofNullable(callbackParams.getCaseData().getRespondent2LitigationFriendCreatedDate())
                            .orElse(currentDateTime))
                    .respondent2LitigationFriend(caseData.getRespondent1LitigationFriend())
                    .respondent2LitigationFriendDate(currentDateTime)
                    .respondent2LitigationFriendCreatedDate(
                        ofNullable(callbackParams.getCaseData().getRespondent2LitigationFriendCreatedDate())
                            .orElse(currentDateTime));

            } else if ("Defendant Two".equals(caseData.getSelectLitigationFriend().getValue().getLabel()
                .split(":")[0])) {
                caseDataUpdated
                    .respondent2LitigationFriend(caseData.getRespondent1LitigationFriend())
                    .respondent2LitigationFriendDate(currentDateTime)
                    .respondent2LitigationFriendCreatedDate(
                        ofNullable(callbackParams.getCaseData().getRespondent2LitigationFriendCreatedDate())
                            .orElse(currentDateTime))
                    .respondent1LitigationFriend(null);

            } else if ("Defendant One".equals(caseData.getSelectLitigationFriend().getValue().getLabel()
                .split(":")[0])) {
                caseDataUpdated
                    .respondent1LitigationFriendDate(currentDateTime)
                    .respondent1LitigationFriendCreatedDate(
                        ofNullable(callbackParams.getCaseData().getRespondent2LitigationFriendCreatedDate())
                            .orElse(currentDateTime));
            }
            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataUpdated.build().toMap(objectMapper))
                .build();
        }

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
        dynamicListOptions.add("Defendant One: " + caseData.getRespondent1().getPartyName());

        if (nonNull(caseData.getRespondent2())) {
            dynamicListOptions.add("Defendant Two: " + caseData.getRespondent2().getPartyName());
        }

        //build options for field (Default Value & List Options), add to case data
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.selectLitigationFriend(DynamicList.fromList(dynamicListOptions));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

}
