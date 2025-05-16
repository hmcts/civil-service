package uk.gov.hmcts.reform.civil.model.querymanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CaseMessage {

    private String id;
    private String subject;
    private String name;
    private String body;
    private List<Element<Document>> attachments;
    private YesOrNo isHearingRelated;
    private LocalDate hearingDate;
    private OffsetDateTime createdOn;
    private String createdBy;
    private String parentId;

}
