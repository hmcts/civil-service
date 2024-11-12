package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.disposalhearing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingMedicalEvidence;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class MedicalEvidenceFieldBuilder implements SdoCaseFieldBuilder {

    private final WorkingDayIndicator workingDayIndicator;

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Setting medical evidence");
        updatedData.disposalHearingMedicalEvidence(DisposalHearingMedicalEvidence.builder()
                .input(
                        "The claimant has permission to rely upon the written expert evidence already "
                                + "uploaded to the Digital Portal with the particulars of claim and in addition has "
                                + "permission to rely upon any associated correspondence or updating report which "
                                + "is uploaded to the Digital Portal by 4pm on")
                .date(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(
                        4)))
                .build());
    }
}
