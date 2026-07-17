package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseNoteType;
import uk.gov.hmcts.reform.civil.enums.CourtStaffNextSteps;
import uk.gov.hmcts.reform.civil.enums.DecisionOnRequestReconsiderationOptions;
import uk.gov.hmcts.reform.civil.enums.SettlementReason;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.caseprogression.ConfirmListingTickBox;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadDisclosure;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadExpert;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadTrial;
import uk.gov.hmcts.reform.civil.enums.caseprogression.EvidenceUploadWitness;
import uk.gov.hmcts.reform.civil.enums.caseprogression.FinalOrderSelection;
import uk.gov.hmcts.reform.civil.enums.caseprogression.OrderOnCourtsList;
import uk.gov.hmcts.reform.civil.enums.cosc.CoscApplicationStatus;
import uk.gov.hmcts.reform.civil.enums.cosc.CoscRPAStatus;
import uk.gov.hmcts.reform.civil.enums.finalorders.AssistedCostTypesList;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrderToggle;
import uk.gov.hmcts.reform.civil.enums.finalorders.FinalOrdersJudgePapers;
import uk.gov.hmcts.reform.civil.enums.finalorders.HearingLengthFinalOrderList;
import uk.gov.hmcts.reform.civil.enums.finalorders.OrderMadeOnTypes;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingChannel;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingDuration;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingNoticeList;
import uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.enums.sendandreply.SendAndReplyOption;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.ConfirmOrderGivesPermission;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.DiscontinuanceTypeList;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.MarkPaidConsentList;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.SettleDiscontinueYesOrNoList;
import uk.gov.hmcts.reform.civil.model.caseprogression.FreeFormOrderValues;
import uk.gov.hmcts.reform.civil.model.caseprogression.HearingOtherComments;
import uk.gov.hmcts.reform.civil.model.caseprogression.RevisedHearingRequirements;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceDocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.dmnacourttasklocation.TaskManagementLocationTab;
import uk.gov.hmcts.reform.civil.model.dmnacourttasklocation.TaskManagementLocationTypes;
import uk.gov.hmcts.reform.civil.model.documents.DocumentAndNote;
import uk.gov.hmcts.reform.civil.model.documents.DocumentWithName;
import uk.gov.hmcts.reform.civil.model.finalorders.AssistedOrderCostDetails;
import uk.gov.hmcts.reform.civil.model.finalorders.AssistedOrderReasons;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderAppeal;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrdersComplexityBand;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderFurtherHearing;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderRecitalsRecorded;
import uk.gov.hmcts.reform.civil.model.finalorders.FinalOrderRepresentation;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderAfterHearingDate;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMade;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMadeOnDetails;
import uk.gov.hmcts.reform.civil.model.finalorders.OrderMadeOnDetailsOrderWithoutNotice;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentInstalmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaidInFull;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaymentPlan;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRecordedReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideOrderType;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentSetAsideReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentFrequency;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.model.querymanagement.LatestQuery;
import uk.gov.hmcts.reform.civil.model.sdo.ReasonForReconsideration;
import uk.gov.hmcts.reform.civil.model.sendandreply.Message;
import uk.gov.hmcts.reform.civil.model.sendandreply.MessageReply;
import uk.gov.hmcts.reform.civil.model.sendandreply.SendMessageMetadata;
import uk.gov.hmcts.reform.civil.model.transferonlinecase.TransferCaseDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Optional.ofNullable;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorJudgeProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruPlus7RolesAvzornAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLTWOUNSPECPROFILERPlus3RolesWplpvdAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLTWOUNSPECPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILERPlus3RolesMtlnhzAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLTWOUNSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECudPlus8RolesZezyuzAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorCudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.JudgeProfileCudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.HearingScheduleAccessCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.JudgeProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.JudgeProfileCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.WluAdminCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRCourtOfficerOrderCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRHearingScheduleAccessCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILECruCaseworkerCivilSystemupdateRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.HearingScheduleAccessCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateRHearingScheduleAccessCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCrudCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffRCaseworkerCivilSystemupdateCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CitizenProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLTWOSPECPROFILERESSOLTWOUNSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILERESSOLONEUNSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECuPlus3RolesFtetnzAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILERESSOLONEUNSPECPROFILECuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLTWOSPECPROFILERESSOLTWOUNSPECPROFILECuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CuiAdminProfileCuiNbcProfileHearingScheduleAccessJudgeProfileCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilJudgeProfileRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.GSProfileCaseworkerCivilSystemupdateCaseworkerRasValidationRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminJudgeProfileCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorCuCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerRasValidationRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRJudgeProfileCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.JudgeProfileCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseflagsAdminCrudPlus3RolesUzqffsAccess;
import uk.gov.hmcts.reform.civil.ccd.access.GSProfileRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruCaseworkerCivilStaffRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRCuiAdminProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruCaseworkerCivilSystemupdateRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECuPlus2RolesSzdyetAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCaseworkerCivilStaffCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILECuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CitizenProfileCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECITIZENCLAIMANTPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffCaseworkerCivilSystemupdateCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CitizenProfileRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruCaseworkerCivilSolicitorCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.LegalAdviserCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRCitizenProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILECrAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLTWOSPECPROFILECrAccess;
import uk.gov.hmcts.reform.civil.ccd.access.JudgeProfileRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECrAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECrAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILECrRESSOLTWOSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECruCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILERPlus5RolesPvjgzlAccess;
import uk.gov.hmcts.reform.civil.ccd.access.LegalAdviserRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLUNSPECPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILERESSOLONEUNSPECPROFILEWluAdminRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLTWOSPECPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILERESSOLONESPECPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.WluAdminCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CuiAdminProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateCuiAdminProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CuiAdminProfileCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminWluAdminCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminJudgeProfileLegalAdviserCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffCtscCudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.GSProfileRPlus4RolesOdnovhAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateCudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECruPlus12RolesJypaltAccess;

@Accessors(chain = true)
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@Data
public class CaseDataCaseProgression extends CivilCaseData implements MappableObject {

