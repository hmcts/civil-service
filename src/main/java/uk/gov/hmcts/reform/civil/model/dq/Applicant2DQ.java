package uk.gov.hmcts.reform.civil.model.dq;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.DeterWithoutHearing;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.civil.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLUNSPECPROFILECruRESSOLONEUNSPECPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECITIZENCLAIMANTPROFILECuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRAccess;

@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Applicant2DQ implements DQ {

    @CCD(
            label = "File directions questionnaire",
            searchable = false,
            access = {DefaultAccess.class, APPSOLUNSPECPROFILECruRESSOLONEUNSPECPROFILERAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private FileDirectionsQuestionnaire applicant2DQFileDirectionsQuestionnaire;
    @CCD(
            label = "Claimant 2 Fixed Recoverable Costs",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, APPSOLUNSPECPROFILECruRESSOLONEUNSPECPROFILERAccess.class, APPSOLSPECPROFILECITIZENCLAIMANTPROFILECuAccess.class}
    )
    private FixedRecoverableCosts applicant2DQFixedRecoverableCosts;
    @CCD(
            label = "Claimant 2 Fixed Recoverable Costs",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, APPSOLUNSPECPROFILECruRESSOLONEUNSPECPROFILERAccess.class, APPSOLSPECPROFILECuAccess.class}
    )
    private FixedRecoverableCosts applicant2DQFixedRecoverableCostsIntermediate;
    @CCD(
            label = "Claimant 2 Disclosure of electronic documents",
            searchable = false,
            access = {DefaultAccess.class, APPSOLUNSPECPROFILECruRESSOLONEUNSPECPROFILERAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private DisclosureOfElectronicDocuments applicant2DQDisclosureOfElectronicDocuments;
    @CCD(
            label = "Claimant 2 Disclosure of non-electronic documents",
            searchable = false,
            access = {DefaultAccess.class, APPSOLUNSPECPROFILECruRESSOLONEUNSPECPROFILERAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private DisclosureOfNonElectronicDocuments applicant2DQDisclosureOfNonElectronicDocuments;
    @CCD(
            label = "Claimant 2 Disclosure report",
            searchable = false,
            access = {DefaultAccess.class, APPSOLUNSPECPROFILECruRESSOLONEUNSPECPROFILERAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private DisclosureReport applicant2DQDisclosureReport;
    @CCD(
            label = "Claimant 2 experts",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, APPSOLUNSPECPROFILECruRESSOLONEUNSPECPROFILERAccess.class, APPSOLSPECPROFILECuAccess.class, CaseworkerCivilSystemupdateCuAccess.class}
    )
    private Experts applicant2DQExperts;
    @CCD(ignore = true)
    private ExpertDetails applicant2RespondToClaimExperts;
    @CCD(
            label = "Claimant 2 witnesses",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, APPSOLUNSPECPROFILECruRESSOLONEUNSPECPROFILERAccess.class, APPSOLSPECPROFILECuAccess.class, CaseworkerCivilSystemupdateCuAccess.class}
    )
    private Witnesses applicant2DQWitnesses;
    @CCD(
            label = "Claimant 2 Hearing",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, APPSOLUNSPECPROFILECruRESSOLONEUNSPECPROFILERAccess.class, CaseworkerCivilSystemupdateCuAccess.class}
    )
    private Hearing applicant2DQHearing;
    @CCD(ignore = true)
    private SmallClaimHearing applicant2DQSmallClaimHearing;
    @CCD(
            label = "Upload file",
            hint = "We accept documents sized 10MB or smaller, in these formats: pdf, txt, doc, dot, docx, rtf, xlt, xlsx, jpg, jpeg or png",
            regex = ".pdf,.txt,.doc,.dot,.docx,.rtf,.xls,.xlt,.xla,.xlsx,.xltx,.xlsb,.ppt,.pot,.pps,.ppa,.pptx,.potx,.ppsx,.jpg,.jpeg,.bmp,.tif,.tiff,.png",
            searchable = false,
            access = {DefaultAccess.class, APPSOLUNSPECPROFILECruRESSOLONEUNSPECPROFILERAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private Document applicant2DQDraftDirections;
    @CCD(label = " ", searchable = false, access = {CaseworkerCivilSystemFieldReaderRAccess.class})
    private RequestedCourt applicant2DQRequestedCourt;
    @CCD(
            label = "Claimant 2 Hearing support requirements",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, APPSOLUNSPECPROFILECruRESSOLONEUNSPECPROFILERAccess.class}
    )
    private HearingSupport applicant2DQHearingSupport;
    @CCD(
            label = "Claimant 2 Further information",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, APPSOLUNSPECPROFILECruRESSOLONEUNSPECPROFILERAccess.class}
    )
    private FurtherInformation applicant2DQFurtherInformation;
    @CCD(
            label = "Welsh language",
            searchable = false,
            access = {DefaultAccess.class, APPSOLUNSPECPROFILECruRESSOLONEUNSPECPROFILERAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private WelshLanguageRequirements applicant2DQLanguage;
    @CCD(ignore = true)
    private WelshLanguageRequirements applicant2DQLanguageLRspec;
    @CCD(ignore = true)
    private RemoteHearingLRspec applicant2DQRemoteHearingLRspec;
    @CCD(
            label = "Statement of truth",
            searchable = false,
            access = {DefaultAccess.class, APPSOLUNSPECPROFILECruRESSOLONEUNSPECPROFILERAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private StatementOfTruth applicant2DQStatementOfTruth;
    @CCD(
            label = "Claimant 2 Vulnerability Questions",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, APPSOLUNSPECPROFILECruRESSOLONEUNSPECPROFILERAccess.class, APPSOLSPECPROFILECITIZENCLAIMANTPROFILECuAccess.class}
    )
    private VulnerabilityQuestions applicant2DQVulnerabilityQuestions;

    private RemoteHearing remoteHearing;

    @JsonProperty("applicant2DQRemoteHearing")
    public RemoteHearing getRemoteHearing() {
        return remoteHearing;
    }

    @Override
    @JsonProperty("applicant2DQFileDirectionsQuestionnaire")
    public FileDirectionsQuestionnaire getFileDirectionQuestionnaire() {
        return applicant2DQFileDirectionsQuestionnaire;
    }

    @Override
    @JsonProperty("applicant2DQFixedRecoverableCosts")
    public FixedRecoverableCosts getFixedRecoverableCosts() {
        return applicant2DQFixedRecoverableCosts;
    }

    @Override
    @JsonProperty("applicant2DQFixedRecoverableCostsIntermediate")
    public FixedRecoverableCosts getFixedRecoverableCostsIntermediate() {
        return applicant2DQFixedRecoverableCostsIntermediate;
    }

    @Override
    @JsonProperty("applicant2DQDisclosureOfElectronicDocuments")
    public DisclosureOfElectronicDocuments getDisclosureOfElectronicDocuments() {
        return applicant2DQDisclosureOfElectronicDocuments;
    }

    @Override
    public DisclosureOfElectronicDocuments getSpecDisclosureOfElectronicDocuments() {
        return null;
    }

    @Override
    @JsonProperty("applicant2DQDisclosureOfNonElectronicDocuments")
    public DisclosureOfNonElectronicDocuments getDisclosureOfNonElectronicDocuments() {
        return applicant2DQDisclosureOfNonElectronicDocuments;
    }

    @Override
    public DisclosureOfNonElectronicDocuments getSpecDisclosureOfNonElectronicDocuments() {
        return null;
    }

    @Override
    @JsonProperty("applicant2DQDisclosureReport")
    public DisclosureReport getDisclosureReport() {
        return applicant2DQDisclosureReport;
    }

    @Override
    @JsonProperty("applicant2DQExperts")
    public Experts getExperts() {
        return getExperts(applicant2DQExperts);
    }

    @Override
    public DeterWithoutHearing getDeterWithoutHearing() {
        return null;
    }

    @JsonProperty("applicant2RespondToClaimExperts")
    public ExpertDetails getSmallClaimExperts() {
        return applicant2RespondToClaimExperts;
    }

    @Override
    @JsonProperty("applicant2DQWitnesses")
    public Witnesses getWitnesses() {
        return getWitnesses(applicant2DQWitnesses);
    }

    @Override
    @JsonProperty("applicant2DQHearing")
    public Hearing getHearing() {
        return getHearing(applicant2DQHearing);
    }

    @Override
    public SmallClaimHearing getSmallClaimHearing() {
        return null;
    }

    @Override
    @JsonProperty("applicant2DQDraftDirections")
    public Document getDraftDirections() {
        return applicant2DQDraftDirections;
    }

    @Override
    public RequestedCourt getRequestedCourt() {
        return null;
    }

    @Override
    @JsonProperty("applicant2DQHearingSupport")
    public HearingSupport getHearingSupport() {
        return applicant2DQHearingSupport;
    }

    @Override
    @JsonProperty("applicant2DQFurtherInformation")
    public FurtherInformation getFurtherInformation() {
        return applicant2DQFurtherInformation;
    }

    @Override
    @JsonProperty("applicant2DQLanguage")
    public WelshLanguageRequirements getWelshLanguageRequirements() {
        return applicant2DQLanguage;
    }

    @Override
    @JsonProperty("applicant2DQLanguageLRspec")
    public WelshLanguageRequirements getWelshLanguageRequirementsLRspec() {
        return applicant2DQLanguageLRspec;
    }

    @Override
    @JsonProperty("applicant2DQRemoteHearingLRspec")
    public RemoteHearingLRspec getRemoteHearingLRspec() {
        return applicant2DQRemoteHearingLRspec;
    }

    @Override
    @JsonProperty("applicant2DQStatementOfTruth")
    public StatementOfTruth getStatementOfTruth() {
        return applicant2DQStatementOfTruth;
    }

    @Override
    @JsonProperty("applicant2DQVulnerabilityQuestions")
    public VulnerabilityQuestions getVulnerabilityQuestions() {
        return applicant2DQVulnerabilityQuestions;
    }

    @Override
    public DocumentsToBeConsidered getDocumentsToBeConsidered() {
        return null;
    }

    public Applicant2DQ copy() {
        return new Applicant2DQ()
            .setApplicant2DQFileDirectionsQuestionnaire(applicant2DQFileDirectionsQuestionnaire)
            .setApplicant2DQFixedRecoverableCosts(applicant2DQFixedRecoverableCosts)
            .setApplicant2DQFixedRecoverableCostsIntermediate(applicant2DQFixedRecoverableCostsIntermediate)
            .setApplicant2DQDisclosureOfElectronicDocuments(applicant2DQDisclosureOfElectronicDocuments)
            .setApplicant2DQDisclosureOfNonElectronicDocuments(applicant2DQDisclosureOfNonElectronicDocuments)
            .setApplicant2DQDisclosureReport(applicant2DQDisclosureReport)
            .setApplicant2DQExperts(applicant2DQExperts)
            .setApplicant2RespondToClaimExperts(applicant2RespondToClaimExperts)
            .setApplicant2DQWitnesses(applicant2DQWitnesses)
            .setApplicant2DQHearing(applicant2DQHearing)
            .setApplicant2DQSmallClaimHearing(applicant2DQSmallClaimHearing)
            .setApplicant2DQDraftDirections(applicant2DQDraftDirections)
            .setApplicant2DQRequestedCourt(applicant2DQRequestedCourt)
            .setApplicant2DQHearingSupport(applicant2DQHearingSupport)
            .setApplicant2DQFurtherInformation(applicant2DQFurtherInformation)
            .setApplicant2DQLanguage(applicant2DQLanguage)
            .setApplicant2DQLanguageLRspec(applicant2DQLanguageLRspec)
            .setApplicant2DQRemoteHearingLRspec(applicant2DQRemoteHearingLRspec)
            .setApplicant2DQStatementOfTruth(applicant2DQStatementOfTruth)
            .setApplicant2DQVulnerabilityQuestions(applicant2DQVulnerabilityQuestions)
            .setRemoteHearing(remoteHearing);
    }
}
