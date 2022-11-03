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
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDate;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert4;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.nonNull;
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
        String respondent1Email = caseData.getRespondentSolicitor1EmailAddress();
        String respondent2Email = caseData.getRespondentSolicitor2EmailAddress();
        String applicant1Email = caseData.getApplicantSolicitor1UserDetails().getEmail();
        List<String> listData = new ArrayList<>();

        if (applicant1Email.equals(userDetails.getEmail())) {
            listData = respondentOtherParticipants(caseData);

        } else if (nonNull(respondent1Email.equals(userDetails.getEmail())) || nonNull(respondent2Email
            .equals(userDetails.getEmail()))) {
            listData = applicantOtherParticipants(caseData);
        }

        UploadEvidenceExpert4 partyName = UploadEvidenceExpert4.builder()
            .expertOption4OtherName4(DynamicList.fromList(listData)).build();
        UploadEvidenceDate partyName2 = UploadEvidenceDate.builder()
            .expertOption3OtherName3(DynamicList.fromList(listData)).build();

        List<Element<UploadEvidenceExpert4>> updatedPartyName = newArrayList();
        updatedPartyName.add(0, element(partyName));

        List<Element<UploadEvidenceDate>> updatedPartyName2 = newArrayList();
        updatedPartyName2.add(0, element(partyName2));

        CaseData updatedCaseData = caseData.toBuilder()
            .documentUploadExpert4(updatedPartyName)
            .documentUploadExpert3(updatedPartyName2)
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

    public List<String> respondentOtherParticipants(CaseData caseData) {
        List<String> listData = new ArrayList<>();
        MultiPartyScenario multiPartyScenario  = getMultiPartyScenario(caseData);
        if (multiPartyScenario.equals(MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP)
            || multiPartyScenario.equals(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP)) {
            listData.add(caseData.getRespondent1().getPartyName());
            listData.add(caseData.getRespondent2().getPartyName());
            listData.add("Both");

        } else {
            listData.add(caseData.getRespondent1().getPartyName());
        }
        return listData;
    }

    public List<String> applicantOtherParticipants(CaseData caseData) {
        List<String> listData = new ArrayList<>();
        MultiPartyScenario multiPartyScenario  = getMultiPartyScenario(caseData);
        if (multiPartyScenario.equals(MultiPartyScenario.TWO_V_ONE)) {
            listData.add(caseData.getApplicant1().getPartyName());
            listData.add(caseData.getApplicant2().getPartyName());
            listData.add("Both");

        } else {
            listData.add(caseData.getApplicant1().getPartyName());
        }
        return listData;
    }

}

