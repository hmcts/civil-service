package uk.gov.hmcts.reform.civil.model.docmosis.querymanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;

import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.util.Objects.nonNull;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class DocumentQueryMessage {

    private final static String CREATED_ON_FORMAT = "dd-MM-yyyy HH:mm";
    private final static String HEARING_DATE_FORMAT = "dd-MM-yyyy";

    private String messageType;
    private String id;
    private String subject;
    private String name;
    private String body;
    private List<Element<Document>> attachments;
    private YesOrNo isHearingRelated;
    private String hearingDate;
    private String createdOn;

    private static String getMessageType(CaseMessage message, boolean isCaseworkerMessage) {
        return (isCaseworkerMessage ? QueryMessageType.RESPONSE
            : nonNull(message.getParentId()) ? QueryMessageType.FOLLOW_UP : QueryMessageType.QUERY).getLabel();
    }

    public static DocumentQueryMessage from(CaseMessage caseMessage, boolean isCaseworkerMessage) {
        boolean isInitialQueryMessage = !nonNull(caseMessage.getParentId());
        return DocumentQueryMessage.builder()
            .messageType(getMessageType(caseMessage, isCaseworkerMessage))
            .id(caseMessage.getId())
            .name(isCaseworkerMessage ? "Caseworker" : caseMessage.getName())
            .subject(isInitialQueryMessage ? caseMessage.getSubject() : null)
            .body(caseMessage.getBody())
            .createdOn(caseMessage.getCreatedOn().format(DateTimeFormatter.ofPattern(CREATED_ON_FORMAT)))
            .isHearingRelated(isInitialQueryMessage ? caseMessage.getIsHearingRelated() : null)
            .hearingDate(nonNull(caseMessage.getHearingDate()) && isInitialQueryMessage ? caseMessage.getHearingDate().format(DateTimeFormatter.ofPattern(HEARING_DATE_FORMAT)) : null)
            .attachments(caseMessage.getAttachments())
            .build();
    }

}

