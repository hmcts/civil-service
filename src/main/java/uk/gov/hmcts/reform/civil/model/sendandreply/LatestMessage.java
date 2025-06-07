package uk.gov.hmcts.reform.civil.model.sendandreply;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sendandreply.RolePool;
import uk.gov.hmcts.reform.civil.enums.sendandreply.SubjectOption;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class LatestMessage {

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

    @Builder.Default
    private List<Element<MessageReply>> history = new ArrayList<>();
}
