package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SdoFastTrackOrderDefaultsServiceTest {

    @Mock
    private SdoFastTrackNarrativeService fastTrackNarrativeService;
    @Mock
    private SdoFastTrackSpecialistDirectionsService specialistDirectionsService;

    private SdoFastTrackOrderDefaultsService service;

    @BeforeEach
    void setUp() {
        service = new SdoFastTrackOrderDefaultsService(fastTrackNarrativeService, specialistDirectionsService);
    }

    @Test
    void shouldPopulateFastTrackDefaults() {
        CaseData caseData = CaseData.builder().build();

        service.populateFastTrackOrderDetails(caseData);

        verify(fastTrackNarrativeService).populateFastTrackNarrative(caseData);
        verify(specialistDirectionsService).populateSpecialistDirections(caseData);
    }
}
