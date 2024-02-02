package uk.gov.hmcts.reform.dashboard.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
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

    @Id
    private Long id;
    private String titleEn;
    private String contentEn;
    private String titleCy;
    private String contentCy;
    private String reference;
    private String taskStatusSequence;
    private String role;
    private Long orderBy;
    private String categoryEn;
    private String categoryCy;
    private Instant createdDate;
}
