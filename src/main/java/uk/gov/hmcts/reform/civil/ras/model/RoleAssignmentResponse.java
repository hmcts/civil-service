package uk.gov.hmcts.reform.civil.ras.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignmentResponse {

    private String actorId;
    private String actorIdType;
    private String roleType;
    private String roleName;
    private String roleLabel;
    private String classification;
    private String grantType;
    private String roleCategory;
    private Boolean readOnly;
    private ZonedDateTime beginTime;
    private ZonedDateTime endTime;
    private ZonedDateTime created;
    private Attributes attributes;

}
