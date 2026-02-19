package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

@ExtendWith(MockitoExtension.class)
class SdoPrePopulateServiceTest {

    private static final String AUTH_TOKEN = "auth-token";

    @Mock
    private DeadlinesCalculator deadlinesCalculator;
    @Mock
    private LocationHelper locationHelper;
    @Mock
    private SdoLocationService locationService;
    @Mock
    private SdoFeatureToggleService featureToggleService;
    @Mock
    private CategoryService categoryService;

    private SdoPrePopulateService service;

    @BeforeEach
    void setUp() {
        SdoDeadlineService deadlineService = new SdoDeadlineService(deadlinesCalculator);
        SdoJourneyToggleService journeyToggleService = new SdoJourneyToggleService(featureToggleService);
        SdoChecklistService checklistService = new SdoChecklistService(journeyToggleService);
        SdoJudgementDeductionService judgementDeductionService = new SdoJudgementDeductionService();
        SdoDisposalOrderDefaultsService disposalOrderDefaultsService = new SdoDisposalOrderDefaultsService(
            new SdoDisposalNarrativeService(deadlineService)
        );
        SdoFastTrackSpecialistDirectionsService fastTrackSpecialistDirectionsService =
            new SdoFastTrackSpecialistDirectionsService(deadlineService, true);
        SdoFastTrackNarrativeService fastTrackNarrativeService = new SdoFastTrackNarrativeService(deadlineService);
        SdoFastTrackOrderDefaultsService fastTrackOrderDefaultsService =
            new SdoFastTrackOrderDefaultsService(fastTrackNarrativeService, fastTrackSpecialistDirectionsService);
        SdoSmallClaimsOrderDefaultsService smallClaimsOrderDefaultsService =
            new SdoSmallClaimsOrderDefaultsService(
                new SdoSmallClaimsNarrativeService(deadlineService),
                journeyToggleService
            );

        SdoExpertEvidenceFieldsService expertEvidenceFieldsService = new SdoExpertEvidenceFieldsService(deadlineService);
        SdoDisclosureOfDocumentsFieldsService disclosureOfDocumentsFieldsService =
            new SdoDisclosureOfDocumentsFieldsService(deadlineService);

        SdoTrackDefaultsService trackDefaultsService = new SdoTrackDefaultsService(
                journeyToggleService,
                checklistService,
                disposalOrderDefaultsService,
                fastTrackOrderDefaultsService,
                smallClaimsOrderDefaultsService,
                expertEvidenceFieldsService,
                disclosureOfDocumentsFieldsService,
                judgementDeductionService
        );
        SdoHearingPreparationService hearingPreparationService = new SdoHearingPreparationService(
                locationHelper,
                locationService,
                categoryService
        );
        hearingPreparationService.ccmccAmount = BigDecimal.valueOf(1000);
        hearingPreparationService.ccmccEpimsId = "EPIMS123";

        SdoDrhFieldsService drhFieldsService = new SdoDrhFieldsService(locationService, trackDefaultsService,
                journeyToggleService, deadlineService);
        SdoNihlFieldsService nihlFieldsService = new SdoNihlFieldsService(locationService, new SdoNihlOrderService(deadlineService));

        service = new SdoPrePopulateService(
                trackDefaultsService,
                hearingPreparationService,
                drhFieldsService,
                nihlFieldsService
        );

        when(deadlinesCalculator.calculateFirstWorkingDay(any(LocalDate.class)))
            .thenAnswer(invocation -> invocation.getArgument(0, LocalDate.class));
        when(deadlinesCalculator.plusWorkingDays(any(LocalDate.class), ArgumentMatchers.anyInt()))
            .thenAnswer(invocation -> {
                LocalDate date = invocation.getArgument(0, LocalDate.class);
                int days = invocation.getArgument(1, Integer.class);
                return date.plusDays(days);
            });
        when(deadlinesCalculator.getOrderSetAsideOrVariedApplicationDeadline(any(LocalDateTime.class)))
            .thenAnswer(invocation -> invocation.getArgument(0, LocalDateTime.class).toLocalDate());
        lenient().when(locationService.fetchHearingLocations(anyString())).thenReturn(Collections.emptyList());
        lenient().when(locationService.fetchCourtLocationsByEpimmsId(anyString(), anyString())).thenReturn(Collections.emptyList());
        lenient().when(locationService.buildLocationList(ArgumentMatchers.nullable(RequestedCourt.class), anyBoolean(), ArgumentMatchers.anyList()))
            .thenAnswer(invocation -> dynamicList("loc", "Location"));
        lenient().when(locationService.buildCourtLocationForSdoR2(ArgumentMatchers.nullable(RequestedCourt.class), ArgumentMatchers.anyList()))
            .thenAnswer(invocation -> dynamicList("court", "Court"));
        lenient().when(locationService.buildAlternativeCourtLocations(ArgumentMatchers.anyList()))
            .thenAnswer(invocation -> dynamicList("alt", "Alt Court"));
        when(categoryService.findCategoryByCategoryIdAndServiceId(anyString(), anyString(), anyString()))
            .thenReturn(Optional.empty());
        when(locationHelper.getCaseManagementLocationWhenLegalAdvisorSdo(any(CaseData.class)))
            .thenReturn(Optional.empty());
    }

