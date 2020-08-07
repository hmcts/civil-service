package uk.gov.hmcts.reform.unspec.model.docmosis.sealedclaim;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.unspec.model.StatementOfTruth;
import uk.gov.hmcts.reform.unspec.model.docmosis.DocmosisData;

import java.time.LocalDate;
import java.util.List;

@Getter
@SuperBuilder
@AllArgsConstructor
@EqualsAndHashCode
public class SealedClaimForm implements DocmosisData {

    @JsonProperty("courtseal")
    private final String courtSeal = "[userImage:courtseal.PNG]";
    private final List<Claimant> claimants;
    private final Representative claimantRepresentative;
    private final List<Defendant> defendants;
    private final String referenceNumber;
    private final String claimantExternalReference;
    private final String defendantExternalReference;
    private final String caseName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate submittedOn;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate issueDate;
    private final String claimDetails;
    private final String statementOfValue;
    private final String claimValue;
    private final String legalRepCost;
    private final String courtFee;
    private final String hearingCourtLocation;
    private final StatementOfTruth statementOfTruth;
}
