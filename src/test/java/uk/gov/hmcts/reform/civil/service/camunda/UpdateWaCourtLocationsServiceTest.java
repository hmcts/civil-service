package uk.gov.hmcts.reform.civil.service.camunda;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.dmnWaCourtTaskLocation.DmnListingLocations;
import uk.gov.hmcts.reform.civil.model.dmnWaCourtTaskLocation.DmnListingLocationsModel;
import uk.gov.hmcts.reform.civil.model.dmnWaCourtTaskLocation.TaskManagementLocationTypes;
import uk.gov.hmcts.reform.civil.model.dmnWaCourtTaskLocation.TaskManagementLocationsModel;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdateWaCourtLocationsServiceTest {

    @InjectMocks
    private UpdateWaCourtLocationsService updateWaCourtLocationsService;

    @Mock
    private CamundaRuntimeClient camundaClient;

    @Mock
    private LocationReferenceDataService locationRefDataService;

    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("Trial",Map.of(
            "type", "String",
            "value", "123456",
            "valueInfo", Map.of()));
        testMap.put("CMC",Map.of(
            "type", "String",
            "value", "123456",
            "valueInfo", Map.of()));
        testMap.put("CCMC",Map.of(
            "type", "String",
            "value", "123456",
            "valueInfo", Map.of()));
        testMap.put("PTR",Map.of(
            "type", "String",
            "value", "123456",
            "valueInfo", Map.of()));

        List<LocationRefData> locations = List.of(
            LocationRefData.builder().epimmsId("123456").region("south").regionId("1").venueName("london somewhere").build(),
            LocationRefData.builder().epimmsId("654321").region("north").regionId("2").venueName("liverpool somewhere").build(),
            LocationRefData.builder().epimmsId("789654").region("west").regionId("3").venueName("stoke somewhere").build()
        );

        DmnListingLocations dmnListingLocations = DmnListingLocations.builder()
            .cmcListingLocation(DmnListingLocationsModel.builder().type("String").value("123456").valueInfo(null).build())
            .ccmcListingLocation(DmnListingLocationsModel.builder().type("String").value("123456").valueInfo(null).build())
            .ptrListingLocation(DmnListingLocationsModel.builder().type("String").value("654321").valueInfo(null).build())
            .trialListingLocation(DmnListingLocationsModel.builder().type("String").value("789654").valueInfo(null).build())
            .build();

        when(camundaClient.getEvaluatedDmnCourtLocations(anyString(), anyString())).thenReturn(testMap);
        when(locationRefDataService.getHearingCourtLocations(anyString())).thenReturn(locations);
        when(objectMapper.convertValue(testMap, DmnListingLocations.class)).thenReturn(dmnListingLocations);

    }

    @Test
    void shouldUpdateCourtListingWALocations_whenCourtFound_Unspec() {

        TaskManagementLocationTypes testTaskManagementLocations = TaskManagementLocationTypes.builder()
            .cmcListingLocation(TaskManagementLocationsModel.builder()
                                    .locationName("london somewhere")
                                    .location("123456")
                                    .region("1")
                                    .regionName("south")
                                    .build())
            .ccmcListingLocation(TaskManagementLocationsModel.builder()
                                    .locationName("london somewhere")
                                    .location("123456")
                                    .region("1")
                                    .regionName("south")
                                    .build())
            .ptrListingLocation(TaskManagementLocationsModel.builder()
                                    .locationName("liverpool somewhere")
                                    .location("654321")
                                    .region("2")
                                    .regionName("north")
                                    .build())
            .trialListingLocation(TaskManagementLocationsModel.builder()
                                    .locationName("stoke somewhere")
                                    .location("789654")
                                    .region("3")
                                    .regionName("west")
                                    .build())
            .build();

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        updateWaCourtLocationsService.updateCourtListingWALocations("123456", caseData, "auth", caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();

        assertEquals(updatedCaseData.getTaskManagementLocations(), testTaskManagementLocations);
    }

    @Test
    void shouldUpdateCourtListingWALocations_whenCourtFound_Spec() {

        TaskManagementLocationTypes testTaskManagementLocations = TaskManagementLocationTypes.builder()
            .cmcListingLocation(TaskManagementLocationsModel.builder()
                                    .locationName("london somewhere")
                                    .location("123456")
                                    .region("1")
                                    .regionName("south")
                                    .build())
            .ccmcListingLocation(TaskManagementLocationsModel.builder()
                                     .locationName("london somewhere")
                                     .location("123456")
                                     .region("1")
                                     .regionName("south")
                                     .build())
            .ptrListingLocation(TaskManagementLocationsModel.builder()
                                    .locationName("liverpool somewhere")
                                    .location("654321")
                                    .region("2")
                                    .regionName("north")
                                    .build())
            .trialListingLocation(TaskManagementLocationsModel.builder()
                                      .locationName("stoke somewhere")
                                      .location("789654")
                                      .region("3")
                                      .regionName("west")
                                      .build())
            .build();

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .responseClaimTrack("INTERMEDIATE_CLAIM")
            .build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        updateWaCourtLocationsService.updateCourtListingWALocations("123456", caseData, "auth", caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();

        assertEquals(updatedCaseData.getTaskManagementLocations(), testTaskManagementLocations);
    }

    @Test
    void shouldNotUpdateCourtListingWALocations_whenCourtNotFound() {

        Map<String, Object> emptyMap = new HashMap<>();
        when(camundaClient.getEvaluatedDmnCourtLocations(anyString(), anyString())).thenReturn(emptyMap);
        when(objectMapper.convertValue(emptyMap, DmnListingLocations.class)).thenReturn(null);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        updateWaCourtLocationsService.updateCourtListingWALocations("123456", caseData, "auth", caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();

        assertEquals(updatedCaseData.getTaskManagementLocations(), null);

    }

    @Test
    void shouldThrowError_whenCourtNotFoundInHearingList() {
        List<LocationRefData> locations = List.of(
            LocationRefData.builder().epimmsId("xxxxx").region("south").regionId("1").venueName("london somewhere").build(),
            LocationRefData.builder().epimmsId("yyyyy").region("north").regionId("2").venueName("liverpool somewhere").build(),
            LocationRefData.builder().epimmsId("zzzzz").region("west").regionId("3").venueName("stoke somewhere").build()
        );

        when(locationRefDataService.getHearingCourtLocations(anyString())).thenReturn(locations);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        assertThrows(IllegalArgumentException.class, () -> updateWaCourtLocationsService
            .updateCourtListingWALocations("123456", caseData, "auth", caseDataBuilder));
    }
}
