package uk.gov.hmcts.reform.civil.service.sdo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.crd.model.Category;
import uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;

@ExtendWith(MockitoExtension.class)
class SdoHearingPreparationServiceTest {

    private static final String AUTH = "auth";

    @Mock
    private LocationHelper locationHelper;
    @Mock
    private SdoLocationService sdoLocationService;
    @Mock
    private CategoryService categoryService;

    private SdoHearingPreparationService service;

    @BeforeEach
    void setUp() {
        service = new SdoHearingPreparationService(locationHelper, sdoLocationService, categoryService);
        service.ccmccAmount = BigDecimal.valueOf(1000);
        service.ccmccEpimsId = "EPIMS123";
    }

    @Test
    void shouldUpdateCaseManagementLocationWhenLegalAdvisorSdo() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .totalClaimAmount(BigDecimal.valueOf(500))
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("EPIMS123").build())
            .build();
        RequestedCourt requestedCourt = RequestedCourt.builder()
            .caseLocation(CaseLocationCivil.builder().baseLocation("NEW").build())
            .build();
        when(locationHelper.getCaseManagementLocationWhenLegalAdvisorSdo(caseData))
            .thenReturn(Optional.of(requestedCourt));

        Optional<RequestedCourt> result = service.updateCaseManagementLocationIfLegalAdvisorSdo(caseData);

        assertThat(result).contains(requestedCourt);
        assertThat(caseData.getCaseManagementLocation()).isEqualTo(requestedCourt.getCaseLocation());
    }

    @Test
    void shouldFallbackToExistingCaseManagementLocationWhenNotLegalAdvisorRoute() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("OTHER").build())
            .totalClaimAmount(BigDecimal.valueOf(2000))
            .build();
        when(locationHelper.getCaseManagementLocation(caseData)).thenReturn(Optional.empty());

        Optional<RequestedCourt> result = service.updateCaseManagementLocationIfLegalAdvisorSdo(caseData);

        assertThat(result).isEmpty();
        verify(locationHelper).getCaseManagementLocation(caseData);
    }

    @Test
    void shouldReturnFilteredHearingMethodList() {
        Category inPerson = Category.builder()
            .categoryKey("HearingChannel")
            .key("INTER")
            .valueEn(HearingMethod.IN_PERSON.getLabel())
            .activeFlag("Y")
            .build();
        Category telephone = Category.builder()
            .categoryKey("HearingChannel")
            .key("TEL")
            .valueEn(HearingMethod.TELEPHONE.getLabel())
            .activeFlag("Y")
            .build();
        Category notInAttendance = Category.builder()
            .categoryKey("HearingChannel")
            .key("NIA")
            .valueEn(HearingMethod.NOT_IN_ATTENDANCE.getLabel())
            .activeFlag("Y")
            .build();
        when(categoryService.findCategoryByCategoryIdAndServiceId(anyString(), anyString(), anyString()))
            .thenReturn(Optional.of(CategorySearchResult.builder().categories(List.of(inPerson, telephone, notInAttendance)).build()));

        DynamicList list = service.getDynamicHearingMethodList(callbackParams(), CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .build());

        assertThat(list.getListItems())
            .extracting(DynamicListElement::getLabel)
            .containsExactly(HearingMethod.IN_PERSON.getLabel(), HearingMethod.TELEPHONE.getLabel());
    }

    @Test
    void shouldApplyVersionSpecificDefaults() {
        DynamicListElement inPerson = DynamicListElement.builder()
            .label(HearingMethod.IN_PERSON.getLabel())
            .code("IN_PERSON")
            .build();
        DynamicList hearingList = DynamicList.builder()
            .listItems(List.of(inPerson))
            .value(DynamicListElement.builder().label("OTHER").code("OTHER").build())
            .build();

        CaseData caseData = CaseData.builder().build();
        service.applyVersionSpecificHearingDefaults(
            CallbackParams.builder().version(V_1).caseData(caseData).build(),
            hearingList
        );

        assertThat(caseData.getHearingMethodValuesFastTrack()).isEqualTo(hearingList);
        assertThat(hearingList.getValue()).isEqualTo(inPerson);
    }

    @Test
    void shouldPopulateHearingLocations() {
        DynamicList locationList = DynamicList.builder()
            .listItems(List.of(DynamicListElement.builder().code("loc").label("Location").build()))
            .build();
        LocationRefData locationRefData = LocationRefData.builder().epimmsId("123").siteName("Site").build();
        when(sdoLocationService.fetchHearingLocations(AUTH)).thenReturn(List.of(locationRefData));
        when(sdoLocationService.buildLocationList(any(), anyBoolean(), anyList())).thenReturn(locationList);

        CaseData caseData = CaseData.builder().build();
        List<LocationRefData> refs = service.populateHearingLocations(Optional.empty(), AUTH, caseData);

        assertThat(refs).hasSize(1);
        assertThat(caseData.getDisposalHearingMethodInPerson()).isEqualTo(locationList);
        verify(sdoLocationService).fetchHearingLocations(AUTH);
    }

    private CallbackParams callbackParams() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("EPIMS123").build())
            .build();
        return CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, AUTH))
            .build();
    }
}
