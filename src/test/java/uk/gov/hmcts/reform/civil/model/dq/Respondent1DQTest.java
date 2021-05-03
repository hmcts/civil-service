package uk.gov.hmcts.reform.civil.model.dq;

import org.junit.jupiter.api.Test;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

class Respondent1DQTest {

    @Test
    void shouldGetVariables_usingInterfaceMethods() {
        Respondent1DQ dq = buildRespondent1Dq();

        assertEquals(disclosureOfElectronicDocuments(), dq.getDisclosureOfElectronicDocuments());
        assertEquals(disclosureOfNonElectronicDocuments(), dq.getDisclosureOfNonElectronicDocuments());
        assertEquals(disclosureReport(), dq.getDisclosureReport());
        assertEquals(draftDirections(), dq.getDraftDirections());
        assertEquals(experts(), dq.getExperts());
        assertEquals(fileDirectionsQuestionnaire(), dq.getFileDirectionQuestionnaire());
        assertEquals(furtherInformation(), dq.getFurtherInformation());
        assertEquals(hearing(), dq.getHearing());
        assertEquals(hearingSupport(), dq.getHearingSupport());
        assertEquals(requestedCourt(), dq.getRequestedCourt());
        assertEquals(statementOfTruth(), dq.getStatementOfTruth());
        assertEquals(witnesses(), dq.getWitnesses());
        assertEquals(welshLanguageRequirements(), dq.getWelshLanguageRequirements());
    }

    private Respondent1DQ buildRespondent1Dq() {
        return Respondent1DQ.builder()
            .respondent1DQDisclosureOfElectronicDocuments(disclosureOfElectronicDocuments())
            .respondent1DQDisclosureOfNonElectronicDocuments(disclosureOfNonElectronicDocuments())
            .respondent1DQDisclosureReport(disclosureReport())
            .respondent1DQDraftDirections(draftDirections())
            .respondent1DQExperts(experts())
            .respondent1DQFileDirectionsQuestionnaire(fileDirectionsQuestionnaire())
            .respondent1DQFurtherInformation(furtherInformation())
            .respondent1DQHearing(hearing())
            .respondent1DQHearingSupport(hearingSupport())
            .respondent1DQRequestedCourt(requestedCourt())
            .respondent1DQStatementOfTruth(statementOfTruth())
            .respondent1DQWitnesses(witnesses())
            .respondent1DQLanguage(welshLanguageRequirements())
            .build();
    }

    private DisclosureOfNonElectronicDocuments disclosureOfNonElectronicDocuments() {
        return DisclosureOfNonElectronicDocuments.builder()
            .directionsForDisclosureProposed(YesOrNo.YES)
            .standardDirectionsRequired(YesOrNo.YES)
            .bespokeDirections("non electronic documents")
            .build();
    }

    private Witnesses witnesses() {
        return Witnesses.builder()
            .witnessesToAppear(YesOrNo.YES)
            .details(wrapElements(Witness.builder().name("John Smith").reasonForWitness("reason").build()))
            .build();
    }

    private StatementOfTruth statementOfTruth() {
        return StatementOfTruth.builder()
            .name("John Smith")
            .role("Solicitor")
            .build();
    }

    private RequestedCourt requestedCourt() {
        return RequestedCourt.builder()
            .responseCourtCode("343")
            .reasonForHearingAtSpecificCourt("reason for court")
            .requestHearingAtSpecificCourt(YesOrNo.YES)
            .build();
    }

    private HearingSupport hearingSupport() {
        return HearingSupport.builder()
            .requirements(List.of(SupportRequirements.values()))
            .languageToBeInterpreted("English")
            .signLanguageRequired("Spanish")
            .otherSupport("other support")
            .build();
    }

    private Hearing hearing() {
        return Hearing.builder()
            .hearingLength(HearingLength.LESS_THAN_DAY)
            .hearingLengthHours("1")
            .unavailableDatesRequired(YesOrNo.YES)
            .unavailableDates(wrapElements(UnavailableDate.builder().who("John Smith").date(LocalDate.now()).build()))
            .build();
    }

    private FurtherInformation furtherInformation() {
        return FurtherInformation.builder()
            .futureApplications(YesOrNo.YES)
            .otherInformationForJudge("Other information")
            .reasonForFutureApplications("Reason for future applications")
            .build();
    }

    private FileDirectionsQuestionnaire fileDirectionsQuestionnaire() {
        return FileDirectionsQuestionnaire.builder()
            .explainedToClient(List.of("yes"))
            .oneMonthStayRequested(YesOrNo.YES)
            .reactionProtocolCompliedWith(YesOrNo.NO)
            .reactionProtocolNotCompliedWithReason("Not complied with reason")
            .build();
    }

    private Experts experts() {
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

    private Document draftDirections() {
        return Document.builder()
            .documentBinaryUrl("binary url")
            .documentFileName("Order")
            .documentUrl("url")
            .build();
    }

    private DisclosureReport disclosureReport() {
        return DisclosureReport.builder()
            .disclosureFormFiledAndServed(YesOrNo.YES)
            .disclosureProposalAgreed(YesOrNo.YES)
            .draftOrderNumber("order number")
            .build();
    }

    private DisclosureOfElectronicDocuments disclosureOfElectronicDocuments() {
        return DisclosureOfElectronicDocuments.builder()
            .agreementLikely(YesOrNo.YES)
            .reachedAgreement(YesOrNo.NO)
            .reasonForNoAgreement("reason")
            .build();
    }

    private WelshLanguageRequirements welshLanguageRequirements() {
        return WelshLanguageRequirements.builder()
            .court(Language.WELSH)
            .documents(Language.WELSH)
            .evidence(Language.WELSH)
            .build();
    }
}
