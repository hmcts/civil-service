package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.generatesdoorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.model.CaseData;

@Slf4j
@Component
public class DisposalHearingMethodCaseDataMapper implements GenerateSdoOrderCaseDataMapper {

    @Override
    public void mapHearingMethodFields(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        if (caseData.getHearingMethodValuesDisposalHearing() != null
                && caseData.getHearingMethodValuesDisposalHearing().getValue() != null) {

            String disposalHearingMethodLabel = caseData.getHearingMethodValuesDisposalHearing().getValue().getLabel();
            log.debug("Handling Disposal Hearing Method: {}", disposalHearingMethodLabel);
            if (disposalHearingMethodLabel.equals(HearingMethod.IN_PERSON.getLabel())) {
                updatedData.disposalHearingMethod(DisposalHearingMethod.disposalHearingMethodInPerson);
            } else if (disposalHearingMethodLabel.equals(HearingMethod.VIDEO.getLabel())) {
                updatedData.disposalHearingMethod(DisposalHearingMethod.disposalHearingMethodVideoConferenceHearing);
            } else if (disposalHearingMethodLabel.equals(HearingMethod.TELEPHONE.getLabel())) {
                updatedData.disposalHearingMethod(DisposalHearingMethod.disposalHearingMethodTelephoneHearing);
            }
        }
    }
}
