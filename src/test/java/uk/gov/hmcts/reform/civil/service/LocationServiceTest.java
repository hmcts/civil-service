package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.model.Party.Type.INDIVIDUAL;

@SpringBootTest(classes = {
    LocationService.class
})
class LocationServiceTest {

    @Autowired
    private LocationService service;

    @MockBean
    private CoreCaseEventDataService coreCaseEventDataService;

    @MockBean
    private LocationReferenceDataService locationRefDataService;

    private static final Respondent1DQ respondent1DQ =
        Respondent1DQ.builder().respondent1DQRequestedCourt(RequestedCourt.builder()
                                                                .responseCourtCode("respondent1DQRequestedCourt")
                                                                .caseLocation(CaseLocationCivil.builder()
                                                                                  .region("2")
                                                                                  .baseLocation("11111")
                                                                                  .build())
                                                                .build()).build();
    private static final Respondent2DQ respondent2DQ =
        Respondent2DQ.builder().respondent2DQRequestedCourt(RequestedCourt.builder()
                                                                .responseCourtCode("respondent2DQRequestedCourt")
                                                                .caseLocation(CaseLocationCivil.builder()
                                                                                  .region("3")
                                                                                  .baseLocation("22222")
                                                                                  .build())
                                                                .build()).build();

    @Test
    void shouldThrowException_whenApplicationMadeAfterSDOMainCaseCMLNotInRefData() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getCaseDataForWorkAllocation(null, SPEC_CLAIM, INDIVIDUAL, null, respondent1DQ,
                                          respondent2DQ
            );
        when(locationRefDataService.getHearingCourtLocations(any())).thenReturn(getSampleCourLocationsRefObjectPostSdoNotInRefData());

        assertThrows(IllegalArgumentException.class, () -> service.getWorkAllocationLocation(caseData, "authToken"));
    }

    @Test
    void shouldThrowException_whenApplicationMadeAfterSDOMainCaseCMLNotInRefDataCaseDiscontinued() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getCaseDataForWorkAllocation(CaseState.CASE_DISCONTINUED, SPEC_CLAIM, INDIVIDUAL, null, respondent1DQ,
                                          respondent2DQ).toBuilder()
            .previousCCDState(null).build();
        when(locationRefDataService.getHearingCourtLocations(any())).thenReturn(getSampleCourLocationsRefObjectPostSdoNotInRefData());

        assertThrows(IllegalArgumentException.class, () -> service.getWorkAllocationLocation(caseData, "authToken"));
    }

    protected List<LocationRefData> getSampleCourLocationsRefObjectPostSdoNotInRefData() {
        return new ArrayList<>(List.of(
            LocationRefData.builder()
                .epimmsId("xxxxx")
                .siteName("xxxxx")
                .courtAddress("xxxxx")
                .postcode("xxxxx")
                .regionId("xxxxx")
                .courtLocationCode("xxxxx").build()
        ));
    }
}
