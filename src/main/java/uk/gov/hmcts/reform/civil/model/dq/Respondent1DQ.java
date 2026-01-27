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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Respondent1DQ implements DQ {

    private FileDirectionsQuestionnaire respondent1DQFileDirectionsQuestionnaire;
    private FixedRecoverableCosts respondent1DQFixedRecoverableCosts;
    private FixedRecoverableCosts respondent1DQFixedRecoverableCostsIntermediate;
    private DisclosureOfElectronicDocuments respondent1DQDisclosureOfElectronicDocuments;
    private DisclosureOfElectronicDocuments specRespondent1DQDisclosureOfElectronicDocuments;
    private DisclosureOfNonElectronicDocuments respondent1DQDisclosureOfNonElectronicDocuments;
    private DisclosureOfNonElectronicDocuments specRespondent1DQDisclosureOfNonElectronicDocuments;
    private DisclosureReport respondent1DQDisclosureReport;
    private Experts respondent1DQExperts;
    private ExpertDetails respondToClaimExperts;
    private Witnesses respondent1DQWitnesses;
    private Hearing respondent1DQHearing;
    private SmallClaimHearing respondent1DQHearingSmallClaim;
    private Hearing respondent1DQHearingFastClaim;
    private Document respondent1DQDraftDirections;
    private RequestedCourt respondent1DQRequestedCourt;
    private RemoteHearing respondent1DQRemoteHearing;
    private RemoteHearingLRspec respondent1DQRemoteHearingLRspec;
    private HearingSupport respondent1DQHearingSupport;
    private FurtherInformation respondent1DQFurtherInformation;
    private WelshLanguageRequirements respondent1DQLanguage;
    private WelshLanguageRequirements respondent1DQLanguageLRspec;
    private StatementOfTruth respondent1DQStatementOfTruth;
    private FutureApplications respondent1DQFutureApplications;
    private List<Element<AccountSimple>> respondent1BankAccountList;
    private HomeDetails respondent1DQHomeDetails;
    private YesOrNo respondent1DQCarerAllowanceCredit;
    private YesOrNo respondent1DQCarerAllowanceCreditFullAdmission;
    private List<Element<RecurringIncomeLRspec>> respondent1DQRecurringIncome;
    private List<Element<RecurringIncomeLRspec>> respondent1DQRecurringIncomeFA;
    private List<Element<RecurringExpenseLRspec>> respondent1DQRecurringExpenses;
    private List<Element<RecurringExpenseLRspec>> respondent1DQRecurringExpensesFA;
    private YesOrNo responseClaimCourtLocationRequired;
    private RequestedCourt respondToCourtLocation;
    private VulnerabilityQuestions respondent1DQVulnerabilityQuestions;
    private DocumentsToBeConsidered respondent1DQClaimantDocumentsToBeConsidered;
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
