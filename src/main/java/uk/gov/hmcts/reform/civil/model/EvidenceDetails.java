package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EvidenceDetails {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final String evidenceType;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final String photoEvidence;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final String contractAndAgreementsEvidence;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final String expertWitnessEvidence;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final String lettersEmailsAndOtherCorrespondenceEvidence;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final String receiptsEvidence;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final String statementOfTruthEvidence;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final String otherEvidence;

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

}
