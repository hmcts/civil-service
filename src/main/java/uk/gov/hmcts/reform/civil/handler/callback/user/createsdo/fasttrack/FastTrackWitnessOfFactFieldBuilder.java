package uk.gov.hmcts.reform.civil.handler.callback.user.createsdo.fasttrack;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdo.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackWitnessOfFact;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class FastTrackWitnessOfFactFieldBuilder implements SdoCaseFieldBuilder {

    private final WorkingDayIndicator workingDayIndicator;

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Building FastTrackTrial fields for caseId: {}", updatedData.build().getCcdCaseReference());
        updatedData.fastTrackWitnessOfFact(getFastTrackWitnessOfFact());
    }

    private FastTrackWitnessOfFact getFastTrackWitnessOfFact() {
        return FastTrackWitnessOfFact.builder()
                .input1("Each party must upload to the Digital Portal copies of the statements of all witnesses of "
                        + "fact on whom they intend to rely.")
                .input2("3")
                .input3("3")
                .input4("For this limitation, a party is counted as a witness.")
                .input5("Each witness statement should be no more than")
                .input6("10")
                .input7("A4 pages. Statements should be double spaced using a font size of 12.")
                .input8("Witness statements shall be uploaded to the Digital Portal by 4pm on")
                .date(workingDayIndicator.getNextWorkingDay(LocalDate.now().plusWeeks(8)))
                .input9("Evidence will not be permitted at trial from a witness whose statement has not been uploaded "
                        + "in accordance with this Order. Evidence not uploaded, or uploaded late, will not be "
                        + "permitted except with permission from the Court.")
                .build();
    }
}
