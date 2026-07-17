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
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.ClaimType;
import uk.gov.hmcts.reform.civil.enums.ClaimTypeUnspec;
import uk.gov.hmcts.reform.civil.enums.ConfirmationToggle;
import uk.gov.hmcts.reform.civil.enums.EmploymentTypeCheckboxFixedListLRspec;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.MultiPartyResponseTypeFlags;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.enums.PersonalInjuryType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.enums.ResponseIntention;
import uk.gov.hmcts.reform.civil.enums.SuperClaimType;
import uk.gov.hmcts.reform.civil.enums.TimelineUploadTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.SettleDiscontinueYesOrNoList;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.ResponseOneVOneShowTag;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceInfo;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.CertOfSC;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.documentremoval.DocumentToKeepCollection;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.ExpertRequirements;
import uk.gov.hmcts.reform.civil.model.dq.RecurringExpenseLRspec;
import uk.gov.hmcts.reform.civil.model.dq.RecurringIncomeLRspec;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationTypeLR;
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDateGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAStatementOfTruth;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimFromType;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimOptions;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimUntilType;
import uk.gov.hmcts.reform.civil.model.interestcalc.SameRateInterestSelection;
import uk.gov.hmcts.reform.civil.model.mediation.MediationAvailability;
import uk.gov.hmcts.reform.civil.model.mediation.MediationContactInformation;
import uk.gov.hmcts.reform.civil.model.sdo.OtherDetails;
import uk.gov.hmcts.reform.civil.model.taskmanagement.ClientContext;
import uk.gov.hmcts.reform.civil.model.welshenhancements.ChangeLanguagePreference;
import uk.gov.hmcts.reform.civil.model.welshenhancements.PreTranslationDocumentType;
import uk.gov.hmcts.reform.civil.model.welshenhancements.PreferredLanguage;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.FINISHED;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVOne;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILECaseworkerCivilSystemupdateCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateCuiAdminProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.JudgeProfileRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCruCaseworkerCivilSolicitorRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CuiAdminProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCrudCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CuiAdminProfileCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILECruPlus3RolesKwiaxwAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCaaCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCaaCuPlus2RolesCjdhidAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECuPlus3RolesFtetnzAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminJudgeProfileLegalAdviserCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CivilAdministratorBasicRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CivilAdministratorStandardRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateCudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILERESSOLONEUNSPECPROFILECuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLTWOSPECPROFILERESSOLTWOUNSPECPROFILECuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCaaCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffRCaseworkerCivilSystemupdateCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffJudgeProfileRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECuCaseworkerCivilSolicitorCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruPlus13RolesArivdgAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseflagsAdminCruCaseflagsViewerRWluAdminCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CitizenProfileCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECaseworkerCivilAdminCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLUNSPECPROFILECudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CitizenProfileCudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CitizenProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECruPlus18RolesXuvvnbAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECruPlus17RolesXrgvfiAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorCuCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECaseworkerCivilSystemupdateCudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECuPlus2RolesSzdyetAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONEUNSPECPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.JudgeProfileLegalAdviserRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffRCaseworkerCivilSystemupdateCudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILECudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCrPlus2RolesKprtpqAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.LegalAdviserRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILECaseworkerCivilSystemupdateCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CitizenProfileCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminRCaseworkerCivilSolicitorCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILECuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRCitizenProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILECruCaseworkerCivilSystemupdateRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.LegalAdviserCrAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CitizenProfileRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRCuiAdminProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderCaseworkerCivilSystemupdateRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCaaCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.JudgeProfileLegalAdviserCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateCudCitizenProfileCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerApproverCruCaseworkerCaaCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerApproverCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CuiAdminProfileCuiNbcProfileHearingScheduleAccessJudgeProfileCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.WluAdminUAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilRparobotCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilCudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECitizenProfileCrAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILERPlus11RolesZckyqjAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLUNSPECPROFILECaseworkerCivilStaffRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLTWOUNSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECudCaseworkerCivilStaffRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminRudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.JudgeProfileCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.LegalAdviserRudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILERESSOLONEUNSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruCaseworkerCivilSystemupdateRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILERESSOLONESPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLUNSPECPROFILERESSOLTWOUNSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateJudgeProfileRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLUNSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONEUNSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECruPlus14RolesPewcraAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECruCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruCaseworkerCivilStaffRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLUNSPECPROFILERuPlus2RolesXyhbtoAccess;
import uk.gov.hmcts.reform.civil.ccd.access.JudgeProfileCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CitizenProfileCruWluAdminRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLUNSPECPROFILECruRESSOLONEUNSPECPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorJudgeProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateRHearingScheduleAccessCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILECruPlus2RolesYbrodiAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorCruCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECrAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILECrAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CitizenProfileCAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILECrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.WluAdminRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCrAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorCrAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorCudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruCaseworkerCivilSolicitorCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECrAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILERPlus3RolesGudjikAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILECruCaseworkerCivilStaffRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILEUPlus7RolesTvhfiqAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONEUNSPECPROFILECrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLUNSPECPROFILERuPlus12RolesHuxvfmAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLUNSPECPROFILERuPlus10RolesGrwjgvAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLUNSPECPROFILECruCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLUNSPECPROFILECrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILEAPPSOLUNSPECPROFILECuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLTWOSPECPROFILECuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILECuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateRuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.JudgeProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.GSProfileCaseworkerCivilSystemupdateCaseworkerRasValidationRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCuPlus9RolesKgcoamAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerRasValidationRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateWluAdminRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.GSProfileRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruPlus10RolesYbbobmAccess;
import uk.gov.hmcts.reform.civil.ccd.access.HearingScheduleAccessCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffCaseworkerCivilSystemupdateCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCaseworkerCivilSolicitorCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCaseworkerCivilSystemupdateCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminJudgeProfileCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECuPlus2RolesTckblxAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCaaJudgeProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLTWOSPECPROFILERESSOLTWOUNSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCaaCaseworkerCivilSystemupdateJudgeProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.WluAdminCudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCaaCaseworkerCivilAdminCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilDocRemovalCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILERESSOLONESPECPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilCrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilCudPlus2RolesVlykptAccess;
import uk.gov.hmcts.reform.civil.ccd.access.GSProfileRPlus4RolesOdnovhAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminWluAdminCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CourtOfficerOrderCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.WluAdminCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.HearingScheduleAccessCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateCrAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRPlus7RolesIlgoorAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRCuiAdminProfileCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCaseworkerCivilStaffCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRHearingScheduleAccessCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilJudgeProfileRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILERPlus5RolesPvjgzlAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILERPlus2RolesUugowqAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CuiAdminProfileRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.GSProfileCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerWaTaskConfigurationCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorCruCaseworkerCivilStaffRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLTWOUNSPECPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILERESSOLTWOSPECPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLTWOUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILEUAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILEUAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminUAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorUAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CitizenProfileUAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorCaseworkerCivilStaffRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.PaymentAccessCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSolicitorCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRCourtOfficerOrderCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CourtOfficerOrderCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECrCaseworkerCivilSystemupdateRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLTWOSPECPROFILERPlus3RolesWydxxsAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILERRESSOLONESPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILERESSOLTWOSPECPROFILECrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLTWOSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.LegalAdviserCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderJudgeProfileRAccess;
import uk.gov.hmcts.reform.civil.model.JoConfirmSentToBothDef;
import uk.gov.hmcts.reform.civil.model.JudgeDecisionOnReconsiderationSelect;
import uk.gov.hmcts.reform.civil.model.OrderTypeTrialAdditionalDirections;
import uk.gov.hmcts.reform.civil.model.DisposalHearingOrderAndHearingDetails;
import uk.gov.hmcts.reform.civil.model.DisposalHearingJudgementDeductionStatement;
import uk.gov.hmcts.reform.civil.model.DisposalHearingClaimSettling;
import uk.gov.hmcts.reform.civil.model.DisposalHearingCosts;
import uk.gov.hmcts.reform.civil.model.SmallClaimsOrderAndHearingDetails;
import uk.gov.hmcts.reform.civil.model.SmallClaimsJudgementDeductionStatement;
import uk.gov.hmcts.reform.civil.model.SmallClaimsAllocation;
import uk.gov.hmcts.reform.civil.model.FastTrackOrderAndHearingDetails;
import uk.gov.hmcts.reform.civil.model.FastTrackJudgementDeductionStatement;
import uk.gov.hmcts.reform.civil.model.FastTrackAltDisputeResolution;
import uk.gov.hmcts.reform.civil.model.FastTrackVariationOfDirections;
import uk.gov.hmcts.reform.civil.model.FastTrackSettlement;
import uk.gov.hmcts.reform.civil.model.FastTrackCosts;
import uk.gov.hmcts.reform.civil.model.FastTrackEmployersLiability;
import uk.gov.hmcts.reform.civil.model.ConfirmCourtPermissionNotNeeded;
import uk.gov.hmcts.reform.civil.model.dq.RemoteHearing;
import uk.gov.hmcts.reform.civil.model.finalorders.AssistedOrderReasons;
import uk.gov.hmcts.reform.civil.model.CPRCertifyAcceptanceDJ;
import uk.gov.hmcts.reform.civil.model.StatementOfTruthConsentGAspec;
import uk.gov.hmcts.reform.civil.model.GAIsUrgentGAspec;
import uk.gov.hmcts.reform.civil.model.GAStatusGAspec;
import uk.gov.hmcts.reform.civil.model.GeneralApplicationGAspec;
import uk.gov.hmcts.reform.civil.model.GAUserDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.GAN245FormUpload;
import uk.gov.hmcts.reform.civil.model.HearingAppFor;
import uk.gov.hmcts.reform.civil.model.SolicitorOrganisationEmail;
import uk.gov.hmcts.reform.civil.model.ClaimTimelineList;
import uk.gov.hmcts.reform.civil.model.UnemployedComplexTypeLRspec;
import uk.gov.hmcts.reform.civil.model.RecurringIncome;
import uk.gov.hmcts.reform.civil.model.RecurringExpense;
import uk.gov.hmcts.reform.civil.model.DisclosureOfNonElectronicDocumentsLRspec;
import uk.gov.hmcts.reform.civil.model.DisposalHearingOrderAndHearingDetailsDJ;
import uk.gov.hmcts.reform.civil.model.BundleUpload;

@Accessors(chain = true)
@SuperBuilder(toBuilder = true)
@Jacksonized
@EqualsAndHashCode(callSuper = true)
@Data
public class CaseData extends CaseDataParent implements MappableObject {

