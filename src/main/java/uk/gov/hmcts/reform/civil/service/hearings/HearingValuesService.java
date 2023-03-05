package uk.gov.hmcts.reform.civil.service.hearings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.exceptions.CaseNotFoundException;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.TempHearingValuesModel;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

@Slf4j
@Service
@RequiredArgsConstructor
public class HearingValuesService {

    //Todo: Introduce lov ref data service and hearing data mapper service
    private final CoreCaseDataService caseDataService;
    private final CaseDetailsConverter caseDetailsConverter;

    public TempHearingValuesModel getValues(Long caseId, String hearingId) {
        CaseData caseData = retrieveCaseData(caseId);

        //ToDo: Use lov refdata model and retrieve data from lov ref data service;
        Object lovRefData = "ref data placeholder";

        //ToDo: Utilise hearing mapper;

        return TempHearingValuesModel.builder().build();
    }

    private CaseData retrieveCaseData(long caseId) {
        try {
            return caseDetailsConverter.toCaseData(caseDataService.getCase(caseId).getData());
        } catch (Exception ex) {
            log.error(String.format("No case found for %d", caseId));
            throw new CaseNotFoundException();
        }
    }
}
