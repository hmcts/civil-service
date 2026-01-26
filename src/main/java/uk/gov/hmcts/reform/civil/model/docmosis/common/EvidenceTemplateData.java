package uk.gov.hmcts.reform.civil.model.docmosis.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.Evidence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class EvidenceTemplateData {

    private String type;
    private String explanation;

    @JsonIgnore
    public static EvidenceTemplateData toEvidenceTemplateData(Evidence evidence) {
        return new EvidenceTemplateData()
            .setType(evidence.getValue().getEvidenceType())
            .setExplanation(evidence.getValue().getEvidenceDescription());
    }

    @JsonIgnore
    public static List<EvidenceTemplateData> toEvidenceTemplateDataList(List<Evidence> evidenceList) {
        return Optional.ofNullable(evidenceList).map(Collection::stream)
            .orElseGet(Stream::empty)
            .map(EvidenceTemplateData::toEvidenceTemplateData)
            .toList();
    }

    @JsonProperty("displayTypeValue")
    public String getDisplayTypeValue() {
        return Optional.ofNullable(type).map(this::getDisplayValueFromEvidenceType).orElse("");
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
