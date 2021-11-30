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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.*;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_DEFENDANT_LITIGATION_FRIEND;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;

@Service
@RequiredArgsConstructor
public class AddDefendantLitigationFriendCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(ADD_DEFENDANT_LITIGATION_FRIEND);

    private final ObjectMapper objectMapper;
    private final ExitSurveyContentService exitSurveyContentService;
    private final FeatureToggleService featureToggleService;

    private static final String CONFIRMATION_SUMMARY = "<br />The defendant litigation friend has"
        + " been notified of the claim details.%n%n"
        + "They must respond by %s. Your account will be updated and you will be sent an email.";

    public static final String NOTIFICATION_ONE_PARTY_SUMMARY = "<br />Notification of claim details sent to "
        + "1 Defendant litigation friend representative only.%n%n"
        + "You must notify the other defendant legal representative of the claim details by %s";

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(V_1, ABOUT_TO_START), this::prepareDefendantSolicitorOptions,
            callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit,
            callbackKey(SUBMITTED), this::buildConfirmation,
            callbackKey(V_1, SUBMITTED), this::buildConfirmationWithSolicitorOptions
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseDataUpdated = callbackParams.getCaseData();

        if (caseDataUpdated.getSelectLitigationFriend().getValue().getLabel().contains("Both")
            || caseDataUpdated.getSelectLitigationFriend().getValue().getLabel().contains("Respondent One:")
        ) {
            System.out.println(caseDataUpdated
                                   .toBuilder()
                                   .businessProcess(BusinessProcess.ready(ADD_DEFENDANT_LITIGATION_FRIEND))
                                   .respondent1LitigationFriendDate(LocalDateTime.now())
                                   .respondent1LitigationFriendCreatedDate(
                                       ofNullable(callbackParams.getCaseData().getRespondent1LitigationFriendCreatedDate())
                                           .orElse(LocalDateTime.now()))
                                   + "Both or respondent one -----------------------------"
            );
            caseDataUpdated
                .toBuilder()
                .businessProcess(BusinessProcess.ready(ADD_DEFENDANT_LITIGATION_FRIEND))
                .respondent1LitigationFriendDate(LocalDateTime.now())
                .respondent1LitigationFriendCreatedDate(
                    ofNullable(callbackParams.getCaseData().getRespondent1LitigationFriendCreatedDate())
                        .orElse(LocalDateTime.now()))
                .build();

        } else {
            //respondent2LitigationFriend + add the date it was added
            caseDataUpdated
                .toBuilder()
                .businessProcess(BusinessProcess.ready(ADD_DEFENDANT_LITIGATION_FRIEND))
                .respondent2LitigationFriendDate(LocalDateTime.now())
                .respondent2LitigationFriendCreatedDate(
                    ofNullable(callbackParams.getCaseData().getRespondent2LitigationFriendCreatedDate())
                        .orElse(LocalDateTime.now()))
                .respondent2LitigationFriend(caseDataUpdated.getRespondent1LitigationFriend())
                .respondent1LitigationFriend(null)
                .build();
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataUpdated.toMap(objectMapper))
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

        //build options for field (Default Value & List Options), add to case data
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.selectLitigationFriend(DynamicList.fromList(dynamicListOptions));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private SubmittedCallbackResponse buildConfirmationWithSolicitorOptions(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        if (!featureToggleService.isMultipartyEnabled()
            || caseData.getSelectLitigationFriend() == null) {
            return buildConfirmation(callbackParams);
        }

        String formattedDeadline = formatLocalDateTime(caseData.getClaimDetailsNotificationDeadline(), DATE_TIME_AT);

        String confirmationText = isLitigationFriendForToBothParty(caseData)
            ? CONFIRMATION_SUMMARY
            : NOTIFICATION_ONE_PARTY_SUMMARY;

        String body = format(confirmationText, formattedDeadline)
            + exitSurveyContentService.applicantSurvey();

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(String.format(
                "# Notification of claim sent%n## Claim number: %s",
                caseData.getLegacyCaseReference()
            ))
            .confirmationBody(body)
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
