package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefendantPinToPostLRspecService {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;

    public CaseDetails checkPinValid(Long caseId, String authorisation, String pin) {
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
}
