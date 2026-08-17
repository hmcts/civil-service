package uk.gov.hmcts.reform.civil.model.citizenui;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.civil.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CitizenProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.JudgeProfileRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorCuCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILECuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CitizenProfileCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.WluAdminRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECuPlus2RolesSzdyetAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCrudCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILERESSOLONESPECPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.WluAdminCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILECaseworkerCivilSystemupdateCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILERESSOLTWOSPECPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruCaseworkerCivilSystemupdateRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CitizenProfileCuiAdminProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRCitizenProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CuiNbcProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.HearingScheduleAccessCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.JudgeProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILERPlus3RolesMtlnhzAccess;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaseDataLiP {

    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class, CITIZENDEFENDANTPROFILECruAccess.class, CitizenProfileCruAccess.class, JudgeProfileRAccess.class}
    )
    @JsonProperty("respondent1LiPResponse")
    private RespondentLiPResponse respondent1LiPResponse;
    @CCD(
            label = "Defendant 1 Mediation",
            searchable = false,
            access = {CaseworkerCivilSolicitorCuCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENDEFENDANTPROFILECuAccess.class, CitizenProfileCuAccess.class}
    )
    @JsonProperty("respondent1LiPResponseCarm")
    private MediationLiPCarm respondent1MediationLiPResponseCarm;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class, CITIZENDEFENDANTPROFILERAccess.class, WluAdminRAccess.class}
    )
    @JsonProperty("applicant1LiPResponse")
    private ClaimantLiPResponse applicant1LiPResponse;
    @CCD(
            label = "Applicant 1 Mediation",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECuPlus2RolesSzdyetAccess.class, CitizenProfileCuAccess.class}
    )
    @JsonProperty("applicant1LiPResponseCarm")
    private MediationLiPCarm applicant1LiPResponseCarm;
    @CCD(
            label = "Translated Document",
            searchable = false,
            access = {CaseworkerCivilAdminCrudCaseworkerCivilSystemFieldReaderRAccess.class, APPSOLSPECPROFILERESSOLONESPECPROFILERAccess.class, CITIZENCLAIMANTPROFILERAccess.class, CaseworkerCivilSystemupdateCrudAccess.class, CaseworkerCivilCrudAccess.class, WluAdminCrudAccess.class}
    )
    private List<Element<TranslatedDocument>> translatedDocuments;
    @CCD(
            label = " ",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECuPlus2RolesSzdyetAccess.class, CITIZENDEFENDANTPROFILECaseworkerCivilSystemupdateCuAccess.class}
    )
    @JsonProperty("respondent1LiPFinancialDetails")
    private FinancialDetailsLiP respondent1LiPFinancialDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILERAccess.class, CITIZENDEFENDANTPROFILERAccess.class}
    )
    @JsonProperty("applicant1ClaimMediationSpecRequiredLip")
    private ClaimantMediationLip applicant1ClaimMediationSpecRequiredLip;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilSystemupdateRAccess.class, CitizenProfileCuiAdminProfileCruAccess.class}
    )
    @JsonProperty("helpWithFees")
    private HelpWithFees helpWithFees;
    @CCD(
            label = "Defendant Additional Details",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECruAccess.class, CaseworkerCivilSystemFieldReaderRCitizenProfileCruAccess.class}
    )
    @JsonProperty("respondent1AdditionalLipPartyDetails")
    private AdditionalLipPartyDetails respondent1AdditionalLipPartyDetails;
    @CCD(
            label = "Respondent Additional Details",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilSystemFieldReaderRCitizenProfileCruAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    @JsonProperty("applicant1AdditionalLipPartyDetails")
    private AdditionalLipPartyDetails applicant1AdditionalLipPartyDetails;

    @CCD(
            label = " ",
            typeOverride = FieldType.YesOrNo,
            access = {CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECruAccess.class, CaseworkerCivilSystemFieldReaderRCitizenProfileCruAccess.class}
    )
    @JsonProperty("respondentSignSettlementAgreement")
    private YesOrNo respondentSignSettlementAgreement;

    @CCD(
            label = "Does the claimant want to settle",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CITIZENCLAIMANTPROFILECruCaseworkerCivilSystemupdateRAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    @JsonProperty("applicant1SettleClaim")
    private YesOrNo applicant1SettleClaim;
    @CCD(
            label = " ",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilSystemFieldReaderRCitizenProfileCruAccess.class}
    )
    @JsonProperty("applicant1ClaimSettledDate")
    private LocalDate applicant1ClaimSettledDate;

    @CCD(
            label = "Request Recon claimant",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, CitizenProfileCuiAdminProfileCruAccess.class, CuiNbcProfileCruAccess.class, HearingScheduleAccessCruAccess.class, JudgeProfileCruAccess.class}
    )
    @JsonProperty("requestForReviewCommentsClaimant")
    private String requestForReviewCommentsClaimant;

    @CCD(
            label = "Request Recon defendant",
            access = {CITIZENCLAIMANTPROFILERPlus3RolesMtlnhzAccess.class, CaseworkerCivilSystemFieldReaderRCitizenProfileCruAccess.class}
    )
    @JsonProperty("requestForReviewCommentsDefendant")
    private String requestForReviewCommentsDefendant;

    @JsonIgnore
    public boolean hasClaimantAgreedToFreeMediation() {
        return applicant1ClaimMediationSpecRequiredLip != null
            && applicant1ClaimMediationSpecRequiredLip.hasClaimantAgreedToFreeMediation();
    }

    @JsonIgnore
    public boolean hasClaimantNotAgreedToFreeMediation() {
        return applicant1ClaimMediationSpecRequiredLip != null
            && applicant1ClaimMediationSpecRequiredLip.hasClaimantNotAgreedToFreeMediation();
    }

    @JsonIgnore
    public String getEvidenceComment() {
        return Optional.ofNullable(respondent1LiPResponse)
            .map(RespondentLiPResponse::getEvidenceComment)
            .orElse("");
    }

    @JsonIgnore
    public String getTimeLineComment() {
        return Optional.ofNullable(respondent1LiPResponse)
            .map(RespondentLiPResponse::getTimelineComment)
            .orElse("");
    }

    @JsonIgnore
    public boolean isDefendantSignedSettlementAgreement() {
        return YesOrNo.YES.equals(respondentSignSettlementAgreement);
    }

    @JsonIgnore
    public boolean isDefendantSignedSettlementNotAgreed() {
        return YesOrNo.NO.equals(respondentSignSettlementAgreement);
    }

    @JsonIgnore
    public boolean hasClaimantAgreedClaimSettled() {
        return YesOrNo.YES.equals(applicant1SettleClaim);
    }
}
