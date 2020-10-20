package uk.gov.hmcts.reform.unspec.model.dq;

import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.unspec.model.StatementOfTruth;
import uk.gov.hmcts.reform.unspec.model.documents.Document;

@Setter
@Data
@Builder
public class Respondent1DQ implements DQ {

    private final FileDirectionsQuestionnaire respondent1DQFileDirectionsQuestionnaire;
    private final DisclosureOfElectronicDocuments respondent1DQDisclosureOfElectronicDocuments;
    private final String respondent1DQDisclosureOfNonElectronicDocuments;
    private final DisclosureReport respondent1DQDisclosureReport;
    private final Experts respondent1DQExperts;
    private final Witnesses respondent1DQWitnesses;
    private final Hearing respondent1DQHearing;
    private final Document respondent1DQDraftDirections;
    private final RequestedCourt respondent1DQRequestedCourt;
    private final HearingSupport respondent1DQHearingSupport;
    private final FurtherInformation respondent1DQFurtherInformation;
    private final StatementOfTruth respondent1DQStatementOfTruth;

    @Override
    public FileDirectionsQuestionnaire getFileDirectionQuestionnaire() {
        return respondent1DQFileDirectionsQuestionnaire;
    }

    @Override
    public DisclosureOfElectronicDocuments getDisclosureOfElectronicDocuments() {
        return respondent1DQDisclosureOfElectronicDocuments;
    }

    @Override
    public String getDisclosureOfNonElectronicDocuments() {
        return respondent1DQDisclosureOfNonElectronicDocuments;
    }

    @Override
    public DisclosureReport getDisclosureReport() {
        return respondent1DQDisclosureReport;
    }

    @Override
    public Experts getExperts() {
        return respondent1DQExperts;
    }

    @Override
    public Witnesses getWitnesses() {
        return respondent1DQWitnesses;
    }

    @Override
    public Hearing getHearing() {
        return respondent1DQHearing;
    }

    @Override
    public Document getDraftDirections() {
        return respondent1DQDraftDirections;
    }

    @Override
    public RequestedCourt getRequestedCourt() {
        return respondent1DQRequestedCourt;
    }

    @Override
    public HearingSupport getHearingSupport() {
        return respondent1DQHearingSupport;
    }

    @Override
    public FurtherInformation getFurtherInformation() {
        return respondent1DQFurtherInformation;
    }

    @Override
    public StatementOfTruth getStatementOfTruth() {
        return respondent1DQStatementOfTruth;
    }
}
