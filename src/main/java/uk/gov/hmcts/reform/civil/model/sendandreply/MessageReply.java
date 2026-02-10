package uk.gov.hmcts.reform.civil.model.sendandreply;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sendandreply.RolePool;
import uk.gov.hmcts.reform.civil.enums.sendandreply.SubjectOption;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MessageReply {

    private LocalDateTime sentTime;
    private RolePool recipientRoleType;
    private RolePool senderRoleType;
    private String senderName;
    private YesOrNo isUrgent;
    private SubjectOption subjectType;
    private String subject;
    private String messageContent;
}
