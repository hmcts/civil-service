package uk.gov.hmcts.reform.civil.handler.callback.user.createsdo.disposalhearing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdo.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingHearingTime;

import java.time.LocalDate;

@Slf4j
@Component
public class HearingTimeFieldBuilder implements SdoCaseFieldBuilder {

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Setting hearing time for caseId: {}", updatedData.build().getCcdCaseReference());
        updatedData.disposalHearingHearingTime(DisposalHearingHearingTime.builder()
                .input(
                        "This claim will be listed for final disposal before a judge on the first "
                                + "available date after")
                .dateTo(LocalDate.now().plusWeeks(16))
                .build());
    }
}
