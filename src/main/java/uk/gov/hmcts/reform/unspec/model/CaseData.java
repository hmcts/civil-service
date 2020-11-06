package uk.gov.hmcts.reform.unspec.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.unspec.enums.AllocatedTrack;
import uk.gov.hmcts.reform.unspec.enums.CaseState;
import uk.gov.hmcts.reform.unspec.enums.ClaimType;
import uk.gov.hmcts.reform.unspec.enums.DefendantResponseType;
import uk.gov.hmcts.reform.unspec.enums.PbaNumber;
import uk.gov.hmcts.reform.unspec.enums.PersonalInjuryType;
import uk.gov.hmcts.reform.unspec.enums.ResponseIntention;
import uk.gov.hmcts.reform.unspec.enums.ServedDocuments;
import uk.gov.hmcts.reform.unspec.enums.YesOrNo;
import uk.gov.hmcts.reform.unspec.model.common.Element;
import uk.gov.hmcts.reform.unspec.model.documents.CaseDocument;
import uk.gov.hmcts.reform.unspec.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.unspec.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.unspec.validation.groups.ConfirmServiceDateGroup;
import uk.gov.hmcts.reform.unspec.validation.interfaces.HasDeemedDateOfServiceTheSameAsOrAfterIssueDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.PastOrPresent;

import static uk.gov.hmcts.reform.unspec.enums.BusinessProcessStatus.FINISHED;

@Data
@Builder(toBuilder = true)
@HasDeemedDateOfServiceTheSameAsOrAfterIssueDate(groups = ConfirmServiceDateGroup.class)
public class CaseData {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private final Long ccdCaseReference;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private final CaseState ccdState;
    private final SolicitorReferences solicitorReferences;
    private final CourtLocation courtLocation;
    private final Party applicant1;
    private final Party applicant2;
    private final Party respondent1;
    private final Party respondent2;
    private final ClaimValue claimValue;
    private final PbaNumber pbaNumber;
    private final ClaimType claimType;
    private final String claimTypeOther;
    private final PersonalInjuryType personalInjuryType;
    private final String personalInjuryTypeOther;
    private final StatementOfTruth applicantSolicitor1ClaimStatementOfTruth;
    private final LocalDateTime claimSubmittedDateTime;
    private final LocalDate claimIssuedDate;
    private LocalDateTime confirmationOfServiceDeadline;
    private final String legacyCaseReference;
    private final AllocatedTrack allocatedTrack;

    private final StatementOfTruth applicant1ServiceStatementOfTruthToRespondentSolicitor1;
    private final List<Element<CaseDocument>> systemGeneratedCaseDocuments;
    private final ServiceMethod serviceMethodToRespondentSolicitor1;

    @PastOrPresent(message = "The date must not be in the future", groups = ConfirmServiceDateGroup.class)
    private final LocalDate serviceDateToRespondentSolicitor1;

    @PastOrPresent(message = "The date must not be in the future", groups = ConfirmServiceDateGroup.class)
    private final LocalDateTime serviceDateTimeToRespondentSolicitor1;

    private final LocalDate deemedServiceDateToRespondentSolicitor1;
    private final LocalDateTime respondentSolicitor1ResponseDeadline;
    private final List<ServedDocuments> servedDocuments;
    private final ServiceLocation serviceLocationToRespondentSolicitor1;
    private final ServedDocumentFiles servedDocumentFiles;
    private final String servedDocumentsOther;
    private final ResponseIntention respondent1ClaimResponseIntentionType;

    private final LocalDate respondentSolicitor1claimResponseExtensionProposedDeadline;
    private final YesOrNo respondentSolicitor1claimResponseExtensionAlreadyAgreed;
    private final String respondentSolicitor1claimResponseExtensionReason;

    private final YesOrNo respondentSolicitor1claimResponseExtensionAccepted;
    private final YesOrNo respondentSolicitor1claimResponseExtensionCounter;
    private final LocalDate respondentSolicitor1claimResponseExtensionCounterDate;
    private final String respondentSolicitor1claimResponseExtensionRejectionReason;

    private final DefendantResponseType respondent1ClaimResponseType;
    private final ResponseDocument respondent1ClaimResponseDocument;
    private final LocalDateTime applicantSolicitorResponseDeadlineToRespondentSolicitor1;

    private final YesOrNo applicant1ProceedWithClaim;
    private final ResponseDocument applicant1DefenceResponseDocument;
    private final ApplicantNotProceedingReason applicant1NotProceedingReason;

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
}
