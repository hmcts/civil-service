package uk.gov.hmcts.reform.civil.handler.callback.user.createsdo.fasttrack;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdo.SdoCaseFieldBuilder;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FastTrackPopulator {

    private final List<SdoCaseFieldBuilder> fastTrackBuilders;

    public void setFastTrackFields(CaseData.CaseDataBuilder<?, ?> updatedData) {
        fastTrackBuilders.forEach(disposalHearingBuilder -> disposalHearingBuilder.build(updatedData));
    }
}
