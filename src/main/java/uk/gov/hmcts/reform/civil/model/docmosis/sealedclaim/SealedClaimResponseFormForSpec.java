package uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.common.EventTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.EvidenceTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.RepaymentPlanTemplateData;
import uk.gov.hmcts.reform.civil.model.docmosis.common.SpecifiedParty;

import java.time.LocalDate;
import java.util.List;

@Getter
@SuperBuilder
@EqualsAndHashCode
public class SealedClaimResponseFormForSpec extends SealedClaimResponseForm implements MappableObject {

    private final String referenceNumber;
    private final String caseName;
    private final SolicitorReferences solicitorReferences;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate submittedOn;
    private final SpecifiedParty respondent1;
    private final SpecifiedParty respondent2;
    private final String defendantResponse;
    private final String whyDisputeTheClaim;
    private final boolean timelineUploaded;
    private final String specResponseTimelineDocumentFiles;
    private final List<TimelineEventDetailsDocmosis> timeline;
    private final String respondent1SpecDefenceResponseDocument;
    private final String poundsPaid;
    private final String paymentMethod;
    private final String hearingCourtLocation;
    private final StatementOfTruth statementOfTruth;

    @Builder
    public SealedClaimResponseFormForSpec(
        String amountToPay,
        String howMuchWasPaid,
        LocalDate paymentDate,
        String paymentHow,
        LocalDate payBy,
        String whyNotPayImmediately,
        RepaymentPlanTemplateData repaymentPlan,
        RespondentResponseTypeSpec responseType,
        String whyReject,
        List<EventTemplateData> timelineEventList,
        String timelineComments,
        List<EvidenceTemplateData> evidenceList,
        String evidenceComments,
        boolean mediation,
        RespondentResponsePartAdmissionPaymentTimeLRspec howToPay,
        String referenceNumber,
        String caseName,
        SolicitorReferences solicitorReferences,
        LocalDate submittedOn,
        SpecifiedParty respondent1,
        SpecifiedParty respondent2,
        String defendantResponse,
        String whyDisputeTheClaim,
        boolean timelineUploaded,
        String specResponseTimelineDocumentFiles,
        List<TimelineEventDetailsDocmosis> timeline,
        String respondent1SpecDefenceResponseDocument,
        String poundsPaid,
        String paymentMethod,
        String hearingCourtLocation,
        StatementOfTruth statementOfTruth)
    {
        super(
            amountToPay,
            howMuchWasPaid,
            paymentDate,
            paymentHow,
            payBy,
            whyNotPayImmediately,
            repaymentPlan,
            responseType,
            whyReject,
            timelineEventList,
            timelineComments,
            evidenceList,
            evidenceComments,
            mediation,
            howToPay
        );
        this.referenceNumber = referenceNumber;
        this.caseName = caseName;
        this.solicitorReferences = solicitorReferences;
        this.submittedOn = submittedOn;
        this.respondent1 = respondent1;
        this.respondent2 = respondent2;
        this.defendantResponse = defendantResponse;
        this.whyDisputeTheClaim = whyDisputeTheClaim;
        this.timelineUploaded = timelineUploaded;
        this.specResponseTimelineDocumentFiles = specResponseTimelineDocumentFiles;
        this.timeline = timeline;
        this.respondent1SpecDefenceResponseDocument = respondent1SpecDefenceResponseDocument;
        this.poundsPaid = poundsPaid;
        this.paymentMethod = paymentMethod;
        this.hearingCourtLocation = hearingCourtLocation;
        this.statementOfTruth = statementOfTruth;
    }
}
