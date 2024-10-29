package uk.gov.hmcts.reform.civil.model.sendAndReply;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sendAndReply.RecipientOption;
import uk.gov.hmcts.reform.civil.enums.sendAndReply.SubjectOption;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageContext {

    private RecipientOption recipient;
    private YesOrNo isUrgent;
    private SubjectOption subject;
    private String otherSubjectName;

}
