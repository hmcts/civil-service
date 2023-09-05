package uk.gov.hmcts.reform.civil.model.dq;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.account.AccountSimple;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Setter
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Respondent1DQ implements DQ {

    private FileDirectionsQuestionnaire respondent1DQFileDirectionsQuestionnaire;
    private FixedRecoverableCosts respondent1DQFixedRecoverableCosts;
    private DisclosureOfElectronicDocuments respondent1DQDisclosureOfElectronicDocuments;
    private DisclosureOfNonElectronicDocuments respondent1DQDisclosureOfNonElectronicDocuments;
    private DisclosureReport respondent1DQDisclosureReport;
    private Experts respondent1DQExperts;
    private ExpertDetails respondToClaimExperts;
    private Witnesses respondent1DQWitnesses;
    private Hearing respondent1DQHearing;
    private SmallClaimHearing respondent1DQHearingSmallClaim;
    private Hearing respondent1DQHearingFastClaim;
    private Document respondent1DQDraftDirections;
    private RequestedCourt respondent1DQRequestedCourt;
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
    @JsonProperty("respondent1DQDisclosureOfElectronicDocuments")
    public DisclosureOfElectronicDocuments getDisclosureOfElectronicDocuments() {
        return respondent1DQDisclosureOfElectronicDocuments;
    }

    @Override
    @JsonProperty("respondent1DQDisclosureOfNonElectronicDocuments")
    public DisclosureOfNonElectronicDocuments getDisclosureOfNonElectronicDocuments() {
        return respondent1DQDisclosureOfNonElectronicDocuments;
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

            RequestedCourt.RequestedCourtBuilder copyBuilder = RequestedCourt.builder()
                .responseCourtCode(responseCourtCode)
                .reasonForHearingAtSpecificCourt(reasonForHearingAtSpecificCourt);

            Stream.of(
                optRespondentDQ.map(RequestedCourt::getCaseLocation),
                optRespond.map(RequestedCourt::getCaseLocation)
            ).filter(Optional::isPresent).findFirst().map(Optional::get).ifPresent(copyBuilder::caseLocation);

            return copyBuilder
                .build();
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
}
