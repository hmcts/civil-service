package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GARespondentResponse;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.CertOfSC;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.time.LocalDateTime;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.civil.model.GAN245FormUpload;
import uk.gov.hmcts.reform.civil.model.GAUserDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.GACaseLocationGAspec;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "GeneralApplicationGAspec", generate = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@Accessors(chain = true)
public class GeneralApplication implements MappableObject {

    @CCD(ignore = true)
    private String generalApplicationState;
    @CCD(label = " ", searchable = false)
    private GAApplicationType generalAppType;
    @CCD(label = " ", searchable = false)
    private GAApplicationTypeLR generalAppTypeLR;
    @CCD(label = " ", searchable = false)
    private GARespondentOrderAgreement generalAppRespondentAgreement;
    @CCD(label = " ")
    private BusinessProcess businessProcess;
    @CCD(label = " ", searchable = false)
    private GAPbaDetails generalAppPBADetails;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo generalAppAskForCosts;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String generalAppDetailsOfOrder;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.Collection, typeParameterOverride = "TextArea")
    private List<Element<String>> generalAppDetailsOfOrderColl;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private String generalAppReasonsOfOrder;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.Collection, typeParameterOverride = "TextArea")
    private List<Element<String>> generalAppReasonsOfOrderColl;
    @CCD(label = " ", searchable = false)
    private GAInformOtherParty generalAppInformOtherParty;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo generalAppConsentOrder;
    @CCD(label = " ", searchable = false)
    private GAUrgencyRequirement generalAppUrgencyRequirement;
    @CCD(label = " ", searchable = false)
    private GAStatementOfTruth generalAppStatementOfTruth;
    @CCD(label = " ", searchable = false)
    private GAHearingDetails generalAppHearingDetails;
    @CCD(label = " ", searchable = false)
    private GASolicitorDetailsGAspec generalAppApplnSolicitor;
    @CCD(label = " ", searchable = false)
    private List<Element<GASolicitorDetailsGAspec>> generalAppRespondentSolicitors;
    @CCD(label = " ", searchable = false)
    private List<Element<GASolicitorDetailsGAspec>> generalAppApplicantAddlSolicitors;
    @CCD(
            label = "Upload evidence",
            regex = ".pdf,.txt,.doc,.dot,.docx,.rtf,.xls,.xlt,.xla,.xlsx,.xltx,.xlsb,.ppt,.pot,.pps,.ppa,.pptx,.potx,.ppsx,.jpg,.jpeg,.bmp,.tif,.tiff,.png,.csv",
            categoryID = "applications",
            searchable = false
    )
    private List<Element<Document>> generalAppEvidenceDocument;
    @CCD(label = " ", searchable = false)
    private LocalDateTime generalAppDateDeadline;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo isMultiParty;
    @CCD(ignore = true)
    private YesOrNo isDocumentVisibleGA;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo parentClaimantIsApplicant;
    @CCD(label = " ", showCondition = "applicationTakenOfflineDate = \"DO_NOT_SHOW_IN_UI\"", searchable = false)
    private String gaApplicantDisplayName;
    @CCD(label = " ", searchable = false)
    private CaseLink caseLink;
    @CCD(label = " ", searchable = false)
    private LocalDateTime generalAppSubmittedDateGAspec;
    @CCD(label = " ", searchable = false, typeParameterClass = GAUserDetailsGAspec.class)
    private IdamUserDetails civilServiceUserRoles;
    @CCD(label = " ", searchable = false)
    private String applicantPartyName;
    @CCD(label = " ", searchable = false)
    private String claimant1PartyName;
    @CCD(label = " ", searchable = false)
    private String claimant2PartyName;
    @CCD(label = " ", searchable = false)
    private String defendant1PartyName;
    @CCD(label = " ", searchable = false)
    private String defendant2PartyName;
    @CCD(label = " ", searchable = false)
    private String litigiousPartyID;
    @CCD(label = " ", searchable = false)
    private String generalAppSuperClaimType;
    @CCD(label = " ", searchable = false, typeParameterClass = GACaseLocationGAspec.class)
    private CaseLocationCivil caseManagementLocation;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo isCcmccLocation;
    @CCD(label = " ", searchable = false)
    private GACaseManagementCategory caseManagementCategory;
    @CCD(ignore = true)
    private CaseCategory caseAccessCategory;
    @CCD(label = " ", searchable = false)
    private String locationName;
    @CCD(label = "Application Closed date", searchable = false)
    private LocalDateTime applicationClosedDate;
    @CCD(label = "Application Closed date", searchable = false)
    private LocalDateTime applicationTakenOfflineDate;
    @CCD(label = "Vary Judgement GA Type", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo generalAppVaryJudgementType;
    @CCD(label = "Upload your completed N245 form", categoryID = "applications", searchable = false)
    private Document generalAppN245FormUpload;
    @CCD(label = "Hearing date for all GA types", searchable = false)
    private GAHearingDateGAspec generalAppHearingDate;
    @CCD(ignore = true)
    private GeneralAppParentCaseLink generalAppParentCaseLink;
    @CCD(ignore = true)
    private List<Element<GARespondentResponse>> respondentsResponses;
    // GA for LIP
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo isGaApplicantLip;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo isGaRespondentOneLip;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo isGaRespondentTwoLip;
    @CCD(label = " ", searchable = false)
    private HelpWithFees generalAppHelpWithFees;
    @CCD(label = " ", searchable = false)
    private CertOfSC certOfSC;
    //caseName
    @CCD(label = " ", searchable = false)
    private String caseNameGaInternal;
    //WA claim track description
    @CCD(label = " ", searchable = false)
    private String gaWaTrackLabel;
    @CCD(label = " ", searchable = false)
    private String emailPartyReference;
    //dates
    @CCD(label = " ", searchable = false)
    private LocalDateTime mainCaseSubmittedDate;

    @JsonCreator
    GeneralApplication(@JsonProperty("generalApplicationState") String generalApplicationState,
                       @JsonProperty("generalAppType") GAApplicationType generalAppType,
                       @JsonProperty("generalAppTypeLR") GAApplicationTypeLR generalAppTypeLR,
                       @JsonProperty("generalAppRespondentAgreement")
                           GARespondentOrderAgreement generalAppRespondentAgreement,
                       @JsonProperty("businessProcess") BusinessProcess businessProcess,
                       @JsonProperty("generalAppPBADetails") GAPbaDetails generalAppPBADetails,
                       @JsonProperty("generalAppAskForCosts") YesOrNo generalAppAskForCosts,
                       @JsonProperty("generalAppDetailsOfOrder") String generalAppDetailsOfOrder,
                       @JsonProperty("generalAppDetailsOfOrderColl") List<Element<String>> generalAppDetailsOfOrderColl,
                       @JsonProperty("generalAppReasonsOfOrder") String generalAppReasonsOfOrder,
                       @JsonProperty("generalAppReasonsOfOrderColl") List<Element<String>> generalAppReasonsOfOrderColl,
                       @JsonProperty("generalAppInformOtherParty") GAInformOtherParty generalAppInformOtherParty,
                       @JsonProperty("generalAppConsentOrder") YesOrNo generalAppConsentOrder,
                       @JsonProperty("generalAppUrgencyRequirement") GAUrgencyRequirement generalAppUrgencyRequirement,
                       @JsonProperty("generalAppStatementOfTruth") GAStatementOfTruth generalAppStatementOfTruth,
                       @JsonProperty("generalAppHearingDetails") GAHearingDetails generalAppHearingDetails,
                       @JsonProperty("generalAppApplnSolicitor") GASolicitorDetailsGAspec generalAppApplnSolicitor,
                       @JsonProperty("generalAppRespondentSolicitors") List<Element<GASolicitorDetailsGAspec>>
                           generalAppRespondentSolicitors,
                       @JsonProperty("generalAppApplicantAddlSolicitors") List<Element<GASolicitorDetailsGAspec>>
                           generalAppApplicantAddlSolicitors,
                       @JsonProperty("generalAppEvidenceDocument") List<Element<Document>> generalAppEvidenceDocument,
                       @JsonProperty("generalAppDateDeadline") LocalDateTime generalAppDateDeadline,
                       @JsonProperty("isMultiParty") YesOrNo isMultiParty,
                       @JsonProperty("isDocumentVisibleGA") YesOrNo isDocumentVisibleGA,
                       @JsonProperty("parentClaimantIsApplicant") YesOrNo parentClaimantIsApplicant,
                       @JsonProperty("gaApplicantDisplayName") String gaApplicantDisplayName,
                       @JsonProperty("caseLink") CaseLink caseLink,
                       @JsonProperty("generalAppSubmittedDateGAspec") LocalDateTime generalAppSubmittedDateGAspec,
                       @JsonProperty("civilServiceUserRoles") IdamUserDetails civilServiceUserRoles,
                       @JsonProperty("applicantPartyName") String applicantPartyName,
                       @JsonProperty("claimant1PartyName") String claimant1PartyName,
                       @JsonProperty("claimant2PartyName") String claimant2PartyName,
                       @JsonProperty("defendant1PartyName") String defendant1PartyName,
                       @JsonProperty("defendant2PartyName") String defendant2PartyName,
                       @JsonProperty("litigiousPartyID") String litigiousPartyID,
                       @JsonProperty("generalAppSuperClaimType") String generalAppSuperClaimType,
                       @JsonProperty("caseManagementLocation") CaseLocationCivil caseManagementLocation,
                       @JsonProperty("isCcmccLocation") YesOrNo isCcmccLocation,
                       @JsonProperty("caseManagementCategory") GACaseManagementCategory caseManagementCategory,
                       @JsonProperty("CaseAccessCategory") CaseCategory caseAccessCategory,
                       @JsonProperty("locationName") String locationName,
                       @JsonProperty("applicationClosedDate") LocalDateTime applicationClosedDate,
                       @JsonProperty("applicationTakenOfflineDate") LocalDateTime applicationTakenOfflineDate,
                       @JsonProperty("generalAppVaryJudgementType") YesOrNo generalAppVaryJudgementType,
                       @JsonProperty("generalAppN245FormUpload") Document generalAppN245FormUpload,
                       @JsonProperty("generalAppHearingDate") GAHearingDateGAspec generalAppHearingDate,
                       @JsonProperty("generalAppParentCaseLink") GeneralAppParentCaseLink generalAppParentCaseLink,
                       @JsonProperty("respondentsResponses") List<Element<GARespondentResponse>> respondentsResponses,
                       @JsonProperty("isGaApplicantLip") YesOrNo isGaApplicantLip,
                       @JsonProperty("isGaRespondentOneLip") YesOrNo isGaRespondentOneLip,
                       @JsonProperty("isGaRespondentTwoLip") YesOrNo isGaRespondentTwoLip,
                       @JsonProperty("generalAppHelpWithFees") HelpWithFees generalAppHelpWithFees,
                       @JsonProperty("certOfSC") CertOfSC certOfSC,
                       @JsonProperty("caseNameGaInternal") String caseNameGaInternal,
                       @JsonProperty("gaWaTrackLabel") String gaWaTrackLabel,
                       @JsonProperty("emailPartyReference") String emailPartyReference,
                       @JsonProperty("mainCaseSubmittedDate") LocalDateTime mainCaseSubmittedDate) {

        this.generalApplicationState = generalApplicationState;
        this.generalAppType = generalAppType;
        this.generalAppRespondentAgreement = generalAppRespondentAgreement;
        this.businessProcess = businessProcess;
        this.generalAppPBADetails = generalAppPBADetails;
        this.generalAppAskForCosts = generalAppAskForCosts;
        this.generalAppDetailsOfOrder = generalAppDetailsOfOrder;
        this.generalAppDetailsOfOrderColl = generalAppDetailsOfOrderColl;
        this.generalAppReasonsOfOrder = generalAppReasonsOfOrder;
        this.generalAppReasonsOfOrderColl = generalAppReasonsOfOrderColl;
        this.generalAppInformOtherParty = generalAppInformOtherParty;
        this.generalAppConsentOrder = generalAppConsentOrder;
        this.generalAppUrgencyRequirement = generalAppUrgencyRequirement;
        this.generalAppStatementOfTruth = generalAppStatementOfTruth;
        this.generalAppHearingDetails = generalAppHearingDetails;
        this.generalAppApplnSolicitor = generalAppApplnSolicitor;
        this.generalAppRespondentSolicitors = generalAppRespondentSolicitors;
        this.generalAppApplicantAddlSolicitors = generalAppApplicantAddlSolicitors;
        this.generalAppEvidenceDocument = generalAppEvidenceDocument;
        this.generalAppDateDeadline = generalAppDateDeadline;
        this.isMultiParty = isMultiParty;
        this.isDocumentVisibleGA = isDocumentVisibleGA;
        this.parentClaimantIsApplicant = parentClaimantIsApplicant;
        this.gaApplicantDisplayName = gaApplicantDisplayName;
        this.caseLink = caseLink;
        this.generalAppSubmittedDateGAspec = generalAppSubmittedDateGAspec;
        this.civilServiceUserRoles = civilServiceUserRoles;
        this.applicantPartyName = applicantPartyName;
        this.claimant1PartyName = claimant1PartyName;
        this.claimant2PartyName = claimant2PartyName;
        this.defendant1PartyName = defendant1PartyName;
        this.defendant2PartyName = defendant2PartyName;
        this.litigiousPartyID = litigiousPartyID;
        this.generalAppSuperClaimType = generalAppSuperClaimType;
        this.caseManagementLocation = caseManagementLocation;
        this.isCcmccLocation = isCcmccLocation;
        this.caseManagementCategory = caseManagementCategory;
        this.caseAccessCategory = caseAccessCategory;
        this.locationName = locationName;
        this.applicationClosedDate = applicationClosedDate;
        this.applicationTakenOfflineDate = applicationTakenOfflineDate;
        this.generalAppVaryJudgementType = generalAppVaryJudgementType;
        this.generalAppN245FormUpload = generalAppN245FormUpload;
        this.generalAppHearingDate = generalAppHearingDate;
        this.generalAppParentCaseLink = generalAppParentCaseLink;
        this.respondentsResponses = respondentsResponses;
        this.isGaApplicantLip = isGaApplicantLip;
        this.isGaRespondentOneLip = isGaRespondentOneLip;
        this.isGaRespondentTwoLip = isGaRespondentTwoLip;
        this.generalAppHelpWithFees = generalAppHelpWithFees;
        this.certOfSC = certOfSC;
        this.generalAppTypeLR = generalAppTypeLR;
        this.caseNameGaInternal = caseNameGaInternal;
        this.gaWaTrackLabel = gaWaTrackLabel;
        this.emailPartyReference = emailPartyReference;
        this.mainCaseSubmittedDate = mainCaseSubmittedDate;
    }

    public GeneralApplication copy() {
        return new GeneralApplication()
            .setGeneralApplicationState(generalApplicationState)
            .setGeneralAppType(generalAppType)
            .setGeneralAppTypeLR(generalAppTypeLR)
            .setGeneralAppRespondentAgreement(generalAppRespondentAgreement)
            .setBusinessProcess(businessProcess)
            .setGeneralAppPBADetails(generalAppPBADetails)
            .setGeneralAppAskForCosts(generalAppAskForCosts)
            .setGeneralAppDetailsOfOrder(generalAppDetailsOfOrder)
            .setGeneralAppDetailsOfOrderColl(generalAppDetailsOfOrderColl)
            .setGeneralAppReasonsOfOrder(generalAppReasonsOfOrder)
            .setGeneralAppReasonsOfOrderColl(generalAppReasonsOfOrderColl)
            .setGeneralAppInformOtherParty(generalAppInformOtherParty)
            .setGeneralAppConsentOrder(generalAppConsentOrder)
            .setGeneralAppUrgencyRequirement(generalAppUrgencyRequirement)
            .setGeneralAppStatementOfTruth(generalAppStatementOfTruth)
            .setGeneralAppHearingDetails(generalAppHearingDetails)
            .setGeneralAppApplnSolicitor(generalAppApplnSolicitor)
            .setGeneralAppRespondentSolicitors(generalAppRespondentSolicitors)
            .setGeneralAppApplicantAddlSolicitors(generalAppApplicantAddlSolicitors)
            .setGeneralAppEvidenceDocument(generalAppEvidenceDocument)
            .setGeneralAppDateDeadline(generalAppDateDeadline)
            .setIsMultiParty(isMultiParty)
            .setIsDocumentVisibleGA(isDocumentVisibleGA)
            .setParentClaimantIsApplicant(parentClaimantIsApplicant)
            .setGaApplicantDisplayName(gaApplicantDisplayName)
            .setCaseLink(caseLink)
            .setGeneralAppSubmittedDateGAspec(generalAppSubmittedDateGAspec)
            .setCivilServiceUserRoles(civilServiceUserRoles)
            .setApplicantPartyName(applicantPartyName)
            .setClaimant1PartyName(claimant1PartyName)
            .setClaimant2PartyName(claimant2PartyName)
            .setDefendant1PartyName(defendant1PartyName)
            .setDefendant2PartyName(defendant2PartyName)
            .setLitigiousPartyID(litigiousPartyID)
            .setGeneralAppSuperClaimType(generalAppSuperClaimType)
            .setCaseManagementLocation(caseManagementLocation)
            .setIsCcmccLocation(isCcmccLocation)
            .setCaseManagementCategory(caseManagementCategory)
            .setCaseAccessCategory(caseAccessCategory)
            .setLocationName(locationName)
            .setApplicationClosedDate(applicationClosedDate)
            .setApplicationTakenOfflineDate(applicationTakenOfflineDate)
            .setGeneralAppVaryJudgementType(generalAppVaryJudgementType)
            .setGeneralAppN245FormUpload(generalAppN245FormUpload)
            .setGeneralAppHearingDate(generalAppHearingDate)
            .setGeneralAppParentCaseLink(generalAppParentCaseLink)
            .setRespondentsResponses(respondentsResponses)
            .setIsGaApplicantLip(isGaApplicantLip)
            .setIsGaRespondentOneLip(isGaRespondentOneLip)
            .setIsGaRespondentTwoLip(isGaRespondentTwoLip)
            .setGeneralAppHelpWithFees(generalAppHelpWithFees)
            .setCertOfSC(certOfSC)
            .setCaseNameGaInternal(caseNameGaInternal)
            .setGaWaTrackLabel(gaWaTrackLabel)
            .setEmailPartyReference(emailPartyReference)
            .setMainCaseSubmittedDate(mainCaseSubmittedDate);
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Upload your completed N245 form Label", searchable = false)
  private GAN245FormUpload gaUploadN245FormUploadLabel;
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Email)
  private String respondentSolicitor1EmailAddress;
  // ==== end synthesised definition-only fields ====
}
