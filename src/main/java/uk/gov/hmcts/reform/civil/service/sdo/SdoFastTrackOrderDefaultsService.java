package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;

@Service
@RequiredArgsConstructor
public class SdoFastTrackOrderDefaultsService {

    private final SdoFastTrackNarrativeService sdoFastTrackNarrativeService;
    private final SdoFastTrackSpecialistDirectionsService specialistDirectionsService;

    public void populateFastTrackOrderDetails(CaseData.CaseDataBuilder<?, ?> updatedData) {
        sdoFastTrackNarrativeService.populateFastTrackNarrative(updatedData);
        specialistDirectionsService.populateSpecialistDirections(updatedData);
    }
}
