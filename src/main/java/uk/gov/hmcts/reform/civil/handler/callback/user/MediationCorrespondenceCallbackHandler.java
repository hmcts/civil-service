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
import uk.gov.hmcts.reform.civil.enums.mediation.MediationCorrespondenceType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseNote;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.DynamicMultiSelectList;
import uk.gov.hmcts.reform.civil.service.CaseNoteService;
import uk.gov.hmcts.reform.civil.service.Time;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MEDIATION_CORRESPONDENCE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;

@Service
@RequiredArgsConstructor
public class MediationCorrespondenceCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(MEDIATION_CORRESPONDENCE);

    static final String DEFENDANT_1 = "DEFENDANT_1";
    static final String DEFENDANT_2 = "DEFENDANT_2";
    static final String CLAIMANT_1 = "CLAIMANT_1";
    static final String CLAIMANT_2 = "CLAIMANT_2";
    static final String SELECT_ALL = "SELECT_ALL";

    static final String ERROR_NO_PARTY_SELECTED = "Who was involved in this correspondence? is required";
    static final String ERROR_FUTURE_DATE = "The date the correspondence was sent or received must be today or in the past";
    static final String CONFIRMATION_HEADER = "# Mediation correspondence recorded";
    static final String CONFIRMATION_BODY = "### What happens next%n%n"
        + "The details you have entered will be saved on the Summary and Notes tab.";

    static final String NOTE_TITLE = "Mediation correspondence";

    private final ObjectMapper objectMapper;
    private final CaseNoteService caseNoteService;
    private final Time time;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::populatePartyOptions)
            .put(callbackKey(MID, "validate-mediation-correspondence"), this::validateCorrespondence)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::recordCorrespondence)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse populatePartyOptions(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        List<DynamicListElement> options = new ArrayList<>(getPartyOptions(caseData));
        options.add(DynamicListElement.dynamicElementFromCode(SELECT_ALL, "Select all"));

        caseData.setMediationCorrespondenceParties(new DynamicMultiSelectList().setListItems(options));
        clearCorrespondenceFields(caseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateCorrespondence(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();

        if (getSelectedCodes(caseData).isEmpty()) {
            errors.add(ERROR_NO_PARTY_SELECTED);
        }
        if (caseData.getMediationCorrespondenceDate() != null
            && caseData.getMediationCorrespondenceDate().isAfter(time.now().toLocalDate())) {
            errors.add(ERROR_FUTURE_DATE);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .errors(errors)
            .build();
    }

    private CallbackResponse recordCorrespondence(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        CaseNote caseNote = caseNoteService.buildCaseNote(
            callbackParams.getParams().get(BEARER_TOKEN).toString(),
            buildNoteText(caseData)
        );
        caseData.setCaseNotes(caseNoteService.addNoteToListStart(caseNote, caseData.getCaseNotes()));

        caseData.setMediationCorrespondenceParties(null);
        clearCorrespondenceFields(caseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse buildConfirmation(CallbackParams callbackParams) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(CONFIRMATION_HEADER)
            .confirmationBody(String.format(CONFIRMATION_BODY))
            .build();
    }

    private String buildNoteText(CaseData caseData) {
        String correspondenceType = ofNullable(caseData.getMediationCorrespondenceType())
            .map(MediationCorrespondenceType::getValue)
            .orElse("");
        String correspondenceDate = ofNullable(caseData.getMediationCorrespondenceDate())
            .map(date -> formatLocalDate(date, DATE))
            .orElse("");
        return String.join(
            "\n",
            NOTE_TITLE,
            "Who was involved in this correspondence?: " + getSelectedPartyLabels(caseData),
            "Which type of correspondence do you want to record?: " + correspondenceType,
            "What date was the correspondence sent or received?: " + correspondenceDate,
            "Summary of the correspondence: " + ofNullable(caseData.getMediationCorrespondenceSummary()).orElse("")
        );
    }

    private List<DynamicListElement> getPartyOptions(CaseData caseData) {
        List<DynamicListElement> options = new ArrayList<>();
        addPartyOption(options, DEFENDANT_1, "Defendant 1", caseData.getRespondent1());
        addPartyOption(options, DEFENDANT_2, "Defendant 2", caseData.getRespondent2());
        addPartyOption(options, CLAIMANT_1, "Claimant 1", caseData.getApplicant1());
        addPartyOption(options, CLAIMANT_2, "Claimant 2", caseData.getApplicant2());
        return options;
    }

    private void addPartyOption(List<DynamicListElement> options, String code, String role, Party party) {
        if (party != null) {
            options.add(DynamicListElement.dynamicElementFromCode(code, role + ": " + party.getPartyName()));
        }
    }

    private Set<String> getSelectedCodes(CaseData caseData) {
        return ofNullable(caseData.getMediationCorrespondenceParties())
            .map(DynamicMultiSelectList::getValue)
            .orElse(List.of())
            .stream()
            .filter(Objects::nonNull)
            .map(DynamicListElement::getCode)
            .collect(Collectors.toSet());
    }

    private String getSelectedPartyLabels(CaseData caseData) {
        Set<String> selectedCodes = getSelectedCodes(caseData);
        boolean selectAll = selectedCodes.contains(SELECT_ALL);
        return getPartyOptions(caseData).stream()
            .filter(option -> selectAll || selectedCodes.contains(option.getCode()))
            .map(DynamicListElement::getLabel)
            .collect(Collectors.joining(", "));
    }

    private void clearCorrespondenceFields(CaseData caseData) {
        caseData.setMediationCorrespondenceType(null);
        caseData.setMediationCorrespondenceDate(null);
        caseData.setMediationCorrespondenceSummary(null);
    }
}
