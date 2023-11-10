package uk.gov.hmcts.reform.civil.model.docmosis.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.Evidence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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

    @JsonIgnore
    public static List<EvidenceTemplateData> toEvidenceTemplateDataList(List<Evidence> evidenceList) {
        return Optional.ofNullable(evidenceList).map(Collection::stream)
            .orElseGet(Stream::empty)
            .map(evidence -> EvidenceTemplateData.toEvidenceTemplateData(evidence))
            .toList();
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
