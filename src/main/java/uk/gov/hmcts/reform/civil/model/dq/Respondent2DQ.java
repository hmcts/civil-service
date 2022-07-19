package uk.gov.hmcts.reform.civil.model.dq;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.account.AccountSimple;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.Document;

import java.util.List;

@Setter
@Data
@Builder(toBuilder = true)
public class Respondent2DQ implements DQ {

    private final FileDirectionsQuestionnaire respondent2DQFileDirectionsQuestionnaire;
    private final DisclosureOfElectronicDocuments respondent2DQDisclosureOfElectronicDocuments;
    private final DisclosureOfNonElectronicDocuments respondent2DQDisclosureOfNonElectronicDocuments;
    private final DisclosureReport respondent2DQDisclosureReport;
    private final Experts respondent2DQExperts;
    private final ExpertDetails respondToClaimExperts2;
    private final Witnesses respondent2DQWitnesses;
    private final Hearing respondent2DQHearing;
    private final SmallClaimHearing respondent2DQHearingSmallClaim;
    private final Document respondent2DQDraftDirections;
    private final RequestedCourt respondent2DQRequestedCourt;
    private final HearingSupport respondent2DQHearingSupport;
    private final YesOrNo respondent2DQCarerAllowanceCredit;
    private final FurtherInformation respondent2DQFurtherInformation;
    private final WelshLanguageRequirements respondent2DQLanguage;
    private final WelshLanguageRequirements respondent2DQLanguageLRspec;
    private final StatementOfTruth respondent2DQStatementOfTruth;
    private final VulnerabilityQuestions respondent2DQVulnerabilityQuestions;
    private final List<Element<RecurringIncomeLRspec>> respondent2DQRecurringIncome;
    private final List<Element<RecurringExpenseLRspec>> respondent2DQRecurringExpenses;
    private final List<Element<AccountSimple>> respondent2BankAccountList;
    private final HomeDetails respondent2DQHomeDetails;
    private final FutureApplications respondent2DQFutureApplications;
    private final HearingLRspec respondent2DQHearingFastClaim;
    private final RequestedCourt respondToCourtLocation2;

    @Override
    @JsonProperty("respondent2DQFileDirectionsQuestionnaire")
    public FileDirectionsQuestionnaire getFileDirectionQuestionnaire() {
        return respondent2DQFileDirectionsQuestionnaire;
    }

    @Override
    @JsonProperty("respondent2DQDisclosureOfElectronicDocuments")
    public DisclosureOfElectronicDocuments getDisclosureOfElectronicDocuments() {
        return respondent2DQDisclosureOfElectronicDocuments;
    }

    @Override
    @JsonProperty("respondent2DQDisclosureOfNonElectronicDocuments")
    public DisclosureOfNonElectronicDocuments getDisclosureOfNonElectronicDocuments() {
        return respondent2DQDisclosureOfNonElectronicDocuments;
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
        return getHearing(respondent2DQHearing);
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
    @JsonProperty("respondent2DQLanguageLRspec")
    public WelshLanguageRequirements getWelshLanguageRequirementsLRspec() {
        return respondent2DQLanguageLRspec;
    }
}
