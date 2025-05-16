package uk.gov.hmcts.reform.civil.model.docmosis.querymanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.Objects.nonNull;

@Builder
@AllArgsConstructor
@Data
public class QueryDocument implements MappableObject {

    private final String referenceNumber;
    private final List<DocumentQueryMessage> messages;

    public static QueryDocument from(String caseId, List<Element<CaseMessage>> messageThread) {
        if (!nonNull(messageThread)) {
            return null;
        }

        return QueryDocument.builder()
            .referenceNumber(caseId)
            .messages(IntStream.range(0, messageThread.size())
                          .mapToObj(i -> DocumentQueryMessage.from(
                              messageThread.get(i).getValue(), i % 2 != 0))
                          .toList())
            .build();
    }

}
