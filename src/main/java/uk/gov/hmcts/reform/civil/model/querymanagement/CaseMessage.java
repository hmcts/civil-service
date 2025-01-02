package uk.gov.hmcts.reform.civil.model.querymanagement;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class CaseMessage {

    private final String id;
    private final String subject;
    private final String name;
    private final String body;
    private final List<Element<Document>> attachments;
    private final YesOrNo isHearingRelated;
    private final LocalDate hearingDate;
    private final LocalDateTime createdOn;
    private final String createdBy;
    private final String parentId;

}
