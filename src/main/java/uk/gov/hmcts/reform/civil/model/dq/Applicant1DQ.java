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
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffWluAdminRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILERESSOLONEUNSPECPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLUNSPECPROFILECruRESSOLONEUNSPECPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECITIZENCLAIMANTPROFILECuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLUNSPECPROFILECuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILERESSOLONESPECPROFILECruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.RESSOLONESPECPROFILERESSOLONEUNSPECPROFILEWluAdminRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRCitizenProfileCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemupdateWluAdminRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruCaseworkerCivilStaffRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CitizenProfileCuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.WluAdminRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CitizenProfileCruWluAdminRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.APPSOLSPECPROFILECruPlus13RolesWqnmqwAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECruCaseworkerCivilSystemupdateRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CITIZENCLAIMANTPROFILECuAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilSystemFieldReaderRAccess;
import uk.gov.hmcts.reform.civil.ccd.access.CaseworkerCivilAdminRAccess;

@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Applicant1DQ implements DQ {

    @CCD(
            label = "File directions questionnaire",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilStaffWluAdminRAccess.class}
    )
    private FileDirectionsQuestionnaire applicant1DQFileDirectionsQuestionnaire;
    @CCD(
            label = "Claimant 1 Fixed Recoverable Costs",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, RESSOLONESPECPROFILERESSOLONEUNSPECPROFILERAccess.class, CITIZENCLAIMANTPROFILECruAccess.class}
    )
    private FixedRecoverableCosts applicant1DQFixedRecoverableCosts;
    @CCD(
            label = "Claimant 1 Fixed Recoverable Costs",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, RESSOLONESPECPROFILERESSOLONEUNSPECPROFILERAccess.class}
    )
    private FixedRecoverableCosts applicant1DQFixedRecoverableCostsIntermediate;
    @CCD(
            label = "Claimant 1 Disclosure of electronic documents",
            searchable = false,
            access = {DefaultAccess.class, APPSOLUNSPECPROFILECruRESSOLONEUNSPECPROFILERAccess.class, APPSOLSPECPROFILECITIZENCLAIMANTPROFILECuAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private DisclosureOfElectronicDocuments applicant1DQDisclosureOfElectronicDocuments;
    @CCD(
            label = "Claimant 1 Disclosure of electronic documents",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess.class, APPSOLUNSPECPROFILECuAccess.class}
    )
    private DisclosureOfElectronicDocuments specApplicant1DQDisclosureOfElectronicDocuments;
    @CCD(
            label = "Claimant 1 Disclosure of non-electronic documents",
            searchable = false,
            access = {DefaultAccess.class, APPSOLUNSPECPROFILECruRESSOLONEUNSPECPROFILERAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private DisclosureOfNonElectronicDocuments applicant1DQDisclosureOfNonElectronicDocuments;
    @CCD(
            label = "Claimant 1 Disclosure of non-electronic documents",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, APPSOLSPECPROFILERESSOLONESPECPROFILECruAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private DisclosureOfNonElectronicDocuments specApplicant1DQDisclosureOfNonElectronicDocuments;
    @CCD(
            label = "Claimant 1 Disclosure report",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, RESSOLONESPECPROFILERESSOLONEUNSPECPROFILEWluAdminRAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private DisclosureReport applicant1DQDisclosureReport;
    @CCD(
            label = "Claimant 1 experts",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, RESSOLONESPECPROFILERESSOLONEUNSPECPROFILEWluAdminRAccess.class, CaseworkerCivilSystemupdateCuAccess.class}
    )
    private Experts applicant1DQExperts;
    @CCD(
            label = "Expert Details",
            hint = "If the name is unknown at this time please add TBC to both the first name and last name lines. Then use the Manage Contact Information event to provide the name when known\n",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemFieldReaderRCitizenProfileCruAccess.class, CaseworkerCivilSystemupdateWluAdminRAccess.class}
    )
    private ExpertDetails applicant1RespondToClaimExperts;
    @CCD(
            label = "Claimant 1 witnesses",
            searchable = false,
            access = {CaseworkerCivilAdminCruCaseworkerCivilSystemFieldReaderRAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, RESSOLONESPECPROFILERESSOLONEUNSPECPROFILEWluAdminRAccess.class, CaseworkerCivilSystemupdateCuAccess.class}
    )
    private Witnesses applicant1DQWitnesses;
    @CCD(
            label = "Claimant 1 Hearing",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, APPSOLUNSPECPROFILECruRESSOLONEUNSPECPROFILERAccess.class, APPSOLSPECPROFILECITIZENCLAIMANTPROFILECuAccess.class, CaseworkerCivilSystemupdateCuAccess.class}
    )
    private Hearing applicant1DQHearing;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilStaffRAccess.class, APPSOLUNSPECPROFILECuAccess.class}
    )
    private Hearing applicant1DQHearingLRspec;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilStaffRAccess.class, APPSOLUNSPECPROFILECuAccess.class, CitizenProfileCuAccess.class, WluAdminRAccess.class}
    )
    private SmallClaimHearing applicant1DQSmallClaimHearing;
    @CCD(
            label = "Upload file",
            hint = "We accept documents sized 10MB or smaller, in these formats: pdf, txt, doc, dot, docx, rtf, xlt, xlsx, jpg, jpeg or png",
            regex = ".pdf,.txt,.doc,.dot,.docx,.rtf,.xls,.xlt,.xla,.xlsx,.xltx,.xlsb,.ppt,.pot,.pps,.ppa,.pptx,.potx,.ppsx,.jpg,.jpeg,.bmp,.tif,.tiff,.png",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, CaseworkerCivilStaffWluAdminRAccess.class}
    )
    private Document applicant1DQDraftDirections;
    @CCD(
            label = " ",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECaseworkerCivilSolicitorCruAccess.class, CaseworkerCivilSystemFieldReaderRCaseworkerCivilSystemupdateCruAccess.class, CitizenProfileCruWluAdminRAccess.class}
    )
    private RequestedCourt applicant1DQRequestedCourt;
    @CCD(
            label = "Claimant 1 Hearing support requirements",
            searchable = false,
            access = {APPSOLSPECPROFILECruPlus13RolesWqnmqwAccess.class}
    )
    private HearingSupport applicant1DQHearingSupport;
    @CCD(
            label = "Claimant 1 Further information",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, CITIZENCLAIMANTPROFILECruCITIZENDEFENDANTPROFILERAccess.class, RESSOLONESPECPROFILERESSOLONEUNSPECPROFILEWluAdminRAccess.class}
    )
    private FurtherInformation applicant1DQFurtherInformation;
    @CCD(
            label = "Welsh language",
            searchable = false,
            access = {DefaultAccess.class, APPSOLSPECPROFILECruPlus4RolesUdjqhbAccess.class, CaseworkerCivilStaffCaseworkerCivilSystemupdateRAccess.class, CITIZENCLAIMANTPROFILECruAccess.class, CitizenProfileCuAccess.class, WluAdminRAccess.class}
    )
    private WelshLanguageRequirements applicant1DQLanguage;
    @CCD(
            label = "Claimant 1 Remote Hearing Questions",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, APPSOLSPECPROFILECruRESSOLONESPECPROFILERAccess.class, APPSOLUNSPECPROFILECuAccess.class, CITIZENCLAIMANTPROFILECruAccess.class}
    )
    private RemoteHearingLRspec applicant1DQRemoteHearingLRspec;
    @CCD(
            label = "Statement of truth",
            searchable = false,
            access = {DefaultAccess.class, APPSOLUNSPECPROFILECruRESSOLONEUNSPECPROFILERAccess.class, CaseworkerCivilStaffRAccess.class}
    )
    private StatementOfTruth applicant1DQStatementOfTruth;
    @CCD(
            label = "Claimant 1 Vulnerability Questions",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerCivilStaffJudgeProfileLegalAdviserRAccess.class, APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, RESSOLONESPECPROFILERESSOLONEUNSPECPROFILEWluAdminRAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilSystemupdateRAccess.class, CitizenProfileCuAccess.class}
    )
    private VulnerabilityQuestions applicant1DQVulnerabilityQuestions;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CITIZENCLAIMANTPROFILECruCaseworkerCivilStaffRAccess.class, APPSOLSPECPROFILERESSOLONESPECPROFILECruAccess.class}
    )
    private FutureApplications applicant1DQFutureApplications;
    @CCD(ignore = true)
    private WelshLanguageRequirements applicant1DQLanguageLRspec;
    @CCD(
            label = "Claimant 1 defendant documents to be considered",
            searchable = false,
            access = {CITIZENCLAIMANTPROFILECuAccess.class, CaseworkerCivilSystemFieldReaderRAccess.class}
    )
    private DocumentsToBeConsidered applicant1DQDefendantDocumentsToBeConsidered;
    @CCD(
            label = "Claimant 1 determination without a hearing",
            searchable = false,
            access = {APPSOLSPECPROFILEAPPSOLUNSPECPROFILECruAccess.class, RESSOLONESPECPROFILERESSOLONEUNSPECPROFILERAccess.class, CaseworkerCivilAdminRAccess.class}
    )
    private DeterWithoutHearing deterWithoutHearing;

    @CCD(ignore = true)
    private RemoteHearing remoteHearing;

    @Override
    @JsonProperty("deterWithoutHearing")
    public DeterWithoutHearing getDeterWithoutHearing() {
        return deterWithoutHearing;
    }

    @JsonProperty("applicant1DQRemoteHearing")
    public RemoteHearing getRemoteHearing() {
        return remoteHearing;
    }

    @Override
    @JsonProperty("applicant1DQFileDirectionsQuestionnaire")
    public FileDirectionsQuestionnaire getFileDirectionQuestionnaire() {
        return applicant1DQFileDirectionsQuestionnaire;
    }

    @Override
    @JsonProperty("applicant1DQFixedRecoverableCosts")
    public FixedRecoverableCosts getFixedRecoverableCosts() {
        return applicant1DQFixedRecoverableCosts;
    }

    @Override
    @JsonProperty("applicant1DQFixedRecoverableCostsIntermediate")
    public FixedRecoverableCosts getFixedRecoverableCostsIntermediate() {
        return applicant1DQFixedRecoverableCostsIntermediate;
    }

    @Override
    @JsonProperty("applicant1DQDisclosureOfElectronicDocuments")
    public DisclosureOfElectronicDocuments getDisclosureOfElectronicDocuments() {
        return applicant1DQDisclosureOfElectronicDocuments;
    }

    @Override
    @JsonProperty("specApplicant1DQDisclosureOfElectronicDocuments")
    public DisclosureOfElectronicDocuments getSpecDisclosureOfElectronicDocuments() {
        return specApplicant1DQDisclosureOfElectronicDocuments;
    }

    @Override
    @JsonProperty("applicant1DQDisclosureOfNonElectronicDocuments")
    public DisclosureOfNonElectronicDocuments getDisclosureOfNonElectronicDocuments() {
        return applicant1DQDisclosureOfNonElectronicDocuments;
    }

    @Override
    @JsonProperty("specApplicant1DQDisclosureOfNonElectronicDocuments")
    public DisclosureOfNonElectronicDocuments getSpecDisclosureOfNonElectronicDocuments() {
        return specApplicant1DQDisclosureOfNonElectronicDocuments;
    }

    @Override
    @JsonProperty("applicant1DQDisclosureReport")
    public DisclosureReport getDisclosureReport() {
        return applicant1DQDisclosureReport;
    }

    @Override
    @JsonProperty("applicant1DQExperts")
    public Experts getExperts() {
        return getExperts(applicant1DQExperts);
    }

    @JsonProperty("applicant1RespondToClaimExperts")
    public ExpertDetails getSmallClaimExperts() {
        return applicant1RespondToClaimExperts;
    }

    @Override
    @JsonProperty("applicant1DQWitnesses")
    public Witnesses getWitnesses() {
        return getWitnesses(applicant1DQWitnesses);
    }

    @Override
    @JsonProperty("applicant1DQHearing")
    public Hearing getHearing() {
        if (applicant1DQHearing != null) {
            return getHearing(applicant1DQHearing);
        }
        DQUtil util = new DQUtil();

        if (applicant1DQHearingLRspec != null) {
            return util.buildFastTrackHearing(applicant1DQHearingLRspec);
        }
        if (applicant1DQSmallClaimHearing != null) {
            SmallClaimHearing small = getSmallClaimHearing();
            return util.buildSmallClaimHearing(small);
        }

        return null;
    }

    @Override
    @JsonProperty("applicant1DQSmallClaimHearing")
    public SmallClaimHearing getSmallClaimHearing() {
        return getSmallClaimHearing(applicant1DQSmallClaimHearing);
    }

    @Override
    @JsonProperty("applicant1DQDraftDirections")
    public Document getDraftDirections() {
        return applicant1DQDraftDirections;
    }

    @Override
    @JsonProperty("applicant1DQRequestedCourt")
    public RequestedCourt getRequestedCourt() {

        if (applicant1DQRequestedCourt != null) {
            return new RequestedCourt()
                .setResponseCourtCode(applicant1DQRequestedCourt.getResponseCourtCode())
                .setReasonForHearingAtSpecificCourt(applicant1DQRequestedCourt.getReasonForHearingAtSpecificCourt())
                .setCaseLocation(applicant1DQRequestedCourt.getCaseLocation())
                .setResponseCourtLocations(applicant1DQRequestedCourt.getResponseCourtLocations());
        }
        return null;
    }

    @Override
    @JsonProperty("applicant1DQHearingSupport")
    public HearingSupport getHearingSupport() {
        return applicant1DQHearingSupport;
    }

    @Override
    @JsonProperty("applicant1DQFurtherInformation")
    public FurtherInformation getFurtherInformation() {
        return applicant1DQFurtherInformation;
    }

    @Override
    @JsonProperty("applicant1DQLanguage")
    public WelshLanguageRequirements getWelshLanguageRequirements() {
        return applicant1DQLanguage;
    }

    @Override
    @JsonProperty("applicant1DQRemoteHearingLRspec")
    public RemoteHearingLRspec getRemoteHearingLRspec() {
        return applicant1DQRemoteHearingLRspec;
    }

    @Override
    @JsonProperty("applicant1DQStatementOfTruth")
    public StatementOfTruth getStatementOfTruth() {
        return applicant1DQStatementOfTruth;
    }

    @Override
    @JsonProperty("applicant1DQVulnerabilityQuestions")
    public VulnerabilityQuestions getVulnerabilityQuestions() {
        return applicant1DQVulnerabilityQuestions;
    }

    @Override
    @JsonProperty("applicant1DQLanguageLRspec")
    public WelshLanguageRequirements getWelshLanguageRequirementsLRspec() {
        return applicant1DQLanguageLRspec;
    }

    @Override
    @JsonProperty("applicant1DQDefendantDocumentsToBeConsidered")
    public DocumentsToBeConsidered getDocumentsToBeConsidered() {
        return applicant1DQDefendantDocumentsToBeConsidered;
    }

    public Applicant1DQ copy() {
        return new Applicant1DQ()
            .setApplicant1DQFileDirectionsQuestionnaire(applicant1DQFileDirectionsQuestionnaire)
            .setApplicant1DQFixedRecoverableCosts(applicant1DQFixedRecoverableCosts)
            .setApplicant1DQFixedRecoverableCostsIntermediate(applicant1DQFixedRecoverableCostsIntermediate)
            .setApplicant1DQDisclosureOfElectronicDocuments(applicant1DQDisclosureOfElectronicDocuments)
            .setSpecApplicant1DQDisclosureOfElectronicDocuments(specApplicant1DQDisclosureOfElectronicDocuments)
            .setApplicant1DQDisclosureOfNonElectronicDocuments(applicant1DQDisclosureOfNonElectronicDocuments)
            .setSpecApplicant1DQDisclosureOfNonElectronicDocuments(specApplicant1DQDisclosureOfNonElectronicDocuments)
            .setApplicant1DQDisclosureReport(applicant1DQDisclosureReport)
            .setApplicant1DQExperts(applicant1DQExperts)
            .setApplicant1RespondToClaimExperts(applicant1RespondToClaimExperts)
            .setApplicant1DQWitnesses(applicant1DQWitnesses)
            .setApplicant1DQHearing(applicant1DQHearing)
            .setApplicant1DQHearingLRspec(applicant1DQHearingLRspec)
            .setApplicant1DQSmallClaimHearing(applicant1DQSmallClaimHearing)
            .setApplicant1DQDraftDirections(applicant1DQDraftDirections)
            .setApplicant1DQRequestedCourt(applicant1DQRequestedCourt)
            .setApplicant1DQHearingSupport(applicant1DQHearingSupport)
            .setApplicant1DQFurtherInformation(applicant1DQFurtherInformation)
            .setApplicant1DQLanguage(applicant1DQLanguage)
            .setApplicant1DQRemoteHearingLRspec(applicant1DQRemoteHearingLRspec)
            .setApplicant1DQStatementOfTruth(applicant1DQStatementOfTruth)
            .setApplicant1DQVulnerabilityQuestions(applicant1DQVulnerabilityQuestions)
            .setApplicant1DQFutureApplications(applicant1DQFutureApplications)
            .setApplicant1DQLanguageLRspec(applicant1DQLanguageLRspec)
            .setApplicant1DQDefendantDocumentsToBeConsidered(applicant1DQDefendantDocumentsToBeConsidered)
            .setDeterWithoutHearing(deterWithoutHearing)
            .setRemoteHearing(remoteHearing);
    }
}
