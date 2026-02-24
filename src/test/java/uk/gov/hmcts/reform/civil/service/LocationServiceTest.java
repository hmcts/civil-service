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

    @MockBean
    private CoreCaseEventDataService coreCaseEventDataService;

    @MockBean
    private LocationReferenceDataService locationRefDataService;

    @MockBean
    private FeatureToggleService featureToggleService;

    private static final Respondent1DQ respondent1DQ;
    private static final Respondent2DQ respondent2DQ;

    static {
        CaseLocationCivil location1 = new CaseLocationCivil();
        location1.setRegion("2");
        location1.setBaseLocation("11111");
        RequestedCourt requestedCourt1 = new RequestedCourt();
        requestedCourt1.setResponseCourtCode("respondent1DQRequestedCourt");
        requestedCourt1.setCaseLocation(location1);
        respondent1DQ = new Respondent1DQ();
        respondent1DQ.setRespondent1DQRequestedCourt(requestedCourt1);

        CaseLocationCivil location2 = new CaseLocationCivil();
        location2.setRegion("3");
        location2.setBaseLocation("22222");
        RequestedCourt requestedCourt2 = new RequestedCourt();
        requestedCourt2.setResponseCourtCode("respondent2DQRequestedCourt");
        requestedCourt2.setCaseLocation(location2);
        respondent2DQ = new Respondent2DQ();
        respondent2DQ.setRespondent2DQRequestedCourt(requestedCourt2);
    }

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
            new LocationRefData()
                .setEpimmsId("xxxxx")
                .setSiteName("xxxxx")
                .setCourtAddress("xxxxx")
                .setPostcode("xxxxx")
                .setRegionId("xxxxx")
                .setCourtLocationCode("xxxxx")
        ));
    }

    protected  uk.gov.hmcts.reform.civil.model.genapplication.CaseLocationCivil getExpectedGACaseLocation() {
        uk.gov.hmcts.reform.civil.model.genapplication.CaseLocationCivil caseLocationCivil =
            new uk.gov.hmcts.reform.civil.model.genapplication.CaseLocationCivil();
        caseLocationCivil.setRegion("xxxxx");
        caseLocationCivil.setBaseLocation("xxxxx");
        caseLocationCivil.setSiteName("xxxxx");
        caseLocationCivil.setAddress("xxxxx");
        caseLocationCivil.setPostcode("xxxxx");
        return caseLocationCivil;
    }
}
