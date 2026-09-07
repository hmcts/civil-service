package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LocationRefDataUtilTest {

    @InjectMocks
    private LocationRefDataUtil locationRefDataUtil;
    @MockBean
    private LocationReferenceDataService locationRefDataService;
    private static final String BEARER_TOKEN = "Bearer Token";

    @BeforeEach
    void setup() {
        locationRefDataService = mock(LocationReferenceDataService.class);
        locationRefDataUtil = new LocationRefDataUtil(locationRefDataService);
    }

    @ParameterizedTest
    @MethodSource("provideCategoryData")
    void shouldReturnPreferredCourtCodeFromRefDataWhenCaseLocationIsPresent(CaseCategory caseCategory, String serviceId) {
        CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful()
            .caseAccessCategory(caseCategory)
            .courtLocation()
            .build();
        List<LocationRefData> courtLocations = new ArrayList<>();
        courtLocations.add(new LocationRefData().setSiteName("SiteName").setCourtAddress("1").setPostcode("1")
                               .setCourtName("Court Name").setRegion("Region").setRegionId("4").setCourtVenueId("000")
                               .setCourtTypeId("10").setCourtLocationCode("121")
                               .setEpimmsId("000000"));
        when(locationRefDataService.getCourtLocationsByEpimmsIdAndCourtType(
            BEARER_TOKEN,
            "000000",
            serviceId
        )).thenReturn(courtLocations);
        String preferredCourtCode = locationRefDataUtil.getPreferredCourtData(caseData,
                                                                              BEARER_TOKEN, true);
        if ("AAA7".equals(serviceId)) {
            assertEquals("121", preferredCourtCode);
        } else {
            assertEquals("", preferredCourtCode);
        }
    }

    private static Stream<Arguments> provideCategoryData() {
        return Stream.of(
            Arguments.of(CaseCategory.UNSPEC_CLAIM, "AAA7"),
            Arguments.of(CaseCategory.SPEC_CLAIM, "AAA6")
        );
    }

    @Test
    void shouldReturnApplicantPreferredCourtCodeWhenCaseLocationIsNotPresent() {
        CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful()
            .courtLocation_old()
            .build();
        List<LocationRefData> courtLocations = new ArrayList<>();
        courtLocations.add(new LocationRefData().setSiteName("SiteName").setCourtAddress("1").setPostcode("1")
                               .setCourtName("Court Name").setRegion("Region").setRegionId("4").setCourtVenueId("000")
                               .setCourtTypeId("10").setCourtLocationCode("127")
                               .setEpimmsId("000000"));
        when(locationRefDataService.getCourtLocationsByEpimmsId(any(), any(), any())).thenReturn(courtLocations);
        String preferredCourtCode = locationRefDataUtil.getPreferredCourtData(caseData,
                                                                              BEARER_TOKEN, true);
        assertEquals("127", preferredCourtCode);
    }

    @Test
    void shouldReturnEmptyStringWhenPreferredCourtCodeNotAvailableFromRefData() {
        CaseData caseData = CaseDataBuilder.builder().atStatePaymentSuccessful()
            .courtLocation()
            .build();
        List<LocationRefData> courtLocations = new ArrayList<>();
        courtLocations.add(new LocationRefData().setSiteName("SiteName").setCourtAddress("1").setPostcode("1")
                               .setCourtName("Court Name").setRegion("Region").setRegionId("4").setCourtVenueId("000")
                               .setCourtTypeId("10")
                               .setEpimmsId("121212"));
        when(locationRefDataService.getCourtLocationsByEpimmsId(any(), any(), any())).thenReturn(courtLocations);
        String preferredCourtCode = locationRefDataUtil.getPreferredCourtData(caseData,
                                                                              BEARER_TOKEN, true);
        assertEquals("", preferredCourtCode);
    }
}
