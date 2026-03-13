package uk.gov.hmcts.reform.civil.ras.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class QueryRequest {

    private List<String> actorId;
    private List<String> roleType;
    private List<String> roleName;
    private List<String> classification;
    private List<String> grantType;

    private LocalDateTime validAt;
    private List<String> roleCategory;

    private Map<String, List<String>> attributes;
    private List<String> authorisations;

    private List<String> hasAttributes;

    private Boolean readOnly;

}
