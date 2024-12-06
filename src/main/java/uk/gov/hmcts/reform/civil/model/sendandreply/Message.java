package uk.gov.hmcts.reform.civil.model.sendandreply;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sendandreply.RolePool;
import uk.gov.hmcts.reform.civil.enums.sendandreply.SubjectOption;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
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

    @Builder.Default
    private List<Element<MessageReply>> history = new ArrayList<>();

    public Message buildNewFullReplyMessage(MessageReply reply, Message userDetails, Time time) {
        return this.toBuilder()
            .isUrgent(reply.getIsUrgent())
            .senderName(userDetails.getSenderName())
            .senderRoleType(userDetails.getSenderRoleType())
            .messageContent(reply.getMessageContent())
            .recipientRoleType(this.senderRoleType)
            .updatedTime(time.now())
            .build();
    }

    public Message buildFullReplyMessageForTable(MessageReply reply) {
        return this.toBuilder()
            .sentTime(reply.getSentTime())
            .senderName(reply.getSenderName())
            .isUrgent(reply.getIsUrgent())
            .messageContent(reply.getMessageContent())
            .recipientRoleType(reply.getRecipientRoleType())
            .build();
    }
}
