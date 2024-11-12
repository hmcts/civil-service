package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.sdor2andnihlfields;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingOnRadioOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsSdoR2PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsSdoR2TimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.TrialOnRadioOptions;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.CreateSDOCallbackHandlerUtils;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsBundleOfDocs;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearingFirstOpenDateAfter;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearingWindow;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsImpNotes;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsPPI;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsRestrictPages;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsRestrictWitness;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsUploadDoc;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsWitnessStatements;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Trial;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2TrialFirstOpenDateAfter;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2TrialWindow;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WelshLanguageUsage;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.model.common.DynamicListElement.dynamicElementFromCode;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrePopulateSdoR2AndNihlFields {

    private final FeatureToggleService featureToggleService;
    private final LocationHelper locationHelper;
    private final CreateSDOCallbackHandlerUtils createSDOCallbackHandlerUtils;
    private final List<IncludeInOrderToggle> includeInOrderToggle = List.of(IncludeInOrderToggle.INCLUDE);
    private final List<SdoR2AndNihlFieldsCaseFieldBuilder> sdoR2AndNihlFieldsBuilders;

    public void populateDRHFields(CallbackParams callbackParams,
                                  CaseData.CaseDataBuilder<?, ?> updatedData,
                                  Optional<RequestedCourt> preferredCourt,
                                  DynamicList hearingMethodList,
                                  List<LocationRefData> locationRefDataList) {
        DynamicList courtList = getCourtLocationForSdoR2(preferredCourt.orElse(null), locationRefDataList);
        courtList.setValue(courtList.getListItems().get(0));

        setHearingMethod(hearingMethodList);
        setSmallClaimsFields(updatedData, courtList, hearingMethodList, preferredCourt, locationRefDataList);

        CaseData caseData = callbackParams.getCaseData();
        if (featureToggleService.isCarmEnabledForCase(caseData)) {
            sdoR2AndNihlFieldsBuilders.forEach(builder -> builder.build(updatedData));
        }
    }

    private void setHearingMethod(DynamicList hearingMethodList) {
        DynamicListElement hearingMethodElement = hearingMethodList.getListItems().stream()
                .filter(elem -> elem.getLabel().equals(HearingMethod.TELEPHONE.getLabel()))
                .findFirst()
                .orElse(null);
        hearingMethodList.setValue(hearingMethodElement);
    }

    private void setSmallClaimsFields(CaseData.CaseDataBuilder<?, ?> updatedData,
                                      DynamicList courtList,
                                      DynamicList hearingMethodList,
                                      Optional<RequestedCourt> preferredCourt,
                                      List<LocationRefData> locationRefDataList) {
        updatedData.sdoR2SmallClaimsJudgesRecital(SdoR2SmallClaimsJudgesRecital.builder().input(
                SdoR2UiConstantSmallClaim.JUDGE_RECITAL).build());
        updatedData.sdoR2SmallClaimsPPI(SdoR2SmallClaimsPPI.builder().ppiDate(LocalDate.now().plusDays(21)).text(
                SdoR2UiConstantSmallClaim.PPI_DESCRIPTION).build());
        updatedData.sdoR2SmallClaimsUploadDoc(SdoR2SmallClaimsUploadDoc.builder().sdoUploadOfDocumentsTxt(
                SdoR2UiConstantSmallClaim.UPLOAD_DOC_DESCRIPTION).build());
        updatedData.sdoR2SmallClaimsWitnessStatements(SdoR2SmallClaimsWitnessStatements.builder()
                .sdoStatementOfWitness(SdoR2UiConstantSmallClaim.WITNESS_STATEMENT_TEXT)
                .isRestrictWitness(NO)
                .isRestrictPages(NO)
                .sdoR2SmallClaimsRestrictWitness(SdoR2SmallClaimsRestrictWitness.builder()
                        .partyIsCountedAsWitnessTxt(SdoR2UiConstantSmallClaim.RESTRICT_WITNESS_TEXT)
                        .build())
                .sdoR2SmallClaimsRestrictPages(SdoR2SmallClaimsRestrictPages.builder()
                        .fontDetails(SdoR2UiConstantSmallClaim.RESTRICT_NUMBER_PAGES_TEXT2)
                        .noOfPages(12)
                        .witnessShouldNotMoreThanTxt(SdoR2UiConstantSmallClaim.RESTRICT_NUMBER_PAGES_TEXT1)
                        .build())
                .text(SdoR2UiConstantSmallClaim.WITNESS_DESCRIPTION_TEXT).build());
        updatedData.sdoR2SmallClaimsHearing(SdoR2SmallClaimsHearing.builder()
                .trialOnOptions(HearingOnRadioOptions.OPEN_DATE)
                .methodOfHearing(hearingMethodList)
                .lengthList(SmallClaimsSdoR2TimeEstimate.THIRTY_MINUTES)
                .physicalBundleOptions(SmallClaimsSdoR2PhysicalTrialBundleOptions.PARTY)
                .sdoR2SmallClaimsHearingFirstOpenDateAfter(SdoR2SmallClaimsHearingFirstOpenDateAfter.builder()
                        .listFrom(LocalDate.now().plusDays(56)).build())
                .sdoR2SmallClaimsHearingWindow(SdoR2SmallClaimsHearingWindow.builder().dateTo(
                                LocalDate.now().plusDays(70))
                        .listFrom(LocalDate.now().plusDays(56)).build())
                .hearingCourtLocationList(courtList)
                .altHearingCourtLocationList(createSDOCallbackHandlerUtils.getLocationList(
                        preferredCourt.orElse(null),
                        true,
                        locationRefDataList
                ))
                .sdoR2SmallClaimsBundleOfDocs(SdoR2SmallClaimsBundleOfDocs.builder()
                        .physicalBundlePartyTxt(SdoR2UiConstantSmallClaim.BUNDLE_TEXT).build()).build());
        updatedData.sdoR2SmallClaimsImpNotes(SdoR2SmallClaimsImpNotes.builder()
                .text(SdoR2UiConstantSmallClaim.IMP_NOTES_TEXT)
                .date(LocalDate.now().plusDays(7)).build());
        updatedData.sdoR2SmallClaimsUploadDocToggle(includeInOrderToggle);
        updatedData.sdoR2SmallClaimsHearingToggle(includeInOrderToggle);
        updatedData.sdoR2SmallClaimsWitnessStatementsToggle(includeInOrderToggle);
        updatedData.sdoR2DrhUseOfWelshLanguage(SdoR2WelshLanguageUsage.builder().description(SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION).build());
    }

    public void prePopulateNihlFields(CaseData.CaseDataBuilder<?, ?> updatedData, DynamicList hearingMethodList,
                                      Optional<RequestedCourt> preferredCourt, List<LocationRefData> locationRefDataList) {
        setHearingMethodInPerson(hearingMethodList);
        sdoR2AndNihlFieldsBuilders.forEach(builder -> builder.build(updatedData));
        setTrialDetails(updatedData, hearingMethodList, preferredCourt, locationRefDataList);
    }

    private void setHearingMethodInPerson(DynamicList hearingMethodList) {
        DynamicListElement hearingMethodInPerson = hearingMethodList.getListItems().stream()
                .filter(elem -> elem.getLabel().equals(HearingMethod.IN_PERSON.getLabel()))
                .findFirst()
                .orElse(null);
        hearingMethodList.setValue(hearingMethodInPerson);
    }

    private void setTrialDetails(CaseData.CaseDataBuilder<?, ?> updatedData, DynamicList hearingMethodList,
                                 Optional<RequestedCourt> preferredCourt, List<LocationRefData> locationRefDataList) {
        updatedData.sdoR2Trial(SdoR2Trial.builder()
                .trialOnOptions(TrialOnRadioOptions.OPEN_DATE)
                .lengthList(FastTrackHearingTimeEstimate.FIVE_HOURS)
                .methodOfHearing(hearingMethodList)
                .physicalBundleOptions(PhysicalTrialBundleOptions.PARTY)
                .sdoR2TrialFirstOpenDateAfter(SdoR2TrialFirstOpenDateAfter.builder()
                        .listFrom(LocalDate.now().plusDays(434)).build())
                .sdoR2TrialWindow(SdoR2TrialWindow.builder()
                        .listFrom(LocalDate.now().plusDays(434))
                        .dateTo(LocalDate.now().plusDays(455))
                        .build())
                .hearingCourtLocationList(DynamicList.builder()
                        .listItems(getCourtLocationForSdoR2(preferredCourt.orElse(null), locationRefDataList).getListItems())
                        .value(getCourtLocationForSdoR2(preferredCourt.orElse(null), locationRefDataList).getListItems().get(0))
                        .build())
                .altHearingCourtLocationList(getAlternativeCourtLocationsForNihl(locationRefDataList))
                .physicalBundlePartyTxt(SdoR2UiConstantFastTrack.PHYSICAL_TRIAL_BUNDLE)
                .build());
    }

    private DynamicList getCourtLocationForSdoR2(RequestedCourt preferredCourt,
                                                 List<LocationRefData> locations) {
        Optional<LocationRefData> matchingLocation = Optional.ofNullable(preferredCourt)
                .flatMap(requestedCourt -> locationHelper.getMatching(locations, preferredCourt));

        List<DynamicListElement> dynamicListOptions = new ArrayList<>();
        matchingLocation.ifPresent(locationRefData -> dynamicListOptions.add(dynamicElementFromCode(
                locationRefData.getEpimmsId(),
                LocationReferenceDataService.getDisplayEntry(locationRefData)
        )));
        dynamicListOptions.add(dynamicElementFromCode("OTHER_LOCATION", "Other location"));
        return DynamicList.fromDynamicListElementList(dynamicListOptions);
    }

    private DynamicList getAlternativeCourtLocationsForNihl(List<LocationRefData> locations) {

        List<DynamicListElement> dynamicListOptions = new ArrayList<>();

        locations.forEach(loc -> dynamicListOptions.add(
                dynamicElementFromCode(loc.getEpimmsId(), LocationReferenceDataService.getDisplayEntry(loc))));
        return DynamicList.fromDynamicListElementList(dynamicListOptions);
    }
}
