package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fasttrack;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackTrialBundleType;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackTrial;

import java.time.LocalDate;
import java.util.Collections;

@Slf4j
@Component
public class FastTrackTrialFieldBuilder implements SdoCaseFieldBuilder {

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData.fastTrackTrial(FastTrackTrial.builder()
                .input1("The time provisionally allowed for this trial is")
                .date1(LocalDate.now().plusWeeks(22))
                .date2(LocalDate.now().plusWeeks(30))
                .input2(
                        "If either party considers that the time estimate is insufficient, they must inform the court within 7 days of the date stated on this order.")
                .input3(
                        "At least 7 days before the trial, the claimant must upload to the Digital Portal")
                .type(Collections.singletonList(FastTrackTrialBundleType.DOCUMENTS))
                .build());
    }
}
