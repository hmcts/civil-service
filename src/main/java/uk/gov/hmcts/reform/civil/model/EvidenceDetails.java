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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "EvidenceList", generate = true)
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class EvidenceDetails {

    @CCD(
            label = "Please make a selection",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "ListOfEvidences",
            typeParameterClass = ListOfEvidences.class
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String evidenceType;
    @CCD(
            label = "Describe this evidence in more detail (optional). For example, a photo of the work you carried out.",
            showCondition = "evidenceType = \"PHOTO_EVIDENCE\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String photoEvidence;
    @CCD(
            label = "Describe this evidence in more detail (optional). For example, a signed contract.",
            showCondition = "evidenceType = \"CONTRACTS_AND_AGREEMENTS\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String contractAndAgreementsEvidence;
    @CCD(
            label = "Describe this evidence in more detail (optional). For example, a surveyor’s report.",
            showCondition = "evidenceType = \"EXPERT_WITNESS\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String expertWitnessEvidence;
    @CCD(
            label = "Describe this evidence in more detail (optional). For example, a letter from the other party.",
            showCondition = "evidenceType = \"LETTERS_EMAILS_AND_OTHER_CORRESPONDENCE\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String lettersEmailsAndOtherCorrespondenceEvidence;
    @CCD(
            label = "Describe this evidence in more detail (optional). For example, a receipt showing the amount you’ve paid.",
            showCondition = "evidenceType = \"RECEIPTS\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String receiptsEvidence;
    @CCD(
            label = "Describe this evidence in more detail (optional). For example, a bank statement showing the amount you’ve paid.",
            showCondition = "evidenceType = \"STATEMENT_OF_ACCOUNT\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String statementOfTruthEvidence;
    @CCD(
            label = "Describe this evidence in more detail (optional).",
            showCondition = "evidenceType = \"OTHER\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
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
