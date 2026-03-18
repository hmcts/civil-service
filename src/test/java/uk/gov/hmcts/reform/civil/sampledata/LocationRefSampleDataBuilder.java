package uk.gov.hmcts.reform.civil.sampledata;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration.OTHER;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingSupportRequirements.OTHER_SUPPORT;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingType.IN_PERSON;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.EXTEND_TIME;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

import static java.time.LocalDate.EPOCH;
import static java.util.Collections.singletonList;

import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GAStatementOfTruth;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUnavailabilityDates;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LocationRefSampleDataBuilder {

    protected static final String STRING_CONSTANT = "this is a string";
    protected static final String CASE_MANAGEMENT_CATEGORY = "Civil";
    protected static final String STRING_NUM_CONSTANT = "123456789";
    protected static final LocalDate APP_DATE_EPOCH = EPOCH;

    protected CaseData getTestCaseData(CaseData caseData) {
        return caseData.copy()
                .generalAppType(new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                .generalAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(NO))
                .generalAppPBADetails(new GAPbaDetails())
                .generalAppEvidenceDocument(
                        wrapElements(
                                new Document()
                                        .setDocumentUrl(STRING_CONSTANT)
                                        .setDocumentBinaryUrl(STRING_CONSTANT)
                                        .setDocumentFileName(STRING_CONSTANT)
                                        .setDocumentHash(STRING_CONSTANT)))
                .generalAppDetailsOfOrder(STRING_CONSTANT)
                .generalAppReasonsOfOrder(STRING_CONSTANT)
                .generalAppInformOtherParty(
                        new GAInformOtherParty()
                                .setIsWithNotice(NO)
                                .setReasonsForWithoutNotice(STRING_CONSTANT))
                .generalAppUrgencyRequirement(
                        new GAUrgencyRequirement()
                                .setGeneralAppUrgency(YES)
                                .setReasonsForUrgency(STRING_CONSTANT)
                                .setUrgentAppConsiderationDate(APP_DATE_EPOCH))
                .generalAppStatementOfTruth(
                        new GAStatementOfTruth().setName(STRING_CONSTANT).setRole(STRING_CONSTANT))
                .generalAppHearingDetails(
                        new GAHearingDetails()
                                .setJudgeName(STRING_CONSTANT)
                                .setHearingDate(APP_DATE_EPOCH)
                                .setTrialDateFrom(APP_DATE_EPOCH)
                                .setTrialDateTo(APP_DATE_EPOCH)
                                .setHearingYesorNo(YES)
                                .setHearingDuration(OTHER)
                                .setGeneralAppHearingDays("1")
                                .setGeneralAppHearingHours("2")
                                .setGeneralAppHearingMinutes("30")
                                .setSupportRequirement(singletonList(OTHER_SUPPORT))
                                .setJudgeRequiredYesOrNo(YES)
                                .setTrialRequiredYesOrNo(YES)
                                .setHearingDetailsEmailID(STRING_CONSTANT)
                                .setSupportRequirementOther(STRING_CONSTANT)
                                .setHearingPreferredLocation(new DynamicList())
                                .setGeneralAppUnavailableDates(
                                        wrapElements(
                                                new GAUnavailabilityDates()
                                                        .setUnavailableTrialDateFrom(APP_DATE_EPOCH)
                                                        .setUnavailableTrialDateTo(APP_DATE_EPOCH)))
                                .setHearingDetailsTelephoneNumber(STRING_NUM_CONSTANT)
                                .setReasonForPreferredHearingType(STRING_CONSTANT)
                                .setTelephoneHearingPreferredType(STRING_CONSTANT)
                                .setSupportRequirementSignLanguage(STRING_CONSTANT)
                                .setHearingPreferencesPreferredType(IN_PERSON)
                                .setUnavailableTrialRequiredYesOrNo(YES)
                                .setSupportRequirementLanguageInterpreter(STRING_CONSTANT))
                .build();
    }

    protected CaseData getTestCaseDataForUrgencyCheckMidEvent(
            CaseData caseData, boolean isApplicationUrgent, LocalDate urgencyConsiderationDate) {
        GAUrgencyRequirement gaUrgencyRequirement =
                new GAUrgencyRequirement().setUrgentAppConsiderationDate(urgencyConsiderationDate);
        if (isApplicationUrgent) {
            gaUrgencyRequirement.setGeneralAppUrgency(YES).setReasonsForUrgency(STRING_CONSTANT);
        } else {
            gaUrgencyRequirement.setGeneralAppUrgency(NO);
        }
        return caseData.copy()
                .generalAppType(new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                .generalAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(NO))
                .generalAppPBADetails(new GAPbaDetails())
                .generalAppDetailsOfOrder(STRING_CONSTANT)
                .generalAppReasonsOfOrder(STRING_CONSTANT)
                .generalAppInformOtherParty(
                        new GAInformOtherParty()
                                .setIsWithNotice(NO)
                                .setReasonsForWithoutNotice(STRING_CONSTANT))
                .generalAppUrgencyRequirement(gaUrgencyRequirement)
                .generalAppStatementOfTruth(
                        new GAStatementOfTruth().setName(STRING_CONSTANT).setRole(STRING_CONSTANT))
                .generalAppEvidenceDocument(
                        wrapElements(
                                new Document()
                                        .setDocumentUrl(STRING_CONSTANT)
                                        .setDocumentBinaryUrl(STRING_CONSTANT)
                                        .setDocumentFileName(STRING_CONSTANT)
                                        .setDocumentHash(STRING_CONSTANT)))
                .generalAppHearingDetails(
                        new GAHearingDetails()
                                .setJudgeName(STRING_CONSTANT)
                                .setHearingDate(APP_DATE_EPOCH)
                                .setTrialDateFrom(APP_DATE_EPOCH)
                                .setTrialDateTo(APP_DATE_EPOCH)
                                .setHearingYesorNo(YES)
                                .setHearingDuration(OTHER)
                                .setGeneralAppHearingDays("1")
                                .setGeneralAppHearingHours("2")
                                .setGeneralAppHearingMinutes("30")
                                .setSupportRequirement(singletonList(OTHER_SUPPORT))
                                .setJudgeRequiredYesOrNo(YES)
                                .setTrialRequiredYesOrNo(YES)
                                .setHearingDetailsEmailID(STRING_CONSTANT)
                                .setGeneralAppUnavailableDates(
                                        wrapElements(
                                                new GAUnavailabilityDates()
                                                        .setUnavailableTrialDateFrom(APP_DATE_EPOCH)
                                                        .setUnavailableTrialDateTo(APP_DATE_EPOCH)))
                                .setSupportRequirementOther(STRING_CONSTANT)
                                .setHearingPreferredLocation(new DynamicList())
                                .setHearingDetailsTelephoneNumber(STRING_NUM_CONSTANT)
                                .setReasonForPreferredHearingType(STRING_CONSTANT)
                                .setTelephoneHearingPreferredType(STRING_CONSTANT)
                                .setSupportRequirementSignLanguage(STRING_CONSTANT)
                                .setHearingPreferencesPreferredType(IN_PERSON)
                                .setUnavailableTrialRequiredYesOrNo(YES)
                                .setSupportRequirementLanguageInterpreter(STRING_CONSTANT))
                .build();
    }

    protected CaseData getTestCaseDataForHearingMidEvent(
            CaseData caseData,
            boolean isTrialScheduled,
            LocalDate trialDateFrom,
            LocalDate trialDateTo,
            boolean isApplicantUnavailable,
            List<Element<GAUnavailabilityDates>> unavailabilityDates) {
        GAHearingDetails builder = new GAHearingDetails();
        if (isTrialScheduled) {
            builder.setTrialRequiredYesOrNo(YES);
        } else {
            builder.setTrialRequiredYesOrNo(NO);
        }
        if (isApplicantUnavailable) {
            builder.setUnavailableTrialRequiredYesOrNo(YES);
        } else {
            builder.setUnavailableTrialRequiredYesOrNo(NO);
        }
        builder.setTrialDateFrom(trialDateFrom);
        builder.setTrialDateTo(trialDateTo);
        builder.setGeneralAppUnavailableDates(unavailabilityDates);
        return getTestCaseData(CaseDataBuilder.builder().build()).copy()
                .generalAppHearingDetails(builder)
                .build();
    }

    protected CaseData getTestCaseDataWithEmptyCollectionOfApps(CaseData caseData) {
        return getTestCaseData(caseData);
    }

    protected List<Element<GAUnavailabilityDates>> getValidUnavailableDateList() {
        GAUnavailabilityDates range1 =
                new GAUnavailabilityDates()
                        .setUnavailableTrialDateFrom(LocalDate.now())
                        .setUnavailableTrialDateTo(LocalDate.now().plusDays(2));
        GAUnavailabilityDates range2 =
                new GAUnavailabilityDates()
                        .setUnavailableTrialDateFrom(LocalDate.now().plusDays(2))
                        .setUnavailableTrialDateTo(LocalDate.now().plusDays(2));
        return wrapElements(range1, range2);
    }

    protected CaseData getTestCaseDataCollectionOfApps(CaseData caseData) {
        GeneralApplication application =
                new GeneralApplication()
                        .setGeneralAppType(
                                new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                        .setGeneralAppRespondentAgreement(
                                new GARespondentOrderAgreement().setHasAgreed(NO))
                        .setGeneralAppPBADetails(new GAPbaDetails())
                        .setGeneralAppDetailsOfOrder(STRING_CONSTANT)
                        .setGeneralAppReasonsOfOrder(STRING_CONSTANT)
                        .setGeneralAppInformOtherParty(
                                new GAInformOtherParty()
                                        .setIsWithNotice(NO)
                                        .setReasonsForWithoutNotice(STRING_CONSTANT))
                        .setGeneralAppUrgencyRequirement(
                                new GAUrgencyRequirement()
                                        .setGeneralAppUrgency(YES)
                                        .setReasonsForUrgency(STRING_CONSTANT)
                                        .setUrgentAppConsiderationDate(APP_DATE_EPOCH))
                        .setGeneralAppStatementOfTruth(
                                new GAStatementOfTruth()
                                        .setName(STRING_CONSTANT)
                                        .setRole(STRING_CONSTANT))
                        .setGeneralAppEvidenceDocument(
                                wrapElements(
                                        new Document()
                                                .setDocumentUrl(STRING_CONSTANT)
                                                .setDocumentBinaryUrl(STRING_CONSTANT)
                                                .setDocumentFileName(STRING_CONSTANT)
                                                .setDocumentHash(STRING_CONSTANT)))
                        .setGeneralAppHearingDetails(
                                new GAHearingDetails()
                                        .setJudgeName(STRING_CONSTANT)
                                        .setHearingDate(APP_DATE_EPOCH)
                                        .setTrialDateFrom(APP_DATE_EPOCH)
                                        .setTrialDateTo(APP_DATE_EPOCH)
                                        .setHearingYesorNo(YES)
                                        .setHearingDuration(OTHER)
                                        .setGeneralAppHearingDays("1")
                                        .setGeneralAppHearingHours("2")
                                        .setGeneralAppHearingMinutes("30")
                                        .setSupportRequirement(singletonList(OTHER_SUPPORT))
                                        .setJudgeRequiredYesOrNo(YES)
                                        .setTrialRequiredYesOrNo(YES)
                                        .setHearingDetailsEmailID(STRING_CONSTANT)
                                        .setGeneralAppUnavailableDates(
                                                wrapElements(
                                                        new GAUnavailabilityDates()
                                                                .setUnavailableTrialDateFrom(
                                                                        APP_DATE_EPOCH)
                                                                .setUnavailableTrialDateTo(
                                                                        APP_DATE_EPOCH)))
                                        .setSupportRequirementOther(STRING_CONSTANT)
                                        .setHearingPreferredLocation(new DynamicList())
                                        .setHearingDetailsTelephoneNumber(STRING_NUM_CONSTANT)
                                        .setReasonForPreferredHearingType(STRING_CONSTANT)
                                        .setTelephoneHearingPreferredType(STRING_CONSTANT)
                                        .setSupportRequirementSignLanguage(STRING_CONSTANT)
                                        .setHearingPreferencesPreferredType(IN_PERSON)
                                        .setUnavailableTrialRequiredYesOrNo(YES)
                                        .setSupportRequirementLanguageInterpreter(STRING_CONSTANT));
        return getTestCaseData(caseData).copy()
                .generalApplications(wrapElements(application))
                .build();
    }

    protected DynamicList getLocationDynamicList(CaseData responseCaseData) {
        return responseCaseData.getGeneralAppHearingDetails().getHearingPreferredLocation();
    }

    protected DynamicList getLocationDynamicListInPersonHearing(CaseData responseCaseData) {
        return responseCaseData.getDisposalHearingMethodInPerson();
    }

    protected List<String> locationsFromDynamicList(DynamicList dynamicList) {
        return dynamicList.getListItems().stream().map(DynamicListElement::getLabel).toList();
    }

    protected List<LocationRefData> getSampleCourLocationsRefObject() {
        return new ArrayList<>(
                List.of(
                        new LocationRefData()
                                .setEpimmsId("111")
                                .setSiteName("Site 1")
                                .setCourtAddress("Adr 1")
                                .setPostcode("AAA 111")
                                .setCourtLocationCode("court1"),
                        new LocationRefData()
                                .setEpimmsId("222")
                                .setSiteName("Site 2")
                                .setCourtAddress("Adr 2")
                                .setPostcode("BBB 222")
                                .setCourtLocationCode("court2"),
                        new LocationRefData()
                                .setEpimmsId("333")
                                .setSiteName("Site 3")
                                .setCourtAddress("Adr 3")
                                .setPostcode("CCC 333")
                                .setCourtLocationCode("court3"),
                        new LocationRefData()
                                .setEpimmsId("00000")
                                .setSiteName("Site 5")
                                .setCourtAddress("Adr 5")
                                .setPostcode("YYY 111")
                                .setCourtLocationCode("court5")));
    }

    protected List<LocationRefData> getSampleCourLocationsRefObjectToSort() {
        return new ArrayList<>(
                List.of(
                        new LocationRefData()
                                .setEpimmsId("111")
                                .setSiteName("Site 1")
                                .setCourtAddress("Adr 1")
                                .setPostcode("VVV 111")
                                .setCourtLocationCode("court1"),
                        new LocationRefData()
                                .setEpimmsId("222")
                                .setSiteName("Site 2")
                                .setCourtAddress("Adr 2")
                                .setPostcode("BBB 222")
                                .setCourtLocationCode("court2"),
                        new LocationRefData()
                                .setEpimmsId("333")
                                .setSiteName("Site 3")
                                .setCourtAddress("Adr 3")
                                .setPostcode("CCC 333")
                                .setCourtLocationCode("court3"),
                        new LocationRefData()
                                .setEpimmsId("444")
                                .setSiteName("A Site 3")
                                .setCourtAddress("Adr 3")
                                .setPostcode("AAA 111")
                                .setCourtLocationCode("court4"),
                        new LocationRefData()
                                .setEpimmsId("00000")
                                .setSiteName("Site 5")
                                .setCourtAddress("Adr 5")
                                .setPostcode("YYY 111")
                                .setCourtLocationCode("court5")));
    }
}
