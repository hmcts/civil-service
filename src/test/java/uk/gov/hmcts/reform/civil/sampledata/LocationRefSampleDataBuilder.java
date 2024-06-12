package uk.gov.hmcts.reform.civil.sampledata;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
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

import static java.time.LocalDate.EPOCH;
import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration.OTHER;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingSupportRequirements.OTHER_SUPPORT;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingType.IN_PERSON;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.EXTEND_TIME;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

public class LocationRefSampleDataBuilder {

    protected static final String STRING_CONSTANT = "this is a string";
    protected static final String CASE_MANAGEMENT_CATEGORY = "Civil";
    protected static final String STRING_NUM_CONSTANT = "123456789";
    protected static final DynamicList PBA_ACCOUNTS = DynamicList.builder().build();
    protected static final LocalDate APP_DATE_EPOCH = EPOCH;
    protected static final DynamicList PBALIST = DynamicList.builder().build();

    protected CaseData getTestCaseData(CaseData caseData) {
        return caseData.toBuilder()
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(NO)
                                               .build())
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .build())
            .generalAppEvidenceDocument(wrapElements(Document.builder()
                                                         .documentUrl(STRING_CONSTANT)
                                                         .documentBinaryUrl(STRING_CONSTANT)
                                                         .documentFileName(STRING_CONSTANT)
                                                         .documentHash(STRING_CONSTANT)
                                                         .build()))
            .generalAppDetailsOfOrder(STRING_CONSTANT)
            .generalAppReasonsOfOrder(STRING_CONSTANT)
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(NO)
                                            .reasonsForWithoutNotice(STRING_CONSTANT)
                                            .build())
            .generalAppUrgencyRequirement(GAUrgencyRequirement.builder()
                                              .generalAppUrgency(YES)
                                              .reasonsForUrgency(STRING_CONSTANT)
                                              .urgentAppConsiderationDate(APP_DATE_EPOCH)
                                              .build())
            .generalAppStatementOfTruth(GAStatementOfTruth.builder()
                                            .name(STRING_CONSTANT)
                                            .role(STRING_CONSTANT)
                                            .build())
            .generalAppHearingDetails(GAHearingDetails.builder()
                                          .judgeName(STRING_CONSTANT)
                                          .hearingDate(APP_DATE_EPOCH)
                                          .trialDateFrom(APP_DATE_EPOCH)
                                          .trialDateTo(APP_DATE_EPOCH)
                                          .hearingYesorNo(YES)
                                          .hearingDuration(OTHER)
                                          .generalAppHearingDays("1")
                                          .generalAppHearingHours("2")
                                          .generalAppHearingMinutes("30")
                                          .supportRequirement(singletonList(OTHER_SUPPORT))
                                          .judgeRequiredYesOrNo(YES)
                                          .trialRequiredYesOrNo(YES)
                                          .hearingDetailsEmailID(STRING_CONSTANT)
                                          .supportRequirementOther(STRING_CONSTANT)
                                          .hearingPreferredLocation(DynamicList.builder().build())
                                          .generalAppUnavailableDates(wrapElements(GAUnavailabilityDates.builder()
                                                                                       .unavailableTrialDateFrom(
                                                                                           APP_DATE_EPOCH)
                                                                                       .unavailableTrialDateTo(
                                                                                           APP_DATE_EPOCH)
                                                                                       .build()))
                                          .hearingDetailsTelephoneNumber(STRING_NUM_CONSTANT)
                                          .reasonForPreferredHearingType(STRING_CONSTANT)
                                          .telephoneHearingPreferredType(STRING_CONSTANT)
                                          .supportRequirementSignLanguage(STRING_CONSTANT)
                                          .hearingPreferencesPreferredType(IN_PERSON)
                                          .unavailableTrialRequiredYesOrNo(YES)
                                          .supportRequirementLanguageInterpreter(STRING_CONSTANT)
                                          .build())
            .build();
    }

    protected CaseData getTestCaseDataForUrgencyCheckMidEvent(CaseData caseData, boolean isApplicationUrgent,
                                                              LocalDate urgencyConsiderationDate) {
        GAUrgencyRequirement.GAUrgencyRequirementBuilder urBuilder = GAUrgencyRequirement.builder();
        if (isApplicationUrgent) {
            urBuilder.generalAppUrgency(YES)
                .reasonsForUrgency(STRING_CONSTANT);
        } else {
            urBuilder.generalAppUrgency(NO);
        }
        urBuilder.urgentAppConsiderationDate(urgencyConsiderationDate);
        GAUrgencyRequirement gaUrgencyRequirement = urBuilder.build();
        return caseData.toBuilder()
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(NO)
                                               .build())
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .build())
            .generalAppDetailsOfOrder(STRING_CONSTANT)
            .generalAppReasonsOfOrder(STRING_CONSTANT)
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(NO)
                                            .reasonsForWithoutNotice(STRING_CONSTANT)
                                            .build())
            .generalAppUrgencyRequirement(gaUrgencyRequirement)
            .generalAppStatementOfTruth(GAStatementOfTruth.builder()
                                            .name(STRING_CONSTANT)
                                            .role(STRING_CONSTANT)
                                            .build())
            .generalAppEvidenceDocument(wrapElements(Document.builder()
                                                         .documentUrl(STRING_CONSTANT)
                                                         .documentBinaryUrl(STRING_CONSTANT)
                                                         .documentFileName(STRING_CONSTANT)
                                                         .documentHash(STRING_CONSTANT)
                                                         .build()))
            .generalAppHearingDetails(GAHearingDetails.builder()
                                          .judgeName(STRING_CONSTANT)
                                          .hearingDate(APP_DATE_EPOCH)
                                          .trialDateFrom(APP_DATE_EPOCH)
                                          .trialDateTo(APP_DATE_EPOCH)
                                          .hearingYesorNo(YES)
                                          .hearingDuration(OTHER)
                                          .generalAppHearingDays("1")
                                          .generalAppHearingHours("2")
                                          .generalAppHearingMinutes("30")
                                          .supportRequirement(singletonList(OTHER_SUPPORT))
                                          .judgeRequiredYesOrNo(YES)
                                          .trialRequiredYesOrNo(YES)
                                          .hearingDetailsEmailID(STRING_CONSTANT)
                                          .generalAppUnavailableDates(wrapElements(GAUnavailabilityDates.builder()
                                                                                       .unavailableTrialDateFrom(
                                                                                           APP_DATE_EPOCH)
                                                                                       .unavailableTrialDateTo(
                                                                                           APP_DATE_EPOCH).build()))
                                          .supportRequirementOther(STRING_CONSTANT)
                                          .hearingPreferredLocation(DynamicList.builder().build())
                                          .hearingDetailsTelephoneNumber(STRING_NUM_CONSTANT)
                                          .reasonForPreferredHearingType(STRING_CONSTANT)
                                          .telephoneHearingPreferredType(STRING_CONSTANT)
                                          .supportRequirementSignLanguage(STRING_CONSTANT)
                                          .hearingPreferencesPreferredType(IN_PERSON)
                                          .unavailableTrialRequiredYesOrNo(YES)
                                          .supportRequirementLanguageInterpreter(STRING_CONSTANT)
                                          .build())
            .build();
    }

    protected CaseData getTestCaseDataForHearingMidEvent(CaseData caseData, boolean isTrialScheduled,
                                                         LocalDate trialDateFrom, LocalDate trialDateTo,
                                                         boolean isApplicantUnavailable,
                                                         List<Element<GAUnavailabilityDates>> unavailabilityDates) {
        GAHearingDetails.GAHearingDetailsBuilder builder = GAHearingDetails.builder();
        if (isTrialScheduled) {
            builder.trialRequiredYesOrNo(YES);
        } else {
            builder.trialRequiredYesOrNo(NO);
        }
        if (isApplicantUnavailable) {
            builder.unavailableTrialRequiredYesOrNo(YES);
        } else {
            builder.unavailableTrialRequiredYesOrNo(NO);
        }
        builder.trialDateFrom(trialDateFrom);
        builder.trialDateTo(trialDateTo);
        builder.generalAppUnavailableDates(unavailabilityDates);
        return getTestCaseData(CaseDataBuilder.builder().build()).toBuilder()
            .generalAppHearingDetails(builder.build())
            .build();
    }

    protected CaseData getTestCaseDataWithEmptyCollectionOfApps(CaseData caseData) {
        return getTestCaseData(caseData);
    }

    protected List<Element<GAUnavailabilityDates>> getValidUnavailableDateList() {
        GAUnavailabilityDates range1 = GAUnavailabilityDates.builder()
            .unavailableTrialDateFrom(LocalDate.now())
            .unavailableTrialDateTo(LocalDate.now().plusDays(2))
            .build();
        GAUnavailabilityDates range2 = GAUnavailabilityDates.builder()
            .unavailableTrialDateFrom(LocalDate.now().plusDays(2))
            .unavailableTrialDateTo(LocalDate.now().plusDays(2))
            .build();
        return wrapElements(range1, range2);
    }

    protected CaseData getTestCaseDataCollectionOfApps(CaseData caseData) {
        GeneralApplication application = GeneralApplication.builder()
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(NO)
                                               .build())
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .build())
            .generalAppDetailsOfOrder(STRING_CONSTANT)
            .generalAppReasonsOfOrder(STRING_CONSTANT)
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(NO)
                                            .reasonsForWithoutNotice(STRING_CONSTANT)
                                            .build())
            .generalAppUrgencyRequirement(GAUrgencyRequirement.builder()
                                              .generalAppUrgency(YES)
                                              .reasonsForUrgency(STRING_CONSTANT)
                                              .urgentAppConsiderationDate(APP_DATE_EPOCH)
                                              .build())
            .generalAppStatementOfTruth(GAStatementOfTruth.builder()
                                            .name(STRING_CONSTANT)
                                            .role(STRING_CONSTANT)
                                            .build())
            .generalAppEvidenceDocument(wrapElements(Document.builder()
                                                         .documentUrl(STRING_CONSTANT)
                                                         .documentBinaryUrl(STRING_CONSTANT)
                                                         .documentFileName(STRING_CONSTANT)
                                                         .documentHash(STRING_CONSTANT)
                                                         .build()))
            .generalAppHearingDetails(GAHearingDetails.builder()
                                          .judgeName(STRING_CONSTANT)
                                          .hearingDate(APP_DATE_EPOCH)
                                          .trialDateFrom(APP_DATE_EPOCH)
                                          .trialDateTo(APP_DATE_EPOCH)
                                          .hearingYesorNo(YES)
                                          .hearingDuration(OTHER)
                                          .generalAppHearingDays("1")
                                          .generalAppHearingHours("2")
                                          .generalAppHearingMinutes("30")
                                          .supportRequirement(singletonList(OTHER_SUPPORT))
                                          .judgeRequiredYesOrNo(YES)
                                          .trialRequiredYesOrNo(YES)
                                          .hearingDetailsEmailID(STRING_CONSTANT)
                                          .generalAppUnavailableDates(wrapElements(GAUnavailabilityDates.builder()
                                                                                       .unavailableTrialDateFrom(
                                                                                           APP_DATE_EPOCH)
                                                                                       .unavailableTrialDateTo(
                                                                                           APP_DATE_EPOCH).build()))
                                          .supportRequirementOther(STRING_CONSTANT)
                                          .hearingPreferredLocation(DynamicList.builder().build())
                                          .hearingDetailsTelephoneNumber(STRING_NUM_CONSTANT)
                                          .reasonForPreferredHearingType(STRING_CONSTANT)
                                          .telephoneHearingPreferredType(STRING_CONSTANT)
                                          .supportRequirementSignLanguage(STRING_CONSTANT)
                                          .hearingPreferencesPreferredType(IN_PERSON)
                                          .unavailableTrialRequiredYesOrNo(YES)
                                          .supportRequirementLanguageInterpreter(STRING_CONSTANT)
                                          .build())
            .build();
        return getTestCaseData(caseData)
            .toBuilder()
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
        return dynamicList.getListItems().stream()
            .map(DynamicListElement::getLabel)
            .toList();
    }

    protected List<LocationRefData> getSampleCourLocationsRefObject() {
        return new ArrayList<>(List.of(
            LocationRefData.builder()
                .epimmsId("111").siteName("Site 1").courtAddress("Adr 1").postcode("AAA 111")
                .courtLocationCode("court1").build(),
            LocationRefData.builder()
                .epimmsId("222").siteName("Site 2").courtAddress("Adr 2").postcode("BBB 222")
                .courtLocationCode("court2").build(),
            LocationRefData.builder()
                .epimmsId("333").siteName("Site 3").courtAddress("Adr 3").postcode("CCC 333")
                .courtLocationCode("court3").build()
        ));
    }

    protected List<LocationRefData> getSampleCourLocationsRefObjectToSort() {
        return new ArrayList<>(List.of(
            LocationRefData.builder()
                .epimmsId("111").siteName("Site 1").courtAddress("Adr 1").postcode("VVV 111")
                .courtLocationCode("court1").build(),
            LocationRefData.builder()
                .epimmsId("222").siteName("Site 2").courtAddress("Adr 2").postcode("BBB 222")
                .courtLocationCode("court2").build(),
            LocationRefData.builder()
                .epimmsId("333").siteName("Site 3").courtAddress("Adr 3").postcode("CCC 333")
                .courtLocationCode("court3").build(),
            LocationRefData.builder()
                .epimmsId("444").siteName("A Site 3").courtAddress("Adr 3").postcode("AAA 111")
                .courtLocationCode("court4").build()
        ));
    }
}
