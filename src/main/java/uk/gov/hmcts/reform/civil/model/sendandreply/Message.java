package uk.gov.hmcts.reform.civil.model.sendandreply;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sendandreply.SubjectOption;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    private LocalDateTime sentTime;
    private LocalDateTime updatedTime;
    private String senderRoleType;
    private String senderName;
    private String recipientRoleType;
    private YesOrNo isUrgent;
    private String headerSubject;
    private String contentSubject;
    private String messageContent;

    public static Message from(SendMessageMetadata messageContext) {
        String subject = SubjectOption.OTHER.equals(messageContext.getSubject())
            ? messageContext.getOtherSubjectName() : messageContext.getSubject().getLabel();
        return Message.builder()
            .recipientRoleType(messageContext.getRecipientRoleType().getLabel())
            .isUrgent(messageContext.getIsUrgent())
            .headerSubject(subject)
            .contentSubject(subject)
            .build();
    }

}
