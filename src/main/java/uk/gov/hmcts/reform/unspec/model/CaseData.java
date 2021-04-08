package uk.gov.hmcts.reform.unspec.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.unspec.enums.AllocatedTrack;
import uk.gov.hmcts.reform.unspec.enums.CaseState;
import uk.gov.hmcts.reform.unspec.enums.ClaimType;
import uk.gov.hmcts.reform.unspec.enums.PersonalInjuryType;
import uk.gov.hmcts.reform.unspec.enums.RespondentResponseType;
import uk.gov.hmcts.reform.unspec.enums.ResponseIntention;
import uk.gov.hmcts.reform.unspec.enums.YesOrNo;
import uk.gov.hmcts.reform.unspec.model.common.DynamicList;
import uk.gov.hmcts.reform.unspec.model.common.Element;
import uk.gov.hmcts.reform.unspec.model.common.MappableObject;
import uk.gov.hmcts.reform.unspec.model.documents.CaseDocument;
import uk.gov.hmcts.reform.unspec.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.unspec.model.dq.Respondent1DQ;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.Valid;

import static uk.gov.hmcts.reform.unspec.enums.BusinessProcessStatus.FINISHED;

@Data
@Builder(toBuilder = true)
public class CaseData implements MappableObject {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private final Long ccdCaseReference;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private final CaseState ccdState;
    private final SolicitorReferences solicitorReferences;
    private final CourtLocation courtLocation;
    private final Party applicant1;
    private final Party applicant2;
    private final CorrectEmail applicantSolicitor1CheckEmail;
    private final IdamUserDetails applicantSolicitor1UserDetails;
    private final Party respondent1;
    private final Party respondent2;
    private final YesOrNo respondent1Represented;
    private final YesOrNo respondent1OrgRegistered;
    private final String respondentSolicitor1EmailAddress;
    private final String detailsOfClaim;
    private final ClaimValue claimValue;
    private final Fee claimFee;
    private final String paymentReference;
    private final DynamicList applicantSolicitor1PbaAccounts;
    private final ClaimType claimType;
    private final String claimTypeOther;
    private final PersonalInjuryType personalInjuryType;
    private final String personalInjuryTypeOther;
    private final StatementOfTruth applicantSolicitor1ClaimStatementOfTruth;
    private final String legacyCaseReference;
    private final AllocatedTrack allocatedTrack;
    private final PaymentDetails paymentDetails;

    private final OrganisationPolicy applicant1OrganisationPolicy;
    private final OrganisationPolicy applicant2OrganisationPolicy;
    private final OrganisationPolicy respondent1OrganisationPolicy;
    private final OrganisationPolicy respondent2OrganisationPolicy;
    private final SolicitorOrganisationDetails respondentSolicitor1OrganisationDetails;
    private final StatementOfTruth applicant1ServiceStatementOfTruthToRespondentSolicitor1;
    private final List<Element<CaseDocument>> systemGeneratedCaseDocuments;

    private final LocalDate respondentSolicitor1AgreedDeadlineExtension;
    private final ResponseIntention respondent1ClaimResponseIntentionType;
    private final ServedDocumentFiles servedDocumentFiles;

    private final RespondentResponseType respondent1ClaimResponseType;
    private final ResponseDocument respondent1ClaimResponseDocument;

    private final YesOrNo applicant1ProceedWithClaim;
    private final ResponseDocument applicant1DefenceResponseDocument;

    @Valid
    private final CloseClaim withdrawClaim;

    @Valid
    private final CloseClaim discontinueClaim;

    private final BusinessProcess businessProcess;

    @JsonUnwrapped
    private final Respondent1DQ respondent1DQ;

    @JsonUnwrapped
    private final Applicant1DQ applicant1DQ;

    public boolean hasNoOngoingBusinessProcess() {
        return businessProcess == null
            || businessProcess.getStatus() == null
            || businessProcess.getStatus() == FINISHED;
    }

    private final LitigationFriend respondent1LitigationFriend;

    private final YesOrNo applicant1LitigationFriendRequired;

    private final LitigationFriend applicant1LitigationFriend;

    @Valid
    private final ClaimProceedsInCaseman claimProceedsInCaseman;

    //CCD UI flag
    private final YesOrNo applicantSolicitor1PbaAccountsIsEmpty;

    // dates
    private final LocalDateTime submittedDate;
    private final LocalDateTime paymentSuccessfulDate;
    private final LocalDate issueDate;
    private final LocalDateTime claimNotificationDeadline;
    private final LocalDateTime claimNotificationDate;
    private final LocalDateTime claimDetailsNotificationDeadline;
    private final LocalDateTime claimDetailsNotificationDate;
    private final LocalDateTime respondent1ResponseDeadline;
    private final LocalDateTime claimDismissedDeadline;
    private final LocalDateTime respondent1TimeExtensionDate;
    private final LocalDateTime respondent1AcknowledgeNotificationDate;
    private final LocalDateTime respondent1ResponseDate;
    private final LocalDateTime applicant1ResponseDeadline;
    private final LocalDateTime applicant1ResponseDate;
    private final LocalDateTime takenOfflineDate;
    private final LocalDateTime claimDismissedDate;
}
