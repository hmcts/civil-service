package uk.gov.hmcts.reform.civil.service.camunda;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dmnacourttasklocation.DmnListingLocations;
import uk.gov.hmcts.reform.civil.model.dmnacourttasklocation.DmnListingLocationsModel;
import uk.gov.hmcts.reform.civil.model.dmnacourttasklocation.TaskManagementLocationTypes;
import uk.gov.hmcts.reform.civil.model.dmnacourttasklocation.TaskManagementLocationsModel;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
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

    @Mock
    private FeatureToggleService featureToggleService;

    private final TaskManagementLocationTypes testTaskManagementLocations = TaskManagementLocationTypes.builder()
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

    @BeforeEach
    void setUp() {
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("Trial", Map.of(
            "type", "String",
            "value", "123456",
            "valueInfo", Map.of()));
        testMap.put("CMC", Map.of(
            "type", "String",
            "value", "123456",
            "valueInfo", Map.of()));
        testMap.put("CCMC", Map.of(
            "type", "String",
            "value", "123456",
            "valueInfo", Map.of()));
        testMap.put("PTR", Map.of(
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

    @ParameterizedTest
    @ValueSource(strings = {"fast, small"})
    void shouldNotEvaluateWALocations_andShouldClearAnyPreviousEvaluatedCourtLocations(String track) {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("12345").region("1").build())
            .allocatedTrack(Objects.equals(track, "fast") ? AllocatedTrack.FAST_CLAIM : AllocatedTrack.SMALL_CLAIM)
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .taskManagementLocations(testTaskManagementLocations)
            .build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        updateWaCourtLocationsService.updateCourtListingWALocations("auth", caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();

        assertNull(updatedCaseData.getTaskManagementLocations());
        verifyNoInteractions(camundaClient);
    }

    @Test
    void shouldUpdateCourtListingWALocations_whenCourtFound_Unspec() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("12345").region("1").build())
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .allocatedTrack(AllocatedTrack.MULTI_CLAIM)
            .build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        updateWaCourtLocationsService.updateCourtListingWALocations( "auth", caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();

        assertEquals(updatedCaseData.getTaskManagementLocations(), testTaskManagementLocations);
    }

    @Test
    void shouldUpdateCourtListingWALocations_whenCourtFound_Spec() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("12345").region("1").build())
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .responseClaimTrack("INTERMEDIATE_CLAIM")
            .build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        updateWaCourtLocationsService.updateCourtListingWALocations("auth", caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();

        assertEquals(updatedCaseData.getTaskManagementLocations(), testTaskManagementLocations);
    }

    @Test
    void shouldNotUpdateCourtListingWALocations_whenCourtNotFound() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
        Map<String, Object> emptyMap = new HashMap<>();
        when(camundaClient.getEvaluatedDmnCourtLocations(anyString(), anyString())).thenReturn(emptyMap);
        when(objectMapper.convertValue(emptyMap, DmnListingLocations.class)).thenReturn(null);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        updateWaCourtLocationsService.updateCourtListingWALocations( "auth", caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();

        assertEquals(updatedCaseData.getTaskManagementLocations(), null);

    }

    @Test
    void shouldThrowError_whenCourtNotFoundInHearingList() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(true);
        List<LocationRefData> locations = List.of(
            LocationRefData.builder().epimmsId("xxxxx").region("south").regionId("1").venueName("london somewhere").build(),
            LocationRefData.builder().epimmsId("yyyyy").region("north").regionId("2").venueName("liverpool somewhere").build(),
            LocationRefData.builder().epimmsId("zzzzz").region("west").regionId("3").venueName("stoke somewhere").build()
        );

        when(locationRefDataService.getHearingCourtLocations(anyString())).thenReturn(locations);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("12345").region("1").build())
            .allocatedTrack(AllocatedTrack.INTERMEDIATE_CLAIM)
            .build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        assertThrows(IllegalArgumentException.class, () -> updateWaCourtLocationsService
            .updateCourtListingWALocations( "auth", caseDataBuilder));
    }

    @Test
    void shouldNotUpdateEvaluateDmn_whenNonMintiCase() {
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(any())).thenReturn(false);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("12345").region("1").build())
            .allocatedTrack(AllocatedTrack.FAST_CLAIM)
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .build();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        updateWaCourtLocationsService.updateCourtListingWALocations("auth", caseDataBuilder);
        CaseData updatedCaseData = caseDataBuilder.build();

        assertNull(updatedCaseData.getTaskManagementLocations());
        verifyNoInteractions(camundaClient);
    }
}
