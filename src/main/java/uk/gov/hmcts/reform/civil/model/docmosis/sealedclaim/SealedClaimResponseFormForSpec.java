package uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.common.SpecifiedParty;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class SealedClaimResponseFormForSpec implements MappableObject {

    private final String referenceNumber;
    private final String ccdCaseReference;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate paymentDate;
    private final String paymentMethod;
    private final String hearingCourtLocation;
    private final StatementOfTruth statementOfTruth;
    //repayment details for repayment plan that are common between LR and LiP
    private final ResponseRepaymentDetailsForm commonDetails;
    // CARM defendant mediation fields
    private final String mediationFirstName;
    private final String mediationLastName;
    private final String mediationContactNumber;
    private final String mediationEmail;
    private final boolean mediationUnavailableDatesExists;
    private final List<Element<UnavailableDate>> mediationUnavailableDatesList;
    private final boolean checkCarmToggle;
    private final String allocatedTrack;
    private final RespondentResponseTypeSpec responseType;
    private final YesOrNo mediation;
}
