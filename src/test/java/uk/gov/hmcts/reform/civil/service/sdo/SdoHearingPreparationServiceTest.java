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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
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
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.SPEC_CLAIM);
        caseData.setTotalClaimAmount(BigDecimal.valueOf(500));
        caseData.setCaseManagementLocation(new CaseLocationCivil().setBaseLocation("EPIMS123"));
        RequestedCourt requestedCourt = new RequestedCourt();
        requestedCourt.setCaseLocation(new CaseLocationCivil().setBaseLocation("NEW"));
        when(locationHelper.getCaseManagementLocationWhenLegalAdvisorSdo(caseData))
            .thenReturn(Optional.of(requestedCourt));
        when(sdoLocationService.fetchCourtLocationsByEpimmsId(AUTH, "NEW"))
            .thenReturn(List.of(locationRefData()));

        Optional<RequestedCourt> result = service.updateCaseManagementLocationIfLegalAdvisorSdo(caseData, AUTH);

        assertThat(result).contains(requestedCourt);
        assertThat(caseData.getCaseManagementLocation()).isEqualTo(requestedCourt.getCaseLocation());
        assertThat(caseData.getLocationName()).isEqualTo("New Court");
    }

    @Test
    void shouldNotFailWhenEpimmsLookupReturnsNull() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.SPEC_CLAIM);
        caseData.setTotalClaimAmount(BigDecimal.valueOf(500));
        caseData.setCaseManagementLocation(new CaseLocationCivil().setBaseLocation("EPIMS123"));
        RequestedCourt requestedCourt = new RequestedCourt();
        requestedCourt.setCaseLocation(new CaseLocationCivil().setBaseLocation("NEW"));
        when(locationHelper.getCaseManagementLocationWhenLegalAdvisorSdo(caseData))
            .thenReturn(Optional.of(requestedCourt));
        when(sdoLocationService.fetchCourtLocationsByEpimmsId(AUTH, "NEW"))
            .thenReturn(null);

        Optional<RequestedCourt> result = service.updateCaseManagementLocationIfLegalAdvisorSdo(caseData, AUTH);

        assertThat(result).contains(requestedCourt);
        assertThat(caseData.getLocationName()).isNull();
    }

    @Test
    void shouldFallbackToExistingCaseManagementLocationWhenNotLegalAdvisorRoute() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.UNSPEC_CLAIM);
        caseData.setCaseManagementLocation(new CaseLocationCivil().setBaseLocation("OTHER"));
        caseData.setTotalClaimAmount(BigDecimal.valueOf(2000));
        when(locationHelper.getCaseManagementLocation(caseData)).thenReturn(Optional.empty());

        Optional<RequestedCourt> result = service.updateCaseManagementLocationIfLegalAdvisorSdo(caseData, AUTH);

        assertThat(result).isEmpty();
        verify(locationHelper).getCaseManagementLocation(caseData);
    }

    @Test
    void shouldReturnFilteredHearingMethodList() {
        Category inPerson = new Category();
        inPerson.setCategoryKey("HearingChannel");
        inPerson.setKey("INTER");
        inPerson.setValueEn(HearingMethod.IN_PERSON.getLabel());
        inPerson.setActiveFlag("Y");
        Category telephone = new Category();
        telephone.setCategoryKey("HearingChannel");
        telephone.setKey("TEL");
        telephone.setValueEn(HearingMethod.TELEPHONE.getLabel());
        telephone.setActiveFlag("Y");
        Category notInAttendance = new Category();
        notInAttendance.setCategoryKey("HearingChannel");
        notInAttendance.setKey("NIA");
        notInAttendance.setValueEn(HearingMethod.NOT_IN_ATTENDANCE.getLabel());
        notInAttendance.setActiveFlag("Y");
        CategorySearchResult searchResult = new CategorySearchResult();
        searchResult.setCategories(List.of(inPerson, telephone, notInAttendance));
        when(categoryService.findCategoryByCategoryIdAndServiceId(anyString(), anyString(), anyString()))
            .thenReturn(Optional.of(searchResult));

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.SPEC_CLAIM);
        DynamicList list = service.getDynamicHearingMethodList(callbackParams(), caseData);

        assertThat(list.getListItems())
            .extracting(DynamicListElement::getLabel)
            .containsExactly(HearingMethod.IN_PERSON.getLabel(), HearingMethod.TELEPHONE.getLabel());
    }

    @Test
    void shouldApplyVersionSpecificDefaults() {
        DynamicListElement inPerson = new DynamicListElement();
        inPerson.setLabel(HearingMethod.IN_PERSON.getLabel());
        inPerson.setCode("IN_PERSON");
        DynamicListElement other = new DynamicListElement();
        other.setLabel("OTHER");
        other.setCode("OTHER");
        DynamicList hearingList = new DynamicList();
        hearingList.setListItems(List.of(inPerson));
        hearingList.setValue(other);

        CaseData caseData = CaseDataBuilder.builder().build();
        service.applyVersionSpecificHearingDefaults(
            CallbackParams.builder().version(V_1).caseData(caseData).build(),
            hearingList
        );

        assertThat(caseData.getHearingMethodValuesFastTrack()).isEqualTo(hearingList);
        assertThat(hearingList.getValue()).isEqualTo(inPerson);
    }

    @Test
    void shouldPopulateHearingLocations() {
        DynamicListElement locationElement = new DynamicListElement();
        locationElement.setCode("loc");
        locationElement.setLabel("Location");
        DynamicList locationList = new DynamicList();
        locationList.setListItems(List.of(locationElement));
        LocationRefData locationRefData = new LocationRefData();
        locationRefData.setEpimmsId("123");
        locationRefData.setSiteName("Site");
        when(sdoLocationService.fetchHearingLocations(AUTH)).thenReturn(List.of(locationRefData));
        when(sdoLocationService.buildLocationList(any(), anyBoolean(), anyList())).thenReturn(locationList);

        CaseData caseData = CaseDataBuilder.builder().build();
        List<LocationRefData> refs = service.populateHearingLocations(Optional.empty(), AUTH, caseData);

        assertThat(refs).hasSize(1);
        assertThat(caseData.getDisposalHearingMethodInPerson()).isEqualTo(locationList);
        verify(sdoLocationService).fetchHearingLocations(AUTH);
    }

    private CallbackParams callbackParams() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.SPEC_CLAIM);
        caseData.setCaseManagementLocation(new CaseLocationCivil().setBaseLocation("EPIMS123"));
        return CallbackParams.builder()
            .caseData(caseData)
            .params(Map.of(BEARER_TOKEN, AUTH))
            .build();
    }

    private LocationRefData locationRefData() {
        LocationRefData locationRefData = new LocationRefData();
        locationRefData.setSiteName("New Court");
        return locationRefData;
    }
}
