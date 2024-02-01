package uk.gov.hmcts.reform.dashboard.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaskListTemplate implements Serializable {
    Long id;
    @Size(max = 256)
    String titleEn;
    @Size(max = 512)
    String contentEn;
    @Size(max = 256)
    String titleCy;
    @Size(max = 512)
    String contentCy;
    @Size(max = 256)
    String reference;
    @Size(max = 256)
    String taskStatusSequence;
    @Size(max = 256)
    String role;
    Long orderBy;
    @Size(max = 256)
    String categoryEn;
    @Size(max = 256)
    String categoryCy;
    @NotNull
    Instant createdDate;
}
