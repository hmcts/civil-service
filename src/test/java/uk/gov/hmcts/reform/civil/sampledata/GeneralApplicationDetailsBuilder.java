package uk.gov.hmcts.reform.civil.sampledata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.lang.Collections;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CourtLocation;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
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

import static com.google.common.collect.Lists.newArrayList;
import static java.time.LocalDate.EPOCH;
import static java.time.LocalDateTime.now;
import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.GENERAL_ORDER;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration.OTHER;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingSupportRequirements.OTHER_SUPPORT;
import static uk.gov.hmcts.reform.civil.enums.dq.GAHearingType.IN_PERSON;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.EXTEND_TIME;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.SUMMARY_JUDGEMENT;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT;
import static uk.gov.hmcts.reform.civil.model.Party.Type.INDIVIDUAL;
import static uk.gov.hmcts.reform.civil.model.common.DynamicList.fromList;
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
            .respondent2OrganisationPolicy(new OrganisationPolicy()
                                               .setOrganisation(new Organisation()
                                                                 .setOrganisationID(STRING_CONSTANT))
                                               .setOrgPolicyReference(STRING_CONSTANT))
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
                .generalAppStatementOfTruth(GAStatementOfTruth.builder().name(STRING_CONSTANT).role(STRING_CONSTANT).build())
                .generalAppEvidenceDocument(wrapElements(new Document().setDocumentUrl(STRING_CONSTANT)))
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
            .respondent2OrganisationPolicy(new OrganisationPolicy()
                                               .setOrganisation(new Organisation()
                                                                 .setOrganisationID(STRING_CONSTANT))
                                               .setOrgPolicyReference(STRING_CONSTANT))
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
                .generalAppStatementOfTruth(GAStatementOfTruth.builder().name(STRING_CONSTANT).role(STRING_CONSTANT).build())
                .generalAppEvidenceDocument(wrapElements(new Document().setDocumentUrl(STRING_CONSTANT)))
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
            .respondent2OrganisationPolicy(new OrganisationPolicy()
                                               .setOrganisation(new Organisation()
                                                                 .setOrganisationID(STRING_CONSTANT))
                                               .setOrgPolicyReference(STRING_CONSTANT))
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                        .hasAgreed(NO)
                        .build())
                .generalAppPBADetails(GAPbaDetails.builder()
                        .build())
                .generalApplications(wrapElements(getGeneralApplication()))
                .applicantSolicitor1UserDetails(new IdamUserDetails()
                        .setId(STRING_CONSTANT)
                        .setEmail(APPLICANT_EMAIL_ID_CONSTANT))
                .applicant1OrganisationPolicy(new OrganisationPolicy()
                        .setOrganisation(new Organisation().setOrganisationID(STRING_CONSTANT))
                        .setOrgPolicyReference(STRING_CONSTANT))
                .respondent1OrganisationPolicy(new OrganisationPolicy()
                        .setOrganisation(new Organisation().setOrganisationID(STRING_CONSTANT))
                        .setOrgPolicyReference(STRING_CONSTANT))
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
                .generalAppStatementOfTruth(GAStatementOfTruth.builder().name(STRING_CONSTANT).role(STRING_CONSTANT).build())
                .generalAppEvidenceDocument(wrapElements(new Document().setDocumentUrl(STRING_CONSTANT)))
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
        caseDataBuilder.caseManagementLocation(new CaseLocationCivil().setBaseLocation("00000")
                                                   .setRegion("2"));
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

    public CaseData getTestCaseDataWithLocationDetailsLip(CaseData caseData,
                                               boolean withGADetails,
                                               boolean withGADetailsResp,
                                               boolean withGADetailsResp2, boolean withGADetailsMaster,
                                               Map<String, String> applicationIdStatus) {

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.caseManagementLocation(new CaseLocationCivil().setBaseLocation("000000")
                                                   .setRegion("2"));
        caseDataBuilder.respondent1Represented(NO);
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
            .respondent2OrganisationPolicy(new OrganisationPolicy()
                                               .setOrganisation(new Organisation()
                                                                 .setOrganisationID(STRING_CONSTANT))
                                               .setOrgPolicyReference(STRING_CONSTANT))
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(NO)
                                               .build())
            .generalAppPBADetails(GAPbaDetails.builder()
                                      .build())
            .generalApplications(wrapElements(getGeneralApplication()))
            .applicantSolicitor1UserDetails(new IdamUserDetails()
                                                .setId(STRING_CONSTANT)
                                                .setEmail(APPLICANT_EMAIL_ID_CONSTANT))
            .applicant1OrganisationPolicy(new OrganisationPolicy()
                                              .setOrganisation(new Organisation()
                                                                .setOrganisationID(STRING_CONSTANT))
                                              .setOrgPolicyReference(STRING_CONSTANT))
            .respondent1OrganisationPolicy(new OrganisationPolicy()
                                               .setOrganisation(new Organisation()
                                                                 .setOrganisationID(STRING_CONSTANT))
                                               .setOrgPolicyReference(STRING_CONSTANT))
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
            .generalAppStatementOfTruth(GAStatementOfTruth.builder().name(STRING_CONSTANT).role(STRING_CONSTANT).build())
            .generalAppEvidenceDocument(wrapElements(new Document().setDocumentUrl(STRING_CONSTANT)))
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
        CaseLocationCivil caseManagementLoc = new CaseLocationCivil().setRegion("1").setBaseLocation("22222");
        return caseData.toBuilder()
            .ccdCaseReference(1234L)
            .caseAccessCategory(SPEC_CLAIM)
            .claimDismissedDeadline(LocalDateTime.now().plusMonths(6))
            .solicitorReferences(new SolicitorReferences().setApplicantSolicitor1Reference("AppSol1Ref").setRespondentSolicitor1Reference("RespSol1ref"))
            .responseClaimTrack("MULTI_CLAIM")
            .respondent2OrganisationPolicy(new OrganisationPolicy()
                                               .setOrganisation(new Organisation()
                                                                 .setOrganisationID(STRING_CONSTANT))
                                               .setOrgPolicyReference(STRING_CONSTANT))
                .applicant1(Party.builder().type(Party.Type.COMPANY).companyName("Applicant1").build())
                .applicant1DQ(new Applicant1DQ()
                        .setApplicant1DQRequestedCourt(new RequestedCourt()
                                .setResponseCourtCode("applicant1DQRequestedCourt")
                                .setCaseLocation(new CaseLocationCivil()
                                                     .setRegion("2")
                                                     .setBaseLocation("00000"))))
                .respondent1(Party.builder().type(Party.Type.COMPANY).companyName("Respondent1").build())
                .respondent1DQ(new Respondent1DQ()
                        .setRespondent1DQRequestedCourt(new RequestedCourt()
                                .setResponseCourtCode("respondent1DQRequestedCourt")
                                .setCaseLocation(new CaseLocationCivil()
                                                     .setRegion("2")
                                                     .setBaseLocation("11111"))))
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
                .applicantSolicitor1UserDetails(new IdamUserDetails()
                        .setId(STRING_CONSTANT)
                        .setEmail(APPLICANT_EMAIL_ID_CONSTANT))
                .applicant1OrganisationPolicy(new OrganisationPolicy()
                        .setOrganisation(new Organisation()
                                .setOrganisationID(STRING_CONSTANT))
                        .setOrgPolicyReference(STRING_CONSTANT))
                .respondent1OrganisationPolicy(new OrganisationPolicy()
                        .setOrganisation(new Organisation()
                                .setOrganisationID(STRING_CONSTANT))
                        .setOrgPolicyReference(STRING_CONSTANT))
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
                .generalAppStatementOfTruth(GAStatementOfTruth.builder().name(STRING_CONSTANT).role(STRING_CONSTANT).build())
                .generalAppEvidenceDocument(wrapElements(new Document()
                        .setDocumentUrl(STRING_CONSTANT)
                        .setDocumentBinaryUrl(STRING_CONSTANT)
                        .setDocumentFileName(STRING_CONSTANT)
                        .setDocumentHash(STRING_CONSTANT)))
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
            .caseManagementLocation(caseManagementLoc)
            .build();
    }

    public CaseData getTestCaseDataForConsentUnconsentCheck(GARespondentOrderAgreement respondentOrderAgreement) {
        return CaseData.builder()
            .caseAccessCategory(UNSPEC_CLAIM)
            .allocatedTrack(AllocatedTrack.FAST_CLAIM)
            .ccdState(AWAITING_RESPONDENT_ACKNOWLEDGEMENT)
            .ccdCaseReference(1234L)
            .respondent2OrganisationPolicy(new OrganisationPolicy()
                    .setOrganisation(new Organisation()
                            .setOrganisationID(STRING_CONSTANT))
                    .setOrgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())
                    .setOrgPolicyReference(STRING_CONSTANT))
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName("Applicant1").build())
            .applicant1DQ(new Applicant1DQ()
                        .setApplicant1DQRequestedCourt(new RequestedCourt()
                                .setResponseCourtCode("applicant1DQRequestedCourt")
                                .setCaseLocation(new CaseLocationCivil()
                                                     .setRegion("2")
                                                     .setBaseLocation("11111"))))
                .respondent1(Party.builder().type(Party.Type.COMPANY).companyName("Respondent1").build())
                .respondent1DQ(new Respondent1DQ()
                        .setRespondent1DQRequestedCourt(new RequestedCourt()
                                .setResponseCourtCode("respondent1DQRequestedCourt")
                                .setCaseLocation(new CaseLocationCivil()
                                                     .setRegion("2")
                                                     .setBaseLocation("00000"))))
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
                .generalAppStatementOfTruth(GAStatementOfTruth.builder().name(STRING_CONSTANT).role(STRING_CONSTANT).build())
                .generalAppEvidenceDocument(wrapElements(new Document()
                        .setDocumentUrl(STRING_CONSTANT)
                        .setDocumentBinaryUrl(STRING_CONSTANT)
                        .setDocumentFileName(STRING_CONSTANT)
                        .setDocumentHash(STRING_CONSTANT)))
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
                .applicantSolicitor1UserDetails(new IdamUserDetails()
                        .setId(STRING_CONSTANT)
                        .setEmail(APPLICANT_EMAIL_ID_CONSTANT))
                .applicant1OrganisationPolicy(new OrganisationPolicy()
                        .setOrganisation(new Organisation()
                                .setOrganisationID(STRING_CONSTANT))
                        .setOrgPolicyCaseAssignedRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName())
                        .setOrgPolicyReference(STRING_CONSTANT))
                .respondent1OrganisationPolicy(new OrganisationPolicy()
                        .setOrganisation(new Organisation()
                                .setOrganisationID(STRING_CONSTANT))
                        .setOrgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
                        .setOrgPolicyReference(STRING_CONSTANT))
                .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
                .caseNameHmctsInternal("Internal caseName")
                .build();
    }

    public CaseData getTestCaseDataForCaseManagementLocation(CaseCategory caseType, CaseState caseState) {
        return CaseData.builder()
            .caseAccessCategory(caseType)
            .ccdState(caseState)
            .ccdCaseReference(1234L)
            .respondent1Represented(NO)
            .caseManagementLocation(new CaseLocationCivil().setRegion("2")
                                        .setBaseLocation("11111"))
            .respondent2OrganisationPolicy(new OrganisationPolicy()
                                               .setOrganisation(new Organisation()
                                                                 .setOrganisationID(STRING_CONSTANT))
                                               .setOrgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())
                                               .setOrgPolicyReference(STRING_CONSTANT))
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName("Applicant1").build())
            .applicant1DQ(new Applicant1DQ()
                              .setApplicant1DQRequestedCourt(new RequestedCourt()
                                                              .setResponseCourtCode("applicant1DQRequestedCourt")
                                                              .setCaseLocation(new CaseLocationCivil()
                                                                                   .setRegion("2")
                                                                                   .setBaseLocation("11111"))))
            .respondent1(Party.builder().type(Party.Type.COMPANY).companyName("Respondent1").build())
            .respondent1DQ(new Respondent1DQ()
                               .setRespondent1DQRequestedCourt(new RequestedCourt()
                                                                .setResponseCourtCode("respondent1DQRequestedCourt")
                                                                .setCaseLocation(new CaseLocationCivil()
                                                                                     .setRegion("2")
                                                                                     .setBaseLocation("00000"))))
            .addApplicant2(YES)
            .applicant2(Party.builder().type(Party.Type.COMPANY).companyName("Applicant2").build())
            .addRespondent2(YES)
            .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("Respondent2").build())
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
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
            .generalAppStatementOfTruth(GAStatementOfTruth.builder().name(STRING_CONSTANT).role(STRING_CONSTANT).build())
            .generalAppEvidenceDocument(wrapElements(new Document()
                                                         .setDocumentUrl(STRING_CONSTANT)
                                                         .setDocumentBinaryUrl(STRING_CONSTANT)
                                                         .setDocumentFileName(STRING_CONSTANT)
                                                         .setDocumentHash(STRING_CONSTANT)))
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
            .applicantSolicitor1UserDetails(new IdamUserDetails()
                                                .setId(STRING_CONSTANT)
                                                .setEmail(APPLICANT_EMAIL_ID_CONSTANT))
            .applicant1OrganisationPolicy(new OrganisationPolicy()
                                              .setOrganisation(new Organisation()
                                                                .setOrganisationID(STRING_CONSTANT))
                                              .setOrgPolicyCaseAssignedRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName())
                                              .setOrgPolicyReference(STRING_CONSTANT))
            .respondent1OrganisationPolicy(new OrganisationPolicy()
                                               .setOrganisation(new Organisation()
                                                                 .setOrganisationID(STRING_CONSTANT))
                                               .setOrgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
                                               .setOrgPolicyReference(STRING_CONSTANT))
            .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
            .build();
    }

    public CaseData getTestCaseDataForStatementOfTruthCheck(GARespondentOrderAgreement respondentOrderAgreement) {
        return CaseData.builder()
            .caseAccessCategory(UNSPEC_CLAIM)
            .allocatedTrack(AllocatedTrack.INTERMEDIATE_CLAIM)
            .ccdState(AWAITING_RESPONDENT_ACKNOWLEDGEMENT)
            .ccdCaseReference(1234L)
            .respondent2OrganisationPolicy(new OrganisationPolicy()
                                               .setOrganisation(new Organisation()
                                                                 .setOrganisationID(STRING_CONSTANT))
                                               .setOrgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())
                                               .setOrgPolicyReference(STRING_CONSTANT))
            .applicant1(Party.builder().type(Party.Type.COMPANY).companyName("Applicant1").build())
            .applicant1DQ(new Applicant1DQ()
                              .setApplicant1DQRequestedCourt(new RequestedCourt()
                                                              .setResponseCourtCode("applicant1DQRequestedCourt")
                                                              .setCaseLocation(new CaseLocationCivil()
                                                                                   .setRegion("2")
                                                                                   .setBaseLocation("11111"))))
            .respondent1(Party.builder().type(Party.Type.COMPANY).companyName("Respondent1").build())
            .respondent1DQ(new Respondent1DQ()
                               .setRespondent1DQRequestedCourt(new RequestedCourt()
                                                                .setResponseCourtCode("respondent1DQRequestedCourt")
                                                                .setCaseLocation(new CaseLocationCivil()
                                                                                     .setRegion("2")
                                                                                     .setBaseLocation("00000"))))
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
            .generalAppEvidenceDocument(wrapElements(new Document()
                                                         .setDocumentUrl(STRING_CONSTANT)
                                                         .setDocumentBinaryUrl(STRING_CONSTANT)
                                                         .setDocumentFileName(STRING_CONSTANT)
                                                         .setDocumentHash(STRING_CONSTANT)))
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
            .applicantSolicitor1UserDetails(new IdamUserDetails()
                                                .setId(STRING_CONSTANT)
                                                .setEmail(APPLICANT_EMAIL_ID_CONSTANT))
            .applicant1OrganisationPolicy(new OrganisationPolicy()
                                              .setOrganisation(new Organisation()
                                                                .setOrganisationID(STRING_CONSTANT))
                                              .setOrgPolicyCaseAssignedRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName())
                                              .setOrgPolicyReference(STRING_CONSTANT))
            .respondent1OrganisationPolicy(new OrganisationPolicy()
                                               .setOrganisation(new Organisation()
                                                                 .setOrganisationID(STRING_CONSTANT))
                                               .setOrgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
                                               .setOrgPolicyReference(STRING_CONSTANT))
            .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
            .build();
    }

    public CaseData getTestCaseDataSPEC(CaseCategory claimType) {
        CaseLocationCivil caseManagementLoc = new CaseLocationCivil().setRegion("1").setBaseLocation("22222");
        return CaseData.builder()
            .ccdCaseReference(1234L)
            .caseAccessCategory(claimType)
            .submittedDate(LocalDateTime.of(2025, 5, 5, 0, 0, 0))
            .courtLocation(new CourtLocation()
                               .setCaseLocation(new CaseLocationCivil()
                                                 .setRegion("2")
                                                 .setBaseLocation("00000")
                                                 ))
            .respondent2OrganisationPolicy(new OrganisationPolicy()
                                               .setOrganisation(new Organisation()
                                                                 .setOrganisationID(STRING_CONSTANT))
                                               .setOrgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORTWO
                                                                              .getFormattedName())
                                               .setOrgPolicyReference(STRING_CONSTANT))
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
            .generalAppStatementOfTruth(GAStatementOfTruth.builder().name(STRING_CONSTANT).role(STRING_CONSTANT).build())
            .generalAppEvidenceDocument(wrapElements(new Document()
                                                         .setDocumentUrl(STRING_CONSTANT)
                                                         .setDocumentBinaryUrl(STRING_CONSTANT)
                                                         .setDocumentFileName(STRING_CONSTANT)
                                                         .setDocumentHash(STRING_CONSTANT)))
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
            .applicantSolicitor1UserDetails(new IdamUserDetails()
                                                .setId(STRING_CONSTANT)
                                                .setEmail(APPLICANT_EMAIL_ID_CONSTANT))
            .applicant1OrganisationPolicy(new OrganisationPolicy()
                                              .setOrganisation(new Organisation()
                                                                .setOrganisationID(STRING_CONSTANT))
                                              .setOrgPolicyCaseAssignedRole(CaseRole.APPLICANTSOLICITORONE
                                                                             .getFormattedName())
                                              .setOrgPolicyReference(STRING_CONSTANT))
            .respondent1OrganisationPolicy(new OrganisationPolicy()
                                               .setOrganisation(new Organisation()
                                                                 .setOrganisationID(STRING_CONSTANT))
                                               .setOrgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORONE
                                                                              .getFormattedName())
                                               .setOrgPolicyReference(STRING_CONSTANT))
            .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
            .caseManagementLocation(caseManagementLoc)
            .build();
    }

    public CaseData getCaseDataForWorkAllocation(CaseState state,
                                                 CaseCategory claimType,
                                                 Party.Type claimant1Type,
                                                 Applicant1DQ applicant1DQ,
                                                 Respondent1DQ respondent1DQ,
                                                 Respondent2DQ respondent2DQ) {
        CaseLocationCivil caseManagementLoc = new CaseLocationCivil().setRegion("1").setBaseLocation("22222");
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder()
                .caseAccessCategory(claimType)
                .ccdCaseReference(1234L)
                .courtLocation(new CourtLocation().setCaseLocation(new CaseLocationCivil()
                                                                        .setRegion("2")
                                                                        .setBaseLocation("00000")
                                                                        ))
                .respondent2OrganisationPolicy(new OrganisationPolicy()
                        .setOrganisation(new Organisation()
                                .setOrganisationID(STRING_CONSTANT))
                        .setOrgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())
                        .setOrgPolicyReference(STRING_CONSTANT))
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
                .generalAppStatementOfTruth(GAStatementOfTruth.builder().name(STRING_CONSTANT).role(STRING_CONSTANT).build())
                .generalAppEvidenceDocument(wrapElements(new Document()
                        .setDocumentUrl(STRING_CONSTANT)
                        .setDocumentBinaryUrl(STRING_CONSTANT)
                        .setDocumentFileName(STRING_CONSTANT)
                        .setDocumentHash(STRING_CONSTANT)))
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
                .applicantSolicitor1UserDetails(new IdamUserDetails()
                        .setId(STRING_CONSTANT)
                        .setEmail(APPLICANT_EMAIL_ID_CONSTANT))
                .applicant1OrganisationPolicy(new OrganisationPolicy()
                        .setOrganisation(new Organisation()
                                .setOrganisationID(STRING_CONSTANT))
                        .setOrgPolicyCaseAssignedRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName())
                        .setOrgPolicyReference(STRING_CONSTANT))
                .respondent1OrganisationPolicy(new OrganisationPolicy()
                        .setOrganisation(new Organisation()
                                .setOrganisationID(STRING_CONSTANT))
                        .setOrgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
                        .setOrgPolicyReference(STRING_CONSTANT))
                .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
                .applicant1DQ(applicant1DQ)
                .respondent1DQ(respondent1DQ)
                .respondent2DQ(respondent2DQ)
                .ccdState(state)
                .caseManagementLocation(caseManagementLoc);
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
            .courtLocation(new CourtLocation().setCaseLocation(new CaseLocationCivil()
                                                                    .setRegion("2")
                                                                    .setBaseLocation("00000")
                                                                    ))
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
            .generalAppStatementOfTruth(GAStatementOfTruth.builder().name(STRING_CONSTANT).role(STRING_CONSTANT).build())
            .generalAppEvidenceDocument(wrapElements(new Document()
                                                         .setDocumentUrl(STRING_CONSTANT)
                                                         .setDocumentBinaryUrl(STRING_CONSTANT)
                                                         .setDocumentFileName(STRING_CONSTANT)
                                                         .setDocumentHash(STRING_CONSTANT)))
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
            .applicantSolicitor1UserDetails(new IdamUserDetails()
                                                .setId(STRING_CONSTANT)
                                                .setEmail(APPLICANT_EMAIL_ID_CONSTANT))
            .applicant1OrganisationPolicy(new OrganisationPolicy()
                                              .setOrganisation(new Organisation()
                                                                .setOrganisationID(STRING_CONSTANT))
                                              .setOrgPolicyCaseAssignedRole(
                                                  CaseRole.APPLICANTSOLICITORONE.getFormattedName())
                                              .setOrgPolicyReference(STRING_CONSTANT))
            .respondent1OrganisationPolicy(new OrganisationPolicy()
                                               .setOrganisation(new Organisation()
                                                                 .setOrganisationID(STRING_CONSTANT))
                                               .setOrgPolicyCaseAssignedRole(
                                                   CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
                                               .setOrgPolicyReference(STRING_CONSTANT))
            .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
            .applicant1DQ(applicant1DQ)
            .respondent1DQ(respondent1DQ)
            .ccdState(state);
        if (claimType != null) {
            builder.caseAccessCategory(claimType);
        }
        return builder.build();
    }

    public CaseData getCaseDataForWorkAllocation1V1FailSafeData() {
        Applicant1DQ applicant1DQ =
                new Applicant1DQ().setApplicant1DQRequestedCourt(new RequestedCourt()
                        .setResponseCourtCode("applicant1DQRequestedCourt")
                        .setCaseLocation(new CaseLocationCivil()
                                             .setRegion("2")
                                             .setBaseLocation("00000")));
        Respondent1DQ respondent1DQ =
                new Respondent1DQ().setRespondent1DQRequestedCourt(new RequestedCourt()
                        .setResponseCourtCode(null)
                        .setCaseLocation(new CaseLocationCivil()
                                             .setRegion("2")
                                             .setBaseLocation("11111")));
        CaseLocationCivil caseManagementLoc = new CaseLocationCivil().setRegion("1").setBaseLocation("22222");
        return getCaseDataForWorkAllocation1V1(null, SPEC_CLAIM, INDIVIDUAL, applicant1DQ, respondent1DQ)
                .toBuilder().caseManagementLocation(caseManagementLoc).build();
    }

    public CaseData getMultiCaseDataForWorkAllocationForOne_V_Two(CaseState state,
                                                 CaseCategory claimType,
                                                 Party.Type claimant1Type,
                                                 Applicant1DQ applicant1DQ,
                                                 Respondent1DQ respondent1DQ,
                                                 Respondent2DQ respondent2DQ) {
        CaseData.CaseDataBuilder<?, ?> builder = CaseData.builder()
            .ccdCaseReference(1234L)
            .courtLocation(new CourtLocation().setCaseLocation(new CaseLocationCivil()
                                                                    .setRegion("2")
                                                                    .setBaseLocation("00000")
                                                                    ))
            .respondent2OrganisationPolicy(new OrganisationPolicy()
                                               .setOrganisation(new Organisation()
                                                                 .setOrganisationID(STRING_CONSTANT))
                                               .setOrgPolicyCaseAssignedRole(
                                                   CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())
                                               .setOrgPolicyReference(STRING_CONSTANT))
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
            .generalAppStatementOfTruth(GAStatementOfTruth.builder().name(STRING_CONSTANT).role(STRING_CONSTANT).build())
            .generalAppEvidenceDocument(wrapElements(new Document()
                                                         .setDocumentUrl(STRING_CONSTANT)
                                                         .setDocumentBinaryUrl(STRING_CONSTANT)
                                                         .setDocumentFileName(STRING_CONSTANT)
                                                         .setDocumentHash(STRING_CONSTANT)))
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
            .applicantSolicitor1UserDetails(new IdamUserDetails()
                                                .setId(STRING_CONSTANT)
                                                .setEmail(APPLICANT_EMAIL_ID_CONSTANT))
            .applicant1OrganisationPolicy(new OrganisationPolicy()
                                              .setOrganisation(new Organisation()
                                                                .setOrganisationID(STRING_CONSTANT))
                                              .setOrgPolicyCaseAssignedRole(
                                                  CaseRole.APPLICANTSOLICITORONE.getFormattedName())
                                              .setOrgPolicyReference(STRING_CONSTANT))
            .respondent1OrganisationPolicy(new OrganisationPolicy()
                                               .setOrganisation(new Organisation()
                                                                 .setOrganisationID(STRING_CONSTANT))
                                               .setOrgPolicyCaseAssignedRole(
                                                   CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
                                               .setOrgPolicyReference(STRING_CONSTANT))
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
                .generalAppStatementOfTruth(GAStatementOfTruth.builder().name(STRING_CONSTANT).role(STRING_CONSTANT).build())
                .generalAppEvidenceDocument(wrapElements(new Document()
                        .setDocumentUrl(STRING_CONSTANT)
                        .setDocumentBinaryUrl(STRING_CONSTANT)
                        .setDocumentFileName(STRING_CONSTANT)
                        .setDocumentHash(STRING_CONSTANT)))
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
                .caseManagementLocation(uk.gov.hmcts.reform.civil.model.genapplication.CaseLocationCivil.builder()
                                            .baseLocation("34567")
                                            .region("4")
                                            .build())
                .isCcmccLocation(YES)
                .caseLink(CaseLink.builder().caseReference("1234").build())
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
                        .fee(new Fee().setCode("FEE_CODE").setCalculatedAmountInPence(BigDecimal.valueOf(10800L))
                                .setVersion("1"))
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
                .generalAppStatementOfTruth(GAStatementOfTruth.builder().name(STRING_CONSTANT).role(STRING_CONSTANT).build())
                .generalAppEvidenceDocument(wrapElements(new Document()
                        .setDocumentUrl(STRING_CONSTANT)
                        .setDocumentBinaryUrl(STRING_CONSTANT)
                        .setDocumentFileName(STRING_CONSTANT)
                        .setDocumentHash(STRING_CONSTANT)))
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
            .generalAppEvidenceDocument(wrapElements(new Document().setDocumentUrl(STRING_CONSTANT)))
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
                .generalAppEvidenceDocument(wrapElements(new Document().setDocumentUrl(STRING_CONSTANT)))
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
            .generalAppEvidenceDocument(wrapElements(new Document().setDocumentUrl(STRING_CONSTANT)))
            .dismissalOrderDocument(singletonList(Element.<CaseDocument>builder().value(pdfDocument).build()))
            .build();
    }

    public CaseData getTestCaseDataWithDismissalOrderStaffPDFDocument(CaseData caseData) {
        return caseData.toBuilder()
                .ccdCaseReference(1234L)
                .generalAppType(GAApplicationType.builder()
                        .types(singletonList(EXTEND_TIME))
                        .build())
                .generalAppEvidenceDocument(wrapElements(new Document().setDocumentUrl(STRING_CONSTANT)))
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
            .generalAppEvidenceDocument(wrapElements(new Document().setDocumentUrl(STRING_CONSTANT)))
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
            .generalAppEvidenceDocument(wrapElements(new Document().setDocumentUrl(STRING_CONSTANT)))
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

    public CaseData getTestCaseDataWithDraftApplicationPDFDocumentLip(CaseData caseData) {
        String uid = "f000aa01-0451-4000-b000-000000000111";
        String uid1 = "f000aa01-0451-4000-b000-000000000000";
        List<Element<CaseDocument>> draftDocs = newArrayList();
        draftDocs.add(Element.<CaseDocument>builder().id(UUID.fromString(uid1))
                          .value(pdfDocument).build());
        draftDocs.add(Element.<CaseDocument>builder().id(UUID.fromString(uid))
                          .value(pdfDocument).build());
        return caseData.toBuilder()
            .ccdCaseReference(1234L)
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .gaDraftDocument(draftDocs)
            .build();
    }

    public CaseData getTestCaseDataWithDraftApplicationPDFDocument(CaseData caseData) {
        String uid = "f000aa01-0451-4000-b000-000000000111";
        String uid1 = "f000aa01-0451-4000-b000-000000000000";
        List<Element<CaseDocument>> draftDocs = newArrayList();
        draftDocs.add(Element.<CaseDocument>builder().id(UUID.fromString(uid1))
                          .value(pdfDocument).build());
        return caseData.toBuilder()
            .ccdCaseReference(1234L)
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .gaDraftDocument(draftDocs)
            .build();
    }

    public CaseData getTestCaseDataWithAdditionalDocument(CaseData caseData) {
        String uid = "f000aa01-0451-4000-b000-000000000111";
        String uid1 = "f000aa01-0451-4000-b000-000000000000";
        return caseData.toBuilder()
            .ccdCaseReference(1234L)
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .gaAddlDoc(singletonList(Element.<CaseDocument>builder().id(UUID.fromString(uid1))
                                               .value(pdfDocument).build()))
            .build();
    }

    public CaseData getTestCaseDataWithDirectionOrderStaffPDFDocument(CaseData caseData) {
        String uid = "f000aa01-0451-4000-b000-000000000111";
        String uid1 = "f000aa01-0451-4000-b000-000000000000";
        List<Element<CaseDocument>> directionOrderDocStaff = new ArrayList<>();
        directionOrderDocStaff.add(Element.<CaseDocument>builder().id(UUID.fromString(uid1)).value(pdfDocument).build());

        return caseData.toBuilder()
                .ccdCaseReference(1234L)
                .generalAppType(GAApplicationType.builder()
                        .types(singletonList(EXTEND_TIME))
                        .build())
                .generalAppEvidenceDocument(wrapElements(new Document().setDocumentUrl(STRING_CONSTANT)))
                .generalOrderDocStaff(singletonList(Element.<CaseDocument>builder().id(UUID.fromString(uid))
                        .value(pdfDocument).build()))
                .directionOrderDocStaff(directionOrderDocStaff)
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

    public CaseData getTestCaseDataWithAddlDocStaffPDFDocument(CaseData caseData) {
        String uid = "f000aa01-0451-4000-b000-000000000111";
        String uid1 = "f000aa01-0451-4000-b000-000000000000";
        return caseData.toBuilder()
            .ccdCaseReference(1234L)
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .gaAddlDocStaff(singletonList(Element.<CaseDocument>builder().id(UUID.fromString(uid1))
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
                .generalAppStatementOfTruth(GAStatementOfTruth.builder().name(STRING_CONSTANT).role(STRING_CONSTANT).build())
                .generalAppEvidenceDocument(wrapElements(new Document()
                        .setDocumentUrl(STRING_CONSTANT)
                        .setDocumentBinaryUrl(STRING_CONSTANT)
                        .setDocumentFileName(STRING_CONSTANT)
                        .setDocumentHash(STRING_CONSTANT)))
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
        CaseLocationCivil caseManagementLoc = new CaseLocationCivil().setRegion("1").setBaseLocation("22222");
        return CaseData
                .builder()
                .ccdCaseReference(1L)
                .caseAccessCategory(SPEC_CLAIM)
                .responseClaimTrack("SMALL_CLAIM")
                .applicant1(Party.builder().type(Party.Type.COMPANY).companyName("Applicant1").build())
                .respondent1(Party.builder().type(Party.Type.COMPANY).companyName("Respondent1").build())
                .addRespondent2(NO)
                .courtLocation(new CourtLocation()
                        .setCaseLocation(new CaseLocationCivil()
                                .setRegion("2")
                                .setBaseLocation("00000")
                                ))
                .applicant1OrganisationPolicy(new OrganisationPolicy()
                        .setOrganisation(new Organisation()
                                .setOrganisationID(STRING_CONSTANT))
                        .setOrgPolicyReference(STRING_CONSTANT))
                .respondent1OrganisationPolicy(new OrganisationPolicy()
                        .setOrganisation(new Organisation()
                                .setOrganisationID(STRING_CONSTANT))
                        .setOrgPolicyReference(STRING_CONSTANT))
                .generalAppType(GAApplicationType.builder()
                        .types(singletonList(VARY_PAYMENT_TERMS_OF_JUDGMENT))
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
                .generalAppN245FormUpload(new Document()
                        .setDocumentUrl(STRING_CONSTANT)
                        .setDocumentBinaryUrl(STRING_CONSTANT)
                        .setDocumentFileName(STRING_CONSTANT)
                        .setDocumentHash(STRING_CONSTANT))
                .generalAppStatementOfTruth(GAStatementOfTruth.builder().name(STRING_CONSTANT).role(STRING_CONSTANT).build())
                .generalAppEvidenceDocument(wrapElements(new Document()
                        .setDocumentUrl(STRING_CONSTANT)
                        .setDocumentBinaryUrl(STRING_CONSTANT)
                        .setDocumentFileName(STRING_CONSTANT)
                        .setDocumentHash(STRING_CONSTANT)))
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
            .caseManagementLocation(caseManagementLoc)
                .build();
    }

    public final CaseDocument pdfDocument = new CaseDocument()
        .setCreatedBy("John")
        .setDocumentName("documentName")
        .setDocumentSize(0L)
        .setDocumentType(GENERAL_ORDER)
        .setCreatedDatetime(now())
        .setDocumentLink(new Document()
                          .setDocumentUrl("fake-url")
                          .setDocumentFileName("file-name")
                          .setDocumentBinaryUrl("binary-url"));

    public final Document pdfDocument1 = new Document()
        .setDocumentUrl("fake-url")
        .setDocumentFileName("file-name")
        .setDocumentBinaryUrl("binary-url");
}
