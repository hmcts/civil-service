package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bankholidays.WorkingDayIndicator;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;

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
    private WorkingDayIndicator workingDayIndicator;
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
        service = new SdoPrePopulateService(
            workingDayIndicator,
            deadlinesCalculator,
            locationHelper,
            locationService,
            featureToggleService,
            categoryService
        );
        service.ccmccAmount = BigDecimal.valueOf(1000);
        service.ccmccEpimsId = "EPIMS123";

        when(workingDayIndicator.getNextWorkingDay(any(LocalDate.class)))
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
        lenient().when(locationService.buildLocationList(ArgumentMatchers.nullable(RequestedCourt.class), anyBoolean(), ArgumentMatchers.<LocationRefData>anyList()))
            .thenAnswer(invocation -> dynamicList("loc", "Location"));
        lenient().when(locationService.buildCourtLocationForSdoR2(ArgumentMatchers.nullable(RequestedCourt.class), ArgumentMatchers.<LocationRefData>anyList()))
            .thenAnswer(invocation -> dynamicList("court", "Court"));
        lenient().when(locationService.buildAlternativeCourtLocations(ArgumentMatchers.<LocationRefData>anyList()))
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

    private DirectionsOrderTaskContext buildContext(CaseData caseData) {
        CallbackParams params = CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, AUTH_TOKEN))
            .build();
        return new DirectionsOrderTaskContext(caseData, params, DirectionsOrderLifecycleStage.PRE_POPULATE);
    }

    private CaseData baseCaseData() {
        return CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("EPIMS123").build())
            .totalClaimAmount(BigDecimal.valueOf(500))
            .build();
    }

    private DynamicList dynamicList(String code, String label) {
        DynamicListElement element = DynamicListElement.builder()
            .code(code)
            .label(label)
            .build();
        return DynamicList.builder()
            .value(element)
            .listItems(List.of(element))
            .build();
    }
}
