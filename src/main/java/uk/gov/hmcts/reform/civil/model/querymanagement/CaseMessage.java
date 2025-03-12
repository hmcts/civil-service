package uk.gov.hmcts.reform.civil.model.querymanagement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private LocalDateTime createdOn;
    private String createdBy;
    private String parentId;

}
