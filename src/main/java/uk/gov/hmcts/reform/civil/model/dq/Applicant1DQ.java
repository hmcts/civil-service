package uk.gov.hmcts.reform.civil.model.dq;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.UnavailableDateLRspec;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.Document;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Data
@Builder(toBuilder = true)
public class Applicant1DQ implements DQ {

    private final FileDirectionsQuestionnaire applicant1DQFileDirectionsQuestionnaire;
    private final DisclosureOfElectronicDocuments applicant1DQDisclosureOfElectronicDocuments;
    private final DisclosureOfNonElectronicDocuments applicant1DQDisclosureOfNonElectronicDocuments;
    private final DisclosureReport applicant1DQDisclosureReport;
    private final Experts applicant1DQExperts;
    private final Witnesses applicant1DQWitnesses;
    private final Hearing applicant1DQHearing;
    private final HearingLRspec applicant1DQHearingLRspec;
    private final SmallClaimHearing applicant1DQSmallClaimHearing;
    private final Document applicant1DQDraftDirections;
    private final RequestedCourt applicant1DQRequestedCourt;
    private final HearingSupport applicant1DQHearingSupport;
    private final FurtherInformation applicant1DQFurtherInformation;
    private final WelshLanguageRequirements applicant1DQLanguage;
    private final StatementOfTruth applicant1DQStatementOfTruth;
    private final VulnerabilityQuestions applicant1DQVulnerabilityQuestions;
    private final FutureApplications applicant1DQFutureApplications;
    private final WelshLanguageRequirements applicant1DQLanguageLRspec;

    @Override
    @JsonProperty("applicant1DQFileDirectionsQuestionnaire")
    public FileDirectionsQuestionnaire getFileDirectionQuestionnaire() {
        return applicant1DQFileDirectionsQuestionnaire;
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
        if (applicant1DQHearingLRspec != null) {
            return Hearing.builder()
                .hearingLength(applicant1DQHearingLRspec.getHearingLength())
                .hearingLengthDays(applicant1DQHearingLRspec.getHearingLengthDays())
                .hearingLengthHours(applicant1DQHearingLRspec.getHearingLengthHours())
                .unavailableDatesRequired(applicant1DQHearingLRspec.getUnavailableDatesRequired())
                .unavailableDates(mapDates(applicant1DQHearingLRspec.getUnavailableDatesLRspec()))
                .build();
        }
        if (applicant1DQSmallClaimHearing != null) {
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
                .requestHearingAtSpecificCourt(applicant1DQRequestedCourt.getRequestHearingAtSpecificCourt())
                .responseCourtCode(applicant1DQRequestedCourt.getResponseCourtCode())
                .reasonForHearingAtSpecificCourt(applicant1DQRequestedCourt.getReasonForHearingAtSpecificCourt())
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
