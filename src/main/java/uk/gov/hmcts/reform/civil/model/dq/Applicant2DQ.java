package uk.gov.hmcts.reform.civil.model.dq;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.documents.Document;
@Setter
@Data
@Builder(toBuilder = true)
public class Applicant2DQ implements DQ {

    private final FileDirectionsQuestionnaire applicant2DQFileDirectionsQuestionnaire;
    private final DisclosureOfElectronicDocuments applicant2DQDisclosureOfElectronicDocuments;
    private final DisclosureOfNonElectronicDocuments applicant2DQDisclosureOfNonElectronicDocuments;
    private final DisclosureReport applicant2DQDisclosureReport;
    private final Experts applicant2DQExperts;
    private final Witnesses applicant2DQWitnesses;
    private final Hearing applicant2DQHearing;
    private final SmallClaimHearing applicant2DQSmallClaimHearing;
    private final Document applicant2DQDraftDirections;
    private final RequestedCourt applicant2DQRequestedCourt;
    private final HearingSupport applicant2DQHearingSupport;
    private final FurtherInformation applicant2DQFurtherInformation;
    private final WelshLanguageRequirements applicant2DQLanguage;
    private final StatementOfTruth applicant2DQStatementOfTruth;

    @Override
    @JsonProperty("applicant2DQFileDirectionsQuestionnaire")
    public FileDirectionsQuestionnaire getFileDirectionQuestionnaire() {
        return applicant2DQFileDirectionsQuestionnaire;
    }

    @Override
    @JsonProperty("applicant2DQDisclosureOfElectronicDocuments")
    public DisclosureOfElectronicDocuments getDisclosureOfElectronicDocuments() {
        return applicant2DQDisclosureOfElectronicDocuments;
    }

    @Override
    @JsonProperty("applicant2DQDisclosureOfNonElectronicDocuments")
    public DisclosureOfNonElectronicDocuments getDisclosureOfNonElectronicDocuments() {
        return applicant2DQDisclosureOfNonElectronicDocuments;
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
    @JsonProperty("applicant2DQStatementOfTruth")
    public StatementOfTruth getStatementOfTruth() {
        return applicant2DQStatementOfTruth;
    }
}
