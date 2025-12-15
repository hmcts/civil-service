package uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
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

@Accessors(chain = true)
@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class SealedClaimResponseFormForSpec implements MappableObject {

    private String referenceNumber;
    private String ccdCaseReference;
    private String caseName;
    private SolicitorReferences solicitorReferences;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate submittedOn;
    private SpecifiedParty respondent1;
    private SpecifiedParty respondent2;
    private String defendantResponse;
    private String whyDisputeTheClaim;
    private boolean timelineUploaded;
    private String specResponseTimelineDocumentFiles;
    private List<TimelineEventDetailsDocmosis> timeline;
    private String respondent1SpecDefenceResponseDocument;
    private String poundsPaid;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate paymentDate;
    private String paymentMethod;
    private String hearingCourtLocation;
    private StatementOfTruth statementOfTruth;
    //repayment details for repayment plan that are common between LR and LiP
    private ResponseRepaymentDetailsForm commonDetails;
    // CARM defendant mediation fields
    private String mediationFirstName;
    private String mediationLastName;
    private String mediationContactNumber;
    private String mediationEmail;
    private boolean mediationUnavailableDatesExists;
    private List<Element<UnavailableDate>> mediationUnavailableDatesList;
    private boolean checkCarmToggle;
    private String allocatedTrack;
    private RespondentResponseTypeSpec responseType;
    private YesOrNo mediation;
    private String respondentRepresentativeOrganisationName;
}
