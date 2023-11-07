package uk.gov.hmcts.reform.civil.model.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class DocumentWithName {

    private Document document;
    private String documentName;
    @Builder.Default
    private LocalDateTime createdDateTime = LocalDateTime.now(ZoneId.of("Europe/London"));
}
