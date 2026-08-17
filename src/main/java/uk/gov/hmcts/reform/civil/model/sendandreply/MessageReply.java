package uk.gov.hmcts.reform.civil.model.sendandreply;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sendandreply.RolePool;
import uk.gov.hmcts.reform.civil.enums.sendandreply.SubjectOption;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MessageReply {

    @CCD(label = "Date and time sent", searchable = false)
    private LocalDateTime sentTime;
    @CCD(label = "Recipient", searchable = false)
    private RolePool recipientRoleType;
    @CCD(
            label = "From",
            showCondition = "isUrgent=\"DO NOT SHOW IN UI\"",
            searchable = false,
            typeOverride = FieldType.Text
    )
    private RolePool senderRoleType;
    @CCD(label = "Sender's name", searchable = false)
    private String senderName;
    @CCD(label = "Urgency", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo isUrgent;
    @CCD(label = "What is it about", searchable = false)
    private SubjectOption subjectType;
    @CCD(label = "Subject", searchable = false, max = 255)
    private String subject;
    @CCD(label = "Message details", searchable = false, typeOverride = FieldType.TextArea)
    private String messageContent;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", showCondition = "isUrgent=\"DO NOT SHOW IN UI\"", searchable = false)
  private String messageId;
  // ==== end synthesised definition-only fields ====
}
