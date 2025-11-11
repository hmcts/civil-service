package uk.gov.hmcts.reform.civil.service.directionsorder;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.sdo.SdoFeatureToggleService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoJourneyToggleService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoLocationService;

/**
 * Shared helper used by both SDO and DJ submission flows to keep EA-court toggles
 * and WA location updates consistent while handler-level services remain slim.
 */
@Service
@RequiredArgsConstructor
public class DirectionsOrderCaseProgressionService {

    private final SdoJourneyToggleService sdoJourneyToggleService;
    private final SdoFeatureToggleService featureToggleService;
    private final SdoLocationService sdoLocationService;

    public void applyEaCourtLocation(CaseData caseData, CaseData.CaseDataBuilder<?, ?> builder) {
        YesOrNo resolvedEaCourt = sdoJourneyToggleService.resolveEaCourtLocation(caseData);
        if (resolvedEaCourt != null) {
            builder.eaCourtLocation(resolvedEaCourt);
        }
    }

    public void updateWaLocationsIfEnabled(CaseData caseData,
                                           CaseData.CaseDataBuilder<?, ?> builder,
                                           String authToken) {
        if (!featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)) {
            return;
        }
        sdoLocationService.updateWaLocationsIfRequired(caseData, builder, authToken);
    }
}
