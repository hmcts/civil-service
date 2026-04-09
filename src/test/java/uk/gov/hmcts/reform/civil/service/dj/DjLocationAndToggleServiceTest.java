package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;
import uk.gov.hmcts.reform.civil.crd.model.Category;
import uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalAndTrialHearingDJToggle;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingMethodDJ;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_2;
import static uk.gov.hmcts.reform.civil.service.dj.StandardDirectionOrderDjConstants.DISPOSAL_HEARING;
import static uk.gov.hmcts.reform.civil.service.dj.StandardDirectionOrderDjConstants.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.civil.service.dj.StandardDirectionOrderDjConstants.SPEC_SERVICE_ID;

@ExtendWith(MockitoExtension.class)
class DjLocationAndToggleServiceTest {

    private static final String AUTH_TOKEN = "BEARER_TOKEN";

    @Mock
    private LocationReferenceDataService locationReferenceDataService;
    @Mock
    private CategoryService categoryService;
    @Mock
    private LocationHelper locationHelper;

    private DjLocationAndToggleService service;

    @BeforeEach
    void setUp() {
        service = new DjLocationAndToggleService(
            locationReferenceDataService,
            categoryService,
            locationHelper
        );
    }

    @Test
    void shouldPrepareDisposalTogglesLocationsAndHearingList() {
        stubLocationMocks("123", true);
        stubHearingCategories();

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.SPEC_CLAIM);
        caseData.setCaseManagementOrderSelection(DISPOSAL_HEARING);

        DirectionsOrderTaskContext context = buildContext(caseData, V_1);

        CaseData result = service.prepareLocationsAndToggles(context);

        assertThat(result.getDisposalHearingDisclosureOfDocumentsDJToggle())
            .containsExactly(DisposalAndTrialHearingDJToggle.SHOW);
        assertThat(result.getTrialHearingMethodInPersonDJ().getValue().getCode()).isEqualTo("123");
        assertThat(result.getDisposalHearingMethodInPersonDJ().getValue().getCode()).isEqualTo("123");

