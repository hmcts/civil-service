package uk.gov.hmcts.reform.civil.model.docmosis.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.Evidence;

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
}
