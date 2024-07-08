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
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.ConfirmOrderGivesPermission;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.DiscontinuanceTypeList;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.SettleDiscontinueYesOrNoList;
import uk.gov.hmcts.reform.civil.helpers.settlediscontinue.DiscontinueClaimHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.VALIDATE_DISCONTINUE_CLAIM_CLAIMANT;

@Service
@RequiredArgsConstructor
public class ValidateDiscontinueClaimClaimantCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(VALIDATE_DISCONTINUE_CLAIM_CLAIMANT);
    public static final String UNABLE_TO_VALIDATE = "# Unable to validate information";
    public static final String INFORMATION_SUCCESSFULLY_VALIDATED = "# Information successfully validated";
    public static final String NEXT_STEPS = """
            ### Next steps:

            No further action required.""";

    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::populateJudgeNameAndDate,
            callbackKey(ABOUT_TO_SUBMIT), this::submitChanges,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    private CallbackResponse populateJudgeNameAndDate(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder()
            .permissionGrantedJudgeCopy(caseData.getPermissionGrantedComplex() == null ? null
                                                : caseData.getPermissionGrantedComplex().getPermissionGrantedJudge())
            .permissionGrantedDateCopy(caseData.getPermissionGrantedComplex() == null ? null
                                               : caseData.getPermissionGrantedComplex().getPermissionGrantedDate());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse submitChanges(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        AboutToStartOrSubmitCallbackResponse
            .AboutToStartOrSubmitCallbackResponseBuilder aboutToStartOrSubmitCallbackResponseBuilder =
            AboutToStartOrSubmitCallbackResponse.builder();

        if (caseData.getTypeOfDiscontinuance() != null) {
            if (DiscontinuanceTypeList.FULL_DISCONTINUANCE.equals(caseData.getTypeOfDiscontinuance())) {
                if (!DiscontinueClaimHelper.is1v2LrVLrCase(caseData)
                    || DiscontinueClaimHelper.is1v2LrVLrCase(caseData)
                    && SettleDiscontinueYesOrNoList.YES.equals(caseData.getIsDiscontinuingAgainstBothDefendants())) {
                    aboutToStartOrSubmitCallbackResponseBuilder.state(CaseState.CASE_DISCONTINUED.name());
                } else {
                    caseData.setConfirmOrderGivesPermission(caseData.getConfirmOrderGivesPermission());
                }
            } else {
                caseData.setConfirmOrderGivesPermission(caseData.getConfirmOrderGivesPermission());
            }
        }

        return aboutToStartOrSubmitCallbackResponseBuilder.data(caseDataBuilder.build().toMap(objectMapper)).build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(getHeader(caseData))
            .confirmationBody(format(NEXT_STEPS))
            .build();
    }

    private static String getHeader(CaseData caseData) {
        if (ConfirmOrderGivesPermission.NO.equals(caseData.getConfirmOrderGivesPermission())) {
            return format(UNABLE_TO_VALIDATE);
        }
        return format(INFORMATION_SUCCESSFULLY_VALIDATED);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