    @CCD(ignore = true)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private  Long ccdCaseReference;
    @CCD(ignore = true)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private  CaseState ccdState;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENDEFENDANTPROFILECaseworkerCivilSystemupdateCruAccess.class}
    )
    private  CaseState previousCCDState;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilSystemupdateCuiAdminProfileCruAccess.class, CaseworkerCivilSolicitorCruAccess.class, JudgeProfileRAccess.class}
    )
    private  String preStayState;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {CaseworkerCivilSystemupdateCuiAdminProfileCruAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSolicitorRAccess.class, JudgeProfileRAccess.class}
    )
    private  LocalDate caseStayDate;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "manageStayOption",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilSolicitorRAccess.class, CuiAdminProfileCruAccess.class, JudgeProfileRAccess.class}
    )
    private  String manageStayOption;
    @CCD(
            label = " ",
            access = {CaseworkerCivilAdminCrudCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilSystemupdateCrudAccess.class, CuiAdminProfileCrudAccess.class}
    )
    private  LocalDate manageStayUpdateRequestDate;
    @CCD(
            label = "What type of application(s) do you want to make?",
            hint = "  ",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruPlus3RolesKwiaxwAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private  GAApplicationType generalAppType;
    @CCD(
            label = " ",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCrudAccess.class, CITIZENDEFENDANTPROFILECruAccess.class, CaseworkerCaaCruAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private  GAApplicationTypeLR generalAppTypeLR;
    @CCD(
            label = "Has the respondent agreed to the order that you want the judge to make?",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruPlus3RolesKwiaxwAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private  GARespondentOrderAgreement generalAppRespondentAgreement;
    @CCD(
            label = "Paying for an application",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruPlus3RolesKwiaxwAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private  GAPbaDetails generalAppPBADetails;
    @CCD(
            label = "What order do you want the court to make?",
            hint = "This is the draft order that you are requesting from the court",
            searchable = false,
            max = 4000,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECrudAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private  String generalAppDetailsOfOrder;
    @CCD(
            label = " ",
            searchable = false,
            max = 4000,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "TextArea",
            access = {CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECrudAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private  List<Element<String>> generalAppDetailsOfOrderColl;
    @CCD(
            label = "Reasons for requesting this order",
            searchable = false,
            max = 4000,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECrudAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private  String generalAppReasonsOfOrder;
    @CCD(
            label = " ",
            searchable = false,
            max = 4000,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "TextArea",
            access = {CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECrudAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private  List<Element<String>> generalAppReasonsOfOrderColl;
    @CCD(
            label = "Ask For Costs",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECruAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private  YesOrNo generalAppAskForCosts;
    @CCD(
            label = "Do you want the court to inform the other party of this application?\n",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruPlus3RolesKwiaxwAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private  GAInformOtherParty generalAppInformOtherParty;
    @CCD(
            label = "Is this an urgent application?",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruPlus3RolesKwiaxwAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private  GAUrgencyRequirement generalAppUrgencyRequirement;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECrudAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private  GAStatementOfTruth generalAppStatementOfTruth;
    @CCD(
            label = "Scheduled hearings and trials",
            searchable = false,
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECrudAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private  GAHearingDetails generalAppHearingDetails;
    @CCD(
            label = "Applicant Solicitor Details",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruPlus3RolesKwiaxwAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private  GASolicitorDetailsGAspec generalAppApplnSolicitor;
    @CCD(
            label = "Hearing fee PBA details\n",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class}
    )
    private  SRPbaDetails hearingFeePBADetails;
    @CCD(
            label = "Claim Issued PBA details\n",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class}
    )
    private  SRPbaDetails claimIssuedPBADetails;
    @CCD(ignore = true)
    private  String applicantPartyName;
    @CCD(
            label = " ",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECruAccess.class, CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCrudAccess.class, CaseworkerCaaCruAccess.class}
    )
    private  CertOfSC certOfSC;
    @CCD(ignore = true)
    private  String gaWaTrackLabel;
    @CCD(label = "Enter next state", searchable = false, access = {CaseworkerCivilSystemupdateCruAccess.class})
    private  String nextState;

    @CCD(
            label = "Vary Judgement GA Type",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruPlus3RolesKwiaxwAccess.class}
    )
    private  YesOrNo generalAppVaryJudgementType;
    @CCD(
            label = "Vary Judgement GA Type",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruPlus3RolesKwiaxwAccess.class}
    )
    private  YesOrNo generalAppParentClaimantIsApplicant;
    @CCD(ignore = true)
    private  YesOrNo parentClaimantIsApplicant;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruPlus3RolesKwiaxwAccess.class}
    )
    private  GAHearingDateGAspec generalAppHearingDate;
    @CCD(
            label = "N245 form",
            regex = ".pdf,.txt,.doc,.dot,.docx,.rtf,.xls,.xlt,.xla,.xlsx,.xltx,.xlsb,.ppt,.pot,.pps,.ppa,.pptx,.potx,.ppsx,.jpg,.jpeg,.bmp,.tif,.tiff,.png,.csv",
            categoryID = "applications",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruPlus3RolesKwiaxwAccess.class}
    )
    private  Document generalAppN245FormUpload;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class}
    )
    private  YesOrNo gaEaCourtLocation;

    @CCD(
            label = "Respondent Solicitor Details",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruPlus3RolesKwiaxwAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    @Builder.Default
    private  List<Element<GASolicitorDetailsGAspec>> generalAppRespondentSolicitors = new ArrayList<>();

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "GeneralApplicationGAspec",
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruPlus3RolesKwiaxwAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    @Builder.Default
    private  List<Element<GeneralApplication>> generalApplications = new ArrayList<>();

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "GeneralApplicationDetailsGAspec",
            access = {CaseworkerCaaCuPlus2RolesCjdhidAccess.class, APPSOLSPECPROFILECuPlus3RolesFtetnzAccess.class}
    )
    private  List<Element<GeneralApplicationsDetails>> claimantGaAppDetails;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "GeneralApplicationDetailsGAspec",
            access = {CaseworkerCaaCuPlus2RolesCjdhidAccess.class, CaseworkerCivilAdminJudgeProfileLegalAdviserCuAccess.class, CivilAdministratorBasicRAccess.class, CivilAdministratorStandardRAccess.class}
    )
    private  List<Element<GeneralApplicationsDetails>> gaDetailsMasterCollection;
    @CCD(
            label = "Applications in translation ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "GeneralApplicationDetailsGAspec",
            access = {CaseworkerCivilSystemupdateCudAccess.class}
    )
    private  List<Element<GeneralApplicationsDetails>> gaDetailsTranslationCollection;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "GeneralApplicationDetailsGAspec",
            access = {CaseworkerCaaCuPlus2RolesCjdhidAccess.class, RESSOLONESPECPROFILERESSOLONEUNSPECPROFILECuAccess.class}
    )
    private  List<Element<GADetailsRespondentSol>> respondentSolGaAppDetails;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "GeneralApplicationDetailsGAspec",
            access = {CaseworkerCaaCuPlus2RolesCjdhidAccess.class, RESSOLTWOSPECPROFILERESSOLTWOUNSPECPROFILECuAccess.class}
    )
    private  List<Element<GADetailsRespondentSol>> respondentSolTwoGaAppDetails;
    @CCD(
            label = "Your File Reference",
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, CaseworkerCaaCuAccess.class}
    )
    private  SolicitorReferences solicitorReferences;
    @CCD(
            label = "Workaround for solicitorReferences label",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
    )
    private  SolicitorReferences solicitorReferencesCopy;
    @CCD(
            label = "Defendant's legal representative's reference",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCrudAccess.class}
    )
    private  String respondentSolicitor2Reference;
    @CCD(
            label = "Court location code",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileRAccess.class, CITIZENCLAIMANTPROFILECuCaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemupdateCuAccess.class}
    )
    private  CourtLocation courtLocation;
    @CCD(
            label = "Claimant's details",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruPlus13RolesArivdgAccess.class}
    )
    private  Party applicant1;
    @CCD(
            label = "Second claimant's details",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, CaseflagsAdminCruCaseflagsViewerRWluAdminCruAccess.class, CaseworkerCaaCuAccess.class, CaseworkerCivilSystemupdateCudAccess.class}
    )
    private  Party applicant2;
    @CCD(
            label = "Notifications",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCrudAccess.class, CaseworkerCivilStaffRAccess.class, CitizenProfileCrudAccess.class}
    )
    private  CorrectEmail applicantSolicitor1CheckEmail;
    @CCD(
            label = "Notification details",
            searchable = false,
            access = {CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilAdminCrudAccess.class, APPSOLUNSPECPROFILECudAccess.class, CaseworkerCivilSystemupdateCuAccess.class, CitizenProfileCudAccess.class}
    )
    private  IdamUserDetails applicantSolicitor1UserDetails;
    @CCD(
            label = "Do you want to add another claimant now?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CitizenProfileCruAccess.class}
    )
    private  YesOrNo addApplicant2;
    @CCD(
            label = "Is there another defendant?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CitizenProfileCruAccess.class}
    )
    private  YesOrNo addRespondent2;
    @CCD(
            label = "Does the second defendant have the same legal representative?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
    )
    private  YesOrNo respondent2SameLegalRepresentative;
    @CCD(label = "Defendant details", searchable = false, access = {APPSOLSPECPROFILECruPlus18RolesXuvvnbAccess.class})
    private  Party respondent1;
    @CCD(
            label = "Workaround for respondentDetails label",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, CITIZENDEFENDANTPROFILECruAccess.class}
    )
    private  Party respondent1Copy;
    @CCD(
            label = "Second defendant's details",
            searchable = false,
            access = {APPSOLSPECPROFILECruPlus17RolesXrgvfiAccess.class}
    )
    private  Party respondent2;
    @CCD(
            label = "Workaround for respondentDetails label",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class}
    )
    private  Party respondent2Copy;
    @CCD(
            label = "Defendant details",
            searchable = false,
            access = {CaseworkerCivilSolicitorCuCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSystemupdateCudAccess.class}
    )
    private  Party respondent1DetailsForClaimDetailsTab;
    @CCD(
            label = "Second Defendant's details",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECuPlus2RolesSzdyetAccess.class, CaseworkerCivilSystemupdateCuAccess.class}
    )
    private  Party respondent2DetailsForClaimDetailsTab;
    @CCD(
            label = "Does the defendant have a legal representative?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private  YesOrNo respondent1Represented;
    @CCD(
            label = "Does the second defendant have a legal representative?",
            hint = "If they are not legally represented you can still continue with the claim but the Defendant will be served offline (on paper)",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, RESSOLONEUNSPECPROFILERAccess.class, CaseworkerCivilSolicitorCruAccess.class}
    )
    private  YesOrNo respondent2Represented;
    @CCD(
            label = "Is the organisation registered with MyHMCTS?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, JudgeProfileLegalAdviserRAccess.class, CaseworkerCivilSystemupdateCuAccess.class}
    )
    private  YesOrNo respondent1OrgRegistered;
    @CCD(
            label = "Is the organisation registered with MyHMCTS?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, JudgeProfileLegalAdviserRAccess.class}
    )
    private  YesOrNo respondent2OrgRegistered;
    @CCD(
            label = "Enter defendant legal representative's email address to be used for notifications",
            searchable = false,
            typeOverride = FieldType.Email,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCudAccess.class}
    )
    private  String respondentSolicitor1EmailAddress;
    @CCD(
            label = "Enter the second defendant legal representatives email address to be used for notifications",
            hint = "This should be the email address that the defendant's legal representative provided. You can still issue the claim without their email address but the claim will then continue on paper (offline).",
            searchable = false,
            typeOverride = FieldType.Email,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCudAccess.class}
    )
    private  String respondentSolicitor2EmailAddress;
    @CCD(
            label = "Do you want to upload Particulars of claim?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
    )
    private  YesOrNo uploadParticularsOfClaim;
    @CCD(
            label = "Description of claim",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCrudAccess.class, CITIZENDEFENDANTPROFILECudAccess.class, CitizenProfileCudAccess.class}
    )
    private  String detailsOfClaim;
    @CCD(
            label = "Expected claim value",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CaseworkerCivilAdminCrPlus2RolesKprtpqAccess.class, CITIZENDEFENDANTPROFILERAccess.class, LegalAdviserRAccess.class}
    )
    private  ClaimValue claimValue;
    @CCD(
            label = "Claim fee",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, CITIZENDEFENDANTPROFILECaseworkerCivilSystemupdateCuAccess.class, CitizenProfileCuAccess.class}
    )
    private  Fee claimFee;
    @CCD(
            label = "Other Remedy fee",
            searchable = false,
            access = {JudgeProfileLegalAdviserRAccess.class, CaseworkerCivilAdminRCaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECuAccess.class}
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  Fee otherRemedyFee;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCitizenProfileCruAccess.class, CITIZENCLAIMANTPROFILECruAccess.class}
    )
    private  String serviceRequestReference;
    @CCD(ignore = true)
    private  String paymentReference;
    @CCD(
            label = "PBA Number",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, CitizenProfileCuAccess.class}
    )
    private  DynamicList applicantSolicitor1PbaAccounts;
    @CCD(
            label = "Type of claim",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, CITIZENDEFENDANTPROFILECruCaseworkerCivilSystemupdateRAccess.class, CaseworkerCaaCruAccess.class}
    )
    private  ClaimTypeUnspec claimTypeUnSpec;
    @CCD(
            label = "Type of claim",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CaseworkerCivilAdminCrPlus2RolesKprtpqAccess.class, CITIZENDEFENDANTPROFILECruAccess.class, CaseworkerCaaCuAccess.class, LegalAdviserCrAccess.class}
    )
    private  ClaimType claimType;
    @CCD(
            label = " ",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private HelpWithFees generalAppHelpWithFees;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILECruAccess.class, CitizenProfileRAccess.class, CuiAdminProfileCruAccess.class}
    )
    private  HelpWithFeesDetails claimIssuedHwfDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCuiAdminProfileCruAccess.class, CitizenProfileRAccess.class}
    )
    private  HelpWithFeesDetails hearingHwfDetails;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILECuAccess.class, CitizenProfileCuAccess.class, CuiAdminProfileCruAccess.class}
    )
    private  FeeType hwfFeeType;
    @CCD(
            label = "Total Interest",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemFieldReaderCaseworkerCivilSystemupdateRAccess.class}
    )
    private  SuperClaimType superClaimType;
    @CCD(
            label = "Enter Claim type",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CaseworkerCaaCrudAccess.class}
    )
    private  String claimTypeOther;
    @CCD(
            label = "What type of personal injury is this?",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CaseworkerCivilAdminCrPlus2RolesKprtpqAccess.class, CaseworkerCaaCruAccess.class, LegalAdviserCrAccess.class}
    )
    private  PersonalInjuryType personalInjuryType;
    @CCD(
            label = "Enter personal injury type",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCrudAccess.class}
    )
    private  String personalInjuryTypeOther;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCrudAccess.class, CITIZENDEFENDANTPROFILERAccess.class}
    )
    private  StatementOfTruth applicantSolicitor1ClaimStatementOfTruth;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECrudAccess.class, CaseworkerCivilStaffRAccess.class, CitizenProfileCrudAccess.class}
    )
    private  StatementOfTruth uiStatementOfTruth;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class, CITIZENDEFENDANTPROFILECruAccess.class, CitizenProfileCruAccess.class}
    )
    private  StatementOfTruth respondent1LiPStatementOfTruth;
    @CCD(
            label = "CaseMan reference number",
            access = {CITIZENCLAIMANTPROFILECuPlus2RolesSzdyetAccess.class, CaseworkerCaaCuAccess.class}
    )
    private  String legacyCaseReference;
    @CCD(
            label = "Allocated track",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "Track",
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CaseworkerCivilAdminCrPlus2RolesKprtpqAccess.class, LegalAdviserRAccess.class}
    )
    private  AllocatedTrack allocatedTrack;
    @CCD(ignore = true)
    private  PaymentDetails paymentDetails;
    @CCD(
            label = "Details of PBA payment",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, JudgeProfileLegalAdviserCruAccess.class, CaseworkerCivilSystemupdateCudCitizenProfileCuAccess.class}
    )
    private  PaymentDetails claimIssuedPaymentDetails;
    @CCD(
            label = "Hearing Payment Details",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECruAccess.class, CaseworkerCivilSystemupdateCruAccess.class}
    )
    private  PaymentDetails hearingFeePaymentDetails;
    @CCD(
            label = "Claimant's legal representative",
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, CaseworkerApproverCruCaseworkerCaaCuAccess.class, CaseworkerCivilSystemupdateCudCitizenProfileCuAccess.class}
    )
    private  OrganisationPolicy applicant1OrganisationPolicy;
    @CCD(
            label = "Applicant 2 organisation policy",
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerApproverCruAccess.class, CaseworkerCaaCruAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private  OrganisationPolicy applicant2OrganisationPolicy;
    @CCD(
            label = "Defendant's legal representative",
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, CaseworkerApproverCruCaseworkerCaaCuAccess.class, CaseworkerCivilSystemupdateCudAccess.class}
    )
    private  OrganisationPolicy respondent1OrganisationPolicy;
    @CCD(
            label = "Defendant 2's legal representative",
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, CaseworkerApproverCruCaseworkerCaaCuAccess.class}
    )
    private  OrganisationPolicy respondent2OrganisationPolicy;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCrudAccess.class}
    )
    private  SolicitorOrganisationDetails respondentSolicitor1OrganisationDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCrudAccess.class}
    )
    private  SolicitorOrganisationDetails respondentSolicitor2OrganisationDetails;
    @CCD(
            label = "Postal correspondence for the Claimant’s legal representative will be sent to the address registered with MyHMCTS. You can, if you wish, change the address to which postal correspondence is sent (eg if you work out of a different office from the address registered with MyHMCTS). Do you wish to enter a different address?",
            hint = "This is the address to which postal correspondence for the Claimant’s legal representative will be sent.",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private  YesOrNo applicantSolicitor1ServiceAddressRequired;
    @CCD(
            label = "Claimant's legal representative correspondence address",
            searchable = false,
            access = {DefaultAccess.class, JudgeProfileLegalAdviserRAccess.class, CITIZENCLAIMANTPROFILECuCaseworkerCivilSolicitorCruAccess.class}
    )
    private  Address applicantSolicitor1ServiceAddress;
    @CCD(
            label = "Postal correspondence to the Defendant’s legal representative will be sent to the address that is currently registered with MyHMCTS. You can, if you wish, change the address to which postal correspondence is sent.",
            hint = "Do you want to change the address?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private  YesOrNo respondentSolicitor1ServiceAddressRequired;
    @CCD(
            label = "Defendant's legal representative Correspondence address",
            searchable = false,
            access = {DefaultAccess.class, JudgeProfileLegalAdviserRAccess.class, CITIZENCLAIMANTPROFILECuCaseworkerCivilSolicitorCruAccess.class}
    )
    private  Address respondentSolicitor1ServiceAddress;
    @CCD(
            label = "Postal correspondence to the Defendant’s legal representative will be sent to the address that is currently registered with MyHMCTS. You can, if you wish, change the address to which postal correspondence is sent.",
            hint = "Do you want to change the address?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private  YesOrNo respondentSolicitor2ServiceAddressRequired;
    @CCD(
            label = "Defendant 2's legal representative Correspondence address",
            searchable = false,
            access = {DefaultAccess.class, JudgeProfileLegalAdviserRAccess.class, CITIZENCLAIMANTPROFILECuCaseworkerCivilSolicitorCruAccess.class}
    )
    private  Address respondentSolicitor2ServiceAddress;
    @CCD(ignore = true)
    private  StatementOfTruth applicant1ServiceStatementOfTruthToRespondentSolicitor1;
    @CCD(
            label = " ",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECrudAccess.class, CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCrudAccess.class}
    )
    private  RespondentSolicitorDetails respondentSolicitorDetails;

    @CCD(
            label = "System generated Case Documents",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECuPlus2RolesSzdyetAccess.class, CuiAdminProfileCuiNbcProfileHearingScheduleAccessJudgeProfileCuAccess.class, CaseworkerCivilStaffCuAccess.class, CaseworkerCivilSystemupdateCuAccess.class, WluAdminUAccess.class}
    )
    @Builder.Default
    private  List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();

    @CCD(
            label = "Documents to be translated",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECuAccess.class, CITIZENDEFENDANTPROFILECuAccess.class, CaseworkerCivilAdminCuAccess.class}
    )
    @Builder.Default
    private  List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
    @CCD(
            label = "Bulk scanned or emailed documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "StaffDocument",
            access = {CaseworkerCivilAdminCrudCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECrudAccess.class, CaseworkerCivilRparobotCrudAccess.class, CaseworkerCivilSystemupdateCudAccess.class, CaseworkerCivilCudAccess.class}
    )
    private  List<Element<ManageDocument>> manageDocuments;
    @CCD(
            label = "Upload files",
            hint = "We only accept documents in pdf format. \n\nPlease do not upload password protected documents as this will prevent the claim from being processed.",
            regex = ".pdf",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruCaseworkerCivilSystemupdateRAccess.class}
    )
    private  Document specClaimTemplateDocumentFiles;
    @CCD(
            label = "Upload file",
            hint = "We accept documents in these formats: pdf, txt, doc, dot, docx, rtf, xlt, xlsx, jpg, jpeg or png. \n\nPlease do not upload password protected documents as this will prevent the claim from being processed.",
            regex = ".pdf,.txt,.doc,.dot,.docx,.rtf,.xls,.xlt,.xla,.xlsx,.xltx,.xlsb,.ppt,.pot,.pps,.ppa,.pptx,.potx,.ppsx,.jpg,.jpeg,.bmp,.tif,.tiff,.png",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemupdateRAccess.class, CitizenProfileCruAccess.class}
    )
    private  Document specClaimDetailsDocumentFiles;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "EvidenceList",
            access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECitizenProfileCrAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private  List<Evidence> speclistYourEvidenceList;
    @CCD(
            label = "Do you want to enter a different address?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CitizenProfileCruAccess.class}
    )
    private  YesOrNo specApplicantCorrespondenceAddressRequired;
    @CCD(
            label = "Claimant's legal representative correspondence address",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, JudgeProfileLegalAdviserRAccess.class, CaseworkerCivilSystemupdateCudCitizenProfileCuAccess.class}
    )
    private  Address specApplicantCorrespondenceAddressdetails;
    @CCD(
            label = "Do you want to change the address?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
    )
    private  YesOrNo specRespondentCorrespondenceAddressRequired;
    @CCD(
            label = "Defendant's legal representative correspondence address",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, JudgeProfileLegalAdviserRAccess.class, CaseworkerCivilSystemupdateCudAccess.class}
    )
    private  Address specRespondentCorrespondenceAddressdetails;
    @CCD(
            label = "Is this address correct?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
    )
    private  YesOrNo specAoSRespondent2HomeAddressRequired;
    @CCD(
            label = "Add the address you want",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemupdateCrudAccess.class}
    )
    private  Address specAoSRespondent2HomeAddressDetails;

    @CCD(
            label = "Enter the extension date that was agreed with the other party",
            hint = "This date cannot be more than 28 days from the original deadline.",
            searchable = false,
            access = {APPSOLSPECPROFILERPlus11RolesZckyqjAccess.class}
    )
    private  LocalDate respondentSolicitor1AgreedDeadlineExtension;
    @CCD(
            label = "Enter the extension date that was agreed with the other party",
            hint = "This date cannot be more than 28 days from the original deadline.",
            searchable = false,
            access = {DefaultAccess.class, RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class, APPSOLUNSPECPROFILECaseworkerCivilStaffRAccess.class}
    )
    private  LocalDate respondentSolicitor2AgreedDeadlineExtension;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess.class, RESSOLTWOUNSPECPROFILECruAccess.class}
    )
    private  ResponseIntention respondent1ClaimResponseIntentionType;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess.class, RESSOLTWOUNSPECPROFILECruAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private  ResponseIntention respondent2ClaimResponseIntentionType;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private  ResponseIntention respondent1ClaimResponseIntentionTypeApplicant2;
    @CCD(
            label = "Upload documents",
            searchable = false,
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECudCaseworkerCivilStaffRAccess.class, CaseworkerCivilAdminRudAccess.class, CaseworkerCivilSystemupdateCuAccess.class, JudgeProfileCrudAccess.class, LegalAdviserRudAccess.class}
    )
    private  ServedDocumentFiles servedDocumentFiles;

    @CCD(
            label = "Do the defendants intend to file a single response to the claim?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECruAccess.class, RESSOLONESPECPROFILERESSOLONEUNSPECPROFILECruAccess.class}
    )
    private  YesOrNo respondentResponseIsSame;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilSystemupdateRAccess.class, APPSOLSPECPROFILERESSOLONESPECPROFILECruAccess.class}
    )
    private  YesOrNo defendantSingleResponseToBothClaimants;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess.class, JudgeProfileRAccess.class}
    )
    private  RespondentResponseType respondent1ClaimResponseType;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLUNSPECPROFILERESSOLTWOUNSPECPROFILECruAccess.class, CaseworkerCivilSystemupdateJudgeProfileRAccess.class}
    )
    private  RespondentResponseType respondent2ClaimResponseType;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess.class}
    )
    private  RespondentResponseType respondent1ClaimResponseTypeToApplicant2;
    @CCD(
            label = "Upload defence",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, APPSOLUNSPECPROFILECruAccess.class, RESSOLONEUNSPECPROFILECruAccess.class}
    )
    private  ResponseDocument respondent1ClaimResponseDocument;
    @CCD(
            label = "Upload defence",
            searchable = false,
            access = {DefaultAccess.class, APPSOLUNSPECPROFILERESSOLTWOUNSPECPROFILECruAccess.class}
    )
    private  ResponseDocument respondent2ClaimResponseDocument;
    @CCD(
            label = "Upload defence",
            searchable = false,
            access = {DefaultAccess.class, APPSOLUNSPECPROFILERESSOLTWOUNSPECPROFILECruAccess.class, RESSOLONEUNSPECPROFILECruAccess.class}
    )
    private  ResponseDocument respondentSharedClaimResponseDocument;
    @CCD(label = " ", searchable = false, access = {APPSOLSPECPROFILECruPlus14RolesPewcraAccess.class})
    private  CaseDocument respondent1GeneratedResponseDocument;
    @CCD(
            label = " ",
            searchable = false,
            access = {RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class, APPSOLSPECPROFILECruCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilStaffRAccess.class, APPSOLUNSPECPROFILERuPlus2RolesXyhbtoAccess.class}
    )
    private  CaseDocument respondent2GeneratedResponseDocument;
    @CCD(
            label = " ",
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECaseworkerCivilSystemupdateCruAccess.class}
    )
    private  LocalDate claimMovedToMediationOn;

    @CCD(
            label = "Defendant response Case Documents",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECuPlus2RolesSzdyetAccess.class, CaseworkerCivilSystemupdateCuAccess.class, JudgeProfileCuAccess.class}
    )
    @Builder.Default
    private  List<Element<CaseDocument>> defendantResponseDocuments = new ArrayList<>();

    @CCD(
            label = "Do you want to proceed with the claim",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, CitizenProfileCruWluAdminRAccess.class}
    )
    private  YesOrNo applicant1ProceedWithClaim;
    @CCD(
            label = "Does Claimant 1 want to proceed with the claim against ${respondent1.partyName}",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, APPSOLUNSPECPROFILECruRESSOLONEUNSPECPROFILERAccess.class}
    )
    private  YesOrNo applicant1ProceedWithClaimMultiParty2v1;
    @CCD(
            label = "Does Claimant 2 want to proceed with the claim against ${respondent1.partyName}",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, APPSOLUNSPECPROFILECruRESSOLONEUNSPECPROFILERAccess.class}
    )
    private  YesOrNo applicant2ProceedWithClaimMultiParty2v1;
    @CCD(
            label = "Do you want to proceed with the claim against ${respondent1.partyName}",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, APPSOLUNSPECPROFILECruRESSOLONEUNSPECPROFILERAccess.class}
    )
    private  YesOrNo applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2;
    @CCD(
            label = "Do you want to proceed with the claim against ${respondent2.partyName}",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, APPSOLUNSPECPROFILECruRESSOLONEUNSPECPROFILERAccess.class}
    )
    private  YesOrNo applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2;
    @CCD(ignore = true)
    private  YesOrNo applicant1ProceedWithClaimRespondent2;
    @CCD(
            label = "Respond to the defence",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CITIZENCLAIMANTPROFILECruAccess.class}
    )
    private  ResponseDocument applicant1DefenceResponseDocument;
    @CCD(
            label = "Respond to the defence",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, APPSOLUNSPECPROFILECruRESSOLONEUNSPECPROFILERAccess.class}
    )
    private  ResponseDocument claimantDefenceResDocToDefendant2;

    @CCD(
            label = "Claimant response to defence documents",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECuPlus2RolesSzdyetAccess.class, CaseworkerCivilSystemupdateCuAccess.class, JudgeProfileCuAccess.class}
    )
    @Builder.Default
    private  List<Element<CaseDocument>> claimantResponseDocuments = new ArrayList<>();

    @CCD(
            label = "Duplicate system generated documents for case file view",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilSolicitorJudgeProfileCruAccess.class, CaseworkerCivilSystemupdateRHearingScheduleAccessCruAccess.class, CITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilStaffCruAccess.class, LegalAdviserRAccess.class}
    )
    @Builder.Default
    private  List<Element<CaseDocument>> duplicateSystemGeneratedCaseDocs = new ArrayList<>();

    @CCD(
            label = "Duplicate claimant and defendant response documents for case file view",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilSolicitorJudgeProfileCruAccess.class, CaseworkerCivilSystemupdateCruAccess.class, LegalAdviserRAccess.class}
    )
    @Builder.Default
    @JsonProperty("duplicateClaimantDefResponseDocs")
    private  List<Element<CaseDocument>> duplicateClaimantDefendantResponseDocs = new ArrayList<>();

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ClaimAmountBreakup",
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECrudAccess.class, CitizenProfileCrudAccess.class}
    )
    private  List<ClaimAmountBreakup> claimAmountBreakup;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "TimelineOfEvents",
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruPlus2RolesYbrodiAccess.class, CitizenProfileCruWluAdminRAccess.class}
    )
    private  List<TimelineOfEvents> timelineOfEvents;
    /**
     * money amount in pounds.
     */
    @CCD(
            label = "Total",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CitizenProfileCruWluAdminRAccess.class, CITIZENDEFENDANTPROFILECruAccess.class}
    )
    private BigDecimal totalClaimAmount;
    @CCD(
            label = "Total Interest",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruPlus2RolesYbrodiAccess.class}
    )
    private BigDecimal totalInterest;
    @CCD(
            label = "Total Claim amount + interest",
            searchable = false,
            access = {RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilSolicitorCruCaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private BigDecimal totalClaimAmountPlusInterestAdmitPart;
    @CCD(
            label = "Total Claim amount + interest",
            searchable = false,
            access = {CaseworkerCivilSolicitorCruCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENDEFENDANTPROFILECruAccess.class}
    )
    private BigDecimal totalClaimAmountPlusInterest;
    @CCD(
            label = "Default Judgement Overall Amount",
            searchable = false,
            access = {DefaultAccess.class, JudgeProfileLegalAdviserRAccess.class, CaseworkerCivilSolicitorCrudAccess.class}
    )
    private BigDecimal defaultJudgementOverallTotal;
    @CCD(
            label = " ",
            searchable = false,
            access = {RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilSolicitorCruAccess.class}
    )
    private String totalClaimAmountPlusInterestAdmitPartString;
    @CCD(
            label = " ",
            searchable = false,
            access = {CITIZENDEFENDANTPROFILECruAccess.class, CaseworkerCivilSolicitorCruAccess.class}
    )
    private String totalClaimAmountPlusInterestString;
    @CCD(
            label = "You can claim interest on the money your client is owed. The court will decide if they’re entitled to it.\n\n",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECrAccess.class, CITIZENDEFENDANTPROFILECrAccess.class, CitizenProfileCAccess.class}
    )
    private  YesOrNo claimInterest;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECitizenProfileCrAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private  InterestClaimOptions interestClaimOptions;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECitizenProfileCrAccess.class}
    )
    private  SameRateInterestSelection sameRateInterestSelection;
    @CCD(
            label = "Total interest amount\n",
            searchable = false,
            access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECitizenProfileCrAccess.class}
    )
    private  BigDecimal breakDownInterestTotal;
    @CCD(
            label = "Show how you calculated the amount\n",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECitizenProfileCrAccess.class}
    )
    private  String breakDownInterestDescription;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECitizenProfileCrAccess.class}
    )
    private  InterestClaimFromType interestClaimFrom;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECitizenProfileCrAccess.class}
    )
    private  InterestClaimUntilType interestClaimUntil;
    @CCD(
            label = "For example, 22 04 2021\n",
            searchable = false,
            access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECitizenProfileCrAccess.class}
    )
    private  LocalDate interestFromSpecificDate;
    @CCD(
            label = "Explain why you’re claiming from this date\n",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECitizenProfileCrAccess.class}
    )
    private  String interestFromSpecificDateDescription;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECrAccess.class}
    )
    private  String calculatedInterest;
    @CCD(
            label = "Email address",
            searchable = false,
            typeOverride = FieldType.Email,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilSystemupdateCrudAccess.class}
    )
    private  String specRespondentSolicitor1EmailAddress;
    @CCD(
            label = "Is this address correct?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECruAccess.class, APPSOLSPECPROFILECruAccess.class, CaseworkerCivilSystemupdateCrudAccess.class}
    )
    private  YesOrNo specAoSApplicantCorrespondenceAddressRequired;
    @CCD(
            label = "Add the address you want",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemupdateCrudAccess.class}
    )
    private  Address specAoSApplicantCorrespondenceAddressdetails;
    @CCD(
            label = "Is this address correct?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
    )
    private  YesOrNo specAoSRespondentCorrespondenceAddressRequired;
    @CCD(
            label = "Add the address you want",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemupdateCrudAccess.class}
    )
    private  Address specAoSRespondentCorrespondenceAddressdetails;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CitizenProfileCruWluAdminRAccess.class}
    )
    private  YesOrNo specRespondent1Represented;
    @CCD(
            label = "Does the second defendant have a legal representative?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, JudgeProfileLegalAdviserRAccess.class}
    )
    private  YesOrNo specRespondent2Represented;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "TimelineOfEvents",
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruPlus2RolesYbrodiAccess.class}
    )
    private  List<TimelineOfEvents> specResponseTimelineOfEvents;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "TimelineOfEvents",
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private  List<TimelineOfEvents> specResponseTimelineOfEvents2;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECruAccess.class}
    )
    private  TimelineUploadTypeSpec specClaimResponseTimelineList;
    @CCD(
            label = "Upload files",
            hint = "We only accept documents in pdf format.",
            regex = ".pdf",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private  Document specResponseTimelineDocumentFiles;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "EvidenceList",
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruPlus2RolesYbrodiAccess.class}
    )
    private  List<Evidence> specResponselistYourEvidenceList;
    @CCD(ignore = true)
    private  List<Evidence> specResponselistYourEvidenceList2;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCrudAccess.class, CITIZENDEFENDANTPROFILECrudAccess.class}
    )
    private  String detailsOfWhyDoesYouDisputeTheClaim;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCrudAccess.class}
    )
    private  String detailsOfWhyDoesYouDisputeTheClaim2;

    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
    )
    private  ResponseDocument respondent1SpecDefenceResponseDocument;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
    )
    private  ResponseDocument respondent2SpecDefenceResponseDocument;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilSystemupdateCuiAdminProfileCruAccess.class}
    )
    private  YesOrNo bundleError;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class}
    )
    private  String bundleEvent;
    @CCD(
            label = " ",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class}
    )
    private  YesOrNo fullAdmitNoPaymentSchedulerProcessed;
    @CCD(
            label = "Are you confirming that you are abandoning your request for an other remedy \n(e.g. injunction, rescission, declaration)?",
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerCivilSolicitorCuAccess.class}
    )
    private  YesOrNo isOtherRemedyAbandoned;

    @CCD(label = "Date Other Remedy abandoned", access = {DefaultAccess.class, CaseworkerCivilSolicitorCuAccess.class})
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  LocalDate otherRemedyAbandonedDate;

    public RespondentResponseTypeSpec getRespondent1ClaimResponseTypeForSpec() {

        if (respondent1ClaimResponseTypeForSpec == null) {
            return getRespondent1ClaimResponseTestForSpec();
        } else {
            return respondent1ClaimResponseTypeForSpec;
        }
    }

    public RespondentResponseTypeSpec getRespondent2ClaimResponseTypeForSpec() {

        if (respondent2ClaimResponseTypeForSpec == null) {
            return getRespondent2ClaimResponseTestForSpec();
        } else {
            return respondent2ClaimResponseTypeForSpec;
        }
    }

    @CCD(
            label = " ",
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECruAccess.class, JudgeProfileRAccess.class, WluAdminRAccess.class}
    )
    private  RespondentResponseTypeSpec respondent1ClaimResponseTypeForSpec;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilSystemupdateJudgeProfileRAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
    )
    private  RespondentResponseTypeSpec respondent2ClaimResponseTypeForSpec;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class}
    )
    private  RespondentResponseTypeSpec claimant1ClaimResponseTypeForSpec;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class}
    )
    private  RespondentResponseTypeSpec claimant2ClaimResponseTypeForSpec;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
    )
    private  RespondentResponseTypeSpecPaidStatus respondent1ClaimResponsePaymentAdmissionForSpec;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECruAccess.class, CaseworkerCivilSolicitorCuAccess.class}
    )
    private  RespondentResponsePartAdmissionPaymentTimeLRspec defenceAdmitPartPaymentTimeRouteRequired;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "RespondentResponseAdmissionType",
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CITIZENDEFENDANTPROFILECruAccess.class}
    )
    private  String defenceRouteRequired;
    @CCD(
            label = "Allocated track",
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECrudAccess.class, CaseworkerCivilAdminCrAccess.class, CaseworkerCivilSolicitorCrAccess.class, CaseworkerCivilSystemupdateRAccess.class, LegalAdviserCrAccess.class}
    )
    private  String responseClaimTrack;
    @CCD(
            label = " ",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruPlus2RolesYbrodiAccess.class}
    )
    private  RespondToClaim respondToClaim;
    @CCD(
            label = " ",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruPlus2RolesYbrodiAccess.class}
    )
    private  RespondToClaim respondToAdmittedClaim;
    /**
     * money amount in pence.
     */
    @CCD(
            label = "How much money does your client admit to owing?",
            hint = "The amount to be considered is the claim amount and any interest claimed. The claim fee and any \n\nfixed costs claimed are not included in this figure but are payable in addition and if judgment is \n\nentered on an admission will be included in the total judgment sum.",
            searchable = false,
            typeOverride = FieldType.MoneyGBP,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruPlus2RolesYbrodiAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private  BigDecimal respondToAdmittedClaimOwingAmount;
    /**
     * money amount in pounds.
     */
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CITIZENDEFENDANTPROFILECruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private  BigDecimal respondToAdmittedClaimOwingAmountPounds;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private  YesOrNo specDefenceFullAdmittedRequired;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
    )
    private  PaymentUponCourtOrder respondent1CourtOrderPayment;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECuAccess.class}
    )
    private  RepaymentPlanLRspec respondent1RepaymentPlan;
    @CCD(
            label = " ",
            hint = "For example 9 December 2020",
            access = {DefaultAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCudAccess.class, CITIZENCLAIMANTPROFILECrudAccess.class, CITIZENDEFENDANTPROFILECudAccess.class, CaseworkerCaaCuAccess.class, CaseworkerCivilSolicitorCudAccess.class}
    )
    private  RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECrudAccess.class, CITIZENDEFENDANTPROFILECudAccess.class, CaseworkerCivilSolicitorCudAccess.class}
    )
    private  UnemployedComplexTypeLRspec respondToClaimAdmitPartUnemployedLRspec;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilSolicitorCuAccess.class, CITIZENDEFENDANTPROFILECuAccess.class}
    )
    private  Respondent1EmployerDetailsLRspec responseClaimAdmitPartEmployer;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruAccess.class}
    )
    private  String responseToClaimAdmitPartWhyNotPayLRspec;
    // Fields related to ROC-9453 & ROC-9455
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECruAccess.class, RESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECrAccess.class, CaseworkerCivilSystemupdateRAccess.class}
    )
    private  YesOrNo responseClaimMediationSpecRequired;
    @CCD(
            label = "Has the ${applicant1.partyName} agreed to free mediation?",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
    )
    private  SmallClaimMedicalLRspec applicant1ClaimMediationSpecRequired;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilSolicitorCuAccess.class, CITIZENDEFENDANTPROFILECuAccess.class}
    )
    private  YesOrNo defenceAdmitPartEmploymentTypeRequired;
    @CCD(
            label = "Do you want to use an expert?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECruAccess.class, RESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECrAccess.class}
    )
    private  YesOrNo responseClaimExpertSpecRequired;
    @CCD(
            label = "Do you want to use an expert?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CitizenProfileCruWluAdminRAccess.class}
    )
    private  YesOrNo applicant1ClaimExpertSpecRequired;
    @CCD(
            label = "How many witnesses, including the defendant, will give evidence at the hearing?",
            regex = "\\d+",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, RESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECrAccess.class}
    )
    private  String responseClaimWitnesses;
    @CCD(
            label = "How many witnesses, including the claimant, will give evidence at the hearing?",
            regex = "\\d+",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CitizenProfileCruWluAdminRAccess.class}
    )
    private  String applicant1ClaimWitnesses;
    @JsonProperty("SmallClaimHearingInterpreterRequired")
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
    )
    private  YesOrNo smallClaimHearingInterpreterRequired;
    @JsonProperty("SmallClaimHearingInterpreterDescription")
    @CCD(
            label = "Type of interpreter",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCrudAccess.class}
    )
    private  String smallClaimHearingInterpreterDescription;
    @CCD(
            label = "Employed or self-employed?",
            hint = "Select both if your client is employed and self-employed",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILERPlus3RolesGudjikAccess.class}
    )
    private  List<EmploymentTypeCheckboxFixedListLRspec> respondToClaimAdmitPartEmploymentTypeLRspec;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruCaseworkerCivilStaffRAccess.class}
    )
    private  YesOrNo specDefenceAdmittedRequired;

    @CCD(
            label = "Claimant Mediation Contact Information",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileRAccess.class, APPSOLSPECPROFILECruAccess.class, CaseworkerCivilSolicitorCuAccess.class}
    )
    private  MediationContactInformation app1MediationContactInfo;
    @CCD(
            label = "Claimant Mediation Availability",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileRAccess.class, APPSOLSPECPROFILECruAccess.class, CaseworkerCivilSolicitorCuAccess.class}
    )
    private  MediationAvailability app1MediationAvailability;
    @CCD(
            label = "Defendant 1 Mediation Contact Information",
            searchable = false,
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffJudgeProfileRAccess.class}
    )
    private  MediationContactInformation resp1MediationContactInfo;
    @CCD(
            label = "Defendant 2 Mediation Contact information",
            searchable = false,
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffJudgeProfileRAccess.class}
    )
    private  MediationContactInformation resp2MediationContactInfo;
    @CCD(
            label = "Defendant 1 Mediation Availability",
            searchable = false,
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffJudgeProfileRAccess.class}
    )
    private  MediationAvailability resp1MediationAvailability;
    @CCD(
            label = "Defendant 2 Mediation Availability",
            searchable = false,
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffJudgeProfileRAccess.class}
    )
    private  MediationAvailability resp2MediationAvailability;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
    )
    private final String additionalInformationForJudge;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilStaffRAccess.class, APPSOLSPECPROFILERESSOLONESPECPROFILECruAccess.class}
    )
    @JsonProperty("applicant1AdditionalInformationForJudge")
    private final String applicantAdditionalInformationForJudge;
    @JsonUnwrapped
    private  ExpertRequirements respondToClaimExperts;

    @CCD(
            label = "Note",
            hint = "Add note detail, including relevant dates and people involved",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerCivilAdminCrudCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private  String caseNote;
    @CCD(
            label = "Note",
            searchable = false,
            access = {CaseworkerCivilAdminCuAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilSystemupdateCudAccess.class}
    )
    private  List<Element<CaseNote>> caseNotes;

    @CCD(
            label = "Summary",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerCivilAdminCrudAccess.class}
    )
    private  String notificationSummary;

    @CCD(
            label = "Withdraw claim",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
    )
    @Valid
    private  CloseClaim withdrawClaim;

    @CCD(
            label = "Discontinue claim",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
    )
    @Valid
    private  CloseClaim discontinueClaim;

    @CCD(label = "Business process for camunda", access = {APPSOLSPECPROFILEUPlus7RolesTvhfiqAccess.class})
    private  BusinessProcess businessProcess;

    @JsonUnwrapped
    private  Respondent1DQ respondent1DQ;

    @JsonUnwrapped
    private  Respondent2DQ respondent2DQ;

    @JsonUnwrapped
    private  Applicant1DQ applicant1DQ;

    @JsonUnwrapped
    private  Applicant2DQ applicant2DQ;

    public boolean hasNoOngoingBusinessProcess() {
        return businessProcess == null
            || businessProcess.getStatus() == null
            || businessProcess.getStatus() == FINISHED;
    }

    @CCD(
            label = "Defendant litigation details",
            searchable = false,
            access = {DefaultAccess.class, APPSOLUNSPECPROFILECaseworkerCivilStaffRAccess.class, RESSOLONEUNSPECPROFILECrudAccess.class}
    )
    private  LitigationFriend genericLitigationFriend;
    @CCD(
            label = "Defendant litigation details",
            searchable = false,
            access = {APPSOLUNSPECPROFILERuPlus12RolesHuxvfmAccess.class}
    )
    private  LitigationFriend respondent1LitigationFriend;
    @CCD(
            label = "Defendant 2 litigation details",
            searchable = false,
            access = {APPSOLUNSPECPROFILERuPlus10RolesGrwjgvAccess.class}
    )
    private  LitigationFriend respondent2LitigationFriend;

    @CCD(
            label = "Is the claimant a child i.e. under the age of 18?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private  YesOrNo applicant1LitigationFriendRequired;
    @CCD(
            label = "Claimant litigation friend details",
            searchable = false,
            access = {CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CaseflagsAdminCruCaseflagsViewerRWluAdminCruAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSystemupdateCudAccess.class, CaseworkerCivilAdminCrudAccess.class}
    )
    private  LitigationFriend applicant1LitigationFriend;

    @CCD(
            label = "Is the second Claimant a child i.e. under the age of 18?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
    )
    private  YesOrNo applicant2LitigationFriendRequired;
    @CCD(
            label = "Second Claimant litigation friend details",
            searchable = false,
            access = {CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CaseflagsAdminCruCaseflagsViewerRWluAdminCruAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSystemupdateCudAccess.class, CaseworkerCivilAdminCrudAccess.class}
    )
    private  LitigationFriend applicant2LitigationFriend;

    @CCD(
            label = "Which defendant legal representative do you want to notify?",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilAdminCrudAccess.class, CaseworkerCivilSystemupdateCrudAccess.class}
    )
    private  DynamicList defendantSolicitorNotifyClaimOptions;
    @CCD(
            label = "Which defendant legal representative do you want to notify of the claim details?",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCrudAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilAdminCrudAccess.class}
    )
    private  DynamicList defendantSolicitorNotifyClaimDetailsOptions;
    @CCD(
            label = "Who requires a litigation friend?",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCrudAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilAdminCrudAccess.class}
    )
    private  DynamicList selectLitigationFriend;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
    )
    private  String litigantFriendSelection;
    @CCD(
            label = "Case proceeds in Caseman",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCrudAccess.class}
    )
    @Valid
    private  ClaimProceedsInCaseman claimProceedsInCaseman;
    @CCD(
            label = "Case proceeds in Caseman",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCrudAccess.class}
    )
    @Valid
    private  ClaimProceedsInCasemanLR claimProceedsInCasemanLR;

    //CCD UI flag
    @CCD(
            label = "Hide pba and show noPbaAccountLabel",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private  YesOrNo applicantSolicitor1PbaAccountsIsEmpty;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class, RESSOLTWOUNSPECPROFILECruAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private MultiPartyResponseTypeFlags multiPartyResponseTypeFlags;
    @CCD(
            label = "Not shown in UI",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, APPSOLUNSPECPROFILECruCaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private YesOrNo applicantsProceedIntention;
    @CCD(
            label = " ",
            searchable = false,
            access = {APPSOLSPECPROFILECrudAccess.class, APPSOLUNSPECPROFILECrudAccess.class, CITIZENCLAIMANTPROFILECrudAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private  MultiPartyScenario claimantResponseScenarioFlag;
    @CCD(
            label = "Not shown in UI",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, APPSOLUNSPECPROFILECruCaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private YesOrNo claimantResponseDocumentToDefendant2Flag;
    @CCD(
            label = "Not shown in UI",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, APPSOLUNSPECPROFILECruCaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private YesOrNo claimant2ResponseFlag;
    @CCD(ignore = true)
    private RespondentResponseTypeSpec atLeastOneClaimResponseTypeForSpecIsFullDefence;
    // used only in 2v1
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
    )
    private YesOrNo specFullAdmissionOrPartAdmission;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
    )
    private YesOrNo sameSolicitorSameResponse;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
    )
    private YesOrNo specPaidLessAmountOrDisputesOrPartAdmission;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
    )
    private YesOrNo specFullDefenceOrPartAdmission1V1;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
    )
    private YesOrNo specFullDefenceOrPartAdmission;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
    )
    private YesOrNo specDisputesOrPartAdmission;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
    )
    private YesOrNo specPartAdmitPaid;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
    )
    private YesOrNo specFullAdmitPaid;

    // dates
    @CCD(
            label = "Date claim Submitted",
            access = {CITIZENCLAIMANTPROFILECuPlus2RolesSzdyetAccess.class, CaseworkerCaaCuAccess.class, CaseworkerCivilSystemupdateCuAccess.class}
    )
    private  LocalDateTime submittedDate;
    @CCD(
            label = "Payment successful date",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorRAccess.class}
    )
    private  LocalDateTime paymentSuccessfulDate;
    @CCD(
            label = "Date claim issued",
            access = {CITIZENCLAIMANTPROFILECuPlus2RolesSzdyetAccess.class, CITIZENDEFENDANTPROFILECaseworkerCivilSystemupdateCuAccess.class}
    )
    private  LocalDate issueDate;
    @CCD(
            label = "Claim notification deadline",
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class}
    )
    private  LocalDateTime claimNotificationDeadline;
    @CCD(
            label = "Date claim notified",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECuPlus2RolesSzdyetAccess.class, CaseworkerCivilSystemupdateCudAccess.class}
    )
    private  LocalDateTime claimNotificationDate;
    @CCD(
            label = "Case details notification deadline",
            access = {DefaultAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorRAccess.class}
    )
    private  LocalDateTime claimDetailsNotificationDeadline;
    @CCD(
            label = "Case details notification date",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
    )
    private  LocalDateTime claimDetailsNotificationDate;
    @CCD(
            label = "Defendant 1 solicitor deadline",
            access = {CITIZENCLAIMANTPROFILECuPlus2RolesSzdyetAccess.class, RESSOLONESPECPROFILERESSOLONEUNSPECPROFILECuAccess.class, CITIZENDEFENDANTPROFILECaseworkerCivilSystemupdateCuAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECuAccess.class, RESSOLTWOSPECPROFILECuAccess.class}
    )
    private  LocalDateTime respondent1ResponseDeadline;
    @CCD(
            label = "Defendant 2 solicitor deadline",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCuAccess.class, RESSOLTWOSPECPROFILERESSOLTWOUNSPECPROFILECuAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECuAccess.class, CITIZENCLAIMANTPROFILECuAccess.class, RESSOLONESPECPROFILECuAccess.class}
    )
    private  LocalDateTime respondent2ResponseDeadline;
    @CCD(
            label = "Add Legal Rep 1 Deadline",
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CITIZENDEFENDANTPROFILERAccess.class}
    )
    private  LocalDateTime addLegalRepDeadlineRes1;
    @CCD(
            label = "Add Legal Rep 2 Deadline",
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CITIZENDEFENDANTPROFILERAccess.class}
    )
    private  LocalDateTime addLegalRepDeadlineRes2;
    @CCD(
            label = "Case dismissed deadline",
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class}
    )
    private  LocalDateTime claimDismissedDeadline;
    @CCD(
            label = "Date respondent 1 submitted time extension",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCrudAccess.class}
    )
    private  LocalDateTime respondent1TimeExtensionDate;
    @CCD(
            label = "Date respondent 2 submitted time extension",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
    )
    private  LocalDateTime respondent2TimeExtensionDate;
    @CCD(
            label = "Date respondent 1 acknowledged notification",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
    )
    private  LocalDateTime respondent1AcknowledgeNotificationDate;
    @CCD(
            label = "Date respondent 2 acknowledged notification",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
    )
    private  LocalDateTime respondent2AcknowledgeNotificationDate;
    @CCD(
            label = "Defendant response date",
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruCaseworkerCivilStaffRAccess.class, CaseworkerCivilSystemupdateRuAccess.class}
    )
    private  LocalDateTime respondent1ResponseDate;
    @CCD(
            label = "Defendant 2 response date",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
    )
    private  LocalDateTime respondent2ResponseDate;
    @CCD(
            label = "Claimant response deadline",
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class, CITIZENDEFENDANTPROFILERAccess.class}
    )
    private  LocalDateTime applicant1ResponseDeadline;
    @CCD(
            label = "Date applicant 1 responded to defence",
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CITIZENDEFENDANTPROFILECruAccess.class}
    )
    private  LocalDateTime applicant1ResponseDate;
    @CCD(
            label = "Date applicant 1 responded to defence",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
    )
    private  LocalDateTime applicant2ResponseDate;
    @CCD(
            label = "Date claim moved offline",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECruAccess.class, CaseworkerCivilSolicitorRAccess.class}
    )
    private  LocalDateTime takenOfflineDate;
    @CCD(
            label = "Date claim moved offline by staff",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorRAccess.class}
    )
    private  LocalDateTime takenOfflineByStaffDate;
    @CCD(
            label = "SDO unsuitable date",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorRAccess.class, JudgeProfileCruAccess.class}
    )
    private  LocalDateTime unsuitableSDODate;
    @CCD(
            label = "Other Details",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECuPlus2RolesSzdyetAccess.class, CaseworkerCivilAdminJudgeProfileLegalAdviserCuAccess.class, CaseworkerCivilStaffCuAccess.class}
    )
    private  OtherDetails otherDetails;
    @CCD(
            label = "Claim dismissed date",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class}
    )
    private  LocalDateTime claimDismissedDate;
    @CCD(
            label = " ",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private  String claimAmountBreakupSummaryObject;
    @CCD(
            label = "Date respondent 1 added or last updated litigation friend",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
    )
    private  LocalDateTime respondent1LitigationFriendDate;
    @CCD(
            label = "Date respondent 2 added or last updated litigation friend",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
    )
    private  LocalDateTime respondent2LitigationFriendDate;
    @CCD(
            label = " ",
            access = {CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilSystemFieldReaderRCitizenProfileCruAccess.class}
    )
    private  LocalDateTime respondent1RespondToSettlementAgreementDeadline;
    @CCD(
            label = "Was respondent response deadline checked?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class}
    )
    private  YesOrNo respondent1ResponseDeadlineChecked;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, GSProfileCaseworkerCivilSystemupdateCaseworkerRasValidationRAccess.class}
    )
    private  String paymentTypePBA;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, GSProfileCaseworkerCivilSystemupdateCaseworkerRasValidationRAccess.class}
    )
    private  String paymentTypePBASpec;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CITIZENDEFENDANTPROFILECruAccess.class, CaseworkerCaaCruAccess.class}
    )
    private  String whenToBePaidText;

    @CCD(
            label = "Date respondent 1 added litigation friend",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
    )
    private  LocalDateTime respondent1LitigationFriendCreatedDate;
    @CCD(
            label = "Date respondent 2 added litigation friend",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
    )
    private  LocalDateTime respondent2LitigationFriendCreatedDate;

    @CCD(
            label = "System generated Case Documents",
            hint = "Bundle to hold documents",
            searchable = false,
            access = {CaseworkerCivilAdminCuPlus9RolesKgcoamAccess.class}
    )
    @Builder.Default
    private  List<IdValue<Bundle>> caseBundles = new ArrayList<>();

    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILERPlus3RolesGudjikAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private  Respondent1DebtLRspec specDefendant1Debts;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILERPlus3RolesGudjikAccess.class}
    )
    private  Respondent1SelfEmploymentLRspec specDefendant1SelfEmploymentDetails;
    @CCD(
            label = "Draft order",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileRAccess.class, CITIZENCLAIMANTPROFILECudAccess.class, CaseworkerCivilSolicitorCudAccess.class}
    )
    private  String detailsOfDirection;

    @CCD(
            label = "Hearing requirements",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileRAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCuAccess.class}
    )
    private  HearingSupportRequirementsDJ hearingSupportRequirementsDJ;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilSolicitorCuAccess.class, CaseworkerCivilSystemupdateCuAccess.class, CaseworkerRasValidationRAccess.class, JudgeProfileCruAccess.class}
    )
    private  CaseLocationCivil caseManagementLocation;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, JudgeProfileRAccess.class}
    )
    private  CaseManagementCategory caseManagementCategory;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemupdateWluAdminRAccess.class, APPSOLSPECPROFILECruAccess.class, GSProfileRAccess.class}
    )
    private  String locationName;
    @CCD(
            label = "Defendant Details",
            searchable = false,
            typeOverride = FieldType.DynamicRadioList,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilSolicitorCuAccess.class}
    )
    private  DynamicList defendantDetailsSpec;
    @CCD(
            label = "Against which Defendant are you requesting default judgment?",
            searchable = false,
            typeOverride = FieldType.DynamicRadioList,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileRAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCuAccess.class}
    )
    private  DynamicList defendantDetails;
    @CCD(
            label = "Both Defendants",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
    )
    private  String bothDefendants;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
    )
    private  String bothDefendantsSpec;
    @CCD(
            label = "Amount already paid",
            searchable = false,
            typeOverride = FieldType.MoneyGBP,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
    )
    private  String partialPaymentAmount;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
    )
    private  YesOrNo partialPayment;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilSolicitorCuAccess.class}
    )
    private  LocalDate paymentSetDate;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilSolicitorCuAccess.class}
    )
    private  String repaymentSummaryObject;
    @CCD(
            label = "This will include fixed costs for judgment and commencement.",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
    )
    private  YesOrNo paymentConfirmationDecisionSpec;
    @CCD(
            label = "Total",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
    )
    private  String repaymentDue;
    @CCD(
            label = "Regular payments of",
            hint = "For example, £10",
            searchable = false,
            typeOverride = FieldType.MoneyGBP,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilSolicitorCuAccess.class}
    )
    private  String repaymentSuggestion;
    @CCD(
            label = "date2",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
    )
    private  String currentDatebox;
    @CCD(
            label = "Date for first instalment",
            hint = "This must be after ${currentDatebox}",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilSolicitorCuAccess.class}
    )
    private  LocalDate repaymentDate;
    @CCD(
            label = "case Name Hmcts Internal",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruPlus10RolesYbbobmAccess.class}
    )
    private  String caseNameHmctsInternal;
    @CCD(
            label = "Public case name",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemupdateCrudAccess.class}
    )
    private  String caseNamePublic;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class}
    )
    private  YesOrNo ccjJudgmentAmountShowInterest;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class}
    )
    private  YesOrNo claimFixedCostsExist;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
    )
    private  YesOrNo partAdmit1v1Defendant;

    @CCD(
            label = "System generated Case Documents",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECuPlus2RolesSzdyetAccess.class, HearingScheduleAccessCuAccess.class, JudgeProfileCuAccess.class}
    )
    @Builder.Default
    private  List<Element<CaseDocument>> defaultJudgmentDocuments = new ArrayList<>();

    @CCD(
            label = "Hearing type",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "HearingSelection",
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileRAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCuAccess.class}
    )
    private  String hearingSelection;

    @CCD(
            label = "Is respondent1?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECrudAccess.class}
    )
    private  YesOrNo isRespondent1;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECrudAccess.class}
    )
    private  YesOrNo isRespondent2;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECrudAccess.class}
    )
    private  YesOrNo isApplicant1;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECuAccess.class}
    )
    private  YesOrNo disabilityPremiumPayments;
    @CCD(
            label = "Does ${respondent1.partyName} receive severe disability premium payments?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECuAccess.class}
    )
    private  YesOrNo severeDisabilityPremiumPayments;

    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
    )
    private  String currentDefendant;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECrudAccess.class}
    )
    private  YesOrNo claimStarted;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
    )
    private  String currentDefendantName;

    @JsonUnwrapped
    private  BreathingSpaceInfo breathing;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, JudgeProfileCruAccess.class}
    )
    private  String applicantVRespondentText;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilSolicitorJudgeProfileCruAccess.class, CaseworkerCivilStaffCruAccess.class, CaseworkerCivilCruAccess.class}
    )
    private YesOrNo setRequestDJDamagesFlagForWA;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateCruAccess.class, CaseworkerCivilAdminCaseworkerCivilSolicitorCruAccess.class}
    )
    private String featureToggleWA;

    @CCD(
            label = " ",
            access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilAdminCaseworkerCivilSystemupdateCrudAccess.class}
    )
    private ContactDetailsUpdatedEvent contactDetailsUpdatedEvent;

    /**
     * RTJ = Refer To Judge.
     */
    @CCD(ignore = true)
    private  String eventDescriptionRTJ;
    /**
     * RTJ = Refer To Judge.
     */
    @CCD(ignore = true)
    private  String additionalInformationRTJ;
    /**
     * Refer To Judge(Defence received in time).
     */
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "ConfirmReferToJudgeDefenceReceived",
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCruAccess.class}
    )
    private List<ConfirmationToggle> confirmReferToJudgeDefenceReceived;

    //general application order documents
    @CCD(
            label = "General order document",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECuPlus2RolesSzdyetAccess.class, CaseworkerCivilAdminJudgeProfileLegalAdviserCuAccess.class, CITIZENDEFENDANTPROFILECaseworkerCivilSystemupdateCuAccess.class, CaseworkerCaaCuAccess.class}
    )
    private  List<Element<CaseDocument>> generalOrderDocument;
    @CCD(
            label = "General order document",
            searchable = false,
            access = {CaseworkerCaaCuPlus2RolesCjdhidAccess.class, CaseworkerCivilAdminJudgeProfileCuAccess.class}
    )
    private  List<Element<CaseDocument>> generalOrderDocStaff;
    @CCD(
            label = "General order document",
            searchable = false,
            access = {CaseworkerCaaCuPlus2RolesCjdhidAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECuAccess.class, CITIZENCLAIMANTPROFILECuAccess.class}
    )
    private  List<Element<CaseDocument>> generalOrderDocClaimant;
    @CCD(
            label = "General order document",
            searchable = false,
            access = {CaseworkerCaaCuPlus2RolesCjdhidAccess.class, RESSOLONESPECPROFILERESSOLONEUNSPECPROFILECuAccess.class, CITIZENDEFENDANTPROFILECuAccess.class}
    )
    private  List<Element<CaseDocument>> generalOrderDocRespondentSol;
    @CCD(
            label = "General order document",
            searchable = false,
            access = {CaseworkerCaaCuPlus2RolesCjdhidAccess.class, RESSOLTWOSPECPROFILERESSOLTWOUNSPECPROFILECuAccess.class}
    )
    private  List<Element<CaseDocument>> generalOrderDocRespondentSolTwo;

    @CCD(
            label = "Consent order document",
            searchable = false,
            access = {CaseworkerCaaCuPlus2RolesCjdhidAccess.class, CaseworkerCivilAdminJudgeProfileLegalAdviserCuAccess.class, CITIZENCLAIMANTPROFILECuPlus2RolesTckblxAccess.class}
    )
    private  List<Element<CaseDocument>> consentOrderDocument;
    @CCD(
            label = "Consent order document",
            searchable = false,
            access = {CaseworkerCaaCuPlus2RolesCjdhidAccess.class, CaseworkerCivilAdminJudgeProfileCuAccess.class}
    )
    private  List<Element<CaseDocument>> consentOrderDocStaff;
    @CCD(
            label = "Consent order document",
            searchable = false,
            access = {CaseworkerCaaCuPlus2RolesCjdhidAccess.class, APPSOLSPECPROFILECuPlus3RolesFtetnzAccess.class}
    )
    private  List<Element<CaseDocument>> consentOrderDocClaimant;
    @CCD(
            label = "Consent order document",
            searchable = false,
            access = {CaseworkerCaaCuPlus2RolesCjdhidAccess.class, RESSOLONESPECPROFILERESSOLONEUNSPECPROFILECuAccess.class}
    )
    private  List<Element<CaseDocument>> consentOrderDocRespondentSol;
    @CCD(
            label = "Consent order document",
            searchable = false,
            access = {CaseworkerCaaCuPlus2RolesCjdhidAccess.class, RESSOLTWOSPECPROFILERESSOLTWOUNSPECPROFILECuAccess.class}
    )
    private  List<Element<CaseDocument>> consentOrderDocRespondentSolTwo;

    @CCD(
            label = "Upload evidence",
            regex = ".pdf,.txt,.doc,.dot,.docx,.rtf,.xls,.xlt,.xla,.xlsx,.xltx,.xlsb,.ppt,.pot,.pps,.ppa,.pptx,.potx,.ppsx,.jpg,.jpeg,.bmp,.tif,.tiff,.png,.csv",
            categoryID = "applications",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruPlus3RolesKwiaxwAccess.class, CaseworkerCivilAdminCruAccess.class}
    )
    private  List<Element<Document>> generalAppEvidenceDocument;

    @CCD(
            label = "Upload evidence casefile view",
            categoryID = "applications",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class, CaseworkerCaaJudgeProfileCruAccess.class}
    )
    private  List<Element<Document>> gaEvidenceDocStaff;
    @CCD(
            label = "Upload evidence casefile view",
            categoryID = "applications",
            searchable = false,
            access = {APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECruAccess.class, CaseworkerCaaCruAccess.class}
    )
    private  List<Element<Document>> gaEvidenceDocClaimant;
    @CCD(
            label = "Upload evidence casefile view",
            categoryID = "applications",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, RESSOLONESPECPROFILERESSOLONEUNSPECPROFILECruAccess.class, CaseworkerCaaCruAccess.class}
    )
    private  List<Element<Document>> gaEvidenceDocRespondentSol;
    @CCD(
            label = "Upload evidence casefile view",
            categoryID = "applications",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, RESSOLTWOSPECPROFILERESSOLTWOUNSPECPROFILECruAccess.class, CaseworkerCaaCruAccess.class}
    )
    private  List<Element<Document>> gaEvidenceDocRespondentSolTwo;
    @CCD(
            label = "Upload addl Doc casefile view",
            regex = ".pdf,.txt,.doc,.dot,.docx,.rtf,.xls,.xlt,.xla,.xlsx,.xltx,.xlsb,.ppt,.pot,.pps,.ppa,.pptx,.potx,.ppsx,.jpg,.jpeg,.bmp,.tif,.tiff,.png,.csv",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CaseworkerCivilAdminCaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemupdateCrudAccess.class}
    )
    private  List<Element<CaseDocument>> gaAddlDoc;
    @CCD(
            label = "Upload addl Doc casefile view",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class, JudgeProfileCruAccess.class, WluAdminRAccess.class}
    )
    private  List<Element<CaseDocument>> gaAddlDocStaff;
    @CCD(
            label = "Upload addl Doc casefile view",
            searchable = false,
            access = {APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, CITIZENCLAIMANTPROFILECruAccess.class}
    )
    private  List<Element<CaseDocument>> gaAddlDocClaimant;
    @CCD(
            label = "Upload addl Doc casefile view",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, RESSOLONESPECPROFILERESSOLONEUNSPECPROFILECruAccess.class, CITIZENDEFENDANTPROFILECruAccess.class}
    )
    private  List<Element<CaseDocument>> gaAddlDocRespondentSol;
    @CCD(
            label = "Upload addl Doc casefile view",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, RESSOLTWOSPECPROFILERESSOLTWOUNSPECPROFILECruAccess.class}
    )
    private  List<Element<CaseDocument>> gaAddlDocRespondentSolTwo;
    @CCD(
            label = "Upload addl Doc casefile view",
            regex = ".pdf,.txt,.doc,.dot,.docx,.rtf,.xls,.xlt,.xla,.xlsx,.xltx,.xlsb,.ppt,.pot,.pps,.ppa,.pptx,.potx,.ppsx,.jpg,.jpeg,.bmp,.tif,.tiff,.png,.csv",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class}
    )
    private  List<Element<CaseDocument>> gaAddlDocBundle;
    @CCD(
            label = "Draft Application document",
            searchable = false,
            access = {CaseworkerCaaCuPlus2RolesCjdhidAccess.class, CaseworkerCivilAdminJudgeProfileLegalAdviserCuAccess.class, CITIZENCLAIMANTPROFILECuPlus2RolesTckblxAccess.class}
    )
    private  List<Element<CaseDocument>> gaDraftDocument;
    @CCD(
            label = "Draft Application document",
            searchable = false,
            access = {CaseworkerCaaCuPlus2RolesCjdhidAccess.class, CaseworkerCivilAdminJudgeProfileCuAccess.class}
    )
    private  List<Element<CaseDocument>> gaDraftDocStaff;
    @CCD(
            label = "Draft Application document",
            searchable = false,
            access = {CaseworkerCaaCuPlus2RolesCjdhidAccess.class, APPSOLSPECPROFILECuPlus3RolesFtetnzAccess.class}
    )
    private  List<Element<CaseDocument>> gaDraftDocClaimant;
    @CCD(
            label = "Draft Application document",
            searchable = false,
            access = {CaseworkerCaaCuPlus2RolesCjdhidAccess.class, RESSOLONESPECPROFILERESSOLONEUNSPECPROFILECuAccess.class, CITIZENCLAIMANTPROFILECuAccess.class, CITIZENDEFENDANTPROFILECuAccess.class}
    )
    private  List<Element<CaseDocument>> gaDraftDocRespondentSol;
    @CCD(
            label = "Draft Application document",
            searchable = false,
            access = {RESSOLTWOSPECPROFILERESSOLTWOUNSPECPROFILECuAccess.class, CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCrudAccess.class, CaseworkerCaaCuAccess.class}
    )
    private  List<Element<CaseDocument>> gaDraftDocRespondentSolTwo;

    @CCD(
            label = "Application Respond Response Document",
            searchable = false,
            access = {RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CaseworkerCaaCaseworkerCivilSystemupdateJudgeProfileCruAccess.class}
    )
    private  List<Element<CaseDocument>> gaRespondDoc;
    @CCD(
            label = "Documents to be translated",
            regex = ".pdf,.txt,.doc,.dot,.docx,.rtf,.xls,.xlt,.xla,.xlsx,.xltx,.xlsb,.ppt,.pot,.pps,.ppa,.pptx,.potx,.ppsx,.jpg,.jpeg,.bmp,.tif,.tiff,.png,.csv",
            searchable = false,
            access = {JudgeProfileLegalAdviserRAccess.class, CaseworkerCivilAdminRAccess.class, CaseworkerCivilSystemupdateCrudAccess.class, WluAdminRAccess.class}
    )
    private  List<Element<CaseDocument>> preTranslationGaDocsApplicant;
    @CCD(
            label = "Documents to be translated",
            regex = ".pdf,.txt,.doc,.dot,.docx,.rtf,.xls,.xlt,.xla,.xlsx,.xltx,.xlsb,.ppt,.pot,.pps,.ppa,.pptx,.potx,.ppsx,.jpg,.jpeg,.bmp,.tif,.tiff,.png,.csv",
            searchable = false,
            access = {JudgeProfileLegalAdviserRAccess.class, CaseworkerCivilAdminRAccess.class, CaseworkerCivilSystemupdateCrudAccess.class, WluAdminRAccess.class}
    )
    private  List<Element<CaseDocument>> preTranslationGaDocsRespondent;
    @CCD(
            label = "System generated Case Documents",
            searchable = false,
            access = {CuiAdminProfileCuiNbcProfileHearingScheduleAccessJudgeProfileCuAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class, WluAdminCudAccess.class}
    )
    @Builder.Default
    private  List<Element<CaseDocument>> hearingDocuments = new ArrayList<>();

    @CCD(
            label = "System generated Case Documents",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCuAccess.class, CuiAdminProfileCuiNbcProfileHearingScheduleAccessJudgeProfileCuAccess.class, WluAdminCudAccess.class}
    )
    @Builder.Default
    private  List<Element<CaseDocument>> hearingDocumentsWelsh = new ArrayList<>();

    // GA for LIP
    @CCD(
            label = "Is GA Applicant Lip",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCrudAccess.class, CaseworkerCaaCaseworkerCivilAdminCruAccess.class}
    )
    private  YesOrNo isGaApplicantLip;
    @CCD(
            label = "Is GA Respondent One LIP",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCrudAccess.class, CaseworkerCaaCaseworkerCivilAdminCruAccess.class}
    )
    private  YesOrNo isGaRespondentOneLip;
    @CCD(
            label = "Is GA Respondent Two LIP",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCrudAccess.class, CaseworkerCaaCaseworkerCivilAdminCruAccess.class}
    )
    private  YesOrNo isGaRespondentTwoLip;

    @CCD(
            label = "Document",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "documentToKeep",
            access = {CaseworkerCivilDocRemovalCrudAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private List<DocumentToKeepCollection> documentToKeepCollection;

    @CCD(
            label = "Court location code",
            searchable = false,
            access = {APPSOLSPECPROFILEAPPSOLUNSPECPROFILECuAccess.class}
    )
    private RequestedCourtForTabDetails requestedCourtForTabDetailsApp;
    @CCD(
            label = "Court location code",
            searchable = false,
            access = {RESSOLONESPECPROFILERESSOLONEUNSPECPROFILECuAccess.class}
    )
    private RequestedCourtForTabDetails requestedCourtForTabDetailsRes1;
    @CCD(
            label = "Court location code",
            searchable = false,
            access = {RESSOLTWOSPECPROFILERESSOLTWOUNSPECPROFILECuAccess.class}
    )
    private RequestedCourtForTabDetails requestedCourtForTabDetailsRes2;

    @CCD(
            label = " ",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILERAccess.class, APPSOLSPECPROFILERESSOLONESPECPROFILERAccess.class, CaseworkerCivilAdminCaseworkerCivilSystemupdateCrudAccess.class, CaseworkerCivilCrudAccess.class}
    )
    private  ChangeLanguagePreference changeLanguagePreference;
    @CCD(
            label = "Claimant language preference",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "PreferredLanguage",
            access = {CaseworkerCivilCudPlus2RolesVlykptAccess.class, CITIZENCLAIMANTPROFILECudAccess.class}
    )
    private  PreferredLanguage claimantLanguagePreferenceDisplay;
    @CCD(
            label = "Defendant language preference",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "PreferredLanguage",
            access = {CaseworkerCivilCudPlus2RolesVlykptAccess.class, CITIZENDEFENDANTPROFILECudAccess.class}
    )
    private  PreferredLanguage defendantLanguagePreferenceDisplay;

    @CCD(
            label = "Query Documents",
            searchable = false,
            access = {RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECruAccess.class, GSProfileRPlus4RolesOdnovhAccess.class, CaseworkerCivilAdminCruAccess.class}
    )
    @Builder.Default
    private  List<Element<CaseDocument>> queryDocuments = new ArrayList<>();

    @CCD(
            label = "Type of document to be translated",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "PreTranslationDocumentType",
            access = {CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECruAccess.class, JudgeProfileLegalAdviserRAccess.class, CaseworkerCivilAdminCruAccess.class, WluAdminRAccess.class}
    )
    private  PreTranslationDocumentType preTranslationDocumentType;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerCivilAdminWluAdminCruAccess.class, APPSOLSPECPROFILERESSOLONESPECPROFILERAccess.class, JudgeProfileLegalAdviserCruAccess.class}
    )
    private  YesOrNo bilingualHint;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class, JudgeProfileRAccess.class}
    )
    private  CaseDocument respondent1OriginalDqDoc;

    @CCD(
            label = " ",
            typeOverride = FieldType.YesOrNo,
            access = {RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECruAccess.class, APPSOLSPECPROFILECruAccess.class, CaseworkerCivilSystemupdateCruAccess.class}
    )
    private  YesOrNo isMintiLipCase;

    @CCD(
            label = "Note: Paragraph numbers and the parties' name to which the penal notice applies must be entered in the box below.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String  smallClaimsPenalNotice;
    @CCD(
            label = "Note: Paragraph numbers and the parties' name to which the penal notice applies must be entered in the box below.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String  fastTrackPenalNotice;

    @CCD(
            label = "Court Officer Orders",
            searchable = false,
            access = {CaseworkerCivilAdminCuAccess.class, CourtOfficerOrderCuAccess.class, WluAdminCuAccess.class}
    )
    @Builder.Default
    private  List<Element<CaseDocument>> courtOfficersOrders = new ArrayList<>();
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private  YesOrNo isReferToJudgeClaim;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSolicitorRAccess.class, GSProfileRAccess.class, CaseworkerCivilSystemupdateCrudAccess.class, CuiAdminProfileCruAccess.class}
    )
    private  YesOrNo enableUploadEvent;

    @CCD(ignore = true)
    private  ClientContext clientContext;

    @CCD(
            label = "Do you want to add an other remedy e.g. declaration, injunction, rescission?",
            hint = "If you file an application for a non-money claim (other than a claim for possession of land or recovery of goods) and a claim for damages, both court fees must be paid. \n\nFor example, county court fee (fee 1.5) plus relevant money claim fee (the court issued claim fee 1.1).",
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, CITIZENCLAIMANTPROFILECuCaseworkerCivilSolicitorCruAccess.class, CaseworkerCaaCuAccess.class}
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  YesOrNo isClaimDeclarationAdded;
    @CCD(
            label = "Other remedy narrative",
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, CITIZENCLAIMANTPROFILECuCaseworkerCivilSolicitorCruAccess.class, CaseworkerCaaCuAccess.class}
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String  claimDeclarationDescription;
    @CCD(
            label = "Does your claim include any issues under the Human Rights Act 1998?",
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, CITIZENCLAIMANTPROFILECuCaseworkerCivilSolicitorCruAccess.class, CaseworkerCaaCuAccess.class}
    )
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  YesOrNo isHumanRightsActIssues;

    @CCD(
            label = "Enter defendant's email address to be used for linking case",
            searchable = false,
            typeOverride = FieldType.Email,
            access = {CaseworkerCivilStaffCaseworkerCivilSystemupdateCruAccess.class, CaseworkerCivilAdminCaseworkerCivilSolicitorCruAccess.class}
    )
    private  String defendantEmailAddress;

    /**
     * There are several fields that can hold the I2P of applicant1 depending
     * on multiparty scenario, which complicates all conditions depending on it.
     * This method tries to simplify those conditions since only one field will be
     * meaningful for that.
     *
     * @return value set among the fields that hold the I2P of applicant1
     */
    @JsonIgnore
    public YesOrNo getApplicant1ProceedsWithClaimSpec() {
        return Stream.of(
                applicant1ProceedWithClaim,
                getApplicant1ProceedWithClaimSpec2v1()
            )
            .filter(Objects::nonNull)
            .findFirst().orElse(null);
    }

    public YesOrNo getRespondent1Represented() {
        return Stream.of(
                respondent1Represented,
                specRespondent1Represented
            )
            .filter(Objects::nonNull)
            .findFirst().orElse(null);
    }

    @JsonIgnore
    public boolean isRespondent1LiP() {
        return YesOrNo.NO.equals(getRespondent1Represented());
    }

    @JsonIgnore
    public boolean isApplicantLiP() {
        return YesOrNo.NO == getApplicant1Represented();
    }

    public YesOrNo getRespondent2Represented() {
        return Stream.of(
                respondent2Represented,
                specRespondent2Represented
            )
            .filter(Objects::nonNull)
            .findFirst().orElse(null);
    }

    @JsonIgnore
    public boolean isRespondent2LiP() {
        return YesOrNo.NO == getRespondent2Represented();
    }

    @JsonIgnore
    public boolean respondent1PaidInFull() {
        return respondent1ClaimResponsePaymentAdmissionForSpec
            == RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT;
    }

    @JsonIgnore
    public boolean isPayBySetDate() {
        return defenceAdmitPartPaymentTimeRouteRequired != null
            && defenceAdmitPartPaymentTimeRouteRequired == RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE;
    }

    @JsonIgnore
    public boolean isPayByInstallment() {
        return defenceAdmitPartPaymentTimeRouteRequired != null
            && defenceAdmitPartPaymentTimeRouteRequired
            == RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN;
    }

    @JsonIgnore
    public boolean isPayImmediately() {
        return RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY.equals(getDefenceAdmitPartPaymentTimeRouteRequired());
    }

    @JsonIgnore
    public boolean hasDefendantPaidTheAmountClaimed() {
        return SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED
            .equals(getDefenceRouteRequired());
    }

    @JsonIgnore
    public boolean isPaidFullAmount() {
        RespondToClaim localRespondToClaim = null;
        if (getRespondent1ClaimResponseTypeForSpec() == FULL_DEFENCE) {
            localRespondToClaim = getRespondToClaim();
        } else if (getRespondent1ClaimResponseTypeForSpec() == PART_ADMISSION) {
            localRespondToClaim = getRespondToAdmittedClaim();
        }

        return ofNullable(localRespondToClaim)
            .map(RespondToClaim::getHowMuchWasPaid)
            .map(amount -> MonetaryConversions.penniesToPounds(amount).compareTo(totalClaimAmount) >= 0)
            .orElse(false);
    }

    @JsonIgnore
    public boolean isClaimBeingDisputed() {
        return SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM
            .equals(getDefenceRouteRequired());
    }

    @JsonIgnore
    public LocalDate getDateForRepayment() {
        return ofNullable(respondToClaimAdmitPartLRspec)
            .map(RespondToClaimAdmitPartLRspec::getWhenWillThisAmountBePaid).orElse(null);
    }

    @JsonIgnore
    public boolean hasBreathingSpace() {
        return getBreathing() != null
            && getBreathing().getEnter() != null
            && getBreathing().getLift() == null;
    }

    @JsonIgnore
    public boolean isDefendantPaymentPlanYes() {
        Set<RespondentResponsePartAdmissionPaymentTimeLRspec> paymentPlan = EnumSet.of(
            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN,
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE
        );

        return hasApplicantAcceptedRepaymentPlan()
            || !paymentPlan.contains(getDefenceAdmitPartPaymentTimeRouteRequired())
            || isClaimantNotSettlePartAdmitClaim();
    }

    @JsonIgnore
    public boolean isDefendantPaymentPlanNo() {
        Set<RespondentResponsePartAdmissionPaymentTimeLRspec> paymentPlan = EnumSet.of(
            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN,
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE
        );

        return hasApplicantRejectedRepaymentPlan()
            || !paymentPlan.contains(getDefenceAdmitPartPaymentTimeRouteRequired())
            || isClaimantNotSettlePartAdmitClaim();
    }

    @JsonIgnore
    public boolean isPartAdmitClaimSettled() {
        return (
            getApplicant1ProceedsWithClaimSpec() == null
                && isPartAdmitClaimSpec()
                && isClaimantIntentionSettlePartAdmit()
                && isClaimantConfirmAmountPaidPartAdmit());
    }

    @JsonIgnore
    public boolean isPartAdmitClaimNotSettled() {
        return (
            getApplicant1ProceedsWithClaimSpec() != null
                || getApplicant1AcceptAdmitAmountPaidSpec() != null
                || isClaimantIntentionNotSettlePartAdmit()
                || isClaimantConfirmAmountNotPaidPartAdmit());
    }

    @JsonIgnore
    public boolean hasDefendantNotPaid() {
        return NO.equals(getApplicant1PartAdmitConfirmAmountPaidSpec());
    }

    @JsonIgnore
    public boolean isSettlementDeclinedByClaimant() {
        return NO.equals(getApplicant1PartAdmitIntentionToSettleClaimSpec());
    }

    @JsonIgnore
    public boolean isClaimantAcceptedClaimAmount() {
        return YES.equals(getApplicant1AcceptAdmitAmountPaidSpec());
    }

    @JsonIgnore
    public boolean isClaimantRejectsClaimAmount() {
        return NO.equals(getApplicant1AcceptAdmitAmountPaidSpec());
    }

    @JsonIgnore
    public boolean isPartAdmitAlreadyPaid() {
        return YES.equals(getSpecDefenceAdmittedRequired());
    }

    @JsonIgnore
    public boolean isFullDefence() {
        return YES.equals(getApplicant1ProceedWithClaim());
    }

    @JsonIgnore
    public boolean hasDefendantAgreedToFreeMediation() {
        return YES.equals(getResponseClaimMediationSpecRequired());
    }

    @JsonIgnore
    public boolean hasDefendant2AgreedToFreeMediation() {
        return YES.equals(getResponseClaimMediationSpec2Required());
    }

    @JsonIgnore
    public boolean isMultiPartyDefendant() {
        return !YES.equals(getDefendantSingleResponseToBothClaimants())
            && YES.equals(getApplicant1ProceedWithClaim());
    }

    @JsonIgnore
    public boolean isMultiPartyClaimant(MultiPartyScenario multiPartyScenario) {
        return multiPartyScenario.equals(TWO_V_ONE)
            && YES.equals(getDefendantSingleResponseToBothClaimants())
            && YES.equals(getApplicant1ProceedWithClaimSpec2v1());
    }

    @JsonIgnore
    public boolean isPaidSomeAmountMoreThanClaimAmount() {
        return getCcjPaymentDetails().getCcjPaymentPaidSomeAmount() != null
            && getCcjPaymentDetails().getCcjPaymentPaidSomeAmount()
            .compareTo(new BigDecimal(MonetaryConversions.poundsToPennies(getTotalClaimAmount()))) > 0;
    }

    @JsonIgnore
    public boolean hasApplicantProceededWithClaim() {
        return YES == getApplicant1ProceedWithClaim()
            || YES == getApplicant1ProceedWithClaimSpec2v1()
            || NO.equals(getApplicant1AcceptAdmitAmountPaidSpec())
            || NO.equals(getApplicant1PartAdmitConfirmAmountPaidSpec())
            || NO.equals(getApplicant1PartAdmitIntentionToSettleClaimSpec());
    }

    @JsonIgnore
    public boolean hasApplicantNotProceededWithClaim() {
        return Objects.nonNull(getApplicant1ProceedWithClaim()) && NO == getApplicant1ProceedWithClaim();
    }

    @JsonIgnore
    public boolean isRespondentResponseFullDefence() {
        return (RespondentResponseTypeSpec.FULL_DEFENCE.equals(getRespondent1ClaimResponseTypeForSpec())
            && !isOneVTwoTwoLegalRep(this))
            || (RespondentResponseTypeSpec.FULL_DEFENCE.equals(getRespondent1ClaimResponseTypeForSpec())
            && RespondentResponseTypeSpec.FULL_DEFENCE.equals(getRespondent2ClaimResponseTypeForSpec()));
    }

    @JsonIgnore
    public boolean hasApplicantAcceptedRepaymentPlan() {
        return YES.equals(getApplicant1AcceptFullAdmitPaymentPlanSpec())
            || YES.equals(getApplicant1AcceptPartAdmitPaymentPlanSpec());
    }

    @JsonIgnore
    public boolean hasApplicantRejectedRepaymentPlan() {
        return NO.equals(getApplicant1AcceptFullAdmitPaymentPlanSpec())
            || NO.equals(getApplicant1AcceptPartAdmitPaymentPlanSpec());
    }

    @JsonIgnore
    public boolean isAcceptDefendantPaymentPlanForPartAdmitYes() {
        return YesOrNo.YES.equals(getApplicant1AcceptPartAdmitPaymentPlanSpec());
    }

    @JsonIgnore
    public boolean isRespondent1NotRepresented() {
        return NO.equals(getRespondent1Represented());
    }

    @JsonIgnore
    public boolean isRespondent2NotRepresented() {
        return NO.equals(Stream.of(
                respondent2Represented,
                specRespondent2Represented
            )
            .filter(Objects::nonNull)
            .findFirst().orElse(null));
    }

    @JsonIgnore
    public boolean isApplicant1NotRepresented() {
        return NO.equals(getApplicant1Represented());
    }

    @JsonIgnore
    public boolean isLRvLipOneVOne() {
        return isRespondent1LiP()
            && !isApplicant1NotRepresented()
            && isOneVOne(this);
    }

    @JsonIgnore
    public boolean isLipvLipOneVOne() {
        return isRespondent1LiP()
            && isApplicant1NotRepresented()
            && isOneVOne(this);
    }

    @JsonIgnore
    public boolean isApplicantLipOneVOne() {
        return isApplicant1NotRepresented() && isOneVOne(this);
    }

    @JsonIgnore
    public boolean isJudgementDateNotPermitted() {
        LocalDate whenWillThisAmountBePaid = null;
        LocalDate firstRepaymentDate;
        if (hasApplicant1CourtDecisionInFavourOfClaimant()) {
            if (applicant1SuggestedPayImmediately()) {
                whenWillThisAmountBePaid = getApplicant1SuggestPayImmediatelyPaymentDateForDefendantSpec();
            } else if (applicant1SuggestedPayBySetDate()) {
                whenWillThisAmountBePaid = ofNullable(getApplicant1RequestedPaymentDateForDefendantSpec()).map(
                    PaymentBySetDate::getPaymentSetDate).orElse(null);
            }
            firstRepaymentDate = getApplicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec();
        } else {
            whenWillThisAmountBePaid =
                ofNullable(getRespondToClaimAdmitPartLRspec()).map(RespondToClaimAdmitPartLRspec::getWhenWillThisAmountBePaid).orElse(
                    null);
            firstRepaymentDate = ofNullable(getRespondent1RepaymentPlan()).map(RepaymentPlanLRspec::getFirstRepaymentDate).orElse(
                null);
        }
        LocalDate respondentSettlementAgreementDeadline = ofNullable(
            getRespondent1RespondToSettlementAgreementDeadline()).map(LocalDateTime::toLocalDate).orElse(null);
        Optional<CaseDataLiP> optionalCaseDataLiP = ofNullable(getCaseDataLiP());
        YesOrNo hasDoneSettlementAgreement = optionalCaseDataLiP.map(CaseDataLiP::getRespondentSignSettlementAgreement).orElse(
            null);
        boolean hasDoneSettlementAgreementInTime = (nonNull(hasDoneSettlementAgreement) && hasDoneSettlementAgreement == YesOrNo.YES)
            || (isNull(hasDoneSettlementAgreement) && isDateAfterToday(respondentSettlementAgreementDeadline));

        return (isNull(whenWillThisAmountBePaid) && isNull(firstRepaymentDate))
            || (isDateAfterToday(whenWillThisAmountBePaid) && hasDoneSettlementAgreementInTime)
            || (isDateAfterToday(firstRepaymentDate) && hasDoneSettlementAgreementInTime)
            || (isDateAfterToday(whenWillThisAmountBePaid) && isFullAdmitPayImmediatelyClaimSpec());

    }

    @JsonIgnore
    public String getFormattedJudgementPermittedDate(LocalDate extendedRespondent1ResponseDate) {
        return formatLocalDateTime(
            extendedRespondent1ResponseDate.atTime(DeadlinesCalculator.END_OF_BUSINESS_DAY),
            DATE_TIME_AT
        );
    }

    @JsonIgnore
    public boolean isPartAdmitClaimSpec() {
        return PART_ADMISSION.equals(getRespondent1ClaimResponseTypeForSpec());
    }

    @JsonIgnore
    public boolean isPartAdmitImmediatePaymentClaimSettled() {
        return (isPartAdmitClaimSpec()
            && (Objects.nonNull(getApplicant1AcceptAdmitAmountPaidSpec())
            && YesOrNo.YES.equals(getApplicant1AcceptAdmitAmountPaidSpec()))
            && (Objects.isNull(getApplicant1AcceptPartAdmitPaymentPlanSpec())));
    }

    @JsonIgnore
    public boolean isFullAdmitClaimSpec() {
        return FULL_ADMISSION.equals(getRespondent1ClaimResponseTypeForSpec());
    }

    @JsonIgnore
    public boolean isFullAdmitPayImmediatelyClaimSpec() {
        return isFullAdmitClaimSpec() && isPayImmediately();
    }

    @JsonIgnore
    public boolean isPartAdmitPayImmediatelyClaimSpec() {
        return isPartAdmitClaimSpec() && isPayImmediately();
    }

    @JsonIgnore
    public boolean isClaimantIntentionSettlePartAdmit() {
        return YesOrNo.YES.equals(getApplicant1PartAdmitIntentionToSettleClaimSpec());
    }

    @JsonIgnore
    public boolean isClaimantIntentionNotSettlePartAdmit() {
        return YesOrNo.NO.equals(getApplicant1PartAdmitIntentionToSettleClaimSpec());
    }

    @JsonIgnore
    public boolean isClaimantConfirmAmountPaidPartAdmit() {
        return YesOrNo.YES.equals(getApplicant1PartAdmitConfirmAmountPaidSpec());
    }

    @JsonIgnore
    public boolean isClaimantConfirmAmountNotPaidPartAdmit() {
        return YesOrNo.NO.equals(getApplicant1PartAdmitConfirmAmountPaidSpec());
    }

    @JsonIgnore
    public YesOrNo doesPartPaymentRejectedOrItsFullDefenceResponse() {
        if (isClaimantNotSettlePartAdmitClaim()
            || (RespondentResponseTypeSpec.FULL_DEFENCE.equals(getRespondent1ClaimResponseTypeForSpec())
            && !(NO.equals(getApplicant1ProceedWithClaim()))
            && !(NO.equals(getApplicant1ProceedWithClaimSpec2v1())))) {
            return YES;
        }
        return NO;
    }

    @JsonIgnore
    public boolean isClaimantNotSettlePartAdmitClaim() {
        return hasDefendantNotPaid()
            || isSettlementDeclinedByClaimant()
            || isClaimantRejectsClaimAmount();
    }

    @JsonIgnore
    public boolean hasDefendantNotAgreedToFreeMediation() {
        return NO.equals(getResponseClaimMediationSpecRequired());
    }

    @JsonIgnore
    public boolean isFastTrackClaim() {
        return FAST_CLAIM.name().equals(getResponseClaimTrack());
    }

    @JsonIgnore
    public boolean isSmallClaim() {
        return SMALL_CLAIM.name().equals(getResponseClaimTrack());
    }

    @JsonIgnore
    public boolean isRejectWithNoMediation() {
        return isClaimantNotSettlePartAdmitClaim()
            && isMediationRejectedOrFastTrack();
    }

    @JsonIgnore
    public boolean isMediationRejectedOrFastTrack() {
        return ((hasClaimantNotAgreedToFreeMediation()
            || hasDefendantNotAgreedToFreeMediation())
            || isFastTrackClaim());
    }

    @JsonIgnore
    public String getApplicantOrganisationId() {
        return getOrganisationId(ofNullable(getApplicant1OrganisationPolicy()));
    }

    @JsonIgnore
    public String getRespondent1OrganisationId() {
        return getOrganisationId(ofNullable(getRespondent1OrganisationPolicy()));
    }

    @JsonIgnore
    public String getRespondent2OrganisationId() {
        return getOrganisationId(ofNullable(getRespondent2OrganisationPolicy()));
    }

    @JsonIgnore
    private String getOrganisationId(Optional<OrganisationPolicy> policy) {
        return policy
            .map(OrganisationPolicy::getOrganisation)
            .map(Organisation::getOrganisationID)
            .orElse("");
    }

    @JsonIgnore
    public boolean isPartAdmitPayImmediatelyAccepted() {
        return SPEC_CLAIM.equals(getCaseAccessCategory())
            && YES.equals(getApplicant1AcceptAdmitAmountPaidSpec())
            && (getShowResponseOneVOneFlag() != null
            && getShowResponseOneVOneFlag().equals(ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY));
    }

    @JsonIgnore
    public Optional<Element<CaseDocument>> getSDODocument() {
        return getLatestDocumentOfType(DocumentType.SDO_ORDER);
    }

    @JsonIgnore
    public Optional<Element<CaseDocument>> getHiddenSDODocument() {
        return getLatestHiddenDocumentOfType(DocumentType.SDO_ORDER);
    }

    @JsonIgnore
    public Optional<Element<CaseDocument>> getTranslatedSDODocument() {
        return getLatestDocumentOfType(DocumentType.SDO_TRANSLATED_DOCUMENT);
    }

    @JsonIgnore
    public Optional<Element<CaseDocument>> getDecisionReconsiderationDocument() {
        return getLatestDocumentOfType(DocumentType.DECISION_MADE_ON_APPLICATIONS);
    }

    @JsonIgnore
    public Optional<Element<CaseDocument>> getTranslatedDecisionReconsiderationDocument() {
        return getLatestDocumentOfType(DocumentType.DECISION_MADE_ON_APPLICATIONS_TRANSLATED);
    }

    @JsonIgnore
    public Optional<Element<CaseDocument>> getLatestDocumentOfType(DocumentType documentType) {
        return ofNullable(systemGeneratedCaseDocuments)
            .flatMap(docs -> docs.stream()
                .filter(doc -> doc.getValue().getDocumentType().equals(documentType))
                .max(Comparator.comparing(doc -> doc.getValue().getCreatedDatetime())));
    }

    @JsonIgnore
    public Optional<Element<CaseDocument>> getLatestHiddenDocumentOfType(DocumentType documentType) {
        return ofNullable(preTranslationDocuments)
            .flatMap(docs -> docs.stream()
                .filter(doc -> doc.getValue().getDocumentType().equals(documentType))
                .max(Comparator.comparing(doc -> doc.getValue().getCreatedDatetime())));
    }

    @JsonIgnore
    public Optional<List<CaseDocument>> getDocumentListByType(List<Element<CaseDocument>> documentCollection, DocumentType documentType) {
        List<CaseDocument> documents = documentCollection.stream()
            .map(Element::getValue)
            .filter(doc -> doc.getDocumentType().equals(documentType))
            .toList();
        return ofNullable(documents.isEmpty() ? null : documents);
    }

    @JsonIgnore
    public Optional<Element<CaseDocument>> getDecisionOnReconsiderationDocumentFromList() {
        if (getSystemGeneratedCaseDocuments() != null) {
            return getSystemGeneratedCaseDocuments().stream()
                .filter(systemGeneratedCaseDocument -> systemGeneratedCaseDocument.getValue()
                    .getDocumentType().equals(DocumentType.DECISION_MADE_ON_APPLICATIONS)).findAny();
        }
        return empty();
    }

    @JsonIgnore
    public boolean isCcjRequestJudgmentByAdmission() {
        return getCcjPaymentDetails() != null
            && getCcjPaymentDetails().getCcjPaymentPaidSomeOption() != null;
    }

    @JsonIgnore
    public boolean hasCoscCert() {
        if (getSystemGeneratedCaseDocuments() != null) {
            return getSystemGeneratedCaseDocuments().stream()
                .filter(systemGeneratedCaseDocument -> systemGeneratedCaseDocument.getValue()
                    .getDocumentType().equals(DocumentType.CERTIFICATE_OF_DEBT_PAYMENT)).findAny().isPresent();
        }
        return false;
    }

    @JsonIgnore
    public Address getRespondent1CorrespondanceAddress() {
        return ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getRespondent1LiPResponse)
            .map(RespondentLiPResponse::getRespondent1LiPCorrespondenceAddress)
            .orElse(null);
    }

    @JsonIgnore
    public RespondToClaim getResponseToClaim() {
        return getRespondToAdmittedClaim() != null ? getRespondToAdmittedClaim() : getRespondToClaim();
    }

    @JsonIgnore
    public List<Element<RecurringIncomeLRspec>> getRecurringIncomeForRespondent1() {
        if (isFullAdmitClaimSpec()) {
            return ofNullable(getRespondent1DQ()).map(Respondent1DQ::getRespondent1DQRecurringIncomeFA).orElse(
                null);
        }
        return ofNullable(getRespondent1DQ()).map(Respondent1DQ::getRespondent1DQRecurringIncome).orElse(null);
    }

    @JsonIgnore
    public List<Element<RecurringExpenseLRspec>> getRecurringExpensesForRespondent1() {
        if (isFullAdmitClaimSpec()) {
            return ofNullable(getRespondent1DQ()).map(Respondent1DQ::getRespondent1DQRecurringExpensesFA)
                .orElse(
                    null);
        }
        return ofNullable(getRespondent1DQ()).map(Respondent1DQ::getRespondent1DQRecurringExpenses).orElse(
            null);
    }

    @JsonIgnore
    public List<Element<ManageDocument>> getManageDocumentsList() {
        return ofNullable(getManageDocuments()).orElse(new ArrayList<>());
    }

    @JsonIgnore
    public boolean getApplicant1ResponseDeadlinePassed() {
        return getApplicant1ResponseDeadline() != null
            && getApplicant1ResponseDeadline().isBefore(LocalDateTime.now())
            && getApplicant1ProceedWithClaim() == null;
    }

    @JsonIgnore
    public String getApplicant1Email() {
        return ofNullable(getApplicant1().getPartyEmail())
            .or(() -> ofNullable(getClaimantUserDetails())
                .map(IdamUserDetails::getEmail))
            .or(() -> ofNullable(getApplicantSolicitor1UserDetails())
                .map(IdamUserDetails::getEmail))
            .orElse(null);
    }

    @JsonIgnore
    public String getHelpWithFeesReferenceNumber() {
        return ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getHelpWithFees)
            .map(HelpWithFees::getHelpWithFeesReferenceNumber).orElse(null);
    }

    @JsonIgnore
    public boolean isHelpWithFees() {
        return getCaseDataLiP() != null && getCaseDataLiP().getHelpWithFees() != null
            && YES.equals(getCaseDataLiP().getHelpWithFees().getHelpWithFee());
    }

    @JsonIgnore
    public Address getRespondent1CorrespondenceAddress() {
        return ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getRespondent1LiPResponse)
            .map(RespondentLiPResponse::getRespondent1LiPCorrespondenceAddress)
            .orElse(null);
    }

    @JsonIgnore
    public String getCurrentCamundaBusinessProcessName() {
        return ofNullable(getBusinessProcess())
            .map(BusinessProcess::getCamundaEvent)
            .orElse(null);
    }

    @JsonIgnore
    public boolean isTranslatedDocumentUploaded() {
        if (getSystemGeneratedCaseDocuments() != null) {
            return getSystemGeneratedCaseDocuments().stream()
                .filter(systemGeneratedCaseDocument -> systemGeneratedCaseDocument.getValue()
                    .getDocumentType().equals(DocumentType.DEFENCE_TRANSLATED_DOCUMENT)).findAny().isPresent();
        }
        return false;
    }

    @JsonIgnore
    public boolean isRespondentSolicitorRegistered() {
        return YesOrNo.YES.equals(getRespondent1OrgRegistered());
    }

    @JsonIgnore
    public boolean isRespondentTwoSolicitorRegistered() {
        return YesOrNo.YES.equals(getRespondent2OrgRegistered());
    }

    @JsonIgnore
    public String getRespondent1Email() {
        if (isRespondent1NotRepresented()) {
            return getRespondent1().getPartyEmail();
        }
        if (isRespondentSolicitorRegistered()) {
            return getRespondentSolicitor1EmailAddress();
        }
        return null;
    }

    @JsonIgnore
    public boolean isRespondentRespondedToSettlementAgreement() {
        return getCaseDataLiP() != null && getCaseDataLiP().getRespondentSignSettlementAgreement() != null;
    }

    @JsonIgnore
    public boolean isRespondentSignedSettlementAgreement() {
        return getCaseDataLiP() != null && YesOrNo.YES.equals(getCaseDataLiP().getRespondentSignSettlementAgreement());
    }

    @JsonIgnore
    public boolean isRespondentRejectedSettlementAgreement() {
        return getCaseDataLiP() != null && YesOrNo.NO.equals(getCaseDataLiP().getRespondentSignSettlementAgreement());
    }

    @JsonIgnore
    public List<ClaimAmountBreakupDetails> getClaimAmountBreakupDetails() {
        return ofNullable(getClaimAmountBreakup())
            .map(Collection::stream)
            .map(claimAmountBreakupStream -> claimAmountBreakupStream
                .map(item -> new ClaimAmountBreakupDetails(
                    MonetaryConversions.penniesToPounds(item.getValue().getClaimAmount()),
                    item.getValue().getClaimReason()
                ))
                .toList())
            .orElse(Collections.emptyList());

    }

    @JsonIgnore
    public BigDecimal getCalculatedClaimFeeInPence() {
        return ofNullable(getClaimFee())
            .map(Fee::getCalculatedAmountInPence)
            .orElse(BigDecimal.ZERO);
    }

    @JsonIgnore
    public BigDecimal getCalculatedHearingFeeInPence() {
        return ofNullable(getHearingFee())
            .map(Fee::getCalculatedAmountInPence)
            .orElse(BigDecimal.ZERO);
    }

    @JsonIgnore
    public BigDecimal getClaimAmountInPounds() {
        if (nonNull(getClaimValue())) {
            return getClaimValue().toPounds();
        } else if (nonNull(getTotalInterest())) {
            return getTotalClaimAmount()
                .add(getTotalInterest())
                .setScale(2, RoundingMode.UNNECESSARY);
        } else if (nonNull(getTotalClaimAmount())) {
            return getTotalClaimAmount().setScale(2, RoundingMode.UNNECESSARY);
        }
        return null;
    }

    @JsonIgnore
    public BigDecimal getClaimIssueRemissionAmount() {
        return ofNullable(getClaimIssuedHwfDetails())
            .map(HelpWithFeesDetails::getRemissionAmount)
            .orElse(BigDecimal.ZERO);
    }

    @JsonIgnore
    public BigDecimal getHearingRemissionAmount() {
        return ofNullable(getHearingHwfDetails())
            .map(HelpWithFeesDetails::getRemissionAmount)
            .orElse(BigDecimal.ZERO);
    }

    public boolean hasApplicant1SignedSettlementAgreement() {
        return ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .filter(ClaimantLiPResponse::hasApplicant1SignedSettlementAgreement).isPresent();

    }

    @JsonIgnore
    public boolean isHWFTypeHearing() {
        return getHwfFeeType() == FeeType.HEARING;
    }

    @JsonIgnore
    public boolean isHWFTypeClaimIssued() {
        return getHwfFeeType() == FeeType.CLAIMISSUED;
    }

    @JsonIgnore
    public CaseEvent getHwFEvent() {
        if (this.isHWFTypeHearing() && this.getHearingHwfDetails() != null) {
            return this.getHearingHwfDetails().getHwfCaseEvent();
        }
        if (this.isHWFTypeClaimIssued() && this.getClaimIssuedHwfDetails() != null) {
            return this.getClaimIssuedHwfDetails().getHwfCaseEvent();
        }
        return null;
    }

    @JsonIgnore
    public boolean isHWFOutcomeReady() {
        return (this.getCcdState() == CaseState.PENDING_CASE_ISSUED && this.isHWFTypeClaimIssued())
            || (this.getCcdState() == CaseState.HEARING_READINESS && this.isHWFTypeHearing());
    }

    @JsonIgnore
    public String getHwFReferenceNumber() {
        if (this.isHWFTypeHearing()) {
            return this.getHearingHelpFeesReferenceNumber();
        }
        if (this.isHWFTypeClaimIssued()) {
            return this.getHelpWithFeesReferenceNumber();
        }
        return null;
    }

    @JsonIgnore
    public BigDecimal getHwFFeeAmount() {
        if (this.isHWFTypeHearing()) {
            return MonetaryConversions.penniesToPounds(this.getCalculatedHearingFeeInPence());
        }
        if (this.isHWFTypeClaimIssued()) {
            return MonetaryConversions.penniesToPounds(this.getCalculatedClaimFeeInPence());
        }
        return null;
    }

    @JsonIgnore
    public BigDecimal getRemissionAmount() {
        if (this.isHWFTypeHearing()) {
            return MonetaryConversions.penniesToPounds(this.getHearingRemissionAmount());
        }
        if (this.isHWFTypeClaimIssued()) {
            return MonetaryConversions.penniesToPounds(this.getClaimIssueRemissionAmount());
        }
        return null;
    }

    @JsonIgnore
    public BigDecimal getOutstandingFeeInPounds() {
        if (this.isHWFTypeHearing() && this.getHearingHwfDetails() != null) {
            return this.getHearingHwfDetails().getOutstandingFeeInPounds();
        }
        if (this.isHWFTypeClaimIssued() && this.getClaimIssuedHwfDetails() != null) {
            return this.getClaimIssuedHwfDetails().getOutstandingFeeInPounds();
        }
        return null;
    }

    @JsonIgnore
    public boolean isSettlementAgreementDeadlineExpired() {
        return nonNull(respondent1RespondToSettlementAgreementDeadline)
            && LocalDateTime.now().isAfter(respondent1RespondToSettlementAgreementDeadline);
    }

    @JsonIgnore
    public boolean hasApplicant1AcceptedCcj() {
        return ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .filter(ClaimantLiPResponse::hasApplicant1RequestedCcj).isPresent();
    }

    @JsonIgnore
    public boolean isDateAfterToday(LocalDate date) {
        return nonNull(date)
            && date.atTime(DeadlinesCalculator.END_OF_BUSINESS_DAY).isAfter(LocalDateTime.now());
    }

    @JsonIgnore
    public boolean hearingFeePaymentDoneWithHWF() {
        return Objects.nonNull(getHearingHelpFeesReferenceNumber())
            && Objects.nonNull(getFeePaymentOutcomeDetails())
            && Objects.nonNull(getFeePaymentOutcomeDetails().getHwfFullRemissionGrantedForHearingFee());
    }

    @JsonIgnore
    public String getAssignedTrack() {
        return nonNull(getAllocatedTrack()) ? getAllocatedTrack().name() : getResponseClaimTrack();
    }

    @JsonIgnore
    public AllocatedTrack getAssignedTrackType() {
        return nonNull(getAssignedTrack()) ? AllocatedTrack.valueOf(getAssignedTrack()) : null;
    }

    @JsonIgnore
    public boolean hasApplicant1AcceptedCourtDecision() {
        return ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .filter(ClaimantLiPResponse::hasClaimantAcceptedCourtDecision).isPresent();
    }

    @JsonIgnore
    public boolean hasApplicant1CourtDecisionInFavourOfClaimant() {
        return ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .filter(ClaimantLiPResponse::hasCourtDecisionInFavourOfClaimant).isPresent();
    }

    public boolean hasApplicant1CourtDecisionInFavourOfDefendant() {
        return ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .filter(ClaimantLiPResponse::hasCourtDecisionInFavourOfDefendant).isPresent();
    }

    @JsonIgnore
    public boolean claimIssueFeePaymentDoneWithHWF() {
        return Objects.nonNull(getHelpWithFeesReferenceNumber())
            && Objects.nonNull(getFeePaymentOutcomeDetails())
            && Objects.nonNull(getFeePaymentOutcomeDetails().getHwfFullRemissionGrantedForClaimIssue());
    }

    @JsonIgnore
    public boolean claimIssueFullRemissionNotGrantedHWF() {
        return Objects.nonNull(getFeePaymentOutcomeDetails())
            && Objects.nonNull(getFeePaymentOutcomeDetails().getHwfFullRemissionGrantedForClaimIssue())
            && getFeePaymentOutcomeDetails().getHwfFullRemissionGrantedForClaimIssue() == NO;
    }

    @JsonIgnore
    public boolean hearingFeeFullRemissionNotGrantedHWF() {
        return Objects.nonNull(getFeePaymentOutcomeDetails())
            && Objects.nonNull(getFeePaymentOutcomeDetails().getHwfFullRemissionGrantedForHearingFee())
            && getFeePaymentOutcomeDetails().getHwfFullRemissionGrantedForHearingFee() == NO;
    }

    @JsonIgnore
    public boolean isLipvLROneVOne() {
        return !isRespondent1LiP()
            && isApplicant1NotRepresented()
            && isOneVOne(this);
    }

    @JsonIgnore
    public boolean nocApplyForLiPClaimant() {
        return isLRvLipOneVOne()
            && (getClaimIssuedPaymentDetails() == null
            && (claimIssueFeePaymentDoneWithHWF()
            || getChangeOfRepresentation() != null));
    }

    @JsonIgnore
    public LocalDate getApplicant1ClaimSettleDate() {
        return ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1ClaimSettledDate).orElse(null);
    }

    @JsonIgnore
    public boolean isPaidLessThanClaimAmount() {
        RespondToClaim localRespondToClaim = null;
        if (getRespondent1ClaimResponseTypeForSpec() == FULL_DEFENCE) {
            localRespondToClaim = getRespondToClaim();
        } else if (getRespondent1ClaimResponseTypeForSpec() == PART_ADMISSION) {
            localRespondToClaim = getRespondToAdmittedClaim();
        }
        return ofNullable(localRespondToClaim).map(RespondToClaim::getHowMuchWasPaid)
            .map(paid -> MonetaryConversions.penniesToPounds(paid).compareTo(totalClaimAmount) < 0).orElse(false);
    }

    @JsonIgnore
    public boolean isCourtDecisionInClaimantFavourImmediateRePayment() {
        return hasApplicant1CourtDecisionInFavourOfClaimant()
            && getApplicant1RepaymentOptionForDefendantSpec() == PaymentType.IMMEDIATELY;
    }

    @JsonIgnore
    public boolean nocApplyForLiPDefendantBeforeOffline() {
        return isLipvLROneVOne() && getChangeOfRepresentation() != null;
    }

    @JsonIgnore
    public boolean nocApplyForLiPDefendant() {
        return isLipvLROneVOne() && getChangeOfRepresentation() != null && this.getCcdState() == CaseState.PROCEEDS_IN_HERITAGE_SYSTEM;
    }

    @JsonIgnore
    public boolean isJudgeOrderVerificationRequired() {
        return (this.getCourtPermissionNeeded() != null && SettleDiscontinueYesOrNoList.YES.equals(this.getCourtPermissionNeeded()));
    }

    @JsonIgnore
    public boolean isClaimantDontWantToProceedWithFulLDefenceFD() {
        return this.isClaimBeingDisputed()
            && this.hasApplicantNotProceededWithClaim();
    }

    @JsonIgnore
    public boolean isLipCase() {
        return this.isApplicant1NotRepresented() || this.isRespondent1LiP();
    }

    @JsonIgnore
    public boolean isHearingFeePaid() {
        return nonNull(this.getHearingFeePaymentDetails())
            && SUCCESS.equals(this.getHearingFeePaymentDetails().getStatus()) || this.hearingFeePaymentDoneWithHWF();
    }

    @JsonIgnore
    public boolean isCcjRequestJudgmentByAdmissionDefendantNotPaid() {
        return getCcjPaymentDetails() != null
            && getCcjPaymentDetails().getCcjPaymentPaidSomeOption() != null
            && NO.equals(getCcjPaymentDetails().getCcjPaymentPaidSomeOption());
    }

    @JsonIgnore
    public String getApplicantSolicitor1UserDetailsEmail() {
        return applicantSolicitor1UserDetails == null ? null : applicantSolicitor1UserDetails.getEmail();
    }

    @JsonIgnore
    public String getClaimantUserDetailsEmail() {
        final IdamUserDetails claimantUserDetails = getClaimantUserDetails();
        return claimantUserDetails == null ? null : claimantUserDetails.getEmail();
    }

    @JsonIgnore
    public String getRespondent1PartyEmail() {
        final Party party = getRespondent1();
        return party == null ? null : party.getPartyEmail();
    }

    @JsonIgnore
    public String getRespondent2PartyEmail() {
        final Party party = getRespondent2();
        return party == null ? null : party.getPartyEmail();
    }

    @JsonIgnore
    public boolean isClaimUnderTranslationAfterDefResponse() {
        return this.getRespondent1ClaimResponseTypeForSpec() != null
            && this.getCcdState() == CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
    }

    @JsonIgnore
    public boolean isClaimUnderTranslationAfterClaimantResponse() {
        return this.getApplicant1ResponseDate() != null
            && this.getCcdState() == CaseState.AWAITING_APPLICANT_INTENTION;
    }

    @JsonIgnore
    public boolean isOtherRemedyClaim() {
        return this.getClaimType() != null
            && (ClaimTypeUnspec.DAMAGES_AND_OTHER_REMEDY.equals(this.getClaimTypeUnSpec())
            || ClaimTypeUnspec.HOUSING_DISREPAIR.equals(this.getClaimTypeUnSpec()));
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "Bundle configuration",
          searchable = false,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateCruAccess.class, CaseworkerCivilAdminCaseworkerCivilSolicitorCruAccess.class, HearingScheduleAccessCruAccess.class}
  )
  private String bundleConfiguration;
  @JsonProperty("TrialBundles")
  @CCD(
          label = "Bundles",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCuAccess.class, CaseworkerCivilAdminJudgeProfileLegalAdviserCuAccess.class, CaseworkerCivilSolicitorCuAccess.class, CaseworkerCivilStaffCuAccess.class, HearingScheduleAccessCuAccess.class}
  )
  private String trialBundles;
  @CCD(
          label = "If you or your client did not attend the mediation appointment to represent your case the judge may issue a penalty. The Judge may order your party to pay costs or automatically rule in the other party's favour. \n\nIf you or your client were unable to attend mediation due to exceptional circumstances, you can explain why with supporting evidence such as a doctor's note or a photograph. If the Judge accepts the reasons given for non-attendance, your party will not be issued with a penalty.\n\nYou do not have to upload supporting evidence if you or your client attended the mediation appointment.\n\nYou cannot withdraw a document once you have submitted it. If you want to add more information to something you have already submitted. you can upload the document again. You should add a version number to the name, for example, 'version 2'.\n\nThe other parties will be able to see the documents you have uploaded and you will be able to see their documents.\n\n### Deadlines for uploading documents\n\n You have until 14 days before the hearing to submit documents. You do not have to upload all documents at once. You can return to upload them later.\n\n### Before you upload your documents\n\n Before you upload a document, give it a name that tells the court what it is, for example \"Mediation non-attendance statement by Jane Smith\".\n\nEach document must be less than 100MB. You can upload the following file types: DOC/DOCX (Word), XLS/XLSM (Excel). PPT/PPTX (PowerPoint). PDF, RTF, TXT, CSV. LPG/JPEG, PNG. BMP. TIF /TIFF.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, APPSOLSPECPROFILECruCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String uploadMediationDocumentsExplanation;
  @CCD(
          label = "You can use the options below to let the court know who this document is from",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, APPSOLSPECPROFILECruCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String whoIsDocumentForLabel;
  @CCD(
          label = "You can select more than one type of document \n\n ## Mediation non-attendance",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, APPSOLSPECPROFILECruCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String documentTypeLabel1;
  @CCD(
          label = "<details><summary><u>Mediation non-attendance</u></summary><div class=\"panel\">\n\n ### Non-attendance statement \n\n A written statement explaining why the mediation appointment was not attended \n\n ### Documents referred to in the statement \n\n Documents referred to in the statement - for example emails, photographs of relevant documents or doctor’s notes</div></details><br />",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, APPSOLSPECPROFILECruCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String documentTypeLabel2;
  @CCD(
          label = "## Mediation non-attendance",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, APPSOLSPECPROFILECruCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String mediationDocumentUploadLabel;
  @CCD(
          label = "<div class=\"govuk-warning-text\"><span class=\"govuk-warning-text__icon\" aria-hidden=\"true\">!</span><strong class=\"govuk-warning-text__text\"><span class=\"govuk-warning-text__assistive\">Warning</span>You cannot withdraw a document once you have uploaded it</strong></div>",
          typeOverride = FieldType.Label,
          access = {RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, APPSOLSPECPROFILECruCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String mediationDocumentUploadWarning;
  @CCD(
          label = "## Mediation contact information\n\nPlease provide the contact details of the individual who will conduct the mediation appointment.\n\nThis should be a party to the claim or their legal representative.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffJudgeProfileRAccess.class, APPSOLSPECPROFILECruAccess.class, CaseworkerCivilSolicitorRAccess.class}
  )
  private String contactDetailsLabel;
  @CCD(
          label = "${hearingHelpFeesReferenceNumber}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRCuiAdminProfileCruAccess.class}
  )
  private String caseLabelHearingReference;
  @CCD(
          label = "Help with Fees type: HwF Hearing fee",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRCuiAdminProfileCruAccess.class}
  )
  private String hearingFeeLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-1\"> ${hearingHelpFeesReferenceNumber} ${applicant1.partyName} Vs ${respondent1.partyName} </h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRCuiAdminProfileCruAccess.class}
  )
  private String caseTitleWithHWFHearingNumber;
  @CCD(
          label = "Date claimant response Submitted",
          access = {CITIZENCLAIMANTPROFILECruAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private java.time.LocalDateTime claimantResponseSubmittedDate;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilSystemupdateCrAccess.class}
  )
  private CaseDocument applicant1JudgmentByDeterminationForm;
  @CCD(
          label = "Administrator Case Documents",
          searchable = false,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<CaseDocument>> caseWorkerDocuments;
  @CCD(
          label = "## Manual Determination",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String headingManualDetermination;
  @CCD(
          label = "Defendant Proposed Plan",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String headingDefendantProposedPlan;
  @CCD(
          label = "Defendant Financial Details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String headingDefendantFinancialDetails;
  @CCD(
          label = "Claimant Proposed Plan",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String headingClaimantProposedPlan;
  @CCD(
          label = "List ${respondent1.partyName}'s bank and savings accounts",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String headingBankAccount;
  @CCD(
          label = "Does ${respondent1.partyName} receive any disability premium payments?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String headingDisabilityPremiumPayments;
  @CCD(
          label = "Where does ${respondent1.partyName} live?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String headingHomeDetails;
  @CCD(
          label = "Is ${respondent1.partyName} in employment?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String headingEmployment;
  @CCD(
          label = "Is ${respondent1.partyName} paying money as a result of any court order?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String headingCourtOrder;
  @CCD(
          label = "Case file view",
          searchable = false,
          typeOverride = FieldType.ComponentLauncher,
          access = {CaseworkerCivilSystemFieldReaderRPlus7RolesIlgoorAccess.class}
  )
  private String componentLauncher;
  @CCD(
          label = "Launch the Flags screen",
          searchable = false,
          typeOverride = FieldType.FlagLauncher,
          access = {CaseflagsAdminCruCaseflagsViewerRWluAdminCruAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String flagLauncher;
  @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRCuiAdminProfileCruAccess.class})
  private HelpWithFees helpWithFeesReferenceNumber;
  @CCD(
          label = "${helpWithFees.helpWithFeesReferenceNumber}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRCuiAdminProfileCruAccess.class}
  )
  private String caseLabelClaimIssueReference;
  @CCD(
          label = "<h2 class=\"govuk-heading-1\">  ${helpWithFees.helpWithFeesReferenceNumber} ${applicant1.partyName} Vs ${respondent1.partyName}</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRCuiAdminProfileCruAccess.class}
  )
  private String caseTitleWithHWFClaimFeeNumber;
  @CCD(
          label = "<h4 class=\"govuk-heading-1\"> HwF Details</h4>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRCuiAdminProfileCruAccess.class}
  )
  private String subTitleHwfDetails;
  @CCD(
          label = "## Hwf Details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRCuiAdminProfileCruAccess.class}
  )
  private String caseTitleWithHwFDetails;
  @CCD(
          label = "Help with Fees type: HwF Claim fee",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRCuiAdminProfileCruAccess.class}
  )
  private String claimFeeLabel;
  @CCD(
          label = "<h4 class=\"govuk-heading-1\"> Warning: Please check you are entering the correct amount, as you will not be able to amend after submission.</h4>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String remissionFeeAmountWarningMessage;
  @CCD(
          label = "### ${applicant1.partyName} Vs ${respondent1.partyName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRCuiAdminProfileCruAccess.class}
  )
  private String caseTitle2;
  @CCD(
          label = "## HwF Details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRCuiAdminProfileCuAccess.class}
  )
  private String headingHelpWithFeesDetails;
  @CCD(
          label = "${joRepaymentSummaryObject}",
          searchable = false,
          typeOverride = FieldType.Label,
          gate = "!CCD_DEF_ENV:prod",
          access = {CITIZENCLAIMANTPROFILECuPlus2RolesSzdyetAccess.class, CaseworkerCivilAdminCaseworkerCivilStaffCuAccess.class}
  )
  private String joRepaymentSummaryLabel;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.Label,
          gate = "!CCD_DEF_ENV:prod",
          access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCruAccess.class}
  )
  private String lblRecordJudgment;
  @CCD(
          label = "**${caseNameHmctsInternal}** <br> <br>",
          searchable = false,
          typeOverride = FieldType.Label,
          gate = "!CCD_DEF_ENV:prod",
          access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, APPSOLSPECPROFILECruAccess.class, CaseworkerCivilStaffCruAccess.class}
  )
  private String joPartiesNames;
  @CCD(
          label = " ",
          searchable = false,
          gate = "!CCD_DEF_ENV:prod",
          access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffCruAccess.class}
  )
  private java.util.Set<JoConfirmSentToBothDef> joConfirmSentToBothDef;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.Label,
          gate = "!CCD_DEF_ENV:prod",
          access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, APPSOLSPECPROFILECruAccess.class, CaseworkerCivilStaffCruAccess.class}
  )
  private String lblMarkPaidInFull;
  @CCD(
          label = "Any cheques or transfers should be clear in your account.<p>You need to tell us if you’ve settled the claim, for example because the defendant has paid you.</p><p>You can settle for less than the full claim amount.</p><p><h3>If you haven’t been paid.</h3></p><p>If the defendant has not paid you, you can request a County Court Judgment by selecting 'Request Judgment by Admission' from the 'Next step' drop down list.</p>",
          searchable = false,
          typeOverride = FieldType.Label,
          gate = "!CCD_DEF_ENV:prod",
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class, CaseworkerCivilSolicitorCruAccess.class}
  )
  private String respondentPayImmediatelyTextAfterDate;
  @CCD(
          label = "You should discuss the complexity band allocation with the other party. Read <a href='https://www.justice.gov.uk/courts/procedure-rules/civil/rules/part26' rel=\"noreferrer noopener\" target=\"_blank\">CPR26.16 Table 2</a></div><br /> for information on complexity bands.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, JudgeProfileLegalAdviserRAccess.class}
  )
  private String applicantDQFixedRecoverableCostsIntermediateLabel;
  @CCD(
          label = "You should discuss the complexity band allocation with the other party. Read <a href='https://www.justice.gov.uk/courts/procedure-rules/civil/rules/part26' rel=\"noreferrer noopener\" target=\"_blank\">CPR26.16 Table 2</a></div><br /> for information on complexity bands.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess.class, JudgeProfileLegalAdviserRAccess.class, RESSOLTWOUNSPECPROFILECruAccess.class}
  )
  private String respondentDQFixedRecoverableCostsIntermediateLabel;
  @CCD(
          label = "**** \n ### Allocation \n For information on which complexity band the claim should be in see <a href='https://www.justice.gov.uk/courts/procedure-rules/civil/rules/part26#16' rel=\"noreferrer noopener\" target=\"_blank\">CPR26.16 Table 2</a>. \n\nThe claim is allocated to the Intermediate Track.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String finalOrderIntermediateTrackComplexityBandLabel;
  @CCD(
          label = "**** \n ### Which template do you wish to use?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String selectTemplateLabel;
  @CCD(
          label = "**** \n ## You must now download the template \n Open the selected template and download it to your computer to complete the order.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String downloadTemplateLabel1;
  @CCD(
          label = "\n Once you've completed the order, you can save and upload it on the next screen.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String downloadTemplateLabel2;
  @CCD(
          label = "**** \n ## Add document \n Upload your completed order in DOC/DOCX (Word) format.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String uploadOrderDocumentFromTemplateLabel;
  @CCD(
          label = "## What type of hearing have you listed?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRHearingScheduleAccessCruAccess.class, CaseworkerCivilJudgeProfileRAccess.class}
  )
  private String hearingListedDynamicListLabel;
  @CCD(
          label = "## Issue civil court proceedings\n### Who can use this service\n### Please note: You can only use this service if you are a solicitor organisation\nYou must be:\n* bringing a claim for damages\n* issuing a claim to the county court.  Claims cannot be issued to the High Court using this service\n* representing 1 claimant against up to 2 Defendants, or 2 claimants against 1 defendant \n* able to pay the full issue fee using Payment by Account (PBA) or card payment\n\nIf the defendant has legal representation, you'll need:\n* name of the defendant's firm of solicitors \n* email address the firm uses for notifications from MyHMCTS \n\n### About the Claimants\n Claimants must: \n* have an address in England or Wales \n* have a litigation friend who can provide a certificate of suitability if they are under 18 \n* not be a protected party as defined in CPR 21.1(2)(d)\n* not have an all proceedings order, civil proceedings order, or civil restraint order in force against them\n\n### About the Defendants \n Defendants must (as far as the Claimant is aware):\n* have a postal address for service in England or Wales\n* be aged 18 years or older\n* not be a protected party\n\n### About the claim\nA County Court damages claim is only suitable for the pilot if it:\n* would not ordinarily follow the Part 8 procedure, except for claims requiring an ‘Other Remedy’\n* meets all the conditions in PD51ZB 1.6(3)\n* is conducted in English \n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECrAccess.class}
  )
  private String eligibilityQuestions;
  @CCD(
          label = "**${caseNameHmctsInternal}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String reasonForReconsiderationPartiesName;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, APPSOLSPECPROFILECruCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String reasonForReconsiderationSeparator;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class, APPSOLSPECPROFILERPlus5RolesPvjgzlAccess.class}
  )
  private JudgeDecisionOnReconsiderationSelect judgeResponseToReconsiderationDetailsNo;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class, APPSOLSPECPROFILERPlus5RolesPvjgzlAccess.class}
  )
  private JudgeDecisionOnReconsiderationSelect judgeResponseToReconsiderationDetailsAmending;
  @CCD(
          label = "## Flight Delay ",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String smallClaimsFlightDelayTitle;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String smallClaimsFlightDelayStartLine;
  @CCD(
          label = "**${caseNameHmctsInternal}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoPartiesName;
  @CCD(
          label = "**${caseNameHmctsInternal}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoPartiesNameDRH;
  @CCD(
          label = "****\n<div class=\"govuk-warning-text\"><span class=\"govuk-warning-text__icon\" aria-hidden=\"true\">!</span><strong class=\"govuk-warning-text__text\"><span class=\"govuk-warning-text__assistive\"></span>All dates should be in the format 16 4 2021</strong></div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoWarningDateFormat;
  @CCD(
          label = "****\n<div class=\"govuk-warning-text\"><span class=\"govuk-warning-text__icon\" aria-hidden=\"true\">!</span><strong class=\"govuk-warning-text__text\"><span class=\"govuk-warning-text__assistive\"></span>All dates should be in the format 16 4 2021</strong></div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoWarningDateFormatDRH;
  @CCD(
          label = "****\n ### Warning\nYou must comply with the terms imposed upon you by this Order otherwise your claim or the defence of it is liable to be struck out or some other sanction imposed. If you cannot comply, you are expected to make a formal application to the Court before any deadline imposed upon you expires.\n ****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoWarning;
  @CCD(
          label = "### Judge’s recital",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoJudgesRecitalLbl;
  @CCD(
          label = "****\n ### Allocation\n The claim is allocated to the Fast Track. \n****\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoAllocation;
  @CCD(
          label = "### Alternative Dispute Resolution",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoAltDisputeResolutionLbl;
  @CCD(
          label = "****\n ### Variation of directions",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoSeparatorVariationOfDirections;
  @CCD(
          label = "****\n ### Settlement",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoSeparatorSettlement;
  @CCD(
          label = "****\n ### Disclosure of documents",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoSeparatorDisclosureOfDocument;
  @CCD(
          label = "****\n ### Witnesses of Fact",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoSeparatorWitnessesOfFact;
  @CCD(
          label = "****\n ### Schedule of loss",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoSeparatorScheduleOfLoss;
  @CCD(
          label = "****\n ### Add a new direction",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoSeparatorAddNewDirection;
  @CCD(
          label = "****\n ### Trial",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoSeparatorTrial;
  @CCD(
          label = "****\n ### Important notes",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoSeparatorImportantNotes;
  @CCD(
          label = "****\n ### Expert Evidence",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoSeparatorExpertEvidence;
  @CCD(
          label = "****\n ### Addendum report",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoSeparatorAddendumReport;
  @CCD(
          label = "****\n ### Further audiogram",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoSeparatorFurtherAudiogram;
  @CCD(
          label = "****\n ### Questions of the Claimant's expert",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoSeparatorQuestionsClaimantExpert;
  @CCD(
          label = "****\n ### Permission for any Defendant to rely on expert evidence from a consultant ENT surgeon",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoSeparatorPermissionToRelyOnExpert;
  @CCD(
          label = "****\n ### Evidence of an expert acoustic engineer",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoSeparatorEvidenceAcousticEngineer;
  @CCD(
          label = "****\n ### Questions to ENT expert(s) after engineering evidence",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoSeparatorQuestionsToEntExpert;
  @CCD(
          label = "****\n ### Upload of documents",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoSeparatorUploadOfDocuments;
  @CCD(
          label = "****\n ### Warning\nYou must comply with the terms imposed upon you by this Order otherwise your claim or the defence of it is liable to be struck out or some other sanction imposed. If you cannot comply, you are expected to make a formal application to the Court before any deadline imposed upon you expires.\n\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoR2SmallClaimsWarning1;
  @CCD(
          label = "\n  You are encouraged to try to settle the case with the other side. You may also contact the Small Claims Mediation Service to arrange an appointment. The service is free and can be contacted on 01604 795 511.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoR2SmallClaimsWarning2;
  @CCD(
          label = "****\n### Judge’s recital",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoR2SmallClaimsJudgesRecitalLbl;
  @CCD(
          label = "****\n ### Allocation\n The claim is allocated to the Small Claims Track.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoR2SmallClaimsAllocation;
  @CCD(
          label = "****\n ### Dispute Resolution Hearing\n  The claim is listed for a Dispute Resolution Hearing before a District Judge on the date and at the time indicated on the Notice of Hearing which will follow separately.\n  A Dispute Resolution Hearing is a preliminary hearing for the purposes of Civil Procedure Rule 27.6.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoR2SmallClaimsDisputeResolutionHearing;
  @CCD(
          label = "#### Legal representation for DRH\n  If a party is legally represented at the dispute resolution hearing and the party is not also in attendance, they must have provided full instructions including as to settlement and be contactable so that meaningful negotiations can take place. Failure to have done so may result in the hearing being adjourned and a costs order being made against the party at fault.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoR2SmallClaimsLRForDRH;
  @CCD(
          label = "#### Judges powers at DRH",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoR2SmallClaimsJudgesPowersDRH;
  @CCD(
          label = " At the Dispute Resolution Hearing the Judge may:  \n  a) Strike out the claim, the defence, and any counterclaim and/or any defence to counterclaim if the court finds that the statement of case discloses no reasonable grounds for bringing or defending the claim or if it is considered a party has no real prospect of success at a final hearing.  \n  b) If a party fails to provide a contact number for the hearing, fails to attend the hearing, or fails to comply with the directions set out in this Order the court may strike out the claim, defence, and/or counterclaim.  \n  c) Conduct mediation with the parties’ consent, to assist the parties to reach an agreed resolution of the claim so that the dispute can be resolved completely at the Dispute Resolution Hearing and/or identify the real issues in the dispute.  \n  d) List any further hearing including final hearing and/or make an Order requiring the parties to take further steps prior to a further or final hearing and provide that if the same are not carried out, that the Statement of Case of any party in default will be struck out.  \n  e) Make any other Order which the court considers appropriate.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoR2SmallClaimsJudgesPowersDRH2;
  @CCD(
          label = "****\n ### Payment Protection Insurance (PPI)",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoR2SmallClaimsPPILbl;
  @CCD(
          label = "****\n ### Witness statements",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoR2SmallClaimsWitnessesOfFactLbl;
  @CCD(
          label = "****\n ### Upload of documents",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoR2SmallClaimsUploadDocLbl;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoR2SmallClaimsAddNewDirectionLbl;
  @CCD(
          label = "****\n ### Hearing",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoR2SmallClaimsHearingLbl;
  @CCD(
          label = "****\n ### Mediation representation",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoR2SmallClaimsMediationSectionTitle;
  @CCD(
          label = "****\n ### Important notes",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoR2SmallClaimsImpNotesLbl;
  @CCD(
          label = "****\n ### Use of the Welsh Language",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoR2NIHLUseOfWelshLanguageTitle;
  @CCD(
          label = "****\n ### Use of the Welsh Language",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoR2DRHUseOfWelshLanguageTitle;
  @CCD(
          label = "### Use of the Welsh Language",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoR2FastTrackUseOfWelshLanguageTitle;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String sdoR2FastTrackWelshLangEndLine;
  @CCD(
          label = "****\n ### Use of the Welsh Language",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoR2SmallClaimsUseOfWelshLanguageTitle;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoR2SmallClaimsWelshLangEndLine;
  @CCD(
          label = "****\n ### Use of the Welsh Language",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String sdoR2DisposalHearingUseOfWelshLanguageTitle;
  @CCD(
          label = "Select additional directions, if any ",
          searchable = false,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private java.util.Set<OrderTypeTrialAdditionalDirections> orderTypeTrialAdditionalDirections;
  @CCD(
          label = "Order and hearing details",
          searchable = false,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private DisposalHearingOrderAndHearingDetails disposalHearingOrderAndHearingDetails;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private DisposalHearingJudgementDeductionStatement disposalHearingJudgementDeductionStatement;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String disposalHearingDisclosureOfDocumentsStartLine;
  @CCD(
          label = "## Disclosure of documents",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String disposalHearingDisclosureOfDocumentsTitle;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String disposalHearingDisclosureOfDocumentsEndLine;
  @CCD(
          label = "## Witnesses of Fact",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String disposalHearingWitnessOfFactTitle;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String disposalHearingWitnessOfFactEndLine;
  @CCD(
          label = "## Expert evidence",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String disposalHearingMedicalEvidenceTitle;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String disposalHearingMedicalEvidenceEndLine;
  @CCD(
          label = "## Questions to experts",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String disposalHearingQuestionsToExpertsTitle;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String disposalHearingQuestionsToExpertsEndLine;
  @CCD(
          label = "## Schedules or counter-schedules of loss",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String disposalHearingSchedulesOfLossTitle;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String disposalHearingSchedulesOfLossEndLine;
  @CCD(
          label = "## Hearing time",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String disposalHearingFinalDisposalHearingTitle;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String disposalHearingFinalDisposalHearingEndLine;
  @CCD(
          label = "## Hearing Method",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String disposalHearingMethodTitle;
  @CCD(
          label = "Parties must file at court details of their respective relevant email addresses by 4.00pm\n 7 days before the disposal hearing so that the court may send out invitations for them to join the disposal hearing",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String disposalHearingMethodVideoConferenceHearingCourtStatement;
  @CCD(
          label = "Parties must file at court details of their respective relevant telephone numbers by 4.00pm\n 7 days before the disposal hearing so that the court may telephone them to join them to the disposal hearing",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String disposalHearingMethodTelephoneHearingCourtStatement;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String disposalHearingHearingNotesStartLine;
  @CCD(
          label = "## Hearing notes",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilAdminCruAccess.class}
  )
  private String disposalHearingHearingNotesTitle;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String disposalHearingBundleStartLine;
  @CCD(
          label = "## Disposal hearing bundle",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String disposalHearingBundleTitle;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String disposalHearingBundleEndLine;
  @CCD(
          label = "## Claim Settling",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String disposalHearingClaimSettlingTitle;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private DisposalHearingClaimSettling disposalHearingClaimSettling;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String disposalHearingClaimSettlingEndLine;
  @CCD(
          label = "## Costs",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String disposalHearingCostsTitle;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private DisposalHearingCosts disposalHearingCosts;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String disposalHearingCostsEndLine;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String disposalHearingAddNewDirectionsEndLine;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private SmallClaimsOrderAndHearingDetails smallClaimsOrderAndHearingDetails;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private SmallClaimsJudgementDeductionStatement smallClaimsJudgementDeductionStatement;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private SmallClaimsAllocation smallClaimsAllocation;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String smallClaimsHearingStartLine;
  @CCD(
          label = "## Hearing time",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String smallClaimsHearingTitle;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String smallClaimsHearingEndLine;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String smallClaimsDocumentsStartLine;
  @CCD(
          label = "## Documents",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String smallClaimsDocumentsTitle;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String smallClaimsDocumentsEndLine;
  @CCD(
          label = "## Witness Statement",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String smallClaimsWitnessStatementTitle;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String smallClaimsWitnessStatementEndLine;
  @CCD(
          label = "## Hearing Method",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String smallClaimsMethodTitle;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String smallClaimsAddNewDirectionsEndLine;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private FastTrackOrderAndHearingDetails fastTrackOrderAndHearingDetails;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private FastTrackJudgementDeductionStatement fastTrackJudgementDeductionStatement;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String fastTrackAltDisputeResolutionStartLine;
  @CCD(
          label = "## Alternative dispute resolution",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String fastTrackAltDisputeResolutionTitle;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private FastTrackAltDisputeResolution fastTrackAltDisputeResolution;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String fastTrackAltDisputeResolutionEndLine;
  @CCD(
          label = "## Variation of directions",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String fastTrackVariationOfDirectionsTitle;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private FastTrackVariationOfDirections fastTrackVariationOfDirections;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String fastTrackVariationOfDirectionsEndLine;
  @CCD(
          label = "## Settlement",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String fastTrackSettlementTitle;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private FastTrackSettlement fastTrackSettlement;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String fastTrackSettlementEndLine;
  @CCD(
          label = "## Disclosure of documents",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String fastTrackDisclosureOfDocumentsTitle;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String fastTrackDisclosureOfDocumentsEndLine;
  @CCD(
          label = "## Witnesses of Fact",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String fastTrackWitnessOfFactTitle;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String fastTrackWitnessOfFactEndLine;
  @CCD(
          label = "## Schedules of loss",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String fastTrackSchedulesOfLossTitle;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String fastTrackSchedulesOfLossEndLine;
  @CCD(
          label = "## Costs",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String fastTrackCostsTitle;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private FastTrackCosts fastTrackCosts;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String fastTrackCostsEndLine;
  @CCD(
          label = "## Hearing time",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String fastTrackTrialTitle;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String fastTrackTrialEndLine;
  @CCD(
          label = "## Trial bundles",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {JudgeProfileLegalAdviserCruAccess.class}
  )
  private String fastTrackTrialBundleTitle;
  @CCD(
          label = "If by a date no later than 14 days before the Trial the claim is proceeding in the Digital Portal, then the following directions in this section will apply on the basis the bundle (\"bundle\") will be automatically generated in the Digital Portal for use by the parties and the Judge at the hearing.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {JudgeProfileLegalAdviserCruAccess.class}
  )
  private String fastTrackTrialBundleText;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {JudgeProfileLegalAdviserCruAccess.class}
  )
  private String fastTrackTrialBundleEndLine;
  @CCD(
          label = "## Hearing Method",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String fastTrackMethodTitle;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String fastTrackMethodEndLine;
  @CCD(
          label = "Employer's liability",
          searchable = false,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private FastTrackEmployersLiability fastTrackEmployersLiability;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String fastTrackAddNewDirectionsEndLine;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilAdminCruAccess.class, CaseworkerCivilStaffCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo claimantResponseDocumentsContainsDefenceAndDirectionsDocuments;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilAdminCruAccess.class, CaseworkerCivilStaffCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo defendantResponseDocumentsContainsDefenceAndDirectionsDocuments;
  @CCD(
          label = "## Mediation representation",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String smallClaimsMediationSectionTitle;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String smallClaimsMediationSectionEndLine;
  @CCD(
          label = "## Penal notice (optional)",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {JudgeProfileLegalAdviserRAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String smallClaimsPenalNoticeTitle;
  @CCD(
          label = "## Penal notice (optional)",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {JudgeProfileLegalAdviserRAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String fastTrackPenalNoticeTitle;
  @CCD(
          label = "**${caseNameHmctsInternal}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CaseworkerCivilStaffCruAccess.class}
  )
  private String partiesNames;
  @CCD(
          label = "**${caseNameHmctsInternal}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {APPSOLSPECPROFILERPlus2RolesUugowqAccess.class}
  )
  private String partiesNames1;
  @CCD(
          label = "**${caseNameHmctsInternal}** \n****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {APPSOLSPECPROFILERPlus2RolesUugowqAccess.class}
  )
  private String partiesNames3;
  @CCD(
          label = "The court's permission to discontinue a claim is not required if:<br /> &bull; The court has not granted an interim injuction<br /> &bull; No party has given an undertaking to the court<br /> &bull; The claimant has not received an interim payment<br /> &bull; The claimant has received an interim payment and the defendant has consented in writing to the <br />&nbsp; discontinuance",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String courtPermissionNeededText;
  @CCD(
          label = " ",
          searchable = false,
          access = {APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private java.util.Set<ConfirmCourtPermissionNotNeeded> courtPermissionNeededChecked;
  @CCD(
          label = "**${caseNameHmctsInternal}** \n****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {APPSOLSPECPROFILERPlus2RolesUugowqAccess.class}
  )
  private String partiesNamesDD;
  @CCD(
          label = "**${caseNameHmctsInternal}** \n****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {APPSOLSPECPROFILERPlus2RolesUugowqAccess.class}
  )
  private String partiesNames4;
  @CCD(
          label = "### Unable to discontinue this claim \n To discontinue this claim you need to get permission from the court.\n Click cancel to return to the case summary screen and select 'Make\n an application' from the next steps menu and select 'Other'",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {APPSOLSPECPROFILERPlus2RolesUugowqAccess.class}
  )
  private String permissionNotGrantedText;
  @CCD(
          label = "**${caseNameHmctsInternal}** \n****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {APPSOLSPECPROFILERPlus2RolesUugowqAccess.class}
  )
  private String partiesNamesTD;
  @CCD(
          label = "**${caseNameHmctsInternal}** \n****\n ### Permission granted by:",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilStaffRAccess.class}
  )
  private String validateDiscontinuanceLbl;
  @CCD(
          label = "### Can you confirm the Order does give permission to discontinue all or part of the claim?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilStaffRAccess.class}
  )
  private String confirmOrderGivesPermissionLbl;
  @CCD(
          label = "### You must update the court of the method of settlement.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class}
  )
  private String lblUpdateCourtMultiple;
  @CCD(
          label = "### You must update the court of the method of settlement.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class}
  )
  private String lblUpdateCourtSingle;
  @CCD(
          label = "## What is the reason for not drawing a Standard Directions Order? \n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class}
  )
  private String lblReasonNotSuitableSDO;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String lblTransferCaseDetails;
  @CCD(
          label = "Update a citizen’s language preference by selecting the user, and their preferred language in which to receive correspondence",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILERAccess.class, APPSOLSPECPROFILERESSOLONESPECPROFILERAccess.class, CaseworkerCivilAdminCaseworkerCivilSystemupdateCrudAccess.class, CaseworkerCivilCrudAccess.class}
  )
  private String changeLanguagePreferenceLabel;
  @CCD(
          label = "<strong class=\"text-16\">Translation of system generated document in progress.</strong>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilCudPlus2RolesVlykptAccess.class}
  )
  private String documentTranslationInProgressLabel;
  @CCD(
          label = "<h2>A party in this case has expressed a Welsh language preference. Delays to visibility of documents could be caused during translation processes</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {JudgeProfileCruAccess.class}
  )
  private String bilingualHintTextNotification;
  @CCD(
          label = "<div class=\"govuk-warning-text\"><span class=\"govuk-warning-text__icon\" aria-hidden=\"true\">!</span><strong class=\"govuk-warning-text__text\"><span class=\"govuk-warning-text__assistive\">Warning</span>You must make sure you have taken action to address the failed bundle before you attempt to restitch.</strong></div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CuiAdminProfileRAccess.class}
  )
  private String bundleWarning;
  @CCD(
          label = "## Amend and restitch bundle\nMake sure you have taken the correct action to amend the bundle before you restitch.  \nThe restitched bundle will replace the existing bundle.  \nAll parties will be notified.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CuiAdminProfileRAccess.class}
  )
  private String bundleText;
  @CCD(
          label = "## Stay the case \n\n All parties will be notified.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CuiAdminProfileRAccess.class}
  )
  private String stayCaseText;
  @CCD(
          label = "## Dismiss the case\nThis case will be dismissed. Parties will be able to view the case details but not take any actions on it.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CuiAdminProfileRAccess.class}
  )
  private String dismissText;
  @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRAccess.class})
  private String manageStayDummyField;
  @CCD(
          label = "What do you need to do?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CuiAdminProfileRAccess.class}
  )
  private String manageStayOptionsLabel;
  @CCD(
          label = "A notification will be sent to all parties, asking them for an update on the case. \n\n After 7 days, a new task will be created to progress the case.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CuiAdminProfileRAccess.class}
  )
  private String manageStayRequestUpdateHintText;
  @CCD(
          label = "By lifting the stay, this case will automatically be sent to a judge. \n\n This will also raise a work allocation task for a judge to make a standard directions order for this case.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CuiAdminProfileRAccess.class}
  )
  private String manageStayJudicialReferralInMediationText;
  @CCD(
          label = "By lifting the stay, this case will return to 'Case progression' state. \n\n A caseworker may need to schedule the next hearing for this case.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CuiAdminProfileRAccess.class}
  )
  private String manageStayCaseProgressedHearingReadyPrepareForHearingText;
  @CCD(
          label = "You are requesting an update on this case, the stay will not be lifted. \n\n A notification will be sent to all parties, asking for an update on the case. \n\n After 7 days, a new task will be created to progress the case.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CuiAdminProfileRAccess.class}
  )
  private String manageStayRequestUpdateText;
  @CCD(
          label = "You must complete all of your tasks before you can submit your order review.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CuiAdminProfileCruAccess.class}
  )
  private String stillTasksText;
  @CCD(
          label = "## Send a message",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String sendMessageTitle;
  @CCD(
          label = "## Send a message",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, JudgeProfileLegalAdviserRAccess.class, WluAdminRAccess.class}
  )
  private String sendMessageContextTitle;
  @CCD(
          label = "## Send a message",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, JudgeProfileLegalAdviserRAccess.class, WluAdminRAccess.class}
  )
  private String sendMessageContentTitle;
  @CCD(
          label = "## Reply to a message",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CaseworkerCivilAdminWluAdminCruAccess.class}
  )
  private String replyToMessageTitle;
  @CCD(
          label = "${messageHistory}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruLegalAdviserCruAccess.class, CaseworkerCivilAdminWluAdminCruAccess.class}
  )
  private String messageHistoryLabel;
  @CCD(
          label = " ",
          searchable = false,
          access = {CITIZENCLAIMANTPROFILECuPlus2RolesSzdyetAccess.class, CaseworkerCivilAdminJudgeProfileLegalAdviserCuAccess.class, GSProfileCuAccess.class, CaseworkerWaTaskConfigurationCruAccess.class, WluAdminCuAccess.class}
  )
  private String caseHistory;
  @CCD(
          label = "Claimant 1 Remote Hearing Questions",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECuAccess.class, CaseworkerCivilSolicitorCruAccess.class}
  )
  private RemoteHearing applicant1DQRemoteHearing;
  @CCD(
          label = "### You don't have a registered PBA.\n### This case will not be saved.\n## Register an existing PBA with MyHMCTS\n\nIf you want to pay with a different PBA, you or your organisation administrator will need to email MyHMCTSsupport@justice.gov.uk to ask for your PBA to be registered with your MyHMCTS account. You should include your organisation name and PBA number.\n\nIt can then take up to 3 days for your account to be updated. You’ll need to start your claim again to pay the fee.\n## Apply to get a new PBA\n\nYou can <a target=\"_blank\" href=\"https://www.gov.uk/government/publications/form-fee-account-application-form-fee-account-customer-application-form\">use this form to apply for a new PBA account.</a>\n\nYou’ll need to provide details for you and your organisation, including the required credit limit for your account.\n\nOnce your account has been registered, you’ll need to start your claim again to pay the fee.\n\nRead more information on <a target=\"_blank\" href=\"https://www.gov.uk/guidance/myhmcts-online-case-management-for-legal-professionals\">registering a PBA.</a>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilSolicitorCruCaseworkerCivilStaffRAccess.class}
  )
  private String noPbaAccountsLabel;
  @CCD(
          label = "<details><summary><u>Pay with another PBA</u></summary><div class=\"panel\">\n\n## If there’s 1 PBA listed\n\nIf you want to pay with a different PBA, you or your organisation administrator will need to email MyHMCTSsupport@justice.gov.uk to ask for your PBA to be registered with your MyHMCTS account. You should include your organisation name and PBA number.\n\nIt can then take up to 3 days for your account to be updated. You’ll need to start your claim again to pay the fee.\n## If there are 2 PBAs listed\n\nYou won’t be able to add another PBA.\n\nYou can only submit this case online using one of the PBAs from the menu.\n\nIf you want to remove a PBA, you or your organisation administrator will need to email MyHMCTSsupport@justice.gov.uk. You should include the PBA number. It can then take up to 3 days for the PBA to be removed. We’ll send you confirmation.\n\n\nSelect ‘cancel’ below to discard the case.\n</div></details><br />",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilSolicitorCruCaseworkerCivilStaffRAccess.class}
  )
  private String pbaAccountsInformationLabel;
  @CCD(
          label = "The claimant(s) believes that the facts stated in the brief details of claim are true. \n\nI am duly authorised by the claimant(s) to sign this statement.\n\nThe claimant(s) understands that proceedings for contempt of court may be brought against anyone who makes, or causes to be made, a false statement in a document verified by a statement of truth without an honest belief in its truth.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilSolicitorCruCaseworkerCivilStaffRAccess.class}
  )
  private String claimStatementOfTruthLabel;
  @CCD(
          label = "<br/>The Claimant legal representative has provided the following name and address for the Defendant.\nIf these are not correct, you should contact the Claimant's legal representative.\n\n**Claimant's email address**<br/>${applicantSolicitor1UserDetails.email}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
  )
  private String respondentDetailsAcknowledgeClaim;
  @CCD(
          label = "The claimant has provided the following name and address for the defendant.\n\nIf this is not correct, you should contact the claimant's legal representative.\n\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class, RESSOLTWOUNSPECPROFILERAccess.class}
  )
  private String confirmRespondentDetailsAdvice;
  @CCD(
          label = "**Party type:** ${respondent1Copy.partyTypeDisplayValue}\n\n**Name:** ${respondent1Copy.partyName} ${respondent1Copy.soleTraderTradingAs}\n\n\n\n**Address:**<br />\r\n${respondent1Copy.primaryAddress.AddressLine1}<br />${respondent1Copy.primaryAddress.AddressLine2}<br />${respondent1Copy.primaryAddress.AddressLine3}<br />${respondent1Copy.primaryAddress.PostTown}<br />${respondent1Copy.primaryAddress.County}<br />${respondent1Copy.primaryAddress.PostCode}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
  )
  private String respondentDetails;
  @CCD(
          label = "**Claimant’s email address:** ${applicantSolicitor1UserDetails.email}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class, RESSOLTWOUNSPECPROFILERAccess.class}
  )
  private String claimantSolicitorEmail;
  @CCD(
          label = "<br/>**Both defendants intend to:**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILERAccess.class}
  )
  private String respondentBothLabel;
  @CCD(
          label = "<br/>**Defendant: ${respondent1.partyName} intends to**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent1PartyNameLabel;
  @CCD(
          label = "<br/>**Defendant: ${respondent1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent1PartyNameLabelDisclosureOfElectronicDocuments;
  @CCD(
          label = "<br/>**Defendant: ${respondent1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent1PartyNameLabelVulnerabilityQuestions;
  @CCD(
          label = "<br/>**Defendant: ${respondent1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent1PartyNameLabelFileDirectionsQuestionnaire;
  @CCD(
          label = "<br/>**Defendant: ${respondent1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent1PartyNameLabelUpload;
  @CCD(
          label = "<br/>**Defendant: ${respondent2.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLTWOUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent2PartyNameLabelUpload;
  @CCD(
          label = "<br/>**Defendant: ${respondent1.partyName} intends to**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent1PartyNameLabel2;
  @CCD(
          label = "**Defendant: ${respondent2.partyName} intends to**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLTWOUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent2PartyNameLabel;
  @CCD(
          label = "**Defendant: ${respondent2.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLTWOUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent2PartyNameLabelFileDirectionsQuestionnaire;
  @CCD(
          label = "**against Claimant: ${applicant1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyNameLabel;
  @CCD(
          label = "**against Claimant: ${applicant1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyNameLabelDisclosureOfElectronicDocuments1;
  @CCD(
          label = "**${applicant1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyNameLabelVulnerabilityQuestions1;
  @CCD(
          label = "**against Claimant: ${applicant1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyNameLabelFileDirectionsQuestionnaire1;
  @CCD(
          label = "**against Claimant: ${applicant1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyNameLabelFileDirectionsQuestionnaire2;
  @CCD(
          label = "**against Claimant: ${applicant1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyNameLabelUpload1;
  @CCD(
          label = "**against Claimant: ${applicant1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyNameLabelUpload2;
  @CCD(
          label = "**against Claimant: ${applicant1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLTWOUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyNameLabel2;
  @CCD(
          label = "The case processing has not completed, please refresh this page in one minute. If the problem persists please contact the helpdesk 0300 123 7050",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CITIZENCLAIMANTPROFILEUAccess.class, CITIZENDEFENDANTPROFILEUAccess.class, CaseworkerCivilAdminUAccess.class, CaseworkerCivilSolicitorUAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class, CitizenProfileUAccess.class}
  )
  private String unfinishedBackEndProcess;
  @CCD(
          label = "**against Claimant: ${applicant2.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant2PartyNameLabel;
  @CCD(
          label = "You will be required to complete a single directions questionnaire for all defendants.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondentSingleResponseRequiredLabel;
  @CCD(
          label = "This online service only currently supports full defences. Post your response to:\n\nCounty Court Money Claims Centre\n\nPO Box 527\n\nSalford\n\nM5 0BY\n\nDX:702634 Salford 5",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String responseHandOffLabel;
  @CCD(
          label = "## Upload draft directions",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilSolicitorCaseworkerCivilStaffRAccess.class}
  )
  private String respondent1DQDraftDirectionsLabel;
  @CCD(
          label = "## Upload draft directions",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilSolicitorCaseworkerCivilStaffRAccess.class}
  )
  private String respondent2DQDraftDirectionsLabel;
  @CCD(
          label = "## Upload draft directions",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLUNSPECPROFILECaseworkerCivilStaffRAccess.class, APPSOLSPECPROFILERAccess.class}
  )
  private String applicant1DQDraftDirectionsLabel;
  @CCD(
          label = "Enter organisation details manually. The claim will then continue offline.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCrudAccess.class, CaseworkerCivilSolicitorCruAccess.class}
  )
  private String respondentSolicitor1OrgLabel;
  @CCD(
          label = "If you continue the defendant legal representative's organisation will be notified and will be granted access to this claim.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilSolicitorCaseworkerCivilStaffRAccess.class}
  )
  private String notifyClaimLabel;
  @CCD(
          label = "## Provide brief details of the claim\nBriefly describe the claim but do not include the full Particulars of Claim. You can provide the Particulars of Claim on the next page.\n\nUse this space to provide additional details such as the date of the accident if making a Personal Injury claim, the date of the alleged negligence if making a clinical negligence claim, the contract start date if making a claim for breach of contract and any other supporting details.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffRAccess.class}
  )
  private String detailsOfClaimLabel;
  @CCD(
          label = "#### Your payment did not go through. To resubmit the same claim please try again by selecting the PBA account number",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String pbaPaymentFailedLabel;
  @CCD(
          label = "Update a legal representative's email address by replacing the existing email.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffRAccess.class}
  )
  private String amendPartyDetailsLabel;
  @CCD(
          label = "In order to verify the claim by a statement of truth please click \"submit\" on the next screen. Doing so will be taken as verifying the claim by the above statement of truth. Alternatively, you will have the opportunity, on the next screen, to change any of the answers before clicking the \"submit\" button.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilSolicitorCruCaseworkerCivilStaffRAccess.class}
  )
  private String statementOfTruthSubmitLabel;
  @CCD(
          label = "Custom Payment History Viewer",
          searchable = false,
          typeOverride = FieldType.CasePaymentHistoryViewer,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String casePaymentHistoryViewer;
  @CCD(
          label = "You can add another Claimant after you have issued the claim but you will need to submit a separate application.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CaseworkerCivilSolicitorCruAccess.class}
  )
  private String addApplicant2Label;
  @CCD(
          label = "Enter organisation details manually. The claim will then continue offline.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCrudAccess.class, CaseworkerCivilSolicitorCruAccess.class}
  )
  private String respondentSolicitor2OrgLabel;
  @CCD(
          label = "Claimant 1 Name",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class, RESSOLTWOUNSPECPROFILERAccess.class}
  )
  private String claimant1Name;
  @CCD(
          label = "Claimant 2 Name",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String claimant2Name;
  @CCD(
          label = "**Defendant: ${respondent2.partyName} intends to**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class, RESSOLTWOUNSPECPROFILERAccess.class}
  )
  private String respondent2PartyName;
  @CCD(
          label = "**Defendant: ${respondent2.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLTWOUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent2PartyNameLabelFurtherInformation;
  @CCD(
          label = "**Defendant: ${respondent2.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLTWOUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent2PartyNameLabelHearingSupport;
  @CCD(
          label = "**Defendant: ${respondent2.partyName} intends to**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLTWOUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent2PartyNameLabelRequestedCourt;
  @CCD(
          label = "**Defendant: ${respondent2.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLTWOUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent2PartyNameLabelDraftDirections;
  @CCD(
          label = "**Defendant: ${respondent2.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLTWOUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent2PartyNameLabelHearing;
  @CCD(
          label = "**Defendant: ${respondent2.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLTWOUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent2PartyNameLabelLanguage;
  @CCD(
          label = "**Defendant: ${respondent2.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLTWOUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent2PartyNameLabelWitnesses;
  @CCD(
          label = "**Defendant: ${respondent2.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLTWOUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent2PartyNameLabelExperts;
  @CCD(
          label = "**Defendant: ${respondent2.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLTWOUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent2PartyNameLabelDisclosureReport;
  @CCD(
          label = "**Defendant: ${respondent2.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLTWOUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent2PartyNameLabelVulnerabilityQuestions;
  @CCD(
          label = "**Defendant: ${respondent2.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLTWOUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent2PartyNameLabelDisclosureOfNonElectronicDocuments;
  @CCD(
          label = "**Defendant: ${respondent2.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLTWOUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent2PartyNameLabelDisclosureOfElectronicDocuments;
  @CCD(
          label = "**against Claimant: ${applicant1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyNameLabelDisclosureOfElectronicDocuments2;
  @CCD(
          label = "**against Claimant: ${applicant1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyNameLabelVulnerabilityQuestions2;
  @CCD(
          label = "**against Claimant: ${applicant1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyNameLabelDisclosureOfNonElectronicDocuments1;
  @CCD(
          label = "**against Claimant: ${applicant1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyNameLabelDisclosureOfNonElectronicDocuments2;
  @CCD(
          label = "**against Claimant: ${applicant1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyNameLabelDisclosureReport1;
  @CCD(
          label = "**against Claimant: ${applicant1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyNameLabelDisclosureReport2;
  @CCD(
          label = "**against Claimant: ${applicant1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyNameLabelExperts1;
  @CCD(
          label = "**against Claimant: ${applicant1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyNameLabelExperts2;
  @CCD(
          label = "**against Claimant: ${applicant1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyNameLabelWitnesses1;
  @CCD(
          label = "**against Claimant: ${applicant1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyNameLabelWitnesses2;
  @CCD(
          label = "**against Claimant: ${applicant1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyNameLabelLanguage1;
  @CCD(
          label = "**against Claimant: ${applicant1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyNameLabelLanguage2;
  @CCD(
          label = "**against Claimant: ${applicant1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyNameLabelHearing1;
  @CCD(
          label = "**against Claimant: ${applicant1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyNameLabelHearing2;
  @CCD(
          label = "**against Claimant: ${applicant1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyNameLabelDraftDirections1;
  @CCD(
          label = "**against Claimant: ${applicant1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyNameLabelDraftDirections2;
  @CCD(
          label = "**against Claimant: ${applicant1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyNameLabelRequestedCourt1;
  @CCD(
          label = "**against Claimant: ${applicant1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyNameLabelRequestedCourt2;
  @CCD(
          label = "**against Claimant: ${applicant1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyNameLabelHearingSupport1;
  @CCD(
          label = "**against Claimant: ${applicant1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyNameLabelHearingSupport2;
  @CCD(
          label = "**against Claimant: ${applicant1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyNameLabelFurtherInformation1;
  @CCD(
          label = "**against Claimant: ${applicant1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyNameLabelFurtherInformation2;
  @CCD(
          label = "<br/>**Defendant: ${respondent1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent1PartyNameLabelDisclosureOfNonElectronicDocuments;
  @CCD(
          label = "<br/>**Defendant: ${respondent1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent1PartyNameLabelDisclosureReport;
  @CCD(
          label = "<br/>**Defendant: ${respondent1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent1PartyNameLabelExperts;
  @CCD(
          label = "<br/>**Defendant: ${respondent1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent1PartyNameLabelWitnesses;
  @CCD(
          label = "<br/>**Defendant: ${respondent1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent1PartyNameLabelLanguage;
  @CCD(
          label = "<br/>**Defendant: ${respondent1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent1PartyNameLabelHearing;
  @CCD(
          label = "<br/>**Defendant: ${respondent1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent1PartyNameLabelDraftDirections;
  @CCD(
          label = "<br/>**Defendant: ${respondent1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent1PartyNameLabelRequestedCourt;
  @CCD(
          label = "<br/>**Defendant: ${respondent1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent1PartyNameLabelHearingSupport;
  @CCD(
          label = "<br/>**Defendant: ${respondent1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent1PartyNameLabelFurtherInformation;
  @CCD(
          label = "### Documents uploaded to the Damages Claims Portal prior to the 'notify claim details' next step (refer to PD51ZB section 4), will be visible to the defendant solicitor once the claim has been issued. \n### If you do not want to provide access to these documents prior to completing the 'notify claim details' next step, please do not upload them now.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECrAccess.class, CaseworkerCivilStaffRAccess.class}
  )
  private String documentUploadWarning;
  @CCD(
          label = "Your answers to the questions above will enable the court and the judge, to consider what steps, adjustments or support can be arranged.\n\nYou are reminded that a copy of this directions questionnaire will be shared with all other parties.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffRAccess.class}
  )
  private String vulnerabilityQuestionsInfoLabel;
  @CCD(
          label = "Update the defendant's legal representative's email address by replacing the existing email.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String amendRespondent1SolicitorEmailLabel;
  @CCD(
          label = "Update the second defendant's legal representative's email address by replacing the existing email.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String amendRespondent2SolicitorEmailLabel;
  @CCD(
          label = "Update the claimant's legal representative's email address by replacing the existing email.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String amendApplicant1SolicitorEmailLabel;
  @CCD(
          label = "Update the second claimant's legal representative's email address by replacing the existing email.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String amendApplicant2SolicitorEmailLabel;
  @CCD(
          label = "<br/>**Claimant's response to defendant**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {APPSOLUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String claimantResponseToDefendantLabel;
  @CCD(
          label = "<br/>**Defendant: ${respondent1.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {APPSOLUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String defendant1ResponseToDefendantLabel;
  @CCD(
          label = "<br/>**Defendant: ${respondent2.partyName}**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {APPSOLUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String defendant2ResponseToDefendantLabel;
  @CCD(
          label = "## Upload draft directions",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLUNSPECPROFILECaseworkerCivilStaffRAccess.class}
  )
  private String applicant2DQDraftDirectionsLabel;
  @CCD(
          label = "## Particulars of claim",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String headingParticularsOfClaim;
  @CCD(
          label = "## Claimant details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String headingClaimantDetails;
  @CCD(
          label = "## Defendant details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String headingDefendantDetails;
  @CCD(
          label = "## Claimant directions questionnaire",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String headingClaimantDQ;
  @CCD(
          label = "## Defendant directions questionnaire",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String headingDefendantDQ;
  @CCD(
          label = "## Solicitor references",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorRAccess.class, CaseworkerCivilSystemupdateRAccess.class}
  )
  private String headingSolicitorReferences;
  @CCD(
          label = "## Amount claiming",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorRAccess.class, CaseworkerCivilSystemupdateRAccess.class}
  )
  private String headingAmountClaiming;
  @CCD(
          label = "**Defendant**\n\n**Name:** ${respondent1Copy.partyName} ${respondent1Copy.soleTraderTradingAs}\n\n**Email address:** ${respondent1Copy.partyEmail}\n\n**Phone number:** ${respondent1Copy.partyPhone}\n\n\n\n**Address:**<br />\r\n${respondent1Copy.primaryAddress.AddressLine1}<br />${respondent1Copy.primaryAddress.AddressLine2}<br />${respondent1Copy.primaryAddress.AddressLine3}<br />${respondent1Copy.primaryAddress.PostTown}<br />${respondent1Copy.primaryAddress.County}<br />${respondent1Copy.primaryAddress.PostCode}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent1Details;
  @CCD(
          label = "**Defendant**\n\n**Name:** ${respondent2Copy.partyName} ${respondent2Copy.soleTraderTradingAs}\n\n**Email address:** ${respondent2Copy.partyEmail}\n\n**Phone number:** ${respondent2Copy.partyPhone}\n\n\n\n**Address:**<br />\r\n${respondent2Copy.primaryAddress.AddressLine1}<br />${respondent2Copy.primaryAddress.AddressLine2}<br />${respondent2Copy.primaryAddress.AddressLine3}<br />${respondent2Copy.primaryAddress.PostTown}<br />${respondent2Copy.primaryAddress.County}<br />${respondent2Copy.primaryAddress.PostCode}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class, JudgeProfileLegalAdviserRAccess.class, RESSOLTWOUNSPECPROFILERAccess.class}
  )
  private String respondent2Details;
  @CCD(
          label = "**Defendant**\n\n**Name:** ${respondent2Copy.partyName} ${respondent2Copy.soleTraderTradingAs}\n\n**Email address:** ${respondent2Copy.partyEmail}\n\n**Phone number:** ${respondent2Copy.partyPhone}\n\n\n\n**Address:**<br />\r\n${respondent2Copy.primaryAddress.AddressLine1}<br />${respondent2Copy.primaryAddress.AddressLine2}<br />${respondent2Copy.primaryAddress.AddressLine3}<br />${respondent2Copy.primaryAddress.PostTown}<br />${respondent2Copy.primaryAddress.County}<br />${respondent2Copy.primaryAddress.PostCode}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLTWOUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent2DetailsDiffLR;
  @CCD(
          label = "Service Request",
          searchable = false,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class, HearingScheduleAccessCuAccess.class, PaymentAccessCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.WaysToPay casePaymentWaysToPay;
  @CCD(
          label = "<div class=\"govuk-warning-text\"><span class=\"govuk-warning-text__icon\" aria-hidden=\"true\">!</span><strong class=\"govuk-warning-text__text\"><span class=\"govuk-warning-text__assistive\">Warning</span>You should only be adding a litigation friend for an individual</strong></div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String defendantLitigationFriendWarning;
  @CCD(
          label = "<div class=\"govuk-warning-text\"><span class=\"govuk-warning-text__icon\" aria-hidden=\"true\">!</span><strong class=\"govuk-warning-text__text\"><span class=\"govuk-warning-text__assistive\">Warning</span>You should only be adding a litigation friend for an individual</strong></div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLTWOUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String defendant2LitigationFriendWarning;
  @CCD(
          label = "<div class=\"govuk-warning-text\"><span class=\"govuk-warning-text__icon\" aria-hidden=\"true\">!</span><strong class=\"govuk-warning-text__text\"><span class=\"govuk-warning-text__assistive\">Warning</span>You should only be adding a litigation friend for an individual</strong></div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONEUNSPECPROFILECaseworkerCivilSystemFieldReaderRAccess.class, RESSOLTWOUNSPECPROFILERAccess.class}
  )
  private String defendantCommonLitigationFriendWarning;
  @CCD(
          label = "Claimant's legal representative's correspondence address",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilSolicitorCruAccess.class}
  )
  private String applicantSolicitor1AddressLabel;
  @CCD(
          label = "Defendant legal representative’s address",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilSolicitorCruAccess.class}
  )
  private String respondentSolicitor1AddressLabel;
  @CCD(
          label = "Defendant legal representative’s address",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilSolicitorCruAccess.class}
  )
  private String respondentSolicitor2AddressLabel;
  @CCD(
          label = "Claimant's name <br /> ${applicant1.partyName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class}
  )
  private String cosLabelForClaimant1_1;
  @CCD(
          label = "Claimant's name <br /> ${applicant1.partyName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String cosLabelForClaimant2_1;
  @CCD(
          label = "Defendant's name <br /> ${respondent1.partyName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class}
  )
  private String cosLabelForDefendant1;
  @CCD(
          label = "Defendant's name <br /> ${respondent2.partyName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class}
  )
  private String cosLabelForDefendant2;
  @CCD(
          label = "# Certificate of Service",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String cosLabel;
  @CCD(
          label = "# Certificate of Service [defendant1]",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String cosLabelDef1;
  @CCD(
          label = "# Certificate of Service [defendant2]",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String cosLabelDef2;
  @CCD(
          label = "Claimant's name <br /> ${applicant1.partyName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class}
  )
  private String cosLabelForClaimant1_2;
  @CCD(
          label = "# Defendant 1 details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String cosLabelTabDef1;
  @CCD(
          label = "Certificate of Service - Notify Claim",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String cosLabelTabDef1CoSNc;
  @CCD(
          label = "Certificate of Service - Notify Claim Details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String cosLabelTabDef1CoSNcd;
  @CCD(
          label = "# Defendant 2 details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String cosLabelTabDef2;
  @CCD(
          label = "Certificate of Service - Notify Claim",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String cosLabelTabDef2CoSNc;
  @CCD(
          label = "Certificate of Service - Notify Claim Details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String cosLabelTabDef2CoSNcd;
  @CCD(
          label = "### Which party's contact information do you want to change?\n This includes any other attendees linked to the party, such as:\n* litigation friends\n* experts\n* witnesses\n\n ### Which details do you want to update?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilAdminCrudAccess.class}
  )
  private String choosePartyHeader;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Experts</h2>\n Please update the contact details of any individual who may attend a hearing as an expert.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilAdminCrudAccess.class}
  )
  private String expertsLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Witnesses</h2>\n Please update the contact details of any individual who may attend a hearing as a witness.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilAdminCrudAccess.class}
  )
  private String witnessesLabel;
  @CCD(
          label = "### Claimant: ${applicant1.partyName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilAdminCrudAccess.class}
  )
  private String applicant1PartyNameManageContactLabel;
  @CCD(
          label = "### Claimant: ${applicant2.partyName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilAdminCrudAccess.class}
  )
  private String applicant2PartyNameManageContactLabel;
  @CCD(
          label = "### Defendant: ${respondent1.partyName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilAdminCrudAccess.class}
  )
  private String respondent1PartyNameManageContactLabel;
  @CCD(
          label = "### Defendant: ${respondent2.partyName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilAdminCrudAccess.class}
  )
  private String respondent2PartyNameManageContactLabel;
  @CCD(
          label = "### Do you have a signed order from a judge?\n You can only update the following details if a judge has given their permission to do so.\n* First name\n* Last name\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilAdminCrudCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyTypeIndividualLabel;
  @CCD(
          label = "### Do you have a signed order from a judge?\n You can only update the following details if a judge has given their permission to do so.\n* Company name",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilAdminCrudCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyTypeCompanyLabel;
  @CCD(
          label = "### Do you have a signed order from a judge?\n You can only update the following details if a judge has given their permission to do so.\n* Trading as\n* First name\n* Last name",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilAdminCrudCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1PartyTypeSoleTraderLabel;
  @CCD(
          label = "### Do you have a signed order from a judge?\n You can only update the following details if a judge has given their permission to do so.\n* First name\n* Last name\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilAdminCrudCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant2PartyTypeIndividualLabel;
  @CCD(
          label = "### Do you have a signed order from a judge?\n You can only update the following details if a judge has given their permission to do so.\n* Company name",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilAdminCrudCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant2PartyTypeCompanyLabel;
  @CCD(
          label = "### Do you have a signed order from a judge?\n You can only update the following details if a judge has given their permission to do so.\n* Trading as\n* First name\n* Last name",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilAdminCrudCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant2PartyTypeSoleTraderLabel;
  @CCD(
          label = "### Do you have a signed order from a judge?\n You can only update the following details if a judge has given their permission to do so.\n* First name\n* Last name\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilAdminCrudCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String defendant1PartyTypeIndividualLabel;
  @CCD(
          label = "### Do you have a signed order from a judge?\n You can only update the following details if a judge has given their permission to do so.\n* Company name",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilAdminCrudCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String defendant1PartyTypeCompanyLabel;
  @CCD(
          label = "### Do you have a signed order from a judge?\n You can only update the following details if a judge has given their permission to do so.\n* Trading as\n* First name\n* Last name",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilAdminCrudCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String defendant1PartyTypeSoleTraderLabel;
  @CCD(
          label = "### Do you have a signed order from a judge?\n You can only update the following details if a judge has given their permission to do so.\n* First name\n* Last name\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilAdminCrudCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String defendant2PartyTypeIndividualLabel;
  @CCD(
          label = "### Do you have a signed order from a judge?\n You can only update the following details if a judge has given their permission to do so.\n* Company name",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilAdminCrudCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String defendant2PartyTypeCompanyLabel;
  @CCD(
          label = "### Do you have a signed order from a judge?\n You can only update the following details if a judge has given their permission to do so.\n* Trading as\n* First name\n* Last name",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilAdminCrudCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String defendant2PartyTypeSoleTraderLabel;
  @CCD(
          label = "### Title:\n${applicant1.individualTitle}\n\n### First name:\n${applicant1.individualFirstName}\n\n### Last name:\n${applicant1.individualLastName}\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1IndividualTypeLabel;
  @CCD(
          label = "### Title:\n${applicant2.individualTitle}\n\n### First name:\n${applicant2.individualFirstName}\n\n### Last name:\n${applicant2.individualLastName}\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant2IndividualTypeLabel;
  @CCD(
          label = "### Title:\n${respondent1.individualTitle}\n\n### First name:\n${respondent1.individualFirstName}\n\n### Last name:\n${respondent1.individualLastName}\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent1IndividualTypeLabel;
  @CCD(
          label = "### Title:\n${respondent2.individualTitle}\n\n### First name:\n${respondent2.individualFirstName}\n\n### Last name:\n${respondent2.individualLastName}\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent2IndividualTypeLabel;
  @CCD(
          label = "### Company name:\n${applicant1.companyName}${applicant1.organisationName}\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1OrganisationTypeLabel;
  @CCD(
          label = "### Company name:\n${applicant2.companyName}${applicant2.organisationName}\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant2OrganisationTypeLabel;
  @CCD(
          label = "### Company name:\n${respondent1.companyName}${respondent1.organisationName}\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent1OrganisationTypeLabel;
  @CCD(
          label = "### Company name:\n${respondent2.companyName}${respondent2.organisationName}\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent2OrganisationTypeLabel;
  @CCD(
          label = "### Trading as:\n${applicant1.soleTraderTradingAs}\n\n### First name:\n${applicant1.soleTraderFirstName}\n\n### Last name:\n${applicant1.soleTraderLastName}\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1SoleTraderTypeLabel;
  @CCD(
          label = "### Trading as:\n${applicant2.soleTraderTradingAs}\n\n### First name:\n${applicant2.soleTraderFirstName}\n\n### Last name:\n${applicant2.soleTraderLastName}\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant2SoleTraderTypeLabel;
  @CCD(
          label = "### Trading as:\n${respondent1.soleTraderTradingAs}\n\n### First name:\n${respondent1.soleTraderFirstName}\n\n### Last name:\n${respondent1.soleTraderLastName}\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent1SoleTraderTypeLabel;
  @CCD(
          label = "### Trading as:\n${respondent2.soleTraderTradingAs}\n\n### First name:\n${respondent2.soleTraderFirstName}\n\n### Last name:\n${respondent2.soleTraderLastName}\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent2SoleTraderTypeLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Individuals attending for the legal representatives</h2>\n Please add or update the contact details of any individual who may attend a hearing.\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilAdminCrudAccess.class}
  )
  private String LROrgIndividualsLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Individuals attending for the organisation</h2>\n Please add or update the contact details of any individual who may attend a hearing.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilAdminCrudAccess.class}
  )
  private String orgIndividualsLabel;
  @CCD(
          label = "## Litigation friend",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilAdminCrudAccess.class}
  )
  private String litigationFriendTitle;
  @CCD(
          label = "### Do you have a signed order from a judge?\n You can only update the following details if a judge has given their permission to do so.\n* First name\n* Last name",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilAdminCrudAccess.class}
  )
  private String litigationFriendHeader;
  @CCD(
          label = "### First name:\n${applicant1LitigationFriend.firstName}\n\n### Last name:\n${applicant1LitigationFriend.lastName}\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilAdminCrudAccess.class}
  )
  private String applicant1LitigationFriendTypeLabel;
  @CCD(
          label = "### First name:\n${applicant2LitigationFriend.firstName}\n\n### Last name:\n${applicant2LitigationFriend.lastName}\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilAdminCrudAccess.class}
  )
  private String applicant2LitigationFriendTypeLabel;
  @CCD(
          label = "### First name:\n${respondent1LitigationFriend.firstName}\n\n### Last name:\n${respondent1LitigationFriend.lastName}\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilAdminCrudAccess.class}
  )
  private String defendant1LitigationFriendTypeLabel;
  @CCD(
          label = "### First name:\n${respondent2LitigationFriend.firstName}\n\n### Last name:\n${respondent2LitigationFriend.lastName}\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilAdminCrudAccess.class}
  )
  private String defendant2LitigationFriendTypeLabel;
  @CCD(
          label = "### Details of this claim have been updated.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String contactDetailsChangedLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Unavailable Dates</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String additionalUnavailableDatesTitle;
  @CCD(
          label = "Please add any new dates when a party, or any of their attendees, are not available for a hearing.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String additionalUnavailableDatesSubText;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Which party's availability do you want to update?</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String additionalUnavailableDatesPartyChoiceQuestion;
  @CCD(
          label = "You can find the dates that have already been marked as unavailable in the listing notes tab",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String additionalUnavailableDatesPartyChoiceSubText;
  @CCD(label = "## Defendant response", searchable = false, typeOverride = FieldType.Label)
  private String headingDefendantResponse;
  @CCD(
          label = "${caseNameHmctsInternal} \n****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String caseNameLabelApplicant;
  @CCD(
          label = "${caseNameHmctsInternal} \n****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String caseNameLabelRespondent1;
  @CCD(
          label = "${caseNameHmctsInternal} \n****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String caseNameLabelRespondent2;
  @CCD(
          label = "### Is the case ready for Trial or Hearing?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String trialReadyLabelApplicant;
  @CCD(
          label = "### Is the case ready for Trial or Hearing?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String trialReadyLabelRespondent1;
  @CCD(
          label = "### Is the case ready for Trial or Hearing?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String trialReadyLabelRespondent2;
  @CCD(
          label = "### You will need to make an application to the court if this case is not ready for the trial or hearing. \nThe trial or hearing will go ahead as planned on the specified date unless a judge makes an order changing the date of hearing. If you want the date of hearing to be changed (or any other order to make the case ready for trial) you will need to make an application to the court. \n\nYou will still need to provide the following information on trial arrangements. \n****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicationNotReadyApplicant;
  @CCD(
          label = "### You will need to make an application to the court if this case is not ready for the trial or hearing. \nThe trial or hearing will go ahead as planned on the specified date unless a judge makes an order changing the date of hearing. If you want the date of hearing to be changed (or any other order to make the case ready for trial) you will need to make an application to the court. \n\nYou will still need to provide the following information on trial arrangements. \n****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicationNotReadyRespondent1;
  @CCD(
          label = "### You will need to make an application to the court if this case is not ready for the trial or hearing. \nThe trial or hearing will go ahead as planned on the specified date unless a judge makes an order changing the date of hearing. If you want the date of hearing to be changed (or any other order to make the case ready for trial) you will need to make an application to the court. \n\nYou will still need to provide the following information on trial arrangements. \n****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicationNotReadyRespondent2;
  @CCD(
          label = "### Hearing Duration \nThe hearing time originally allocated is ${hearingDurationTextApplicant}. \n****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String hearingDurationNotReadyApplicant;
  @CCD(
          label = "### Hearing Duration \nThe hearing time originally allocated is ${hearingDurationTextRespondent1}. \n****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String hearingDurationNotReadyRespondent1;
  @CCD(
          label = "### Hearing Duration \nThe hearing time originally allocated is ${hearingDurationTextRespondent2}. \n****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String hearingDurationNotReadyRespondent2;
  @CCD(
          label = "### Hearing Duration \nThe hearing time originally allocated is ${hearingDurationTextApplicant}. \n\nIf you require less time please set out in your reasons in the 'Other Information' box below. \n\nIf you think you will need more time for the hearing, you will need to liaise with the other party and make an application to the court. The time allocated to the hearing or trial will not be increased until an application is received, the fee paid, and an order made. \n****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String hearingDurationReadyApplicant;
  @CCD(
          label = "### Hearing Duration \nThe hearing time originally allocated is ${hearingDurationTextRespondent1}. \n\nIf you require less time please set out in your reasons in the 'Other Information' box below. \n\nIf you think you will need more time for the hearing, you will need to liaise with the other party and make an application to the court. The time allocated to the hearing or trial will not be increased until an application is received, the fee paid, and an order made. \n****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String hearingDurationReadyRespondent1;
  @CCD(
          label = "### Hearing Duration \nThe hearing time originally allocated is ${hearingDurationTextRespondent2}. \n\nIf you require less time please set out in your reasons in the 'Other Information' box below. \n\nIf you think you will need more time for the hearing, you will need to liaise with the other party and make an application to the court. The time allocated to the hearing or trial will not be increased until an application is received, the fee paid, and an order made. \n****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String hearingDurationReadyRespondent2;
  @CCD(
          label = "You can use the options below to let the court know who this document is from",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String evidenceUploadOptionsLabel;
  @CCD(
          label = "## Order \n ### The Court orders that",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRCourtOfficerOrderCruAccess.class}
  )
  private String courtOfficerOrderedLabel;
  @CCD(
          label = "## Reasons \n ### Do you want to give reasons for the order?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRCourtOfficerOrderCruAccess.class}
  )
  private String courtOfficerGiveReasonsLabel;
  @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRCourtOfficerOrderCruAccess.class})
  private AssistedOrderReasons courtOfficerGiveReasonsComplex;
  @CCD(
          label = "* The time for responding to the claim has expired. \n* The Defendant has not responded to the claim. \n* There is no outstanding application by the Defendant to strike out the claim for summary judgment. \n* The Defendant has not satisfied the whole claim, including costs. \n* The Defendant has not filed an admission together with request for time to pay.\n\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
  )
  private String CPRcertify;
  @CCD(
          label = "\n* The time for responding to the claim has expired. \n* The Defendant has not responded to the claim. \n* There is no outstanding application by the Defendant to strike out the claim for summary judgment. \n* The Defendant has not satisfied the whole claim, including costs. \n* The Defendant has not filed an admission together with request for time to pay.\n\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
  )
  private String CPRcertifys;
  @CCD(
          label = "Select if the statements all apply to the defendant",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilSolicitorCruAccess.class}
  )
  private String CPRAcceptanceLabel;
  @CCD(
          label = " ",
          searchable = false,
          access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
  )
  private CPRCertifyAcceptanceDJ CPRAcceptance;
  @CCD(
          label = "Select if the statements all apply to the defendants",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilSolicitorCruAccess.class}
  )
  private String CPRAcceptance2DefLabel;
  @CCD(
          label = " ",
          searchable = false,
          access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
  )
  private CPRCertifyAcceptanceDJ CPRAcceptance2Def;
  @CCD(
          label = "## How would you like the court to decide the amount of damages?\n While your preference will be taken into account, the court will make the final decision",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilSolicitorCruAccess.class}
  )
  private String hearingLabel;
  @CCD(
          label = "This is a hearing that lasts a maximum of 30 minutes, where information is presented only on paper, with no spoken evidence.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
  )
  private String hearingDisposalText;
  @CCD(
          label = "This is a hearing which lasts longer than 30 minutes, where the court will receive evidence on paper and listen to any spoken evidence that you wish it to consider when deciding the amount of damages.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
  )
  private String hearingTrialText;
  @CCD(
          label = "## What directions would you like the court to give?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilSolicitorCrudAccess.class}
  )
  private String hearingDirectionsLabel;
  @CCD(
          label = "<div class=\"govuk-inset-text\">The system only allows default judgment when a claim for other remedy has been abandoned. If you are not abandoning the request for other remedy, you must make an application for judgment.</div>",
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilAdminRCaseworkerCivilSolicitorCruAccess.class}
  )
  private String isOtherRemedyAbandonedText;
  @CCD(
          label = "## Do you wish to abandon your request for an other remedy?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilAdminRCaseworkerCivilSolicitorCruAccess.class}
  )
  private String isOtherRemedyAbandonedBoldLabel;
  @CCD(
          label = "* The time for responding to the claim has expired. \n* The Defendant has not responded to the claim. \n* There is no outstanding application by the Defendant to strike out the claim for summary judgment. \n* The Defendant has not satisfied the whole claim, including costs. \n* The Defendant has not filed an admission together with request for time to pay.\n\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
  )
  private String CPRCertifySpec;
  @CCD(
          label = "* The time for responding to the claim has expired. \n* The Defendant has not responded to the claim. \n* There is no outstanding application by the Defendant to strike out the claim for summary judgment. \n* The Defendant has not satisfied the whole claim, including costs. \n* The Defendant has not filed an admission together with request for time to pay.\n\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
  )
  private String CPRCertifySpecTwoDefendant;
  @CCD(
          label = "## ${currentDefendant}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilSolicitorCruAccess.class}
  )
  private String partialPaymentLabel;
  @CCD(
          label = "<div class=\"govuk-inset-text\">The system only allows interest to be calculated on the full claim amount. If you have received any part payment and you are requesting a CCJ, please use form <a target=\"_blank\" href=\"https://www.gov.uk/government/publications/form-n225-request-for-judgment-and-reply-to-admission-specified-amount\">N225</a>. If you have received any part payment and you are requesting a Judgment on a Part Admission response, please use form <a target=\"_blank\" href=\"https://assets.publishing.service.gov.uk/media/63f49b03e90e077bb3d7af45/n225a-eng-04-13_save.pdf\">N225a</a>. You can calculate the correct interest, and send to <a target=\"_blank\" href=\"mailto:contactocmc@justice.gov.uk\">contactocmc@justice.gov.uk</a></div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilSolicitorCruAccess.class}
  )
  private String partialPaymentInsetHelpText;
  @CCD(
          label = "## Would you like to claim for fixed costs?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
  )
  private String paymentConfirmationTitleSpec;
  @CCD(
          label = "${repaymentSummaryObject}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilSolicitorCuAccess.class}
  )
  private String repaymentSummaryLabel;
  @CCD(
          label = "## How do you want ${currentDefendantName} to pay?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilSolicitorCruAccess.class}
  )
  private String paymentTypeLabel;
  @CCD(
          label = "## When do you want ${currentDefendantName} to pay?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilSolicitorCruAccess.class}
  )
  private String paymentSetDateLabel;
  @CCD(
          label = "## Suggest instalments for ${currentDefendantName} \n Total claim amount is £${repaymentDue}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilSolicitorCruAccess.class}
  )
  private String repaymentDetailsLabel;
  @CCD(
          label = "## Write a Note \n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String caseNoteTypeNoteLabel;
  @CCD(
          label = "${caseNameHmctsInternal}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class, CourtOfficerOrderCruAccess.class}
  )
  private String caseParticipants;
  @CCD(
          label = "**** \n ### What type of order do you wish to make?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String finalOrderSelectionLabel;
  @CCD(
          label = "${caseNameHmctsInternal}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String caseParticipantsAssistedOrder;
  @CCD(
          label = "${caseNameHmctsInternal}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String caseParticipantsFreeForm;
  @CCD(
          label = "**** \n ## Order Made",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String finalOrderMadeLabel;
  @CCD(
          label = "#### Is this order made following a hearing?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String finalOrderMadeSelectionLabel;
  @CCD(
          label = "**** \n ## Judge considered the papers",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String finalOrderJudgePapersLabel;
  @CCD(
          label = "**** \n ## Judge heard from",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String finalOrderJudgeHeardLabel;
  @CCD(
          label = "**** \n ## Recitals",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String finalOrderRecitalsLabel;
  @CCD(
          label = "**** \n ## Order",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String finalOrderOrdered;
  @CCD(
          label = "### The court orders that:",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String finalOrderOrderedThat;
  @CCD(
          label = "**** \n ## Costs",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String finalOrderCost;
  @CCD(
          label = "### Select as appropriate",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String finalOrderCostSecondaryLabel;
  @CCD(
          label = "### Does the paying party have public funding costs protection?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String publicFundingCostsProtectionLabel;
  @CCD(
          label = "The paying party has the benefit of cost protection under section 26 of the Legal Aid, Sentencing and Punishment of Offenders Act 2012. The amount of costs that the paying party shall pay shall be determined on an application by the receiving party under rule 16 of the Civil legal Aid (Costs) Regulations 2013. Any objection by the paying party to the amount of costs claimed shall be dealt with on that occasion",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String publicFundingCostsProtectionDropdown;
  @CCD(
          label = "**** \n ## Further hearing (part heard or adjourned)",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String finalOrderFurtherHearingLabel;
  @CCD(
          label = "**** \n ## Appeal",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String finalOrderAppealLabel;
  @CCD(
          label = "**** \n ## Order made on court's own initiative/without notice",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String orderMadeOnDetailsPrimaryLabel;
  @CCD(
          label = "### Select as appropriate",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String orderMadeOnDetailsSecondaryLabel;
  @CCD(
          label = "**** \n ## Reasons",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String finalOrderReasonsLabel;
  @CCD(
          label = "### Do you want to give reasons for the order? (including any reasons for refusing/granting permission to appeal)",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String finalOrderGiveReasonsLabel;
  @CCD(
          label = "**** \n ## Recitals",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String freeFormRecitalAndOrderLabel;
  @CCD(
          label = "### The court records that:",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String freeFormRecordedTextAreaLabel;
  @CCD(
          label = "**** \n ## Order \n ### The court orders that:",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String freeFormOrderedLabel;
  @CCD(
          label = "**** \n ## Order made on court's own initiative/without notice \n ### Select as appropriate",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String orderOnCourtsListLabel;
  @CCD(
          label = "### Hearing notes (Optional)",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String freeFormHearingNotesLabel;
  @CCD(
          label = "## Penal notice (optional)",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {JudgeProfileCruAccess.class}
  )
  private String assistedOrderPenalNoticeTitle;
  @CCD(
          label = "What type of application(s) do you want to make?",
          hint = "  ",
          searchable = false,
          access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruPlus3RolesKwiaxwAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String generalApplicationType;
  @CCD(
          label = "## Statement of truth\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffRAccess.class}
  )
  private String generalAppStatementOfTruthLabel;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECrudAccess.class, CaseworkerCivilStaffRAccess.class}
  )
  private java.util.Set<StatementOfTruthConsentGAspec> generalAppStatementOfTruthConsent;
  @CCD(
          label = " ",
          searchable = false,
          access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private GAIsUrgentGAspec isApplicationUrgent;
  @CCD(
          label = "## How to upload your evidence \n If a document has multiple pages, upload them as one file rather than multiple files.\n\n Before you upload the document, give it a name that tells the court what it is, for example \"Witness statement by Jane Smith dated DD/MM/YYYY\" \n\n Each document must be less than 100MB. You can upload the following file types: Word, Excel, PowerPoint, PDF, RTF, TXT, CSV, JPG, JPEG, PNG, BMP, TIF, TIFF.\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrudCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffRAccess.class}
  )
  private String generalAppUploadEvidenceLabel;
  @CCD(
          label = "Applicant Addln Solicitor Details",
          searchable = false,
          access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruPlus3RolesKwiaxwAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private GASolicitorDetailsGAspec generalAppApplicantAddlSolicitors;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerCivilStaffRCaseworkerCivilSystemupdateCrudAccess.class, CaseworkerCaaCruAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.CaseLink caseLink;
  @CCD(
          label = " ",
          searchable = false,
          access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruPlus3RolesKwiaxwAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String caseState;
  @CCD(label = "Status", searchable = false, access = {CaseworkerCivilSystemFieldReaderRAccess.class})
  private GAStatusGAspec generalAppStatusGAspec;
  @CCD(
          label = "General Application Submitted on",
          searchable = false,
          access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CITIZENDEFENDANTPROFILECruPlus3RolesKwiaxwAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private java.time.LocalDateTime generalAppSubmittedDateGAspec;
  @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRAccess.class})
  private GeneralApplicationGAspec generalAppDateDeadline;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerCivilStaffRAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private GAUserDetailsGAspec civilServiceUserRoles;
  @CCD(
          label = "Upload your completed N245 form",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCrudAccess.class, CaseworkerCaaCruAccess.class, CaseworkerCivilSolicitorCruAccess.class}
  )
  private GAN245FormUpload gaUploadN245FormUploadLabel;
  @JsonProperty("SearchCriteria")
  @CCD(
          label = "Search Criteria",
          searchable = false,
          access = {GSProfileRAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.SearchCriteria searchCriteria;
  @CCD(
          label = "Application details",
          searchable = false,
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "HearingAppFor",
          access = {CaseworkerCivilSystemFieldReaderRHearingScheduleAccessCruAccess.class}
  )
  private HearingAppFor applicationDetails;
  @CCD(
          label = "Type of application",
          searchable = false,
          access = {CaseworkerCivilSystemFieldReaderRHearingScheduleAccessCruAccess.class}
  )
  private String typeOfApplication;
  @CCD(
          label = "**Location**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRHearingScheduleAccessCruAccess.class}
  )
  private String hearingLocationLabel;
  @CCD(
          label = "**Channel**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRHearingScheduleAccessCruAccess.class}
  )
  private String channelLabel;
  @CCD(
          label = "**Date**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRHearingScheduleAccessCruAccess.class}
  )
  private String hearingDateLabel;
  @CCD(
          label = "**Time**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRHearingScheduleAccessCruAccess.class}
  )
  private String hearingTimeLabel;
  @CCD(
          label = "## Issue civil court proceedings\nTo use this service you must be:\n* issuing a claim for a specified (fixed) amount of money\n* issuing a claim to the county court.  Claims cannot be issued to the High Court using this service\n* have an address in England or Wales and an email address\n* able to pay the full issue fee using Payment by Account (PBA) or card payment\n* serving a claim form in English\n\nThe claimant must:\n\n* have an address in England or Wales\n* not have a civil proceedings order, civil restraint order, or all proceedings order against them\n\nThe claim you’re issuing must not:\n* Contain interest with a breakdown for different periods of time or items\n* ordinarily follow the Part 8 procedure\n* be brought under the Consumer Credit Act 1974\n* be against the Crown",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECrCaseworkerCivilSystemupdateRAccess.class}
  )
  private String specEligibilityQuestions;
  @CCD(
          label = "<details><summary>More information about asking to use Welsh</summary><div class=\"panel panel-border-narrow\"><p>You can also use Welsh at mediation. This will not delay the mediation appointment.</p><p>The spoken language chosen here includes the language that you or the defendant will speak at mediation.</p></div></details><br /> ",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String defendantDQLanguageMediationHint;
  @CCD(
          label = "<details><summary>More information about asking to use Welsh</summary><div class=\"panel panel-border-narrow\"><p>You can also use Welsh at mediation. This will not delay the mediation appointment.</p><p>The spoken language chosen here includes the language that you or the claimant will speak at mediation.</p></div></details><br />",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {APPSOLSPECPROFILECruCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String claimantDQLanguageMediationHint;
  @CCD(
          label = "This includes support for you or the defendant at mediation.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String defendantDQSupportMediationHint;
  @CCD(
          label = "This includes support for you or the claimant at mediation.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {APPSOLSPECPROFILECruCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String claimantDQSupportMediationHint;
  @CCD(
          label = "${respondent1.partyName} admits they owe the claimant £${respondToAdmittedClaimOwingAmountPounds} including any interest claimed but exclusive of the claim fee and any fixed costs \nclaimed which are payable in addition. \n\nThey have offered to pay £${respondToAdmittedClaimOwingAmountPounds} immediately, plus claim fee and any fixed costs claimed. \n\n If the claimant accepts, they must contact the claimant and pay the full amount within the 5 days. Any cheques or transfers must be clear in the claimant's account.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilSystemupdateRAccess.class}
  )
  private String respondent1ClaimPartAdmitImmediatelyLabelWithInterest;
  @CCD(
          label = "${respondent1.partyName} admits they owe the claimant £${respondToAdmittedClaimOwingAmountPounds} including any interest claimed but exclusive of the claim fee and any fixed costs \nclaimed which are payable in addition. \n\nThey have offered to pay by ${respondent1PaymentDateToStringSpec}.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilSystemupdateRAccess.class}
  )
  private String respondent1ClaimPartAdmitBySetDateLabelWithInterest;
  @CCD(
          label = "${respondent1.partyName} admits they owe the claimant £${respondToAdmittedClaimOwingAmountPounds} including any interest claimed but exclusive of the claim fee and any fixed costs claimed which are payable in addition. \n\nIf judgment is entered on an admission the claim fee and any fixed costs claimed will be included in the total judgment sum. \n\nThey have offered to pay in instalments. \n\n Details of the repayment plan can be found in the Defendant Response form.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilSystemupdateRAccess.class}
  )
  private String respondent1ClaimPartAdmitInstalmentLabelWithInterest;
  @CCD(
          label = "### Claim amount (including interest if it has been claimed)\n\n£${ccjJudgmentAmountClaimAmount}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class}
  )
  private String ccjJudgmentAmountClaimAmountWithInterestLabel;
  @CCD(
          label = "### Claim amount (including interest if it has been claimed)\n\n£${ccjJudgmentAmountClaimAmount}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CaseworkerCivilStaffRAccess.class}
  )
  private String ccjJudgmentAmountClaimAmountLabel;
  @CCD(
          label = "${respondent1ClaimResponseTypeForSpec}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess.class, CaseworkerCivilSystemupdateRAccess.class}
  )
  private String respondent1ClaimResponseTypeLabelForSpec;
  @CCD(
          label = "${respondent1.partyName} admits all of the claim and has offered to pay in instalments. \n\nDetails of the repayment plan can be found in the Defendant response document",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess.class, CaseworkerCivilSystemupdateRAccess.class}
  )
  private String respondent1ClaimAdmitAllRepaymentPlanLabel;
  @CCD(
          label = "${respondent1.partyName} admits all of the claim and said that they will pay by ${respondent1PaymentDateToStringSpec}.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess.class, CaseworkerCivilSystemupdateRAccess.class}
  )
  private String respondent1ClaimAdmitAllBySetDateLabel;
  @CCD(
          label = "${respondent1.partyName} admits they owe the claimant £${respondToAdmittedClaimOwingAmountPounds}. They don't believe they owe the full amount claimed. \n\nThey have offered to pay the £${respondToAdmittedClaimOwingAmountPounds} immediately. \n\n They must make sure the claimant has the money by ${respondent1PaymentDateToStringSpec}.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilSystemupdateRAccess.class}
  )
  private String respondent1ClaimPartAdmitImmediatelyLabel;
  @CCD(
          label = "${respondent1.partyName} admits they owe the claimant £${respondToAdmittedClaimOwingAmountPounds}. They don't believe they owe the full amount claimed. \n\nThey have offered to pay by ${respondent1PaymentDateToStringSpec}.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CaseworkerCivilSystemupdateRAccess.class}
  )
  private String respondent1ClaimPartAdmitBySetDateLabel;
  @CCD(
          label = "${respondent1.partyName} admits they owe the claimant £${respondToAdmittedClaimOwingAmountPounds}. They don't believe they owe the full amount claimed. \n\nThey have offered to pay in instalments. \n\n Details of the repayment plan can be found in the Defendant Response form.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CaseworkerCivilSystemupdateRAccess.class}
  )
  private String respondent1ClaimPartAdmitInstalmentLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">How do you want ${respondent1.partyName} to pay?</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class}
  )
  private String applicant1RepaymentOptionForDefendantSpecLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Suggest instalments for ${respondent1.partyName}</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class}
  )
  private String applicant1SuggestInstalmentsForDefendantSpecHeader;
  @CCD(
          label = "The claim amount is £${totalClaimAmount}. This includes all court fees and interest.\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class}
  )
  private String applicant1SuggestInstalmentsForDefendantSpecLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Has ${respondent1.partyName} paid some of the amount owed?</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CaseworkerCivilSystemupdateRAccess.class}
  )
  private String ccjPaymentPaidSomeLabel;
  @CCD(
          label = "<div class=\"govuk-inset-text\">The system only allows interest to be calculated on the full claim amount. If you have received any part payment and you are requesting a CCJ, please use form <a target=\"_blank\" href=\"https://www.gov.uk/government/publications/form-n225-request-for-judgment-and-reply-to-admission-specified-amount\">N225</a>. If you have received any part payment and you are requesting a Judgment on a Part Admission response, please use form <a target=\"_blank\" href=\"https://assets.publishing.service.gov.uk/media/63f49b03e90e077bb3d7af45/n225a-eng-04-13_save.pdf\">N225a</a>. You can calculate the correct interest, and send to <a target=\"_blank\" href=\"mailto:contactocmc@justice.gov.uk\">contactocmc@justice.gov.uk</a></div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CaseworkerCivilSystemupdateRAccess.class}
  )
  private String ccjPaymentPaidSomeLabelInsetHelpText;
  @CCD(
          label = "${respondent1.partyName} states they paid the claimant £${partAdmitPaidValuePounds} on ${respondent1PaymentDateToStringSpec}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CaseworkerCivilSystemupdateRAccess.class}
  )
  private String respondent1PartAdmitPaidImmediatelyLabelSpec;
  @CCD(
          label = "Why does the claimant reject their response?",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilSystemupdateRAccess.class}
  )
  private String applicant1PartAdmitRejectReasonSpec;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Judgment Amount</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CaseworkerCivilStaffRAccess.class}
  )
  private String ccjJudgmentAmountSummaryLabel;
  @CCD(
          label = "${ccjJudgmentStatement}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilStaffRAccess.class}
  )
  private String ccjJudgmentAmountSummaryResponse;
  @CCD(
          label = "### Interest to date\n\n£${ccjJudgmentAmountInterestToDate}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CaseworkerCivilStaffRAccess.class}
  )
  private String ccjJudgmentAmountInterestToDateLabel;
  @CCD(
          label = "### Claim fee amount\n\n£${ccjJudgmentAmountClaimFee}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CaseworkerCivilStaffRAccess.class}
  )
  private String ccjJudgmentAmountClaimFeeLabel;
  @CCD(
          label = "### Subtotal\n\n£${ccjJudgmentSummarySubtotalAmount}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CaseworkerCivilStaffRAccess.class}
  )
  private String ccjJudgmentAmountSubtotalLabel;
  @CCD(
          label = " ",
          searchable = false,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilStaffRAccess.class}
  )
  private String ccjJudgmentAmountSubtotal;
  @CCD(
          label = "### Amount already paid\n\n£${ccjPaymentPaidSomeAmountInPounds}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CaseworkerCivilStaffRAccess.class}
  )
  private String ccjPaymentPaidSomeAmountInPoundsLabel;
  @CCD(
          label = "### Total still owed\n\n£${ccjJudgmentTotalStillOwed}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CaseworkerCivilStaffRAccess.class}
  )
  private String ccjJudgmentTotalStillOwedLabel;
  @CCD(
          label = "Total Claim amount is £${totalClaimAmount}. This includes all court fees and interest.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent1FullAdmitAmountLabel;
  @CCD(
          label = "### ${applicant1.partyName} Vs ${respondent1.partyName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String caseTitle;
  @CCD(
          label = "### Defendant Name",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String defendantNameLabel;
  @CCD(
          label = "${respondent1.partyName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String defendantName;
  @CCD(
          label = "## Upload Mediation Agreement",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String mediationAgreementTitle;
  @CCD(
          label = "### Staff uploaded documents",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String staffUploadedDocumentsLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Would you like to claim for fixed costs?</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CaseworkerCivilStaffRAccess.class}
  )
  private String ccjJudgmentFixedCostOptionLabel;
  @CCD(
          label = "### Fixed cost amount\n\n£${ccjJudgmentFixedCostAmount}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CaseworkerCivilStaffRAccess.class}
  )
  private String ccjJudgmentFixedCostAmountLabel;
  @CCD(
          label = "<details><summary>More information about fixed commencement costs</summary><div class=\"panel panel-border-narrow\"><p>For information as to how fixed commencement costs are calculated and the amounts allowed please see <a target=\"_blank\" href=\"https://www.justice.gov.uk/courts/procedure-rules/civil/rules/part45-fixed-costs/practice-direction-45-fixed-costs#2\">Civil Procedure Rules Practice Direction 45 Table 2</a> and <a target=\"_blank\" href=\"https://www.justice.gov.uk/courts/procedure-rules/civil/rules/practice-direction-51r-online-court-pilot\">Practice Direction 51R</a>.</p>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess.class, RESSOLTWOSPECPROFILERPlus3RolesWydxxsAccess.class}
  )
  private String fixedCostsHint;
  @CCD(
          label = "The claim fee and any fixed costs claimed are payable in addition to the claim amount admitted.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String responseClaimAdmitPartOfClaimSpecExtraCostsLabel;
  @CCD(
          label = "The claim fee and any fixed costs claimed are payable in addition to the claim amount admitted.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String responseClaimAdmitPartOfClaimSpecExtraCostsLabel2;
  @CCD(
          label = "The claim fee and any fixed costs claimed are payable in addition to the claim amount admitted.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String responseClaimAdmitPartOfClaimSpecExtraCostsLabelBothDefendants;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">When does ${respondent1.partyName} want to pay the £${totalClaimAmountPlusInterestString}?</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String responseClaimAdmitFullClaimWhenToPaySpecLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">When does ${respondent2.partyName} want to pay the £${totalClaimAmountPlusInterestString}?</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String responseClaimAdmitFullClaimWhenToPaySpecLabel2;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">When do ${respondent1.partyName} and ${respondent2.partyName} want to pay the £${totalClaimAmountPlusInterestString}?</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String responseClaimAdmitFullClaimWhenToPaySpecLabelBothDefendants;
  @CCD(
          label = "This amount includes interest if it has been claimed which may continue to accrue on the amount outstanding up to the date of judgment or earlier payment.\n\nThe above amount does not include the claim fee and any fixed costs claimed which are payable in addition and if judgment is entered on an admission will be included in the total judgment sum.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class}
  )
  private String respondent1ClaimAdmitFullInterestLabel;
  @CCD(
          label = "This amount includes interest if it has been claimed which may continue to accrue on the amount outstanding up to the date of judgment or earlier payment.\n\nThe above amount does not include the claim fee and any fixed costs claimed which are payable in addition and if judgment is entered on an admission will be included in the total judgment sum.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class}
  )
  private String respondent2ClaimAdmitFullInterestLabel;
  @CCD(
          label = "## The total amount, including any interest claimed to date, is £${totalClaimAmountPlusInterestAdmitPartString}.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String responseToClaimAdmitPartTotalAmountWithInterestLabel1;
  @CCD(
          label = "## The total amount, including any interest claimed to date, is £${totalClaimAmountPlusInterestAdmitPartString}.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String responseToClaimAdmitPartTotalAmountWithInterestLabel2;
  @CCD(
          label = "The total amount claimed is £${totalClaimAmountPlusInterestString}.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String respondent1RepaymentPlanAdmitFullLabel;
  @CCD(
          label = "This amount includes interest if it has been claimed which may continue to accrue on the amount outstanding up to the date of judgment or earlier payment.\n\nThe above amount does not include the claim fee and any fixed costs claimed which are payable in addition and if judgment is entered on an admission will be included in the total judgment sum.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String respondent1RepaymentPlanAdmitFullInterestLabel;
  @CCD(
          label = "The total amount claimed is £${totalClaimAmountPlusInterestString}.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String respondent2RepaymentPlanAdmitFullLabel;
  @CCD(
          label = "This amount includes interest if it has been claimed which may continue to accrue on the amount outstanding up to the date of judgment or earlier payment.\n\nThe above amount does not include the claim fee and any fixed costs claimed which are payable in addition and if judgment is entered on an admission will be included in the total judgment sum.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String respondent2RepaymentPlanAdmitFullInterestLabel;
  @CCD(
          label = "<p>The amount admitted is £${respondToAdmittedClaimOwingAmountPounds}</p> <p>The amount includes interest if it has been claimed.</br>The claim fee and any fixed costs claimed are not included in this figure but are payable in addition and if judgment is entered on an admission will be included in the total judgment sum.</p>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String respondent1RepaymentPlanLabelPartAdmit;
  @CCD(
          label = "<p>The amount admitted is £${respondToAdmittedClaimOwingAmountPounds}</p> <p>The amount includes interest if it has been claimed.</br>The claim fee and any fixed costs claimed are not included in this figure but are payable in addition and if judgment is entered on an admission will be included in the total judgment sum.</p>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String respondentGenericRepaymentPlanLabelPartAdmit;
  @CCD(
          label = "<p>The amount admitted is £${respondToAdmittedClaimOwingAmountPounds2}</p> <p>The amount includes interest if it has been claimed.</br>The claim fee and any fixed costs claimed are not included in this figure but are payable in addition and if judgment is entered on an admission will be included in the total judgment sum.</p>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String respondent2RepaymentPlanLabelPartAdmit;
  @CCD(
          label = "If the claimant accepts, you must contact the claimant and pay the full amount within 5 days. Any cheques or transfers must be clear in their account.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String partAdmissionWhenWillDefendantPayImmediatelyLRALabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Statement of truth</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilSolicitorCruAccess.class}
  )
  private String respondStatementOfTruthHeaderLabel;
  @CCD(
          label = "The defendant believes that the facts stated in the response are true.\n\nI am duly authorised by the defendant to sign this statement.\n\nThe defendant understands that proceedings for contempt of court may be brought against anyone who makes, or causes to be made, a false statement in a document verified by a statement of truth without an honest belief in its truth.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilSolicitorCruAccess.class}
  )
  private String respondStatementOfTruthLabel;
  @CCD(
          label = "In order to verify the response to claim by a statement of truth please click “submit” on the next screen. Doing so will be taken as verifying the response by the above statement of truth. Alternatively, you will have the opportunity, on the next screen, to change any of the answers before clicking the “submit” button.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilSolicitorCruAccess.class}
  )
  private String respondStatementOfTruthSubmitLabel;
  @CCD(
          label = "## Legal representatives: specified civil money claims service\n### Before you start\n### Please note: You can only use this service if you are a solicitor organisation\nDownload and complete the claim timeline template with all events in your timeline. You will upload the template in this service. \nPlease do not upload password protected documents as this will prevent the claim from being processed.\n\n<a target=\"_blank\" href=\"https://github.com/hmcts/cmc-citizen-frontend/raw/1ef9e83f68c6c5289f36c27de88780eeb7001cab/src/main/public/pdf/timeline-event-template.pdf\">claim timeline template.</a> \n\nYou can complete and upload as many templates as necessary to record all events in your timeline. \n\nYou can also manually enter your timeline in this service without using the template. \n\nIf you're claiming interest you will need your interest calculation and dates.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECrCaseworkerCivilSystemupdateRAccess.class}
  )
  private String specCheckList;
  @CCD(
          label = " ",
          searchable = false,
          access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemupdateCrudAccess.class}
  )
  private SolicitorOrganisationEmail specRespondentSolicitor1OrganisationEmail;
  @CCD(
          label = "## Email for defendant’s legal representative \n\nUse the email address of the legal representative's firm if you have it, otherwise use their personal email. \n\n <p class=\"govuk-inset-text\">You can still issue the claim without their email address but the claim will then continue offline.</p>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String specRespondentSolicitor1OrgLabel;
  @CCD(
          label = "## Email for second defendant's legal representative \n\nUse the email address of the legal representative's firm if you have it, otherwise use their personal email. \n\n <p class=\"govuk-inset-text\">You can still issue the claim without their email address but the claim will then continue offline.</p>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String specRespondentSolicitor2OrgLabel;
  @CCD(
          label = "## Describe your claim\nDo not give us a detailed timeline - we'll ask for that separately.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String specDetailsOfClaimLabel;
  @CCD(
          label = "## Upload supporting documents (optional)\n\n<div class=\"govuk-inset-text\">You can upload documents in support of the claim details above.</div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String specDetailsOfClaimUploadDocumentLabel;
  @CCD(
          label = " ",
          searchable = false,
          access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemupdateRAccess.class, CitizenProfileCruAccess.class}
  )
  private ClaimTimelineList specClaimTimelineList;
  @CCD(
          label = "## How do you want to add your claim timeline?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String specClaimTimelineListLabel;
  @CCD(
          label = "## Claim timeline\nIf you do not know the exact date, tell us the month and year.\n### Example timeline\n* 12 January 2020 - John Smith gave me a quote to replace the roof.\n* 14 January 2021 - We agreed and signed a contract for the work.\n* 21 March 2021 - I noticed a leak on the landing and told Mr Smith about this.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String specClaimTimeline;
  @CCD(
          label = "<details><summary>How interest to date is calculated</summary><div class=\"panel panel-border-narrow\"><p>For most types of debt the rate is usually 8%.</p><p>To calculate this, we use the steps below.</p><ol class=\"list list-number\"><li><p>Work out the yearly interest: take the amount you’re claiming and multiply it by 0.08 (which is 8%).</p></li><li><p>Work out the daily interest: divide your yearly interest from step 1 by 365 (the number of days in a year).</p></li><li><p>Work out the total amount of interest: multiply the daily interest from step 2 by the number of days the debt has been overdue.</p></li></ol><div><strong class=\"govuk-tag govuk-tag--grey\">EXAMPLE</strong><br /><ol class=\"panel list\"><li>The rate of annual interest is 8%.</li><li>Annual interest on, for example, £1,000 is £80 (1000 x 0.08)</li><li>Daily interest on £1,000 is 22p (80 / 365)</li><li>Total amount of interest on £1,000 for 2 days is 44p (80 x 2 / 365)</li></ol></div></div></details><br /> \n\n There may be additional fees as your case progresses. <a target=\"_blank\" href=\"https://www.gov.uk/government/publications/fees-in-the-civil-and-family-courts-main-fees-ex50\">There may be additional fees as your case progresses.</a> ",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String calculatedInterestPageLabel;
  @CCD(
          label = "## List your evidence\nTell us about any evidence you wish to provide. You do not need to send us any evidence now. If your case goes to a court hearing, and is not settled, you will need to provide evidence.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String speclistYourEvidenceLabel;
  @CCD(
          label = "## Claim Amount\n Your claim can be for single or multiple amounts. Do not include interest or the claim fee, you can add these on the next page.\n\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String specClaimAmountLabel;
  @CCD(
          label = "${claimAmountBreakupSummaryObject}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String claimAmountBreakupSummaryLabel;
  @CCD(
          label = "## Do you want to claim interest?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String claimInterestLabel;
  @CCD(
          label = "<details><summary><u>Help with interest rates</u></summary><div class=\"panel\">\n\n You can claim 8% interest on money owed to you. This is the statutory rate. If you know that a different rate applies you can use that. For example, if you have a contract with a specific rate. \n\n The court will decide if you're entitled to some, or all, of the interest claimed.</div></details><br />",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECrAccess.class}
  )
  private String claimInterestHelpTextLink;
  @CCD(
          label = "## How do you want to claim interest?\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilSystemupdateRAccess.class}
  )
  private String interestClaimOptionsLabel;
  @CCD(
          label = "## What annual rate of interest do you want to claim?\n You can claim 8% per year unless you know that a different rate applies\n\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String sameRateInterestSelectionLabel;
  @CCD(
          label = "<details><summary><u>Help with interest rates</u></summary><div class=\"panel\">\n\n You can claim 8% interest on money owed to you. This is the statutory rate. If you know that a different rate applies you can use that. For example, if you have a contract with a specific rate. \n\n The court will decide if you're entitled to some, or all, of the interest claimed.</div></details><br />",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECrAccess.class}
  )
  private String sameInterestHelpTextLink;
  @CCD(
          label = "## What is the total interest for your claim?\n Calculate interest for different periods of time, or items, and let us know the total. We’ll add it to your claim amount.\n\n ",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String breakDownInterestLabel;
  @CCD(
          label = "## When are you claiming interest from?\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String interestClaimFromLabel;
  @CCD(
          label = "## When are you claiming interest to?\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String interestClaimUntilLabel;
  @CCD(
          label = "## Date you want to claim interest from\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String interestFromSpecificDateLabel;
  @CCD(
          label = " ${calculatedInterest} ",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String calculatedInterestLabel;
  @CCD(
          label = "## Total amount of claim",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECrAccess.class}
  )
  private String calculatedInterestHeading;
  @CCD(
          label = "<details><summary><u>Pay with another PBA</u></summary><div class=\"panel\">\n\n## One PBA listed\n\nTo pay with a different PBA, email: MyHMCTSsupport@justice.gov.uk \n\nAsk for your PBA to be registered with your MyHMCTS account and include your organisation name and PBA number. \n\nIt can then take up to 3 days for your account to be updated. You’ll need to start your claim again to pay the fee.\n## Two PBAs listed\n\nYou won’t be able to add another PBA.\n\nYou can only submit this case online using one of the PBAs from the menu.\n\nTo remove a PBA, email: MyHMCTSsupport@justice.gov.uk. \n\nInclude the PBA number. It can then take up to 3 days for the PBA to be removed. We’ll send you confirmation.\n</div></details><br />",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilSolicitorCruAccess.class}
  )
  private String specPbaAccountsInformationLabel;
  @CCD(
          label = "## Upload Claim timeline template",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String specClaimTimelineUpload;
  @CCD(
          label = "## Does the defendant have a legal representative?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCruCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String specRespondent1RepresentedLabel;
  @CCD(
          label = "Postal correspondence will be sent to the address registered with MyHMCTS. You can change this address if, for example, you work in a different office from the address registered with MyHMCTS.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCruCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String specCorrespondenceAddressSubHeadingLabel;
  @CCD(
          label = "Postal correspondence to the Defendant’s legal representative will be sent to the address that is currently registered with MyHMCTS. You can, if you wish, change the address to which postal correspondence is sent.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCruCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String specRespondentCorrespondenceAddressSubHeadingLabel;
  @CCD(
          label = "Postal correspondence to the Defendant’s legal representative will be sent to the address that is currently registered with MyHMCTS. You can, if you wish, change the address to which postal correspondence is sent.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCruCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String specRespondent2CorrespondenceAddressSubHeadingLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-m\">Confirm name and address</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String specAoSRespondentDetailLabel;
  @CCD(
          label = "The claimant has provided the following name and address for the defendant.\n\nIf this is not correct contact the claimant's legal representative.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String specAoSRespondentDetails;
  @CCD(
          label = "<p class=\"govuk-inset-text\">You only need to complete this template if you're providing a timeline of events.</p> \n <p>Download and complete the claim timeline template with all events in your timeline. You will upload the template in this service. </p>\n\n <a target=\"_blank\" href=\"https://github.com/hmcts/cmc-citizen-frontend/raw/1ef9e83f68c6c5289f36c27de88780eeb7001cab/src/main/public/pdf/timeline-event-template.pdf\">claim timeline template.</a> \n\nYou can upload as many templates as necessary to record all events in your timeline. \n\nYou can also manually enter your timeline in this service without using the template.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class, CITIZENCLAIMANTPROFILECrCaseworkerCivilSystemupdateRAccess.class}
  )
  private String specResponseCheckList;
  @CCD(
          label = "## Defendant: ${respondent1.partyName}\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent1ClaimResponseTypeForSpecLabel;
  @CCD(
          label = "## Defendant: ${respondent1.partyName} and ${respondent2.partyName}\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
  )
  private String respondentMPClaimResponseTypeForSpecLabel;
  @CCD(
          label = "## Claimant: ${applicant1.partyName}\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilSystemupdateRAccess.class}
  )
  private String claimant1ClaimResponseTypeForSpecLabel;
  @CCD(
          label = "## Claimant: ${applicant2.partyName}\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilSystemupdateRAccess.class}
  )
  private String claimant2ClaimResponseTypeForSpecLabel;
  @CCD(
          label = "## Why does ${respondent1.partyName} not owe money to the claimant? \n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
  )
  private String specDefenceRouteAmountClaimedLabel;
  @CCD(
          label = "## Why do ${respondent1.partyName} and ${respondent2.partyName} not owe money to the claimant? \n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
  )
  private String specMPDefenceRouteAmountClaimedLabel;
  @CCD(
          label = "## Why does ${respondent1.partyName} not owe money to the claimants? \n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String specDefenceRouteAmountClaimedLabel2v1;
  @CCD(
          label = "## Why does ${respondent2.partyName} not owe money to the claimant? \n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
  )
  private String specDefenceRouteAmountClaimedRespondent2Label;
  @CCD(
          label = "## Has ${respondent1.partyName} paid the claimant the admitted amount? \n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
  )
  private String specDefenceRouteAdmittedAmountClaimedLabel;
  @CCD(
          label = "## Have ${respondent1.partyName} and ${respondent2.partyName} paid the claimant the admitted amount? \n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
  )
  private String specDefenceRouteAdmittedAmountClaimedMPLabel;
  @CCD(
          label = "## Has ${respondent1.partyName} paid the claimants the admitted amount? \n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String specDefenceRouteAdmittedAmountClaimedLabel2V1;
  @CCD(
          label = "<h2 class=\"govuk-heading-m\">Why does ${respondent1.partyName} dispute the claim?</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
  )
  private String specDefenceRouteUploadDocumentLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-m\">Why does ${respondent2.partyName} dispute the claim?</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
  )
  private String specDefenceRouteUploadDocumentLabelRespondent2;
  @CCD(
          label = "<h2 class=\"govuk-heading-m\">Why do ${respondent1.partyName} and ${respondent2.partyName} dispute the claim?</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
  )
  private String specDefenceRouteUploadDocumentMPLabel;
  @CCD(
          label = "<div><p>Do not give us a detailed timeline - we'll ask for that separately.</p></div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
  )
  private String specDefenceRouteUploadDocumentLabel4;
  @CCD(
          label = "<h2 class=\"govuk-heading-m\">Upload supporting evidence (optional)</h2></h1> \n ",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
  )
  private String specDefenceRouteUploadDocumentHelpLabel;
  @CCD(
          label = "<p class=\"govuk-inset-text\">If you've referred to evidence in the above box, please upload them here.</p>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
  )
  private String specDefenceRouteUploadDocumentHelpLabel2;
  @CCD(
          label = "## ${respondent1.partyName} has paid less than the total claim amount \n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
  )
  private String specDefenceRouteUploadDocumentLabel2;
  @CCD(
          label = "## ${respondent2.partyName} has paid less than the total claim amount \n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
  )
  private String specDefenceRouteUploadDocumentLabel2Respondent2;
  @CCD(
          label = "## How do you want to add the claim timeline? ",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String specClaimResponseTimelineLabel;
  @CCD(
          label = "Upload files",
          hint = "We only accept documents in pdf format.",
          regex = ".pdf",
          searchable = false,
          access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemupdateRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document specResponsTimelineDocumentFiles2;
  @CCD(
          label = "List the events in order. Provide dates if you know them.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffRAccess.class}
  )
  private String specDefenceRouteManualTimelineLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Has ${respondent1.partyName} agreed to free mediation?</h2>\n ",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruAccess.class}
  )
  private String responseClaimMediationSpecLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Have ${respondent1.partyName} and ${respondent2.partyName} agreed to free mediation?</h2>\n ",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
  )
  private String responseMPClaimMediationSpecLabel;
  @CCD(
          label = "<p>Find out more about <a href=\"https://www.gov.uk/guidance/small-claims-mediation-service\" rel=\"noreferrer noopener\" target=\"_blank\">free mediation (opens in a new tab)</a>.</p>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
  )
  private String responseClaimMediationSpecLabel2;
  @CCD(
          label = "## Use of experts in court",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
  )
  private String responseClaimExpertSpecLabel;
  @CCD(
          label = "## Court Location",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class}
  )
  private String responseClaimCourtLocationLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Hearing availability</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String respondent1DQHearingFastClaimLabel;
  @JsonProperty("SmallClaimHearingInterpreter2Required")
  @CCD(
          label = " ",
          searchable = false,
          access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo smallClaimHearingInterpreter2Required;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Application</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String respondent1DQFutureApplicationsLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">When does ${respondent1.partyName} want to pay the £${respondToAdmittedClaimOwingAmountPounds}?</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String responseClaimAdmitPartOfClaimWhenToPaySpecLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">When does ${respondent2.partyName} want to pay the £${respondToAdmittedClaimOwingAmountPounds2}?</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String responseClaimAdmitPartOfClaimWhenToPaySpecLabel2;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">When do ${respondent1.partyName} and ${respondent2.partyName} want to pay the £${respondToAdmittedClaimOwingAmountPounds}?</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String responseClaimAdmitPartOfClaimWhenToPaySpecLabelBothDefendants;
  @CCD(
          label = "You must contact the claimant to arrange payment of the full amount within 5 days. Any cheques or transfers must be clear in their account.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String partAdmissionWhenWillDefendantPayImmediatelyLabel;
  @CCD(
          label = "The claimant can request a County Court Judgment (CCJ) against your client if the amount is not paid immediately.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String partAdmissionWhenWillDefendantPaySuggestRepaymentPlanLabel;
  @CCD(
          label = "You must contact the claimant to arrange payment of the full amount within 5 days. Any cheques or transfers must be clear in their account.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String partAdmissionWhenWillDefendantPayImmediately2Label;
  @CCD(
          label = "The claimant can request a County Court Judgment (CCJ) against your client if the amount is not paid immediately.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String partAdmissionWhenWillDefendantPaySuggestRepaymentPlan2Label;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Is ${respondent1.partyName} in employment?</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String responseClaimAdmitPartOfClaimEmploymentDeclarationLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Is ${respondent2.partyName} in employment?</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String responseClaimAdmitPartOfClaimEmploymentDeclarationLabel2;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Is ${respondent2.partyName} in employment?</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String responseClaimAdmitPartOfClaimEmploymentDeclarationLabelRespondent2;
  @CCD(
          label = " ",
          searchable = false,
          access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo defenceAdmitPartEmploymentTypeRequiredRespondent2;
  @CCD(
          label = "Employed or self-employed?",
          hint = "Select both if your client is employed and self-employed",
          searchable = false,
          access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCrudAccess.class}
  )
  private java.util.Set<EmploymentTypeCheckboxFixedListLRspec> respondToClaimAdmitPartEmploymentTypeLRspecRespondent2;
  @CCD(
          label = " ",
          searchable = false,
          access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCrudAccess.class}
  )
  private UnemployedComplexTypeLRspec respondToClaimAdmitPartUnemployedLRspecRespondent2;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Is ${respondent1.partyName} paying money as a result of any court order?</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String respondent1CourtOrderPaymentLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Is ${respondent2.partyName} paying money as a result of any court order?</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String respondent2CourtOrderPaymentLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">List ${respondent1Copy.partyName}'s bank and savings accounts</h2> \n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
  )
  private String minusBeforeOverdrawnAmount;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">List ${respondent2Copy.partyName}'s bank and savings accounts</h2> \n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String minusBeforeOverdrawnAmountRes2;
  @CCD(
          label = "<div><p>Put a minus (-) in front of any overdrawn amounts. For example &pound;-804.45.</p></div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
  )
  private String minusBeforeOverdrawnAmount2;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">List ${respondent2Copy.partyName}'s bank and savings accounts</h2> \n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
  )
  private String minusBeforeOverdrawnAmountRespondent2;
  @CCD(
          label = "<div><p>Put a minus (-) in front of any overdrawn amounts. For example &pound;-804.45.</p></div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
  )
  private String minusBeforeOverdrawnAmount2Respondent2;
  @CCD(
          label = "<h1 class=\"govuk-heading-l\">Details of ${respondent1.partyName}'s finances for the claimant</h1> \n <div><p>Provide details of your client's finances and we'll send them to claimant to review.</p><p>Your proposal can be rejected if the claimant believes your client can pay sooner.</p><p>If this happens, the court will make a new plan using the financial details provided.</p></div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECrudAccess.class}
  )
  private String specFinancialDetailsPurpose;
  @CCD(
          label = "<h1 class=\"govuk-heading-l\">Details of ${respondent2.partyName}'s finances for the claimant</h1> \n <div><p>Provide details of your client's finances and we'll send them to claimant to review.</p><p>Your proposal can be rejected if the claimant believes your client can pay sooner.</p><p>If this happens, the court will make a new plan using the financial details provided.</p></div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String specFinancialDetailsPurpose2;
  @CCD(
          label = "<h1 class=\"govuk-heading-l\">Details of ${respondent2.partyName}'s finances for the claimant</h1> \n <div><p>Provide details of your client's finances and we'll send them to claimant to review.</p><p>Your proposal can be rejected if the claimant believes your client can pay sooner.</p><p>If this happens, the court will make a new plan using the financial details provided.</p></div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECrudAccess.class}
  )
  private String specFinancialDetailsPurposeRespondent2;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">${respondent1Copy.partyName}'s debts</h2> \n</div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffRAccess.class}
  )
  private String specDebtPageLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">${respondent2Copy.partyName}'s debts</h2> \n</div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffRAccess.class}
  )
  private String specDebtPageLabel2;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">${respondent1.partyName}'s employment details</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String responseClaimAdmitPartOfClaimEmploymentSpecLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">${respondent2.partyName}'s employment details</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String responseClaimAdmitPartOfClaimEmploymentSpecLabel2;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">${respondent1Copy.partyName}'s employment details</h2> \n</div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String specSelfEmployedPageLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">${respondent2Copy.partyName}'s employment details</h2> \n</div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCrCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String specSelfEmployedPageLabel2;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Where does ${respondent1.partyName} live?</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
  )
  private String homeOptionsTitleLRspec;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Where does ${respondent2.partyName} live?</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String homeOptionsTitleLRspec2;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Where does ${respondent2.partyName} live?</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
  )
  private String homeOptionsTitleLRspecRespondent2;
  @CCD(
          label = "## ${respondent1.partyName}'s income and expenses\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String respondent1DQIncomeAndExpenseLabel;
  @CCD(
          label = "## ${respondent2.partyName}'s income and expenses\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String respondent2DQIncomeAndExpenseLabel;
  @CCD(
          label = "## ${respondent1.partyName}'s income and expenses\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String respondent1DQIncomeAndExpenseLabelFullAdmission;
  @CCD(
          label = "## ${respondent2.partyName}'s income and expenses\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String respondent2DQIncomeAndExpenseLabelFullAdmission;
  @CCD(
          label = "Add details of any regular income your client receives:",
          searchable = false,
          access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<RecurringIncome>> respondent2DQRecurringIncomeFA;
  @CCD(
          label = "Add details of any regular expenses your client has:",
          searchable = false,
          access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<RecurringExpense>> respondent2DQRecurringExpensesFA;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Tell us why ${respondent1.partyName} cannot pay immediately</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String responseToClaimAdmitPartWhyNotPayLRspecLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Tell us why ${respondent2.partyName} cannot pay immediately</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String responseToClaimAdmitPartWhyNotPayLRspecLabel2;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Repayment plan for ${respondent1.partyName} </h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String respondent1RepaymentPlanLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Repayment plan for ${respondent2.partyName} </h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String respondent2RepaymentPlanLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-m\">Witnesses</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String respondent1DQWitnessesLabel;
  @CCD(
          label = "## Does the defendant intend to file a single response to both claimants?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilSystemupdateRAccess.class}
  )
  private String defendantSingleResponseToBothClaimantsLabel;
  @CCD(
          label = "## Defendant: ${respondent2.partyName}\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderCaseworkerCivilSystemupdateRAccess.class, RESSOLTWOSPECPROFILECruAccess.class}
  )
  private String respondent2ClaimResponseTypeForSpecLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Does ${respondent1.partyName} receive any disability premium payments?</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String disabilityPremiumPaymentsLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Does ${respondent2.partyName} receive any disability premium payments?</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String disabilityPremiumPaymentsRespondent2Label;
  @CCD(
          label = " ",
          searchable = false,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo disabilityPremiumPaymentsRespondent2;
  @CCD(
          label = "Does ${respondent2.partyName} receive severe disability premium payments?",
          searchable = false,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo severeDisabilityPremiumPaymentsRespondent2;
  @CCD(
          label = "You have chosen to counterclaim. This means your defence cannot continue online. Select 'continue' for instructions on how to counterclaim using form N9B",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String responseCounterClaimLabel;
  @CCD(
          label = "You have chosen to counterclaim. This means your defence cannot continue online. Select 'continue' for instructions on how to counterclaim using form N9B",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String responseCounterClaimLabel2;
  @CCD(
          label = "You have chosen to counterclaim. This means your defence cannot continue online. Select 'continue' for instructions on how to counterclaim using form N9B",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {RESSOLONESPECPROFILERESSOLTWOSPECPROFILERAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String claimantCounterClaimLabel_2v1;
  @CCD(
          label = "Claimant 2 Disclosure of non-electronic documents",
          searchable = false,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private DisclosureOfNonElectronicDocumentsLRspec specApplicant2DQDisclosureOfNonElectronicDocuments;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Application</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERESSOLONESPECPROFILERAccess.class}
  )
  private String applicant1DQFutureApplicationsLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Respond to the defence</h2>\n ",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String applicant1DefenceResponseDocumentSpecLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">Claimant's requested court</h2>\n ",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess.class}
  )
  private String applicant1DQRequestedCourtSpecLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">${respondent1.partyName} partner and dependents</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String respondent1PartnerAndDependentLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-l\">${respondent2.partyName} partner and dependents</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String respondent2PartnerAndDependentLabel;
  @CCD(
          label = "<strong class=\"text-16\">This claim is in Breathing Space</strong>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String breathingSpaceLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-m\">Witnesses</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String respondent1SmallClaimWitnessesLabel;
  @CCD(
          label = "The claimant believes that the facts stated in the response are true.\n\nI am duly authorised by the claimant to sign this statement.\n\nThe claimant understands that proceedings for contempt of court may be brought against anyone who makes, or causes to be made, a false statement in a document verified by a statement of truth without an honest belief in its truth.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerCivilSolicitorCruAccess.class}
  )
  private String applicantStatementOfTruthLabel;
  @CCD(
          label = "<h2 class=\"govuk-heading-m\">Witnesses</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess.class}
  )
  private String applicant1SmallClaimWitnessesLabel;
  @CCD(
          label = "## The defendant said they'll pay you immediately.\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class}
  )
  private String respondentPayImmediatelyLabel;
  @CCD(
          label = "<p>Claim Number: ${legacyCaseReference}</p><p>They must make sure you have the money by:",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class, CaseworkerCivilSolicitorCruAccess.class}
  )
  private String respondentPayImmediatelyTextBeforeDate;
  @CCD(
          label = "**Party type:** ${respondent1Copy.partyTypeDisplayValue}\n\n**Name: ${respondent1Copy.partyName}** ${respondent1Copy.soleTraderTradingAs}\n\n**Email:** ${respondent1Copy.partyEmail}\n\n**Phone:** ${respondent1Copy.partyPhone}\n\n\n\n**Address:**<br />\r\n${respondent1Copy.primaryAddress.AddressLine1}<br />${respondent1Copy.primaryAddress.AddressLine2}<br />${respondent1Copy.primaryAddress.AddressLine3}<br />${respondent1Copy.primaryAddress.PostTown}<br />${respondent1Copy.primaryAddress.County}<br />${respondent1Copy.primaryAddress.PostCode}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
  )
  private String specAoSRespondentInformation;
  @CCD(
          label = "**Party type:** ${respondent1Copy.partyTypeDisplayValue}\n\n**Name: ${respondent1Copy.partyName}** ${respondent1Copy.soleTraderTradingAs}\n\n**Email:** ${respondent1Copy.partyEmail}\n\n\n\n**Address:**<br />\r\n${respondent1Copy.primaryAddress.AddressLine1}<br />${respondent1Copy.primaryAddress.AddressLine2}<br />${respondent1Copy.primaryAddress.AddressLine3}<br />${respondent1Copy.primaryAddress.PostTown}<br />${respondent1Copy.primaryAddress.County}<br />${respondent1Copy.primaryAddress.PostCode}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
  )
  private String specAoSCompanyOrganisationRespondentInformation;
  @CCD(
          label = "**Party type:** ${respondent2Copy.partyTypeDisplayValue}\n\n**Name: ${respondent2Copy.partyName}** ${respondent2Copy.soleTraderTradingAs}\n\n**Email:** ${respondent2Copy.partyEmail}\n\n**Phone:** ${respondent2Copy.partyPhone}\n\n\n\n**Address:**<br />\r\n${respondent2Copy.primaryAddress.AddressLine1}<br />${respondent2Copy.primaryAddress.AddressLine2}<br />${respondent2Copy.primaryAddress.AddressLine3}<br />${respondent2Copy.primaryAddress.PostTown}<br />${respondent2Copy.primaryAddress.County}<br />${respondent2Copy.primaryAddress.PostCode}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
  )
  private String specAoSRespondent2Information;
  @CCD(
          label = "**Party type:** ${respondent2Copy.partyTypeDisplayValue}\n\n**Name: ${respondent2Copy.partyName}** ${respondent2Copy.soleTraderTradingAs}\n\n**Email:** ${respondent2Copy.partyEmail}\n\n\n\n**Address:**<br />\r\n${respondent2Copy.primaryAddress.AddressLine1}<br />${respondent2Copy.primaryAddress.AddressLine2}<br />${respondent2Copy.primaryAddress.AddressLine3}<br />${respondent2Copy.primaryAddress.PostTown}<br />${respondent2Copy.primaryAddress.County}<br />${respondent2Copy.primaryAddress.PostCode}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
  )
  private String specAoSCompanyOrganisationRespondent2Information;
  @CCD(
          label = "## Is this an airline claim?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSolicitorCruCaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String isFlightDelayClaimLbl;
  @CCD(
          label = "Event Description",
          hint = "A few words describing the purpose of the event",
          searchable = false,
          access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, JudgeProfileRAccess.class, LegalAdviserCruAccess.class}
  )
  private String eventDescription;
  @CCD(
          label = "Additional Information ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {CaseworkerCivilSystemFieldReaderJudgeProfileRAccess.class, LegalAdviserCruAccess.class}
  )
  private String additionalInformation;
  @CCD(
          label = "Select continue to request a listing.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRHearingScheduleAccessCruAccess.class}
  )
  private String relistHearingNotice;
  @CCD(
          label = "## What type of hearing do you want to have listed?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRHearingScheduleAccessCruAccess.class}
  )
  private String requestHearingNoticeLabel;
  @CCD(
          label = "What type of hearing do you want to have listed?",
          searchable = false,
          access = {CaseworkerCivilSystemFieldReaderRHearingScheduleAccessCruAccess.class}
  )
  private String requestHearingOtherText;
  @CCD(
          label = "# ${applicantVRespondentText} \n What order would you like to make",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, JudgeProfileCruAccess.class}
  )
  private String caseManagementOrder;
  @CCD(
          label = "This is a hearing that lasts no longer than 30 minutes, with only paper-based evidence presented",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String hearingDisposalText2;
  @CCD(
          label = "This is a hearing which lasts longer than 30 minutes, with spoken and paper-based evidence presented",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String hearingTrialText2;
  @CCD(
          label = "### ${applicantVRespondentText}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
  )
  private String caseManagementOrderDisposal;
  @CCD(
          label = "Order and hearing details",
          searchable = false,
          access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
  )
  private DisposalHearingOrderAndHearingDetailsDJ disposalHearingOrderAndHearingDetailsDJ;
  @CCD(
          label = "### There is judgment for the claimant for an amount to be decided by the court",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
  )
  private String disposalHearingJudgementStatementDJ;
  @CCD(
          label = "## Hearing Method",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
  )
  private String disposalHearingMethodTitleDJ;
  @CCD(
          label = "Parties must file at court details of their respective relevant email addresses by 4.00pm\n 7 days before the disposal hearing so that the court may send out invitations for them to join the disposal hearing",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
  )
  private String disposalHearingMethodVideoConferenceHearingCourtStatementDJ;
  @CCD(
          label = "Parties must file at court details of their respective relevant telephone numbers by 4.00pm\n 7 days before the disposal hearing so that the court may telephone them to join them to the disposal hearing",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
  )
  private String disposalHearingMethodTelephoneHearingCourtStatementDJ;
  @CCD(
          label = "## Claim settling \n The Claimant must notify the court immediately if the case is settled. Where appropriate the Claimant must use the portal to notify the court of settlement.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
  )
  private String disposalHearingClaimSettlingDJ;
  @JsonProperty("DisposalHearingCostsDJ")
  @CCD(
          label = "## Costs \n Costs in the case",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
  )
  private String disposalHearingCostsDJ;
  @CCD(
          label = "### ${applicantVRespondentText}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
  )
  private String caseManagementOrderTrial;
  @CCD(
          label = "# Order and hearing details \n Warning: you must comply with this order: otherwise, your case might be struck out or some other sanction might be imposed. If you cannot comply, you are expected to make a formal application to the court before any deadline imposed upon you expires.\n ****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
  )
  private String trialHearingOrderAndHearingDetailsDJ;
  @CCD(
          label = "### Judgment for the claimant for an sum to be decided by the court \n ### Allocation \n the claim is allocated to the fast track \n ****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
  )
  private String trialHearingAllocation;
  @CCD(
          label = "## Alternative dispute resolution \nAt all stages, the parties must consider settling this litigation by any means of Alternative Dispute Resolution. This includes round table conferences, early neutral evaluation, mediation and arbitration. Any party not engaging in any such means proposed by another must upload to the Digital Portal a witness statement giving reasons within 21 days of receipt of that proposal. That witness statement must not be shown to the trial judge until questions of costs arise.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
  )
  private String trialFastTrackAltDisputeResolution;
  @CCD(
          label = "## Variation of directions \nThe Parties may, by written agreement, extend time for compliance with a direction where that is permitted by CPR 3.8(4). Otherwise, the time for compliance with a direction may only be extended by making an application",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
  )
  private String trialFastTrackVariationOfDirections;
  @CCD(
          label = "## Settlement \nEach party must inform the Court immediately if the case is settled whether or not it is then possible to upload to the Digital Portal a draft consent order to give effect to their agreement",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
  )
  private String trialFastTrackSettlement;
  @CCD(
          label = "## Costs \n Costs in the case \n ****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
  )
  private String trialHearingCosts;
  @CCD(
          label = "## Hearing method",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
  )
  private String trialHearingMethodTitleDJ;
  @CCD(
          label = "Parties must file at court details of their respective relevant email addresses by 4.00pm\n 7 days before the disposal hearing so that the court may send out invitations for them to join the disposal hearing",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
  )
  private String trialHearingMethodVideoConferenceHearingCourtStatementDJ;
  @CCD(
          label = "Parties must file at court details of their respective relevant telephone numbers by 4.00pm\n 7 days before the disposal hearing so that the court may telephone them to join them to the disposal hearing",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CITIZENCLAIMANTPROFILECruPlus4RolesPrbgzjAccess.class}
  )
  private String trialHearingMethodTelephoneHearingCourtStatementDJ;
  @CCD(
          label = "### ${applicantVRespondentText}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String orderApplicantVRespondent;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderJudgeProfileRAccess.class}
  )
  private String sdoR2DisposalHearingWelshLanguageEndLineDJ;
  @CCD(
          label = "****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderJudgeProfileRAccess.class}
  )
  private String sdoR2TrialWelshLanguageEndLineDJ;
  @CCD(
          label = "## Use of the Welsh language",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String useOfWelshLanguageDisposal;
  @CCD(
          label = "## Use of the Welsh language",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRJudgeProfileCruAccess.class}
  )
  private String useOfWelshLanguageTrial;
  @CCD(
          label = "## Bundles",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class}
  )
  private String bundleLabel;
  @CCD(
          label = " ",
          searchable = false,
          access = {DefaultAccess.class, RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class}
  )
  private java.util.Set<BundleUpload> bundleSelectionEvidence;
  @CCD(
          label = "Check the order the court sent you for what documents you need to upload \n for your case. \n\n You cannot withdraw a document once you have uploaded it. If you want to \n add more information to something you have already uploaded, you can \n upload the document again and add a version number to the name, for \n example \"version 2\". \n\n The other parties will be able to see the documents you have uploaded, and \n you will be able to see their documents \n # Deadlines for uploading documents \n Check the order the court sent you for the deadlines for uploading your documents. If you upload a document after the deadline, you will have to apply to the court to use it at the hearing and the judge will decide if it can be accepted \n\n You do not have to upload all your documents at once. You can return to upload them later. \n # How to upload your evidence \n If a document has multiple pages, upload them as one file rather than multiple files. \n\n Before you upload the document, give it a name that tells the court what it is, for example \"Witness statement by Jane Smith\". \n\n Each document must be less than 100MB. You can upload the following file types: Word, Excel, PowerPoint, PDF, RTF, TXT, CSV, JPG, JPEG, PNG, BMP. TIF, TIFF.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class}
  )
  private String evidenceUploadText;
  @CCD(
          label = "You can select more than one type of document \n\n\n ## Disclosure",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class}
  )
  private String disclosureLabel;
  @CCD(
          label = "<details><summary><u>Disclosure</u></summary><div class=\"panel\">\n\n ## Disclosure list \n\n A list of the document that you must show the other parties \n\n ## Documents for disclosure \n\n Recorded information that you must show the other parties - for example, contracts, invoices, receipts, emails, text messages, photos, social media messages.</div></details><br />",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class}
  )
  private String disclosureToggleText;
  @CCD(
          label = "## Witness evidence",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class}
  )
  private String witnessLabel;
  @CCD(
          label = "<details><summary><u>Witness evidence</u></summary><div class=\"panel\">\n\n ## Witness statement \n\n Your written evidence if you are giving evidence, written evidence by your witness \n\n ## Witness summary \n\n If you cannot get a full witness statement, you can use a witness summary of the evidence you would include in a witness statement. You need permission from the court to use a witness summary \n\n ## Notice of the intention to rely on hearsay evidence \n\n Notice to tell the other parties that you intend to rely on hearsay evidence at the trial. If the evidence is in a witness statement and the witness is not going to be in court, you must say why. \n\n ## Documents referred to in the statement \n\n Documents you or your witness must refer to in the statement - for example, emails, receipts, invoices, contracts and photos.</div></details><br />",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class}
  )
  private String witnessToggleText;
  @CCD(
          label = "## Expert evidence",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class}
  )
  private String expertLabel;
  @CCD(
          label = "<details><summary><u>Expert evidence</u></summary><div class=\"panel\">\n\n ## Expert's report \n\n Written evidence by an expert, If you gave agreed to share a single joint expert, check if the other party has uploaded the report \n\n ## Joint Statement of Experts / Single Joint Expert Report \n\n Statement by both parties' experts setting out their areas of agreement and disagreement. \n\n ## Questions for other party/other joint expert \n\n Written questions about an expert's report or a joint statement of experts \n\n ## Answers to questions asked \n\n Your expert's answers to questions put by the other party.</div></details><br />",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class}
  )
  private String expertToggleText;
  @CCD(
          label = "## Trial Documents",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class}
  )
  private String trialLabel;
  @CCD(
          label = "<details><summary><u>Trial documents</u></summary><div class=\"panel\">\n\n ## Case summary \n\n Overview of your whole case \n\n ## Skeleton argument \n\n Summary of the case, the areas in dispute and the reason why you think those disputes should be resolved in your favour \n\n ## Authorities \n\n Details of the case law, legislation or other legal precedent that you are going to rely on in court \n\n ## Costs \n\n A detailed list of the costs you have incurred in making or defending the claim, for example photocopying, getting copies of contracts and legal fees. Include receipts \n\n ## Documentary evidence for trial \n\n Documents that you wish to reply on at the trial - for example, emails, receipts, invoices, contracts and photos. You do not need to upload documents you have already uploaded</div></details><br />",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class}
  )
  private String trialToggleText;
  @CCD(
          label = "You can select more than one type of document \n\n\n ## Witness evidence \n\n Your statement if you are giving evidence, statement written by your witness, summary of evidence",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class}
  )
  private String witnessLabelSmallClaim;
  @CCD(
          label = "<details><summary><u>Witness evidence</u></summary><div class=\"panel\">\n\n ## Witness statement \n\n Your written evidence if you are giving evidence, written evidence by your witness \n\n ## Witness summary \n\n If you cannot get a full witness statement, you can use a witness summary of the evidence you would include in a witness statement. You need permission from the court to use a witness summary \n\n ## Documents referred to in the statement \n\n Documents you or your witness must refer to in the statement - for example, emails, receipts, invoices, contracts and photos.</div></details><br />",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class}
  )
  private String witnessToggleTextSmallClaim;
  @CCD(
          label = "## Expert evidence \n\n Written evidence by your expert, joint statement by both parties' experts.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class}
  )
  private String expertLabelSmallClaim;
  @CCD(
          label = "<details><summary><u>Expert evidence</u></summary><div class=\"panel\">\n\n ## Expert's report \n\n Written evidence by an expert, If you gave agreed to share a single joint expert, check if the other party has uploaded the report \n\n ## Joint Statement of Experts / Single Joint Expert Report \n\n Statement by both parties' experts setting out their areas of agreement and disagreement.</div></details><br />",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class}
  )
  private String expertToggleTextSmallClaim;
  @CCD(
          label = "## Trial Documents \n\n Documents for your trial including authorities, costs and documentary evidence for trial.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class}
  )
  private String trialLabelSmallClaim;
  @CCD(
          label = "<details><summary><u>Trial documents</u></summary><div class=\"panel\">\n\n## Authorities \n\n Details of the case law, legislation or other legal precedent that you are going to rely on in court \n\n ## Costs \n\n A detailed list of the costs you have incurred in making or defending the claim, for example photocopying, getting copies of contracts and legal fees. Include receipts \n\n ## Documentary evidence for trial \n\n Documents that you wish to reply on at the trial - for example, emails, receipts, invoices, contracts and photos. You do not need to upload documents you have already uploaded</div></details><br />",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class}
  )
  private String trialToggleTextSmallClaim;
  @CCD(
          label = "## You cannot withdraw a document once you have uploaded it \n ****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class}
  )
  private String documentUploadLabel;
  @CCD(
          label = "Check the order the court sent you for what documents you need to upload \n for your case. \n\n You cannot withdraw a document once you have uploaded it. If you want to \n add more information to something you have already uploaded, you can \n upload the document again and add a version number to the name, for \n example \"version 2\". \n\n The other parties will be able to see the documents you have uploaded, and \n you will be able to see their documents \n # Deadlines for uploading documents \n Check the order the court sent you for the deadlines for uploading your documents. If you upload a document after the deadline, you will have to apply to the court to use it at the hearing and the judge will decide if it can be accepted \n\n You do not have to upload all your documents at once. You can return to upload them later. \n # How to upload your evidence \n If a document has multiple pages, upload them as one file rather than multiple files. \n\n Before you upload the document, give it a name that tells the court what it is, for example \"Witness statement by Jane Smith\". \n\n Each document must be less than 100MB. You can upload the following file types: Word, Excel, PowerPoint, PDF, RTF, TXT, CSV, JPG, JPEG, PNG, BMP. TIF, TIFF.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class}
  )
  private String evidenceUploadTextRes;
  @CCD(
          label = "You can select more than one type of document \n\n\n ## Disclosure",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class}
  )
  private String disclosureLabelRes;
  @CCD(
          label = "<details><summary><u>Disclosure</u></summary><div class=\"panel\">\n\n ## Disclosure list \n\n A list of the document that you must show the other parties \n\n ## Documents for disclosure \n\n Recorded information that you must show the other parties - for example, contracts, invoices, receipts, emails, text messages, photos, social media messages.</div></details><br />",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class}
  )
  private String disclosureToggleTextRes;
  @CCD(
          label = "## Witness evidence",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class}
  )
  private String witnessLabelRes;
  @CCD(
          label = "<details><summary><u>Witness evidence</u></summary><div class=\"panel\">\n\n ## Witness statement \n\n Your written evidence if you are giving evidence, written evidence by your witness \n\n ## Witness summary \n\n If you cannot get a full witness statement, you can use a witness summary of the evidence you would include in a witness statement. You need permission from the court to use a witness summary \n\n ## Notice of the intention to rely on hearsay evidence \n\n Notice to tell the other parties that you intend to rely on hearsay evidence at the trial. If the evidence is in a witness statement and the witness is not going to be in court, you must say why. \n\n ## Documents referred to in the statement \n\n Documents you or your witness must refer to in the statement - for example, emails, receipts, invoices, contracts and photos.</div></details><br />",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class}
  )
  private String witnessToggleTextRes;
  @CCD(
          label = "## Expert evidence",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class}
  )
  private String expertLabelRes;
  @CCD(
          label = "<details><summary><u>Expert evidence</u></summary><div class=\"panel\">\n\n ## Expert's report \n\n Written evidence by an expert, If you gave agreed to share a single joint expert, check if the other party has uploaded the report \n\n ## Joint Statement of Experts / Single Joint Expert Report \n\n Statement by both parties' experts setting out their areas of agreement and disagreement. \n\n ## Questions for other party/other joint expert \n\n Written questions about an expert's report or a joint statement of experts \n\n ## Answers to questions asked \n\n Your expert's answers to questions put by the other party.</div></details><br />",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class}
  )
  private String expertToggleTextRes;
  @CCD(
          label = "## Trial Documents",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class}
  )
  private String trialLabelRes;
  @CCD(
          label = "<details><summary><u>Trial documents</u></summary><div class=\"panel\">\n\n ## Case summary \n\n Overview of your whole case \n\n ## Skeleton argument \n\n Summary of the case, the areas in dispute and the reason why you think those disputes should be resolved in your favour \n\n ## Authorities \n\n Details of the case law, legislation or other legal precedent that you are going to rely on in court \n\n ## Costs \n\n A detailed list of the costs you have incurred in making or defending the claim, for example photocopying, getting copies of contracts and legal fees. Include receipts \n\n ## Documentary evidence for trial \n\n Documents that you wish to reply on at the trial - for example, emails, receipts, invoices, contracts and photos. You do not need to upload documents you have already uploaded</div></details><br />",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class}
  )
  private String trialToggleTextRes;
  @CCD(
          label = "You can select more than one type of document \n\n\n ## Witness evidence \n\n Your statement if you are giving evidence, statement written by your witness, summary of evidence",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class}
  )
  private String witnessLabelSmallClaimRes;
  @CCD(
          label = "<details><summary><u>Witness evidence</u></summary><div class=\"panel\">\n\n ## Witness statement \n\n Your written evidence if you are giving evidence, written evidence by your witness \n\n ## Witness summary \n\n If you cannot get a full witness statement, you can use a witness summary of the evidence you would include in a witness statement. You need permission from the court to use a witness summary \n\n ## Documents referred to in the statement \n\n Documents you or your witness must refer to in the statement - for example, emails, receipts, invoices, contracts and photos.</div></details><br />",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class}
  )
  private String witnessToggleTextSmallClaimRes;
  @CCD(
          label = "## Expert evidence \n\n Written evidence by your expert, joint statement by both parties' experts.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class}
  )
  private String expertLabelSmallClaimRes;
  @CCD(
          label = "<details><summary><u>Expert evidence</u></summary><div class=\"panel\">\n\n ## Expert's report \n\n Written evidence by an expert, If you gave agreed to share a single joint expert, check if the other party has uploaded the report \n\n ## Joint Statement of Experts / Single Joint Expert Report \n\n Statement by both parties' experts setting out their areas of agreement and disagreement.</div></details><br />",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class}
  )
  private String expertToggleTextSmallClaimRes;
  @CCD(
          label = "## Trial Documents \n\n Documents for your trial including case summary, skeleton argument and authorities and Costs.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class}
  )
  private String trialLabelSmallClaimRes;
  @CCD(
          label = "<details><summary><u>Trial documents</u></summary><div class=\"panel\">\n\n## Authorities \n\n Details of the case law, legislation or other legal precedent that you are going to rely on in court \n\n ## Costs \n\n A detailed list of the costs you have incurred in making or defending the claim, for example photocopying, getting copies of contracts and legal fees. Include receipts \n\n ## Documentary evidence for trial \n\n Documents that you wish to reply on at the trial - for example, emails, receipts, invoices, contracts and photos. You do not need to upload documents you have already uploaded</div></details><br />",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class}
  )
  private String trialToggleTextSmallClaimRes;
  @CCD(
          label = "## You cannot withdraw a document once you have uploaded it \n ****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class}
  )
  private String documentUploadLabelRes;
  @CCD(
          label = "### ${legacyCaseReference} ${applicant1.partyName} Vs ${respondent1.partyName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerCivilSystemFieldReaderRAccess.class}
  )
  private String caseTitleWithReference;
  @JsonProperty("QueryManagementComponentLauncher")
  @CCD(
          label = "Component Launcher",
          searchable = false,
          typeOverride = FieldType.ComponentLauncher,
          access = {RESSOLONESPECPROFILECruPlus3RolesYnfilwAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILECITIZENDEFENDANTPROFILECruAccess.class, GSProfileRPlus4RolesOdnovhAccess.class, CaseworkerCivilSystemupdateCudAccess.class}
  )
  private String queryManagementComponentLauncher;
  // ==== end synthesised definition-only fields ====
}
