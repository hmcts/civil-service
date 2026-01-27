package uk.gov.hmcts.reform.civil.model.docmosis.querymanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;

import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.utils.DateUtils.convertFromUTC;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class DocumentQueryMessage {

    private static final String HEARING_DATE_FORMAT = "dd-MM-yyyy";
    private static final String CREATED_ON_FORMAT = "dd-MM-yyyy HH:mm";

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
        return new DocumentQueryMessage()
            .setMessageType(getMessageType(caseMessage, isCaseworkerMessage))
            .setId(caseMessage.getId())
            .setName(isCaseworkerMessage ? "Caseworker" : caseMessage.getName())
            .setSubject(isInitialQueryMessage ? caseMessage.getSubject() : null)
            .setBody(caseMessage.getBody())
            .setCreatedOn(convertFromUTC(caseMessage.getCreatedOn().toLocalDateTime()).format(DateTimeFormatter.ofPattern(
                CREATED_ON_FORMAT)))
            .setIsHearingRelated(isInitialQueryMessage ? caseMessage.getIsHearingRelated() : null)
            .setHearingDate(nonNull(caseMessage.getHearingDate()) && isInitialQueryMessage
                                ? caseMessage.getHearingDate().format(DateTimeFormatter.ofPattern(HEARING_DATE_FORMAT)) : null)
            .setAttachments(caseMessage.getAttachments());
    }

}

