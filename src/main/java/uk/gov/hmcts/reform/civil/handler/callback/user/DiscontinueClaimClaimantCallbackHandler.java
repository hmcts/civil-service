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
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.DiscontinuanceTypeList;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.SettleDiscontinueYesOrNoList;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.helpers.settlediscontinue.DiscontinueClaimHelper;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.DISCONTINUE_CLAIM_CLAIMANT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_DISCONTINUED;
import static uk.gov.hmcts.reform.civil.helpers.settlediscontinue.DiscontinueClaimHelper.is1v2LrVLrCase;
import static uk.gov.hmcts.reform.civil.utils.PersistDataUtils.persistFlagsForParties;

@Service
@RequiredArgsConstructor
public class DiscontinueClaimClaimantCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(DISCONTINUE_CLAIM_CLAIMANT);
    private static final String BOTH = "Both";
    private static final String ERROR_MESSAGE_DATE_ORDER_MUST_BE_IN_PAST = "Date must be in the past";
    private static final String ERROR_MESSAGE_UNABLE_TO_DISCONTINUE = "Unable to discontinue this claim";
    public static final String PERMISSION_GRANTED_BY_COURT = "# Your request is being reviewed";
    public static final String CASE_DISCONTINUED_FULL_DISCONTINUE = "# Your claim has been discontinued";
    public static final String NO_COURT_PERMISSION_FULL_DISCONTINUE_ANYONE = "# Your claim will be fully discontinued against the specified defendants";
    public static final String NO_COURT_PERMISSION_PART_DISCONTINUE = "#  We have noted your claim has been partly discontinued and your claim has been updated";
    public static final String PERMISSION_GRANTED_BY_COURT_BODY = "### Next steps \n "
            + "You will be notified of the outcome.\n\n"
            + "You may be contacted by the court to provide more information if necessary.";
    public static final String CASE_DISCONTINUED_FULL_DISCONTINUE_BODY = "### Next step \n "
            + "Any hearing listed will be vacated and all other parties will be notified.";
    public static final String NO_COURT_PERMISSION_PART_DISCONTINUE_BODY = "### Next step \n "
            + "Any listed hearings will still proceed as normal.\n\n"
            + "All other parties will be notified.";
    public static final String NO_COURT_PERMISSION_FULL_DISCONTINUE_ANYONE_BODY = "### Next step \n "
            + "This will now be reviewed and the claim will proceed offline and your online account will not "
            + "be updated for this claim.Any updates will be sent by post.";
    private final ObjectMapper objectMapper;
    private final CaseDetailsConverter caseDetailsConverter;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::populateData)
            .put(callbackKey(MID, "showClaimantConsent"), this::updateSelectedClaimant)
            .put(callbackKey(MID, "checkPermissionGranted"), this::checkPermissionGrantedFields)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::aboutToSubmit)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
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

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        Map<String, String> confirmationContent = getConfirmationContent(caseData);
        if (!confirmationContent.isEmpty()) {
            return SubmittedCallbackResponse.builder()
                    .confirmationHeader(confirmationContent.get("header"))
                    .confirmationBody(confirmationContent.get("body"))
                    .build();
        }
        return SubmittedCallbackResponse.builder().build();
    }

    private Map<String, String> getConfirmationContent(CaseData caseData) {
        boolean isNoCourtPermission = SettleDiscontinueYesOrNoList.NO.equals(caseData.getCourtPermissionNeeded());

        if (!isNoCourtPermission && SettleDiscontinueYesOrNoList.YES.equals(caseData.getIsPermissionGranted())) {
            return addConfirmationContent(format(PERMISSION_GRANTED_BY_COURT),
                    format(PERMISSION_GRANTED_BY_COURT_BODY));
        }

        if (DiscontinuanceTypeList.FULL_DISCONTINUANCE.equals(caseData.getTypeOfDiscontinuance())) {
            if (isNoCourtPermission && isCaseDiscontinued(caseData)) {
                return addConfirmationContent(format(CASE_DISCONTINUED_FULL_DISCONTINUE),
                        format(CASE_DISCONTINUED_FULL_DISCONTINUE_BODY));
            }
            if (isNoCourtPermission && isFullyDiscontinuedAgainst(caseData)) {
                return addConfirmationContent(format(NO_COURT_PERMISSION_FULL_DISCONTINUE_ANYONE),
                        format(NO_COURT_PERMISSION_FULL_DISCONTINUE_ANYONE_BODY));
            }
        } else {
            if (isNoCourtPermission) {
                return addConfirmationContent(format(NO_COURT_PERMISSION_PART_DISCONTINUE),
                        format(NO_COURT_PERMISSION_PART_DISCONTINUE_BODY));
            }
        }
        return new HashMap<>();
    }

    private Map<String, String> addConfirmationContent(String headerContent, String bodyContent) {
        Map<String, String> confirmationContent = new HashMap<>();
        confirmationContent.put("header", headerContent);
        confirmationContent.put("body", bodyContent);
        return confirmationContent;
    }

    private boolean isCaseDiscontinued(CaseData caseData) {
        boolean isBothClaimantsSelected = caseData.getClaimantWhoIsDiscontinuing() != null
                && caseData.getClaimantWhoIsDiscontinuing().getValue().getLabel().equals(BOTH);
        boolean isAgainstBothDefendants = SettleDiscontinueYesOrNoList.YES.equals(caseData.getIsDiscontinuingAgainstBothDefendants());

        return (isBothClaimantsSelected || isAgainstBothDefendants || MultiPartyScenario.isOneVOne(caseData));
    }

    private boolean isFullyDiscontinuedAgainst(CaseData caseData) {
        boolean isNotAgainstBothDefendants = SettleDiscontinueYesOrNoList.NO.equals(caseData.getIsDiscontinuingAgainstBothDefendants());
        boolean isNotBothClaimantsSelected = caseData.getClaimantWhoIsDiscontinuing() != null
                && !caseData.getClaimantWhoIsDiscontinuing().getValue().getLabel().equals(BOTH);

        return (isNotBothClaimantsSelected || isNotAgainstBothDefendants);
    }

    private CallbackResponse aboutToSubmit(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        CaseData oldCaseData = caseDetailsConverter.toCaseData(callbackParams.getRequest().getCaseDetailsBefore());

        // persist party flags (ccd issue)
        persistFlagsForParties(oldCaseData, caseData, caseDataBuilder);

        caseDataBuilder.businessProcess(BusinessProcess.ready(DISCONTINUE_CLAIM_CLAIMANT));
        if (MultiPartyScenario.isTwoVOne(caseData)) {
            caseDataBuilder.selectedClaimantForDiscontinuance(caseData.getClaimantWhoIsDiscontinuing()
                                                                  .getValue().getLabel());
        }
        caseDataBuilder.previousCCDState(caseData.getCcdState());
        return AboutToStartOrSubmitCallbackResponse.builder()
                .state(updateCaseState(caseData))
                .data(caseDataBuilder.build().toMap(objectMapper))
                .build();
    }

    private String updateCaseState(CaseData caseData) {
        if (DiscontinuanceTypeList.FULL_DISCONTINUANCE.equals(caseData.getTypeOfDiscontinuance())) {
            boolean isNoCourtPermission = SettleDiscontinueYesOrNoList.NO.equals(caseData.getCourtPermissionNeeded());
            if (isNoCourtPermission && isCaseDiscontinued(caseData)) {
                return CASE_DISCONTINUED.name();
            }
        }
        return caseData.getCcdState().name();
    }

    public static boolean validateIfFutureDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        return date.isAfter(today);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

}
