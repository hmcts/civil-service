package uk.gov.hmcts.reform.civil.model.dq;

import uk.gov.hmcts.reform.civil.enums.ExpertReportsSent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.HearingLength;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.enums.dq.SupportRequirements;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.UnavailableDateLRspec;
import uk.gov.hmcts.reform.civil.model.documents.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

abstract class DQTest {

    protected DisclosureOfNonElectronicDocuments disclosureOfNonElectronicDocuments() {
        return DisclosureOfNonElectronicDocuments.builder()
            .directionsForDisclosureProposed(YES)
            .standardDirectionsRequired(YES)
            .bespokeDirections("non electronic documents")
            .build();
    }

    protected Witnesses witnesses() {
        return Witnesses.builder()
            .witnessesToAppear(YES)
            .details(wrapElements(Witness.builder().name("John Smith").reasonForWitness("reason").build()))
            .build();
    }

    protected StatementOfTruth statementOfTruth() {
        return StatementOfTruth.builder()
            .name("John Smith")
            .role("Solicitor")
            .build();
    }

    protected RequestedCourt requestedCourt() {
        return RequestedCourt.builder()
            .responseCourtCode("343")
            .reasonForHearingAtSpecificCourt("reason for court")
            .requestHearingAtSpecificCourt(YES)
            .build();
    }

    protected HearingSupport hearingSupport() {
        return HearingSupport.builder()
            .requirements(List.of(SupportRequirements.values()))
            .languageToBeInterpreted("English")
            .signLanguageRequired("Spanish")
            .otherSupport("other support")
            .build();
    }

    protected Hearing hearing() {
        return Hearing.builder()
            .hearingLength(HearingLength.LESS_THAN_DAY)
            .hearingLengthHours("1")
            .unavailableDatesRequired(YES)
            .unavailableDates(wrapElements(UnavailableDate.builder().who("John Smith").date(LocalDate.now()).build()))
            .build();
    }

    protected HearingLRspec hearingLRspec() {
        return HearingLRspec.builder()
            .hearingLength(HearingLength.LESS_THAN_DAY)
            .hearingLengthHours("1")
            .unavailableDatesRequired(YES)
            .unavailableDatesLRspec(
                wrapElements(UnavailableDateLRspec.builder().who("John Smith").date(LocalDate.now()).build()))
            .build();
    }

    protected FurtherInformation furtherInformation() {
        return FurtherInformation.builder()
            .futureApplications(YES)
            .otherInformationForJudge("Other information")
            .reasonForFutureApplications("Reason for future applications")
            .build();
    }

    protected FileDirectionsQuestionnaire fileDirectionsQuestionnaire() {
        return FileDirectionsQuestionnaire.builder()
            .explainedToClient(List.of("yes"))
            .oneMonthStayRequested(YES)
            .reactionProtocolCompliedWith(YesOrNo.NO)
            .reactionProtocolNotCompliedWithReason("Not complied with reason")
            .build();
    }

    protected Experts experts() {
        return Experts.builder()
            .expertRequired(YES)
            .expertReportsSent(ExpertReportsSent.YES)
            .jointExpertSuitable(YES)
            .details(wrapElements(Expert.builder()
                                      .name("John Smith")
                                      .fieldOfExpertise("Science")
                                      .estimatedCost(BigDecimal.ONE)
                                      .whyRequired("Reason")
                                      .build()))
            .build();
    }

    protected Document draftDirections() {
        return Document.builder()
            .documentBinaryUrl("binary url")
            .documentFileName("Order")
            .documentUrl("url")
            .build();
    }

    protected DisclosureReport disclosureReport() {
        return DisclosureReport.builder()
            .disclosureFormFiledAndServed(YES)
            .disclosureProposalAgreed(YES)
            .draftOrderNumber("order number")
            .build();
    }

    protected DisclosureOfElectronicDocuments disclosureOfElectronicDocuments() {
        return DisclosureOfElectronicDocuments.builder()
            .agreementLikely(YES)
            .reachedAgreement(YesOrNo.NO)
            .reasonForNoAgreement("reason")
            .build();
    }

    protected WelshLanguageRequirements welshLanguageRequirements() {
        return WelshLanguageRequirements.builder()
            .court(Language.WELSH)
            .documents(Language.WELSH)
            .evidence(Language.WELSH)
            .build();
    }

    protected VulnerabilityQuestions vulnerabilityQuestions() {
        return VulnerabilityQuestions.builder()
            .vulnerabilityAdjustmentsRequired(YES)
            .vulnerabilityAdjustments("required adjustments")
            .build();
    }
}
