package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@Component
@RequiredArgsConstructor
@Slf4j
public class Respondent1CaseDataUpdater implements SetApplicantResponseDeadlineCaseDataUpdater {

    private static final int RESPONSE_CLAIM_SPEC_DEADLINE_EXTENSION_MONTHS = 36;
    private final DeadlinesCalculator deadlinesCalculator;

    @Override
    public void update(CaseData caseData) {
        log.info("Updating Respondent1CaseData for caseId: {}", caseData.getCcdCaseReference());

        Party updatedRespondent1;
        if (NO.equals(caseData.getSpecAoSApplicantCorrespondenceAddressRequired())) {
            log.info("Setting primary address to applicant correspondence address for caseId: {}", caseData.getCcdCaseReference());
            updatedRespondent1 = caseData.getRespondent1();
            updatedRespondent1.setPrimaryAddress(caseData.getSpecAoSApplicantCorrespondenceAddressdetails());
        } else {
            log.info("Setting primary address to respondent1 copy's primary address for caseId: {}", caseData.getCcdCaseReference());
            updatedRespondent1 = caseData.getRespondent1();
            updatedRespondent1.setPrimaryAddress(caseData.getRespondent1Copy().getPrimaryAddress());
        }
        caseData.setRespondent1(updatedRespondent1);

        if (caseData.getRespondent1Copy() != null) {
            log.info("Copying flags from respondent1 copy for caseId: {}", caseData.getCcdCaseReference());
            updatedRespondent1 = caseData.getRespondent1();
            updatedRespondent1.setFlags(caseData.getRespondent1Copy().getFlags());
            caseData.setRespondent1(updatedRespondent1);
        }

        log.info("Setting respondent1Copy to null and updating claimDismissedDeadline for caseId: {}", caseData.getCcdCaseReference());
        caseData.setRespondent1Copy(null);
        caseData.setClaimDismissedDeadline(deadlinesCalculator.addMonthsToDateToNextWorkingDayAtMidnight(
                RESPONSE_CLAIM_SPEC_DEADLINE_EXTENSION_MONTHS,
                LocalDate.now()
        ));
    }
}
