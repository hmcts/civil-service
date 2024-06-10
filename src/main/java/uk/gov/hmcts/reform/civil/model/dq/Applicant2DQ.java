package uk.gov.hmcts.reform.civil.model.dq;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

@Setter
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Applicant2DQ implements DQ {

    private FileDirectionsQuestionnaire applicant2DQFileDirectionsQuestionnaire;
    private FixedRecoverableCosts applicant2DQFixedRecoverableCosts;
    private FixedRecoverableCosts applicant2DQFixedRecoverableCostsIntermediate;
    private DisclosureOfElectronicDocuments applicant2DQDisclosureOfElectronicDocuments;
    private DisclosureOfNonElectronicDocuments applicant2DQDisclosureOfNonElectronicDocuments;
    private DisclosureReport applicant2DQDisclosureReport;
    private Experts applicant2DQExperts;
    private ExpertDetails applicant2RespondToClaimExperts;
    private Witnesses applicant2DQWitnesses;
    private Hearing applicant2DQHearing;
    private SmallClaimHearing applicant2DQSmallClaimHearing;
    private Document applicant2DQDraftDirections;
    private RequestedCourt applicant2DQRequestedCourt;
    private HearingSupport applicant2DQHearingSupport;
    private FurtherInformation applicant2DQFurtherInformation;
    private WelshLanguageRequirements applicant2DQLanguage;
    private WelshLanguageRequirements applicant2DQLanguageLRspec;
    private RemoteHearingLRspec applicant2DQRemoteHearingLRspec;
    private StatementOfTruth applicant2DQStatementOfTruth;
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
}
