package uk.gov.hmcts.reform.civil.model.docmosis;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.EvidenceDetails;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.TimelineOfEventDetails;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ClaimResponseForm implements MappableObject {

    private final String referenceNumber;
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate issueDate;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private final LocalDateTime submittedOn;
    private final Party respondent;

    private final String defendantResponse;
    private final String whyDisputeTheClaim;
    private final List<TimelineOfEventDetails> defendantTimeline;
    private final String commentsAboutClaimantTimeline;
    private final List<EvidenceDetails> defendantEvidence;
    private final String commentsAboutClaimantEvidence;
    private final YesOrNo willingToTryMediation;
    private final StatementOfTruth statementOfTruth;
}
