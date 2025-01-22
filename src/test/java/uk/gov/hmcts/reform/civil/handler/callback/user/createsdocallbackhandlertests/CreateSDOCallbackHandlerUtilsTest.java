package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.CreateSDOCallbackHandlerUtils;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.utils.HearingMethodUtils;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;

@ExtendWith(MockitoExtension.class)
class CreateSDOCallbackHandlerUtilsTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private LocationHelper locationHelper;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CreateSDOCallbackHandlerUtils createSDOCallbackHandlerUtils;

    @Test
    void shouldReturnLocationListWhenMatchingLocationIsPresent() {
        RequestedCourt requestedCourt = new RequestedCourt();
        LocationRefData locationRefData = LocationRefData.builder().build();
        List<LocationRefData> locations = List.of(locationRefData);

        when(locationHelper.getMatching(anyList(), any(RequestedCourt.class))).thenReturn(Optional.of(locationRefData));
        when(featureToggleService.isSdoR2Enabled()).thenReturn(false);

        DynamicList result = createSDOCallbackHandlerUtils.getLocationList(requestedCourt, false, locations);

        assertNotNull(result);
    }

    @Test
    void shouldReturnLocationListWhenSdoR2IsEnabled() {
        RequestedCourt requestedCourt = new RequestedCourt();
        LocationRefData locationRefData = LocationRefData.builder().build();
        List<LocationRefData> locations = List.of(locationRefData);

        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
        lenient().when(locationHelper.getMatching(anyList(), any(RequestedCourt.class))).thenReturn(Optional.empty());

        DynamicList result = createSDOCallbackHandlerUtils.getLocationList(requestedCourt, true, locations);

        assertNotNull(result);
    }

    @Test
    void shouldReturnEmptyLocationListWhenNoLocationsMatch() {
        RequestedCourt requestedCourt = new RequestedCourt();
        List<LocationRefData> locations = Collections.emptyList();

        when(featureToggleService.isSdoR2Enabled()).thenReturn(false);
        when(locationHelper.getMatching(anyList(), any(RequestedCourt.class))).thenReturn(Optional.empty());

        DynamicList result = createSDOCallbackHandlerUtils.getLocationList(requestedCourt, false, locations);

        assertNotNull(result);
    }

    @ParameterizedTest
    @CsvSource({"true, true", "true, false", "false, true", "false, false"
    })
    void shouldSetCheckList(boolean isSdoR2Enabled, boolean isCarmEnabledForCase) {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();
        List<OrderDetailsPagesSectionsToggle> checkList = List.of(OrderDetailsPagesSectionsToggle.SHOW);

        when(featureToggleService.isSdoR2Enabled()).thenReturn(isSdoR2Enabled);
        lenient().when(featureToggleService.isCarmEnabledForCase(caseDataBuilder.build())).thenReturn(isCarmEnabledForCase);

        createSDOCallbackHandlerUtils.setCheckList(caseDataBuilder, checkList);

        verify(featureToggleService).isSdoR2Enabled();
        verify(featureToggleService).isCarmEnabledForCase(caseDataBuilder.build());
    }

    @Test
    void shouldReturnDynamicHearingMethodListWhenCategorySearchResultIsPresent() {
        CaseData caseData = CaseData.builder().caseAccessCategory(CaseCategory.SPEC_CLAIM).build();
        EnumMap<CallbackParams.Params, Object> params = new EnumMap<>(CallbackParams.Params.class);
        params.put(CallbackParams.Params.BEARER_TOKEN, "testToken");
        CallbackParams callbackParams = CallbackParams.builder()
                .caseData(caseData)
                .params(params)
                .build();
        CategorySearchResult categorySearchResult = new CategorySearchResult();
        categorySearchResult.setCategories(List.of());
        DynamicListElement element = DynamicListElement.builder().label("Hearing Method 1").build();
        DynamicList dynamicList = DynamicList.builder().listItems(List.of(element)).build();

        when(categoryService.findCategoryByCategoryIdAndServiceId(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(categorySearchResult));

        try (MockedStatic<HearingMethodUtils> mockedHearingMethodUtils = mockStatic(HearingMethodUtils.class)) {
            mockedHearingMethodUtils.when(() -> HearingMethodUtils.getHearingMethodList(categorySearchResult))
                    .thenReturn(dynamicList);

            DynamicList result = createSDOCallbackHandlerUtils.getDynamicHearingMethodList(callbackParams, caseData);

            assertNotNull(result);
        }
    }

    @Test
    void shouldReturnDynamicHearingMethodListWhenCategorySearchResultIsEmpty() {
        CaseData caseData = CaseData.builder().caseAccessCategory(CaseCategory.SPEC_CLAIM).build();
        EnumMap<CallbackParams.Params, Object> params = new EnumMap<>(CallbackParams.Params.class);
        params.put(BEARER_TOKEN, "testToken");
        CallbackParams callbackParams = CallbackParams.builder()
                .caseData(caseData)
                .params(params)
                .build();
        DynamicListElement element = DynamicListElement.builder().label("Hearing Method 1").build();
        DynamicList dynamicList = DynamicList.builder().listItems(List.of(element)).build();

        when(categoryService.findCategoryByCategoryIdAndServiceId(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        try (MockedStatic<HearingMethodUtils> mockedHearingMethodUtils = mockStatic(HearingMethodUtils.class)) {
            mockedHearingMethodUtils.when(() -> HearingMethodUtils.getHearingMethodList(null))
                    .thenReturn(dynamicList);

            DynamicList result = createSDOCallbackHandlerUtils.getDynamicHearingMethodList(callbackParams, caseData);

            assertNotNull(result);
        }
    }
}
