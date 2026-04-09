package uk.gov.hmcts.reform.civil.model.docmosis.querymanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.Objects.nonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class QueryDocument implements MappableObject {

    private String referenceNumber;
    private List<DocumentQueryMessage> messages;

    public static QueryDocument from(String caseId, List<Element<CaseMessage>> messageThread) {
        if (!nonNull(messageThread)) {
            return null;
        }

        return new QueryDocument()
            .setReferenceNumber(caseId)
            .setMessages(IntStream.range(0, messageThread.size())
                             .mapToObj(i -> DocumentQueryMessage.from(
                                 messageThread.get(i).getValue(), i % 2 != 0))
                             .toList());
    }

}
