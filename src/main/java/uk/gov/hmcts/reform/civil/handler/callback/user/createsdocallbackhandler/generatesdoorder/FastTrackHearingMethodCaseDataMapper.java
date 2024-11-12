package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.generatesdoorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.model.CaseData;

@Slf4j
@Component
public class FastTrackHearingMethodCaseDataMapper implements GenerateSdoOrderCaseDataMapper {

    @Override
    public void mapHearingMethodFields(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (caseData.getHearingMethodValuesFastTrack() != null
                && caseData.getHearingMethodValuesFastTrack().getValue() != null) {

            String fastTrackHearingMethodLabel = caseData.getHearingMethodValuesFastTrack().getValue().getLabel();
            log.debug("Handling Fast Track Hearing Method: {}", fastTrackHearingMethodLabel);
            if (fastTrackHearingMethodLabel.equals(HearingMethod.IN_PERSON.getLabel())) {
                updatedData.fastTrackMethod(FastTrackMethod.fastTrackMethodInPerson);
            } else if (fastTrackHearingMethodLabel.equals(HearingMethod.VIDEO.getLabel())) {
                updatedData.fastTrackMethod(FastTrackMethod.fastTrackMethodVideoConferenceHearing);
            } else if (fastTrackHearingMethodLabel.equals(HearingMethod.TELEPHONE.getLabel())) {
                updatedData.fastTrackMethod(FastTrackMethod.fastTrackMethodTelephoneHearing);
            }
        }
    }
}
