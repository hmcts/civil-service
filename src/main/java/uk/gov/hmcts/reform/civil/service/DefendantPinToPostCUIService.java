package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.model.citizenui.dto.EventDto;
import uk.gov.hmcts.reform.civil.service.citizen.events.EventSubmissionParams;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefendantPinToPostCUIService {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;

    public CaseDetails checkPinValid(Long caseId, String authorisation, String pin, EventDto eventDto) {
        var caseDetailsResponse = coreCaseDataService.getCase(caseId, authorisation);
        CaseData caseData = caseDetailsConverter.toCaseData(caseDetailsResponse);
        DefendantPinToPostLRspec pinInPostData = caseData.getRespondent1PinToPostLRspec();
        // Checking on the Pin entered valid
        if (pinInPostData != null && pinInPostData.getAccessCode().equals(pin)) {

            return caseDetailsResponse;
        }
        return null;
    }

    public CaseDetails getLRCase(Long caseId, String authorisation) {
        var caseDetailsResponse = coreCaseDataService.getCase(caseId, authorisation);
        CaseData caseData = caseDetailsConverter.toCaseData(caseDetailsResponse);
        DefendantPinToPostLRspec pinInPostData = caseData.getRespondent1PinToPostLRspec();
        // Checking if case is expired and used already
        if (pinInPostData == null || pinInPostData.getExpiryDate().isAfter(LocalDate.now())) {
            return null;
        }
        return caseDetailsResponse;
    }

//    public CaseData removePinAfterValidation(CaseData caseData, String authorisation, EventDto eventDto) {
//        EventSubmissionParams params = EventSubmissionParams
//            .builder()
//            .authorisation(authorisation)
//            .caseId(caseData.getCaseId())
//            .userId(submitterId)
//            .event(eventDto.getEvent())
//            .updates(eventDto.getCaseDataUpdate())
//            .build();
//        CaseData caseData = caseDetailsConverter
//            .toCaseData(caseEventService.submitEvent(params));
//
//    }
}
