package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalAndTrialHearingDJToggle;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingMethodDJ;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderLifecycleStage;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.crd.model.Category;
import uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
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
        stubLocationMocks("123");
        stubHearingCategories();

        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .caseManagementOrderSelection(DISPOSAL_HEARING)
            .build();

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
    void shouldPrepareTrialTogglesWhenNotDisposal() {
        stubLocationMocks("456");
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .caseManagementOrderSelection("TRIAL_HEARING")
            .build();

        DirectionsOrderTaskContext context = buildContext(caseData, V_2);

        CaseData result = service.prepareLocationsAndToggles(context);

        assertThat(result.getTrialHearingAlternativeDisputeDJToggle())
            .containsExactly(DisposalAndTrialHearingDJToggle.SHOW);
        assertThat(result.getDisposalHearingDisclosureOfDocumentsDJToggle()).isNull();
        assertThat(result.getTrialHearingMethodInPersonDJ().getValue().getCode()).isEqualTo("456");
    }

    @Test
    void shouldApplyDisposalHearingSelectionWhenVersionV1() {
        DynamicList disposalList = DynamicList.builder()
            .value(DynamicListElement.builder()
                .label(HearingMethod.TELEPHONE.getLabel())
                .code("TEL")
                .build())
            .build();
        CaseData caseData = CaseData.builder()
            .hearingMethodValuesDisposalHearingDJ(disposalList)
            .build();

        CaseData result = service.applyHearingSelections(caseData, V_1);

        assertThat(result.getDisposalHearingMethodDJ())
            .isEqualTo(DisposalHearingMethodDJ.disposalHearingMethodTelephoneHearing);
    }

    @Test
    void shouldApplyTrialHearingSelectionWhenDisposalNotPresent() {
        DynamicList trialList = DynamicList.builder()
            .value(DynamicListElement.builder()
                .label(HearingMethod.VIDEO.getLabel())
                .code("VID")
                .build())
            .build();
        CaseData caseData = CaseData.builder()
            .hearingMethodValuesTrialHearingDJ(trialList)
            .build();

        CaseData result = service.applyHearingSelections(caseData, V_1);

        assertThat(result.getTrialHearingMethodDJ())
            .isEqualTo(DisposalHearingMethodDJ.disposalHearingMethodVideoConferenceHearing);
    }

    @Test
    void shouldReturnOriginalCaseDataWhenVersionIsNotV1() {
        CaseData caseData = CaseData.builder().build();

        CaseData result = service.applyHearingSelections(caseData, V_2);

        assertThat(result).isSameAs(caseData);
    }

    @Test
    void shouldApplyInPersonSelectionForDisposalHearing() {
        DynamicList disposalList = DynamicList.builder()
            .value(DynamicListElement.builder()
                       .label(HearingMethod.IN_PERSON.getLabel())
                       .code("INP")
                       .build())
            .build();
        CaseData caseData = CaseData.builder()
            .hearingMethodValuesDisposalHearingDJ(disposalList)
            .build();

        CaseData result = service.applyHearingSelections(caseData, V_1);

        assertThat(result.getDisposalHearingMethodDJ())
            .isEqualTo(DisposalHearingMethodDJ.disposalHearingMethodInPerson);
    }

    @Test
    void shouldApplyTelephoneSelectionForTrialHearingWhenDisposalNotProvided() {
        DynamicList trialList = DynamicList.builder()
            .value(DynamicListElement.builder()
                       .label(HearingMethod.TELEPHONE.getLabel())
                       .code("TEL")
                       .build())
            .build();
        CaseData caseData = CaseData.builder()
            .hearingMethodValuesTrialHearingDJ(trialList)
            .build();

        CaseData result = service.applyHearingSelections(caseData, V_1);

        assertThat(result.getTrialHearingMethodDJ())
            .isEqualTo(DisposalHearingMethodDJ.disposalHearingMethodTelephoneHearing);
    }

    @Test
    void shouldReturnCaseDataWhenSelectionsAbsent() {
        CaseData caseData = CaseData.builder().build();

        CaseData result = service.applyHearingSelections(caseData, V_1);

        assertThat(result).isEqualTo(caseData);
    }

    @Test
    void shouldLeaveDisposalSelectionUnsetWhenLabelNotRecognised() {
        DynamicList disposalList = DynamicList.builder()
            .value(DynamicListElement.builder()
                       .label("Other option")
                       .code("OTHER")
                       .build())
            .build();
        CaseData caseData = CaseData.builder()
            .hearingMethodValuesDisposalHearingDJ(disposalList)
            .build();

        CaseData result = service.applyHearingSelections(caseData, V_1);

        assertThat(result.getDisposalHearingMethodDJ()).isNull();
    }

    @Test
    void shouldLeaveTrialSelectionUnsetWhenLabelNotRecognised() {
        DynamicList trialList = DynamicList.builder()
            .value(DynamicListElement.builder()
                       .label("Other option")
                       .code("OTHER")
                       .build())
            .build();
        CaseData caseData = CaseData.builder()
            .hearingMethodValuesTrialHearingDJ(trialList)
            .build();

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
        Category inPerson = Category.builder()
            .categoryKey(HEARING_CHANNEL)
            .key("INP")
            .valueEn(HearingMethod.IN_PERSON.getLabel())
            .activeFlag("Y")
            .build();
        Category telephone = Category.builder()
            .categoryKey(HEARING_CHANNEL)
            .key("TEL")
            .valueEn(HearingMethod.TELEPHONE.getLabel())
            .activeFlag("Y")
            .build();
        Category notInAttendance = Category.builder()
            .categoryKey(HEARING_CHANNEL)
            .key("NIA")
            .valueEn(HearingMethod.NOT_IN_ATTENDANCE.getLabel())
            .activeFlag("Y")
            .build();
        CategorySearchResult categorySearchResult =
            CategorySearchResult.builder().categories(List.of(inPerson, telephone, notInAttendance)).build();
        when(categoryService.findCategoryByCategoryIdAndServiceId(AUTH_TOKEN, HEARING_CHANNEL, SPEC_SERVICE_ID))
            .thenReturn(Optional.of(categorySearchResult));
    }

    private void stubLocationMocks(String epimmsId) {
        LocationRefData location = LocationRefData.builder()
            .epimmsId(epimmsId)
            .siteName("Court-" + epimmsId)
            .build();
        List<LocationRefData> locations = List.of(location);

        RequestedCourt requestedCourt = RequestedCourt.builder()
            .caseLocation(CaseLocationCivil.builder().baseLocation(epimmsId).build())
            .build();

        when(locationReferenceDataService.getCourtLocationsForDefaultJudgments(AUTH_TOKEN)).thenReturn(locations);
        when(locationHelper.getCaseManagementLocation(any())).thenReturn(Optional.of(requestedCourt));
        when(locationHelper.getMatching(eq(locations), eq(requestedCourt))).thenReturn(Optional.of(location));
    }
}