    @CCD(
            label = " ",
            searchable = false,
            access = {RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CaseworkerCivilSolicitorJudgeProfileCruAccess.class, CaseworkerCivilSystemupdateCruAccess.class}
    )
    private  String notificationText;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class}
    )
    private  List<EvidenceUploadDisclosure> disclosureSelectionEvidence;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class}
    )
    private  List<EvidenceUploadDisclosure> disclosureSelectionEvidenceRes;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class}
    )
    private  List<EvidenceUploadWitness> witnessSelectionEvidence;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "EvidenceUploadWitnessSmallClaim",
            access = {DefaultAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class}
    )
    private  List<EvidenceUploadWitness> witnessSelectionEvidenceSmallClaim;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class}
    )
    private  List<EvidenceUploadWitness> witnessSelectionEvidenceRes;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "EvidenceUploadWitnessSmallClaim",
            access = {DefaultAccess.class, RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class}
    )
    private  List<EvidenceUploadWitness> witnessSelectionEvidenceSmallClaimRes;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class}
    )
    private  List<EvidenceUploadExpert> expertSelectionEvidenceRes;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class}
    )
    private  List<EvidenceUploadExpert> expertSelectionEvidence;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "EvidenceUploadExpertSmallClaim",
            access = {DefaultAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class}
    )
    private  List<EvidenceUploadExpert> expertSelectionEvidenceSmallClaim;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "EvidenceUploadExpertSmallClaim",
            access = {DefaultAccess.class, RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class}
    )
    private  List<EvidenceUploadExpert> expertSelectionEvidenceSmallClaimRes;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class}
    )
    private  List<EvidenceUploadTrial> trialSelectionEvidence;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "EvidenceUploadTrialSmallClaim",
            access = {DefaultAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class}
    )
    private  List<EvidenceUploadTrial> trialSelectionEvidenceSmallClaim;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class}
    )
    private  List<EvidenceUploadTrial> trialSelectionEvidenceRes;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "EvidenceUploadTrialSmallClaim",
            access = {DefaultAccess.class, RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class}
    )
    private  List<EvidenceUploadTrial> trialSelectionEvidenceSmallClaimRes;
    //applicant
    @CCD(
            label = "Disclosure list",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadDocumentOnly",
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruPlus7RolesAvzornAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentDisclosureList;
    @CCD(
            label = "Documents for disclosure",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruPlus7RolesAvzornAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentForDisclosure;
    @CCD(
            label = "Witness statement",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceWitness1",
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruPlus7RolesAvzornAccess.class}
    )
    private  List<Element<UploadEvidenceWitness>> documentWitnessStatement;
    @CCD(
            label = "Witness summary",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceWitness2",
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruPlus7RolesAvzornAccess.class}
    )
    private  List<Element<UploadEvidenceWitness>> documentWitnessSummary;
    @CCD(
            label = "Notice of the intention to rely on hearsay evidence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceWitness3",
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruPlus7RolesAvzornAccess.class}
    )
    private  List<Element<UploadEvidenceWitness>> documentHearsayNotice;
    @CCD(
            label = "Documents referred to in the statement",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceDocumentTypeWithName",
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruPlus7RolesAvzornAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentReferredInStatement;
    @CCD(
            label = "Expert's report",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceExpert1",
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruPlus7RolesAvzornAccess.class}
    )
    private  List<Element<UploadEvidenceExpert>> documentExpertReport;
    @CCD(
            label = "Joint Statement of Experts / Single Joint Expert Report",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceExpert2",
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruPlus7RolesAvzornAccess.class}
    )
    private  List<Element<UploadEvidenceExpert>> documentJointStatement;
    @CCD(
            label = "Questions asked of other party expert",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceExpert3",
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruPlus7RolesAvzornAccess.class}
    )
    private  List<Element<UploadEvidenceExpert>> documentQuestions;
    @CCD(
            label = "Answers to questions asked by the other party",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceExpert4",
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruPlus7RolesAvzornAccess.class}
    )
    private  List<Element<UploadEvidenceExpert>> documentAnswers;
    @CCD(
            label = "Case summary",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadDocumentOnly",
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruPlus7RolesAvzornAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentCaseSummary;
    @CCD(
            label = "Skeleton argument",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadDocumentOnly",
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruPlus7RolesAvzornAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentSkeletonArgument;
    @CCD(
            label = "Authorities",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadDocumentOnly",
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruPlus7RolesAvzornAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentAuthorities;
    @CCD(
            label = "Costs",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadDocumentOnly",
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruPlus7RolesAvzornAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentCosts;
    @CCD(
            label = "Documentary evidence for trial",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruPlus7RolesAvzornAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrial;
    //applicant2
    @CCD(
            label = "Disclosure list",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadDocumentOnly",
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, RESSOLTWOUNSPECPROFILERPlus3RolesWplpvdAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentDisclosureListApp2;
    @CCD(
            label = "Documents for disclosure",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, RESSOLTWOUNSPECPROFILERPlus3RolesWplpvdAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentForDisclosureApp2;
    @CCD(
            label = "Witness statement",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceWitness1",
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, RESSOLTWOUNSPECPROFILERPlus3RolesWplpvdAccess.class}
    )
    private  List<Element<UploadEvidenceWitness>> documentWitnessStatementApp2;
    @CCD(
            label = "Witness summary",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceWitness2",
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, RESSOLTWOUNSPECPROFILERPlus3RolesWplpvdAccess.class}
    )
    private  List<Element<UploadEvidenceWitness>> documentWitnessSummaryApp2;
    @CCD(
            label = "Notice of the intention to rely on hearsay evidence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceWitness3",
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, RESSOLTWOUNSPECPROFILERPlus3RolesWplpvdAccess.class}
    )
    private  List<Element<UploadEvidenceWitness>> documentHearsayNoticeApp2;
    @CCD(
            label = "Documents referred to in the statement",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceDocumentTypeWithName",
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, RESSOLTWOUNSPECPROFILERPlus3RolesWplpvdAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentReferredInStatementApp2;
    @CCD(
            label = "Expert's report",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceExpert1",
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, RESSOLTWOUNSPECPROFILERPlus3RolesWplpvdAccess.class}
    )
    private  List<Element<UploadEvidenceExpert>> documentExpertReportApp2;
    @CCD(
            label = "Joint Statement of Experts / Single Joint Expert Report",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceExpert2",
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, RESSOLTWOUNSPECPROFILERPlus3RolesWplpvdAccess.class}
    )
    private  List<Element<UploadEvidenceExpert>> documentJointStatementApp2;
    @CCD(
            label = "Questions asked of other party expert",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceExpert3",
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, RESSOLTWOUNSPECPROFILERPlus3RolesWplpvdAccess.class}
    )
    private  List<Element<UploadEvidenceExpert>> documentQuestionsApp2;
    @CCD(
            label = "Answers to questions asked by the other party",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceExpert4",
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, RESSOLTWOUNSPECPROFILERPlus3RolesWplpvdAccess.class}
    )
    private  List<Element<UploadEvidenceExpert>> documentAnswersApp2;
    @CCD(
            label = "Case summary",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadDocumentOnly",
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, RESSOLTWOUNSPECPROFILERPlus3RolesWplpvdAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentCaseSummaryApp2;
    @CCD(
            label = "Skeleton argument",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadDocumentOnly",
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, RESSOLTWOUNSPECPROFILERPlus3RolesWplpvdAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentSkeletonArgumentApp2;
    @CCD(
            label = "Authorities",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadDocumentOnly",
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, RESSOLTWOUNSPECPROFILERPlus3RolesWplpvdAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentAuthoritiesApp2;
    @CCD(
            label = "Costs",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadDocumentOnly",
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, RESSOLTWOUNSPECPROFILERPlus3RolesWplpvdAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentCostsApp2;
    @CCD(
            label = "Documentary evidence for trial",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, RESSOLTWOUNSPECPROFILERPlus3RolesWplpvdAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrialApp2;
    @CCD(
            label = "upload date",
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, RESSOLTWOUNSPECPROFILERAccess.class}
    )
    private  LocalDateTime caseDocumentUploadDate;
    //respondent
    @CCD(
            label = "Disclosure list",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadDocumentOnly",
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess.class, CITIZENCLAIMANTPROFILERPlus3RolesMtlnhzAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentDisclosureListRes;
    @CCD(
            label = "Documents for disclosure",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess.class, CITIZENCLAIMANTPROFILERPlus3RolesMtlnhzAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentForDisclosureRes;
    @CCD(
            label = "Witness statement",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceWitness1",
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess.class, CITIZENCLAIMANTPROFILERPlus3RolesMtlnhzAccess.class}
    )
    private  List<Element<UploadEvidenceWitness>> documentWitnessStatementRes;
    @CCD(
            label = "Witness summary",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceWitness2",
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess.class, CITIZENCLAIMANTPROFILERPlus3RolesMtlnhzAccess.class}
    )
    private  List<Element<UploadEvidenceWitness>> documentWitnessSummaryRes;
    @CCD(
            label = "Notice of the intention to rely on hearsay evidence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceWitness3",
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess.class, CITIZENCLAIMANTPROFILERPlus3RolesMtlnhzAccess.class}
    )
    private  List<Element<UploadEvidenceWitness>> documentHearsayNoticeRes;
    @CCD(
            label = "Documents referred to in the statement",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceDocumentTypeWithName",
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess.class, CITIZENCLAIMANTPROFILERPlus3RolesMtlnhzAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentReferredInStatementRes;
    @CCD(
            label = "Expert's report",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceExpert1",
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess.class, CITIZENCLAIMANTPROFILERPlus3RolesMtlnhzAccess.class}
    )
    private  List<Element<UploadEvidenceExpert>> documentExpertReportRes;
    @CCD(
            label = "Joint Statement of Experts / Single Joint Expert Report",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceExpert2",
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess.class, CITIZENCLAIMANTPROFILERPlus3RolesMtlnhzAccess.class}
    )
    private  List<Element<UploadEvidenceExpert>> documentJointStatementRes;
    @CCD(
            label = "Questions asked of other party expert",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceExpert3",
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess.class, CITIZENCLAIMANTPROFILERPlus3RolesMtlnhzAccess.class}
    )
    private  List<Element<UploadEvidenceExpert>> documentQuestionsRes;
    @CCD(
            label = "Answers to questions asked by the other party",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceExpert4",
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess.class, CITIZENCLAIMANTPROFILERPlus3RolesMtlnhzAccess.class}
    )
    private  List<Element<UploadEvidenceExpert>> documentAnswersRes;
    @CCD(
            label = "Case summary",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadDocumentOnly",
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess.class, CITIZENCLAIMANTPROFILERPlus3RolesMtlnhzAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentCaseSummaryRes;
    @CCD(
            label = "Skeleton argument",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadDocumentOnly",
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess.class, CITIZENCLAIMANTPROFILERPlus3RolesMtlnhzAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentSkeletonArgumentRes;
    @CCD(
            label = "Authorities",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadDocumentOnly",
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess.class, CITIZENCLAIMANTPROFILERPlus3RolesMtlnhzAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentAuthoritiesRes;
    @CCD(
            label = "Costs",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadDocumentOnly",
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess.class, CITIZENCLAIMANTPROFILERPlus3RolesMtlnhzAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentCostsRes;
    @CCD(
            label = "Documentary evidence for trial",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess.class, CITIZENCLAIMANTPROFILERPlus3RolesMtlnhzAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrialRes;
    //these fields are shown if the solicitor is for respondent 2 and respondents have different solicitors
    @CCD(
            label = "Disclosure list",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadDocumentOnly",
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentDisclosureListRes2;
    @CCD(
            label = "Documents for disclosure",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentForDisclosureRes2;
    @CCD(
            label = "Witness statement",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceWitness1",
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess.class}
    )
    private  List<Element<UploadEvidenceWitness>> documentWitnessStatementRes2;
    @CCD(
            label = "Witness summary",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceWitness2",
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess.class}
    )
    private  List<Element<UploadEvidenceWitness>> documentWitnessSummaryRes2;
    @CCD(
            label = "Notice of the intention to rely on hearsay evidence",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceWitness3",
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess.class}
    )
    private  List<Element<UploadEvidenceWitness>> documentHearsayNoticeRes2;
    @CCD(
            label = "Documents referred to in the statement",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceDocumentTypeWithName",
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentReferredInStatementRes2;
    @CCD(
            label = "Expert's report",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceExpert1",
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess.class}
    )
    private  List<Element<UploadEvidenceExpert>> documentExpertReportRes2;
    @CCD(
            label = "Joint Statement of Experts / Single Joint Expert Report",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceExpert2",
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess.class}
    )
    private  List<Element<UploadEvidenceExpert>> documentJointStatementRes2;
    @CCD(
            label = "Questions asked of other party expert",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceExpert3",
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess.class}
    )
    private  List<Element<UploadEvidenceExpert>> documentQuestionsRes2;
    @CCD(
            label = "Answers to questions asked by the other party",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceExpert4",
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess.class}
    )
    private  List<Element<UploadEvidenceExpert>> documentAnswersRes2;
    @CCD(
            label = "Case summary",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadDocumentOnly",
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentCaseSummaryRes2;
    @CCD(
            label = "Skeleton argument",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadDocumentOnly",
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentSkeletonArgumentRes2;
    @CCD(
            label = "Authorities",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadDocumentOnly",
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentAuthoritiesRes2;
    @CCD(
            label = "Costs",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadDocumentOnly",
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentCostsRes2;
    @CCD(
            label = "Documentary evidence for trial",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERPlus5RolesKvhyhiAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> documentEvidenceForTrialRes2;
    @CCD(
            label = "upload date",
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECruAccess.class, RESSOLTWOUNSPECPROFILECruAccess.class}
    )
    private  LocalDateTime caseDocumentUploadDateRes;
    @CCD(
            label = "Hearing notes for listing officer",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private  HearingNotes hearingNotes;
    @CCD(
            label = "Claimant uploaded documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceDocumentBundles",
            access = {APPSOLSPECPROFILECudPlus8RolesZezyuzAccess.class, CaseworkerCivilSolicitorCudAccess.class, JudgeProfileCudAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> applicantDocsUploadedAfterBundle;
    @CCD(
            label = "Defendant uploaded documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceDocumentBundles",
            access = {APPSOLSPECPROFILECudPlus8RolesZezyuzAccess.class, CaseworkerCivilSolicitorCudAccess.class, JudgeProfileCudAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> respondentDocsUploadedAfterBundle;
    @CCD(
            label = "Bundle",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UploadEvidenceBundle",
            access = {RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class, CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, HearingScheduleAccessCuAccess.class}
    )
    private  List<Element<UploadEvidenceDocumentType>> bundleEvidence;

    /* Final Orders */
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
    )
    private YesOrNo finalOrderMadeSelection;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class})
    private OrderMade finalOrderDateHeardComplex;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class})
    private List<FinalOrdersJudgePapers> finalOrderJudgePapers;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "FinalOrderJudgeHeardShowHide",
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
    )
    private List<FinalOrderToggle> finalOrderJudgeHeardFrom;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class})
    private FinalOrderRepresentation finalOrderRepresentation;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "FinalOrderJudgeHeardShowHide",
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
    )
    private List<FinalOrderToggle> finalOrderRecitals;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class})
    private FinalOrderRecitalsRecorded finalOrderRecitalsRecorded;
    @CCD(
            label = " ",
            hint = "Please insert paragraph numbers before each term of the Order as you wish them to appear, as they will not be automatically generated",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
    )
    private String finalOrderOrderedThatText;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class})
    private AssistedCostTypesList assistedOrderCostList;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class})
    private AssistedOrderCostDetails assistedOrderCostsReserved;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class})
    private AssistedOrderCostDetails assistedOrderCostsBespoke;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class})
    private AssistedOrderCostDetails assistedOrderMakeAnOrderForCosts;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
    )
    private YesOrNo publicFundingCostsProtection;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "FinalOrderJudgeHeardShowHide",
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
    )
    private List<FinalOrderToggle> finalOrderFurtherHearingToggle;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class})
    private FinalOrderFurtherHearing finalOrderFurtherHearingComplex;
    @CCD(ignore = true)
    private HearingLengthFinalOrderList lengthList;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "FinalOrderJudgeHeardShowHide",
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
    )
    private List<FinalOrderToggle> finalOrderAppealToggle;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class})
    private FinalOrderAppeal finalOrderAppealComplex;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class})
    private OrderMadeOnTypes orderMadeOnDetailsList;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class})
    private OrderMadeOnDetails orderMadeOnDetailsOrderCourt;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class})
    private OrderMadeOnDetailsOrderWithoutNotice orderMadeOnDetailsOrderWithoutNotice;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "AssistedOrderPenalNoticeToggle",
            access = {JudgeProfileCruAccess.class}
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<FinalOrderToggle> assistedOrderPenalNoticeToggle;
    @CCD(
            label = "Note: Paragraph numbers and the parties' name to which the penal notice applies must be entered in the box below.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {JudgeProfileCruAccess.class}
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String assistedOrderPenalNoticeContent;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
    )
    private YesOrNo finalOrderGiveReasonsYesNo;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class})
    private AssistedOrderReasons finalOrderGiveReasonsComplex;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
    )
    private YesOrNo finalOrderAllocateToTrack;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
    )
    private YesOrNo allowOrderTrackAllocation;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class})
    private AllocatedTrack finalOrderTrackAllocation;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class})
    private FinalOrdersComplexityBand finalOrderIntermediateTrackComplexityBand;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
    )
    private DynamicList finalOrderDownloadTemplateOptions;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class})
    private CaseDocument finalOrderDownloadTemplateDocument;
    @CCD(
            label = " ",
            regex = ".doc,.docx",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
    )
    private Document uploadOrderDocumentFromTemplate;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class})
    private String finalOrderTrackToggle;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class})
    private OrderAfterHearingDate orderAfterHearingDate;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
    )
    private YesOrNo showOrderAfterHearingDatePage;

    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class})
    private  FinalOrderSelection finalOrderSelection;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
    )
    private  String freeFormRecordedTextArea;
    @CCD(
            label = " ",
            hint = "Please insert paragraph numbers before each term of the Order as you wish them to appear, as they will not be automatically generated",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
    )
    private  String freeFormOrderedTextArea;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class})
    private  FreeFormOrderValues orderOnCourtInitiative;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class})
    private  FreeFormOrderValues orderWithoutNotice;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class})
    private  OrderOnCourtsList orderOnCourtsList;
    @CCD(
            label = " ",
            hint = "For example, potentially violent, reading time needed, CVP for hearing",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
    )
    private  String freeFormHearingNotes;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class})
    private CaseDocument finalOrderDocument;
    @CCD(
            label = "System generated Case Documents",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCuAccess.class, JudgeProfileCuAccess.class, WluAdminCuAccess.class}
    )
    @Builder.Default
    private List<Element<CaseDocument>> finalOrderDocumentCollection = new ArrayList<>();

    // Court officer order
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRCourtOfficerOrderCruAccess.class})
    private FinalOrderFurtherHearing courtOfficerFurtherHearingComplex;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerCivilSystemFieldReaderRCourtOfficerOrderCruAccess.class}
    )
    private String courtOfficerOrdered;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRCourtOfficerOrderCruAccess.class}
    )
    private YesOrNo courtOfficerGiveReasonsYesNo;
    @CCD(
            label = "Order document",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilSystemFieldReaderRCourtOfficerOrderCruAccess.class, CaseworkerCivilSolicitorRAccess.class}
    )
    private CaseDocument previewCourtOfficerOrder;

    //Hearing Scheduled
    @CCD(
            label = " ",
            hint = "Venue",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerCivilSystemFieldReaderRHearingScheduleAccessCruAccess.class, CITIZENDEFENDANTPROFILECruCaseworkerCivilSystemupdateRAccess.class, CaseworkerCivilSolicitorCruAccess.class}
    )
    private DynamicList hearingLocation;
    @CCD(
            label = "Date of application",
            hint = "For example, 27 3 2022",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRHearingScheduleAccessCruAccess.class}
    )
    private LocalDate dateOfApplication;
    @CCD(
            label = " ",
            hint = "For example, 27 3 2022",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECruAccess.class, CaseworkerCivilSolicitorCrudAccess.class, CaseworkerCivilStaffCruAccess.class, CaseworkerCivilSystemupdateCrudAccess.class, HearingScheduleAccessCrudAccess.class}
    )
    private LocalDate hearingDate;
    @CCD(
            label = "Hearing Due Date",
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCrudAccess.class, CITIZENCLAIMANTPROFILERAccess.class, HearingScheduleAccessCrudAccess.class}
    )
    private LocalDate hearingDueDate;
    @CCD(
            label = "Start time",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "HearingTimeHourMinute",
            access = {CaseworkerCivilSystemFieldReaderRHearingScheduleAccessCruAccess.class, CITIZENDEFENDANTPROFILECruAccess.class, CaseworkerCivilSolicitorCruAccess.class}
    )
    private String hearingTimeHourMinute;
    @CCD(
            label = "Hearing reference",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilSystemupdateRHearingScheduleAccessCruAccess.class, CaseworkerCivilSolicitorRAccess.class}
    )
    private String hearingReferenceNumber;
    @CCD(
            label = "Is this a listing or a relisting?",
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilSystemupdateRHearingScheduleAccessCruAccess.class, CaseworkerCivilSolicitorRAccess.class}
    )
    private ListingOrRelisting listingOrRelisting;
    @CCD(
            label = "What hearing notice do you want to create?",
            hint = "Select one option.",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRHearingScheduleAccessCruAccess.class}
    )
    private HearingNoticeList hearingNoticeList;
    @CCD(
            label = "Hearing Fee",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRHearingScheduleAccessCruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
    )
    private Fee hearingFee;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRHearingScheduleAccessCruAccess.class}
    )
    private HearingChannel channel;
    @CCD(
            label = "Duration",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "HearingLengthCasePro",
            access = {CaseworkerCivilSystemFieldReaderRHearingScheduleAccessCruAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilSolicitorCruAccess.class}
    )
    private HearingDuration hearingDuration;
    @CCD(
            label = "Duration",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerCivilSystemFieldReaderRHearingScheduleAccessCruAccess.class, CaseworkerCivilSolicitorCruAccess.class}
    )
    private String hearingDurationMinti;
    @CCD(
            label = "Is there more information you need to add to the Hearing Notification letter?",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerCivilSystemFieldReaderRHearingScheduleAccessCruAccess.class}
    )
    private String information;
    @CCD(
            label = "What hearing notice are you creating?",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRHearingScheduleAccessCruAccess.class}
    )
    private String hearingNoticeListOther;
    @CCD(
            label = "Date of dismissal",
            hint = "For example, 27 3 2022",
            searchable = false,
            access = {CaseworkerCivilAdminCrudCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCrudAccess.class, CaseworkerCivilSolicitorRAccess.class}
    )
    private LocalDateTime caseDismissedHearingFeeDueDate;
    @CCD(
            label = "Duration AHN",
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECruAccess.class, CitizenProfileCruAccess.class}
    )
    private String hearingDurationInMinutesAHN;
    @CCD(
            label = "trial doc date",
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECruAccess.class, CitizenProfileCruAccess.class}
    )
    private LocalDateTime claimantTrialReadyDocumentCreated;
    @CCD(
            label = "trial doc date",
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECruAccess.class, CitizenProfileCruAccess.class}
    )
    private LocalDateTime defendantTrialReadyDocumentCreated;

    //Trial Readiness
    @CCD(
            label = "Was trial ready notification sent out?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILERAccess.class}
    )
    private YesOrNo trialReadyNotified;
    @CCD(
            label = "Was trial ready notification sent out?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILERAccess.class}
    )
    private YesOrNo trialReadyChecked;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, RESSOLTWOUNSPECPROFILERAccess.class}
    )
    private YesOrNo trialReadyApplicant;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECruAccess.class, RESSOLTWOUNSPECPROFILECruAccess.class}
    )
    private YesOrNo trialReadyRespondent1;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, RESSOLTWOSPECPROFILERESSOLTWOUNSPECPROFILECruAccess.class}
    )
    private YesOrNo trialReadyRespondent2;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILECruAccess.class}
    )
    private RevisedHearingRequirements applicantRevisedHearingRequirements;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLONEUNSPECPROFILECruAccess.class, CITIZENDEFENDANTPROFILECruAccess.class}
    )
    private RevisedHearingRequirements respondent1RevisedHearingRequirements;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, RESSOLTWOSPECPROFILERESSOLTWOUNSPECPROFILECruAccess.class}
    )
    private RevisedHearingRequirements respondent2RevisedHearingRequirements;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILECruAccess.class}
    )
    private HearingOtherComments applicantHearingOtherComments;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLONEUNSPECPROFILECruAccess.class, CITIZENDEFENDANTPROFILECruAccess.class}
    )
    private HearingOtherComments respondent1HearingOtherComments;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, RESSOLTWOSPECPROFILERESSOLTWOUNSPECPROFILECruAccess.class}
    )
    private HearingOtherComments respondent2HearingOtherComments;
    @CCD(
            label = "System generated Case Documents",
            searchable = false,
            access = {APPSOLSPECPROFILECuPlus3RolesFtetnzAccess.class, RESSOLONESPECPROFILERESSOLONEUNSPECPROFILECuAccess.class, RESSOLTWOSPECPROFILERESSOLTWOUNSPECPROFILECuAccess.class, CuiAdminProfileCuiNbcProfileHearingScheduleAccessJudgeProfileCuAccess.class, CaseworkerCivilAdminCuAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    @Builder.Default
    private  List<Element<CaseDocument>> trialReadyDocuments = new ArrayList<>();

    // // MINTI case prog
    @CCD(
            label = "Hearing type",
            searchable = false,
            typeOverride = FieldType.DynamicRadioList,
            access = {CaseworkerCivilSystemFieldReaderRHearingScheduleAccessCruAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private DynamicList requestHearingNoticeDynamic;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRHearingScheduleAccessCruAccess.class, CaseworkerCivilJudgeProfileRAccess.class}
    )
    private  List<ConfirmListingTickBox> confirmListingTickBox;
    @CCD(
            label = "Task management location",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, GSProfileCaseworkerCivilSystemupdateCaseworkerRasValidationRAccess.class, CaseworkerCivilSolicitorJudgeProfileCruAccess.class}
    )
    private TaskManagementLocationTypes taskManagementLocations;
    @CCD(
            label = "Task management locations",
            access = {CaseworkerCivilAdminJudgeProfileCuAccess.class, CaseworkerCivilSolicitorCuCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerRasValidationRAccess.class}
    )
    private TaskManagementLocationTab taskManagementLocationsTab;
    @CCD(
            label = "Case management location",
            access = {CaseworkerCivilAdminJudgeProfileCuAccess.class, CaseworkerCivilSolicitorCuCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerRasValidationRAccess.class}
    )
    private TaskManagementLocationTab caseManagementLocationTab;
    @CCD(
            label = "Hearing type",
            searchable = false,
            typeOverride = FieldType.DynamicRadioList,
            access = {CaseworkerCivilSystemFieldReaderRHearingScheduleAccessCruAccess.class, CaseworkerCivilJudgeProfileRAccess.class}
    )
    private DynamicList hearingListedDynamicList;

    //case progression
    @CCD(
            label = "Upload a document",
            hint = "Each document must be less than 100MB. You can upload the following file types: Word, Excel, PowerPoint, PDF, RTF, TXT, CSV, JPG, PNG, BMP, TIF, TIFF",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentAndName",
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCuAccess.class}
    )
    private  List<Element<DocumentWithName>> documentAndName;
    @CCD(
            label = "Upload a document",
            hint = "Each document must be less than 100MB. You can upload the following file types: Word, Excel, PowerPoint, PDF, RTF, TXT, CSV, JPG, PNG, BMP, TIF, TIFF",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentAndName",
            access = {CaseworkerCivilSystemFieldReaderRAccess.class, JudgeProfileCrudAccess.class}
    )
    private  List<Element<DocumentWithName>> documentAndNameToAdd;
    @CCD(
            label = "Upload a document and case note",
            hint = "Each document must be less than 100MB, You can upload the following file types: Word, Excel, PowerPoint, PDF, RTF, TXT, CSV, JPG, PNG, BMP, TIF, TIFF",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCuAccess.class}
    )
    private  List<Element<DocumentAndNote>> documentAndNote;
    @CCD(
            label = "Upload a document",
            hint = "Each document must be less than 100MB, You can upload the following file types: Word, Excel, PowerPoint, PDF, RTF, TXT, CSV, JPG, PNG, BMP, TIF, TIFF",
            access = {CaseworkerCivilSystemFieldReaderRAccess.class, JudgeProfileCrudAccess.class}
    )
    private  List<Element<DocumentAndNote>> documentAndNoteToAdd;
    @CCD(
            label = "This case note is only visible to you and other judges",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
    )
    private  CaseNoteType caseNoteType;
    @CCD(
            label = "Write a note about this case",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
    )
    private  String caseNoteTA;
    @CCD(
            label = "Write a note about this case",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCuAccess.class}
    )
    private  List<Element<CaseNote>> caseNotesTA;
    @CCD(label = "Note was added on", searchable = false, access = {CaseworkerCivilSystemFieldReaderRAccess.class})
    private  LocalDateTime noteAdditionDateTime;
    @CCD(
            label = " ",
            searchable = false,
            access = {RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class}
    )
    private  String caseTypeFlag;
    @CCD(
            label = " ",
            searchable = false,
            access = {RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private  String witnessStatementFlag;
    @CCD(
            label = " ",
            searchable = false,
            access = {RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private  String witnessSummaryFlag;
    @CCD(
            label = " ",
            searchable = false,
            access = {RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private  String witnessReferredStatementFlag;
    @CCD(
            label = " ",
            searchable = false,
            access = {RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private  String expertReportFlag;
    @CCD(
            label = " ",
            searchable = false,
            access = {RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private  String expertJointFlag;
    @CCD(
            label = " ",
            searchable = false,
            access = {RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private  String trialAuthorityFlag;
    @CCD(
            label = " ",
            searchable = false,
            access = {RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private  String trialCostsFlag;
    @CCD(
            label = " ",
            searchable = false,
            access = {RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private  String trialDocumentaryFlag;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseflagsAdminCrudPlus3RolesUzqffsAccess.class, GSProfileRAccess.class, CaseworkerCivilSystemupdateCrudAccess.class}
    )
    private  YesOrNo urgentFlag;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class}
    )
    private  String caseProgAllocatedTrack;
    @CCD(
            label = "Select one of the options",
            searchable = false,
            typeOverride = FieldType.DynamicRadioList,
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECrudAccess.class, CaseworkerCivilAdminCrudAccess.class}
    )
    private  DynamicList evidenceUploadOptions;

    @CCD(
            label = "registration information respondent one",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "RegistrationTypeInformation",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilStaffRAccess.class}
    )
    private  List<Element<RegistrationInformation>> registrationTypeRespondentOne;
    @CCD(
            label = "registration information respondent two",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "RegistrationTypeInformation",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilStaffRAccess.class}
    )
    private  List<Element<RegistrationInformation>> registrationTypeRespondentTwo;

    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, GSProfileRAccess.class}
    )
    private  String respondent1DocumentURL;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, GSProfileRAccess.class}
    )
    private  String respondent2DocumentURL;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, GSProfileRAccess.class}
    )
    private  String respondent2DocumentGeneration;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCuiAdminProfileCruAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilSystemupdateRAccess.class, CitizenProfileCruAccess.class}
    )
    private  String hearingHelpFeesReferenceNumber;

    @CCD(ignore = true)
    private  String hearingLocationCourtName;
    // bulk claims
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
    )
    private  String bulkCustomerId;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
    )
    private  String sdtRequestIdFromSdt;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Text",
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
    )
    private  List<Element<String>> sdtRequestId;

    //Judgments Online
    @CCD(
            label = "Why is this judgment being recorded?",
            searchable = false,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private JudgmentRecordedReason joJudgmentRecordReason;
    @CCD(
            label = "Enter the date the determination was made or the date the judge made the order",
            hint = "For example, 16 04 2021",
            searchable = false,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private LocalDate joOrderMadeDate;
    @CCD(
            label = " ",
            searchable = false,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private LocalDate joIssuedDate;
    @CCD(
            label = "Enter the amount of the judgment ordered",
            hint = "For example £451.00",
            searchable = false,
            typeOverride = FieldType.MoneyGBP,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private String joAmountOrdered;
    @CCD(
            label = "Enter the amount of the costs ordered",
            hint = "For example £50.00",
            searchable = false,
            typeOverride = FieldType.MoneyGBP,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private String joAmountCostOrdered;
    @CCD(
            label = "Should this judgment be registered with RTL?",
            hint = "This judgment should only be registered with RTL if the judge has ordered the amount should be paid in instalments, or if the claimant has applied to enforce the judgment",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private YesOrNo joIsRegisteredWithRTL;
    @CCD(
            label = "Select how the judgment will be paid",
            searchable = false,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private JudgmentPaymentPlan joPaymentPlan;
    @CCD(
            label = " ",
            searchable = false,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private JudgmentInstalmentDetails joInstalmentDetails;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            gate = "!CCD_DEF_ENV:prod",
            access = {CITIZENCLAIMANTPROFILECuPlus2RolesSzdyetAccess.class, CaseworkerCivilAdminCaseworkerCivilStaffCuAccess.class, CITIZENDEFENDANTPROFILECuAccess.class, CitizenProfileCuAccess.class}
    )
    private YesOrNo joIsLiveJudgmentExists;
    @CCD(
            label = " ",
            searchable = false,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, APPSOLSPECPROFILECITIZENCLAIMANTPROFILECruAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private JudgmentPaidInFull joJudgmentPaidInFull;
    @CCD(
            label = "Why is this judgment being set aside?",
            searchable = false,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private JudgmentSetAsideReason joSetAsideReason;
    @CCD(
            label = "Enter any additional information. This will be shown to the parties in the case.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private String joSetAsideJudgmentErrorText;
    @CCD(
            label = "Which type of order has been made?",
            searchable = false,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private JudgmentSetAsideOrderType joSetAsideOrderType;
    @CCD(
            label = "Enter the date of the order setting aside the judgment",
            hint = "For example, 16 04 2021",
            searchable = false,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private LocalDate joSetAsideOrderDate;
    @CCD(
            label = "Enter the date of the application to set aside",
            hint = "For example, 16 04 2021",
            searchable = false,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private LocalDate joSetAsideApplicationDate;
    @CCD(
            label = "Enter the date of the defence was received",
            hint = "For example, 16 04 2021",
            searchable = false,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private LocalDate joSetAsideDefenceReceivedDate;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private YesOrNo joShowRegisteredWithRTLOption;
    @CCD(
            label = " ",
            searchable = false,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateCruAccess.class, CITIZENDEFENDANTPROFILERAccess.class, CitizenProfileRAccess.class}
    )
    private JudgmentDetails activeJudgment;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "JoJudgment",
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateCruAccess.class}
    )
    private List<Element<JudgmentDetails>> historicJudgment;
    @CCD(
            label = "Enter the date set aside is done",
            searchable = false,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCuAccess.class, CaseworkerCivilAdminCaseworkerCivilStaffCuAccess.class, CITIZENCLAIMANTPROFILECuAccess.class}
    )
    private LocalDateTime joSetAsideCreatedDate;

    @CCD(
            label = "Defendant 1",
            searchable = false,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilSolicitorCuAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private String joDefendantName1;
    @CCD(
            label = "Defendant 2",
            searchable = false,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilSolicitorCuAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private String joDefendantName2;
    @CCD(
            label = "Payment type",
            searchable = false,
            typeOverride = FieldType.Text,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilSolicitorCuAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private PaymentPlanSelection joPaymentPlanSelected;
    @CCD(
            label = "Regular payments of",
            hint = "For example, £10",
            searchable = false,
            typeOverride = FieldType.MoneyGBP,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilSolicitorCuAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private String joRepaymentAmount;
    @CCD(
            label = "Date for first instalment",
            hint = "This must be after ${currentDatebox}",
            searchable = false,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilSolicitorCuAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private LocalDate joRepaymentStartDate;
    @CCD(
            label = "How often do you want to receive payments?",
            searchable = false,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilSolicitorCuAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private PaymentFrequency joRepaymentFrequency;
    @CCD(
            label = "Date judgment order is issued",
            searchable = false,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilSolicitorCuAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private LocalDate joIssueDate;
    @CCD(
            label = "Status of active judgment",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "JudgmentState",
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilSolicitorCuAccess.class, CaseworkerCivilStaffCruAccess.class, CaseworkerCivilSystemupdateCuAccess.class}
    )
    private JudgmentState joState;
    @CCD(
            label = "Date judgment amount paid in full",
            searchable = false,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilSolicitorCuAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private LocalDate joFullyPaymentMadeDate;
    @CCD(
            label = " ",
            searchable = false,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class}
    )
    private LocalDateTime joMarkedPaidInFullIssueDate;
    @CCD(
            label = " ",
            searchable = false,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class}
    )
    private LocalDateTime joDefendantMarkedPaidInFullIssueDate;
    @CCD(
            label = " ",
            searchable = false,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILECruAccess.class, CitizenProfileCruAccess.class}
    )
    private LocalDateTime joJudgementByAdmissionIssueDate;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "CoscRpaStatus",
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class}
    )
    private CoscRPAStatus joCoscRpaStatus;
    @CCD(
            label = "Claim amount",
            searchable = false,
            typeOverride = FieldType.MoneyGBP,
            gate = "!CCD_DEF_ENV:prod",
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private String joOrderedAmount;
    @CCD(
            label = "Claim fee amount",
            searchable = false,
            typeOverride = FieldType.MoneyGBP,
            gate = "!CCD_DEF_ENV:prod",
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private String joCosts;
    @CCD(
            label = "Total amount owed",
            searchable = false,
            typeOverride = FieldType.MoneyGBP,
            gate = "!CCD_DEF_ENV:prod",
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private String joTotalAmount;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilSolicitorCuAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private YesOrNo joIsDisplayInJudgmentTab;
    @CCD(
            label = " ",
            searchable = false,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilSolicitorCuAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private String joRepaymentSummaryObject;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private YesOrNo respondForImmediateOption;
    @CCD(
            label = " ",
            gate = "!CCD_DEF_ENV:prod",
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateCruAccess.class, CitizenProfileCruAccess.class}
    )
    private LocalDateTime joDJCreatedDate;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo, gate = "!CCD_DEF_ENV:prod")
    private YesOrNo isJoRequested;

    @CCD(
            label = "The case should be sent to another hearing centre for directions",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCuAccess.class, LegalAdviserCuAccess.class}
    )
    private  TransferCaseDetails transferCaseDetails;

    //SDO-R2
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRCitizenProfileCruAccess.class, CaseworkerCivilSolicitorCruAccess.class}
    )
    private YesOrNo isFlightDelayClaim;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCitizenProfileCruAccess.class, CaseworkerCivilSolicitorCruAccess.class}
    )
    private FlightDelayDetails flightDelayDetails;
    @CCD(
            label = "Reason for reconsideration of directions order by a Legal Advisor",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCuAccess.class, APPSOLSPECPROFILECruAccess.class, RESSOLONESPECPROFILECrAccess.class, RESSOLTWOSPECPROFILECrAccess.class, JudgeProfileRAccess.class}
    )
    private ReasonForReconsideration reasonForReconsiderationApplicant;
    @CCD(
            label = "Reason for reconsideration of directions order by a Legal Advisor",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCuAccess.class, RESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECrAccess.class, APPSOLSPECPROFILECrAccess.class, JudgeProfileRAccess.class}
    )
    private ReasonForReconsideration reasonForReconsiderationRespondent1;
    @CCD(
            label = "Reason for reconsideration of directions order by a Legal Advisor",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCuAccess.class, RESSOLONESPECPROFILECrRESSOLTWOSPECPROFILECruAccess.class, APPSOLSPECPROFILECrAccess.class, JudgeProfileRAccess.class}
    )
    private ReasonForReconsideration reasonForReconsiderationRespondent2;
    @CCD(
            label = " ",
            searchable = false,
            access = {RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, APPSOLSPECPROFILECruCaseworkerCivilSystemFieldReaderRAccess.class, JudgeProfileRAccess.class}
    )
    private String casePartyRequestForReconsideration;
    @CCD(
            label = "Do you wish to uphold the previous order made?",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class, APPSOLSPECPROFILERPlus5RolesPvjgzlAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILERAccess.class}
    )
    private DecisionOnRequestReconsiderationOptions decisionOnRequestReconsiderationOptions;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class, APPSOLSPECPROFILERPlus5RolesPvjgzlAccess.class}
    )
    private UpholdingPreviousOrderReason upholdingPreviousOrderReason;
    @CCD(ignore = true)
    private String dashboardNotificationTypeOrder;
    @CCD(
            label = "View directions order",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilSolicitorRAccess.class, JudgeProfileCruAccess.class, LegalAdviserRAccess.class}
    )
    private CaseDocument decisionOnReconsiderationDocument;
    @CCD(
            label = " ",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private CaseDocument requestForReconsiderationDocument;
    @CCD(
            label = " ",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECruAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private CaseDocument requestForReconsiderationDocumentRes;
    @CCD(
            label = "Deadline Request Reconsideration",
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECruAccess.class, CitizenProfileCruAccess.class}
    )
    private LocalDateTime requestForReconsiderationDeadline;
    @CCD(
            label = "Deadline Request Reconsideration",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECruAccess.class, CitizenProfileCruAccess.class}
    )
    private YesOrNo requestForReconsiderationDeadlineChecked;

    //Settle And Discontinue
    @CCD(
            label = "Does marking this Claim as settled relate to all claimants?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private YesOrNo markPaidForAllClaimants;
    @CCD(
            label = "Select the claimant this relates to",
            searchable = false,
            typeOverride = FieldType.DynamicRadioList,
            access = {APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private DynamicList claimantWhoIsSettling;
    @CCD(
            label = "Which claimants are discontinuing?",
            searchable = false,
            typeOverride = FieldType.DynamicRadioList,
            access = {APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private DynamicList claimantWhoIsDiscontinuing;
    @CCD(
            label = "Which defendant is the discontinuance against?",
            searchable = false,
            typeOverride = FieldType.DynamicRadioList,
            access = {APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private DynamicList discontinuingAgainstOneDefendant;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateCruAccess.class, JudgeProfileRAccess.class}
    )
    private String selectedClaimantForDiscontinuance;
    @CCD(
            label = "Is the court's permission needed to discontinue?",
            hint = "For more information, refer to the Civil Procedure Rules 38.2.",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class, CITIZENDEFENDANTPROFILERAccess.class}
    )
    private SettleDiscontinueYesOrNoList courtPermissionNeeded;
    @CCD(
            label = "Has permission been granted by a Judge to discontinue",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private SettleDiscontinueYesOrNoList isPermissionGranted;
    @CCD(
            label = "Permission granted by:",
            searchable = false,
            access = {APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private PermissionGranted permissionGrantedComplex;
    @CCD(
            label = "Judge name",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private String permissionGrantedJudgeCopy;
    @CCD(
            label = "On date",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private LocalDate permissionGrantedDateCopy;
    @CCD(
            label = "Is this a full or part discontinuance?",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private DiscontinuanceTypeList typeOfDiscontinuance;
    @CCD(
            label = "Which part of the claim do you want to discontinue?",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private String partDiscontinuanceDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateCruAccess.class, CITIZENDEFENDANTPROFILERAccess.class}
    )
    private ConfirmOrderGivesPermission confirmOrderGivesPermission;
    @CCD(
            label = "Is the discontinuance against all defendants?",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private SettleDiscontinueYesOrNoList isDiscontinuingAgainstBothDefendants;
    @CCD(
            label = "How has the claim been settled?",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private SettlementReason settleReason;
    @CCD(
            label = "I confirm this claim should be marked as settled",
            searchable = false,
            access = {APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private  MarkPaidConsentList markPaidConsent;
    @CCD(
            label = "Do all claimants consent to the discontinuance?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private YesOrNo claimantsConsentToDiscontinuance;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class}
    )
    private CaseDocument applicant1NoticeOfDiscontinueCWViewDoc;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class}
    )
    private CaseDocument respondent1NoticeOfDiscontinueCWViewDoc;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class}
    )
    private CaseDocument respondent2NoticeOfDiscontinueCWViewDoc;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class, APPSOLSPECPROFILERAccess.class, APPSOLUNSPECPROFILERAccess.class, JudgeProfileRAccess.class}
    )
    private CaseDocument applicant1NoticeOfDiscontinueAllPartyViewDoc;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class, RESSOLONESPECPROFILERESSOLONEUNSPECPROFILEWluAdminRAccess.class, CITIZENDEFENDANTPROFILERAccess.class, JudgeProfileRAccess.class}
    )
    private CaseDocument respondent1NoticeOfDiscontinueAllPartyViewDoc;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class, RESSOLTWOSPECPROFILERAccess.class, RESSOLTWOUNSPECPROFILERAccess.class, JudgeProfileRAccess.class}
    )
    private CaseDocument respondent2NoticeOfDiscontinueAllPartyViewDoc;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class, APPSOLSPECPROFILERESSOLONESPECPROFILERAccess.class, CITIZENDEFENDANTPROFILERAccess.class, JudgeProfileRAccess.class, WluAdminCruAccess.class}
    )
    private CaseDocument respondent1NoticeOfDiscontinueAllPartyTranslatedDoc;

    @JsonUnwrapped
    private FeePaymentOutcomeDetails feePaymentOutcomeDetails;
    @CCD(label = " ", access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class})
    private LocalDate coscSchedulerDeadline;
    @CCD(
            label = "CoSC Application Status",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "CoscApplicationStatus",
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class}
    )
    private CoscApplicationStatus coSCApplicationStatus;

    //Caseworker events
    @CCD(
            label = "Is there an obligation date?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CuiAdminProfileCruAccess.class}
    )
    private YesOrNo obligationDatePresent;
    @CCD(
            label = "Have all the next steps for court staff been completed?",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CuiAdminProfileCruAccess.class}
    )
    private CourtStaffNextSteps courtStaffNextSteps;
    @CCD(
            label = "Obligation date",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ObligationDataCollection",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilSystemupdateCuiAdminProfileCruAccess.class}
    )
    private List<Element<ObligationData>> obligationData;
    @CCD(
            label = "Obligation date",
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCuAccess.class, CaseworkerCivilAdminCuAccess.class, CuiAdminProfileCuAccess.class}
    )
    private List<Element<StoredObligationData>> storedObligationData;
    @CCD(
            label = "Is this a final order?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilSystemupdateRAccess.class, CuiAdminProfileCruAccess.class}
    )
    private YesOrNo isFinalOrder;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CaseworkerCivilAdminWluAdminCruAccess.class}
    )
    private SendAndReplyOption sendAndReplyOption;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CaseworkerCivilAdminWluAdminCruAccess.class}
    )
    private SendMessageMetadata sendMessageMetadata;
    @CCD(
            label = " ",
            hint = "Explain what you're requesting and why. Include any answers and decision you need.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CaseworkerCivilAdminWluAdminCruAccess.class}
    )
    private String sendMessageContent;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CaseworkerCivilAdminWluAdminCruAccess.class}
    )
    private MessageReply messageReplyMetadata;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CaseworkerCivilAdminWluAdminCruAccess.class}
    )
    private String messageHistory;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Message",
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CaseworkerCivilAdminWluAdminCruAccess.class}
    )
    private DynamicList messagesToReplyTo;
    @CCD(
            label = "Messages",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCuAccess.class, CaseworkerCivilAdminJudgeProfileLegalAdviserCuAccess.class, WluAdminCuAccess.class}
    )
    private List<Element<Message>> messages;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilSystemupdateCuiAdminProfileCruAccess.class}
    )
    private ObligationWAFlag obligationWAFlag;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CaseworkerCivilAdminWluAdminCruAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private Message lastMessage;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CaseworkerCivilAdminWluAdminCruAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private String lastMessageAllocatedTrack;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CaseworkerCivilAdminWluAdminCruAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private String lastMessageJudgeLabel;
    @CCD(ignore = true)
    private String waTaskToCompleteId;

    //QueryManagement
    @CCD(
            label = "Queries",
            searchable = false,
            access = {APPSOLSPECPROFILECudPlus8RolesZezyuzAccess.class, CaseworkerCivilStaffCtscCudAccess.class}
    )
    private  CaseQueriesCollection qmApplicantSolicitorQueries;
    @CCD(
            label = " ",
            searchable = false,
            access = {APPSOLSPECPROFILECudPlus8RolesZezyuzAccess.class, CaseworkerCivilStaffCtscCudAccess.class}
    )
    private  CaseQueriesCollection qmRespondentSolicitor1Queries;
    @CCD(
            label = " ",
            searchable = false,
            access = {APPSOLSPECPROFILECudPlus8RolesZezyuzAccess.class, CaseworkerCivilStaffCtscCudAccess.class}
    )
    private  CaseQueriesCollection qmRespondentSolicitor2Queries;
    @CCD(
            label = "Queries",
            searchable = false,
            access = {RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECruAccess.class, CaseworkerCivilAdminWluAdminCruAccess.class, GSProfileRPlus4RolesOdnovhAccess.class, CaseworkerCivilSystemupdateCudAccess.class}
    )
    private  CaseQueriesCollection queries;
    @CCD(ignore = true)
    private  CaseMessage caseMessage;
    @CCD(label = " ", searchable = false, access = {APPSOLSPECPROFILECruPlus12RolesJypaltAccess.class})
    private  LatestQuery qmLatestQuery;

    /**
     * Claimant has requested a reconsideration of the SDO.
     */
    @CCD(
            label = "Request Recon claimant",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECruAccess.class, CitizenProfileCruAccess.class}
    )
    private YesOrNo orderRequestedForReviewClaimant;
    /**
     * Defendant has requested a reconsideration of the SDO.
     */
    @CCD(
            label = "Request Recon defendant",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECruAccess.class, CitizenProfileCruAccess.class}
    )
    private YesOrNo orderRequestedForReviewDefendant;

    @JsonIgnore
    public String getHearingLocationText() {
        return ofNullable(hearingLocation)
            .map(DynamicList::getValue)
            .map(DynamicListElement::getLabel)
            .orElse(null);
    }
}
