package uk.gov.hmcts.reform.dashboard.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.Instant;

@lombok.Getter
@lombok.Setter
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaskListTemplate implements Serializable {

    Long id;
    String titleEn;
    String contentEn;
    String titleCy;
    String contentCy;
    String reference;
    String taskStatusSequence;
    String role;
    Long orderBy;
    String categoryEn;
    String categoryCy;
    Instant createdDate;
}
