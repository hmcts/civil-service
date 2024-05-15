package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LocationRefDataUtilTest {

    private static final String BEARER_TOKEN = "Bearer Token";
    @InjectMocks
    private LocationRefDataUtil locationRefDataUtil;
    @MockBean
    private LocationRefDataService locationRefDataService;

    @BeforeEach
    void setup() {
        locationRefDataService = mock(LocationRefDataService.class);
        locationRefDataUtil = new LocationRefDataUtil(locationRefDataService);
    }

    @Test
    void shouldReturnPreferredCourtCodeFromRefDataWhenCaseLocationIsPresent() {
        CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful()
            .courtLocation()
            .build();
        List<LocationRefData> courtLocations = new ArrayList<>();
        courtLocations.add(LocationRefData.builder().siteName("SiteName").courtAddress("1").postcode("1")
                               .courtName("Court Name").region("Region").regionId("4").courtVenueId("000")
                               .courtTypeId("10").courtLocationCode("121")
                               .epimmsId("000000").build());
        when(locationRefDataService.getCourtLocationsByEpimmsIdAndCourtType(any(), any())).thenReturn(courtLocations);
        String preferredCourtCode = locationRefDataUtil.getPreferredCourtData(caseData,
                                                                              BEARER_TOKEN, true
        );
        assertEquals("121", preferredCourtCode);
    }

    @Test
    void shouldReturnApplicantPreferredCourtCodeWhenCaseLocationIsNotPresent() {
        CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful()
            .courtLocation_old()
            .build();

        String preferredCourtCode = locationRefDataUtil.getPreferredCourtData(caseData,
                                                                              BEARER_TOKEN, true
        );
        assertEquals("127", preferredCourtCode);
    }

    @Test
    void shouldReturnEmptyStringWhenPreferredCourtCodeNotAvailableFromRefData() {
        CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful()
            .courtLocation()
            .build();
        String preferredCourtCode = locationRefDataUtil.getPreferredCourtData(caseData,
                                                                              BEARER_TOKEN, true
        );
        assertEquals("", preferredCourtCode);
    }

    @Test
    void shouldReturnApplicantPreferredCourtCodeWhenCaseLocationIsNotPresentLive() {
        CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful()
            .courtLocationLive()
            .build();
        List<LocationRefData> courtLocations = new ArrayList<>();
        courtLocations.add(LocationRefData.builder().siteName("SiteName").courtAddress("1").postcode("1")
                               .courtName("Court Name").region("Region").regionId("4").courtVenueId("000")
                               .courtTypeId("10")
                               .courtLocationCode("223503")
                               .epimmsId("425094").build());
        when(locationRefDataService.getCourtLocationsByEpimmsIdAndCourtType(any(), any()))
            .thenReturn(courtLocations);
        String preferredCourtCode = locationRefDataUtil.getPreferredCourtData(caseData,
                                                                              BEARER_TOKEN, true
        );
        assertEquals("223503", preferredCourtCode);
    }

    @Test
    void getLiveEpimmsIdForCourt() {
        assertEquals("223503", locationRefDataUtil.getLiveCourtByEpimmsId("425094"));
        assertEquals("123456", locationRefDataUtil.getLiveCourtByEpimmsId("123456"));
    }
}
