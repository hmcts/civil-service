package uk.gov.hmcts.reform.civil.model.caseflags;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class FlagDetail {

    private String name;
    private String subTypeValue;
    private String subTypeKey;
    private String otherDescription;
    private String flagComment;
    private LocalDateTime dateTimeModified;
    private LocalDateTime dateTimeCreated;
    private List<Element<String>> path;
    private YesOrNo hearingRelevant;
    private String flagCode;
    private String status;

}
