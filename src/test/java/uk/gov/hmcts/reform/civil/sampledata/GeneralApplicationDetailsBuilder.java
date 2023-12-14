package uk.gov.hmcts.reform.civil.sampledata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.lang.Collections;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CourtLocation;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLink;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GAStatementOfTruth;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUnavailabilityDates;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.time.LocalDate.EPOCH;
import static java.time.LocalDateTime.now;
import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration.OTHER;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingSupportRequirements.OTHER_SUPPORT;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingType.IN_PERSON;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.EXTEND_TIME;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.SUMMARY_JUDGEMENT;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.VARY_JUDGEMENT;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.GENERAL_ORDER;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@SuppressWarnings("unchecked")
public class GeneralApplicationDetailsBuilder {

    public static final String STRING_CONSTANT = "this is a string";
    public static final String STRING_NUM_CONSTANT = "123456789";
    public static final String APPLICANT_EMAIL_ID_CONSTANT = "testUser@gmail.com";
    public static final String RESPONDENT_EMAIL_ID_CONSTANT = "respondent@gmail.com";
    public static final DynamicList PBA_ACCOUNTS = DynamicList.builder().build();
    public static final LocalDate APP_DATE_EPOCH = EPOCH;
    public static final DynamicList PBALIST = DynamicList.builder().build();

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static GeneralApplicationDetailsBuilder builder() {
        return new GeneralApplicationDetailsBuilder();
    }

