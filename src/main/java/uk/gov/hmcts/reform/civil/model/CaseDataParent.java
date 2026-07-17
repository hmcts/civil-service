package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.EmploymentTypeCheckboxFixedListLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyClaimantResponseLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.enums.RepaymentFrequencyDJ;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.TimelineUploadTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dj.CaseManagementOrderAdditional;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalAndTrialHearingDJToggle;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.enums.sdo.DateToShowToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.DefendantResponseShowTag;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.ResponseOneVOneShowTag;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantMediationLip;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesForTab;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesMoreInformation;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocument;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsReferredInStatement;
import uk.gov.hmcts.reform.civil.model.mediation.MediationNonAttendanceStatement;
import uk.gov.hmcts.reform.civil.model.mediation.UploadMediationDocumentsForm;
import uk.gov.hmcts.reform.civil.model.noc.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackAllocation;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.PPI;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2AddNewDirection;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2AddendumReport;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2DisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2EvidenceAcousticEngineer;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ExpertEvidence;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackAltDisputeResolution;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FurtherAudiogram;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2PermissionToRelyOnExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsClaimantExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2QuestionsToEntExpert;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ScheduleOfLoss;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Settlement;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsAddNewDirection;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsImpNotes;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsJudgesRecital;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsMediation;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsPPI;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsUploadDoc;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsWitnessStatements;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Trial;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2UploadOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2VariationOfDirections;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WelshLanguageUsage;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WitnessOfFact;
import uk.gov.hmcts.reform.civil.model.transferonlinecase.NotSuitableSdoOptions;
import uk.gov.hmcts.reform.civil.model.transferonlinecase.TocTransferCaseReason;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.math.BigDecimal.ZERO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.civil.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruCaseworkerCivilSystemupdateRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILERESSOLTWOSPECPROFILECrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILERPlus3RolesGudjikAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruCaseworkerCivilSolicitorCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CitizenProfileCruWluAdminRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.JudgeProfileLegalAdviserRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLUNSPECPROFILERRESSOLTWOUNSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECudCaseworkerCivilStaffRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCaaCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorCudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCaaCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILECrRESSOLTWOSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECITIZENCLAIMANTPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILECruCaseworkerCivilSystemupdateRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffJudgeProfileRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CitizenProfileCudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerApproverCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.NextHearingDateAdminRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILECaseworkerCivilSystemupdateCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorCruCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLUNSPECPROFILERuPlus2RolesXyhbtoAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILERuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONEUNSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLUNSPECPROFILECuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CitizenProfileCuiAdminProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRCuiAdminProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRCuiAdminProfileCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECuPlus3RolesTjnmxuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseflagsAdminCruCaseflagsViewerRWluAdminCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseflagsAdminCrudPlus3RolesUzqffsAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateCudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCudPlus3RolesMwdwidAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCaseworkerCivilSolicitorCudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess;
import uk.gov.hmcts.reform.civil.ccd.access.JudgeProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminJudgeProfileCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECuPlus3RolesFtetnzAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILERESSOLONEUNSPECPROFILECuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILECuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLTWOSPECPROFILERESSOLTWOUNSPECPROFILECuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCaaCaseworkerCivilSystemupdateJudgeProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCaaJudgeProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.WluAdminRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILERESSOLONEUNSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLTWOSPECPROFILERESSOLTWOUNSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCaaCaseworkerCivilAdminCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECuPlus2RolesSzdyetAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminJudgeProfileLegalAdviserCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILECaseworkerCivilSystemupdateCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCaaCuPlus2RolesCjdhidAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECuPlus2RolesTckblxAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILEAPPSOLUNSPECPROFILECuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminLegalAdviserCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRCitizenProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilCitizenUiPcqextractorRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffCaseworkerCivilSystemupdateCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateJudgeProfileRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLTWOSPECPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILERRESSOLONESPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILERESSOLONESPECPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLTWOSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLTWOSPECPROFILERPlus3RolesWydxxsAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLTWOSPECPROFILERCaseworkerCivilCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CitizenProfileCruAccess;

@Accessors(chain = true)
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@Data
public class CaseDataParent extends CaseDataCaseProgression implements MappableObject {

