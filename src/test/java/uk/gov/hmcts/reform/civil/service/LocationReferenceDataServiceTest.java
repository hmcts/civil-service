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

        loc1 = LocationRefData.builder()
            .siteName("Site A")
            .welshSiteName("Welsh A")
            .courtAddress("Address A")
            .postcode("AA1 1AA")
            .region("England")
            .regionId("1")
            .epimmsId("111")
            .courtLocationCode("AAA")
            .courtStatus("Open")
            .isCaseManagementLocation("Y")
            .isHearingLocation("Y")
            .build();

        loc2 = LocationRefData.builder()
            .siteName("Site B")
            .courtAddress("Address B")
            .postcode("BB1 1BB")
            .region("Wales")
            .regionId("2")
            .epimmsId("222")
            .courtLocationCode("BBB")
            .courtStatus("Open")
            .isCaseManagementLocation("Y")
            .isHearingLocation("Y")
            .build();

        loc3 = LocationRefData.builder()
            .siteName("Site C")
            .courtAddress("Address C")
            .postcode("CC1 1CC")
            .region("Scotland")
            .epimmsId("333")
            .courtLocationCode("CCC")
            .courtStatus("Closed")
            .isCaseManagementLocation("N")
            .isHearingLocation("N")
            .build();
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
        LocationRefData duplicateLoc = LocationRefData.builder()
            .siteName("Site D")
            .courtAddress("Address D")
            .postcode("DD1 1DD")
            .epimmsId("444")
            .courtLocationCode("AAA") // same as loc1
            .courtStatus("Open")
            .isCaseManagementLocation("Y")
            .isHearingLocation("Y")
            .build();

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
