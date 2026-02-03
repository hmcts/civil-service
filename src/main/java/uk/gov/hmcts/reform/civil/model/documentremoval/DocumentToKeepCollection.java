package uk.gov.hmcts.reform.civil.model.documentremoval;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class DocumentToKeepCollection {

    private DocumentToKeep value;
}
