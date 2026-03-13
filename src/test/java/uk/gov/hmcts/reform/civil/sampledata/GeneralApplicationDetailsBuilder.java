package uk.gov.hmcts.reform.civil.sampledata;

import static com.google.common.collect.Lists.newArrayList;

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

import static java.time.LocalDate.EPOCH;
import static java.time.LocalDateTime.now;
import static java.util.Collections.singletonList;

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

@SuppressWarnings("unchecked")
public class GeneralApplicationDetailsBuilder {

    public static final String STRING_CONSTANT = "this is a string";
    public static final String STRING_NUM_CONSTANT = "123456789";
    public static final String APPLICANT_EMAIL_ID_CONSTANT = "testUser@gmail.com";
    public static final String RESPONDENT_EMAIL_ID_CONSTANT = "respondent@gmail.com";
    public static final DynamicList PBA_ACCOUNTS = new DynamicList();
    public static final LocalDate APP_DATE_EPOCH = EPOCH;
    public static final DynamicList PBALIST = new DynamicList();

    private final ObjectMapper mapper =
            new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static GeneralApplicationDetailsBuilder builder() {
        return new GeneralApplicationDetailsBuilder();
    }

    public CaseData getTestCaseDataForUrgencyCheckMidEvent(
            CaseData caseData, boolean isApplicationUrgent, LocalDate urgencyConsiderationDate) {
        GAUrgencyRequirement gaUrgencyRequirement =
                new GAUrgencyRequirement().setUrgentAppConsiderationDate(urgencyConsiderationDate);
        if (isApplicationUrgent) {
            gaUrgencyRequirement.setGeneralAppUrgency(YES).setReasonsForUrgency(STRING_CONSTANT);
        } else {
            gaUrgencyRequirement.setGeneralAppUrgency(NO);
        }
        return caseData.toBuilder()
                .ccdCaseReference(1234L)
                .respondent2OrganisationPolicy(
                        new OrganisationPolicy()
                                .setOrganisation(
                                        new Organisation().setOrganisationID(STRING_CONSTANT))
                                .setOrgPolicyReference(STRING_CONSTANT))
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
                        wrapElements(new Document().setDocumentUrl(STRING_CONSTANT)))
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
                                .setHearingPreferredLocation(getPreferredLoc())
                                .setHearingDetailsTelephoneNumber(STRING_NUM_CONSTANT)
                                .setReasonForPreferredHearingType(STRING_CONSTANT)
                                .setTelephoneHearingPreferredType(STRING_CONSTANT)
                                .setSupportRequirementSignLanguage(STRING_CONSTANT)
                                .setHearingPreferencesPreferredType(IN_PERSON)
                                .setUnavailableTrialRequiredYesOrNo(YES)
                                .setSupportRequirementLanguageInterpreter(STRING_CONSTANT))
                .build();
    }

    public CaseData getTestCaseDataForApplicationFee(
            CaseData caseData, boolean isConsented, boolean isWithNotice) {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        if (!isConsented) {
            caseDataBuilder
                    .generalAppRespondentAgreement(
                            new GARespondentOrderAgreement().setHasAgreed(NO))
                    .generalAppInformOtherParty(
                            new GAInformOtherParty()
                                    .setIsWithNotice(isWithNotice ? YES : NO)
                                    .setReasonsForWithoutNotice(
                                            isWithNotice ? null : STRING_CONSTANT));
        } else {
            caseDataBuilder
                    .generalAppRespondentAgreement(
                            new GARespondentOrderAgreement().setHasAgreed(YES))
                    .generalAppInformOtherParty(null);
        }
        return caseDataBuilder
                .ccdCaseReference(1234L)
                .respondent2OrganisationPolicy(
                        new OrganisationPolicy()
                                .setOrganisation(
                                        new Organisation().setOrganisationID(STRING_CONSTANT))
                                .setOrgPolicyReference(STRING_CONSTANT))
                .generalAppType(new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                .generalAppPBADetails(new GAPbaDetails())
                .generalAppDetailsOfOrder(STRING_CONSTANT)
                .generalAppReasonsOfOrder(STRING_CONSTANT)
                .generalAppUrgencyRequirement(
                        new GAUrgencyRequirement()
                                .setGeneralAppUrgency(YES)
                                .setReasonsForUrgency(STRING_CONSTANT))
                .generalAppStatementOfTruth(
                        new GAStatementOfTruth().setName(STRING_CONSTANT).setRole(STRING_CONSTANT))
                .generalAppEvidenceDocument(
                        wrapElements(new Document().setDocumentUrl(STRING_CONSTANT)))
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
                                .setHearingPreferredLocation(getPreferredLoc())
                                .setHearingDetailsTelephoneNumber(STRING_NUM_CONSTANT)
                                .setReasonForPreferredHearingType(STRING_CONSTANT)
                                .setTelephoneHearingPreferredType(STRING_CONSTANT)
                                .setSupportRequirementSignLanguage(STRING_CONSTANT)
                                .setHearingPreferencesPreferredType(IN_PERSON)
                                .setUnavailableTrialRequiredYesOrNo(YES)
                                .setSupportRequirementLanguageInterpreter(STRING_CONSTANT))
                .build();
    }

    public CaseData getTestCaseData(CaseData caseData) {

        return caseData.toBuilder()
                .ccdCaseReference(1234L)
                .generalAppType(new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                .respondent2OrganisationPolicy(
                        new OrganisationPolicy()
                                .setOrganisation(
                                        new Organisation().setOrganisationID(STRING_CONSTANT))
                                .setOrgPolicyReference(STRING_CONSTANT))
                .generalAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(NO))
                .generalAppPBADetails(new GAPbaDetails())
                .generalApplications(wrapElements(getGeneralApplication()))
                .applicantSolicitor1UserDetails(
                        new IdamUserDetails()
                                .setId(STRING_CONSTANT)
                                .setEmail(APPLICANT_EMAIL_ID_CONSTANT))
                .applicant1OrganisationPolicy(
                        new OrganisationPolicy()
                                .setOrganisation(
                                        new Organisation().setOrganisationID(STRING_CONSTANT))
                                .setOrgPolicyReference(STRING_CONSTANT))
                .respondent1OrganisationPolicy(
                        new OrganisationPolicy()
                                .setOrganisation(
                                        new Organisation().setOrganisationID(STRING_CONSTANT))
                                .setOrgPolicyReference(STRING_CONSTANT))
                .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
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
                .generalAppEvidenceDocument(
                        wrapElements(new Document().setDocumentUrl(STRING_CONSTANT)))
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
                                                        .setUnavailableTrialDateTo(APP_DATE_EPOCH)
                                                        .setUnavailableTrialDateFrom(
                                                                APP_DATE_EPOCH)))
                                .setSupportRequirementOther(STRING_CONSTANT)
                                .setHearingPreferredLocation(getPreferredLoc())
                                .setHearingDetailsTelephoneNumber(STRING_NUM_CONSTANT)
                                .setReasonForPreferredHearingType(STRING_CONSTANT)
                                .setTelephoneHearingPreferredType(STRING_CONSTANT)
                                .setSupportRequirementSignLanguage(STRING_CONSTANT)
                                .setHearingPreferencesPreferredType(IN_PERSON)
                                .setUnavailableTrialRequiredYesOrNo(YES)
                                .setSupportRequirementLanguageInterpreter(STRING_CONSTANT))
                .build();
    }

    public CaseData getTestCaseDataWithDetails(
            CaseData caseData,
            boolean withGADetails,
            boolean withGADetailsResp,
            boolean withGADetailsResp2,
            boolean withGADetailsMaster,
            Map<String, String> applicationIdStatus) {

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.caseManagementLocation(
                new CaseLocationCivil().setBaseLocation("00000").setRegion("2"));
        caseDataBuilder.locationName("locationOfRegion2");
        caseDataBuilder.ccdCaseReference(1L);
        if (!Collections.isEmpty(applicationIdStatus)) {
            List<GeneralApplication> genApps = new ArrayList<>();
            applicationIdStatus.forEach((key, value) -> genApps.add(getGeneralApplication(key)));
            caseDataBuilder.generalApplications(
                    wrapElements(genApps.toArray(new GeneralApplication[0])));
        }

        if (withGADetails) {
            List<GeneralApplicationsDetails> allGaDetails = new ArrayList<>();
            applicationIdStatus.forEach((key, value) -> allGaDetails.add(getGADetails(key, value)));
            caseDataBuilder.claimantGaAppDetails(
                    wrapElements(allGaDetails.toArray(new GeneralApplicationsDetails[0])));
        }

        if (withGADetailsMaster) {
            List<GeneralApplicationsDetails> allGaDetails = new ArrayList<>();
            applicationIdStatus.forEach((key, value) -> allGaDetails.add(getGADetails(key, value)));
            caseDataBuilder.gaDetailsMasterCollection(
                    wrapElements(allGaDetails.toArray(new GeneralApplicationsDetails[0])));
        }

        List<GADetailsRespondentSol> gaDetailsRespo = new ArrayList<>();
        applicationIdStatus.forEach(
                (key, value) -> gaDetailsRespo.add(getGADetailsRespondent(key, value)));
        if (withGADetailsResp) {
            caseDataBuilder.respondentSolGaAppDetails(
                    wrapElements(gaDetailsRespo.toArray(new GADetailsRespondentSol[0])));
        }

        if (withGADetailsResp2) {
            caseDataBuilder.respondentSolTwoGaAppDetails(
                    wrapElements(gaDetailsRespo.toArray(new GADetailsRespondentSol[0])));
        }
        return caseDataBuilder.build();
    }

    public CaseData getTestCaseDataWithLocationDetailsLip(
            CaseData caseData,
            boolean withGADetails,
            boolean withGADetailsResp,
            boolean withGADetailsResp2,
            boolean withGADetailsMaster,
            Map<String, String> applicationIdStatus) {

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        caseDataBuilder.caseManagementLocation(
                new CaseLocationCivil().setBaseLocation("000000").setRegion("2"));
        caseDataBuilder.respondent1Represented(NO);
        caseDataBuilder.ccdCaseReference(1L);
        if (!Collections.isEmpty(applicationIdStatus)) {
            List<GeneralApplication> genApps = new ArrayList<>();
            applicationIdStatus.forEach((key, value) -> genApps.add(getGeneralApplication(key)));
            caseDataBuilder.generalApplications(
                    wrapElements(genApps.toArray(new GeneralApplication[0])));
        }

        if (withGADetails) {
            List<GeneralApplicationsDetails> allGaDetails = new ArrayList<>();
            applicationIdStatus.forEach((key, value) -> allGaDetails.add(getGADetails(key, value)));
            caseDataBuilder.claimantGaAppDetails(
                    wrapElements(allGaDetails.toArray(new GeneralApplicationsDetails[0])));
        }

        if (withGADetailsMaster) {
            List<GeneralApplicationsDetails> allGaDetails = new ArrayList<>();
            applicationIdStatus.forEach((key, value) -> allGaDetails.add(getGADetails(key, value)));
            caseDataBuilder.gaDetailsMasterCollection(
                    wrapElements(allGaDetails.toArray(new GeneralApplicationsDetails[0])));
        }

        List<GADetailsRespondentSol> gaDetailsRespo = new ArrayList<>();
        applicationIdStatus.forEach(
                (key, value) -> gaDetailsRespo.add(getGADetailsRespondent(key, value)));
        if (withGADetailsResp) {
            caseDataBuilder.respondentSolGaAppDetails(
                    wrapElements(gaDetailsRespo.toArray(new GADetailsRespondentSol[0])));
        }

        if (withGADetailsResp2) {
            caseDataBuilder.respondentSolTwoGaAppDetails(
                    wrapElements(gaDetailsRespo.toArray(new GADetailsRespondentSol[0])));
        }
        return caseDataBuilder.build();
    }

    private GeneralApplicationsDetails getGADetails(String applicationId, String caseState) {
        return new GeneralApplicationsDetails()
                .setGeneralApplicationType("Summary Judgement")
                .setGeneralAppSubmittedDateGAspec(now())
                .setCaseLink(new CaseLink(applicationId))
                .setCaseState(caseState);
    }

    private GADetailsRespondentSol getGADetailsRespondent(String applicationId, String caseState) {
        return new GADetailsRespondentSol()
                .setGeneralApplicationType("Summary Judgement")
                .setGeneralAppSubmittedDateGAspec(now())
                .setCaseLink(new CaseLink(applicationId))
                .setCaseState(caseState);
    }

    public CaseData getTestCaseDataWithEmptyPreferredLocation(CaseData caseData) {

        return caseData.toBuilder()
                .ccdCaseReference(1234L)
                .generalAppType(new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                .respondent2OrganisationPolicy(
                        new OrganisationPolicy()
                                .setOrganisation(
                                        new Organisation().setOrganisationID(STRING_CONSTANT))
                                .setOrgPolicyReference(STRING_CONSTANT))
                .generalAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(NO))
                .generalAppPBADetails(new GAPbaDetails())
                .generalApplications(wrapElements(getGeneralApplication()))
                .applicantSolicitor1UserDetails(
                        new IdamUserDetails()
                                .setId(STRING_CONSTANT)
                                .setEmail(APPLICANT_EMAIL_ID_CONSTANT))
                .applicant1OrganisationPolicy(
                        new OrganisationPolicy()
                                .setOrganisation(
                                        new Organisation().setOrganisationID(STRING_CONSTANT))
                                .setOrgPolicyReference(STRING_CONSTANT))
                .respondent1OrganisationPolicy(
                        new OrganisationPolicy()
                                .setOrganisation(
                                        new Organisation().setOrganisationID(STRING_CONSTANT))
                                .setOrgPolicyReference(STRING_CONSTANT))
                .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
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
                .generalAppEvidenceDocument(
                        wrapElements(new Document().setDocumentUrl(STRING_CONSTANT)))
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
                                                        .setUnavailableTrialDateTo(APP_DATE_EPOCH)
                                                        .setUnavailableTrialDateFrom(
                                                                APP_DATE_EPOCH)))
                                .setSupportRequirementOther(STRING_CONSTANT)
                                .setHearingDetailsTelephoneNumber(STRING_NUM_CONSTANT)
                                .setReasonForPreferredHearingType(STRING_CONSTANT)
                                .setTelephoneHearingPreferredType(STRING_CONSTANT)
                                .setSupportRequirementSignLanguage(STRING_CONSTANT)
                                .setHearingPreferencesPreferredType(IN_PERSON)
                                .setUnavailableTrialRequiredYesOrNo(YES)
                                .setSupportRequirementLanguageInterpreter(STRING_CONSTANT))
                .build();
    }

    public CaseData getTestCaseDataWithEmptyCollectionOfApps(CaseData caseData) {
        CaseLocationCivil caseManagementLoc =
                new CaseLocationCivil().setRegion("1").setBaseLocation("22222");
        return caseData.toBuilder()
                .ccdCaseReference(1234L)
                .caseAccessCategory(SPEC_CLAIM)
                .claimDismissedDeadline(LocalDateTime.now().plusMonths(6))
                .solicitorReferences(
                        new SolicitorReferences()
                                .setApplicantSolicitor1Reference("AppSol1Ref")
                                .setRespondentSolicitor1Reference("RespSol1ref"))
                .responseClaimTrack("MULTI_CLAIM")
                .respondent2OrganisationPolicy(
                        new OrganisationPolicy()
                                .setOrganisation(
                                        new Organisation().setOrganisationID(STRING_CONSTANT))
                                .setOrgPolicyReference(STRING_CONSTANT))
                .applicant1(new Party().setType(Party.Type.COMPANY).setCompanyName("Applicant1"))
                .applicant1DQ(
                        new Applicant1DQ()
                                .setApplicant1DQRequestedCourt(
                                        new RequestedCourt()
                                                .setResponseCourtCode("applicant1DQRequestedCourt")
                                                .setCaseLocation(
                                                        new CaseLocationCivil()
                                                                .setRegion("2")
                                                                .setBaseLocation("00000"))))
                .respondent1(new Party().setType(Party.Type.COMPANY).setCompanyName("Respondent1"))
                .respondent1DQ(
                        new Respondent1DQ()
                                .setRespondent1DQRequestedCourt(
                                        new RequestedCourt()
                                                .setResponseCourtCode("respondent1DQRequestedCourt")
                                                .setCaseLocation(
                                                        new CaseLocationCivil()
                                                                .setRegion("2")
                                                                .setBaseLocation("11111"))))
                .addApplicant2(YES)
                .applicant2(new Party().setType(Party.Type.COMPANY).setCompanyName("Applicant2"))
                .addRespondent2(YES)
                .respondent2(new Party().setType(Party.Type.COMPANY).setCompanyName("Respondent2"))
                .generalAppType(new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                .generalAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(NO))
                .generalAppPBADetails(new GAPbaDetails())
                .applicantSolicitor1UserDetails(
                        new IdamUserDetails()
                                .setId(STRING_CONSTANT)
                                .setEmail(APPLICANT_EMAIL_ID_CONSTANT))
                .applicant1OrganisationPolicy(
                        new OrganisationPolicy()
                                .setOrganisation(
                                        new Organisation().setOrganisationID(STRING_CONSTANT))
                                .setOrgPolicyReference(STRING_CONSTANT))
                .respondent1OrganisationPolicy(
                        new OrganisationPolicy()
                                .setOrganisation(
                                        new Organisation().setOrganisationID(STRING_CONSTANT))
                                .setOrgPolicyReference(STRING_CONSTANT))
                .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
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
                                .setHearingPreferredLocation(getPreferredLoc())
                                .setHearingDetailsTelephoneNumber(STRING_NUM_CONSTANT)
                                .setReasonForPreferredHearingType(STRING_CONSTANT)
                                .setTelephoneHearingPreferredType(STRING_CONSTANT)
                                .setSupportRequirementSignLanguage(STRING_CONSTANT)
                                .setHearingPreferencesPreferredType(IN_PERSON)
                                .setUnavailableTrialRequiredYesOrNo(YES)
                                .setSupportRequirementLanguageInterpreter(STRING_CONSTANT))
                .caseManagementLocation(caseManagementLoc)
                .build();
    }

    public CaseData getTestCaseDataForConsentUnconsentCheck(
            GARespondentOrderAgreement respondentOrderAgreement) {
        return CaseData.builder()
                .caseAccessCategory(UNSPEC_CLAIM)
                .allocatedTrack(AllocatedTrack.FAST_CLAIM)
                .ccdState(AWAITING_RESPONDENT_ACKNOWLEDGEMENT)
                .ccdCaseReference(1234L)
                .respondent2OrganisationPolicy(
                        new OrganisationPolicy()
                                .setOrganisation(
                                        new Organisation().setOrganisationID(STRING_CONSTANT))
                                .setOrgPolicyCaseAssignedRole(
                                        CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())
                                .setOrgPolicyReference(STRING_CONSTANT))
                .applicant1(new Party().setType(Party.Type.COMPANY).setCompanyName("Applicant1"))
                .applicant1DQ(
                        new Applicant1DQ()
                                .setApplicant1DQRequestedCourt(
                                        new RequestedCourt()
                                                .setResponseCourtCode("applicant1DQRequestedCourt")
                                                .setCaseLocation(
                                                        new CaseLocationCivil()
                                                                .setRegion("2")
                                                                .setBaseLocation("11111"))))
                .respondent1(new Party().setType(Party.Type.COMPANY).setCompanyName("Respondent1"))
                .respondent1DQ(
                        new Respondent1DQ()
                                .setRespondent1DQRequestedCourt(
                                        new RequestedCourt()
                                                .setResponseCourtCode("respondent1DQRequestedCourt")
                                                .setCaseLocation(
                                                        new CaseLocationCivil()
                                                                .setRegion("2")
                                                                .setBaseLocation("00000"))))
                .addApplicant2(YES)
                .applicant2(new Party().setType(Party.Type.COMPANY).setCompanyName("Applicant2"))
                .addRespondent2(YES)
                .respondent2(new Party().setType(Party.Type.COMPANY).setCompanyName("Respondent2"))
                .generalAppType(new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                .generalAppRespondentAgreement(respondentOrderAgreement)
                .generalAppUrgencyRequirement(
                        new GAUrgencyRequirement()
                                .setGeneralAppUrgency(YES)
                                .setReasonsForUrgency(STRING_CONSTANT)
                                .setUrgentAppConsiderationDate(APP_DATE_EPOCH))
                .generalAppInformOtherParty(
                        new GAInformOtherParty()
                                .setIsWithNotice(NO)
                                .setReasonsForWithoutNotice(STRING_CONSTANT))
                .generalAppDetailsOfOrder(STRING_CONSTANT)
                .generalAppReasonsOfOrder(STRING_CONSTANT)
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
                                .setHearingPreferredLocation(getPreferredLoc())
                                .setHearingDetailsTelephoneNumber(STRING_NUM_CONSTANT)
                                .setReasonForPreferredHearingType(STRING_CONSTANT)
                                .setTelephoneHearingPreferredType(STRING_CONSTANT)
                                .setSupportRequirementSignLanguage(STRING_CONSTANT)
                                .setHearingPreferencesPreferredType(IN_PERSON)
                                .setUnavailableTrialRequiredYesOrNo(YES)
                                .setSupportRequirementLanguageInterpreter(STRING_CONSTANT))
                .generalAppPBADetails(new GAPbaDetails())
                .applicantSolicitor1UserDetails(
                        new IdamUserDetails()
                                .setId(STRING_CONSTANT)
                                .setEmail(APPLICANT_EMAIL_ID_CONSTANT))
                .applicant1OrganisationPolicy(
                        new OrganisationPolicy()
                                .setOrganisation(
                                        new Organisation().setOrganisationID(STRING_CONSTANT))
                                .setOrgPolicyCaseAssignedRole(
                                        CaseRole.APPLICANTSOLICITORONE.getFormattedName())
                                .setOrgPolicyReference(STRING_CONSTANT))
                .respondent1OrganisationPolicy(
                        new OrganisationPolicy()
                                .setOrganisation(
                                        new Organisation().setOrganisationID(STRING_CONSTANT))
                                .setOrgPolicyCaseAssignedRole(
                                        CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
                                .setOrgPolicyReference(STRING_CONSTANT))
                .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
                .caseNameHmctsInternal("Internal caseName")
                .build();
    }

    public CaseData getTestCaseDataForCaseManagementLocation(
            CaseCategory caseType, CaseState caseState) {
        return CaseData.builder()
                .caseAccessCategory(caseType)
                .ccdState(caseState)
                .ccdCaseReference(1234L)
                .respondent1Represented(NO)
                .caseManagementLocation(
                        new CaseLocationCivil().setRegion("2").setBaseLocation("11111"))
                .respondent2OrganisationPolicy(
                        new OrganisationPolicy()
                                .setOrganisation(
                                        new Organisation().setOrganisationID(STRING_CONSTANT))
                                .setOrgPolicyCaseAssignedRole(
                                        CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())
                                .setOrgPolicyReference(STRING_CONSTANT))
                .applicant1(new Party().setType(Party.Type.COMPANY).setCompanyName("Applicant1"))
                .applicant1DQ(
                        new Applicant1DQ()
                                .setApplicant1DQRequestedCourt(
                                        new RequestedCourt()
                                                .setResponseCourtCode("applicant1DQRequestedCourt")
                                                .setCaseLocation(
                                                        new CaseLocationCivil()
                                                                .setRegion("2")
                                                                .setBaseLocation("11111"))))
                .respondent1(new Party().setType(Party.Type.COMPANY).setCompanyName("Respondent1"))
                .respondent1DQ(
                        new Respondent1DQ()
                                .setRespondent1DQRequestedCourt(
                                        new RequestedCourt()
                                                .setResponseCourtCode("respondent1DQRequestedCourt")
                                                .setCaseLocation(
                                                        new CaseLocationCivil()
                                                                .setRegion("2")
                                                                .setBaseLocation("00000"))))
                .addApplicant2(YES)
                .applicant2(new Party().setType(Party.Type.COMPANY).setCompanyName("Applicant2"))
                .addRespondent2(YES)
                .respondent2(new Party().setType(Party.Type.COMPANY).setCompanyName("Respondent2"))
                .generalAppType(new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                .generalAppUrgencyRequirement(
                        new GAUrgencyRequirement()
                                .setGeneralAppUrgency(YES)
                                .setReasonsForUrgency(STRING_CONSTANT)
                                .setUrgentAppConsiderationDate(APP_DATE_EPOCH))
                .generalAppInformOtherParty(
                        new GAInformOtherParty()
                                .setIsWithNotice(NO)
                                .setReasonsForWithoutNotice(STRING_CONSTANT))
                .generalAppDetailsOfOrder(STRING_CONSTANT)
                .generalAppReasonsOfOrder(STRING_CONSTANT)
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
                                .setHearingPreferredLocation(getPreferredLoc())
                                .setHearingDetailsTelephoneNumber(STRING_NUM_CONSTANT)
                                .setReasonForPreferredHearingType(STRING_CONSTANT)
                                .setTelephoneHearingPreferredType(STRING_CONSTANT)
                                .setSupportRequirementSignLanguage(STRING_CONSTANT)
                                .setHearingPreferencesPreferredType(IN_PERSON)
                                .setUnavailableTrialRequiredYesOrNo(YES)
                                .setSupportRequirementLanguageInterpreter(STRING_CONSTANT))
                .generalAppPBADetails(new GAPbaDetails())
                .applicantSolicitor1UserDetails(
                        new IdamUserDetails()
                                .setId(STRING_CONSTANT)
                                .setEmail(APPLICANT_EMAIL_ID_CONSTANT))
                .applicant1OrganisationPolicy(
                        new OrganisationPolicy()
                                .setOrganisation(
                                        new Organisation().setOrganisationID(STRING_CONSTANT))
                                .setOrgPolicyCaseAssignedRole(
                                        CaseRole.APPLICANTSOLICITORONE.getFormattedName())
                                .setOrgPolicyReference(STRING_CONSTANT))
                .respondent1OrganisationPolicy(
                        new OrganisationPolicy()
                                .setOrganisation(
                                        new Organisation().setOrganisationID(STRING_CONSTANT))
                                .setOrgPolicyCaseAssignedRole(
                                        CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
                                .setOrgPolicyReference(STRING_CONSTANT))
                .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
                .build();
    }

    public CaseData getTestCaseDataForStatementOfTruthCheck(
            GARespondentOrderAgreement respondentOrderAgreement) {
        return CaseData.builder()
                .caseAccessCategory(UNSPEC_CLAIM)
                .allocatedTrack(AllocatedTrack.INTERMEDIATE_CLAIM)
                .ccdState(AWAITING_RESPONDENT_ACKNOWLEDGEMENT)
                .ccdCaseReference(1234L)
                .respondent2OrganisationPolicy(
                        new OrganisationPolicy()
                                .setOrganisation(
                                        new Organisation().setOrganisationID(STRING_CONSTANT))
                                .setOrgPolicyCaseAssignedRole(
                                        CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())
                                .setOrgPolicyReference(STRING_CONSTANT))
                .applicant1(new Party().setType(Party.Type.COMPANY).setCompanyName("Applicant1"))
                .applicant1DQ(
                        new Applicant1DQ()
                                .setApplicant1DQRequestedCourt(
                                        new RequestedCourt()
                                                .setResponseCourtCode("applicant1DQRequestedCourt")
                                                .setCaseLocation(
                                                        new CaseLocationCivil()
                                                                .setRegion("2")
                                                                .setBaseLocation("11111"))))
                .respondent1(new Party().setType(Party.Type.COMPANY).setCompanyName("Respondent1"))
                .respondent1DQ(
                        new Respondent1DQ()
                                .setRespondent1DQRequestedCourt(
                                        new RequestedCourt()
                                                .setResponseCourtCode("respondent1DQRequestedCourt")
                                                .setCaseLocation(
                                                        new CaseLocationCivil()
                                                                .setRegion("2")
                                                                .setBaseLocation("00000"))))
                .addApplicant2(YES)
                .applicant2(new Party().setType(Party.Type.COMPANY).setCompanyName("Applicant2"))
                .addRespondent2(YES)
                .respondent2(new Party().setType(Party.Type.COMPANY).setCompanyName("Respondent2"))
                .generalAppType(new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                .generalAppRespondentAgreement(respondentOrderAgreement)
                .generalAppUrgencyRequirement(
                        new GAUrgencyRequirement()
                                .setGeneralAppUrgency(YES)
                                .setReasonsForUrgency(STRING_CONSTANT)
                                .setUrgentAppConsiderationDate(APP_DATE_EPOCH))
                .generalAppInformOtherParty(
                        new GAInformOtherParty()
                                .setIsWithNotice(NO)
                                .setReasonsForWithoutNotice(STRING_CONSTANT))
                .generalAppDetailsOfOrder(STRING_CONSTANT)
                .generalAppReasonsOfOrder(STRING_CONSTANT)
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
                                .setHearingPreferredLocation(getPreferredLoc())
                                .setHearingDetailsTelephoneNumber(STRING_NUM_CONSTANT)
                                .setReasonForPreferredHearingType(STRING_CONSTANT)
                                .setTelephoneHearingPreferredType(STRING_CONSTANT)
                                .setSupportRequirementSignLanguage(STRING_CONSTANT)
                                .setHearingPreferencesPreferredType(IN_PERSON)
                                .setUnavailableTrialRequiredYesOrNo(YES)
                                .setSupportRequirementLanguageInterpreter(STRING_CONSTANT))
                .generalAppPBADetails(new GAPbaDetails())
                .applicantSolicitor1UserDetails(
                        new IdamUserDetails()
                                .setId(STRING_CONSTANT)
                                .setEmail(APPLICANT_EMAIL_ID_CONSTANT))
                .applicant1OrganisationPolicy(
                        new OrganisationPolicy()
                                .setOrganisation(
                                        new Organisation().setOrganisationID(STRING_CONSTANT))
                                .setOrgPolicyCaseAssignedRole(
                                        CaseRole.APPLICANTSOLICITORONE.getFormattedName())
                                .setOrgPolicyReference(STRING_CONSTANT))
                .respondent1OrganisationPolicy(
                        new OrganisationPolicy()
                                .setOrganisation(
                                        new Organisation().setOrganisationID(STRING_CONSTANT))
                                .setOrgPolicyCaseAssignedRole(
                                        CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
                                .setOrgPolicyReference(STRING_CONSTANT))
                .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
                .build();
    }

    public CaseData getTestCaseDataSPEC(CaseCategory claimType) {
        CaseLocationCivil caseManagementLoc =
                new CaseLocationCivil().setRegion("1").setBaseLocation("22222");
        return CaseData.builder()
                .ccdCaseReference(1234L)
                .caseAccessCategory(claimType)
                .submittedDate(LocalDateTime.of(2025, 5, 5, 0, 0, 0))
                .courtLocation(
                        new CourtLocation()
                                .setCaseLocation(
                                        new CaseLocationCivil()
                                                .setRegion("2")
                                                .setBaseLocation("00000")))
                .respondent2OrganisationPolicy(
                        new OrganisationPolicy()
                                .setOrganisation(
                                        new Organisation().setOrganisationID(STRING_CONSTANT))
                                .setOrgPolicyCaseAssignedRole(
                                        CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())
                                .setOrgPolicyReference(STRING_CONSTANT))
                .applicant1(new Party().setType(Party.Type.COMPANY).setCompanyName("Applicant1"))
                .respondent1(new Party().setType(Party.Type.COMPANY).setCompanyName("Respondent1"))
                .addApplicant2(YES)
                .applicant2(new Party().setType(Party.Type.COMPANY).setCompanyName("Applicant2"))
                .addRespondent2(YES)
                .respondent2(new Party().setType(Party.Type.COMPANY).setCompanyName("Respondent2"))
                .generalAppType(new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                .generalAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(YES))
                .generalAppUrgencyRequirement(
                        new GAUrgencyRequirement()
                                .setGeneralAppUrgency(YES)
                                .setReasonsForUrgency(STRING_CONSTANT)
                                .setUrgentAppConsiderationDate(APP_DATE_EPOCH))
                .generalAppInformOtherParty(
                        new GAInformOtherParty()
                                .setIsWithNotice(NO)
                                .setReasonsForWithoutNotice(STRING_CONSTANT))
                .generalAppDetailsOfOrder(STRING_CONSTANT)
                .generalAppReasonsOfOrder(STRING_CONSTANT)
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
                .generalAppPBADetails(new GAPbaDetails())
                .applicantSolicitor1UserDetails(
                        new IdamUserDetails()
                                .setId(STRING_CONSTANT)
                                .setEmail(APPLICANT_EMAIL_ID_CONSTANT))
                .applicant1OrganisationPolicy(
                        new OrganisationPolicy()
                                .setOrganisation(
                                        new Organisation().setOrganisationID(STRING_CONSTANT))
                                .setOrgPolicyCaseAssignedRole(
                                        CaseRole.APPLICANTSOLICITORONE.getFormattedName())
                                .setOrgPolicyReference(STRING_CONSTANT))
                .respondent1OrganisationPolicy(
                        new OrganisationPolicy()
                                .setOrganisation(
                                        new Organisation().setOrganisationID(STRING_CONSTANT))
                                .setOrgPolicyCaseAssignedRole(
                                        CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
                                .setOrgPolicyReference(STRING_CONSTANT))
                .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
                .caseManagementLocation(caseManagementLoc)
                .build();
    }

    public CaseData getCaseDataForWorkAllocation(
            CaseState state,
            CaseCategory claimType,
            Party.Type claimant1Type,
            Applicant1DQ applicant1DQ,
            Respondent1DQ respondent1DQ,
            Respondent2DQ respondent2DQ) {
        CaseLocationCivil caseManagementLoc =
                new CaseLocationCivil().setRegion("1").setBaseLocation("22222");
        CaseData.CaseDataBuilder<?, ?> builder =
                CaseData.builder()
                        .caseAccessCategory(claimType)
                        .ccdCaseReference(1234L)
                        .courtLocation(
                                new CourtLocation()
                                        .setCaseLocation(
                                                new CaseLocationCivil()
                                                        .setRegion("2")
                                                        .setBaseLocation("00000")))
                        .respondent2OrganisationPolicy(
                                new OrganisationPolicy()
                                        .setOrganisation(
                                                new Organisation()
                                                        .setOrganisationID(STRING_CONSTANT))
                                        .setOrgPolicyCaseAssignedRole(
                                                CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())
                                        .setOrgPolicyReference(STRING_CONSTANT))
                        .applicant1(new Party().setType(claimant1Type).setCompanyName("Applicant1"))
                        .respondent1(
                                new Party().setType(claimant1Type).setCompanyName("Respondent1"))
                        .addApplicant2(YES)
                        .applicant2(
                                new Party()
                                        .setType(Party.Type.COMPANY)
                                        .setCompanyName("Applicant2"))
                        .addRespondent2(YES)
                        .respondent2(
                                new Party()
                                        .setType(Party.Type.COMPANY)
                                        .setCompanyName("Respondent2"))
                        .generalAppType(
                                new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                        .generalAppRespondentAgreement(
                                new GARespondentOrderAgreement().setHasAgreed(NO))
                        .generalAppUrgencyRequirement(
                                new GAUrgencyRequirement()
                                        .setGeneralAppUrgency(YES)
                                        .setReasonsForUrgency(STRING_CONSTANT)
                                        .setUrgentAppConsiderationDate(APP_DATE_EPOCH))
                        .generalAppInformOtherParty(
                                new GAInformOtherParty()
                                        .setIsWithNotice(NO)
                                        .setReasonsForWithoutNotice(STRING_CONSTANT))
                        .generalAppDetailsOfOrder(STRING_CONSTANT)
                        .generalAppReasonsOfOrder(STRING_CONSTANT)
                        .generalAppStatementOfTruth(
                                new GAStatementOfTruth()
                                        .setName(STRING_CONSTANT)
                                        .setRole(STRING_CONSTANT))
                        .generalAppEvidenceDocument(
                                wrapElements(
                                        new Document()
                                                .setDocumentUrl(STRING_CONSTANT)
                                                .setDocumentBinaryUrl(STRING_CONSTANT)
                                                .setDocumentFileName(STRING_CONSTANT)
                                                .setDocumentHash(STRING_CONSTANT)))
                        .respondent1ResponseDate(LocalDateTime.parse("2022-09-25T15:20:37.2363012"))
                        .respondent2ResponseDate(LocalDateTime.parse("2022-09-26T15:20:37.2363012"))
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
                                        .setSupportRequirementLanguageInterpreter(STRING_CONSTANT))
                        .generalAppPBADetails(new GAPbaDetails())
                        .applicantSolicitor1UserDetails(
                                new IdamUserDetails()
                                        .setId(STRING_CONSTANT)
                                        .setEmail(APPLICANT_EMAIL_ID_CONSTANT))
                        .applicant1OrganisationPolicy(
                                new OrganisationPolicy()
                                        .setOrganisation(
                                                new Organisation()
                                                        .setOrganisationID(STRING_CONSTANT))
                                        .setOrgPolicyCaseAssignedRole(
                                                CaseRole.APPLICANTSOLICITORONE.getFormattedName())
                                        .setOrgPolicyReference(STRING_CONSTANT))
                        .respondent1OrganisationPolicy(
                                new OrganisationPolicy()
                                        .setOrganisation(
                                                new Organisation()
                                                        .setOrganisationID(STRING_CONSTANT))
                                        .setOrgPolicyCaseAssignedRole(
                                                CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
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

    public CaseData getCaseDataForWorkAllocation1V1(
            CaseState state,
            CaseCategory claimType,
            Party.Type respondent1Type,
            Applicant1DQ applicant1DQ,
            Respondent1DQ respondent1DQ) {
        CaseData.CaseDataBuilder<?, ?> builder =
                CaseData.builder()
                        .ccdCaseReference(1234L)
                        .courtLocation(
                                new CourtLocation()
                                        .setCaseLocation(
                                                new CaseLocationCivil()
                                                        .setRegion("2")
                                                        .setBaseLocation("00000")))
                        .applicant1(
                                new Party()
                                        .setType(Party.Type.SOLE_TRADER)
                                        .setCompanyName("Applicant1"))
                        .respondent1(
                                new Party().setType(respondent1Type).setCompanyName("Respondent1"))
                        .addApplicant2(NO)
                        .addRespondent2(NO)
                        .generalAppType(
                                new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                        .generalAppRespondentAgreement(
                                new GARespondentOrderAgreement().setHasAgreed(NO))
                        .generalAppUrgencyRequirement(
                                new GAUrgencyRequirement()
                                        .setGeneralAppUrgency(YES)
                                        .setReasonsForUrgency(STRING_CONSTANT)
                                        .setUrgentAppConsiderationDate(APP_DATE_EPOCH))
                        .generalAppInformOtherParty(
                                new GAInformOtherParty()
                                        .setIsWithNotice(NO)
                                        .setReasonsForWithoutNotice(STRING_CONSTANT))
                        .generalAppDetailsOfOrder(STRING_CONSTANT)
                        .generalAppReasonsOfOrder(STRING_CONSTANT)
                        .generalAppStatementOfTruth(
                                new GAStatementOfTruth()
                                        .setName(STRING_CONSTANT)
                                        .setRole(STRING_CONSTANT))
                        .generalAppEvidenceDocument(
                                wrapElements(
                                        new Document()
                                                .setDocumentUrl(STRING_CONSTANT)
                                                .setDocumentBinaryUrl(STRING_CONSTANT)
                                                .setDocumentFileName(STRING_CONSTANT)
                                                .setDocumentHash(STRING_CONSTANT)))
                        .respondent1ResponseDate(LocalDateTime.parse("2022-09-25T15:20:37.2363012"))
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
                                        .setSupportRequirementLanguageInterpreter(STRING_CONSTANT))
                        .generalAppPBADetails(new GAPbaDetails())
                        .applicantSolicitor1UserDetails(
                                new IdamUserDetails()
                                        .setId(STRING_CONSTANT)
                                        .setEmail(APPLICANT_EMAIL_ID_CONSTANT))
                        .applicant1OrganisationPolicy(
                                new OrganisationPolicy()
                                        .setOrganisation(
                                                new Organisation()
                                                        .setOrganisationID(STRING_CONSTANT))
                                        .setOrgPolicyCaseAssignedRole(
                                                CaseRole.APPLICANTSOLICITORONE.getFormattedName())
                                        .setOrgPolicyReference(STRING_CONSTANT))
                        .respondent1OrganisationPolicy(
                                new OrganisationPolicy()
                                        .setOrganisation(
                                                new Organisation()
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
                new Applicant1DQ()
                        .setApplicant1DQRequestedCourt(
                                new RequestedCourt()
                                        .setResponseCourtCode("applicant1DQRequestedCourt")
                                        .setCaseLocation(
                                                new CaseLocationCivil()
                                                        .setRegion("2")
                                                        .setBaseLocation("00000")));
        Respondent1DQ respondent1DQ =
                new Respondent1DQ()
                        .setRespondent1DQRequestedCourt(
                                new RequestedCourt()
                                        .setResponseCourtCode(null)
                                        .setCaseLocation(
                                                new CaseLocationCivil()
                                                        .setRegion("2")
                                                        .setBaseLocation("11111")));
        CaseLocationCivil caseManagementLoc =
                new CaseLocationCivil().setRegion("1").setBaseLocation("22222");
        return getCaseDataForWorkAllocation1V1(
                        null, SPEC_CLAIM, INDIVIDUAL, applicant1DQ, respondent1DQ)
                .toBuilder()
                .caseManagementLocation(caseManagementLoc)
                .build();
    }

    public CaseData getMultiCaseDataForWorkAllocationForOne_V_Two(
            CaseState state,
            CaseCategory claimType,
            Party.Type claimant1Type,
            Applicant1DQ applicant1DQ,
            Respondent1DQ respondent1DQ,
            Respondent2DQ respondent2DQ) {
        CaseData.CaseDataBuilder<?, ?> builder =
                CaseData.builder()
                        .ccdCaseReference(1234L)
                        .courtLocation(
                                new CourtLocation()
                                        .setCaseLocation(
                                                new CaseLocationCivil()
                                                        .setRegion("2")
                                                        .setBaseLocation("00000")))
                        .respondent2OrganisationPolicy(
                                new OrganisationPolicy()
                                        .setOrganisation(
                                                new Organisation()
                                                        .setOrganisationID(STRING_CONSTANT))
                                        .setOrgPolicyCaseAssignedRole(
                                                CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())
                                        .setOrgPolicyReference(STRING_CONSTANT))
                        .applicant1(new Party().setType(claimant1Type).setCompanyName("Applicant1"))
                        .respondent1(
                                new Party().setType(claimant1Type).setCompanyName("Respondent1"))
                        .addRespondent2(YES)
                        .respondent2(
                                new Party()
                                        .setType(Party.Type.COMPANY)
                                        .setCompanyName("Respondent2"))
                        .generalAppType(
                                new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                        .generalAppRespondentAgreement(
                                new GARespondentOrderAgreement().setHasAgreed(NO))
                        .generalAppUrgencyRequirement(
                                new GAUrgencyRequirement()
                                        .setGeneralAppUrgency(YES)
                                        .setReasonsForUrgency(STRING_CONSTANT)
                                        .setUrgentAppConsiderationDate(APP_DATE_EPOCH))
                        .generalAppInformOtherParty(
                                new GAInformOtherParty()
                                        .setIsWithNotice(NO)
                                        .setReasonsForWithoutNotice(STRING_CONSTANT))
                        .generalAppDetailsOfOrder(STRING_CONSTANT)
                        .generalAppReasonsOfOrder(STRING_CONSTANT)
                        .generalAppStatementOfTruth(
                                new GAStatementOfTruth()
                                        .setName(STRING_CONSTANT)
                                        .setRole(STRING_CONSTANT))
                        .generalAppEvidenceDocument(
                                wrapElements(
                                        new Document()
                                                .setDocumentUrl(STRING_CONSTANT)
                                                .setDocumentBinaryUrl(STRING_CONSTANT)
                                                .setDocumentFileName(STRING_CONSTANT)
                                                .setDocumentHash(STRING_CONSTANT)))
                        .respondent1ResponseDate(LocalDateTime.parse("2022-09-27T15:20:37.2363012"))
                        .respondent2ResponseDate(LocalDateTime.parse("2022-09-25T15:20:37.2363012"))
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
                                        .setSupportRequirementLanguageInterpreter(STRING_CONSTANT))
                        .generalAppPBADetails(new GAPbaDetails())
                        .applicantSolicitor1UserDetails(
                                new IdamUserDetails()
                                        .setId(STRING_CONSTANT)
                                        .setEmail(APPLICANT_EMAIL_ID_CONSTANT))
                        .applicant1OrganisationPolicy(
                                new OrganisationPolicy()
                                        .setOrganisation(
                                                new Organisation()
                                                        .setOrganisationID(STRING_CONSTANT))
                                        .setOrgPolicyCaseAssignedRole(
                                                CaseRole.APPLICANTSOLICITORONE.getFormattedName())
                                        .setOrgPolicyReference(STRING_CONSTANT))
                        .respondent1OrganisationPolicy(
                                new OrganisationPolicy()
                                        .setOrganisation(
                                                new Organisation()
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
                                                                .setUnavailableTrialDateTo(
                                                                        APP_DATE_EPOCH)
                                                                .setUnavailableTrialDateFrom(
                                                                        APP_DATE_EPOCH)))
                                        .setSupportRequirementOther(STRING_CONSTANT)
                                        .setHearingPreferredLocation(getPreferredLoc())
                                        .setHearingDetailsTelephoneNumber(STRING_NUM_CONSTANT)
                                        .setReasonForPreferredHearingType(STRING_CONSTANT)
                                        .setTelephoneHearingPreferredType(STRING_CONSTANT)
                                        .setSupportRequirementSignLanguage(STRING_CONSTANT)
                                        .setHearingPreferencesPreferredType(IN_PERSON)
                                        .setUnavailableTrialRequiredYesOrNo(YES)
                                        .setSupportRequirementLanguageInterpreter(STRING_CONSTANT));
        return getTestCaseDataWithEmptyCollectionOfApps(caseData).toBuilder()
                .generalApplications(wrapElements(application))
                .build();
    }

    public GeneralApplication getGeneralApplication(String key) {
        return getGeneralApplication().copy().setCaseLink(new CaseLink(key));
    }

    public GeneralApplication getGeneralApplication() {
        GeneralApplication builder = new GeneralApplication();
        return builder.setGeneralAppType(
                        new GAApplicationType().setTypes(singletonList(SUMMARY_JUDGEMENT)))
                .setCaseManagementLocation(
                        new uk.gov.hmcts.reform.civil.model.genapplication.CaseLocationCivil()
                                .setBaseLocation("34567")
                                .setRegion("4"))
                .setIsCcmccLocation(YES)
                .setCaseLink(new CaseLink("1234"))
                .setGeneralAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(NO))
                .setGeneralAppInformOtherParty(
                        new GAInformOtherParty()
                                .setIsWithNotice(YES)
                                .setReasonsForWithoutNotice(STRING_CONSTANT))
                .setGeneralAppUrgencyRequirement(
                        new GAUrgencyRequirement()
                                .setGeneralAppUrgency(NO)
                                .setReasonsForUrgency(STRING_CONSTANT)
                                .setUrgentAppConsiderationDate(APP_DATE_EPOCH))
                .setGeneralAppType(new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                .setIsMultiParty(NO)
                .setGeneralAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(NO))
                .setGeneralAppPBADetails(
                        new GAPbaDetails()
                                .setFee(
                                        new Fee()
                                                .setCode("FEE_CODE")
                                                .setCalculatedAmountInPence(
                                                        BigDecimal.valueOf(10800L))
                                                .setVersion("1")))
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
                        new GAStatementOfTruth().setName(STRING_CONSTANT).setRole(STRING_CONSTANT))
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
                                                        .setUnavailableTrialDateFrom(APP_DATE_EPOCH)
                                                        .setUnavailableTrialDateTo(APP_DATE_EPOCH)))
                                .setSupportRequirementOther(STRING_CONSTANT)
                                .setHearingPreferredLocation(getPreferredLoc())
                                .setHearingDetailsTelephoneNumber(STRING_NUM_CONSTANT)
                                .setReasonForPreferredHearingType(STRING_CONSTANT)
                                .setTelephoneHearingPreferredType(STRING_CONSTANT)
                                .setSupportRequirementSignLanguage(STRING_CONSTANT)
                                .setHearingPreferencesPreferredType(IN_PERSON)
                                .setUnavailableTrialRequiredYesOrNo(YES)
                                .setSupportRequirementLanguageInterpreter(STRING_CONSTANT));
    }

    public DynamicList getPreferredLoc() {
        DynamicList dynamicList =
                fromList(
                        List.of(
                                "ABCD - RG0 0AL",
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
                .generalAppType(new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                .generalAppEvidenceDocument(
                        wrapElements(new Document().setDocumentUrl(STRING_CONSTANT)))
                .generalOrderDocument(
                        singletonList(
                                new Element<CaseDocument>().setId(UUID.fromString(uid)).setValue(pdfDocument)))
                .build();
    }

    public CaseData getTestCaseDataWithGeneralOrderStaffPDFDocument(CaseData caseData) {
        String uid = "f000aa01-0451-4000-b000-000000000111";
        return caseData.toBuilder()
                .ccdCaseReference(1234L)
                .generalAppType(new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                .generalAppEvidenceDocument(
                        wrapElements(new Document().setDocumentUrl(STRING_CONSTANT)))
                .generalOrderDocStaff(
                        singletonList(
                                new Element<CaseDocument>().setId(UUID.fromString(uid)).setValue(pdfDocument)))
                .build();
    }

    public CaseData getTestCaseDataWithDismissalOrderPDFDocument(CaseData caseData) {
        return caseData.toBuilder()
                .ccdCaseReference(1234L)
                .generalAppType(new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                .generalAppEvidenceDocument(
                        wrapElements(new Document().setDocumentUrl(STRING_CONSTANT)))
                .dismissalOrderDocument(
                        singletonList(new Element<CaseDocument>().setValue(pdfDocument)))
                .build();
    }

    public CaseData getTestCaseDataWithDismissalOrderStaffPDFDocument(CaseData caseData) {
        return caseData.toBuilder()
                .ccdCaseReference(1234L)
                .generalAppType(new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                .generalAppEvidenceDocument(
                        wrapElements(new Document().setDocumentUrl(STRING_CONSTANT)))
                .dismissalOrderDocStaff(
                        singletonList(new Element<CaseDocument>().setValue(pdfDocument)))
                .build();
    }

    public CaseData getTestCaseDataWithDirectionOrderPDFDocument(CaseData caseData) {
        String uid = "f000aa01-0451-4000-b000-000000000111";
        String uid1 = "f000aa01-0451-4000-b000-000000000000";
        return caseData.toBuilder()
                .ccdCaseReference(1234L)
                .generalAppType(new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                .generalAppEvidenceDocument(
                        wrapElements(new Document().setDocumentUrl(STRING_CONSTANT)))
                .generalOrderDocument(
                        singletonList(
                                new Element<CaseDocument>().setId(UUID.fromString(uid)).setValue(pdfDocument)))
                .directionOrderDocument(
                        singletonList(
                                new Element<CaseDocument>().setId(UUID.fromString(uid1)).setValue(pdfDocument)))
                .build();
    }

    public CaseData getTestCaseDataWithDirectionResponseDocument(CaseData caseData) {
        String uid = "f000aa01-0451-4000-b000-000000000111";
        String uid1 = "f000aa01-0451-4000-b000-000000000000";
        return caseData.toBuilder()
                .ccdCaseReference(1234L)
                .generalAppType(new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                .generalAppEvidenceDocument(
                        wrapElements(new Document().setDocumentUrl(STRING_CONSTANT)))
                .generalOrderDocument(
                        singletonList(
                                new Element<CaseDocument>().setId(UUID.fromString(uid)).setValue(pdfDocument)))
                .gaRespDocument(
                        singletonList(
                                new Element<Document>().setId(UUID.fromString(uid1)).setValue(pdfDocument1)))
                .build();
    }

    public CaseData getTestCaseDataWithConsentOrderPDFDocument(CaseData caseData) {
        String uid = "f000aa01-0451-4000-b000-000000000111";
        String uid1 = "f000aa01-0451-4000-b000-000000000000";
        return caseData.toBuilder()
                .ccdCaseReference(1234L)
                .generalAppType(new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                .consentOrderDocument(
                        singletonList(
                                new Element<CaseDocument>().setId(UUID.fromString(uid1)).setValue(pdfDocument)))
                .build();
    }

    public CaseData getTestCaseDataWithDraftApplicationPDFDocumentLip(CaseData caseData) {
        String uid = "f000aa01-0451-4000-b000-000000000111";
        String uid1 = "f000aa01-0451-4000-b000-000000000000";
        List<Element<CaseDocument>> draftDocs = newArrayList();
        draftDocs.add(
                new Element<CaseDocument>().setId(UUID.fromString(uid1)).setValue(pdfDocument));
        draftDocs.add(
                new Element<CaseDocument>().setId(UUID.fromString(uid)).setValue(pdfDocument));
        return caseData.toBuilder()
                .ccdCaseReference(1234L)
                .generalAppType(new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                .gaDraftDocument(draftDocs)
                .build();
    }

    public CaseData getTestCaseDataWithDraftApplicationPDFDocument(CaseData caseData) {
        String uid = "f000aa01-0451-4000-b000-000000000111";
        String uid1 = "f000aa01-0451-4000-b000-000000000000";
        List<Element<CaseDocument>> draftDocs = newArrayList();
        draftDocs.add(
                new Element<CaseDocument>().setId(UUID.fromString(uid1)).setValue(pdfDocument));
        return caseData.toBuilder()
                .ccdCaseReference(1234L)
                .generalAppType(new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                .gaDraftDocument(draftDocs)
                .build();
    }

    public CaseData getTestCaseDataWithAdditionalDocument(CaseData caseData) {
        String uid = "f000aa01-0451-4000-b000-000000000111";
        String uid1 = "f000aa01-0451-4000-b000-000000000000";
        return caseData.toBuilder()
                .ccdCaseReference(1234L)
                .generalAppType(new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                .gaAddlDoc(
                        singletonList(
                                new Element<CaseDocument>().setId(UUID.fromString(uid1)).setValue(pdfDocument)))
                .build();
    }

    public CaseData getTestCaseDataWithDirectionOrderStaffPDFDocument(CaseData caseData) {
        String uid = "f000aa01-0451-4000-b000-000000000111";
        String uid1 = "f000aa01-0451-4000-b000-000000000000";
        List<Element<CaseDocument>> directionOrderDocStaff = new ArrayList<>();
        directionOrderDocStaff.add(
                new Element<CaseDocument>().setId(UUID.fromString(uid1)).setValue(pdfDocument));

        return caseData.toBuilder()
                .ccdCaseReference(1234L)
                .generalAppType(new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                .generalAppEvidenceDocument(
                        wrapElements(new Document().setDocumentUrl(STRING_CONSTANT)))
                .generalOrderDocStaff(
                        singletonList(
                                new Element<CaseDocument>().setId(UUID.fromString(uid)).setValue(pdfDocument)))
                .directionOrderDocStaff(directionOrderDocStaff)
                .build();
    }

    public CaseData getTestCaseDataWithConsentOrderStaffPDFDocument(CaseData caseData) {
        String uid = "f000aa01-0451-4000-b000-000000000111";
        String uid1 = "f000aa01-0451-4000-b000-000000000000";
        return caseData.toBuilder()
                .ccdCaseReference(1234L)
                .generalAppType(new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                .consentOrderDocStaff(
                        singletonList(
                                new Element<CaseDocument>().setId(UUID.fromString(uid1)).setValue(pdfDocument)))
                .build();
    }

    public CaseData getTestCaseDataWithDraftStaffPDFDocument(CaseData caseData) {
        String uid = "f000aa01-0451-4000-b000-000000000111";
        String uid1 = "f000aa01-0451-4000-b000-000000000000";
        return caseData.toBuilder()
                .ccdCaseReference(1234L)
                .generalAppType(new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                .gaDraftDocStaff(
                        singletonList(
                                new Element<CaseDocument>().setId(UUID.fromString(uid1)).setValue(pdfDocument)))
                .build();
    }

    public CaseData getTestCaseDataWithAddlDocStaffPDFDocument(CaseData caseData) {
        String uid = "f000aa01-0451-4000-b000-000000000111";
        String uid1 = "f000aa01-0451-4000-b000-000000000000";
        return caseData.toBuilder()
                .ccdCaseReference(1234L)
                .generalAppType(new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                .gaAddlDocStaff(
                        singletonList(
                                new Element<CaseDocument>().setId(UUID.fromString(uid1)).setValue(pdfDocument)))
                .build();
    }

    public CaseData getTestCaseDataWithHearingNoticeDocumentPDFDocument(CaseData caseData) {
        String uid1 = "f000aa01-0451-4000-b000-000000000000";
        return caseData.toBuilder()
                .ccdCaseReference(1234L)
                .generalAppType(new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                .hearingNoticeDocument(
                        singletonList(
                                new Element<CaseDocument>().setId(UUID.fromString(uid1)).setValue(pdfDocument)))
                .build();
    }

    public CaseData getTestCaseDataWithHearingNoticeStaffDocumentPDFDocument(CaseData caseData) {
        String uid1 = "f000aa01-0451-4000-b000-000000000000";
        return caseData.toBuilder()
                .ccdCaseReference(1234L)
                .generalAppType(new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                .hearingNoticeDocStaff(
                        singletonList(
                                new Element<CaseDocument>().setId(UUID.fromString(uid1)).setValue(pdfDocument)))
                .build();
    }

    public CaseData getTriggerGeneralApplicationTestData() {
        return CaseData.builder()
                .ccdCaseReference(1L)
                .generalAppType(new GAApplicationType().setTypes(singletonList(EXTEND_TIME)))
                .generalAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(NO))
                .generalAppPBADetails(new GAPbaDetails())
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
                                .setHearingPreferredLocation(getPreferredLoc())
                                .setHearingDetailsTelephoneNumber(STRING_NUM_CONSTANT)
                                .setReasonForPreferredHearingType(STRING_CONSTANT)
                                .setTelephoneHearingPreferredType(STRING_CONSTANT)
                                .setSupportRequirementSignLanguage(STRING_CONSTANT)
                                .setHearingPreferencesPreferredType(IN_PERSON)
                                .setUnavailableTrialRequiredYesOrNo(YES)
                                .setSupportRequirementLanguageInterpreter(STRING_CONSTANT))
                .build();
    }

    public CaseData getVaryJudgmentWithN245TestData() {
        CaseLocationCivil caseManagementLoc =
                new CaseLocationCivil().setRegion("1").setBaseLocation("22222");
        return CaseData.builder()
                .ccdCaseReference(1L)
                .caseAccessCategory(SPEC_CLAIM)
                .responseClaimTrack("SMALL_CLAIM")
                .applicant1(new Party().setType(Party.Type.COMPANY).setCompanyName("Applicant1"))
                .respondent1(new Party().setType(Party.Type.COMPANY).setCompanyName("Respondent1"))
                .addRespondent2(NO)
                .courtLocation(
                        new CourtLocation()
                                .setCaseLocation(
                                        new CaseLocationCivil()
                                                .setRegion("2")
                                                .setBaseLocation("00000")))
                .applicant1OrganisationPolicy(
                        new OrganisationPolicy()
                                .setOrganisation(
                                        new Organisation().setOrganisationID(STRING_CONSTANT))
                                .setOrgPolicyReference(STRING_CONSTANT))
                .respondent1OrganisationPolicy(
                        new OrganisationPolicy()
                                .setOrganisation(
                                        new Organisation().setOrganisationID(STRING_CONSTANT))
                                .setOrgPolicyReference(STRING_CONSTANT))
                .generalAppType(
                        new GAApplicationType()
                                .setTypes(singletonList(VARY_PAYMENT_TERMS_OF_JUDGMENT)))
                .generalAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(YES))
                .generalAppPBADetails(new GAPbaDetails())
                .generalAppDetailsOfOrder(STRING_CONSTANT)
                .generalAppReasonsOfOrder(STRING_CONSTANT)
                .generalAppInformOtherParty(
                        new GAInformOtherParty()
                                .setIsWithNotice(YES)
                                .setReasonsForWithoutNotice(STRING_CONSTANT))
                .generalAppUrgencyRequirement(
                        new GAUrgencyRequirement()
                                .setGeneralAppUrgency(NO)
                                .setReasonsForUrgency(STRING_CONSTANT)
                                .setUrgentAppConsiderationDate(APP_DATE_EPOCH))
                .generalAppN245FormUpload(
                        new Document()
                                .setDocumentUrl(STRING_CONSTANT)
                                .setDocumentBinaryUrl(STRING_CONSTANT)
                                .setDocumentFileName(STRING_CONSTANT)
                                .setDocumentHash(STRING_CONSTANT))
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
                                .setHearingPreferredLocation(getPreferredLoc())
                                .setHearingDetailsTelephoneNumber(STRING_NUM_CONSTANT)
                                .setReasonForPreferredHearingType(STRING_CONSTANT)
                                .setTelephoneHearingPreferredType(STRING_CONSTANT)
                                .setSupportRequirementSignLanguage(STRING_CONSTANT)
                                .setHearingPreferencesPreferredType(IN_PERSON)
                                .setUnavailableTrialRequiredYesOrNo(YES)
                                .setSupportRequirementLanguageInterpreter(STRING_CONSTANT))
                .caseManagementLocation(caseManagementLoc)
                .build();
    }

    public final CaseDocument pdfDocument =
            new CaseDocument()
                    .setCreatedBy("John")
                    .setDocumentName("documentName")
                    .setDocumentSize(0L)
                    .setDocumentType(GENERAL_ORDER)
                    .setCreatedDatetime(now())
                    .setDocumentLink(
                            new Document()
                                    .setDocumentUrl("fake-url")
                                    .setDocumentFileName("file-name")
                                    .setDocumentBinaryUrl("binary-url"));

    public final Document pdfDocument1 =
            new Document()
                    .setDocumentUrl("fake-url")
                    .setDocumentFileName("file-name")
                    .setDocumentBinaryUrl("binary-url");
}
