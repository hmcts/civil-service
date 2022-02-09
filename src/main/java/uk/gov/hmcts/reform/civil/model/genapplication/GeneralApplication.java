package uk.gov.hmcts.reform.civil.model.genapplication;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import java.util.List;
@Data
@Builder(toBuilder = true)
public class GeneralApplication implements MappableObject {
    private final GATypeGAspec generalAppType;
    private final GARespondentOrderAgreement generalAppRespondentAgreement;
    private final IdamUserDetails applicantSolicitor1UserDetails;
    private final OrganisationPolicy applicant1OrganisationPolicy;
    private final OrganisationPolicy respondent1OrganisationPolicy;
    private final java.lang.String respondentSolicitor1EmailAddress;
    private final BusinessProcess businessProcess;
    private final GAPbaDetails generalAppPBADetails;
    private final java.lang.String generalAppDetailsOfOrder;
    private final java.lang.String generalAppReasonsOfOrder;
    private final GAInformOtherParty generalAppInformOtherParty;
    private final GAUrgencyRequirement generalAppUrgencyRequirement;
    private final GAStatementOfTruth generalAppStatementOfTruth;
    private final GAHearingDetails generalAppHearingDetails;
    private final List<Element<Document>> generalAppEvidenceDocument;
    private final YesOrNo isMultiParty;
    private final CaseLink caseLink;
    private final LocalDateTime generalAppSubmittedDateGAspec;
    @JsonCreator
    GeneralApplication(@JsonProperty("generalAppType") GATypeGAspec generalAppType,
                       @JsonProperty("generalAppRespondentAgreement")
                           GARespondentOrderAgreement generalAppRespondentAgreement,
                       @JsonProperty("applicantSolicitor1UserDetails") IdamUserDetails applicantSolicitor1UserDetails,
                       @JsonProperty("applicant1OrganisationPolicy") OrganisationPolicy applicant1OrganisationPolicy,
                       @JsonProperty("respondent1OrganisationPolicy") OrganisationPolicy respondent1OrganisationPolicy,
                       @JsonProperty("respondentSolicitor1EmailAddress") java.lang.String respondentSolicitor1EmailAddress,
                       @JsonProperty("businessProcess") BusinessProcess businessProcess,
                       @JsonProperty("generalAppPBADetails") GAPbaDetails generalAppPBADetails,
                       @JsonProperty("generalAppDetailsOfOrder") java.lang.String generalAppDetailsOfOrder,
                       @JsonProperty("generalAppReasonsOfOrder") java.lang.String generalAppReasonsOfOrder,
                       @JsonProperty("generalAppInformOtherParty") GAInformOtherParty generalAppInformOtherParty,
                       @JsonProperty("generalAppUrgencyRequirement") GAUrgencyRequirement generalAppUrgencyRequirement,
                       @JsonProperty("generalAppStatementOfTruth") GAStatementOfTruth generalAppStatementOfTruth,
                       @JsonProperty("generalAppHearingDetails") GAHearingDetails generalAppHearingDetails,
                       @JsonProperty("generalAppEvidenceDocument") List<Element<Document>> generalAppEvidenceDocument,
                       @JsonProperty("isMultiParty") YesOrNo isMultiParty,
                       @JsonProperty("caseLink") CaseLink caseLink,
                       @JsonProperty("generalAppSubmittedDateGAspec") LocalDateTime generalAppSubmittedDateGAspec) {
        this.generalAppType = generalAppType;
        this.generalAppRespondentAgreement = generalAppRespondentAgreement;
        this.applicantSolicitor1UserDetails = applicantSolicitor1UserDetails;
        this.applicant1OrganisationPolicy = applicant1OrganisationPolicy;
        this.respondent1OrganisationPolicy = respondent1OrganisationPolicy;
        this.respondentSolicitor1EmailAddress = respondentSolicitor1EmailAddress;
        this.businessProcess = businessProcess;
        this.generalAppPBADetails = generalAppPBADetails;
        this.generalAppDetailsOfOrder = generalAppDetailsOfOrder;
        this.generalAppReasonsOfOrder = generalAppReasonsOfOrder;
        this.generalAppInformOtherParty = generalAppInformOtherParty;
        this.generalAppUrgencyRequirement = generalAppUrgencyRequirement;
        this.generalAppStatementOfTruth = generalAppStatementOfTruth;
        this.generalAppHearingDetails = generalAppHearingDetails;
        this.generalAppEvidenceDocument = generalAppEvidenceDocument;
        this.isMultiParty = isMultiParty;
        this.caseLink = caseLink;
        this.generalAppSubmittedDateGAspec = generalAppSubmittedDateGAspec;
    }
}
