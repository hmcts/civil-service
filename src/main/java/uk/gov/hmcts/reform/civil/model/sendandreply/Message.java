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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Slf4j
public class Message {

    private LocalDateTime sentTime;
    private LocalDateTime updatedTime;
    private RolePool senderRoleType;
    private String senderName;
    private RolePool recipientRoleType;
    private SubjectOption subjectType;
    private String subject;
    private String messageContent;
    private YesOrNo isUrgent;
    private String messageId;

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
