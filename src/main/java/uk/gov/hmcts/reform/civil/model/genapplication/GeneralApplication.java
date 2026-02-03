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

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@Accessors(chain = true)
public class GeneralApplication implements MappableObject {

    private String generalApplicationState;
    private GAApplicationType generalAppType;
    private GAApplicationTypeLR generalAppTypeLR;
    private GARespondentOrderAgreement generalAppRespondentAgreement;
    private BusinessProcess businessProcess;
    private GAPbaDetails generalAppPBADetails;
    private YesOrNo generalAppAskForCosts;
    private String generalAppDetailsOfOrder;
    private List<Element<String>> generalAppDetailsOfOrderColl;
    private String generalAppReasonsOfOrder;
    private List<Element<String>> generalAppReasonsOfOrderColl;
    private GAInformOtherParty generalAppInformOtherParty;
    private YesOrNo generalAppConsentOrder;
    private GAUrgencyRequirement generalAppUrgencyRequirement;
    private GAStatementOfTruth generalAppStatementOfTruth;
    private GAHearingDetails generalAppHearingDetails;
    private GASolicitorDetailsGAspec generalAppApplnSolicitor;
    private List<Element<GASolicitorDetailsGAspec>> generalAppRespondentSolicitors;
    private List<Element<GASolicitorDetailsGAspec>> generalAppApplicantAddlSolicitors;
    private List<Element<Document>> generalAppEvidenceDocument;
    private LocalDateTime generalAppDateDeadline;
    private YesOrNo isMultiParty;
    private YesOrNo isDocumentVisibleGA;
    private YesOrNo parentClaimantIsApplicant;
    private String gaApplicantDisplayName;
    private CaseLink caseLink;
    private LocalDateTime generalAppSubmittedDateGAspec;
    private IdamUserDetails civilServiceUserRoles;
    private String applicantPartyName;
    private String claimant1PartyName;
    private String claimant2PartyName;
    private String defendant1PartyName;
    private String defendant2PartyName;
    private String litigiousPartyID;
    private String generalAppSuperClaimType;
    private CaseLocationCivil caseManagementLocation;
    private YesOrNo isCcmccLocation;
    private GACaseManagementCategory caseManagementCategory;
    private CaseCategory caseAccessCategory;
    private String locationName;
    private LocalDateTime applicationClosedDate;
    private LocalDateTime applicationTakenOfflineDate;
    private YesOrNo generalAppVaryJudgementType;
    private Document generalAppN245FormUpload;
    private GAHearingDateGAspec generalAppHearingDate;
    private GeneralAppParentCaseLink generalAppParentCaseLink;
    private List<Element<GARespondentResponse>> respondentsResponses;
    // GA for LIP
    private YesOrNo isGaApplicantLip;
    private YesOrNo isGaRespondentOneLip;
    private YesOrNo isGaRespondentTwoLip;
    private HelpWithFees generalAppHelpWithFees;
    private CertOfSC certOfSC;
    //caseName
    private String caseNameGaInternal;
    //WA claim track description
    private String gaWaTrackLabel;
    private String emailPartyReference;
    //dates
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
            .setGeneralApplicationState(this.generalApplicationState)
            .setGeneralAppType(this.generalAppType)
            .setGeneralAppTypeLR(this.generalAppTypeLR)
            .setGeneralAppRespondentAgreement(this.generalAppRespondentAgreement)
            .setBusinessProcess(this.businessProcess)
            .setGeneralAppPBADetails(this.generalAppPBADetails)
            .setGeneralAppAskForCosts(this.generalAppAskForCosts)
            .setGeneralAppDetailsOfOrder(this.generalAppDetailsOfOrder)
            .setGeneralAppDetailsOfOrderColl(this.generalAppDetailsOfOrderColl)
            .setGeneralAppReasonsOfOrder(this.generalAppReasonsOfOrder)
            .setGeneralAppReasonsOfOrderColl(this.generalAppReasonsOfOrderColl)
            .setGeneralAppInformOtherParty(this.generalAppInformOtherParty)
            .setGeneralAppConsentOrder(this.generalAppConsentOrder)
            .setGeneralAppUrgencyRequirement(this.generalAppUrgencyRequirement)
            .setGeneralAppStatementOfTruth(this.generalAppStatementOfTruth)
            .setGeneralAppHearingDetails(this.generalAppHearingDetails)
            .setGeneralAppApplnSolicitor(this.generalAppApplnSolicitor)
            .setGeneralAppRespondentSolicitors(this.generalAppRespondentSolicitors)
            .setGeneralAppApplicantAddlSolicitors(this.generalAppApplicantAddlSolicitors)
            .setGeneralAppEvidenceDocument(this.generalAppEvidenceDocument)
            .setGeneralAppDateDeadline(this.generalAppDateDeadline)
            .setIsMultiParty(this.isMultiParty)
            .setIsDocumentVisibleGA(this.isDocumentVisibleGA)
            .setParentClaimantIsApplicant(this.parentClaimantIsApplicant)
            .setGaApplicantDisplayName(this.gaApplicantDisplayName)
            .setCaseLink(this.caseLink)
            .setGeneralAppSubmittedDateGAspec(this.generalAppSubmittedDateGAspec)
            .setCivilServiceUserRoles(this.civilServiceUserRoles)
            .setApplicantPartyName(this.applicantPartyName)
            .setClaimant1PartyName(this.claimant1PartyName)
            .setClaimant2PartyName(this.claimant2PartyName)
            .setDefendant1PartyName(this.defendant1PartyName)
            .setDefendant2PartyName(this.defendant2PartyName)
            .setLitigiousPartyID(this.litigiousPartyID)
            .setGeneralAppSuperClaimType(this.generalAppSuperClaimType)
            .setCaseManagementLocation(this.caseManagementLocation)
            .setIsCcmccLocation(this.isCcmccLocation)
            .setCaseManagementCategory(this.caseManagementCategory)
            .setCaseAccessCategory(this.caseAccessCategory)
            .setLocationName(this.locationName)
            .setApplicationClosedDate(this.applicationClosedDate)
            .setApplicationTakenOfflineDate(this.applicationTakenOfflineDate)
            .setGeneralAppVaryJudgementType(this.generalAppVaryJudgementType)
            .setGeneralAppN245FormUpload(this.generalAppN245FormUpload)
            .setGeneralAppHearingDate(this.generalAppHearingDate)
            .setGeneralAppParentCaseLink(this.generalAppParentCaseLink)
            .setRespondentsResponses(this.respondentsResponses)
            .setIsGaApplicantLip(this.isGaApplicantLip)
            .setIsGaRespondentOneLip(this.isGaRespondentOneLip)
            .setIsGaRespondentTwoLip(this.isGaRespondentTwoLip)
            .setGeneralAppHelpWithFees(this.generalAppHelpWithFees)
            .setCertOfSC(this.certOfSC)
            .setCaseNameGaInternal(this.caseNameGaInternal)
            .setGaWaTrackLabel(this.gaWaTrackLabel)
            .setEmailPartyReference(this.emailPartyReference)
            .setMainCaseSubmittedDate(this.mainCaseSubmittedDate);
    }
}
