package uk.gov.hmcts.reform.civil.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.utils.AccessCodeGenerator;

import java.time.LocalDate;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_CASE_DATA;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefendantPinToPostLRspecService {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;

    public CaseDetails checkPinValid(String caseRef, String authorisation, String pin) {
        // search Case
        var caseDetailsResponse = coreCaseDataService.getCase(caseRef.chars().count(), authorisation);
        CaseData caseData = caseDetailsConverter.toCaseData(caseDetailsResponse);
        DefendantPinToPostLRspec pinInPostData = caseData.getRespondent1PinToPostLRspec();
        // Checking on the Pin entered valid
        if (pinInPostData != null && pinInPostData.getAccessCode().equals(pin)) {
            removePinInPostData(caseData.getCcdCaseReference());
            return caseDetailsResponse;
        }
        return null;
    }

    public void removePinInPostData(Long caseId) {
        try {
            var startEventResponse = coreCaseDataService.startUpdate(caseId.toString(), UPDATE_CASE_DATA);
            coreCaseDataService.submitUpdate(caseId.toString(), removePinInPostDataContent(startEventResponse));
        } catch (FeignException e) {
            log.error(String.format("Updating case data failed: %s", e.contentUTF8()));
            throw e;
        }
    }

    private CaseDataContent removePinInPostDataContent(StartEventResponse startEventResponse) {
        Map<String, Object> data = startEventResponse.getCaseDetails().getData();
        data.replace("respondent1PinToPostLRspec.accessCode", null);

        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(Event.builder().id(startEventResponse.getEventId()).build())
            .data(data)
            .build();
    }

    public DefendantPinToPostLRspec buildDefendantPinToPost() {
        LocalDate expiryDate = LocalDate.now().plusDays(180);
        return DefendantPinToPostLRspec.builder()
            .accessCode(AccessCodeGenerator.generateAccessCode())
            .respondentCaseRole(
                CaseRole.RESPONDENTSOLICITORONESPEC.getFormattedName())
            .expiryDate(expiryDate)
            .build();
    }
}
