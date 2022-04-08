package uk.gov.hmcts.reform.civil.model.dq;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.UnavailableDateLRspec;
import uk.gov.hmcts.reform.civil.model.account.AccountSimple;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Setter
@Data
@Builder(toBuilder = true)
public class Respondent1DQ implements DQ {

    private final FileDirectionsQuestionnaire respondent1DQFileDirectionsQuestionnaire;
    private final DisclosureOfElectronicDocuments respondent1DQDisclosureOfElectronicDocuments;
    private final DisclosureOfNonElectronicDocuments respondent1DQDisclosureOfNonElectronicDocuments;
    private final DisclosureReport respondent1DQDisclosureReport;
    private final Experts respondent1DQExperts;
    private final Witnesses respondent1DQWitnesses;
    private final Hearing respondent1DQHearing;
    private final SmallClaimHearing respondent1DQHearingSmallClaim;
    private final HearingLRspec respondent1DQHearingFastClaim;
    private final Document respondent1DQDraftDirections;
    private final RequestedCourt respondent1DQRequestedCourt;
    private final HearingSupport respondent1DQHearingSupport;
    private final FurtherInformation respondent1DQFurtherInformation;
    private final WelshLanguageRequirements respondent1DQLanguage;
    private final StatementOfTruth respondent1DQStatementOfTruth;
    private final FutureApplications respondent1DQFutureApplications;
    private final List<Element<AccountSimple>> respondent1BankAccountList;
    private final HomeDetails respondent1DQHomeDetails;
    private final YesOrNo respondent1DQCarerAllowanceCredit;
    private final List<Element<RecurringIncomeLRspec>> respondent1DQRecurringIncome;
    private final List<Element<RecurringExpenseLRspec>> respondent1DQRecurringExpenses;
    private final YesOrNo responseClaimCourtLocationRequired;
    private final RequestedCourt respondToCourtLocation;
    private final VulnerabilityQuestions respondent1DQVulnerabilityQuestions;

    @Override
    @JsonProperty("respondent1DQFileDirectionsQuestionnaire")
    public FileDirectionsQuestionnaire getFileDirectionQuestionnaire() {
        return respondent1DQFileDirectionsQuestionnaire;
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
        if (respondent1DQHearingFastClaim != null) {
            return Hearing.builder()
                .hearingLength(respondent1DQHearingFastClaim.getHearingLength())
                .hearingLengthDays(respondent1DQHearingFastClaim.getHearingLengthDays())
                .hearingLengthHours(respondent1DQHearingFastClaim.getHearingLengthHours())
                .unavailableDatesRequired(respondent1DQHearingFastClaim.getUnavailableDatesRequired())
                .unavailableDates(mapDates(respondent1DQHearingFastClaim.getUnavailableDatesLRspec()))
                .build();
        }
        if (respondent1DQHearingSmallClaim != null) {
            SmallClaimHearing small = getSmallClaimHearing();
            return Hearing.builder()
                .unavailableDatesRequired(small.getUnavailableDatesRequired())
                .unavailableDates(mapDates(small.getSmallClaimUnavailableDate()))
                .build();
        }

        return null;
    }

    private List<Element<UnavailableDate>> mapDates(List<Element<UnavailableDateLRspec>> lrDates) {
        if (lrDates == null) {
            return Collections.emptyList();
        } else {
            return lrDates.stream().map(Element::getValue)
                .map(this::mapDate)
                .map(ElementUtils::element)
                .collect(Collectors.toList());
        }
    }

    private UnavailableDate mapDate(UnavailableDateLRspec lrSpec) {
        UnavailableDate.UnavailableDateBuilder builder = UnavailableDate.builder()
            .who(lrSpec.getWho());
        if (lrSpec.getDate() != null) {
            builder.date(lrSpec.getDate());
        } else {
            builder.fromDate(lrSpec.getFromDate()).toDate(lrSpec.getToDate());
        }
        return builder.build();
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

            YesOrNo requestHearingAtSpecificCourt = Stream.of(
                optRespondentDQ.map(RequestedCourt::getRequestHearingAtSpecificCourt),
                Optional.ofNullable(responseClaimCourtLocationRequired),
                optRespond.map(RequestedCourt::getRequestHearingAtSpecificCourt)
            ).filter(Optional::isPresent).findFirst().map(Optional::get).orElse(YesOrNo.NO);

            String responseCourtCode = Stream.of(
                optRespondentDQ.map(RequestedCourt::getResponseCourtCode),
                optRespond.map(RequestedCourt::getResponseCourtCode)
            ).filter(Optional::isPresent).findFirst().map(Optional::get).orElse(null);

            String reasonForHearingAtSpecificCourt = Stream.of(
                optRespondentDQ.map(RequestedCourt::getReasonForHearingAtSpecificCourt),
                optRespond.map(RequestedCourt::getReasonForHearingAtSpecificCourt)
            ).filter(Optional::isPresent).findFirst().map(Optional::get).orElse(null);

            return RequestedCourt.builder()
                .requestHearingAtSpecificCourt(requestHearingAtSpecificCourt)
                .responseCourtCode(responseCourtCode)
                .reasonForHearingAtSpecificCourt(reasonForHearingAtSpecificCourt)
                .build();
        }
        return respondent1DQRequestedCourt;
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
    @JsonProperty("respondent1DQVulnerabilityQuestions")
    public VulnerabilityQuestions getVulnerabilityQuestions() {
        return respondent1DQVulnerabilityQuestions;
    }
}
