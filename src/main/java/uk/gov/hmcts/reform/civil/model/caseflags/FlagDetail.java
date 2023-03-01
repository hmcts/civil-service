package uk.gov.hmcts.reform.civil.model.caseflags;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class FlagDetail {

    private String name;
    private String subTypeValue;
    private String subTypeKey;
    private String otherDescription;
    private String flagComment;
    private LocalDateTime dateTimeModified;
    private LocalDateTime dateTimeCreated;
    private List<String> path;
    private YesOrNo hearingRelevant;
    private String flagCode;
    private String status;

}

