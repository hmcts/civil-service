package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.referencedata.LocationRefDataException;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.refdata.CourtVenueService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationReferenceDataServiceTest {

    @Mock
    private CourtVenueService courtVenueService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private LocationReferenceDataService service;

    private final String auth = "auth";
    private final String generatedAuth = "generatedAuth";

    private LocationRefData loc1;
    private LocationRefData loc2;
    private LocationRefData loc3;

    @BeforeEach
    void setup() {
        lenient().when(authTokenGenerator.generate()).thenReturn(generatedAuth);

        loc1 = new LocationRefData()
            .setSiteName("Site A")
            .setWelshSiteName("Welsh A")
            .setCourtAddress("Address A")
            .setPostcode("AA1 1AA")
            .setRegion("England")
            .setRegionId("1")
            .setEpimmsId("111")
            .setCourtLocationCode("AAA")
            .setCourtStatus("Open")
            .setIsCaseManagementLocation("Y")
            .setIsHearingLocation("Y");

        loc2 = new LocationRefData()
            .setSiteName("Site B")
            .setCourtAddress("Address B")
            .setPostcode("BB1 1BB")
            .setRegion("Wales")
            .setRegionId("2")
            .setEpimmsId("222")
            .setCourtLocationCode("BBB")
            .setCourtStatus("Open")
            .setIsCaseManagementLocation("Y")
            .setIsHearingLocation("Y");

        loc3 = new LocationRefData()
            .setSiteName("Site C")
            .setCourtAddress("Address C")
            .setPostcode("CC1 1CC")
            .setRegion("Scotland")
            .setEpimmsId("333")
            .setCourtLocationCode("CCC")
            .setCourtStatus("Closed")
            .setIsCaseManagementLocation("N")
            .setIsHearingLocation("N");
    }

    @Test
    void shouldReturnCNBcLocation() {
        when(courtVenueService.getCourtVenueByName(generatedAuth, auth,
                                                   LocationReferenceDataService.CIVIL_NATIONAL_BUSINESS_CENTRE))
            .thenReturn(List.of(loc1));

        LocationRefData result = service.getCnbcLocation(auth);

        assertThat(result).isEqualTo(loc1);
    }

    @Test
    void shouldReturnEmptyWhenCNBcNotFound() {
        when(courtVenueService.getCourtVenueByName(any(), any(), any()))
            .thenReturn(List.of());

        LocationRefData result = service.getCnbcLocation(auth);

        assertThat(result.getSiteName()).isNull();
    }

    @Test
    void shouldReturnCcmccLocation() {
        when(courtVenueService.getCourtVenueByName(generatedAuth, auth,
                                                   LocationReferenceDataService.COUNTY_COURT_MONEY_CLAIMS_CENTRE))
            .thenReturn(List.of(loc2));

        LocationRefData result = service.getCcmccLocation(auth);

        assertThat(result).isEqualTo(loc2);
    }

    @Test
    void shouldReturnCourtsForDefaultJudgments() {
        when(courtVenueService.getCMLAndHLCourts(generatedAuth, auth))
            .thenReturn(List.of(loc1, loc2));

        List<LocationRefData> result = service.getCourtLocationsForDefaultJudgments(auth);

        assertThat(result).containsExactly(loc1, loc2);
    }

    @Test
    void shouldReturnSortedEnglandAndWalesCourtsForGA() {
        when(courtVenueService.getCMLAndHLCourts(generatedAuth, auth))
            .thenReturn(List.of(loc1, loc2, loc3));

        List<LocationRefData> result = service.getCourtLocationsForGeneralApplication(auth);

        assertThat(result).containsExactly(loc1, loc2);
        assertThat(result.get(0).getSiteName()).isEqualTo("Site A");
    }

    @Test
    void shouldReturnCourtLocationsByEpimmsId() {
        when(courtVenueService.getCourtByEpimmsId(generatedAuth, auth, "111"))
            .thenReturn(List.of(loc1));

        List<LocationRefData> result = service.getCourtLocationsByEpimmsId(auth, "111");

        assertThat(result).containsExactly(loc1);
    }

    @Test
    void shouldReturnCourtLocationsByEpimmsIdWithCML() {
        when(courtVenueService.getCMLCourtByEpimmsId(generatedAuth, auth, "111"))
            .thenReturn(List.of(loc1));

        List<LocationRefData> result = service.getCourtLocationsByEpimmsIdWithCML(auth, "111");

        assertThat(result).containsExactly(loc1);
    }

    @Test
    void shouldReturnHearingLocationCourts() {
        when(courtVenueService.getHearingLocationCourts(generatedAuth, auth))
            .thenReturn(List.of(loc1, loc2));

        List<LocationRefData> result = service.getHearingCourtLocations(auth);

        assertThat(result).containsExactly(loc1, loc2);
    }

    @Test
    void shouldReturnMatchingLabelLocation() {
        when(courtVenueService.getHearingLocationCourts(generatedAuth, auth))
            .thenReturn(List.of(loc1));

        String label = LocationReferenceDataService.getDisplayEntry(loc1);

        Optional<LocationRefData> result = service.getLocationMatchingLabel(label, auth);

        assertThat(result).contains(loc1);
    }

    @Test
    void shouldReturnEmptyWhenLabelBlank() {
        Optional<LocationRefData> result = service.getLocationMatchingLabel("", auth);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldBuildDisplayEntry() {
        String label = LocationReferenceDataService.getDisplayEntry(loc1);
        assertThat(label).isEqualTo("Site A - Address A - AA1 1AA");
    }

    @Test
    void shouldBuildDisplayEntryWelsh() {
        String label = LocationReferenceDataService.getDisplayEntryWelsh(loc1);
        assertThat(label).isEqualTo("Welsh A - Address A - AA1 1AA");
    }

    @Test
    void shouldReturnCourtLocationByThreeDigitCode() {
        when(courtVenueService.getCourtVenueByLocationCode(generatedAuth, auth, "AAA"))
            .thenReturn(List.of(loc1));

        LocationRefData result = service.getCourtLocation(auth, "AAA");

        assertThat(result).isEqualTo(loc1);
    }

    @Test
    void shouldReturnEmptyWhenNoCourtFoundByThreeDigitCode() {
        when(courtVenueService.getCourtVenueByLocationCode(any(), any(), any()))
            .thenReturn(List.of());

        LocationRefData result = service.getCourtLocation(auth, "AAA");

        assertThat(result.getSiteName()).isNull();
    }

    @Test
    void shouldThrowExceptionWhenMoreThanOneCourtFound() {
        // Duplicate court with the same location code "AAA"
        LocationRefData duplicateLoc = new LocationRefData()
            .setSiteName("Site D")
            .setCourtAddress("Address D")
            .setPostcode("DD1 1DD")
            .setEpimmsId("444")
            .setCourtLocationCode("AAA") // same as loc1
            .setCourtStatus("Open")
            .setIsCaseManagementLocation("Y")
            .setIsHearingLocation("Y");

        when(courtVenueService.getCourtVenueByLocationCode(any(), any(), any()))
            .thenReturn(List.of(loc1, duplicateLoc));

        assertThatThrownBy(() -> service.getCourtLocation(auth, "AAA"))
            .isInstanceOf(LocationRefDataException.class)
            .hasMessageContaining("More than one court location found");
    }

    @Test
    void shouldThrowExceptionWhenFilterFindsNone() {
        when(courtVenueService.getCourtVenueByLocationCode(any(), any(), any()))
            .thenReturn(List.of(loc2));

        assertThatThrownBy(() -> service.getCourtLocation(auth, "AAA"))
            .isInstanceOf(LocationRefDataException.class)
            .hasMessageContaining("No court Location Found");
    }
}
