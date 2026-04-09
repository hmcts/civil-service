package uk.gov.hmcts.reform.civil.model.dq;

import uk.gov.hmcts.reform.civil.enums.ExpertReportsSent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.HearingLength;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.enums.dq.SupportRequirements;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

abstract class DQTest {

    protected DisclosureOfNonElectronicDocuments disclosureOfNonElectronicDocuments() {
        return new DisclosureOfNonElectronicDocuments()
            .setDirectionsForDisclosureProposed(YES)
            .setStandardDirectionsRequired(YES)
            .setBespokeDirections("non electronic documents");
    }

    protected Witnesses witnesses() {
        return new Witnesses()
            .setWitnessesToAppear(YES)
            .setDetails(wrapElements(new Witness().setName("John Smith").setReasonForWitness("reason")));
    }

    protected StatementOfTruth statementOfTruth() {
        return new StatementOfTruth()
            .setName("John Smith")
            .setRole("Solicitor");
    }

    protected RequestedCourt requestedCourt() {
        return new RequestedCourt()
            .setResponseCourtCode("343")
            .setReasonForHearingAtSpecificCourt("reason for court");
    }

    protected HearingSupport hearingSupport() {
        return new HearingSupport()
            .setRequirements(List.of(SupportRequirements.values()))
            .setLanguageToBeInterpreted("English")
            .setSignLanguageRequired("Spanish")
            .setOtherSupport("other support")
            .setSupportRequirements(YES)
            .setSupportRequirementsAdditional("additional support");
    }

    protected Hearing hearing() {
        UnavailableDate unavailableDate = new UnavailableDate();
        unavailableDate.setWho("John Smith");
        unavailableDate.setDate(LocalDate.now());
        return new Hearing()
            .setHearingLength(HearingLength.LESS_THAN_DAY)
            .setHearingLengthHours("1")
            .setUnavailableDatesRequired(YES)
            .setUnavailableDates(wrapElements(unavailableDate));
    }

    protected Hearing hearingLRspec() {
        UnavailableDate unavailableDate = new UnavailableDate();
        unavailableDate.setWho("John Smith");
        unavailableDate.setDate(LocalDate.now());
        return new Hearing()
            .setHearingLength(HearingLength.LESS_THAN_DAY)
            .setHearingLengthHours("1")
            .setUnavailableDatesRequired(YES)
            .setUnavailableDates(wrapElements(unavailableDate));
    }

    protected FurtherInformation furtherInformation() {
        return new FurtherInformation()
            .setFutureApplications(YES)
            .setOtherInformationForJudge("Other information")
            .setReasonForFutureApplications("Reason for future applications");
    }

    protected FileDirectionsQuestionnaire fileDirectionsQuestionnaire() {
        return new FileDirectionsQuestionnaire()
            .setExplainedToClient(List.of("yes"))
            .setOneMonthStayRequested(YES)
            .setReactionProtocolCompliedWith(YesOrNo.NO)
            .setReactionProtocolNotCompliedWithReason("Not complied with reason");
    }

    protected Experts experts() {
        return new Experts()
            .setExpertRequired(YES)
            .setExpertReportsSent(ExpertReportsSent.YES)
            .setJointExpertSuitable(YES)
            .setDetails(wrapElements(new Expert()
                                         .setName("John Smith")
                                         .setFieldOfExpertise("Science")
                                         .setEstimatedCost(BigDecimal.ONE)
                                         .setWhyRequired("Reason")));
    }

    protected Document draftDirections() {
        return new Document()
            .setDocumentBinaryUrl("binary url")
            .setDocumentFileName("Order")
            .setDocumentUrl("url");
    }

    protected DisclosureReport disclosureReport() {
        return new DisclosureReport()
            .setDisclosureFormFiledAndServed(YES)
            .setDisclosureProposalAgreed(YES)
            .setDraftOrderNumber("order number");
    }

    protected DisclosureOfElectronicDocuments disclosureOfElectronicDocuments() {
        return new DisclosureOfElectronicDocuments()
            .setAgreementLikely(YES)
            .setReachedAgreement(YesOrNo.NO)
            .setReasonForNoAgreement("reason");
    }

    protected WelshLanguageRequirements welshLanguageRequirements() {
        return new WelshLanguageRequirements()
            .setCourt(Language.WELSH)
            .setDocuments(Language.WELSH)
            .setEvidence(Language.WELSH);
    }

    protected VulnerabilityQuestions vulnerabilityQuestions() {
        return new VulnerabilityQuestions()
            .setVulnerabilityAdjustmentsRequired(YES)
            .setVulnerabilityAdjustments("required adjustments");
    }
}
