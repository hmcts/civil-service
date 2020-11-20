package uk.gov.hmcts.reform.unspec.model.docmosis.cos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.unspec.model.SolicitorReferences;
import uk.gov.hmcts.reform.unspec.model.StatementOfTruth;
import uk.gov.hmcts.reform.unspec.model.docmosis.DocmosisData;
import uk.gov.hmcts.reform.unspec.model.docmosis.sealedclaim.Representative;

import java.time.LocalDate;

@Getter
@SuperBuilder
@AllArgsConstructor
@EqualsAndHashCode
public class CertificateOfServiceForm implements DocmosisData {

    @JsonProperty("courtseal")
    private final String courtSeal = "[userImage:courtseal.PNG]"; //NOSONAR
    private final String caseName;
    private final String referenceNumber;
    private final SolicitorReferences solicitorReferences;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate dateServed;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    @JsonSerialize(using = LocalDateSerializer.class)
    private final LocalDate deemedDateOfService;
    private final String applicantName;
    private final String respondentName;
    private final Representative respondentRepresentative;
    private final String serviceMethod;
    private final String servedLocation;
    private final String onWhomServed;
    private final String documentsServed;
    private final StatementOfTruth statementOfTruth;
    private final Representative applicantRepresentative;
}
