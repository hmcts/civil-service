package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@Component
@RequiredArgsConstructor
public class Respondent1CaseDataUpdater implements SetApplicantResponseDeadlineCaseDataUpdater {

    private static final int RESPONSE_CLAIM_SPEC_DEADLINE_EXTENSION_MONTHS = 24;
    private final DeadlinesCalculator deadlinesCalculator;

    @Override
    public void update(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        Party updatedRespondent1;
        if (NO.equals(caseData.getSpecAoSApplicantCorrespondenceAddressRequired())) {
            updatedRespondent1 = caseData.getRespondent1().toBuilder()
                    .primaryAddress(caseData.getSpecAoSApplicantCorrespondenceAddressdetails())
                    .build();
        } else {
            updatedRespondent1 = caseData.getRespondent1().toBuilder()
                    .primaryAddress(caseData.getRespondent1Copy().getPrimaryAddress())
                    .build();
        }
        updatedData.respondent1(updatedRespondent1);

        if (caseData.getRespondent1Copy() != null) {
            updatedRespondent1 = caseData.getRespondent1().toBuilder()
                    .flags(caseData.getRespondent1Copy().getFlags())
                    .build();
            updatedData.respondent1(updatedRespondent1);
        }

        updatedData.respondent1Copy(null);
        updatedData.claimDismissedDeadline(deadlinesCalculator.addMonthsToDateToNextWorkingDayAtMidnight(
                RESPONSE_CLAIM_SPEC_DEADLINE_EXTENSION_MONTHS,
                LocalDate.now()
        ));
    }
}
