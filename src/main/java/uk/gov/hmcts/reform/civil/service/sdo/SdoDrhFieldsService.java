package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingOnRadioOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsSdoR2PhysicalTrialBundleOptions;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsSdoR2TimeEstimate;
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
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsUploadDoc;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsRestrictPages;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsRestrictWitness;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsWitnessStatements;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WelshLanguageUsage;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.BUNDLE_TEXT;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.IMP_NOTES_TEXT;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.JUDGE_RECITAL;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.PPI_DESCRIPTION;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.RESTRICT_NUMBER_PAGES_TEXT1;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.RESTRICT_NUMBER_PAGES_TEXT2;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.RESTRICT_WITNESS_TEXT;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.UPLOAD_DOC_DESCRIPTION;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.WITNESS_DESCRIPTION_TEXT;
import static uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim.WITNESS_STATEMENT_TEXT;

@Service
@RequiredArgsConstructor
public class SdoDrhFieldsService {

    private final SdoLocationService sdoLocationService;
    private final SdoTrackDefaultsService sdoTrackDefaultsService;
    private final SdoJourneyToggleService sdoJourneyToggleService;
    private final SdoDeadlineService sdoDeadlineService;

    public void populateDrhFields(CaseData caseData,
                                  CaseData.CaseDataBuilder<?, ?> updatedData,
                                  Optional<RequestedCourt> preferredCourt,
                                  DynamicList hearingMethodList,
                                  List<LocationRefData> locationRefDataList) {
        List<IncludeInOrderToggle> includeInOrderToggle = sdoTrackDefaultsService.defaultIncludeInOrderToggle();
        DynamicList courtList = sdoLocationService.buildCourtLocationForSdoR2(preferredCourt.orElse(null), locationRefDataList);
        courtList.setValue(courtList.getListItems().get(0));

        DynamicListElement telephoneOption = hearingMethodList.getListItems().stream()
            .filter(elem -> elem.getLabel().equals(HearingMethod.TELEPHONE.getLabel()))
            .findFirst()
            .orElse(null);
        hearingMethodList.setValue(telephoneOption);

        updatedData.sdoR2SmallClaimsJudgesRecital(SdoR2SmallClaimsJudgesRecital.builder().input(JUDGE_RECITAL).build());
        updatedData.sdoR2SmallClaimsPPI(SdoR2SmallClaimsPPI.builder()
                                            .ppiDate(sdoDeadlineService.calendarDaysFromNow(21))
                                            .text(PPI_DESCRIPTION)
                                            .build());
        updatedData.sdoR2SmallClaimsUploadDoc(SdoR2SmallClaimsUploadDoc.builder()
                                                  .sdoUploadOfDocumentsTxt(UPLOAD_DOC_DESCRIPTION)
                                                  .build());
        updatedData.sdoR2SmallClaimsWitnessStatements(SdoR2SmallClaimsWitnessStatements.builder()
                                                          .sdoStatementOfWitness(WITNESS_STATEMENT_TEXT)
                                                          .isRestrictWitness(YesOrNo.NO)
                                                          .isRestrictPages(YesOrNo.NO)
                                                          .sdoR2SmallClaimsRestrictWitness(SdoR2SmallClaimsRestrictWitness.builder()
                                                                                             .partyIsCountedAsWitnessTxt(RESTRICT_WITNESS_TEXT)
                                                                                             .build())
                                                          .sdoR2SmallClaimsRestrictPages(SdoR2SmallClaimsRestrictPages.builder()
                                                                                            .fontDetails(RESTRICT_NUMBER_PAGES_TEXT2)
                                                                                            .noOfPages(12)
                                                                                            .witnessShouldNotMoreThanTxt(RESTRICT_NUMBER_PAGES_TEXT1)
                                                                                            .build())
                                                          .text(WITNESS_DESCRIPTION_TEXT)
                                                          .build());
        updatedData.sdoR2SmallClaimsHearing(SdoR2SmallClaimsHearing.builder()
                                                .trialOnOptions(HearingOnRadioOptions.OPEN_DATE)
                                                .methodOfHearing(hearingMethodList)
                                                .lengthList(SmallClaimsSdoR2TimeEstimate.THIRTY_MINUTES)
                                                .physicalBundleOptions(SmallClaimsSdoR2PhysicalTrialBundleOptions.PARTY)
                                                .sdoR2SmallClaimsHearingFirstOpenDateAfter(
                                                    SdoR2SmallClaimsHearingFirstOpenDateAfter.builder()
                                                        .listFrom(sdoDeadlineService.calendarDaysFromNow(56))
                                                        .build())
                                                .sdoR2SmallClaimsHearingWindow(SdoR2SmallClaimsHearingWindow.builder()
                                                                                .dateTo(sdoDeadlineService.calendarDaysFromNow(70))
                                                                                .listFrom(sdoDeadlineService.calendarDaysFromNow(56))
                                                                                .build())
                                                .hearingCourtLocationList(courtList)
                                                .altHearingCourtLocationList(sdoLocationService.buildLocationList(
                                                    preferredCourt.orElse(null), true, locationRefDataList))
                                                .sdoR2SmallClaimsBundleOfDocs(SdoR2SmallClaimsBundleOfDocs.builder()
                                                                                  .physicalBundlePartyTxt(BUNDLE_TEXT)
                                                                                  .build())
                                                .build());
        updatedData.sdoR2SmallClaimsImpNotes(SdoR2SmallClaimsImpNotes.builder()
                                                 .text(IMP_NOTES_TEXT)
                                                 .date(sdoDeadlineService.calendarDaysFromNow(7))
                                                 .build());
        updatedData.sdoR2SmallClaimsUploadDocToggle(includeInOrderToggle);
        updatedData.sdoR2SmallClaimsHearingToggle(includeInOrderToggle);
        updatedData.sdoR2SmallClaimsWitnessStatementsToggle(includeInOrderToggle);
        updatedData.sdoR2DrhUseOfWelshLanguage(SdoR2WelshLanguageUsage.builder().description(WELSH_LANG_DESCRIPTION).build());

        sdoJourneyToggleService.applyR2SmallClaimsMediation(caseData, updatedData, includeInOrderToggle);
    }
}
