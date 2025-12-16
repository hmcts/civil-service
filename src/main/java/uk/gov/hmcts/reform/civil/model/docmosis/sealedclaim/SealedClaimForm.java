package uk.gov.hmcts.reform.civil.model.docmosis.sealedclaim;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class SealedClaimForm implements MappableObject {

    @JsonProperty("courtseal")
    @Builder.Default
    private String courtSeal = "[userImage:courtseal.PNG]"; //NOSONAR
    private List<Party> applicants;
    private List<Party> respondents;
    private String referenceNumber;
    private String ccdCaseReference;
    private String applicantExternalReference;
    private String respondent1ExternalReference;
    private String respondent2ExternalReference;
    private String caseName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate submittedOn;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate issueDate;
    private String claimDetails;
    private String statementOfValue;
    private String claimValue;
    private String legalRepCost;
    private String courtFee;
    private String hearingCourtLocation;
    private StatementOfTruth statementOfTruth;
    private String applicantRepresentativeOrganisationName;
    private String claimDeclarationDescription;
    private YesOrNo isHumanRightsActIssues;
    private YesOrNo isClaimDeclarationAdded;
}
