package uk.gov.hmcts.reform.civil.model.docmosis.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.Evidence;

import java.util.Optional;

@Getter
@Builder
public class EvidenceTemplateData {

    private String type;
    private String explanation;

    @JsonIgnore
    public static EvidenceTemplateData toEvidenceTemplateData(Evidence evidence) {
        return EvidenceTemplateData.builder().type(evidence.getValue().getEvidenceType())
            .explanation(evidence.getValue().getEvidenceDescription())
            .build();
    }

    @JsonProperty("displayTypeValue")
    public String getDisplayTypeValue() {
        return Optional.ofNullable(type).map(type -> getDisplayValueFromEvidenceType(type)).orElse("");
    }

    private String getDisplayValueFromEvidenceType(String type) {
        String displayValue;
        try {
            displayValue = EvidenceType.valueOf(type).getDisplayValue();
        } catch (IllegalArgumentException ex) {
            displayValue = type;
        }
        return displayValue;
    }
}
