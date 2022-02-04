package uk.gov.hmcts.reform.ras.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignmentResponse {

    private String actorId;
    private String actorIdType;
    private String roleType;
    private String roleName;
    private String classification;
    private String grantType;
    private String roleCategory;
    private Boolean readOnly;
    private LocalDate beginTime;
    private LocalDate created;
    private Attributes attributes;

}
