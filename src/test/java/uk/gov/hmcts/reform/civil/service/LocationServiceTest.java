package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.civil.testsupport.mockito.MockitoBean;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @MockitoBean
    private CoreCaseEventDataService coreCaseEventDataService;

    @MockitoBean
    private LocationReferenceDataService locationRefDataService;

    @MockitoBean
    private FeatureToggleService featureToggleService;

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
    void shouldThrowException_whenApplicationMadeAfterSDOMainCaseCMLNotInRefDataQMoff() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getCaseDataForWorkAllocation(CaseState.JUDICIAL_REFERRAL, SPEC_CLAIM, INDIVIDUAL, null, respondent1DQ,
                                          respondent2DQ
            );
        when(locationRefDataService.getCourtLocationsByEpimmsIdWithCML(any(), any())).thenReturn(new ArrayList<>(List.of()));

        assertThrows(IllegalArgumentException.class, () -> service.getWorkAllocationLocation(caseData, "authToken"));
    }

    @Test
    void shouldThrowException_whenApplicationMadeAfterSDOMainCaseCMLNotInRefDataQMOn() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getCaseDataForWorkAllocation(CaseState.JUDICIAL_REFERRAL, SPEC_CLAIM, INDIVIDUAL, null, respondent1DQ,
                                          respondent2DQ
            );
        when(locationRefDataService.getCourtLocationsByEpimmsIdWithCML(any(), any())).thenReturn(new ArrayList<>(List.of()));

        assertThrows(IllegalArgumentException.class, () -> service.getWorkAllocationLocation(caseData, "authToken"));
    }

    @Test
    void shouldThrowException_whenApplicationMadeAfterSDOMainCaseCMLNotInRefDataCaseDiscontinued() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getCaseDataForWorkAllocation(CaseState.CASE_DISCONTINUED, SPEC_CLAIM, INDIVIDUAL, null, respondent1DQ,
                                          respondent2DQ).toBuilder()
            .previousCCDState(null).build();
        when(locationRefDataService.getCourtLocationsByEpimmsIdWithCML(any(), any())).thenReturn(new ArrayList<>(List.of()));

        assertThrows(IllegalArgumentException.class, () -> service.getWorkAllocationLocation(caseData, "authToken"));
    }

    @Test
    void shouldThrowException_whenApplicationMadeAfterSDOMainCaseCMLNotInCaseData() {
        CaseData caseData = CaseDataBuilder.builder().build();
        when(locationRefDataService.getCourtLocationsByEpimmsIdWithCML(any(), any())).thenReturn(new ArrayList<>(List.of()));

        assertThrows(IllegalArgumentException.class, () -> service.getWorkAllocationLocation(caseData, "authToken"));
    }

    @Test
    void shouldNotThrowException_whenApplicationMadeAfterSDOMainCaseCMLInRefData() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
            .getCaseDataForWorkAllocation(CaseState.CASE_DISCONTINUED, SPEC_CLAIM, INDIVIDUAL, null, respondent1DQ,
                                          respondent2DQ).toBuilder()
            .previousCCDState(null).build();
        when(locationRefDataService.getCourtLocationsByEpimmsIdWithCML(any(), any())).thenReturn(getSampleCourLocationsRefObjectPostSdoNotInRefData());

        assertEquals(getExpectedGACaseLocation(), service.getWorkAllocationLocation(caseData, "authToken").getLeft());
    }

    protected List<LocationRefData> getSampleCourLocationsRefObjectPostSdoNotInRefData() {
        return new ArrayList<>(List.of(
            LocationRefData.builder()
                .epimmsId("xxxxx")
                .siteName("xxxxx")
                .courtAddress("xxxxx")
                .postcode("xxxxx")
                .regionId("xxxxx")
                .courtLocationCode("xxxxx")
                .build()
        ));
    }

    protected  uk.gov.hmcts.reform.civil.model.genapplication.CaseLocationCivil getExpectedGACaseLocation() {
        return uk.gov.hmcts.reform.civil.model.genapplication.CaseLocationCivil.builder()
            .region("xxxxx")
            .baseLocation("xxxxx")
            .siteName("xxxxx")
            .address("xxxxx")
            .postcode("xxxxx")
            .build();
    }
}
