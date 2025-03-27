package uk.gov.hmcts.reform.civil.model.querymanagement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.civil.enums.QueryCollectionType;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
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

    @JsonIgnore
    public List<Element<CaseMessage>> messageThread(String parentQueryId) {
        if (isNull(caseMessages) || caseMessages.isEmpty() || isNull(parentQueryId)) {
            return Collections.emptyList();
        }

        return caseMessages.stream()
            .filter(message ->
                        nonNull(message.getValue())
                            && (nonNull(message.getValue().getParentId())
                            ? message.getValue().getParentId().equals(parentQueryId)
                            : message.getValue().getId().equals(parentQueryId)))
            .sorted(Comparator.comparing(message -> message.getValue().getCreatedOn()))
            .collect(Collectors.toList());
    }

    @JsonIgnore
    public boolean isSame(CaseQueriesCollection caseQueriesCollection) {
        // purposely not overriding equals to avoid unexpected behaviour
        return nonNull(caseQueriesCollection) && caseQueriesCollection.equals(this);
    }

}
