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
public class Applicant1DQ implements DQ {

    private FileDirectionsQuestionnaire applicant1DQFileDirectionsQuestionnaire;
    private FixedRecoverableCosts applicant1DQFixedRecoverableCosts;
    private DisclosureOfElectronicDocuments applicant1DQDisclosureOfElectronicDocuments;
    private DisclosureOfNonElectronicDocuments applicant1DQDisclosureOfNonElectronicDocuments;
    private DisclosureReport applicant1DQDisclosureReport;
    private Experts applicant1DQExperts;
    private ExpertDetails applicant1RespondToClaimExperts;
    private Witnesses applicant1DQWitnesses;
    private Hearing applicant1DQHearing;
    private Hearing applicant1DQHearingLRspec;
    private SmallClaimHearing applicant1DQSmallClaimHearing;
    private Document applicant1DQDraftDirections;
    private RequestedCourt applicant1DQRequestedCourt;
    private HearingSupport applicant1DQHearingSupport;
    private FurtherInformation applicant1DQFurtherInformation;
    private WelshLanguageRequirements applicant1DQLanguage;
    private RemoteHearingLRspec applicant1DQRemoteHearingLRspec;
    private StatementOfTruth applicant1DQStatementOfTruth;
    private VulnerabilityQuestions applicant1DQVulnerabilityQuestions;
    private FutureApplications applicant1DQFutureApplications;
    private WelshLanguageRequirements applicant1DQLanguageLRspec;

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
    @JsonProperty("applicant1DQDisclosureOfElectronicDocuments")
    public DisclosureOfElectronicDocuments getDisclosureOfElectronicDocuments() {
        return applicant1DQDisclosureOfElectronicDocuments;
    }

    @Override
    @JsonProperty("applicant1DQDisclosureOfNonElectronicDocuments")
    public DisclosureOfNonElectronicDocuments getDisclosureOfNonElectronicDocuments() {
        return applicant1DQDisclosureOfNonElectronicDocuments;
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
            return RequestedCourt.builder()
                .responseCourtCode(applicant1DQRequestedCourt.getResponseCourtCode())
                .reasonForHearingAtSpecificCourt(applicant1DQRequestedCourt.getReasonForHearingAtSpecificCourt())
                .caseLocation(applicant1DQRequestedCourt.getCaseLocation())
                .responseCourtLocations(applicant1DQRequestedCourt.getResponseCourtLocations())
                .build();
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
}
