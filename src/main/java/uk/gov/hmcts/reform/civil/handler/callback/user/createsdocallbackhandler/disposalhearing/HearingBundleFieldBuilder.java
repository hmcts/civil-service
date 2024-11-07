package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.disposalhearing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingBundle;

@Slf4j
@Component
public class HearingBundleFieldBuilder implements SdoCaseFieldBuilder {

    @Override
    public void build(CaseData.CaseDataBuilder<?, ?> updatedData) {
        log.info("Setting hearing bundle");
        updatedData.disposalHearingBundle(DisposalHearingBundle.builder()
                                              .input(
                                                  "At least 7 days before the disposal hearing, the claimant must file and serve")
                                              .build());
    }
}
