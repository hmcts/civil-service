package uk.gov.hmcts.reform.civil.model.querymanagement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CaseQueriesCollection {

    private String partyName;
    private String roleOnCase;
    private List<Element<CaseMessage>> caseMessages;

    @JsonIgnore
    public CaseMessage latest() {
        return unwrapElements(ofNullable(getCaseMessages()).orElse(new ArrayList<>())).stream()
            .max(Comparator.comparing(CaseMessage::getCreatedOn))
            .orElse(null);
    }
}
