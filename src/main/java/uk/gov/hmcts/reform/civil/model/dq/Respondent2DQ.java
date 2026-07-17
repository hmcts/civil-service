package uk.gov.hmcts.reform.civil.model.dq;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.DeterWithoutHearing;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.account.AccountSimple;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.civil.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLUNSPECPROFILERRESSOLTWOUNSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILERESSOLTWOSPECPROFILECuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILERPlus10RolesBeiltlAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECruPlus12RolesFfumtqAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENDEFENDANTPROFILECuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLTWOUNSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILERESSOLTWOSPECPROFILECrudAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECITIZENCLAIMANTPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruCaseworkerCivilSolicitorCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminRAccess;

@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Respondent2DQ implements DQ {

    @CCD(
            label = "File directions questionnaire",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERRESSOLTWOUNSPECPROFILECruAccess.class}
    )
    private FileDirectionsQuestionnaire respondent2DQFileDirectionsQuestionnaire;
    @CCD(
            label = "Defendant 2 Fixed Recoverable Costs",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, APPSOLUNSPECPROFILERRESSOLTWOUNSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
    )
    private FixedRecoverableCosts respondent2DQFixedRecoverableCosts;
    @CCD(
            label = "Defendant 2 Fixed Recoverable Costs",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, APPSOLUNSPECPROFILERRESSOLTWOUNSPECPROFILECruAccess.class}
    )
    private FixedRecoverableCosts respondent2DQFixedRecoverableCostsIntermediate;
    @CCD(
            label = "Defendant 2 Disclosure of electronic documents",
            searchable = false,
            access = {DefaultAccess.class, APPSOLUNSPECPROFILERRESSOLTWOUNSPECPROFILECruAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private DisclosureOfElectronicDocuments respondent2DQDisclosureOfElectronicDocuments;
    @CCD(
            label = "Defendant 2 Disclosure of electronic documents",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
    )
    private DisclosureOfElectronicDocuments specRespondent2DQDisclosureOfElectronicDocuments;
    @CCD(
            label = "Defendant 2 Disclosure of non-electronic documents",
            searchable = false,
            access = {DefaultAccess.class, APPSOLUNSPECPROFILERRESSOLTWOUNSPECPROFILECruAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private DisclosureOfNonElectronicDocuments respondent2DQDisclosureOfNonElectronicDocuments;
    @CCD(
            label = "Defendant 2 Disclosure of non-electronic documents",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
    )
    private DisclosureOfNonElectronicDocuments specRespondent2DQDisclosureOfNonElectronicDocuments;
    @CCD(
            label = "Defendant 2 Disclosure report",
            searchable = false,
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERRESSOLTWOUNSPECPROFILECruAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private DisclosureReport respondent2DQDisclosureReport;
    @CCD(
            label = "Defendant 2 experts",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERCITIZENDEFENDANTPROFILECuAccess.class, APPSOLUNSPECPROFILERRESSOLTWOUNSPECPROFILECruAccess.class, CaseworkerCivilSystemupdateCuAccess.class}
    )
    private Experts respondent2DQExperts;
    @CCD(
            label = "Expert Details",
            hint = "If the name is unknown at this time please add TBC to both the first name and last name lines. Then use the Manage Contact Information event to provide the name when known\n",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private ExpertDetails respondToClaimExperts2;
    @CCD(
            label = "Defendant 2 witnesses",
            searchable = false,
            access = {APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, APPSOLUNSPECPROFILERRESSOLTWOUNSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class, CaseworkerCivilSystemupdateCuAccess.class}
    )
    private Witnesses respondent2DQWitnesses;
    @CCD(
            label = "Defendant 2 Hearing",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, APPSOLUNSPECPROFILERRESSOLTWOUNSPECPROFILECruAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECuAccess.class, CaseworkerCivilSystemupdateCuAccess.class}
    )
    private Hearing respondent2DQHearing;
    @CCD(
            label = "Respondent 1 Hearing",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class}
    )
    private SmallClaimHearing respondent2DQHearingSmallClaim;
    @CCD(
            label = "Upload file",
            hint = "We accept documents sized 10MB or smaller, in these formats: pdf, txt, doc, dot, docx, rtf, xlt, xlsx, jpg, jpeg or png",
            regex = ".pdf,.txt,.doc,.dot,.docx,.rtf,.xls,.xlt,.xla,.xlsx,.xltx,.xlsb,.ppt,.pot,.pps,.ppa,.pptx,.potx,.ppsx,.jpg,.jpeg,.bmp,.tif,.tiff,.png",
            searchable = false,
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERRESSOLTWOUNSPECPROFILECruAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private Document respondent2DQDraftDirections;
    @CCD(
            label = "Court location code",
            searchable = false,
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERRESSOLTWOUNSPECPROFILECruAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private RequestedCourt respondent2DQRequestedCourt;

    @CCD(
            label = "Defendant 2 Remote Hearing Questions",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, APPSOLUNSPECPROFILERRESSOLTWOUNSPECPROFILECruAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECuAccess.class}
    )
    private RemoteHearing respondent2DQRemoteHearing;
    @CCD(
            label = "Defendant 2 Remote Hearing Questions",
            searchable = false,
            access = {APPSOLSPECPROFILERPlus10RolesBeiltlAccess.class}
    )
    private RemoteHearingLRspec respondent2DQRemoteHearingLRspec;
    @CCD(
            label = "Defendant 2 Hearing support requirements",
            searchable = false,
            access = {APPSOLSPECPROFILECruPlus12RolesFfumtqAccess.class}
    )
    private HearingSupport respondent2DQHearingSupport;
    @CCD(
            label = "Does your client claim Carer's Allowance or Carer's Credit?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class}
    )
    private YesOrNo respondent2DQCarerAllowanceCredit;
    @CCD(
            label = "Defendant 2 Further information",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, APPSOLUNSPECPROFILERRESSOLTWOUNSPECPROFILECruAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECuAccess.class, CITIZENDEFENDANTPROFILECuAccess.class}
    )
    private FurtherInformation respondent2DQFurtherInformation;
    @CCD(
            label = "Welsh language",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class, RESSOLTWOUNSPECPROFILECruAccess.class}
    )
    private WelshLanguageRequirements respondent2DQLanguage;
    @CCD(ignore = true)
    private WelshLanguageRequirements respondent2DQLanguageLRspec;
    @CCD(
            label = "Statement of truth",
            searchable = false,
            access = {DefaultAccess.class, APPSOLUNSPECPROFILERRESSOLTWOUNSPECPROFILECruAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private StatementOfTruth respondent2DQStatementOfTruth;
    @CCD(
            label = "Defendant 2 Vulnerability Questions",
            searchable = false,
            access = {APPSOLSPECPROFILECruPlus12RolesFfumtqAccess.class}
    )
    private VulnerabilityQuestions respondent2DQVulnerabilityQuestions;
    @CCD(
            label = "Add details of any regular income your client receives:",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "RecurringIncome",
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class}
    )
    private List<Element<RecurringIncomeLRspec>> respondent2DQRecurringIncome;
    @CCD(
            label = "Add details of any regular expenses your client has:",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "RecurringExpense",
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class}
    )
    private List<Element<RecurringExpenseLRspec>> respondent2DQRecurringExpenses;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "BankAccount",
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECrudAccess.class}
    )
    private List<Element<AccountSimple>> respondent2BankAccountList;
    @CCD(
            label = " ",
            searchable = false,
            access = {APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILERAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private HomeDetails respondent2DQHomeDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, APPSOLSPECPROFILECITIZENCLAIMANTPROFILECruAccess.class}
    )
    private FutureApplications respondent2DQFutureApplications;
    @CCD(
            label = "Respondent 1 Hearing",
            searchable = false,
            access = {DefaultAccess.class, RESSOLONESPECPROFILERESSOLTWOSPECPROFILECruAccess.class, APPSOLSPECPROFILECITIZENCLAIMANTPROFILECruAccess.class}
    )
    private Hearing respondent2DQHearingFastClaim;
    @CCD(
            label = "Court Location",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECruCaseworkerCivilSolicitorCuAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private RequestedCourt respondToCourtLocation2;
    @CCD(
            label = "Defendant 2 determination without a hearing",
            searchable = false,
            access = {APPSOLSPECPROFILERRESSOLONESPECPROFILECruRESSOLTWOSPECPROFILECruAccess.class, APPSOLUNSPECPROFILERRESSOLONEUNSPECPROFILECruAccess.class, RESSOLTWOUNSPECPROFILECruAccess.class, CaseworkerCivilAdminRAccess.class}
    )
    private DeterWithoutHearing deterWithoutHearingRespondent2;

    @Override
    @JsonProperty("deterWithoutHearingRespondent2")
    public DeterWithoutHearing getDeterWithoutHearing() {
        return deterWithoutHearingRespondent2;
    }

    @Override
    @JsonProperty("respondent2DQFileDirectionsQuestionnaire")
    public FileDirectionsQuestionnaire getFileDirectionQuestionnaire() {
        return respondent2DQFileDirectionsQuestionnaire;
    }

    @Override
    @JsonProperty("respondent2DQFixedRecoverableCosts")
    public FixedRecoverableCosts getFixedRecoverableCosts() {
        return respondent2DQFixedRecoverableCosts;
    }

    @Override
    @JsonProperty("respondent2DQFixedRecoverableCostsIntermediate")
    public FixedRecoverableCosts getFixedRecoverableCostsIntermediate() {
        return respondent2DQFixedRecoverableCostsIntermediate;
    }

    @Override
    @JsonProperty("respondent2DQDisclosureOfElectronicDocuments")
    public DisclosureOfElectronicDocuments getDisclosureOfElectronicDocuments() {
        return respondent2DQDisclosureOfElectronicDocuments;
    }

    @Override
    @JsonProperty("specRespondent2DQDisclosureOfElectronicDocuments")
    public DisclosureOfElectronicDocuments getSpecDisclosureOfElectronicDocuments() {
        return specRespondent2DQDisclosureOfElectronicDocuments;
    }

    @Override
    @JsonProperty("respondent2DQDisclosureOfNonElectronicDocuments")
    public DisclosureOfNonElectronicDocuments getDisclosureOfNonElectronicDocuments() {
        return respondent2DQDisclosureOfNonElectronicDocuments;
    }

    @Override
    @JsonProperty("specRespondent2DQDisclosureOfNonElectronicDocuments")
    public DisclosureOfNonElectronicDocuments getSpecDisclosureOfNonElectronicDocuments() {
        return specRespondent2DQDisclosureOfNonElectronicDocuments;
    }

    @Override
    @JsonProperty("respondent2DQDisclosureReport")
    public DisclosureReport getDisclosureReport() {
        return respondent2DQDisclosureReport;
    }

    @Override
    @JsonProperty("respondent2DQExperts")
    public Experts getExperts() {
        return getExperts(respondent2DQExperts);
    }

    @JsonProperty("respondToClaimExperts2")
    public ExpertDetails getSmallClaimExperts() {
        return respondToClaimExperts2;
    }

    @Override
    @JsonProperty("respondent2DQWitnesses")
    public Witnesses getWitnesses() {
        return getWitnesses(respondent2DQWitnesses);
    }

    @Override
    @JsonProperty("respondent2DQHearing")
    public Hearing getHearing() {
        if (respondent2DQHearing != null) {
            return getHearing(respondent2DQHearing);
        }
        DQUtil util = new DQUtil();

        if (respondent2DQHearingFastClaim != null) {
            return util.buildFastTrackHearing(respondent2DQHearingFastClaim);
        }
        if (respondent2DQHearingSmallClaim != null) {
            SmallClaimHearing small = getSmallClaimHearing();
            return util.buildSmallClaimHearing(small);
        }

        return null;
    }

    @Override
    @JsonProperty("respondent2DQHearingSmallClaim")
    public SmallClaimHearing getSmallClaimHearing() {
        return getSmallClaimHearing(respondent2DQHearingSmallClaim);
    }

    @Override
    @JsonProperty("respondent2DQDraftDirections")
    public Document getDraftDirections() {
        return respondent2DQDraftDirections;
    }

    @Override
    @JsonProperty("respondent2DQRequestedCourt")
    public RequestedCourt getRequestedCourt() {
        return respondent2DQRequestedCourt;
    }

    @Override
    @JsonProperty("respondent2DQRemoteHearingLRspec")
    public RemoteHearingLRspec getRemoteHearingLRspec() {
        return respondent2DQRemoteHearingLRspec;
    }

    @Override
    @JsonProperty("respondent2DQHearingSupport")
    public HearingSupport getHearingSupport() {
        return respondent2DQHearingSupport;
    }

    @Override
    @JsonProperty("respondent2DQFurtherInformation")
    public FurtherInformation getFurtherInformation() {
        return respondent2DQFurtherInformation;
    }

    @Override
    @JsonProperty("respondent2DQLanguage")
    public WelshLanguageRequirements getWelshLanguageRequirements() {
        return respondent2DQLanguage;
    }

    @Override
    @JsonProperty("respondent2DQStatementOfTruth")
    public StatementOfTruth getStatementOfTruth() {
        return respondent2DQStatementOfTruth;
    }

    @Override
    @JsonProperty("respondent2DQVulnerabilityQuestions")
    public VulnerabilityQuestions getVulnerabilityQuestions() {
        return respondent2DQVulnerabilityQuestions;
    }

    @Override
    public DocumentsToBeConsidered getDocumentsToBeConsidered() {
        return null;
    }

    @Override
    @JsonProperty("respondent2DQLanguageLRspec")
    public WelshLanguageRequirements getWelshLanguageRequirementsLRspec() {
        return respondent2DQLanguageLRspec;
    }

    @Override
    @JsonProperty("respondent2DQRemoteHearing")
    public RemoteHearing getRemoteHearing() {
        return respondent2DQRemoteHearing;
    }

    public Respondent2DQ copy() {
        return new Respondent2DQ()
            .setRespondent2DQFileDirectionsQuestionnaire(respondent2DQFileDirectionsQuestionnaire)
            .setRespondent2DQFixedRecoverableCosts(respondent2DQFixedRecoverableCosts)
            .setRespondent2DQFixedRecoverableCostsIntermediate(respondent2DQFixedRecoverableCostsIntermediate)
            .setRespondent2DQDisclosureOfElectronicDocuments(respondent2DQDisclosureOfElectronicDocuments)
            .setSpecRespondent2DQDisclosureOfElectronicDocuments(specRespondent2DQDisclosureOfElectronicDocuments)
            .setRespondent2DQDisclosureOfNonElectronicDocuments(respondent2DQDisclosureOfNonElectronicDocuments)
            .setSpecRespondent2DQDisclosureOfNonElectronicDocuments(specRespondent2DQDisclosureOfNonElectronicDocuments)
            .setRespondent2DQDisclosureReport(respondent2DQDisclosureReport)
            .setRespondent2DQExperts(respondent2DQExperts)
            .setRespondToClaimExperts2(respondToClaimExperts2)
            .setRespondent2DQWitnesses(respondent2DQWitnesses)
            .setRespondent2DQHearing(respondent2DQHearing)
            .setRespondent2DQHearingSmallClaim(respondent2DQHearingSmallClaim)
            .setRespondent2DQDraftDirections(respondent2DQDraftDirections)
            .setRespondent2DQRequestedCourt(respondent2DQRequestedCourt)
            .setRespondent2DQRemoteHearing(respondent2DQRemoteHearing)
            .setRespondent2DQRemoteHearingLRspec(respondent2DQRemoteHearingLRspec)
            .setRespondent2DQHearingSupport(respondent2DQHearingSupport)
            .setRespondent2DQCarerAllowanceCredit(respondent2DQCarerAllowanceCredit)
            .setRespondent2DQFurtherInformation(respondent2DQFurtherInformation)
            .setRespondent2DQLanguage(respondent2DQLanguage)
            .setRespondent2DQLanguageLRspec(respondent2DQLanguageLRspec)
            .setRespondent2DQStatementOfTruth(respondent2DQStatementOfTruth)
            .setRespondent2DQVulnerabilityQuestions(respondent2DQVulnerabilityQuestions)
            .setRespondent2DQRecurringIncome(respondent2DQRecurringIncome)
            .setRespondent2DQRecurringExpenses(respondent2DQRecurringExpenses)
            .setRespondent2BankAccountList(respondent2BankAccountList)
            .setRespondent2DQHomeDetails(respondent2DQHomeDetails)
            .setRespondent2DQFutureApplications(respondent2DQFutureApplications)
            .setRespondent2DQHearingFastClaim(respondent2DQHearingFastClaim)
            .setRespondToCourtLocation2(respondToCourtLocation2)
            .setDeterWithoutHearingRespondent2(deterWithoutHearingRespondent2);
    }
}