    @CCD(
            label = "Have the claimants agreed to free mediation?",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
    )
    private  SmallClaimMedicalLRspec applicantMPClaimMediationSpecRequired;
    @CCD(
            label = "Do you want to use an expert?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
    )
    private  YesOrNo applicantMPClaimExpertSpecRequired;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECuAccess.class}
    )
    private  PartnerAndDependentsLRspec respondent1PartnerAndDependent;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
    )
    private  PartnerAndDependentsLRspec respondent2PartnerAndDependent;
    @CCD(
            label = "Do the claimants want to proceed with the claim?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilSystemupdateRAccess.class}
    )
    private  YesOrNo applicant1ProceedWithClaimSpec2v1;

    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRAccess.class})
    private  PaymentUponCourtOrder respondent2CourtOrderPayment;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
    )
    private  RepaymentPlanLRspec respondent2RepaymentPlan;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private  Respondent1EmployerDetailsLRspec responseClaimAdmitPartEmployerRespondent2;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECrudAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private  Respondent1DebtLRspec specDefendant2Debts;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECrudAccess.class}
    )
    private  Respondent1SelfEmploymentLRspec specDefendant2SelfEmploymentDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, APPSOLSPECPROFILERAccess.class}
    )
    private  RespondentResponseTypeSpec respondentClaimResponseTypeForSpecGeneric;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECrudAccess.class, CITIZENCLAIMANTPROFILECrudAccess.class}
    )
    private  RespondentResponseTypeSpec respondent1ClaimResponseTestForSpec;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECrudAccess.class, CITIZENCLAIMANTPROFILECrudAccess.class}
    )
    private  RespondentResponseTypeSpec respondent2ClaimResponseTestForSpec;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECuAccess.class}
    )
    private  YesOrNo respondent1CourtOrderPaymentOption;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PaymentOrder",
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECuAccess.class}
    )
    private  List<Element<Respondent1CourtOrderDetails>> respondent1CourtOrderDetails;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
    )
    private  YesOrNo respondent2CourtOrderPaymentOption;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PaymentOrder",
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
    )
    private  List<Element<Respondent2CourtOrderDetails>> respondent2CourtOrderDetails;
    @CCD(
            label = "Does your client have any loans, or credit card debts?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILERPlus3RolesGudjikAccess.class}
    )
    private  YesOrNo respondent1LoanCreditOption;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PaymentOption",
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILERPlus3RolesGudjikAccess.class}
    )
    private  List<Element<Respondent1LoanCreditDetails>> respondent1LoanCreditDetails;
    @CCD(
            label = "Does your client have any loans, or credit card debts?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECrudAccess.class}
    )
    private  YesOrNo respondent2LoanCreditOption;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PaymentOption",
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECrudAccess.class}
    )
    private  List<Element<Respondent2LoanCreditDetails>> respondent2LoanCreditDetails;
    // for default judgment specified tab
    @CCD(
            label = "Payment type",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilSolicitorCuAccess.class}
    )
    private  DJPaymentTypeSelection paymentTypeSelection;
    @CCD(
            label = "How often do you want to receive payments?",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilSolicitorCuAccess.class}
    )
    private  RepaymentFrequencyDJ repaymentFrequency;
    // for default judgment specified tab
    // for witness
    @CCD(
            label = "Are there any witnesses who should attend the hearing?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
    )
    private  YesOrNo respondent1DQWitnessesRequiredSpec;
    @CCD(
            label = "Witnesses details",
            hint = "If the name is unknown at this time please add TBC to both the first name and last name lines. Then use the Manage Contact Information event to provide the name when known\n",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
    )
    private  List<Element<Witness>> respondent1DQWitnessesDetailsSpec;
    @CCD(
            label = "Claimant 1 witnesses",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CitizenProfileCruWluAdminRAccess.class, CITIZENCLAIMANTPROFILECruAccess.class}
    )
    private  Witnesses applicant1DQWitnessesSmallClaim;
    @CCD(
            label = "Defendant 1 witnesses",
            searchable = false,
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess.class, JudgeProfileLegalAdviserRAccess.class}
    )
    private  Witnesses respondent1DQWitnessesSmallClaim;
    @CCD(
            label = "Defendant 2 witnesses",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, JudgeProfileLegalAdviserRAccess.class, APPSOLUNSPECPROFILERRESSOLTWOUNSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
    )
    private  Witnesses respondent2DQWitnessesSmallClaim;

    @CCD(
            label = "Add Legal Rep Deadline",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCrudAccess.class}
    )
    @Deprecated
    private  LocalDateTime addLegalRepDeadline;

    @CCD(ignore = true)
    @Builder.Default
    private  List<Value<Document>> caseDocuments = new ArrayList<>();
    @CCD(ignore = true)
    private  String caseDocument1Name;
    //TrialReadiness
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECrudAccess.class}
    )
    private  String hearingDurationTextApplicant;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECrudAccess.class}
    )
    private  String hearingDurationTextRespondent1;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private  String hearingDurationTextRespondent2;
    //workaround for showing cases in unassigned case list
    @CCD(
            label = "Respondent 1 Organisation ID Copy",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class}
    )
    private  String respondent1OrganisationIDCopy;
    @CCD(
            label = "Respondent 2 Organisation ID Copy",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class}
    )
    private  String respondent2OrganisationIDCopy;

    @JsonUnwrapped
    private  Mediation mediation;

    /**
     * SNI-5142 made mandatory SHOW.
     */
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<OrderDetailsPagesSectionsToggle> smallClaimsMethodToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<OrderDetailsPagesSectionsToggle> smallClaimsDocumentsToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<OrderDetailsPagesSectionsToggle> smallClaimsWitnessStatementToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<OrderDetailsPagesSectionsToggle> smallClaimsFlightDelayToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<OrderDetailsPagesSectionsToggle> smallClaimsMediationSectionToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<OrderDetailsPagesSectionsToggle> smallClaimsPenalNoticeToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<OrderDetailsPagesSectionsToggle> fastTrackPenalNoticeToggle;
    @CCD(ignore = true)
    private List<DateToShowToggle> smallClaimsHearingDateToToggle;
    @CCD(ignore = true)
    private List<DateToShowToggle> fastTrackTrialDateToToggle;

    //SDOR2
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private YesOrNo isSdoR2NewScreen;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private FastTrackJudgesRecital sdoFastTrackJudgesRecital;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SdoR2FastTrackAltDisputeResolution sdoAltDisputeResolution;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SdoR2VariationOfDirections sdoVariationOfDirections;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SdoR2Settlement sdoR2Settlement;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<IncludeInOrderToggle> sdoR2DisclosureOfDocumentsToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SdoR2DisclosureOfDocuments sdoR2DisclosureOfDocuments;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<IncludeInOrderToggle> sdoR2SeparatorWitnessesOfFactToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SdoR2WitnessOfFact sdoR2WitnessesOfFact;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SdoR2WitnessOfFact sdoR2FastTrackWitnessOfFact;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<IncludeInOrderToggle> sdoR2ScheduleOfLossToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SdoR2ScheduleOfLoss sdoR2ScheduleOfLoss;
    @CCD(
            label = "Add a new direction",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<Element<SdoR2AddNewDirection>> sdoR2AddNewDirection;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<IncludeInOrderToggle> sdoR2TrialToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SdoR2Trial sdoR2Trial;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private String sdoR2ImportantNotesTxt;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private LocalDate sdoR2ImportantNotesDate;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<IncludeInOrderToggle> sdoR2SeparatorExpertEvidenceToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SdoR2ExpertEvidence sdoR2ExpertEvidence;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<IncludeInOrderToggle> sdoR2SeparatorAddendumReportToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SdoR2AddendumReport sdoR2AddendumReport;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<IncludeInOrderToggle> sdoR2SeparatorFurtherAudiogramToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SdoR2FurtherAudiogram sdoR2FurtherAudiogram;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<IncludeInOrderToggle> sdoR2SeparatorQuestionsClaimantExpertToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SdoR2QuestionsClaimantExpert sdoR2QuestionsClaimantExpert;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<IncludeInOrderToggle> sdoR2SeparatorPermissionToRelyOnExpertToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SdoR2PermissionToRelyOnExpert sdoR2PermissionToRelyOnExpert;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<IncludeInOrderToggle> sdoR2SeparatorEvidenceAcousticEngineerToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SdoR2EvidenceAcousticEngineer sdoR2EvidenceAcousticEngineer;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<IncludeInOrderToggle> sdoR2SeparatorQuestionsToEntExpertToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SdoR2QuestionsToEntExpert sdoR2QuestionsToEntExpert;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<IncludeInOrderToggle> sdoR2SeparatorUploadOfDocumentsToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SdoR2UploadOfDocuments sdoR2UploadOfDocuments;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SdoR2SmallClaimsJudgesRecital sdoR2SmallClaimsJudgesRecital;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<IncludeInOrderToggle> sdoR2SmallClaimsPPIToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SdoR2SmallClaimsPPI sdoR2SmallClaimsPPI;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<IncludeInOrderToggle> sdoR2SmallClaimsWitnessStatementsToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CaseworkerCivilSystemupdateCruAccess.class}
    )
    private SdoR2SmallClaimsWitnessStatements sdoR2SmallClaimsWitnessStatements;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CaseworkerCivilSystemupdateCruAccess.class}
    )
    private SdoR2SmallClaimsWitnessStatements sdoR2SmallClaimsWitnessStatementOther;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<IncludeInOrderToggle> sdoR2SmallClaimsUploadDocToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SdoR2SmallClaimsUploadDoc sdoR2SmallClaimsUploadDoc;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<IncludeInOrderToggle> sdoR2SmallClaimsHearingToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SdoR2SmallClaimsHearing sdoR2SmallClaimsHearing;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SdoR2SmallClaimsMediation sdoR2SmallClaimsMediationSectionStatement;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<IncludeInOrderToggle> sdoR2SmallClaimsMediationSectionToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SdoR2SmallClaimsImpNotes sdoR2SmallClaimsImpNotes;
    @CCD(
            label = "Add a new direction",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<Element<SdoR2SmallClaimsAddNewDirection>> sdoR2SmallClaimsAddNewDirection;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<OrderDetailsPagesSectionsToggle> sdoR2FastTrackUseOfWelshToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SdoR2WelshLanguageUsage sdoR2FastTrackUseOfWelshLanguage;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<OrderDetailsPagesSectionsToggle> sdoR2SmallClaimsUseOfWelshToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SdoR2WelshLanguageUsage sdoR2SmallClaimsUseOfWelshLanguage;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<IncludeInOrderToggle> sdoR2NihlUseOfWelshIncludeInOrderToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SdoR2WelshLanguageUsage sdoR2NihlUseOfWelshLanguage;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<IncludeInOrderToggle> sdoR2DrhUseOfWelshIncludeInOrderToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SdoR2WelshLanguageUsage sdoR2DrhUseOfWelshLanguage;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private List<OrderDetailsPagesSectionsToggle> sdoR2DisposalHearingUseOfWelshToggle;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SdoR2WelshLanguageUsage sdoR2DisposalHearingUseOfWelshLanguage;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class})
    private SdoR2WelshLanguageUsage sdoR2DisposalHearingWelshLanguageDJ;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class})
    private List<DisposalAndTrialHearingDJToggle> sdoR2DisposalHearingUseOfWelshLangToggleDJ;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class})
    private SdoR2WelshLanguageUsage sdoR2TrialWelshLanguageDJ;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class})
    private List<DisposalAndTrialHearingDJToggle> sdoR2TrialUseOfWelshLangToggleDJ;
    @CCD(
            label = "Credit hire",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CaseworkerCivilSystemupdateCruAccess.class}
    )
    private SdoR2FastTrackCreditHire sdoR2FastTrackCreditHire;

    @CCD(
            label = "Payment Protection Insurance (PPI)",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PPI smallClaimsPPI;
    @CCD(
            label = "Payment Protection Insurance (PPI)",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private PPI fastTrackPPI;

    @CCD(
            label = "Next Deadline",
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECudCaseworkerCivilStaffRAccess.class, CaseworkerCaaCuAccess.class, CaseworkerCivilSolicitorCudAccess.class}
    )
    private  LocalDate nextDeadline;
    @CCD(
            label = "All party names",
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECudCaseworkerCivilStaffRAccess.class, CaseworkerCaaCuAccess.class, CaseworkerCivilSolicitorCrudAccess.class}
    )
    private  String allPartyNames;
    @CCD(
            label = "Case List Display Defendant Solicitor Reference",
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECudCaseworkerCivilStaffRAccess.class, CaseworkerCivilSolicitorCrudAccess.class}
    )
    private  String caseListDisplayDefendantSolicitorReferences;
    @CCD(
            label = "Unassigned Case List Solicitor References",
            access = {DefaultAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCrudAccess.class, CaseworkerCaaCruAccess.class}
    )
    private  String unassignedCaseListDisplayOrganisationReferences;
    @CCD(
            label = "Is this address correct?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private  YesOrNo specAoSRespondent2CorrespondenceAddressRequired;
    @CCD(
            label = "Add the address you want",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemupdateCrudAccess.class}
    )
    private  Address specAoSRespondent2CorrespondenceAddressdetails;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "RespondentResponseAdmissionType",
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
    )
    private  String defenceRouteRequired2;

    @CCD(
            label = "showHowToAddTimeLinePage",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECrudAccess.class}
    )
    private  YesOrNo showHowToAddTimeLinePage;
    @CCD(
            label = "fullAdmissionAndFullAmountPaid",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECrudAccess.class}
    )
    private  YesOrNo fullAdmissionAndFullAmountPaid;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
    )
    private  YesOrNo specDefenceFullAdmitted2Required;
    @CCD(
            label = "partAdmittedByEitherRespondents",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECrudAccess.class}
    )
    private  YesOrNo partAdmittedByEitherRespondents;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
    )
    private  YesOrNo specDefenceAdmitted2Required;

    @CCD(
            label = "## Has ${respondent2.partyName} paid the claimant the admitted amount? \n",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
    )
    private  String specDefenceRouteAdmittedAmountClaimed2Label;
    @CCD(
            label = " ",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private  RespondToClaim respondToAdmittedClaim2;
    @CCD(
            label = " ",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private  RespondToClaim respondToClaim2;
    /**
     * money amount in pence.
     */
    @CCD(
            label = "How much money does your client admit to owing?",
            hint = "The amount to be considered is the claim amount and any interest claimed. The claim fee and any \n\nfixed costs claimed are not included in this figure but are payable in addition and if judgment is \n\nentered on an admission will be included in the total judgment sum.",
            searchable = false,
            typeOverride = FieldType.MoneyGBP,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private  BigDecimal respondToAdmittedClaimOwingAmount2;
    private  String detailsOfWhyDoesYouDisputeTheClaim2;
    @CCD(
            label = "<div><p>Tell us why your client does not owe the remaining amount.</p><p>If the claimant rejects your explanation you may have to go to a hearing.</p></div>",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
    )
    private  String specDefenceRouteUploadDocumentLabel3;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
    )
    private  TimelineUploadTypeSpec specClaimResponseTimelineList2;
    private  List<TimelineOfEvents> specResponseTimelineOfEvents2;
    @CCD(
            label = "<h2 class=\"govuk-heading-l\">Has ${respondent2.partyName} agreed to free mediation?</h2>\n ",
            searchable = false,
            typeOverride = FieldType.Label,
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class}
    )
    private  String responseClaimMediationSpecLabelRes2;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, RESSOLONESPECPROFILECrRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private  YesOrNo responseClaimMediationSpec2Required;
    @CCD(
            label = "Do you want to use an expert?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, RESSOLONESPECPROFILECrRESSOLTWOSPECPROFILECruAccess.class}
    )
    private  YesOrNo responseClaimExpertSpecRequired2;
    @CCD(
            label = "Do you want hearing to be held at specific court ? \n",
            hint = "If the defendant is individual the case will be held at defendant's preferred court ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, RESSOLONESPECPROFILECrRESSOLTWOSPECPROFILECruAccess.class}
    )
    private  YesOrNo responseClaimCourtLocation2Required;
    @CCD(
            label = "How many witnesses, including the defendant, will give evidence at the hearing?",
            regex = "\\d+",
            searchable = false,
            access = {DefaultAccess.class, RESSOLONESPECPROFILECrRESSOLTWOSPECPROFILECruAccess.class}
    )
    private  String responseClaimWitnesses2;
    @CCD(
            label = "Type of interpreter",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCrudAccess.class}
    )
    private  String smallClaimHearingInterpreterDescription2;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, APPSOLSPECPROFILECITIZENCLAIMANTPROFILECruAccess.class}
    )
    private  String additionalInformationForJudge2;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
    )
    private  RespondentResponsePartAdmissionPaymentTimeLRspec defenceAdmitPartPaymentTimeRouteRequired2;
    @CCD(
            label = " ",
            hint = "For example 9 December 2020",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCrudAccess.class}
    )
    private  RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec2;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
    )
    private  YesOrNo defenceAdmitPartEmploymentType2Required;
    @CCD(
            label = "Employed or self-employed?",
            hint = "Select both if your client is employed and self-employed",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCrudAccess.class}
    )
    private  List<EmploymentTypeCheckboxFixedListLRspec> respondToClaimAdmitPartEmploymentTypeLRspec2;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCrudAccess.class}
    )
    private  UnemployedComplexTypeLRspec respondToClaimAdmitPartUnemployedLRspec2;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
    )
    private  Respondent1EmployerDetailsLRspec responseClaimAdmitPartEmployer2;
    private  YesOrNo respondent2DQCarerAllowanceCredit;

    @CCD(
            label = "Does your client claim Carer's Allowance of Carer's Credit?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class}
    )
    @Deprecated
    private  YesOrNo respondent2DQCarerAllowanceCreditFullAdmission;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
    )
    @Deprecated
    private  String responseToClaimAdmitPartWhyNotPayLRspec2;
    @CCD(
            label = "neitherCompanyNorOrganisation",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECrudAccess.class}
    )
    private  YesOrNo neitherCompanyNorOrganisation;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECrudAccess.class}
    )
    private  RespondentResponsePartAdmissionPaymentTimeLRspec defenceAdmitPartPaymentTimeRouteGeneric;
    @CCD(
            label = "Employed or self-employed?",
            hint = "Select both if your client is employed and self-employed",
            searchable = false,
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECrudAccess.class}
    )
    private  List<EmploymentTypeCheckboxFixedListLRspec> respondToClaimAdmitPartEmploymentTypeLRspecGeneric;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "ShowConditionFlags",
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECrudAccess.class, CITIZENCLAIMANTPROFILECrudAccess.class, CaseworkerCivilStaffCrudAccess.class, CaseworkerCivilSystemupdateCrudAccess.class}
    )
    @Builder.Default
    private  Set<DefendantResponseShowTag> showConditionFlags = new HashSet<>();

    /**
     * money amount in pounds. Waiting here until we address the issue with CaseData having
     * too many fields
     */
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private  BigDecimal respondToAdmittedClaimOwingAmountPounds2;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruCaseworkerCivilSystemupdateRAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private  BigDecimal partAdmitPaidValuePounds;

    @CCD(
            label = "Case Access Category",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "CaseAccessCategory",
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileRAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCrudAccess.class, CaseworkerCivilSystemupdateCrudAccess.class, CitizenProfileCudAccess.class}
    )
    @JsonProperty("CaseAccessCategory")
    private  CaseCategory caseAccessCategory;

    @CCD(
            label = "Change Organisation Request",
            searchable = false,
            access = {CaseworkerCivilSolicitorCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerApproverCrudAccess.class, CaseworkerCaaCruAccess.class}
    )
    private  ChangeOrganisationRequest changeOrganisationRequestField;
    @CCD(
            label = "change of representation details",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemupdateCruAccess.class}
    )
    private  ChangeOfRepresentation changeOfRepresentation;

    /**
     * Adding for PiP to citizen UI.
     */
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECruAccess.class, APPSOLSPECPROFILECruAccess.class, CaseworkerCivilStaffCrudAccess.class, CaseworkerCivilSystemupdateCrudAccess.class}
    )
    private  DefendantPinToPostLRspec respondent1PinToPostLRspec;

    @CCD(
            label = "Next hearing details",
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilSolicitorRAccess.class, CaseworkerCivilSystemupdateCrudAccess.class, NextHearingDateAdminRAccess.class}
    )
    private  NextHearingDetails nextHearingDetails;

    @CCD(ignore = true)
    private  String respondent1EmailAddress;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CitizenProfileCruWluAdminRAccess.class, CITIZENDEFENDANTPROFILECaseworkerCivilSystemupdateCruAccess.class}
    )
    private  YesOrNo applicant1Represented;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENDEFENDANTPROFILECaseworkerCivilSystemupdateCruAccess.class}
    )
    private  YesOrNo anyRepresented;

    /**
     * Adding for LR ITP Update.
     */
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CITIZENCLAIMANTPROFILECruAccess.class}
    )
    private  ResponseOneVOneShowTag showResponseOneVOneFlag;
    @CCD(
            label = "Does the claimant want to settle the claim for the £${respondToAdmittedClaimOwingAmountPounds} including any interest claimed but exclusive of the claim fee and any fixed costs claimed which are payable in addition?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private  YesOrNo applicant1AcceptAdmitAmountPaidSpec;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private  YesOrNo applicant1FullDefenceConfirmAmountPaidSpec;
    @CCD(
            label = "Has ${respondent1.partyName} paid the claimant £${partAdmitPaidValuePounds}?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private  YesOrNo applicant1PartAdmitConfirmAmountPaidSpec;
    @CCD(
            label = "Does the claimant want to settle the claim for the £${partAdmitPaidValuePounds} the defendant has paid?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private  YesOrNo applicant1PartAdmitIntentionToSettleClaimSpec;
    @CCD(
            label = "Does the claimant accept the repayment plan?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private  YesOrNo applicant1AcceptFullAdmitPaymentPlanSpec;
    @CCD(
            label = "Does the claimant accept the repayment plan?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private  YesOrNo applicant1AcceptPartAdmitPaymentPlanSpec;
    @CCD(
            label = " ",
            searchable = false,
            access = {RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECruAccess.class, CaseworkerCivilSolicitorCruCaseworkerCivilSystemFieldReaderRAccess.class, APPSOLUNSPECPROFILERuPlus2RolesXyhbtoAccess.class, APPSOLSPECPROFILERuAccess.class, RESSOLONEUNSPECPROFILECruAccess.class}
    )
    private  CaseDocument respondent1ClaimResponseDocumentSpec;
    @CCD(ignore = true)
    private  CaseDocument respondent2ClaimResponseDocumentSpec;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private  String respondent1PaymentDateToStringSpec;
    @CCD(
            label = "When do you want ${respondent1.partyName} to pay?",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess.class, APPSOLUNSPECPROFILECuAccess.class}
    )
    private  PaymentBySetDate applicant1RequestedPaymentDateForDefendantSpec;
    @CCD(
            label = "Regular payments of\n",
            hint = "For example, £10",
            searchable = false,
            typeOverride = FieldType.MoneyGBP,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess.class, APPSOLUNSPECPROFILECuAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private  BigDecimal applicant1SuggestInstalmentsPaymentAmountForDefendantSpec;
    @CCD(
            label = "How often will your client make payments?",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess.class, APPSOLUNSPECPROFILECuAccess.class}
    )
    private  PaymentFrequencyClaimantResponseLRspec applicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec;
    @CCD(
            label = "Date for first instalment",
            hint = "This must be after ${currentDateboxDefendantSpec}",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess.class, APPSOLUNSPECPROFILECuAccess.class}
    )
    private  LocalDate applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec;
    @CCD(
            label = "Date for claimant suggest pay immediately",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private  LocalDate applicant1SuggestPayImmediatelyPaymentDateForDefendantSpec;
    @CCD(
            label = "currentDate",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruAccess.class}
    )
    private  String currentDateboxDefendantSpec;
    @JsonUnwrapped
    private  CCJPaymentDetails ccjPaymentDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess.class, APPSOLUNSPECPROFILECuAccess.class}
    )
    private  PaymentType applicant1RepaymentOptionForDefendantSpec;

    @JsonUnwrapped
    private  CaseDataLiP caseDataLiP;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, CitizenProfileCuiAdminProfileCruAccess.class}
    )
    private  HelpWithFeesMoreInformation helpWithFeesMoreInformationClaimIssue;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRCuiAdminProfileCruAccess.class})
    private  HelpWithFeesMoreInformation helpWithFeesMoreInformationHearing;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRCuiAdminProfileCuAccess.class})
    private  HelpWithFeesForTab claimIssuedHwfForTab;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRCuiAdminProfileCuAccess.class})
    private  HelpWithFeesForTab hearingHwfForTab;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private  YesOrNo applicantDefenceResponseDocumentAndDQFlag;
    @CCD(
            label = "Migration Id",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCrudAccess.class}
    )
    private  String migrationId;

    @JsonIgnore
    public boolean isApplicantNotRepresented() {
        return this.applicant1Represented == NO;
    }

    @JsonIgnore
    public boolean isApplicantRepresented() {
        return this.applicant1Represented == YES;
    }

    /**
     * Adding for Certificate of Service.
     */
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECuPlus3RolesTjnmxuAccess.class}
    )
    private  CertificateOfService cosNotifyClaimDetails1;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECuPlus3RolesTjnmxuAccess.class}
    )
    private  CertificateOfService cosNotifyClaimDetails2;
    @CCD(
            label = "Is defendant 1 is litigant Party at claim issue?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class}
    )
    private  YesOrNo defendant1LIPAtClaimIssued;
    @CCD(
            label = "Is defendant 2 is litigant Party at claim issue?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class}
    )
    private  YesOrNo defendant2LIPAtClaimIssued;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECuPlus3RolesTjnmxuAccess.class}
    )
    private  CertificateOfService cosNotifyClaimDefendant1;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECuPlus3RolesTjnmxuAccess.class}
    )
    private  CertificateOfService cosNotifyClaimDefendant2;

    //Top level structure objects used for Hearings + Case Flags
    @CCD(
            label = "Case Flags",
            searchable = false,
            access = {CaseflagsAdminCruCaseflagsViewerRWluAdminCruAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private  Flags caseFlags;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseflagsAdminCrudPlus3RolesUzqffsAccess.class, CaseworkerCivilSystemupdateCudAccess.class}
    )
    private  List<Element<PartyFlagStructure>> applicantExperts;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseflagsAdminCrudPlus3RolesUzqffsAccess.class, CaseworkerCivilSystemupdateCudAccess.class}
    )
    private  List<Element<PartyFlagStructure>> respondent1Experts;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseflagsAdminCrudPlus3RolesUzqffsAccess.class, CaseworkerCivilSystemupdateCudAccess.class}
    )
    private  List<Element<PartyFlagStructure>> respondent2Experts;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseflagsAdminCrudPlus3RolesUzqffsAccess.class, CaseworkerCivilSystemupdateCudAccess.class}
    )
    private  List<Element<PartyFlagStructure>> applicantWitnesses;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseflagsAdminCrudPlus3RolesUzqffsAccess.class, CaseworkerCivilSystemupdateCudAccess.class}
    )
    private  List<Element<PartyFlagStructure>> respondent1Witnesses;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseflagsAdminCrudPlus3RolesUzqffsAccess.class, CaseworkerCivilSystemupdateCudAccess.class}
    )
    private  List<Element<PartyFlagStructure>> respondent2Witnesses;
    //Individuals attending from parties that are Org/Company
    @CCD(
            label = "Individuals attending for the organisation details",
            searchable = false,
            access = {CaseworkerCivilAdminCudPlus3RolesMwdwidAccess.class}
    )
    private  List<Element<PartyFlagStructure>> applicant1OrgIndividuals;
    @CCD(
            label = "Individuals attending for the organisation details",
            searchable = false,
            access = {CaseworkerCivilAdminCudPlus3RolesMwdwidAccess.class}
    )
    private  List<Element<PartyFlagStructure>> applicant2OrgIndividuals;
    @CCD(
            label = "Individuals attending for the organisation details",
            searchable = false,
            access = {CaseworkerCivilAdminCudPlus3RolesMwdwidAccess.class}
    )
    private  List<Element<PartyFlagStructure>> respondent1OrgIndividuals;
    @CCD(
            label = "Individuals attending for the organisation details",
            searchable = false,
            access = {CaseworkerCivilAdminCudPlus3RolesMwdwidAccess.class}
    )
    private  List<Element<PartyFlagStructure>> respondent2OrgIndividuals;
    //Individuals attending from Legal Representative Firms
    @CCD(
            label = "Individual attending for the legal representative details",
            searchable = false,
            access = {CaseflagsAdminCrudPlus3RolesUzqffsAccess.class, CaseworkerCivilAdminCaseworkerCivilSolicitorCudAccess.class}
    )
    private  List<Element<PartyFlagStructure>> applicant1LRIndividuals;
    @CCD(
            label = "Individual attending for the legal representative details",
            searchable = false,
            access = {CaseflagsAdminCrudPlus3RolesUzqffsAccess.class, CaseworkerCivilAdminCaseworkerCivilSolicitorCudAccess.class}
    )
    private  List<Element<PartyFlagStructure>> respondent1LRIndividuals;
    @CCD(
            label = "Individual attending for the legal representative details",
            searchable = false,
            access = {CaseflagsAdminCrudPlus3RolesUzqffsAccess.class, CaseworkerCivilAdminCaseworkerCivilSolicitorCudAccess.class}
    )
    private  List<Element<PartyFlagStructure>> respondent2LRIndividuals;

    @CCD(label = " ", searchable = false, access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class})
    private List<DisposalAndTrialHearingDJToggle> disposalHearingDisclosureOfDocumentsDJToggle;
    @CCD(label = " ", searchable = false, access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class})
    private List<DisposalAndTrialHearingDJToggle> disposalHearingWitnessOfFactDJToggle;
    @CCD(label = " ", searchable = false, access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class})
    private List<DisposalAndTrialHearingDJToggle> disposalHearingMedicalEvidenceDJToggle;
    @CCD(label = " ", searchable = false, access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class})
    private List<DisposalAndTrialHearingDJToggle> disposalHearingQuestionsToExpertsDJToggle;
    @CCD(label = " ", searchable = false, access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class})
    private List<DisposalAndTrialHearingDJToggle> disposalHearingSchedulesOfLossDJToggle;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRAccess.class})
    private List<DisposalAndTrialHearingDJToggle> disposalHearingStandardDisposalOrderDJToggle;
    @CCD(label = " ", searchable = false, access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class})
    private List<DisposalAndTrialHearingDJToggle> disposalHearingFinalDisposalHearingDJToggle;
    @CCD(label = " ", searchable = false, access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class})
    private List<DisposalAndTrialHearingDJToggle> disposalHearingBundleDJToggle;
    @CCD(label = " ", searchable = false, access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class})
    private List<DisposalAndTrialHearingDJToggle> disposalHearingClaimSettlingDJToggle;
    @CCD(label = " ", searchable = false, access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class})
    private List<DisposalAndTrialHearingDJToggle> disposalHearingCostsDJToggle;

    @CCD(label = " ", searchable = false, access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class})
    private List<DisposalAndTrialHearingDJToggle> trialHearingAlternativeDisputeDJToggle;
    @CCD(label = " ", searchable = false, access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class})
    private List<DisposalAndTrialHearingDJToggle> trialHearingVariationsDirectionsDJToggle;
    @CCD(label = " ", searchable = false, access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class})
    private List<DisposalAndTrialHearingDJToggle> trialHearingSettlementDJToggle;
    @CCD(label = " ", searchable = false, access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class})
    private List<DisposalAndTrialHearingDJToggle> trialHearingDisclosureOfDocumentsDJToggle;
    @CCD(label = " ", searchable = false, access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class})
    private List<DisposalAndTrialHearingDJToggle> trialHearingWitnessOfFactDJToggle;
    @CCD(label = " ", searchable = false, access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class})
    private List<DisposalAndTrialHearingDJToggle> trialHearingSchedulesOfLossDJToggle;
    @CCD(label = " ", searchable = false, access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class})
    private List<DisposalAndTrialHearingDJToggle> trialHearingCostsToggle;
    @CCD(label = " ", searchable = false, access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class})
    private List<DisposalAndTrialHearingDJToggle> trialHearingTrialDJToggle;

    @CCD(
            label = "Select additional directions, if any",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, JudgeProfileCruAccess.class}
    )
    private List<CaseManagementOrderAdditional> caseManagementOrderAdditional;

    @CCD(
            label = "Request for information document in casefile view",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCuAccess.class, CaseworkerCivilAdminJudgeProfileCuAccess.class, CaseworkerCaaCuAccess.class}
    )
    private  List<Element<CaseDocument>> requestForInfoDocStaff;
    @CCD(
            label = "Request for information document in casefile view",
            searchable = false,
            access = {APPSOLSPECPROFILECuPlus3RolesFtetnzAccess.class, CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCuAccess.class, CaseworkerCaaCuAccess.class}
    )
    private  List<Element<CaseDocument>> requestForInfoDocClaimant;
    @CCD(
            label = "Request for information document in casefile view",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCuAccess.class, RESSOLONESPECPROFILERESSOLONEUNSPECPROFILECuAccess.class, CITIZENDEFENDANTPROFILECuAccess.class, CaseworkerCaaCuAccess.class}
    )
    private  List<Element<CaseDocument>> requestForInfoDocRespondentSol;
    @CCD(
            label = "Request for information document in casefile view",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCuAccess.class, RESSOLTWOSPECPROFILERESSOLTWOUNSPECPROFILECuAccess.class, CaseworkerCaaCuAccess.class}
    )
    private  List<Element<CaseDocument>> requestForInfoDocRespondentSolTwo;

    @CCD(
            label = "written sequential document in casefile view",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCaaCaseworkerCivilSystemupdateJudgeProfileCruAccess.class}
    )
    private  List<Element<CaseDocument>> writtenRepSequentialDocument;

    @CCD(
            label = "written sequential document in casefile view",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class, CaseworkerCaaJudgeProfileCruAccess.class, WluAdminRAccess.class}
    )
    private  List<Element<CaseDocument>> writtenRepSeqDocStaff;
    @CCD(
            label = "written sequential document in casefile view",
            searchable = false,
            access = {APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECruAccess.class, CaseworkerCaaCruAccess.class}
    )
    private  List<Element<CaseDocument>> writtenRepSeqDocClaimant;
    @CCD(
            label = "written sequential document in casefile view",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, RESSOLONESPECPROFILERESSOLONEUNSPECPROFILECruAccess.class, CaseworkerCaaCruAccess.class}
    )
    private  List<Element<CaseDocument>> writtenRepSeqDocRespondentSol;
    @CCD(
            label = "Upload Request for information casefile view",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, RESSOLTWOSPECPROFILERESSOLTWOUNSPECPROFILECruAccess.class, CaseworkerCaaCruAccess.class}
    )
    private  List<Element<CaseDocument>> writtenRepSeqDocRespondentSolTwo;

    @CCD(
            label = "written sequential document in casefile view",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCaaCaseworkerCivilSystemupdateJudgeProfileCruAccess.class}
    )
    private  List<Element<CaseDocument>> writtenRepConcurrentDocument;

    @CCD(
            label = "written sequential document in casefile view",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class, CaseworkerCaaJudgeProfileCruAccess.class, WluAdminRAccess.class}
    )
    private  List<Element<CaseDocument>> writtenRepConDocStaff;
    @CCD(
            label = "written sequential document in casefile view",
            searchable = false,
            access = {APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECruAccess.class, CaseworkerCaaCruAccess.class}
    )
    private  List<Element<CaseDocument>> writtenRepConDocClaimant;
    @CCD(
            label = "written sequential document in casefile view",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, RESSOLONESPECPROFILERESSOLONEUNSPECPROFILECruAccess.class, CaseworkerCaaCruAccess.class}
    )
    private  List<Element<CaseDocument>> writtenRepConDocRespondentSol;
    @CCD(
            label = "written sequential document in casefile view",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, RESSOLTWOSPECPROFILERESSOLTWOUNSPECPROFILECruAccess.class, CaseworkerCaaCruAccess.class}
    )
    private  List<Element<CaseDocument>> writtenRepConDocRespondentSolTwo;

    @CCD(
            label = "hearing order doc in casefile view",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECaseworkerCivilSystemupdateCruAccess.class, CaseworkerCaaCaseworkerCivilAdminCruAccess.class}
    )
    private  List<Element<CaseDocument>> hearingOrderDocument;

    @CCD(
            label = "hearing order doc in casefile view",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCuAccess.class, CaseworkerCivilAdminJudgeProfileCuAccess.class, CaseworkerCaaCuAccess.class}
    )
    private  List<Element<CaseDocument>> hearingOrderDocStaff;
    @CCD(
            label = "hearing order doc in casefile view",
            searchable = false,
            access = {APPSOLSPECPROFILECuPlus3RolesFtetnzAccess.class, CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCuAccess.class, CaseworkerCaaCuAccess.class}
    )
    private  List<Element<CaseDocument>> hearingOrderDocClaimant;
    @CCD(
            label = "hearing order doc in casefile view",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCuAccess.class, RESSOLONESPECPROFILERESSOLONEUNSPECPROFILECuAccess.class, CaseworkerCaaCuAccess.class}
    )
    private  List<Element<CaseDocument>> hearingOrderDocRespondentSol;
    @CCD(
            label = "hearing order doc in casefile view",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCuAccess.class, RESSOLTWOSPECPROFILERESSOLTWOUNSPECPROFILECuAccess.class, CaseworkerCaaCuAccess.class}
    )
    private  List<Element<CaseDocument>> hearingOrderDocRespondentSolTwo;

    @CCD(
            label = "Request for information document in casefile view",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECruAccess.class, CaseworkerCaaCaseworkerCivilSystemupdateJudgeProfileCruAccess.class}
    )
    private  List<Element<CaseDocument>> requestForInformationDocument;

    @CCD(
            label = "Dismissal order document",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECuPlus2RolesSzdyetAccess.class, CaseworkerCivilAdminJudgeProfileLegalAdviserCuAccess.class, CITIZENDEFENDANTPROFILECaseworkerCivilSystemupdateCuAccess.class, CaseworkerCaaCuAccess.class}
    )
    private  List<Element<CaseDocument>> dismissalOrderDocument;
    @CCD(
            label = "Dismissal order document",
            searchable = false,
            access = {CaseworkerCaaCuPlus2RolesCjdhidAccess.class, CaseworkerCivilAdminJudgeProfileCuAccess.class}
    )
    private  List<Element<CaseDocument>> dismissalOrderDocStaff;
    @CCD(
            label = "Dismissal order document",
            searchable = false,
            access = {CaseworkerCaaCuPlus2RolesCjdhidAccess.class, APPSOLSPECPROFILECuPlus3RolesFtetnzAccess.class}
    )
    private  List<Element<CaseDocument>> dismissalOrderDocClaimant;
    @CCD(
            label = "Dismissal order document",
            searchable = false,
            access = {CaseworkerCaaCuPlus2RolesCjdhidAccess.class, RESSOLONESPECPROFILERESSOLONEUNSPECPROFILECuAccess.class}
    )
    private  List<Element<CaseDocument>> dismissalOrderDocRespondentSol;
    @CCD(
            label = "Dismissal order document",
            searchable = false,
            access = {CaseworkerCaaCuPlus2RolesCjdhidAccess.class, RESSOLTWOSPECPROFILERESSOLTWOUNSPECPROFILECuAccess.class}
    )
    private  List<Element<CaseDocument>> dismissalOrderDocRespondentSolTwo;

    @CCD(
            label = "Directions order document",
            searchable = false,
            access = {CaseworkerCaaCuPlus2RolesCjdhidAccess.class, CaseworkerCivilAdminJudgeProfileLegalAdviserCuAccess.class, CITIZENCLAIMANTPROFILECuPlus2RolesTckblxAccess.class}
    )
    private  List<Element<CaseDocument>> directionOrderDocument;
    @CCD(
            label = "Directions order document",
            searchable = false,
            access = {CaseworkerCaaCuPlus2RolesCjdhidAccess.class, CaseworkerCivilAdminJudgeProfileCuAccess.class}
    )
    private  List<Element<CaseDocument>> directionOrderDocStaff;
    @CCD(
            label = "Directions order document",
            searchable = false,
            access = {CaseworkerCaaCuPlus2RolesCjdhidAccess.class, APPSOLSPECPROFILECuPlus3RolesFtetnzAccess.class}
    )
    private  List<Element<CaseDocument>> directionOrderDocClaimant;
    @CCD(
            label = "Directions order document",
            searchable = false,
            access = {CaseworkerCaaCuPlus2RolesCjdhidAccess.class, RESSOLONESPECPROFILERESSOLONEUNSPECPROFILECuAccess.class}
    )
    private  List<Element<CaseDocument>> directionOrderDocRespondentSol;
    @CCD(
            label = "Directions order document",
            searchable = false,
            access = {CaseworkerCaaCuPlus2RolesCjdhidAccess.class, RESSOLTWOSPECPROFILERESSOLTWOUNSPECPROFILECuAccess.class}
    )
    private  List<Element<CaseDocument>> directionOrderDocRespondentSolTwo;

    @CCD(ignore = true)
    private  List<Element<CaseDocument>> hearingNoticeDocument;
    @CCD(
            label = "Hearing Notice",
            searchable = false,
            access = {CaseworkerCaaCuPlus2RolesCjdhidAccess.class, CaseworkerCivilAdminJudgeProfileCuAccess.class}
    )
    private  List<Element<CaseDocument>> hearingNoticeDocStaff;
    @CCD(
            label = "Hearing Notice",
            searchable = false,
            access = {CaseworkerCaaCuPlus2RolesCjdhidAccess.class, APPSOLSPECPROFILECuPlus3RolesFtetnzAccess.class}
    )
    private  List<Element<CaseDocument>> hearingNoticeDocClaimant;
    @CCD(
            label = "Hearing Notice",
            searchable = false,
            access = {CaseworkerCaaCuPlus2RolesCjdhidAccess.class, RESSOLONESPECPROFILERESSOLONEUNSPECPROFILECuAccess.class}
    )
    private  List<Element<CaseDocument>> hearingNoticeDocRespondentSol;
    @CCD(
            label = "Hearing Notice",
            searchable = false,
            access = {CaseworkerCaaCuPlus2RolesCjdhidAccess.class, RESSOLTWOSPECPROFILERESSOLTWOUNSPECPROFILECuAccess.class}
    )
    private  List<Element<CaseDocument>> hearingNoticeDocRespondentSolTwo;

    @CCD(ignore = true)
    private  List<Element<Document>> gaRespDocument;
    @CCD(
            label = "Respondent Evidence in casefile view",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCaaCaseworkerCivilSystemupdateJudgeProfileCruAccess.class}
    )
    private  List<Element<Document>> gaRespDocStaff;
    @CCD(
            label = "Respondent Evidence in casefile view",
            searchable = false,
            access = {APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECruAccess.class, CaseworkerCaaCruAccess.class}
    )
    private  List<Element<Document>> gaRespDocClaimant;
    @CCD(
            label = "Respondent Evidence casefile view",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, RESSOLONESPECPROFILERESSOLONEUNSPECPROFILECruAccess.class, CaseworkerCaaCruAccess.class}
    )
    private  List<Element<Document>> gaRespDocRespondentSol;
    @CCD(
            label = "Respondent Evidence casefile view",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, RESSOLTWOSPECPROFILERESSOLTWOUNSPECPROFILECruAccess.class, CaseworkerCaaCruAccess.class}
    )
    private  List<Element<Document>> gaRespDocRespondentSolTwo;

    @CCD(
            label = "Defendant 2's legal representative correspondence address",
            searchable = false,
            access = {DefaultAccess.class, JudgeProfileLegalAdviserRAccess.class, CaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemupdateCudAccess.class}
    )
    private  Address specRespondent2CorrespondenceAddressdetails;
    @CCD(
            label = "Do you want to change the address?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerCivilSolicitorCruAccess.class}
    )
    private  YesOrNo specRespondent2CorrespondenceAddressRequired;

    @CCD(
            label = "Claimant 1 unavailable dates",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCuAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECuAccess.class, CaseworkerCivilAdminLegalAdviserCuAccess.class}
    )
    private List<Element<UnavailableDate>> applicant1UnavailableDatesForTab;
    @CCD(
            label = "Claimant 2 unavailable dates",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCuAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECuAccess.class, CaseworkerCivilAdminLegalAdviserCuAccess.class}
    )
    private List<Element<UnavailableDate>> applicant2UnavailableDatesForTab;
    @CCD(
            label = "Defendant 1 unavailable dates",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCuAccess.class, RESSOLONESPECPROFILERESSOLONEUNSPECPROFILECuAccess.class, CaseworkerCivilAdminLegalAdviserCuAccess.class}
    )
    private List<Element<UnavailableDate>> respondent1UnavailableDatesForTab;
    @CCD(
            label = "Defendant 2 unavailable dates",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCuAccess.class, RESSOLTWOSPECPROFILERESSOLTWOUNSPECPROFILECuAccess.class, CaseworkerCivilAdminLegalAdviserCuAccess.class}
    )
    private List<Element<UnavailableDate>> respondent2UnavailableDatesForTab;
    @CCD(
            label = "PCQ id",
            access = {CaseworkerCivilSystemFieldReaderRCitizenProfileCruAccess.class, CaseworkerCivilCitizenUiPcqextractorRAccess.class, CaseworkerCivilCruAccess.class}
    )
    private String pcqId;
    @CCD(
            label = "PCQ id",
            access = {CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECruAccess.class, CaseworkerCivilCitizenUiPcqextractorRAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private String respondentResponsePcqId;

    // Transfer a Case Online
    @CCD(
            label = "Reasons for transfer",
            hint = "For example, allocated court location is not appropriate",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateCruAccess.class, CaseworkerCivilRAccess.class}
    )
    private String reasonForTransfer;
    @CCD(
            label = "New hearing centre location",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateCruAccess.class, CaseworkerCivilRAccess.class}
    )
    private DynamicList transferCourtLocationList;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CaseworkerCivilAdminCruAccess.class}
    )
    private NotSuitableSdoOptions notSuitableSdoOptions;
    @CCD(
            label = "The case should be sent to another hearing centre for directions",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private TocTransferCaseReason tocTransferCaseReason;
    @CCD(
            label = "Claimant Language Preference",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilSystemFieldReaderRCitizenProfileCruAccess.class, CaseworkerCivilSystemupdateJudgeProfileRAccess.class}
    )
    private String claimantBilingualLanguagePreference;

    @JsonUnwrapped
    private  UpdateDetailsForm updateDetailsForm;

    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private FastTrackAllocation fastTrackAllocation;

    @CCD(
            label = "Show CARM field",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, APPSOLSPECPROFILECruAccess.class, CITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilCruAccess.class}
    )
    private YesOrNo showCarmFields;

    @JsonUnwrapped
    private UploadMediationDocumentsForm uploadMediationDocumentsForm;

    @CCD(
            label = "Claimant One - Non-attendance statement",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "NonAttendanceMediationStatement",
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess.class, CaseworkerCivilStaffJudgeProfileRAccess.class, RESSOLTWOSPECPROFILERAccess.class}
    )
    private List<Element<MediationNonAttendanceStatement>> app1MediationNonAttendanceDocs;
    @CCD(
            label = "Claimant One - Documents referred to in statement",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentsReferred",
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess.class, CaseworkerCivilStaffJudgeProfileRAccess.class, RESSOLTWOSPECPROFILERAccess.class}
    )
    private List<Element<MediationDocumentsReferredInStatement>> app1MediationDocumentsReferred;

    @CCD(
            label = "Claimant Two - Non-attendance statement",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "NonAttendanceMediationStatement",
            access = {DefaultAccess.class, APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess.class, CaseworkerCivilStaffJudgeProfileRAccess.class, RESSOLTWOSPECPROFILERAccess.class}
    )
    private List<Element<MediationNonAttendanceStatement>> app2MediationNonAttendanceDocs;
    @CCD(
            label = "Claimant Two - Documents referred to in statement",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentsReferred",
            access = {DefaultAccess.class, APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess.class, CaseworkerCivilStaffJudgeProfileRAccess.class, RESSOLTWOSPECPROFILERAccess.class}
    )
    private List<Element<MediationDocumentsReferredInStatement>> app2MediationDocumentsReferred;

    @CCD(
            label = "Defendant One - Non-attendance statement",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "NonAttendanceMediationStatement",
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileRAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECruAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruAccess.class, RESSOLTWOSPECPROFILERAccess.class}
    )
    private List<Element<MediationNonAttendanceStatement>> res1MediationNonAttendanceDocs;
    @CCD(
            label = "Defendant One - Documents referred to in statement",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentsReferred",
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileRAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECruAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruAccess.class, RESSOLTWOSPECPROFILERAccess.class}
    )
    private List<Element<MediationDocumentsReferredInStatement>> res1MediationDocumentsReferred;

    @CCD(
            label = "Defendant Two - Non-attendance statement",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "NonAttendanceMediationStatement",
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileRAccess.class, APPSOLSPECPROFILERESSOLONESPECPROFILERAccess.class, RESSOLTWOSPECPROFILECruAccess.class}
    )
    private List<Element<MediationNonAttendanceStatement>> res2MediationNonAttendanceDocs;
    @CCD(
            label = "Defendant Two - Documents referred to in statement",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentsReferred",
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileRAccess.class, APPSOLSPECPROFILERESSOLONESPECPROFILERAccess.class, RESSOLTWOSPECPROFILECruAccess.class}
    )
    private List<Element<MediationDocumentsReferredInStatement>> res2MediationDocumentsReferred;

    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    private SmallClaimsMediation smallClaimsMediationSectionStatement;

    @CCD(
            label = "Fixed Costs",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess.class, RESSOLTWOSPECPROFILERPlus3RolesWydxxsAccess.class, CITIZENDEFENDANTPROFILERAccess.class}
    )
    private FixedCosts fixedCosts;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess.class, RESSOLTWOSPECPROFILERPlus3RolesWydxxsAccess.class}
    )
    private YesOrNo showDJFixedCostsScreen;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess.class, RESSOLTWOSPECPROFILERPlus3RolesWydxxsAccess.class}
    )
    private YesOrNo showOldDJFixedCostsScreen;
    @CCD(
            label = "Would you like to claim for fixed costs on entry of judgment?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess.class, RESSOLTWOSPECPROFILERPlus3RolesWydxxsAccess.class}
    )
    private YesOrNo claimFixedCostsOnEntryDJ;

    @CCD(
            label = " ",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCrudAccess.class}
    )
    private YesOrNo mediationFileSentToMmt;
    @CCD(
            label = " ",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCrudAccess.class}
    )
    private YesOrNo evidenceUploadNotificationSent;

    @JsonIgnore
    public boolean isResponseAcceptedByClaimant() {
        return applicant1AcceptAdmitAmountPaidSpec == YesOrNo.YES
            || applicant1AcceptFullAdmitPaymentPlanSpec == YesOrNo.YES
            || applicant1AcceptPartAdmitPaymentPlanSpec == YesOrNo.YES;
    }

    @CCD(
            label = "Claimant User Details",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class, APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess.class, RESSOLTWOSPECPROFILERCaseworkerCivilCruAccess.class, CitizenProfileCruAccess.class}
    )
    private  IdamUserDetails claimantUserDetails;
    @CCD(
            label = "Defendant User Details",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECruAccess.class}
    )
    private  IdamUserDetails defendantUserDetails;

    private  ClaimProceedsInCasemanLR claimProceedsInCasemanLR;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess.class, CitizenProfileCruWluAdminRAccess.class, RESSOLTWOSPECPROFILERAccess.class}
    )
    private  ResponseDocument applicant1DefenceResponseDocumentSpec;

    @JsonIgnore
    public BigDecimal getUpFixedCostAmount(BigDecimal claimAmount) {
        BigDecimal lowerRangeClaimAmount = BigDecimal.valueOf(25);
        BigDecimal upperRangeClaimAmount = BigDecimal.valueOf(5000);
        BigDecimal midCostAmount = BigDecimal.valueOf(40);

        if ((!YES.equals(getCcjPaymentDetails().getCcjJudgmentFixedCostOption())
            || (claimAmount.compareTo(lowerRangeClaimAmount) < 0))) {
            return ZERO;
        }
        if (claimAmount.compareTo(upperRangeClaimAmount) <= 0) {
            return midCostAmount;
        }
        return BigDecimal.valueOf(55);
    }

    @JsonIgnore
    public boolean isRespondentResponseBilingual() {
        return Optional.ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getRespondent1LiPResponse)
            .map(RespondentLiPResponse::getRespondent1ResponseLanguage)
            .filter(language -> language.equals(Language.BOTH.toString())
                || language.equals(Language.WELSH.toString()))
            .isPresent();
    }

    @JsonIgnore
    public String getDefendantBilingualLanguagePreference() {
        return Optional.ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getRespondent1LiPResponse)
            .map(RespondentLiPResponse::getRespondent1ResponseLanguage)
            .orElse(null);
    }

    @JsonIgnore
    public boolean hasClaimantAgreedToFreeMediation() {
        Optional<CaseDataLiP> caseDataLiP1 = Optional.ofNullable(getCaseDataLiP());
        return caseDataLiP1.map(CaseDataLiP::getApplicant1ClaimMediationSpecRequiredLip)
            .filter(ClaimantMediationLip::hasClaimantAgreedToFreeMediation).isPresent()
            || isCorrectEmailPresent(caseDataLiP1)
            || isCorrectPhonePresent(caseDataLiP1);
    }

    private static boolean isCorrectPhonePresent(Optional<CaseDataLiP> caseDataLiP1) {
        return (caseDataLiP1.map(CaseDataLiP::getApplicant1LiPResponseCarm)
            .filter(carm -> carm.getIsMediationPhoneCorrect() == YES).isPresent()
            || caseDataLiP1.map(CaseDataLiP::getApplicant1LiPResponseCarm)
            .filter(carm -> carm.getAlternativeMediationTelephone() != null).isPresent());
    }

    private static boolean isCorrectEmailPresent(Optional<CaseDataLiP> caseDataLiP1) {
        return (caseDataLiP1.map(CaseDataLiP::getApplicant1LiPResponseCarm)
            .filter(carm -> carm.getIsMediationEmailCorrect() == YES).isPresent()
            || caseDataLiP1.map(CaseDataLiP::getApplicant1LiPResponseCarm)
            .filter(carm -> carm.getAlternativeMediationEmail() != null).isPresent());
    }

    @JsonIgnore
    public List<Element<TranslatedDocument>> getTranslatedDocuments() {
        return Optional.ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getTranslatedDocuments)
            .orElse(Collections.emptyList());
    }

    @JsonIgnore
    public boolean hasClaimantNotAgreedToFreeMediation() {
        return Optional.ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1ClaimMediationSpecRequiredLip)
            .filter(ClaimantMediationLip::hasClaimantNotAgreedToFreeMediation).isPresent();
    }

    @JsonIgnore
    public boolean isClaimantBilingual() {
        return null != claimantBilingualLanguagePreference
            && !claimantBilingualLanguagePreference.equalsIgnoreCase(Language.ENGLISH.toString());
    }

    @JsonIgnore
    public boolean isFullDefenceNotPaid() {
        return NO.equals(getApplicant1FullDefenceConfirmAmountPaidSpec());
    }

    @JsonIgnore
    public boolean applicant1SuggestedPayImmediately() {
        return applicant1RepaymentOptionForDefendantSpec == PaymentType.IMMEDIATELY;
    }

    @JsonIgnore
    public boolean applicant1SuggestedPayBySetDate() {
        return applicant1RepaymentOptionForDefendantSpec == PaymentType.SET_DATE;
    }

    @JsonIgnore
    public boolean applicant1SuggestedPayByInstalments() {
        return applicant1RepaymentOptionForDefendantSpec == PaymentType.REPAYMENT_PLAN;
    }

    @JsonIgnore
    public boolean hasClaimantAgreedClaimSettled() {
        return Optional.ofNullable(getCaseDataLiP())
            .filter(CaseDataLiP::hasClaimantAgreedClaimSettled).isPresent();
    }
}