    public CaseData getTestCaseDataForUrgencyCheckMidEvent(CaseData caseData, boolean isApplicationUrgent,
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
            .ccdCaseReference(1234L)
            .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                                               .organisation(Organisation.builder()
                                                                 .organisationID(STRING_CONSTANT).build())
                                               .orgPolicyReference(STRING_CONSTANT).build())
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
                .generalAppEvidenceDocument(wrapElements(Document.builder().documentUrl(STRING_CONSTANT).build()))
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
                                .unavailableTrialDateFrom(APP_DATE_EPOCH)
                                .unavailableTrialDateTo(APP_DATE_EPOCH).build()))
                        .supportRequirementOther(STRING_CONSTANT)
                        .hearingPreferredLocation(getPreferredLoc())
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

    public CaseData getTestCaseDataForApplicationFee(CaseData caseData, boolean isConsented,
                                                     boolean isWithNotice) {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        if (!isConsented) {
            caseDataBuilder.generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                    .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isWithNotice ? YES : NO)
                            .reasonsForWithoutNotice(isWithNotice ? null : STRING_CONSTANT)
                            .build());
        } else {
            caseDataBuilder.generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YES).build())
                    .generalAppInformOtherParty(null);
        }
        return caseDataBuilder
            .ccdCaseReference(1234L)
            .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                                               .organisation(Organisation.builder()
                                                                 .organisationID(STRING_CONSTANT).build())
                                               .orgPolicyReference(STRING_CONSTANT).build())
            .generalAppType(GAApplicationType.builder()
                        .types(singletonList(EXTEND_TIME))
                        .build())
                .generalAppPBADetails(GAPbaDetails.builder()
                        .build())
                .generalAppDetailsOfOrder(STRING_CONSTANT)
                .generalAppReasonsOfOrder(STRING_CONSTANT)
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder()
                        .generalAppUrgency(YES)
                        .reasonsForUrgency(STRING_CONSTANT)
                        .build())
                .generalAppStatementOfTruth(GAStatementOfTruth.builder()
                        .name(STRING_CONSTANT)
                        .role(STRING_CONSTANT)
                        .build())
                .generalAppEvidenceDocument(wrapElements(Document.builder().documentUrl(STRING_CONSTANT).build()))
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
                                .unavailableTrialDateFrom(APP_DATE_EPOCH)
                                .unavailableTrialDateTo(APP_DATE_EPOCH).build()))
                        .supportRequirementOther(STRING_CONSTANT)
                        .hearingPreferredLocation(getPreferredLoc())
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

    public CaseData getTestCaseData(CaseData caseData) {

        return caseData.toBuilder()
            .ccdCaseReference(1234L)
            .generalAppType(GAApplicationType.builder()
                        .types(singletonList(EXTEND_TIME))
                        .build())
            .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                                               .organisation(Organisation.builder()
                                                                 .organisationID(STRING_CONSTANT).build())
                                               .orgPolicyReference(STRING_CONSTANT).build())
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                        .hasAgreed(NO)
                        .build())
                .generalAppPBADetails(GAPbaDetails.builder()
                        .build())
                .generalApplications(wrapElements(getGeneralApplication()))
                .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                        .id(STRING_CONSTANT)
                        .email(APPLICANT_EMAIL_ID_CONSTANT).build())
                .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                        .organisation(Organisation.builder()
                                .organisationID(STRING_CONSTANT).build())
                        .orgPolicyReference(STRING_CONSTANT).build())
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                        .organisation(Organisation.builder()
                                .organisationID(STRING_CONSTANT).build())
                        .orgPolicyReference(STRING_CONSTANT).build())
                .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
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
                .generalAppEvidenceDocument(wrapElements(Document.builder().documentUrl(STRING_CONSTANT).build()))
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
                                .unavailableTrialDateTo(APP_DATE_EPOCH)
                                .unavailableTrialDateFrom(APP_DATE_EPOCH).build()))
                        .supportRequirementOther(STRING_CONSTANT)
                        .hearingPreferredLocation(getPreferredLoc())
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

    public CaseData getTestCaseDataWithDetails(CaseData caseData,
                                               boolean withGADetails,
                                               boolean withGADetailsResp,
                                               boolean withGADetailsResp2, boolean withGADetailsMaster,
                                               Map<String, String> applicationIdStatus) {

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.caseManagementLocation(CaseLocationCivil.builder().baseLocation("00000")
                                                   .region("2").build());
        caseDataBuilder.locationName("locationOfRegion2");
        caseDataBuilder.ccdCaseReference(1L);
        if (!Collections.isEmpty(applicationIdStatus)) {
            List<GeneralApplication> genApps = new ArrayList<>();
            applicationIdStatus.forEach((key, value) -> genApps.add(getGeneralApplication(key)));
            caseDataBuilder.generalApplications(wrapElements(genApps.toArray(new GeneralApplication[0])));
        }

        if (withGADetails) {
            List<GeneralApplicationsDetails> allGaDetails = new ArrayList<>();
            applicationIdStatus.forEach((key, value) -> allGaDetails.add(getGADetails(key, value)));
            caseDataBuilder.claimantGaAppDetails(
                    wrapElements(allGaDetails.toArray(new GeneralApplicationsDetails[0])
            ));
        }

        if (withGADetailsMaster) {
            List<GeneralApplicationsDetails> allGaDetails = new ArrayList<>();
            applicationIdStatus.forEach((key, value) -> allGaDetails.add(getGADetails(key, value)));
            caseDataBuilder.gaDetailsMasterCollection(
                wrapElements(allGaDetails.toArray(new GeneralApplicationsDetails[0])
                ));
        }

        List<GADetailsRespondentSol> gaDetailsRespo = new ArrayList<>();
        applicationIdStatus.forEach((key, value) -> gaDetailsRespo.add(getGADetailsRespondent(key, value)));
        if (withGADetailsResp) {
            caseDataBuilder.respondentSolGaAppDetails(wrapElements(gaDetailsRespo
                                                                       .toArray(new GADetailsRespondentSol[0])));
        }

        if (withGADetailsResp2) {
            caseDataBuilder.respondentSolTwoGaAppDetails(wrapElements(gaDetailsRespo
                                                                       .toArray(new GADetailsRespondentSol[0])));
        }
        return caseDataBuilder.build();
    }

    public CaseData getTestCaseDataWithLocationDetails(CaseData caseData,
                                               boolean withGADetails,
                                               boolean withGADetailsResp,
                                               boolean withGADetailsResp2, boolean withGADetailsMaster,
                                               Map<String, String> applicationIdStatus) {

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.caseManagementLocation(CaseLocationCivil.builder().baseLocation("000000")
                                                   .region("2").build());
        caseDataBuilder.ccdCaseReference(1L);
        if (!Collections.isEmpty(applicationIdStatus)) {
            List<GeneralApplication> genApps = new ArrayList<>();
            applicationIdStatus.forEach((key, value) -> genApps.add(getGeneralApplication(key)));
            caseDataBuilder.generalApplications(wrapElements(genApps.toArray(new GeneralApplication[0])));
        }

        if (withGADetails) {
            List<GeneralApplicationsDetails> allGaDetails = new ArrayList<>();
            applicationIdStatus.forEach((key, value) -> allGaDetails.add(getGADetails(key, value)));
            caseDataBuilder.claimantGaAppDetails(
                wrapElements(allGaDetails.toArray(new GeneralApplicationsDetails[0])
                ));
        }

        if (withGADetailsMaster) {
            List<GeneralApplicationsDetails> allGaDetails = new ArrayList<>();
            applicationIdStatus.forEach((key, value) -> allGaDetails.add(getGADetails(key, value)));
            caseDataBuilder.gaDetailsMasterCollection(
                wrapElements(allGaDetails.toArray(new GeneralApplicationsDetails[0])
                ));
        }

        List<GADetailsRespondentSol> gaDetailsRespo = new ArrayList<>();
        applicationIdStatus.forEach((key, value) -> gaDetailsRespo.add(getGADetailsRespondent(key, value)));
        if (withGADetailsResp) {
            caseDataBuilder.respondentSolGaAppDetails(wrapElements(gaDetailsRespo
                                                                       .toArray(new GADetailsRespondentSol[0])));
        }

        if (withGADetailsResp2) {
            caseDataBuilder.respondentSolTwoGaAppDetails(wrapElements(gaDetailsRespo
                                                                          .toArray(new GADetailsRespondentSol[0])));
        }
        return caseDataBuilder.build();
    }

    private GeneralApplicationsDetails getGADetails(String applicationId, String caseState) {
        return GeneralApplicationsDetails.builder()
                .generalApplicationType("Summary Judgement")
                .generalAppSubmittedDateGAspec(now())
                .caseLink(CaseLink.builder().caseReference(applicationId).build())
                .caseState(caseState)
                .build();
    }

    private GADetailsRespondentSol getGADetailsRespondent(String applicationId, String caseState) {
        return GADetailsRespondentSol.builder()
                .generalApplicationType("Summary Judgement")
                .generalAppSubmittedDateGAspec(now())
                .caseLink(CaseLink.builder().caseReference(applicationId).build())
                .caseState(caseState)
                .build();
    }

    public CaseData getTestCaseDataWithEmptyPreferredLocation(CaseData caseData) {

        return caseData.toBuilder()
            .ccdCaseReference(1234L)
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                                               .organisation(Organisation.builder()
                                                                 .organisationID(STRING_CONSTANT).build())
                                               .orgPolicyReference(STRING_CONSTANT).build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(NO)
                                               .build())
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .build())
            .generalApplications(wrapElements(getGeneralApplication()))
            .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                .id(STRING_CONSTANT)
                                                .email(APPLICANT_EMAIL_ID_CONSTANT).build())
            .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                              .organisation(Organisation.builder()
                                                                .organisationID(STRING_CONSTANT).build())
                                              .orgPolicyReference(STRING_CONSTANT).build())
            .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                               .organisation(Organisation.builder()
                                                                 .organisationID(STRING_CONSTANT).build())
                                               .orgPolicyReference(STRING_CONSTANT).build())
            .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
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
            .generalAppEvidenceDocument(wrapElements(Document.builder().documentUrl(STRING_CONSTANT).build()))
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
                                          .generalAppUnavailableDates(wrapElements(
                                              GAUnavailabilityDates.builder()
                                                  .unavailableTrialDateTo(APP_DATE_EPOCH)
                                                  .unavailableTrialDateFrom(APP_DATE_EPOCH)
                                                  .build()))
                                          .supportRequirementOther(STRING_CONSTANT)
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

    public CaseData getTestCaseDataWithEmptyCollectionOfApps(CaseData caseData) {
        return caseData.toBuilder()
            .ccdCaseReference(1234L)
            .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                                               .organisation(Organisation.builder()
                                                                 .organisationID(STRING_CONSTANT).build())
                                               .orgPolicyReference(STRING_CONSTANT).build())
                .applicant1(Party.builder().type(Party.Type.COMPANY).companyName("Applicant1").build())
                .applicant1DQ(Applicant1DQ.builder()
                        .applicant1DQRequestedCourt(RequestedCourt.builder()
                                .responseCourtCode("applicant1DQRequestedCourt")
                                                        .caseLocation(CaseLocationCivil.builder()
                                                                          .region("2")
                                                                          .baseLocation("00000")
                                                                          .build())
                                .build())
                        .build())
                .respondent1(Party.builder().type(Party.Type.COMPANY).companyName("Respondent1").build())
                .respondent1DQ(Respondent1DQ.builder()
                        .respondent1DQRequestedCourt(RequestedCourt.builder()
                                .responseCourtCode("respondent1DQRequestedCourt")
                                                         .caseLocation(CaseLocationCivil.builder()
                                                                           .region("2")
                                                                           .baseLocation("11111")
                                                                           .build())
                                .build())
                        .build())
                .addApplicant2(YES)
                .applicant2(Party.builder().type(Party.Type.COMPANY).companyName("Applicant2").build())
                .addRespondent2(YES)
                .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("Respondent2").build())
                .generalAppType(GAApplicationType.builder()
                        .types(singletonList(EXTEND_TIME))
                        .build())
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                        .hasAgreed(NO)
                        .build())
                .generalAppPBADetails(GAPbaDetails.builder()
                        .build())
                .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                        .id(STRING_CONSTANT)
                        .email(APPLICANT_EMAIL_ID_CONSTANT).build())
                .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                        .organisation(Organisation.builder()
                                .organisationID(STRING_CONSTANT).build())
                        .orgPolicyReference(STRING_CONSTANT).build())
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                        .organisation(Organisation.builder()
                                .organisationID(STRING_CONSTANT).build())
                        .orgPolicyReference(STRING_CONSTANT).build())
                .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
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
                                .unavailableTrialDateFrom(APP_DATE_EPOCH)
                                .unavailableTrialDateTo(APP_DATE_EPOCH).build()))
                        .supportRequirementOther(STRING_CONSTANT)
                        .hearingPreferredLocation(getPreferredLoc())
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

    public CaseData getTestCaseDataForConsentUnconsentCheck(GARespondentOrderAgreement respondentOrderAgreement) {
        return CaseData.builder()
            .ccdState(AWAITING_RESPONDENT_ACKNOWLEDGEMENT)
            .ccdCaseReference(1234L)
            .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                    .organisation(Organisation.builder()
                            .organisationID(STRING_CONSTANT).build())
                    .orgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())
                    .orgPolicyReference(STRING_CONSTANT).build())
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName("Applicant1").build())
            .applicant1DQ(Applicant1DQ.builder()
                        .applicant1DQRequestedCourt(RequestedCourt.builder()
                                .responseCourtCode("applicant1DQRequestedCourt")
                                                        .caseLocation(CaseLocationCivil.builder()
                                                                          .region("2")
                                                                          .baseLocation("11111")
                                                                          .build())
                                .build())
                        .build())
                .respondent1(Party.builder().type(Party.Type.COMPANY).companyName("Respondent1").build())
                .respondent1DQ(Respondent1DQ.builder()
                        .respondent1DQRequestedCourt(RequestedCourt.builder()
                                .responseCourtCode("respondent1DQRequestedCourt")
                                                         .caseLocation(CaseLocationCivil.builder()
                                                                           .region("2")
                                                                           .baseLocation("00000")
                                                                           .build())
                                .build())
                        .build())
                .addApplicant2(YES)
                .applicant2(Party.builder().type(Party.Type.COMPANY).companyName("Applicant2").build())
                .addRespondent2(YES)
                .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("Respondent2").build())
                .generalAppType(GAApplicationType.builder()
                        .types(singletonList(EXTEND_TIME))
                        .build())
                .generalAppRespondentAgreement(respondentOrderAgreement)
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder()
                        .generalAppUrgency(YES)
                        .reasonsForUrgency(STRING_CONSTANT)
                        .urgentAppConsiderationDate(APP_DATE_EPOCH)
                        .build())
                .generalAppInformOtherParty(GAInformOtherParty.builder()
                        .isWithNotice(NO)
                        .reasonsForWithoutNotice(STRING_CONSTANT)
                        .build())
                .generalAppDetailsOfOrder(STRING_CONSTANT)
                .generalAppReasonsOfOrder(STRING_CONSTANT)
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
                                .unavailableTrialDateFrom(APP_DATE_EPOCH)
                                .unavailableTrialDateTo(APP_DATE_EPOCH).build()))
                        .supportRequirementOther(STRING_CONSTANT)
                        .hearingPreferredLocation(getPreferredLoc())
                        .hearingDetailsTelephoneNumber(STRING_NUM_CONSTANT)
                        .reasonForPreferredHearingType(STRING_CONSTANT)
                        .telephoneHearingPreferredType(STRING_CONSTANT)
                        .supportRequirementSignLanguage(STRING_CONSTANT)
                        .hearingPreferencesPreferredType(IN_PERSON)
                        .unavailableTrialRequiredYesOrNo(YES)
                        .supportRequirementLanguageInterpreter(STRING_CONSTANT)
                        .build())
                .generalAppPBADetails(GAPbaDetails.builder()
                        .build())
                .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                        .id(STRING_CONSTANT)
                        .email(APPLICANT_EMAIL_ID_CONSTANT).build())
                .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                        .organisation(Organisation.builder()
                                .organisationID(STRING_CONSTANT).build())
                        .orgPolicyCaseAssignedRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName())
                        .orgPolicyReference(STRING_CONSTANT).build())
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                        .organisation(Organisation.builder()
                                .organisationID(STRING_CONSTANT).build())
                        .orgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
                        .orgPolicyReference(STRING_CONSTANT).build())
                .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
                .build();
    }

    public CaseData getTestCaseDataForStatementOfTruthCheck(GARespondentOrderAgreement respondentOrderAgreement) {
        return CaseData.builder()
            .ccdState(AWAITING_RESPONDENT_ACKNOWLEDGEMENT)
            .ccdCaseReference(1234L)
            .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                                               .organisation(Organisation.builder()
                                                                 .organisationID(STRING_CONSTANT).build())
                                               .orgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())
                                               .orgPolicyReference(STRING_CONSTANT).build())
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName("Applicant1").build())
            .applicant1DQ(Applicant1DQ.builder()
                              .applicant1DQRequestedCourt(RequestedCourt.builder()
                                                              .responseCourtCode("applicant1DQRequestedCourt")
                                                              .caseLocation(CaseLocationCivil.builder()
                                                                                .region("2")
                                                                                .baseLocation("11111")
                                                                                .build())
                                                              .build())
                              .build())
            .respondent1(Party.builder().type(Party.Type.COMPANY).companyName("Respondent1").build())
            .respondent1DQ(Respondent1DQ.builder()
                               .respondent1DQRequestedCourt(RequestedCourt.builder()
                                                                .responseCourtCode("respondent1DQRequestedCourt")
                                                                .caseLocation(CaseLocationCivil.builder()
                                                                                  .region("2")
                                                                                  .baseLocation("00000")
                                                                                  .build())
                                                                .build())
                               .build())
            .addApplicant2(YES)
            .applicant2(Party.builder().type(Party.Type.COMPANY).companyName("Applicant2").build())
            .addRespondent2(YES)
            .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("Respondent2").build())
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .generalAppRespondentAgreement(respondentOrderAgreement)
            .generalAppUrgencyRequirement(GAUrgencyRequirement.builder()
                                              .generalAppUrgency(YES)
                                              .reasonsForUrgency(STRING_CONSTANT)
                                              .urgentAppConsiderationDate(APP_DATE_EPOCH)
                                              .build())
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(NO)
                                            .reasonsForWithoutNotice(STRING_CONSTANT)
                                            .build())
            .generalAppDetailsOfOrder(STRING_CONSTANT)
            .generalAppReasonsOfOrder(STRING_CONSTANT)
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
                                                                                       .unavailableTrialDateFrom(APP_DATE_EPOCH)
                                                                                       .unavailableTrialDateTo(APP_DATE_EPOCH).build()))
                                          .supportRequirementOther(STRING_CONSTANT)
                                          .hearingPreferredLocation(getPreferredLoc())
                                          .hearingDetailsTelephoneNumber(STRING_NUM_CONSTANT)
                                          .reasonForPreferredHearingType(STRING_CONSTANT)
                                          .telephoneHearingPreferredType(STRING_CONSTANT)
                                          .supportRequirementSignLanguage(STRING_CONSTANT)
                                          .hearingPreferencesPreferredType(IN_PERSON)
                                          .unavailableTrialRequiredYesOrNo(YES)
                                          .supportRequirementLanguageInterpreter(STRING_CONSTANT)
                                          .build())
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                .id(STRING_CONSTANT)
                                                .email(APPLICANT_EMAIL_ID_CONSTANT).build())
            .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                              .organisation(Organisation.builder()
                                                                .organisationID(STRING_CONSTANT).build())
                                              .orgPolicyCaseAssignedRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName())
                                              .orgPolicyReference(STRING_CONSTANT).build())
            .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                               .organisation(Organisation.builder()
                                                                 .organisationID(STRING_CONSTANT).build())
                                               .orgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
                                               .orgPolicyReference(STRING_CONSTANT).build())
            .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
            .build();
    }

    public CaseData getTestCaseDataSPEC(CaseCategory claimType) {
        return CaseData.builder()
            .ccdCaseReference(1234L)
            .caseAccessCategory(claimType)
            .courtLocation(CourtLocation.builder()
                               .caseLocation(CaseLocationCivil.builder()
                                                 .region("2")
                                                 .baseLocation("00000")
                                                 .build())
                               .build())
            .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                                               .organisation(Organisation.builder()
                                                                 .organisationID(STRING_CONSTANT).build())
                                               .orgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORTWO
                                                                              .getFormattedName())
                                               .orgPolicyReference(STRING_CONSTANT).build())
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName("Applicant1").build())
            .respondent1(Party.builder().type(Party.Type.COMPANY).companyName("Respondent1").build())
            .addApplicant2(YES)
            .applicant2(Party.builder().type(Party.Type.COMPANY).companyName("Applicant2").build())
            .addRespondent2(YES)
            .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("Respondent2").build())
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YES).build())
            .generalAppUrgencyRequirement(GAUrgencyRequirement.builder()
                                              .generalAppUrgency(YES)
                                              .reasonsForUrgency(STRING_CONSTANT)
                                              .urgentAppConsiderationDate(APP_DATE_EPOCH)
                                              .build())
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(NO)
                                            .reasonsForWithoutNotice(STRING_CONSTANT)
                                            .build())
            .generalAppDetailsOfOrder(STRING_CONSTANT)
            .generalAppReasonsOfOrder(STRING_CONSTANT)
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
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                .id(STRING_CONSTANT)
                                                .email(APPLICANT_EMAIL_ID_CONSTANT).build())
            .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                              .organisation(Organisation.builder()
                                                                .organisationID(STRING_CONSTANT).build())
                                              .orgPolicyCaseAssignedRole(CaseRole.APPLICANTSOLICITORONE
                                                                             .getFormattedName())
                                              .orgPolicyReference(STRING_CONSTANT).build())
            .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                               .organisation(Organisation.builder()
                                                                 .organisationID(STRING_CONSTANT).build())
                                               .orgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORONE
                                                                              .getFormattedName())
                                               .orgPolicyReference(STRING_CONSTANT).build())
            .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
            .build();
    }

    public CaseData getCaseDataForWorkAllocation(CaseState state,
                                                 CaseCategory claimType,
                                                 Party.Type claimant1Type,
                                                 Applicant1DQ applicant1DQ,
                                                 Respondent1DQ respondent1DQ,
                                                 Respondent2DQ respondent2DQ) {
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder()
                .ccdCaseReference(1234L)
                .courtLocation(CourtLocation.builder().caseLocation(CaseLocationCivil.builder()
                                                                        .region("2")
                                                                        .baseLocation("00000")
                                                                        .build()).build())
                .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                        .organisation(Organisation.builder()
                                .organisationID(STRING_CONSTANT).build())
                        .orgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())
                        .orgPolicyReference(STRING_CONSTANT).build())
                .applicant1(Party.builder().type(claimant1Type).companyName("Applicant1").build())
                .respondent1(Party.builder().type(claimant1Type).companyName("Respondent1").build())
                .addApplicant2(YES)
                .applicant2(Party.builder().type(Party.Type.COMPANY).companyName("Applicant2").build())
                .addRespondent2(YES)
                .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("Respondent2").build())
                .generalAppType(GAApplicationType.builder()
                        .types(singletonList(EXTEND_TIME))
                        .build())
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder()
                        .generalAppUrgency(YES)
                        .reasonsForUrgency(STRING_CONSTANT)
                        .urgentAppConsiderationDate(APP_DATE_EPOCH)
                        .build())
                .generalAppInformOtherParty(GAInformOtherParty.builder()
                        .isWithNotice(NO)
                        .reasonsForWithoutNotice(STRING_CONSTANT)
                        .build())
                .generalAppDetailsOfOrder(STRING_CONSTANT)
                .generalAppReasonsOfOrder(STRING_CONSTANT)
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
            .respondent1ResponseDate(LocalDateTime.parse("2022-09-25T15:20:37.2363012"))
            .respondent2ResponseDate(LocalDateTime.parse("2022-09-26T15:20:37.2363012"))
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
                                .unavailableTrialDateFrom(APP_DATE_EPOCH)
                                .unavailableTrialDateTo(APP_DATE_EPOCH).build()))
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
                .generalAppPBADetails(GAPbaDetails.builder()
                        .build())
                .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                        .id(STRING_CONSTANT)
                        .email(APPLICANT_EMAIL_ID_CONSTANT).build())
                .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                        .organisation(Organisation.builder()
                                .organisationID(STRING_CONSTANT).build())
                        .orgPolicyCaseAssignedRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName())
                        .orgPolicyReference(STRING_CONSTANT).build())
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                        .organisation(Organisation.builder()
                                .organisationID(STRING_CONSTANT).build())
                        .orgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
                        .orgPolicyReference(STRING_CONSTANT).build())
                .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
                .applicant1DQ(applicant1DQ)
                .respondent1DQ(respondent1DQ)
                .respondent2DQ(respondent2DQ)
                .ccdState(state);
        if (claimType != null) {
            builder.caseAccessCategory(claimType);
        }
        return builder.build();
    }

    public CaseData getCaseDataForWorkAllocation1V1(CaseState state,
                                                 CaseCategory claimType,
                                                 Party.Type respondent1Type,
                                                 Applicant1DQ applicant1DQ,
                                                 Respondent1DQ respondent1DQ) {
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder()
            .ccdCaseReference(1234L)
            .courtLocation(CourtLocation.builder().caseLocation(CaseLocationCivil.builder()
                                                                    .region("2")
                                                                    .baseLocation("00000")
                                                                    .build()).build())
            .applicant1(Party.builder().type(Party.Type.SOLE_TRADER).companyName("Applicant1").build())
            .respondent1(Party.builder().type(respondent1Type).companyName("Respondent1").build())
            .addApplicant2(NO)
            .addRespondent2(NO)
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
            .generalAppUrgencyRequirement(GAUrgencyRequirement.builder()
                                              .generalAppUrgency(YES)
                                              .reasonsForUrgency(STRING_CONSTANT)
                                              .urgentAppConsiderationDate(APP_DATE_EPOCH)
                                              .build())
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(NO)
                                            .reasonsForWithoutNotice(STRING_CONSTANT)
                                            .build())
            .generalAppDetailsOfOrder(STRING_CONSTANT)
            .generalAppReasonsOfOrder(STRING_CONSTANT)
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
            .respondent1ResponseDate(LocalDateTime.parse("2022-09-25T15:20:37.2363012"))
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
                                          .generalAppUnavailableDates(
                                              wrapElements(GAUnavailabilityDates.builder()
                                                               .unavailableTrialDateFrom(APP_DATE_EPOCH)
                                                               .unavailableTrialDateTo(APP_DATE_EPOCH).build()))
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
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                .id(STRING_CONSTANT)
                                                .email(APPLICANT_EMAIL_ID_CONSTANT).build())
            .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                              .organisation(Organisation.builder()
                                                                .organisationID(STRING_CONSTANT).build())
                                              .orgPolicyCaseAssignedRole(
                                                  CaseRole.APPLICANTSOLICITORONE.getFormattedName())
                                              .orgPolicyReference(STRING_CONSTANT).build())
            .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                               .organisation(Organisation.builder()
                                                                 .organisationID(STRING_CONSTANT).build())
                                               .orgPolicyCaseAssignedRole(
                                                   CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
                                               .orgPolicyReference(STRING_CONSTANT).build())
            .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
            .applicant1DQ(applicant1DQ)
            .respondent1DQ(respondent1DQ)
            .ccdState(state);
        if (claimType != null) {
            builder.caseAccessCategory(claimType);
        }
        return builder.build();
    }

    public CaseData getMultiCaseDataForWorkAllocationForOne_V_Two(CaseState state,
                                                 CaseCategory claimType,
                                                 Party.Type claimant1Type,
                                                 Applicant1DQ applicant1DQ,
                                                 Respondent1DQ respondent1DQ,
                                                 Respondent2DQ respondent2DQ) {
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder()
            .ccdCaseReference(1234L)
            .courtLocation(CourtLocation.builder().caseLocation(CaseLocationCivil.builder()
                                                                    .region("2")
                                                                    .baseLocation("00000")
                                                                    .build()).build())
            .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                                               .organisation(Organisation.builder()
                                                                 .organisationID(STRING_CONSTANT).build())
                                               .orgPolicyCaseAssignedRole(
                                                   CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())
                                               .orgPolicyReference(STRING_CONSTANT).build())
            .applicant1(Party.builder().type(claimant1Type).companyName("Applicant1").build())
            .respondent1(Party.builder().type(claimant1Type).companyName("Respondent1").build())
            .addRespondent2(YES)
            .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("Respondent2").build())
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
            .generalAppUrgencyRequirement(GAUrgencyRequirement.builder()
                                              .generalAppUrgency(YES)
                                              .reasonsForUrgency(STRING_CONSTANT)
                                              .urgentAppConsiderationDate(APP_DATE_EPOCH)
                                              .build())
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(NO)
                                            .reasonsForWithoutNotice(STRING_CONSTANT)
                                            .build())
            .generalAppDetailsOfOrder(STRING_CONSTANT)
            .generalAppReasonsOfOrder(STRING_CONSTANT)
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
            .respondent1ResponseDate(LocalDateTime.parse("2022-09-27T15:20:37.2363012"))
            .respondent2ResponseDate(LocalDateTime.parse("2022-09-25T15:20:37.2363012"))
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
                                          .generalAppUnavailableDates(
                                              wrapElements(GAUnavailabilityDates.builder()
                                                               .unavailableTrialDateFrom(APP_DATE_EPOCH)
                                                               .unavailableTrialDateTo(APP_DATE_EPOCH).build()))
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
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                .id(STRING_CONSTANT)
                                                .email(APPLICANT_EMAIL_ID_CONSTANT).build())
            .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                              .organisation(Organisation.builder()
                                                                .organisationID(STRING_CONSTANT).build())
                                              .orgPolicyCaseAssignedRole(
                                                  CaseRole.APPLICANTSOLICITORONE.getFormattedName())
                                              .orgPolicyReference(STRING_CONSTANT).build())
            .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                               .organisation(Organisation.builder()
                                                                 .organisationID(STRING_CONSTANT).build())
                                               .orgPolicyCaseAssignedRole(
                                                   CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
                                               .orgPolicyReference(STRING_CONSTANT).build())
            .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
            .applicant1DQ(applicant1DQ)
            .respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ)
            .ccdState(state);
        if (claimType != null) {
            builder.caseAccessCategory(claimType);
        }
        return builder.build();
    }

    public CaseData getTestCaseDataCollectionOfApps(CaseData caseData) {
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
                                .unavailableTrialDateTo(APP_DATE_EPOCH)
                                .unavailableTrialDateFrom(APP_DATE_EPOCH).build()))
                        .supportRequirementOther(STRING_CONSTANT)
                        .hearingPreferredLocation(getPreferredLoc())
                        .hearingDetailsTelephoneNumber(STRING_NUM_CONSTANT)
                        .reasonForPreferredHearingType(STRING_CONSTANT)
                        .telephoneHearingPreferredType(STRING_CONSTANT)
                        .supportRequirementSignLanguage(STRING_CONSTANT)
                        .hearingPreferencesPreferredType(IN_PERSON)
                        .unavailableTrialRequiredYesOrNo(YES)
                        .supportRequirementLanguageInterpreter(STRING_CONSTANT)
                        .build())
                .build();
        return getTestCaseDataWithEmptyCollectionOfApps(caseData)
                .toBuilder()
                .generalApplications(wrapElements(application))
                .build();
    }

    public GeneralApplication getGeneralApplication(String key) {
        return getGeneralApplication().toBuilder()
                .caseLink(CaseLink.builder().caseReference(key).build())
                .build();
    }

    public GeneralApplication getGeneralApplication() {
        GeneralApplication.GeneralApplicationBuilder builder = GeneralApplication.builder();
        return builder.generalAppType(GAApplicationType.builder()
                        .types(singletonList(SUMMARY_JUDGEMENT))
                        .build())
                .caseManagementLocation(uk.gov.hmcts.reform.civil.model.genapplication
                                            .CaseLocationCivil.builder()
                                            .baseLocation("34567")
                                            .region("4").build())
                .isCcmccLocation(YES)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                        .hasAgreed(NO)
                        .build())
                .generalAppInformOtherParty(GAInformOtherParty.builder()
                        .isWithNotice(YES)
                        .reasonsForWithoutNotice(STRING_CONSTANT)
                        .build())
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder()
                        .generalAppUrgency(NO)
                        .reasonsForUrgency(STRING_CONSTANT)
                        .urgentAppConsiderationDate(APP_DATE_EPOCH)
                        .build())
                .generalAppType(GAApplicationType.builder()
                        .types(singletonList(EXTEND_TIME))
                        .build())
                .isMultiParty(NO)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                        .hasAgreed(NO)
                        .build())
                .generalAppPBADetails(GAPbaDetails.builder()
                        .fee(Fee.builder().code("FEE_CODE").calculatedAmountInPence(BigDecimal.valueOf(10800L))
                                .version("1").build())
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
                                .unavailableTrialDateFrom(APP_DATE_EPOCH)
                                .unavailableTrialDateTo(APP_DATE_EPOCH).build()))
                        .supportRequirementOther(STRING_CONSTANT)
                        .hearingPreferredLocation(getPreferredLoc())
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

    public DynamicList getPreferredLoc() {
        DynamicList dynamicList = fromList(List.of("ABCD - RG0 0AL",
                                                   "PQRS - GU0 0EE",
                                                   "WXYZ - EW0 0HE",
                                                   "LMNO - NE0 0BH"));
        Optional<DynamicListElement> first = dynamicList.getListItems().stream().findFirst();
        first.ifPresent(dynamicList::setValue);
        return dynamicList;
    }

    public CaseData getTestCaseDataWithGeneralOrderPDFDocument(CaseData caseData) {
        String uid = "f000aa01-0451-4000-b000-000000000111";
        return caseData.toBuilder()
            .ccdCaseReference(1234L)
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .generalAppEvidenceDocument(wrapElements(Document.builder().documentUrl(STRING_CONSTANT).build()))
            .generalOrderDocument(singletonList(Element.<CaseDocument>builder().id(UUID.fromString(uid))
                                                    .value(pdfDocument).build()))
            .build();
    }

    public CaseData getTestCaseDataWithGeneralOrderStaffPDFDocument(CaseData caseData) {
        String uid = "f000aa01-0451-4000-b000-000000000111";
        return caseData.toBuilder()
                .ccdCaseReference(1234L)
                .generalAppType(GAApplicationType.builder()
                        .types(singletonList(EXTEND_TIME))
                        .build())
                .generalAppEvidenceDocument(wrapElements(Document.builder().documentUrl(STRING_CONSTANT).build()))
                .generalOrderDocStaff(singletonList(Element.<CaseDocument>builder().id(UUID.fromString(uid))
                        .value(pdfDocument).build()))
                .build();
    }

    public CaseData getTestCaseDataWithDismissalOrderPDFDocument(CaseData caseData) {
        return caseData.toBuilder()
            .ccdCaseReference(1234L)
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .generalAppEvidenceDocument(wrapElements(Document.builder().documentUrl(STRING_CONSTANT).build()))
            .dismissalOrderDocument(singletonList(Element.<CaseDocument>builder().value(pdfDocument).build()))
            .build();
    }

    public CaseData getTestCaseDataWithDismissalOrderStaffPDFDocument(CaseData caseData) {
        return caseData.toBuilder()
                .ccdCaseReference(1234L)
                .generalAppType(GAApplicationType.builder()
                        .types(singletonList(EXTEND_TIME))
                        .build())
                .generalAppEvidenceDocument(wrapElements(Document.builder().documentUrl(STRING_CONSTANT).build()))
                .dismissalOrderDocStaff(singletonList(Element.<CaseDocument>builder().value(pdfDocument).build()))
                .build();
    }

    public CaseData getTestCaseDataWithDirectionOrderPDFDocument(CaseData caseData) {
        String uid = "f000aa01-0451-4000-b000-000000000111";
        String uid1 = "f000aa01-0451-4000-b000-000000000000";
        return caseData.toBuilder()
            .ccdCaseReference(1234L)
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .generalAppEvidenceDocument(wrapElements(Document.builder().documentUrl(STRING_CONSTANT).build()))
            .generalOrderDocument(singletonList(Element.<CaseDocument>builder().id(UUID.fromString(uid))
                                                    .value(pdfDocument).build()))
            .directionOrderDocument(singletonList(Element.<CaseDocument>builder().id(UUID.fromString(uid1))
                                                      .value(pdfDocument).build()))
            .build();
    }

    public CaseData getTestCaseDataWithDirectionResponseDocument(CaseData caseData) {
        String uid = "f000aa01-0451-4000-b000-000000000111";
        String uid1 = "f000aa01-0451-4000-b000-000000000000";
        return caseData.toBuilder()
            .ccdCaseReference(1234L)
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .generalAppEvidenceDocument(wrapElements(Document.builder().documentUrl(STRING_CONSTANT).build()))
            .generalOrderDocument(singletonList(Element.<CaseDocument>builder().id(UUID.fromString(uid))
                                                    .value(pdfDocument).build()))
            .gaRespDocument(singletonList(Element.<Document>builder().id(UUID.fromString(uid1))
                                                      .value(pdfDocument1).build()))
            .build();
    }

    public CaseData getTestCaseDataWithConsentOrderPDFDocument(CaseData caseData) {
        String uid = "f000aa01-0451-4000-b000-000000000111";
        String uid1 = "f000aa01-0451-4000-b000-000000000000";
        return caseData.toBuilder()
            .ccdCaseReference(1234L)
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .consentOrderDocument(singletonList(Element.<CaseDocument>builder().id(UUID.fromString(uid1))
                                                      .value(pdfDocument).build()))
            .build();
    }

    public CaseData getTestCaseDataWithDraftApplicationPDFDocument(CaseData caseData) {
        String uid = "f000aa01-0451-4000-b000-000000000111";
        String uid1 = "f000aa01-0451-4000-b000-000000000000";
        return caseData.toBuilder()
            .ccdCaseReference(1234L)
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .gaDraftDocument(singletonList(Element.<CaseDocument>builder().id(UUID.fromString(uid1))
                                                    .value(pdfDocument).build()))
            .build();
    }

    public CaseData getTestCaseDataWithDirectionOrderStaffPDFDocument(CaseData caseData) {
        String uid = "f000aa01-0451-4000-b000-000000000111";
        String uid1 = "f000aa01-0451-4000-b000-000000000000";
        return caseData.toBuilder()
                .ccdCaseReference(1234L)
                .generalAppType(GAApplicationType.builder()
                        .types(singletonList(EXTEND_TIME))
                        .build())
                .generalAppEvidenceDocument(wrapElements(Document.builder().documentUrl(STRING_CONSTANT).build()))
                .generalOrderDocStaff(singletonList(Element.<CaseDocument>builder().id(UUID.fromString(uid))
                        .value(pdfDocument).build()))
                .directionOrderDocStaff(singletonList(Element.<CaseDocument>builder().id(UUID.fromString(uid1))
                        .value(pdfDocument).build()))
                .build();
    }

    public CaseData getTestCaseDataWithConsentOrderStaffPDFDocument(CaseData caseData) {
        String uid = "f000aa01-0451-4000-b000-000000000111";
        String uid1 = "f000aa01-0451-4000-b000-000000000000";
        return caseData.toBuilder()
            .ccdCaseReference(1234L)
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .consentOrderDocStaff(singletonList(Element.<CaseDocument>builder().id(UUID.fromString(uid1))
                                                      .value(pdfDocument).build()))
            .build();
    }

    public CaseData getTestCaseDataWithDraftStaffPDFDocument(CaseData caseData) {
        String uid = "f000aa01-0451-4000-b000-000000000111";
        String uid1 = "f000aa01-0451-4000-b000-000000000000";
        return caseData.toBuilder()
            .ccdCaseReference(1234L)
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .gaDraftDocStaff(singletonList(Element.<CaseDocument>builder().id(UUID.fromString(uid1))
                                                    .value(pdfDocument).build()))
            .build();
    }

    public CaseData getTestCaseDataWithHearingNoticeDocumentPDFDocument(CaseData caseData) {
        String uid1 = "f000aa01-0451-4000-b000-000000000000";
        return caseData.toBuilder()
                .ccdCaseReference(1234L)
                .generalAppType(GAApplicationType.builder()
                        .types(singletonList(EXTEND_TIME))
                        .build())
                .hearingNoticeDocument(singletonList(Element.<CaseDocument>builder().id(UUID.fromString(uid1))
                        .value(pdfDocument).build()))
                .build();
    }

    public CaseData getTestCaseDataWithHearingNoticeStaffDocumentPDFDocument(CaseData caseData) {
        String uid1 = "f000aa01-0451-4000-b000-000000000000";
        return caseData.toBuilder()
                .ccdCaseReference(1234L)
                .generalAppType(GAApplicationType.builder()
                        .types(singletonList(EXTEND_TIME))
                        .build())
                .hearingNoticeDocStaff(singletonList(Element.<CaseDocument>builder().id(UUID.fromString(uid1))
                        .value(pdfDocument).build()))
                .build();
    }

    public CaseData getTriggerGeneralApplicationTestData() {
        return CaseData
                .builder()
                .ccdCaseReference(1L)
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
                                .unavailableTrialDateFrom(APP_DATE_EPOCH)
                                .unavailableTrialDateTo(APP_DATE_EPOCH).build()))
                        .supportRequirementOther(STRING_CONSTANT)
                        .hearingPreferredLocation(getPreferredLoc())
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

    public CaseData getVaryJudgmentWithN245TestData() {
        return CaseData
                .builder()
                .ccdCaseReference(1L)
                .applicant1(Party.builder().type(Party.Type.COMPANY).companyName("Applicant1").build())
                .respondent1(Party.builder().type(Party.Type.COMPANY).companyName("Respondent1").build())
                .addRespondent2(NO)
                .courtLocation(CourtLocation.builder()
                        .caseLocation(CaseLocationCivil.builder()
                                .region("2")
                                .baseLocation("00000")
                                .build())
                        .build())
                .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                        .organisation(Organisation.builder()
                                .organisationID(STRING_CONSTANT).build())
                        .orgPolicyReference(STRING_CONSTANT).build())
                .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                        .organisation(Organisation.builder()
                                .organisationID(STRING_CONSTANT).build())
                        .orgPolicyReference(STRING_CONSTANT).build())
                .generalAppType(GAApplicationType.builder()
                        .types(singletonList(VARY_JUDGEMENT))
                        .build())
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                        .hasAgreed(YES)
                        .build())
                .generalAppPBADetails(GAPbaDetails.builder()
                        .build())
                .generalAppDetailsOfOrder(STRING_CONSTANT)
                .generalAppReasonsOfOrder(STRING_CONSTANT)
                .generalAppInformOtherParty(GAInformOtherParty.builder()
                        .isWithNotice(YES)
                        .reasonsForWithoutNotice(STRING_CONSTANT)
                        .build())
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder()
                        .generalAppUrgency(NO)
                        .reasonsForUrgency(STRING_CONSTANT)
                        .urgentAppConsiderationDate(APP_DATE_EPOCH)
                        .build())
                .generalAppN245FormUpload(Document.builder()
                        .documentUrl(STRING_CONSTANT)
                        .documentBinaryUrl(STRING_CONSTANT)
                        .documentFileName(STRING_CONSTANT)
                        .documentHash(STRING_CONSTANT)
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
                                .unavailableTrialDateFrom(APP_DATE_EPOCH)
                                .unavailableTrialDateTo(APP_DATE_EPOCH).build()))
                        .supportRequirementOther(STRING_CONSTANT)
                        .hearingPreferredLocation(getPreferredLoc())
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

    public final CaseDocument pdfDocument = CaseDocument.builder()
        .createdBy("John")
        .documentName("documentName")
        .documentSize(0L)
        .documentType(GENERAL_ORDER)
        .createdDatetime(now())
        .documentLink(Document.builder()
                          .documentUrl("fake-url")
                          .documentFileName("file-name")
                          .documentBinaryUrl("binary-url")
                          .build())
        .build();

    public final Document pdfDocument1 = Document.builder()
        .documentUrl("fake-url")
        .documentFileName("file-name")
        .documentBinaryUrl("binary-url")
        .build();
}
