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

@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Respondent2DQ implements DQ {

    private FileDirectionsQuestionnaire respondent2DQFileDirectionsQuestionnaire;
    private FixedRecoverableCosts respondent2DQFixedRecoverableCosts;
    private FixedRecoverableCosts respondent2DQFixedRecoverableCostsIntermediate;
    private DisclosureOfElectronicDocuments respondent2DQDisclosureOfElectronicDocuments;
    private DisclosureOfElectronicDocuments specRespondent2DQDisclosureOfElectronicDocuments;
    private DisclosureOfNonElectronicDocuments respondent2DQDisclosureOfNonElectronicDocuments;
    private DisclosureOfNonElectronicDocuments specRespondent2DQDisclosureOfNonElectronicDocuments;
    private DisclosureReport respondent2DQDisclosureReport;
    private Experts respondent2DQExperts;
    private ExpertDetails respondToClaimExperts2;
    private Witnesses respondent2DQWitnesses;
    private Hearing respondent2DQHearing;
    private SmallClaimHearing respondent2DQHearingSmallClaim;
    private Document respondent2DQDraftDirections;
    private RequestedCourt respondent2DQRequestedCourt;

    private RemoteHearing respondent2DQRemoteHearing;
    private RemoteHearingLRspec respondent2DQRemoteHearingLRspec;
    private HearingSupport respondent2DQHearingSupport;
    private YesOrNo respondent2DQCarerAllowanceCredit;
    private FurtherInformation respondent2DQFurtherInformation;
    private WelshLanguageRequirements respondent2DQLanguage;
    private WelshLanguageRequirements respondent2DQLanguageLRspec;
    private StatementOfTruth respondent2DQStatementOfTruth;
    private VulnerabilityQuestions respondent2DQVulnerabilityQuestions;
    private List<Element<RecurringIncomeLRspec>> respondent2DQRecurringIncome;
    private List<Element<RecurringExpenseLRspec>> respondent2DQRecurringExpenses;
    private List<Element<AccountSimple>> respondent2BankAccountList;
    private HomeDetails respondent2DQHomeDetails;
    private FutureApplications respondent2DQFutureApplications;
    private Hearing respondent2DQHearingFastClaim;
    private RequestedCourt respondToCourtLocation2;
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
