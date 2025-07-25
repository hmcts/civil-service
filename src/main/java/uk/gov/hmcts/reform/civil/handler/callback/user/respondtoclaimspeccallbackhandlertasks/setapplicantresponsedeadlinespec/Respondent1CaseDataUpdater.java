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

    private static final int RESPONSE_CLAIM_SPEC_DEADLINE_EXTENSION_MONTHS = 24;
    private final DeadlinesCalculator deadlinesCalculator;

    @Override
    public void update(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Updating Respondent1CaseData for caseId: {}", caseData.getCcdCaseReference());

        Party updatedRespondent1;
        if (NO.equals(caseData.getSpecAoSApplicantCorrespondenceAddressRequired())) {
            log.debug("Setting primary address to applicant correspondence address for caseId: {}", caseData.getCcdCaseReference());
            updatedRespondent1 = caseData.getRespondent1().toBuilder()
                    .primaryAddress(caseData.getSpecAoSApplicantCorrespondenceAddressdetails())
                    .build();
        } else {
            log.debug("Setting primary address to respondent1 copy's primary address for caseId: {}", caseData.getCcdCaseReference());
            updatedRespondent1 = caseData.getRespondent1().toBuilder()
                    .primaryAddress(caseData.getRespondent1Copy().getPrimaryAddress())
                    .build();
        }
        updatedData.respondent1(updatedRespondent1);

        if (caseData.getRespondent1Copy() != null) {
            log.debug("Copying flags from respondent1 copy for caseId: {}", caseData.getCcdCaseReference());
            updatedRespondent1 = caseData.getRespondent1().toBuilder()
                    .flags(caseData.getRespondent1Copy().getFlags())
                    .build();
            updatedData.respondent1(updatedRespondent1);
        }

        log.debug("Setting respondent1Copy to null and updating claimDismissedDeadline for caseId: {}", caseData.getCcdCaseReference());
        updatedData.respondent1Copy(null);
        updatedData.claimDismissedDeadline(deadlinesCalculator.addMonthsToDateToNextWorkingDayAtMidnight(
                RESPONSE_CLAIM_SPEC_DEADLINE_EXTENSION_MONTHS,
                LocalDate.now()
        ));
    }
}
