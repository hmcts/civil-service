package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.orderdetailspages;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPersonalInjury;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpertEvidenceFieldFieldBuilder implements OrderDetailsPagesCaseFieldBuilder {

    private final WorkingDayIndicator workingDayIndicator;

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.debug("Updating expert evidence fields with calculated dates");
        FastTrackPersonalInjury tempFastTrackPersonalInjury = FastTrackPersonalInjury.builder()
                .input1("The Claimant has permission to rely upon the written expert evidence already uploaded to the"
                        + " Digital Portal with the particulars of claim")
                .input2("The Defendant(s) may ask questions of the Claimant's expert which must be sent to the expert " +
                        "directly and uploaded to the Digital Portal by 4pm on")
                .date2(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusDays(14)))
                .input3("The answers to the questions shall be answered by the Expert by")
                .date3(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusDays(42)))
                .input4("and uploaded to the Digital Portal by the party who has asked the question by")
                .date4(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusDays(49)))
                .build();

        updatedData.fastTrackPersonalInjury(tempFastTrackPersonalInjury).build();
        log.debug("Expert evidence fields updated successfully");
    }
}
