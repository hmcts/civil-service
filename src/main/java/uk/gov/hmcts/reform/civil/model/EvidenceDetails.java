package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Objects;
import java.util.stream.Stream;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class EvidenceDetails {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String evidenceType;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String photoEvidence;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String contractAndAgreementsEvidence;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String expertWitnessEvidence;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String lettersEmailsAndOtherCorrespondenceEvidence;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String receiptsEvidence;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String statementOfTruthEvidence;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String otherEvidence;

    @JsonCreator
    public EvidenceDetails(@JsonProperty("evidenceType") String evidenceType,
                           @JsonProperty("photoEvidence") String photoEvidence,
                           @JsonProperty("contractAndAgreementsEvidence") String contractAndAgreementsEvidence,
                           @JsonProperty("expertWitnessEvidence") String expertWitnessEvidence,
                           @JsonProperty("lettersEmailsAndOtherCorrespondenceEvidence")
                           String lettersEmailsAndOtherCorrespondenceEvidence,
                           @JsonProperty("receiptsEvidence") String receiptsEvidence,
                           @JsonProperty("statementOfTruthEvidence") String statementOfTruthEvidence,
                           @JsonProperty("otherEvidence") String otherEvidence) {
        this.evidenceType = evidenceType;
        this.photoEvidence = photoEvidence;
        this.contractAndAgreementsEvidence = contractAndAgreementsEvidence;
        this.expertWitnessEvidence = expertWitnessEvidence;
        this.lettersEmailsAndOtherCorrespondenceEvidence = lettersEmailsAndOtherCorrespondenceEvidence;
        this.receiptsEvidence = receiptsEvidence;
        this.statementOfTruthEvidence = statementOfTruthEvidence;
        this.otherEvidence = otherEvidence;
    }

    @JsonIgnore
    public String getEvidenceDescription() {
        return Stream.of(
                photoEvidence,
                contractAndAgreementsEvidence,
                expertWitnessEvidence,
                lettersEmailsAndOtherCorrespondenceEvidence,
                receiptsEvidence,
                statementOfTruthEvidence,
                otherEvidence
            )
            .filter(Objects::nonNull).findFirst().orElse("");
    }

}
