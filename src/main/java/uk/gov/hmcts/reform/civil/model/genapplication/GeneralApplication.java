package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeneralApplication implements MappableObject {

    private final String generalApplicationState;
    private final GAApplicationType generalAppType;
    private final GARespondentOrderAgreement generalAppRespondentAgreement;
    private final BusinessProcess businessProcess;
    private final GAPbaDetails generalAppPBADetails;
    private final String generalAppDetailsOfOrder;
    private final String generalAppReasonsOfOrder;
    private final GAInformOtherParty generalAppInformOtherParty;
    private final YesOrNo generalAppConsentOrder;
    private final GAUrgencyRequirement generalAppUrgencyRequirement;
    private final GAStatementOfTruth generalAppStatementOfTruth;
    private final GAHearingDetails generalAppHearingDetails;
    private final GASolicitorDetailsGAspec generalAppApplnSolicitor;
    private final List<Element<GASolicitorDetailsGAspec>> generalAppRespondentSolicitors;
    private final List<Element<Document>> generalAppEvidenceDocument;
    private final LocalDateTime generalAppDateDeadline;
    private final YesOrNo isMultiParty;
    private final YesOrNo parentClaimantIsApplicant;
    private final String gaApplicantDisplayName;
    private final CaseLink caseLink;
    private final LocalDateTime generalAppSubmittedDateGAspec;
    private final IdamUserDetails civilServiceUserRoles;
    private final String applicantPartyName;
    private final String claimant1PartyName;
    private final String claimant2PartyName;
    private final String defendant1PartyName;
    private final String defendant2PartyName;
    private final String generalAppSuperClaimType;
    private final CaseLocationCivil caseManagementLocation;
    private final YesOrNo isCcmccLocation;
    private final GACaseManagementCategory caseManagementCategory;
    private final CaseCategory caseAccessCategory;
    private final String locationName;
    private final LocalDateTime applicationClosedDate;
    private final LocalDateTime applicationTakenOfflineDate;
    private final YesOrNo generalAppVaryJudgementType;
    private final Document generalAppN245FormUpload;
    private final GAHearingDateGAspec generalAppHearingDate;

    @JsonCreator
    GeneralApplication(@JsonProperty("generalApplicationState") String generalApplicationState,
                       @JsonProperty("generalAppType") GAApplicationType generalAppType,
                       @JsonProperty("generalAppRespondentAgreement")
                           GARespondentOrderAgreement generalAppRespondentAgreement,
                       @JsonProperty("businessProcess") BusinessProcess businessProcess,
                       @JsonProperty("generalAppPBADetails") GAPbaDetails generalAppPBADetails,
                       @JsonProperty("generalAppDetailsOfOrder") String generalAppDetailsOfOrder,
                       @JsonProperty("generalAppReasonsOfOrder") String generalAppReasonsOfOrder,
                       @JsonProperty("generalAppInformOtherParty") GAInformOtherParty generalAppInformOtherParty,
                       @JsonProperty("generalAppConsentOrder") YesOrNo generalAppConsentOrder,
                       @JsonProperty("generalAppUrgencyRequirement") GAUrgencyRequirement generalAppUrgencyRequirement,
                       @JsonProperty("generalAppStatementOfTruth") GAStatementOfTruth generalAppStatementOfTruth,
                       @JsonProperty("generalAppHearingDetails") GAHearingDetails generalAppHearingDetails,
                       @JsonProperty("generalAppApplnSolicitor") GASolicitorDetailsGAspec generalAppApplnSolicitor,
                       @JsonProperty("generalAppRespondentSolicitors") List<Element<GASolicitorDetailsGAspec>>
                           generalAppRespondentSolicitors,
                       @JsonProperty("generalAppEvidenceDocument") List<Element<Document>> generalAppEvidenceDocument,
                       @JsonProperty("generalAppDateDeadline") LocalDateTime generalAppDateDeadline,
                       @JsonProperty("isMultiParty") YesOrNo isMultiParty,
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
                       @JsonProperty("generalAppHearingDate") GAHearingDateGAspec generalAppHearingDate) {

        this.generalApplicationState = generalApplicationState;
        this.generalAppType = generalAppType;
        this.generalAppRespondentAgreement = generalAppRespondentAgreement;
        this.businessProcess = businessProcess;
        this.generalAppPBADetails = generalAppPBADetails;
        this.generalAppDetailsOfOrder = generalAppDetailsOfOrder;
        this.generalAppReasonsOfOrder = generalAppReasonsOfOrder;
        this.generalAppInformOtherParty = generalAppInformOtherParty;
        this.generalAppConsentOrder = generalAppConsentOrder;
        this.generalAppUrgencyRequirement = generalAppUrgencyRequirement;
        this.generalAppStatementOfTruth = generalAppStatementOfTruth;
        this.generalAppHearingDetails = generalAppHearingDetails;
        this.generalAppApplnSolicitor = generalAppApplnSolicitor;
        this.generalAppRespondentSolicitors = generalAppRespondentSolicitors;
        this.generalAppEvidenceDocument = generalAppEvidenceDocument;
        this.generalAppDateDeadline = generalAppDateDeadline;
        this.isMultiParty = isMultiParty;
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
    }
}
