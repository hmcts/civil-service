package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.submitsdo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

@Slf4j
@Component
@RequiredArgsConstructor
public class HearingMethodsFieldUpdater implements SdoCaseDataFieldUpdater {

    private final SubmitSdoUtils submitSdoUtils;

    @Override
    public void update(CaseData caseData, CaseData.CaseDataBuilder<?, ?> dataBuilder) {
        log.debug("Handling hearing methods for case {}", caseData.getCcdCaseReference());
        dataBuilder.disposalHearingMethodInPerson(submitSdoUtils.deleteLocationList(caseData.getDisposalHearingMethodInPerson()));
        dataBuilder.fastTrackMethodInPerson(submitSdoUtils.deleteLocationList(caseData.getFastTrackMethodInPerson()));
        dataBuilder.smallClaimsMethodInPerson(submitSdoUtils.deleteLocationList(caseData.getSmallClaimsMethodInPerson()));
    }
}
