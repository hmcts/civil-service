package uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class SealedClaimLipResponseForm implements MappableObject {
    private final String referenceNumber;
    private final String caseName;
    private final SolicitorReferences solicitorReferences;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate submittedOn;
    private final Party respondent1;
    private final String defendantResponse;
    private final String whyDisputeTheClaim;
    private final boolean timelineUploaded;
    private final String specResponseTimelineDocumentFiles;
    private final List<TimelineEventDetailsDocmosis> timeline;
    private final String respondent1SpecDefenceResponseDocument;
    private final String poundsPaid;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate paymentDate;
    private final String paymentMethod;
    private final StatementOfTruth statementOfTruth;
}
