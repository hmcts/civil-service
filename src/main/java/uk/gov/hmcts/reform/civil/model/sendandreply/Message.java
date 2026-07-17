package uk.gov.hmcts.reform.civil.model.sendandreply;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sendandreply.RolePool;
import uk.gov.hmcts.reform.civil.enums.sendandreply.SubjectOption;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Slf4j
public class Message {

    @CCD(label = "Date and time sent", searchable = false)
    private LocalDateTime sentTime;
    @CCD(label = "Updated time", searchable = false)
    private LocalDateTime updatedTime;
    @CCD(label = "From", searchable = false, typeOverride = FieldType.FixedList, typeParameterOverride = "RolePool")
    private RolePool senderRoleType;
    @CCD(label = "Sender's name", searchable = false)
    private String senderName;
    @CCD(label = "To", searchable = false, typeOverride = FieldType.FixedList, typeParameterOverride = "RolePool")
    private RolePool recipientRoleType;
    @CCD(label = "What is it about", searchable = false)
    private SubjectOption subjectType;
    @CCD(label = "Subject", searchable = false, max = 255)
    private String subject;
    @CCD(label = "Message details", searchable = false, typeOverride = FieldType.TextArea)
    private String messageContent;
    @CCD(label = "Urgency", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo isUrgent;
    @CCD(label = " ", searchable = false)
    private String messageId;

    @CCD(label = "Message history", searchable = false)
    private List<Element<MessageReply>> history = new ArrayList<>();

    public Message buildNewFullReplyMessage(MessageReply reply, Message userDetails, Time time) {
        return copyOf()
            .setIsUrgent(reply.getIsUrgent())
            .setSenderName(userDetails.getSenderName())
            .setSenderRoleType(userDetails.getSenderRoleType())
            .setMessageContent(reply.getMessageContent())
            .setRecipientRoleType(this.senderRoleType)
            .setUpdatedTime(time.now());
    }

    public Message buildFullReplyMessageForTable(MessageReply reply) {
        return copyOf()
            .setSentTime(reply.getSentTime())
            .setSenderName(reply.getSenderName())
            .setIsUrgent(reply.getIsUrgent())
            .setMessageContent(reply.getMessageContent())
            .setRecipientRoleType(reply.getRecipientRoleType());
    }

    public Message copyOf() {
        Message copy = new Message()
            .setSentTime(this.sentTime)
            .setUpdatedTime(this.updatedTime)
            .setSenderRoleType(this.senderRoleType)
            .setSenderName(this.senderName)
            .setRecipientRoleType(this.recipientRoleType)
            .setSubjectType(this.subjectType)
            .setSubject(this.subject)
            .setMessageContent(this.messageContent)
            .setIsUrgent(this.isUrgent)
            .setMessageId(this.messageId);

        copy.setHistory(this.history);
        return copy;
    }

    public Message copyNoHistory() {
        return new Message()
            .setSentTime(this.sentTime)
            .setUpdatedTime(this.updatedTime)
            .setSenderRoleType(this.senderRoleType)
            .setSenderName(this.senderName)
            .setRecipientRoleType(this.recipientRoleType)
            .setSubjectType(this.subjectType)
            .setSubject(this.subject)
            .setMessageContent(this.messageContent)
            .setIsUrgent(this.isUrgent)
            .setMessageId(this.messageId);
    }
}
