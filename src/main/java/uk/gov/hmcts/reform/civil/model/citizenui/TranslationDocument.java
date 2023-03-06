package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.documents.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslationDocument {
    private String translationType;
    private Document translation;
}
