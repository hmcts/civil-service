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
import uk.gov.hmcts.reform.civil.enums.PartySelected;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EDIT_PARTY_DETAILS;
@Service
@RequiredArgsConstructor
public class EditPartyDetailsCallBackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(EDIT_PARTY_DETAILS);

    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::buildPartyList,
            callbackKey(MID, "specificParty"), this::buildSpecificParty,
            callbackKey(ABOUT_TO_SUBMIT), this::validateUpdatedDetails,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse buildPartyList(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();
        List<String> dynamicListOptions = new ArrayList<>();
        dynamicListOptions.add("Claimant One: " + caseData.getApplicant1().getPartyName());
        if (nonNull(caseData.getApplicant2())) {
            dynamicListOptions.add("Claimant Two: " + caseData.getApplicant2().getPartyName());
        }
        dynamicListOptions.add("Defendant One: " + caseData.getRespondent1().getPartyName());
        if (nonNull(caseData.getRespondent2())) {
            dynamicListOptions.add("Defendant Two: " + caseData.getRespondent2().getPartyName());
        }

        addLitigationFriends(dynamicListOptions, caseData);
        addExperts(dynamicListOptions, caseData);
        addWitnesses(dynamicListOptions, caseData);

        caseDataBuilder.partyOptions(DynamicList.fromList(dynamicListOptions));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private void addLitigationFriends(List<String> dynamicListOptions, CaseData caseData) {
        if (nonNull(caseData.getApplicant1LitigationFriend())) {
            dynamicListOptions.add(String.format("Claimant One Litigation Friend: %s %s",
                                                 caseData.getApplicant1LitigationFriend().getFirstName(),
                                                 caseData.getApplicant1LitigationFriend().getLastName()));
        }

        if (nonNull(caseData.getApplicant2LitigationFriend())) {
            dynamicListOptions.add(String.format("Claimant Two Litigation Friend: %s %s",
                                                 caseData.getApplicant2LitigationFriend().getFirstName(),
                                                 caseData.getApplicant2LitigationFriend().getLastName()));
        }

        if (nonNull(caseData.getRespondent1LitigationFriend())) {
            dynamicListOptions.add(String.format("Defendant One Litigation Friend: %s %s",
                                                 caseData.getRespondent1LitigationFriend().getFirstName(),
                                                 caseData.getRespondent1LitigationFriend().getLastName()));
        }

        if (nonNull(caseData.getRespondent2LitigationFriend())) {
            dynamicListOptions.add(String.format("Defendant Two Litigation Friend: %s %s",
                                                 caseData.getRespondent1LitigationFriend().getFirstName(),
                                                 caseData.getRespondent1LitigationFriend().getLastName()));
        }
    }

    private void addExperts(List<String> dynamicListOptions, CaseData caseData) {
        if (nonNull(caseData.getApplicant1DQ()) && nonNull(caseData.getApplicant1DQ().getApplicant1DQExperts())) {
            dynamicListOptions.add("Claimant Experts");
        }
        if (nonNull(caseData.getRespondent1DQ()) && nonNull(caseData.getRespondent1DQ().getRespondent1DQExperts())) {
            dynamicListOptions.add("Defendant One Experts");
        }
        if (nonNull(caseData.getRespondent2DQ()) && nonNull(caseData.getRespondent2DQ().getRespondent2DQExperts())) {
            dynamicListOptions.add("Defendant Two Experts");
        }
    }

    private void addWitnesses(List<String> dynamicListOptions, CaseData caseData) {
        if (nonNull(caseData.getApplicant1DQ()) && nonNull(caseData.getApplicant1DQ().getApplicant1DQWitnesses())) {
            dynamicListOptions.add("Claimant Witnesses");
        }
        if (nonNull(caseData.getRespondent1DQ()) && nonNull(caseData.getRespondent1DQ().getRespondent1DQWitnesses())) {
            dynamicListOptions.add("Defendant One Witnesses");
        }
        if (nonNull(caseData.getRespondent2DQ()) && nonNull(caseData.getRespondent2DQ().getRespondent2DQWitnesses())) {
            dynamicListOptions.add("Defendant Two Witnesses");
        }
    }

    private CallbackResponse buildSpecificParty(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        String partyChosen = caseData.getPartyOptions().getValue().getLabel();

        String selection = String.format("%s_%s%s",
                                         getClaimantOrDefendant(partyChosen),
                                         getOneOrTwo(partyChosen),
                                         getType(partyChosen));

        caseDataBuilder.partySelected(PartySelected.valueOf(selection));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private String getClaimantOrDefendant(String selection){
        return selection.toLowerCase().contains("claimant") ? "CLAIMANT" : "DEFENDANT";
    }

    private String getOneOrTwo(String selection){
        return selection.toLowerCase().contains("two") ? "TWO" : "ONE";
    }

    private String getType(String selection){
        if(selection.toLowerCase().contains("experts")){
            return "_EXPERTS";
        } else if (selection.toLowerCase().contains("litigation")) {
            return "_LITIGATION_FRIEND";
        } else if (selection.toLowerCase().contains("witness")){
            return "_WITNESSES";
        }
        return "";
    }

    private CallbackResponse validateUpdatedDetails(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder caseDataBuilder = caseData.toBuilder();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("# You have updated party details")
            .confirmationBody("<br />")
            .build();
    }
}
