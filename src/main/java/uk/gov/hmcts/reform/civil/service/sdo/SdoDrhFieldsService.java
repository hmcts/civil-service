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
                                  Optional<RequestedCourt> preferredCourt,
                                  DynamicList hearingMethodList,
                                  List<LocationRefData> locationRefDataList) {
        final List<IncludeInOrderToggle> includeInOrderToggle = sdoTrackDefaultsService.defaultIncludeInOrderToggle();
        DynamicList courtList = sdoLocationService.buildCourtLocationForSdoR2(preferredCourt.orElse(null), locationRefDataList);
        courtList.setValue(courtList.getListItems().get(0));

        DynamicListElement telephoneOption = hearingMethodList.getListItems().stream()
            .filter(elem -> elem.getLabel().equals(HearingMethod.TELEPHONE.getLabel()))
            .findFirst()
            .orElse(null);
        hearingMethodList.setValue(telephoneOption);

        SdoR2SmallClaimsJudgesRecital judgesRecital = new SdoR2SmallClaimsJudgesRecital();
        judgesRecital.setInput(JUDGE_RECITAL);
        caseData.setSdoR2SmallClaimsJudgesRecital(judgesRecital);
        SdoR2SmallClaimsPPI ppi = new SdoR2SmallClaimsPPI();
        ppi.setPpiDate(sdoDeadlineService.calendarDaysFromNow(21));
        ppi.setText(PPI_DESCRIPTION);
        caseData.setSdoR2SmallClaimsPPI(ppi);

        SdoR2SmallClaimsUploadDoc uploadDoc = new SdoR2SmallClaimsUploadDoc();
        uploadDoc.setSdoUploadOfDocumentsTxt(UPLOAD_DOC_DESCRIPTION);
        caseData.setSdoR2SmallClaimsUploadDoc(uploadDoc);

        SdoR2SmallClaimsRestrictWitness restrictWitness = new SdoR2SmallClaimsRestrictWitness();
        restrictWitness.setPartyIsCountedAsWitnessTxt(RESTRICT_WITNESS_TEXT);

        SdoR2SmallClaimsRestrictPages restrictPages = new SdoR2SmallClaimsRestrictPages();
        restrictPages.setFontDetails(RESTRICT_NUMBER_PAGES_TEXT2);
        restrictPages.setNoOfPages(12);
        restrictPages.setWitnessShouldNotMoreThanTxt(RESTRICT_NUMBER_PAGES_TEXT1);

        SdoR2SmallClaimsWitnessStatements witnessStatements = new SdoR2SmallClaimsWitnessStatements();
        witnessStatements.setSdoStatementOfWitness(WITNESS_STATEMENT_TEXT);
        witnessStatements.setIsRestrictWitness(YesOrNo.NO);
        witnessStatements.setIsRestrictPages(YesOrNo.NO);
        witnessStatements.setSdoR2SmallClaimsRestrictWitness(restrictWitness);
        witnessStatements.setSdoR2SmallClaimsRestrictPages(restrictPages);
        witnessStatements.setText(WITNESS_DESCRIPTION_TEXT);
        caseData.setSdoR2SmallClaimsWitnessStatements(witnessStatements);
        SdoR2SmallClaimsHearingFirstOpenDateAfter firstOpenDateAfter = new SdoR2SmallClaimsHearingFirstOpenDateAfter();
        firstOpenDateAfter.setListFrom(sdoDeadlineService.calendarDaysFromNow(56));

        SdoR2SmallClaimsHearingWindow hearingWindow = new SdoR2SmallClaimsHearingWindow();
        hearingWindow.setDateTo(sdoDeadlineService.calendarDaysFromNow(70));
        hearingWindow.setListFrom(sdoDeadlineService.calendarDaysFromNow(56));

        SdoR2SmallClaimsBundleOfDocs bundleOfDocs = new SdoR2SmallClaimsBundleOfDocs();
        bundleOfDocs.setPhysicalBundlePartyTxt(BUNDLE_TEXT);

        SdoR2SmallClaimsHearing hearing = new SdoR2SmallClaimsHearing();
        hearing.setTrialOnOptions(HearingOnRadioOptions.OPEN_DATE);
        hearing.setMethodOfHearing(hearingMethodList);
        hearing.setLengthList(SmallClaimsSdoR2TimeEstimate.THIRTY_MINUTES);
        hearing.setPhysicalBundleOptions(SmallClaimsSdoR2PhysicalTrialBundleOptions.PARTY);
        hearing.setSdoR2SmallClaimsHearingFirstOpenDateAfter(firstOpenDateAfter);
        hearing.setSdoR2SmallClaimsHearingWindow(hearingWindow);
        hearing.setHearingCourtLocationList(courtList);
        hearing.setAltHearingCourtLocationList(sdoLocationService.buildLocationList(
            preferredCourt.orElse(null), true, locationRefDataList));
        hearing.setSdoR2SmallClaimsBundleOfDocs(bundleOfDocs);
        caseData.setSdoR2SmallClaimsHearing(hearing);

        SdoR2SmallClaimsImpNotes importantNotes = new SdoR2SmallClaimsImpNotes();
        importantNotes.setText(IMP_NOTES_TEXT);
        importantNotes.setDate(sdoDeadlineService.calendarDaysFromNow(7));
        caseData.setSdoR2SmallClaimsImpNotes(importantNotes);
        caseData.setSdoR2SmallClaimsUploadDocToggle(includeInOrderToggle);
        caseData.setSdoR2SmallClaimsHearingToggle(includeInOrderToggle);
        caseData.setSdoR2SmallClaimsWitnessStatementsToggle(includeInOrderToggle);
        SdoR2WelshLanguageUsage welshLanguageUsage = new SdoR2WelshLanguageUsage();
        welshLanguageUsage.setDescription(WELSH_LANG_DESCRIPTION);
        caseData.setSdoR2DrhUseOfWelshLanguage(welshLanguageUsage);

        sdoJourneyToggleService.applyR2SmallClaimsMediation(caseData, includeInOrderToggle);
    }
}
