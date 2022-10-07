package uk.gov.hmcts.reform.civil.model.documents;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;


@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@Getter
public class DocumentAndNote {


    private final Document DOCUMENT;
    private final String DOCUMENT_NOTE;
}
