package uk.gov.hmcts.reform.civil.model.sendandreply;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sendandreply.RecipientOption;
import uk.gov.hmcts.reform.civil.enums.sendandreply.SubjectOption;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SendMessageMetadata {

    private RecipientOption recipientRoleType;
    private YesOrNo isUrgent;
    private SubjectOption subjectType;
    private String subject;

}
