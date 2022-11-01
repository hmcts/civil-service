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

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseProgression.UploadEvidenceDate;
import uk.gov.hmcts.reform.civil.model.caseProgression.UploadEvidenceExpert4;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
public class EvidenceUploadHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(EVIDENCE_UPLOAD);
    private final ObjectMapper objectMapper;
    private final IdamClient idamClient;
    private String otherPartyName;
    private UploadEvidenceExpert4 uploadEvidenceExpert4;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::populateValues)
            .put(callbackKey(MID, "validateValues"), this::validateValues)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::emptyCallbackResponse)
            .put(callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse populateValues(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        UserDetails userDetails = idamClient.getUserDetails(callbackParams.getParams().get(BEARER_TOKEN).toString());
        var respondent1Email = caseData.getRespondentSolicitor1EmailAddress();
        var respondent2Email = caseData.getRespondentSolicitor2EmailAddress();
        var applicant1Email = caseData.getApplicantSolicitor1UserDetails().getEmail();

        if (applicant1Email.equals(userDetails.getEmail())) {
            otherPartyName = RespondentOtherParticipants(caseData).toString();
        } else if (respondent1Email.equals(userDetails.getEmail()) || respondent2Email
            .equals(userDetails.getEmail())) {
            otherPartyName = ApplicantOtherParticipants(caseData).toString();
        }
        var partyName = UploadEvidenceExpert4.builder()
                                                                .expertOption4OtherName(otherPartyName).build();

        List<Element<UploadEvidenceExpert4>> updatedPartyName = newArrayList();
        updatedPartyName.add(0, element(partyName));

        CaseData updatedCaseData = caseData.toBuilder()
            .documentUploadExpert4(updatedPartyName)
            .build();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData.toMap(objectMapper))
            .build();
    }

    private CallbackResponse validateValues(CallbackParams callbackParams) {
        var caseData = callbackParams.getCaseData();
        List<String> errors = new ArrayList<>();

        if (caseData.getDocumentUploadWitness1() != null) {
            List<Element<UploadEvidenceDate>> dateList = caseData.getDocumentUploadWitness1();
            dateList.forEach(date -> {
                if (date.getValue().getWitnessOption1UploadDate().isAfter(LocalDate.now())) {
                    errors.add("Invalid date: date entered must not be in the future.");
                }
            });
        }

        if (caseData.getDocumentUploadWitness3() != null) {
            List<Element<UploadEvidenceDate>> dateList = caseData.getDocumentUploadWitness3();
            dateList.forEach(date -> {
                if (date.getValue().getWitnessOption3UploadDate().isAfter(LocalDate.now())) {
                    errors.add("Invalid date: date entered must not be in the future.");
                }
            });
        }

        if (caseData.getDocumentUploadExpert1() != null) {
            List<Element<UploadEvidenceDate>> dateList = caseData.getDocumentUploadExpert1();
            dateList.forEach(date -> {
                if (date.getValue().getExpertOption1UploadDate().isAfter(LocalDate.now())) {
                    errors.add("Invalid date: date entered must not be in the future.");
                }
            });
        }

        if (caseData.getDocumentUploadExpert2() != null) {
            List<Element<UploadEvidenceDate>> dateList = caseData.getDocumentUploadExpert2();
            dateList.forEach(date -> {
                if (date.getValue().getExpertOption2UploadDate().isAfter(LocalDate.now())) {
                    errors.add("Invalid date: date entered must not be in the future.");
                }
            });
        }

        if (caseData.getDocumentUploadExpert3() != null) {
            List<Element<UploadEvidenceDate>> dateList = caseData.getDocumentUploadExpert3();
            dateList.forEach(date -> {
                if (date.getValue().getExpertOption3UploadDate().isAfter(LocalDate.now())) {
                    errors.add("Invalid date: date entered must not be in the future.");
                }
            });
        }

        if (caseData.getDocumentUploadExpert4() != null) {
            List<Element<UploadEvidenceExpert4>> dateList = caseData.getDocumentUploadExpert4();
            dateList.forEach(date -> {
                if (date.getValue().getExpertOption4UploadDate().isAfter(LocalDate.now())) {
                    errors.add("Invalid date: date entered must not be in the future.");
                }
            });
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .build();
    }

    public StringBuilder RespondentOtherParticipants(CaseData caseData) {
        StringBuilder otherParticipantString = new StringBuilder();
        MultiPartyScenario multiPartyScenario  = getMultiPartyScenario(caseData);
        if (multiPartyScenario.equals(MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP)
            || multiPartyScenario.equals(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP)) {
            otherParticipantString.append(caseData.getRespondent1().getPartyName())
                .append(" and ").append(caseData.getRespondent2().getPartyName());

        } else {
            otherParticipantString.append(caseData.getRespondent1()
                                              .getPartyName());
        }
        return otherParticipantString;
    }

    public StringBuilder ApplicantOtherParticipants(CaseData caseData) {
        StringBuilder otherParticipantString = new StringBuilder();
        MultiPartyScenario multiPartyScenario  = getMultiPartyScenario(caseData);
        if (multiPartyScenario.equals(MultiPartyScenario.TWO_V_ONE)) {
            otherParticipantString.append(caseData.getApplicant1().getPartyName())
                .append(caseData.getApplicant2().getPartyName());
        } else {
            otherParticipantString.append(caseData.getApplicant1()
                                              .getPartyName());
        }
        return otherParticipantString;
    }

}