        DynamicList hearingList = result.getHearingMethodValuesDisposalHearingDJ();
        assertThat(hearingList).isNotNull();
        assertThat(hearingList.getListItems())
            .extracting(DynamicListElement::getLabel)
            .doesNotContain(HearingMethod.NOT_IN_ATTENDANCE.getLabel());
        assertThat(hearingList.getValue().getLabel()).isEqualTo(HearingMethod.IN_PERSON.getLabel());
    }

    @Test
    void shouldPrepareDisposalTogglesLocationsAndHearingListV1() {
        stubLocationMocks("214320", false);
        stubHearingCategories();

        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.SPEC_CLAIM);
        caseData.setCaseManagementOrderSelection(DISPOSAL_HEARING);

        CaseLocationCivil civil = new CaseLocationCivil();
        civil.setRegion("1");
        civil.setBaseLocation("214320");
        caseData.setCaseManagementLocation(civil);
        caseData.setReasonForTransfer("Court Closed");

        DirectionsOrderTaskContext context = buildContext(caseData, V_1);

        CaseData result = service.prepareLocationsAndToggles(context);

        assertThat(result.getDisposalHearingDisclosureOfDocumentsDJToggle())
            .containsExactly(DisposalAndTrialHearingDJToggle.SHOW);
        assertThat(result.getTrialHearingMethodInPersonDJ().getValue().getCode()).isEqualTo("214320");
        assertThat(result.getDisposalHearingMethodInPersonDJ().getValue().getCode()).isEqualTo("214320");

        DynamicList hearingList = result.getHearingMethodValuesDisposalHearingDJ();
        assertThat(hearingList).isNotNull();
        assertThat(hearingList.getListItems())
            .extracting(DynamicListElement::getLabel)
            .doesNotContain(HearingMethod.NOT_IN_ATTENDANCE.getLabel());
        assertThat(hearingList.getValue().getLabel()).isEqualTo(HearingMethod.IN_PERSON.getLabel());
    }

    @Test
    void shouldPrepareTrialTogglesWhenNotDisposal() {
        stubLocationMocks("456", true);
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setCaseAccessCategory(CaseCategory.UNSPEC_CLAIM);
        caseData.setCaseManagementOrderSelection("TRIAL_HEARING");

        DirectionsOrderTaskContext context = buildContext(caseData, V_2);

        CaseData result = service.prepareLocationsAndToggles(context);

        assertThat(result.getTrialHearingAlternativeDisputeDJToggle())
            .containsExactly(DisposalAndTrialHearingDJToggle.SHOW);
        assertThat(result.getDisposalHearingDisclosureOfDocumentsDJToggle()).isNull();
        assertThat(result.getTrialHearingMethodInPersonDJ().getValue().getCode()).isEqualTo("456");
    }

    @Test
    void shouldApplyDisposalHearingSelectionWhenVersionV1() {
        DynamicList disposalList = dynamicListWithValue(
            dynamicListElement("TEL", HearingMethod.TELEPHONE.getLabel())
        );
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setHearingMethodValuesDisposalHearingDJ(disposalList);

        CaseData result = service.applyHearingSelections(caseData, V_1);

        assertThat(result.getDisposalHearingMethodDJ())
            .isEqualTo(DisposalHearingMethodDJ.disposalHearingMethodTelephoneHearing);
    }

    @Test
    void shouldApplyTrialHearingSelectionWhenDisposalNotPresent() {
        DynamicList trialList = dynamicListWithValue(
            dynamicListElement("VID", HearingMethod.VIDEO.getLabel())
        );
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setHearingMethodValuesTrialHearingDJ(trialList);

        CaseData result = service.applyHearingSelections(caseData, V_1);

        assertThat(result.getTrialHearingMethodDJ())
            .isEqualTo(DisposalHearingMethodDJ.disposalHearingMethodVideoConferenceHearing);
    }

    @Test
    void shouldReturnOriginalCaseDataWhenVersionIsNotV1() {
        CaseData caseData = CaseDataBuilder.builder().build();

        CaseData result = service.applyHearingSelections(caseData, V_2);

        assertThat(result).isSameAs(caseData);
    }

    @Test
    void shouldApplyInPersonSelectionForDisposalHearing() {
        DynamicList disposalList = dynamicListWithValue(
            dynamicListElement("INP", HearingMethod.IN_PERSON.getLabel())
        );
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setHearingMethodValuesDisposalHearingDJ(disposalList);

        CaseData result = service.applyHearingSelections(caseData, V_1);

        assertThat(result.getDisposalHearingMethodDJ())
            .isEqualTo(DisposalHearingMethodDJ.disposalHearingMethodInPerson);
    }

    @Test
    void shouldApplyTelephoneSelectionForTrialHearingWhenDisposalNotProvided() {
        DynamicList trialList = dynamicListWithValue(
            dynamicListElement("TEL", HearingMethod.TELEPHONE.getLabel())
        );
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setHearingMethodValuesTrialHearingDJ(trialList);

        CaseData result = service.applyHearingSelections(caseData, V_1);

        assertThat(result.getTrialHearingMethodDJ())
            .isEqualTo(DisposalHearingMethodDJ.disposalHearingMethodTelephoneHearing);
    }

    @Test
    void shouldReturnCaseDataWhenSelectionsAbsent() {
        CaseData caseData = CaseDataBuilder.builder().build();

        CaseData result = service.applyHearingSelections(caseData, V_1);

        assertThat(result).isEqualTo(caseData);
    }

    @Test
    void shouldLeaveDisposalSelectionUnsetWhenLabelNotRecognised() {
        DynamicList disposalList = dynamicListWithValue(dynamicListElement("OTHER", "Other option"));
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setHearingMethodValuesDisposalHearingDJ(disposalList);

        CaseData result = service.applyHearingSelections(caseData, V_1);

        assertThat(result.getDisposalHearingMethodDJ()).isNull();
    }

    @Test
    void shouldLeaveTrialSelectionUnsetWhenLabelNotRecognised() {
        DynamicList trialList = dynamicListWithValue(dynamicListElement("OTHER", "Other option"));
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setHearingMethodValuesTrialHearingDJ(trialList);

        CaseData result = service.applyHearingSelections(caseData, V_1);

        assertThat(result.getTrialHearingMethodDJ()).isNull();
    }

    private DirectionsOrderTaskContext buildContext(CaseData caseData, CallbackVersion version) {
        CallbackParams params = CallbackParamsBuilder.builder()
            .of(CallbackType.ABOUT_TO_START, caseData)
            .version(version)
            .build();
        return new DirectionsOrderTaskContext(caseData, params, DirectionsOrderLifecycleStage.ORDER_DETAILS);
    }

    private void stubHearingCategories() {
        Category inPerson = hearingCategory("INP", HearingMethod.IN_PERSON.getLabel());
        Category telephone = hearingCategory("TEL", HearingMethod.TELEPHONE.getLabel());
        Category notInAttendance = hearingCategory("NIA", HearingMethod.NOT_IN_ATTENDANCE.getLabel());
        CategorySearchResult categorySearchResult = new CategorySearchResult();
        categorySearchResult.setCategories(List.of(inPerson, telephone, notInAttendance));
        when(categoryService.findCategoryByCategoryIdAndServiceId(AUTH_TOKEN, HEARING_CHANNEL, SPEC_SERVICE_ID))
            .thenReturn(Optional.of(categorySearchResult));
    }

    private void stubLocationMocks(String epimmsId, boolean isRequire) {
        LocationRefData location = new LocationRefData();
        location.setEpimmsId(epimmsId);
        location.setSiteName("Court-" + epimmsId);
        List<LocationRefData> locations = List.of(location);
        RequestedCourt requestedCourt = new RequestedCourt();
        requestedCourt.setCaseLocation(new CaseLocationCivil().setBaseLocation(epimmsId).setRegion("1"));
        if (isRequire) {
            when(locationHelper.getCaseManagementLocation(any())).thenReturn(Optional.of(requestedCourt));
        }
        when(locationReferenceDataService.getCourtLocationsForDefaultJudgments(AUTH_TOKEN)).thenReturn(locations);
        when(locationHelper.getMatching(locations, requestedCourt)).thenReturn(Optional.of(location));
    }

    private static DynamicListElement dynamicListElement(String code, String label) {
        DynamicListElement element = new DynamicListElement();
        element.setCode(code);
        element.setLabel(label);
        return element;
    }

    private static DynamicList dynamicListWithValue(DynamicListElement value) {
        DynamicList list = new DynamicList();
        list.setValue(value);
        return list;
    }

    private static Category hearingCategory(String key, String label) {
        Category category = new Category();
        category.setCategoryKey(HEARING_CHANNEL);
        category.setKey(key);
        category.setValueEn(label);
        category.setActiveFlag("Y");
        return category;
    }
}
