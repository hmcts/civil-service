package uk.gov.hmcts.reform.civil.model.dq;

import uk.gov.hmcts.reform.civil.enums.ExpertReportsSent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.HearingLength;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.enums.dq.SupportRequirements;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.documents.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

abstract class DQTest {

    protected DisclosureOfNonElectronicDocuments disclosureOfNonElectronicDocuments() {
        return DisclosureOfNonElectronicDocuments.builder()
            .directionsForDisclosureProposed(YesOrNo.YES)
            .standardDirectionsRequired(YesOrNo.YES)
            .bespokeDirections("non electronic documents")
            .build();
    }

    protected Witnesses witnesses() {
        return Witnesses.builder()
            .witnessesToAppear(YesOrNo.YES)
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
            .requestHearingAtSpecificCourt(YesOrNo.YES)
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
            .unavailableDatesRequired(YesOrNo.YES)
            .unavailableDates(wrapElements(UnavailableDate.builder().who("John Smith").date(LocalDate.now()).build()))
            .build();
    }

    protected FurtherInformation furtherInformation() {
        return FurtherInformation.builder()
            .futureApplications(YesOrNo.YES)
            .otherInformationForJudge("Other information")
            .reasonForFutureApplications("Reason for future applications")
            .build();
    }

    protected FileDirectionsQuestionnaire fileDirectionsQuestionnaire() {
        return FileDirectionsQuestionnaire.builder()
            .explainedToClient(List.of("yes"))
            .oneMonthStayRequested(YesOrNo.YES)
            .reactionProtocolCompliedWith(YesOrNo.NO)
            .reactionProtocolNotCompliedWithReason("Not complied with reason")
            .build();
    }

    protected Experts experts() {
        return Experts.builder()
            .expertRequired(YesOrNo.YES)
            .expertReportsSent(ExpertReportsSent.YES)
            .jointExpertSuitable(YesOrNo.YES)
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
            .disclosureFormFiledAndServed(YesOrNo.YES)
            .disclosureProposalAgreed(YesOrNo.YES)
            .draftOrderNumber("order number")
            .build();
    }

    protected DisclosureOfElectronicDocuments disclosureOfElectronicDocuments() {
        return DisclosureOfElectronicDocuments.builder()
            .agreementLikely(YesOrNo.YES)
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
}