    @Test
    void shouldSetCarmFlagAndBuildLocationLists() {
        when(featureToggleService.isCarmEnabled(any(CaseData.class))).thenReturn(true);

        CaseData caseData = baseCaseData();
        DirectionsOrderTaskContext context = buildContext(caseData);

        CaseData result = service.prePopulate(context);

        assertThat(result.getShowCarmFields()).isEqualTo(YesOrNo.YES);
        assertThat(result.getDisposalHearingMethodInPerson()).isNotNull();
        assertThat(result.getFastTrackMethodInPerson()).isNotNull();
        assertThat(result.getSmallClaimsMethodInPerson()).isNotNull();
        verify(locationService).fetchHearingLocations(AUTH_TOKEN);
    }

    @Test
    void shouldClearCarmFlagWhenToggleDisabled() {
        when(featureToggleService.isCarmEnabled(any(CaseData.class))).thenReturn(false);

        CaseData caseData = baseCaseData();
        DirectionsOrderTaskContext context = buildContext(caseData);

        CaseData result = service.prePopulate(context);

        assertThat(result.getShowCarmFields()).isEqualTo(YesOrNo.NO);
    }

    @Test
    void shouldSetWelshHintWhenWelshJourneyEnabled() {
        when(featureToggleService.isCarmEnabled(any(CaseData.class))).thenReturn(false);
        when(featureToggleService.isWelshJourneyEnabled(any(CaseData.class))).thenReturn(true);

        CaseData caseData = baseCaseData();
        DirectionsOrderTaskContext context = buildContext(caseData);

        CaseData result = service.prePopulate(context);

        assertThat(result.getBilingualHint()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldPopulateMediationSectionWhenCarmEnabled() {
        when(featureToggleService.isCarmEnabled(any(CaseData.class))).thenReturn(true);
        when(featureToggleService.isWelshJourneyEnabled(any(CaseData.class))).thenReturn(false);

        CaseData caseData = baseCaseData();
        DirectionsOrderTaskContext context = buildContext(caseData);

        CaseData result = service.prePopulate(context);

        assertThat(result.getSmallClaimsMediationSectionStatement()).isNotNull();
    }

    @Test
    void shouldPopulateLocationNameWhenLegalAdvisorRoute() {
        CaseData caseData = baseCaseData();
        RequestedCourt requestedCourt = new RequestedCourt();
        requestedCourt.setCaseLocation(new CaseLocationCivil().setBaseLocation("EPIMS999"));
        when(locationHelper.getCaseManagementLocationWhenLegalAdvisorSdo(caseData)).thenReturn(Optional.of(requestedCourt));
        when(locationService.fetchCourtLocationsByEpimmsId(AUTH_TOKEN, "EPIMS999"))
            .thenReturn(List.of(locationRefData()));

        DirectionsOrderTaskContext context = buildContext(caseData);

        CaseData result = service.prePopulate(context);

        assertThat(result.getLocationName()).isEqualTo("Preferred Court");
    }

    private DirectionsOrderTaskContext buildContext(CaseData caseData) {
        CallbackParams params = new CallbackParams()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, AUTH_TOKEN));
        return new DirectionsOrderTaskContext(caseData, params, DirectionsOrderLifecycleStage.PRE_POPULATE);
    }

    private CaseData baseCaseData() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.SPEC_CLAIM);
        caseData.setCaseManagementLocation(new CaseLocationCivil().setBaseLocation("EPIMS123"));
        caseData.setTotalClaimAmount(BigDecimal.valueOf(500));
        return caseData;
    }

    private DynamicList dynamicList(String code, String label) {
        DynamicListElement element = new DynamicListElement();
        element.setCode(code);
        element.setLabel(label);
        DynamicList list = new DynamicList();
        list.setValue(element);
        list.setListItems(List.of(element));
        return list;
    }

    private LocationRefData locationRefData() {
        LocationRefData locationRefData = new LocationRefData();
        locationRefData.setSiteName("Preferred Court");
        return locationRefData;
    }
}
