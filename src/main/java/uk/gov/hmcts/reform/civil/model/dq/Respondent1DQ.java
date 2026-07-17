package uk.gov.hmcts.reform.civil.model.dq;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.DeterWithoutHearing;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.account.AccountSimple;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.civil.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.WluAdminRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderCaseworkerCivilSystemupdateRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILECaseworkerCivilSystemupdateCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILERESSOLTWOSPECPROFILECuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffWluAdminRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILERPlus10RolesDfldtcAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECruPlus13RolesYennhdAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILERPlus3RolesGudjikAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruCaseworkerCivilSolicitorCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECruPlus13RolesRrmnypAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILECuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLTWOUNSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminRAccess;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Respondent1DQ implements DQ {

    @CCD(
            label = "File directions questionnaire",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECruAccess.class, WluAdminRAccess.class}
    )
    private FileDirectionsQuestionnaire respondent1DQFileDirectionsQuestionnaire;
    @CCD(
            label = "Defendant 1 Fixed Recoverable Costs",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
    )
    private FixedRecoverableCosts respondent1DQFixedRecoverableCosts;
    @CCD(
            label = "Defendant 1 Fixed Recoverable Costs",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECuAccess.class}
    )
    private FixedRecoverableCosts respondent1DQFixedRecoverableCostsIntermediate;
    @CCD(
            label = "Defendant 1 Disclosure of electronic documents",
            searchable = false,
            access = {DefaultAccess.class, APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private DisclosureOfElectronicDocuments respondent1DQDisclosureOfElectronicDocuments;
    @CCD(
            label = "Defendant 1 Disclosure of electronic documents",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECuAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private DisclosureOfElectronicDocuments specRespondent1DQDisclosureOfElectronicDocuments;
    @CCD(
            label = "Defendant 1 Disclosure of non-electronic documents",
            searchable = false,
            access = {DefaultAccess.class, APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private DisclosureOfNonElectronicDocuments respondent1DQDisclosureOfNonElectronicDocuments;
    @CCD(
            label = "Defendant 1 Disclosure of non-electronic documents",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECuAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private DisclosureOfNonElectronicDocuments specRespondent1DQDisclosureOfNonElectronicDocuments;
    @CCD(
            label = "Defendant 1 Disclosure report",
            searchable = false,
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECuAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private DisclosureReport respondent1DQDisclosureReport;
    @CCD(
            label = "Defendant 1 experts",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECuAccess.class, CaseworkerCivilSystemupdateCuAccess.class}
    )
    private Experts respondent1DQExperts;
    @CCD(
            label = "Expert Details",
            hint = "If the name is unknown at this time please add TBC to both the first name and last name lines. Then use the Manage Contact Information event to provide the name when known\n",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemFieldReaderCaseworkerCivilSystemupdateRAccess.class}
    )
    private ExpertDetails respondToClaimExperts;
    @CCD(
            label = "Defendant 1 witnesses",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess.class, CITIZENDEFENDANTPROFILECaseworkerCivilSystemupdateCuAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECuAccess.class}
    )
    private Witnesses respondent1DQWitnesses;
    @CCD(
            label = "Defendant 1 Hearing",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess.class, CITIZENDEFENDANTPROFILECaseworkerCivilSystemupdateCuAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECuAccess.class}
    )
    private Hearing respondent1DQHearing;
    @CCD(
            label = "Respondent 1 Hearing",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECuAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private SmallClaimHearing respondent1DQHearingSmallClaim;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECuAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private Hearing respondent1DQHearingFastClaim;
    @CCD(
            label = "Upload file",
            hint = "We accept documents sized 10MB or smaller, in these formats: pdf, txt, doc, dot, docx, rtf, xlt, xlsx, jpg, jpeg or png",
            regex = ".pdf,.txt,.doc,.dot,.docx,.rtf,.xls,.xlt,.xla,.xlsx,.xltx,.xlsb,.ppt,.pot,.pps,.ppa,.pptx,.potx,.ppsx,.jpg,.jpeg,.bmp,.tif,.tiff,.png",
            searchable = false,
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECruAccess.class, CaseworkerCivilStaffWluAdminRAccess.class}
    )
    private Document respondent1DQDraftDirections;
    @CCD(
            label = "Court location code",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffRCaseworkerCivilSystemupdateCruAccess.class, APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECruAccess.class, RESSOLONESPECPROFILECruAccess.class, WluAdminRAccess.class}
    )
    private RequestedCourt respondent1DQRequestedCourt;
    @CCD(
            label = "Defendant 1 Remote Hearing Questions",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECuAccess.class}
    )
    private RemoteHearing respondent1DQRemoteHearing;
    @CCD(
            label = "Defendant 1 Remote Hearing Questions",
            searchable = false,
            access = {APPSOLSPECPROFILERPlus10RolesDfldtcAccess.class}
    )
    private RemoteHearingLRspec respondent1DQRemoteHearingLRspec;
    @CCD(
            label = "Defendant 1 Hearing support requirements",
            searchable = false,
            access = {APPSOLSPECPROFILECruPlus13RolesYennhdAccess.class}
    )
    private HearingSupport respondent1DQHearingSupport;
    @CCD(
            label = "Defendant 1 Further information",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess.class}
    )
    private FurtherInformation respondent1DQFurtherInformation;
    @CCD(
            label = "Welsh language",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECruAccess.class, WluAdminRAccess.class}
    )
    private WelshLanguageRequirements respondent1DQLanguage;
    @CCD(ignore = true)
    private WelshLanguageRequirements respondent1DQLanguageLRspec;
    @CCD(
            label = "Statement of truth",
            searchable = false,
            access = {DefaultAccess.class, APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private StatementOfTruth respondent1DQStatementOfTruth;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private FutureApplications respondent1DQFutureApplications;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "BankAccount",
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILERPlus3RolesGudjikAccess.class}
    )
    private List<Element<AccountSimple>> respondent1BankAccountList;
    @CCD(
            label = " ",
            searchable = false,
            access = {APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECuAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private HomeDetails respondent1DQHomeDetails;
    @CCD(
            label = "Does your client claim Carer's Allowance or Carer's Credit?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECuAccess.class}
    )
    private YesOrNo respondent1DQCarerAllowanceCredit;
    @CCD(
            label = "Does your client claim Carer's Allowance of Carer's Credit?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECuAccess.class}
    )
    private YesOrNo respondent1DQCarerAllowanceCreditFullAdmission;
    @CCD(
            label = "Add details of any regular income your client receives:",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "RecurringIncome",
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECuAccess.class}
    )
    private List<Element<RecurringIncomeLRspec>> respondent1DQRecurringIncome;
    @CCD(
            label = "Add details of any regular income your client receives:",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "RecurringIncome",
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECuAccess.class}
    )
    private List<Element<RecurringIncomeLRspec>> respondent1DQRecurringIncomeFA;
    @CCD(
            label = "Add details of any regular expenses your client has:",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "RecurringExpense",
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECuAccess.class}
    )
    private List<Element<RecurringExpenseLRspec>> respondent1DQRecurringExpenses;
    @CCD(
            label = "Add details of any regular expenses your client has:",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "RecurringExpense",
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECuAccess.class}
    )
    private List<Element<RecurringExpenseLRspec>> respondent1DQRecurringExpensesFA;
    @CCD(
            label = "Do you want hearing to be held at specific court ? \n",
            hint = "If the defendant is an individual the case will be held at defendant's preferred court ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class}
    )
    private YesOrNo responseClaimCourtLocationRequired;
    @CCD(
            label = "Court Location",
            searchable = false,
            access = {CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCuAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilSolicitorCuAccess.class}
    )
    private RequestedCourt respondToCourtLocation;
    @CCD(
            label = "Defendant 1 Vulnerability Questions",
            searchable = false,
            access = {APPSOLSPECPROFILECruPlus13RolesRrmnypAccess.class}
    )
    private VulnerabilityQuestions respondent1DQVulnerabilityQuestions;
    @CCD(
            label = "Defendant 1 claimant documents to be considered",
            searchable = false,
            access = {CITIZENDEFENDANTPROFILECuAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private DocumentsToBeConsidered respondent1DQClaimantDocumentsToBeConsidered;
    @CCD(
            label = "Defendant 1 determination without a hearing",
            searchable = false,
            access = {APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess.class, RESSOLTWOUNSPECPROFILECruAccess.class, CaseworkerCivilAdminRAccess.class}
    )
    private DeterWithoutHearing deterWithoutHearingRespondent1;

    @Override
    @JsonProperty("deterWithoutHearingRespondent1")
    public DeterWithoutHearing getDeterWithoutHearing() {
        return deterWithoutHearingRespondent1;
    }

    @Override
    @JsonProperty("respondent1DQFileDirectionsQuestionnaire")
    public FileDirectionsQuestionnaire getFileDirectionQuestionnaire() {
        return respondent1DQFileDirectionsQuestionnaire;
    }

    @Override
    @JsonProperty("respondent1DQFixedRecoverableCosts")
    public FixedRecoverableCosts getFixedRecoverableCosts() {
        return respondent1DQFixedRecoverableCosts;
    }

    @Override
    @JsonProperty("respondent1DQFixedRecoverableCostsIntermediate")
    public FixedRecoverableCosts getFixedRecoverableCostsIntermediate() {
        return respondent1DQFixedRecoverableCostsIntermediate;
    }

    @Override
    @JsonProperty("respondent1DQDisclosureOfElectronicDocuments")
    public DisclosureOfElectronicDocuments getDisclosureOfElectronicDocuments() {
        return respondent1DQDisclosureOfElectronicDocuments;
    }

    @Override
    @JsonProperty("specRespondent1DQDisclosureOfElectronicDocuments")
    public DisclosureOfElectronicDocuments getSpecDisclosureOfElectronicDocuments() {
        return specRespondent1DQDisclosureOfElectronicDocuments;
    }

    @Override
    @JsonProperty("respondent1DQDisclosureOfNonElectronicDocuments")
    public DisclosureOfNonElectronicDocuments getDisclosureOfNonElectronicDocuments() {
        return respondent1DQDisclosureOfNonElectronicDocuments;
    }

    @Override
    @JsonProperty("specRespondent1DQDisclosureOfNonElectronicDocuments")
    public DisclosureOfNonElectronicDocuments getSpecDisclosureOfNonElectronicDocuments() {
        return specRespondent1DQDisclosureOfNonElectronicDocuments;
    }

    @Override
    @JsonProperty("respondent1DQDisclosureReport")
    public DisclosureReport getDisclosureReport() {
        return respondent1DQDisclosureReport;
    }

    @Override
    @JsonProperty("respondent1DQExperts")
    public Experts getExperts() {
        return getExperts(respondent1DQExperts);
    }

    @JsonProperty("respondToClaimExperts")
    public ExpertDetails getSmallClaimExperts() {
        return respondToClaimExperts;
    }

    @Override
    @JsonProperty("respondent1DQWitnesses")
    public Witnesses getWitnesses() {
        return getWitnesses(respondent1DQWitnesses);
    }

    @Override
    @JsonProperty("respondent1DQHearing")
    public Hearing getHearing() {
        if (respondent1DQHearing != null) {
            return getHearing(respondent1DQHearing);
        }
        DQUtil util = new DQUtil();

        if (respondent1DQHearingFastClaim != null) {
            return util.buildFastTrackHearing(respondent1DQHearingFastClaim);
        }
        if (respondent1DQHearingSmallClaim != null) {
            SmallClaimHearing small = getSmallClaimHearing();
            return util.buildSmallClaimHearing(small);
        }

        return null;
    }

    @Override
    @JsonProperty("respondent1DQHearingSmallClaim")
    public SmallClaimHearing getSmallClaimHearing() {
        return getSmallClaimHearing(respondent1DQHearingSmallClaim);
    }

    @Override
    @JsonProperty("respondent1DQDraftDirections")
    public Document getDraftDirections() {
        return respondent1DQDraftDirections;
    }

    @Override
    @JsonProperty("respondent1DQRequestedCourt")
    public RequestedCourt getRequestedCourt() {
        if (respondToCourtLocation != null || YesOrNo.YES.equals(responseClaimCourtLocationRequired)) {
            Optional<RequestedCourt> optRespondentDQ = Optional.ofNullable(this.respondent1DQRequestedCourt);
            Optional<RequestedCourt> optRespond = Optional.ofNullable(this.respondToCourtLocation);

            String responseCourtCode = Stream.of(
                optRespondentDQ.map(RequestedCourt::getResponseCourtCode),
                optRespond.map(RequestedCourt::getResponseCourtCode)
            ).filter(Optional::isPresent).findFirst().map(Optional::get).orElse(null);

            String reasonForHearingAtSpecificCourt = Stream.of(
                optRespondentDQ.map(RequestedCourt::getReasonForHearingAtSpecificCourt),
                optRespond.map(RequestedCourt::getReasonForHearingAtSpecificCourt)
            ).filter(Optional::isPresent).findFirst().map(Optional::get).orElse(null);

            RequestedCourt copy = new RequestedCourt()
                .setResponseCourtCode(responseCourtCode)
                .setReasonForHearingAtSpecificCourt(reasonForHearingAtSpecificCourt);

            Stream.of(
                optRespondentDQ.map(RequestedCourt::getCaseLocation),
                optRespond.map(RequestedCourt::getCaseLocation)
            ).filter(Optional::isPresent).findFirst().map(Optional::get).ifPresent(copy::setCaseLocation);

            return copy;
        }
        return respondent1DQRequestedCourt;
    }

    @Override
    @JsonProperty("respondent1DQRemoteHearingLRspec")
    public RemoteHearingLRspec getRemoteHearingLRspec() {
        return respondent1DQRemoteHearingLRspec;
    }

    @Override
    @JsonProperty("respondent1DQHearingSupport")
    public HearingSupport getHearingSupport() {
        return respondent1DQHearingSupport;
    }

    @Override
    @JsonProperty("respondent1DQFurtherInformation")
    public FurtherInformation getFurtherInformation() {
        return respondent1DQFurtherInformation;
    }

    @Override
    @JsonProperty("respondent1DQLanguage")
    public WelshLanguageRequirements getWelshLanguageRequirements() {
        return respondent1DQLanguage;
    }

    @Override
    @JsonProperty("respondent1DQStatementOfTruth")
    public StatementOfTruth getStatementOfTruth() {
        return respondent1DQStatementOfTruth;
    }

    @JsonProperty("respondent1DQFutureApplications")
    public FutureApplications getFutureApplications() {
        return respondent1DQFutureApplications;
    }

    @Override
    @JsonProperty("respondent1DQLanguageLRspec")
    public WelshLanguageRequirements getWelshLanguageRequirementsLRspec() {
        return respondent1DQLanguageLRspec;
    }

    @Override
    @JsonProperty("respondent1DQVulnerabilityQuestions")
    public VulnerabilityQuestions getVulnerabilityQuestions() {
        return respondent1DQVulnerabilityQuestions;
    }

    @Override
    @JsonProperty("respondent1DQRemoteHearing")
    public RemoteHearing getRemoteHearing() {
        return respondent1DQRemoteHearing;
    }

    @Override
    @JsonProperty("respondent1DQClaimantDocumentsToBeConsidered")
    public DocumentsToBeConsidered getDocumentsToBeConsidered() {
        return respondent1DQClaimantDocumentsToBeConsidered;
    }

    public Respondent1DQ copy() {
        return new Respondent1DQ()
            .setRespondent1DQFileDirectionsQuestionnaire(respondent1DQFileDirectionsQuestionnaire)
            .setRespondent1DQFixedRecoverableCosts(respondent1DQFixedRecoverableCosts)
            .setRespondent1DQFixedRecoverableCostsIntermediate(respondent1DQFixedRecoverableCostsIntermediate)
            .setRespondent1DQDisclosureOfElectronicDocuments(respondent1DQDisclosureOfElectronicDocuments)
            .setSpecRespondent1DQDisclosureOfElectronicDocuments(specRespondent1DQDisclosureOfElectronicDocuments)
            .setRespondent1DQDisclosureOfNonElectronicDocuments(respondent1DQDisclosureOfNonElectronicDocuments)
            .setSpecRespondent1DQDisclosureOfNonElectronicDocuments(specRespondent1DQDisclosureOfNonElectronicDocuments)
            .setRespondent1DQDisclosureReport(respondent1DQDisclosureReport)
            .setRespondent1DQExperts(respondent1DQExperts)
            .setRespondToClaimExperts(respondToClaimExperts)
            .setRespondent1DQWitnesses(respondent1DQWitnesses)
            .setRespondent1DQHearing(respondent1DQHearing)
            .setRespondent1DQHearingSmallClaim(respondent1DQHearingSmallClaim)
            .setRespondent1DQHearingFastClaim(respondent1DQHearingFastClaim)
            .setRespondent1DQDraftDirections(respondent1DQDraftDirections)
            .setRespondent1DQRequestedCourt(respondent1DQRequestedCourt)
            .setRespondent1DQRemoteHearing(respondent1DQRemoteHearing)
            .setRespondent1DQRemoteHearingLRspec(respondent1DQRemoteHearingLRspec)
            .setRespondent1DQHearingSupport(respondent1DQHearingSupport)
            .setRespondent1DQFurtherInformation(respondent1DQFurtherInformation)
            .setRespondent1DQLanguage(respondent1DQLanguage)
            .setRespondent1DQLanguageLRspec(respondent1DQLanguageLRspec)
            .setRespondent1DQStatementOfTruth(respondent1DQStatementOfTruth)
            .setRespondent1DQFutureApplications(respondent1DQFutureApplications)
            .setRespondent1BankAccountList(respondent1BankAccountList)
            .setRespondent1DQHomeDetails(respondent1DQHomeDetails)
            .setRespondent1DQCarerAllowanceCredit(respondent1DQCarerAllowanceCredit)
            .setRespondent1DQCarerAllowanceCreditFullAdmission(respondent1DQCarerAllowanceCreditFullAdmission)
            .setRespondent1DQRecurringIncome(respondent1DQRecurringIncome)
            .setRespondent1DQRecurringIncomeFA(respondent1DQRecurringIncomeFA)
            .setRespondent1DQRecurringExpenses(respondent1DQRecurringExpenses)
            .setRespondent1DQRecurringExpensesFA(respondent1DQRecurringExpensesFA)
            .setResponseClaimCourtLocationRequired(responseClaimCourtLocationRequired)
            .setRespondToCourtLocation(respondToCourtLocation)
            .setRespondent1DQVulnerabilityQuestions(respondent1DQVulnerabilityQuestions)
            .setRespondent1DQClaimantDocumentsToBeConsidered(respondent1DQClaimantDocumentsToBeConsidered)
            .setDeterWithoutHearingRespondent1(deterWithoutHearingRespondent1);
    }
}
