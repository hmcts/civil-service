package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.disposalhearing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearing;

import java.time.LocalDate;

@Slf4j
@Component
public class FinalDisposalHearingFieldBuilder implements SdoCaseFieldBuilder {

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Setting final disposal hearing");
        updatedData.disposalHearingFinalDisposalHearing(
            DisposalHearingFinalDisposalHearing.builder()
                .input("This claim will be listed for final disposal before a judge on the first "
                           + "available date after")
                .date(LocalDate.now().plusWeeks(16))
                .build());
    }
}
