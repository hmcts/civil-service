package uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DeadlineExtensionCalculatorService {

    private final WorkingDayIndicator workingDayIndicator;
    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;

    public LocalDate calculateExtendedDeadline(LocalDate dateProposed) {
        return workingDayIndicator.isWorkingDay(dateProposed)
            ? dateProposed
            : workingDayIndicator.getNextWorkingDay(dateProposed);
    }

    public LocalDate getAgreedDeadlineResponseDate(Long caseId, String authorization) {
        CaseData caseData = caseDetailsConverter.toCaseData(coreCaseDataService.getCase(caseId, authorization));
        return caseData.getRespondentSolicitor1AgreedDeadlineExtension();
    }
}
