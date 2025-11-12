package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SdoNihlFieldsService {

    private final SdoLocationService sdoLocationService;
    private final SdoNihlOrderService sdoNihlOrderService;

    public void populateNihlFields(CaseData.CaseDataBuilder<?, ?> updatedData,
                                   DynamicList hearingMethodList,
                                   Optional<RequestedCourt> preferredCourt,
                                   List<LocationRefData> locationRefDataList) {


        DynamicListElement hearingMethodInPerson = hearingMethodList.getListItems().stream().filter(elem -> elem.getLabel()
            .equals(HearingMethod.IN_PERSON.getLabel())).findFirst().orElse(null);
        hearingMethodList.setValue(hearingMethodInPerson);
        DynamicList trialCourtList = sdoLocationService.buildCourtLocationForSdoR2(preferredCourt.orElse(null), locationRefDataList);
        if (trialCourtList != null && trialCourtList.getListItems() != null && !trialCourtList.getListItems().isEmpty()) {
            trialCourtList.setValue(trialCourtList.getListItems().get(0));
        }
        DynamicList alternativeCourtLocations = sdoLocationService.buildAlternativeCourtLocations(locationRefDataList);

        sdoNihlOrderService.populateStandardDirections(
            updatedData,
            hearingMethodList,
            trialCourtList,
            alternativeCourtLocations
        );
    }
}
