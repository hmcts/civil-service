package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.helpers.settlediscontinue.DiscontinueClaimHelper;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.SettleDiscontinueYesOrNoList;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.PermissionGranted;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DISCONTINUE_CLAIM_CLAIMANT;
import static uk.gov.hmcts.reform.civil.helpers.settlediscontinue.DiscontinueClaimHelper.is1v2LrVLrCase;

@Service
@RequiredArgsConstructor
public class DiscontinueClaimClaimantCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(DISCONTINUE_CLAIM_CLAIMANT);
    private static final String BOTH = "Both";
    private static final String ERROR_MESSAGE_DATE_ORDER_MUST_BE_IN_PAST = "Date must be in the past";
    private static final String ERROR_MESSAGE_UNABLE_TO_DISCONTINUE = "Unable to discontinue this claim";

    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::populateData)
            .put(callbackKey(MID, "showClaimantConsent"), this::updateSelectedClaimant)
            .put(callbackKey(MID, "checkPermissionGranted"), this::checkPermissionGrantedFields)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::submitChanges)
            .put(callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse)
            .build();
    }

    private CallbackResponse checkPermissionGrantedFields(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        List<String> errors = new ArrayList<>();

        if (null != caseData.getPermissionGrantedComplex()
            && validateIfFutureDate(caseData.getPermissionGrantedComplex().getPermissionGrantedDate())) {
            errors.add(ERROR_MESSAGE_DATE_ORDER_MUST_BE_IN_PAST);
        }

        if (SettleDiscontinueYesOrNoList.NO.equals(caseData.getIsPermissionGranted())) {
            errors.add(ERROR_MESSAGE_UNABLE_TO_DISCONTINUE);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private CallbackResponse updateSelectedClaimant(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        if (MultiPartyScenario.isTwoVOne(caseData)) {
            caseDataBuilder.selectedClaimantForDiscontinuance(caseData.getClaimantWhoIsDiscontinuing()
                                                                  .getValue().getLabel());
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse populateData(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        final var caseDataBuilder = caseData.toBuilder();
        List<String> errors = new ArrayList<>();

        DiscontinueClaimHelper.checkState(caseData, errors);
        if (errors.isEmpty()) {
            if (MultiPartyScenario.isTwoVOne(caseData)) {
                List<String> claimantNames = new ArrayList<>();
                claimantNames.add(caseData.getApplicant1().getPartyName());
                claimantNames.add(caseData.getApplicant2().getPartyName());
                claimantNames.add(BOTH);

                caseDataBuilder.claimantWhoIsDiscontinuing(DynamicList.fromList(claimantNames));
            }
            if (is1v2LrVLrCase(caseData)) {
                List<String> defendantNames = new ArrayList<>();
                defendantNames.add(caseData.getRespondent1().getPartyName());
                defendantNames.add(caseData.getRespondent2().getPartyName());

                caseDataBuilder.discontinuingAgainstOneDefendant(DynamicList.fromList(defendantNames));
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    public static boolean validateIfFutureDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        return date.isAfter(today);
    }

    private CallbackResponse submitChanges(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder()
            .permissionGrantedComplex(
                PermissionGranted.builder()
                    .permissionGrantedJudge(callbackParams.getCaseData().getPermissionGrantedJudgeCopy())
                    .permissionGrantedDate(callbackParams.getCaseData().getPermissionGrantedDateCopy())
                    .build());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
