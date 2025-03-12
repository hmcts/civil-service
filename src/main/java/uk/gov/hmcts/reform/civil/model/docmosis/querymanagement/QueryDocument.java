package uk.gov.hmcts.reform.civil.model.docmosis.querymanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class QueryDocument implements MappableObject {

    private final String referenceNumber;
    private final List<DocumentQueryMessage> messages;

    public static QueryDocument from(String caseId, List<Element<CaseMessage>> messageThread) {
        if (!nonNull(messageThread)) {
            return null;
        }

        // Assumes message thread is already sorted. The first message should be the initial query which will contain the
        // requester user id. Using this we can differentiate party message from caseworker message.
        String partyUserId = messageThread.get(0).getValue().getCreatedBy();
        return QueryDocument.builder()
            .referenceNumber(caseId)
            .messages(messageThread.stream()
                          .map(caseMessage -> DocumentQueryMessage.from(
                              caseMessage.getValue(), !caseMessage.getValue().getCreatedBy().equals(partyUserId))).toList())
            .build();
    }

}
