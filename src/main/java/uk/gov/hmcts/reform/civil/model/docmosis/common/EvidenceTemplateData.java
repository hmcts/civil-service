package uk.gov.hmcts.reform.civil.model.docmosis.common;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EvidenceTemplateData {

    private String type;
    private String explanation;
}
