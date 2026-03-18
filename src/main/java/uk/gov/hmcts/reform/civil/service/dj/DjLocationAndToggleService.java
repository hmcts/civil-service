package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackVersion;
import uk.gov.hmcts.reform.civil.crd.model.CategorySearchResult;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalAndTrialHearingDJToggle;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingMethodDJ;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.handler.callback.user.directionsorder.tasks.DirectionsOrderTaskContext;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.CategoryService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.HearingMethodUtils;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.service.dj.StandardDirectionOrderDjConstants.DISPOSAL_HEARING;
import static uk.gov.hmcts.reform.civil.service.dj.StandardDirectionOrderDjConstants.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.civil.service.dj.StandardDirectionOrderDjConstants.SPEC_SERVICE_ID;
import static uk.gov.hmcts.reform.civil.service.dj.StandardDirectionOrderDjConstants.UNSPEC_SERVICE_ID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DjLocationAndToggleService {

    private static final List<DisposalAndTrialHearingDJToggle> DEFAULT_TOGGLE =
        List.of(DisposalAndTrialHearingDJToggle.SHOW);

    private final LocationReferenceDataService locationReferenceDataService;
    private final CategoryService categoryService;
    private final LocationHelper locationHelper;

    public CaseData prepareLocationsAndToggles(DirectionsOrderTaskContext context) {
        CallbackParams callbackParams = context.callbackParams();
        CaseData caseData = callbackParams.getCaseData();

        CaseData toggledCaseData = applyToggleDefaults(caseData);
        CaseData updatedData = toggledCaseData.copy();

        DynamicList locationsList = buildLocationList(callbackParams, toggledCaseData);
        updatedData.setDisposalHearingMethodInPersonDJ(locationsList);
        updatedData.setTrialHearingMethodInPersonDJ(locationsList);

        log.info("Prepared DJ locations/toggles for caseId {}", caseData.getCcdCaseReference());
        if (V_1.equals(callbackParams.getVersion())) {
            DynamicList hearingMethods = buildHearingMethods(callbackParams, toggledCaseData);
            updatedData.setHearingMethodValuesDisposalHearingDJ(hearingMethods);
            updatedData.setHearingMethodValuesTrialHearingDJ(hearingMethods);
        }

        return updatedData;
    }

    public CaseData applyHearingSelections(CaseData caseData, CallbackVersion version) {
        if (!V_1.equals(version)) {
            return caseData;
        }

        CaseData updatedData = caseData.copy();

        if (caseData.getHearingMethodValuesDisposalHearingDJ() != null
            && caseData.getHearingMethodValuesDisposalHearingDJ().getValue() != null) {
            String label = caseData.getHearingMethodValuesDisposalHearingDJ().getValue().getLabel();
            applyDisposalSelection(updatedData, label);
        } else if (caseData.getHearingMethodValuesTrialHearingDJ() != null
            && caseData.getHearingMethodValuesTrialHearingDJ().getValue() != null) {
            String label = caseData.getHearingMethodValuesTrialHearingDJ().getValue().getLabel();
            applyTrialSelection(updatedData, label);
        }

        return updatedData;
    }

    private void applyDisposalSelection(CaseData updatedData, String label) {
        if (HearingMethod.IN_PERSON.getLabel().equals(label)) {
            updatedData.setDisposalHearingMethodDJ(DisposalHearingMethodDJ.disposalHearingMethodInPerson);
        } else if (HearingMethod.VIDEO.getLabel().equals(label)) {
            updatedData.setDisposalHearingMethodDJ(DisposalHearingMethodDJ.disposalHearingMethodVideoConferenceHearing);
        } else if (HearingMethod.TELEPHONE.getLabel().equals(label)) {
            updatedData.setDisposalHearingMethodDJ(DisposalHearingMethodDJ.disposalHearingMethodTelephoneHearing);
        }
    }

    private void applyTrialSelection(CaseData updatedData, String label) {
        if (HearingMethod.IN_PERSON.getLabel().equals(label)) {
            updatedData.setTrialHearingMethodDJ(DisposalHearingMethodDJ.disposalHearingMethodInPerson);
        } else if (HearingMethod.VIDEO.getLabel().equals(label)) {
            updatedData.setTrialHearingMethodDJ(DisposalHearingMethodDJ.disposalHearingMethodVideoConferenceHearing);
        } else if (HearingMethod.TELEPHONE.getLabel().equals(label)) {
            updatedData.setTrialHearingMethodDJ(DisposalHearingMethodDJ.disposalHearingMethodTelephoneHearing);
        }
    }

    private CaseData applyToggleDefaults(CaseData caseData) {
        if (DISPOSAL_HEARING.equals(caseData.getCaseManagementOrderSelection())) {
            return fillDisposalToggle(caseData);
        }
        return fillTrialToggle(caseData);
    }

    private CaseData fillDisposalToggle(CaseData caseData) {
        CaseData updatedData = caseData.copy();
        updatedData.setDisposalHearingDisclosureOfDocumentsDJToggle(DEFAULT_TOGGLE);
        updatedData.setDisposalHearingWitnessOfFactDJToggle(DEFAULT_TOGGLE);
        updatedData.setDisposalHearingMedicalEvidenceDJToggle(DEFAULT_TOGGLE);
        updatedData.setDisposalHearingQuestionsToExpertsDJToggle(DEFAULT_TOGGLE);
        updatedData.setDisposalHearingSchedulesOfLossDJToggle(DEFAULT_TOGGLE);
        updatedData.setDisposalHearingStandardDisposalOrderDJToggle(DEFAULT_TOGGLE);
        updatedData.setDisposalHearingFinalDisposalHearingDJToggle(DEFAULT_TOGGLE);
        updatedData.setDisposalHearingBundleDJToggle(DEFAULT_TOGGLE);
        updatedData.setDisposalHearingClaimSettlingDJToggle(DEFAULT_TOGGLE);
        updatedData.setDisposalHearingCostsDJToggle(DEFAULT_TOGGLE);
        return updatedData;
    }

    private CaseData fillTrialToggle(CaseData caseData) {
        CaseData updatedData = caseData.copy();
        updatedData.setTrialHearingAlternativeDisputeDJToggle(DEFAULT_TOGGLE);
        updatedData.setTrialHearingVariationsDirectionsDJToggle(DEFAULT_TOGGLE);
        updatedData.setTrialHearingSettlementDJToggle(DEFAULT_TOGGLE);
        updatedData.setTrialHearingDisclosureOfDocumentsDJToggle(DEFAULT_TOGGLE);
        updatedData.setTrialHearingWitnessOfFactDJToggle(DEFAULT_TOGGLE);
        updatedData.setTrialHearingSchedulesOfLossDJToggle(DEFAULT_TOGGLE);
        updatedData.setTrialHearingCostsToggle(DEFAULT_TOGGLE);
        updatedData.setTrialHearingTrialDJToggle(DEFAULT_TOGGLE);
        return updatedData;
    }

    private DynamicList buildLocationList(CallbackParams callbackParams, CaseData caseData) {
        List<LocationRefData> locations = locationReferenceDataService.getCourtLocationsForDefaultJudgments(
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );

        Optional<RequestedCourt> preferredCourt;
        if (caseData.getReasonForTransfer() != null
            && caseData.getCaseManagementLocation() != null) {
            RequestedCourt requestedCourt = new RequestedCourt();
            requestedCourt.setCaseLocation(caseData.getCaseManagementLocation());
            preferredCourt = Optional.of(requestedCourt);
            log.info("setting location based on new case management location for caseID {}", caseData.getCcdCaseReference());
        } else {
            preferredCourt = locationHelper.getCaseManagementLocation(caseData);
            log.info("setting default location for caseID {}", caseData.getCcdCaseReference());
        }
        Optional<LocationRefData> selectedLocation = preferredCourt
            .flatMap(requestedCourt -> locationHelper.getMatching(locations, requestedCourt));

        return DynamicList.fromList(
            locations,
            this::getLocationEpimms,
            LocationReferenceDataService::getDisplayEntry,
            selectedLocation.orElse(null),
            true
        );
    }

    private DynamicList buildHearingMethods(CallbackParams callbackParams, CaseData caseData) {
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        String serviceId = CaseCategory.SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            ? SPEC_SERVICE_ID
            : UNSPEC_SERVICE_ID;
        Optional<CategorySearchResult> categorySearchResult = categoryService.findCategoryByCategoryIdAndServiceId(
            authToken,
            HEARING_CHANNEL,
            serviceId
        );

        DynamicList hearingMethodList = HearingMethodUtils.getHearingMethodList(categorySearchResult.orElse(null));
        List<DynamicListElement> filteredItems = hearingMethodList.getListItems()
            .stream()
            .filter(item -> !HearingMethod.NOT_IN_ATTENDANCE.getLabel().equals(item.getLabel()))
            .toList();
        hearingMethodList.setListItems(filteredItems);
        hearingMethodList.setValue(filteredItems.stream()
            .filter(item -> HearingMethod.IN_PERSON.getLabel().equals(item.getLabel()))
            .findFirst()
            .orElse(null));
        return hearingMethodList;
    }

    private String getLocationEpimms(LocationRefData location) {
        return location.getEpimmsId();
    }
}
