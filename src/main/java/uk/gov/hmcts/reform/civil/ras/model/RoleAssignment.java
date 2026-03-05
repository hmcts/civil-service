package uk.gov.hmcts.reform.civil.ras.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignment {

    private String id;

    private String actorIdType = "IDAM";

    private Map<String, Object> attributes;

    private List<String> authorisations;

    private List<String> notes;

    private ZonedDateTime beginTime;

    private ZonedDateTime endTime;

    private ZonedDateTime created;

    private String status = "CREATE_REQUESTED";

    private String classification = "PUBLIC";

    private String actorId;
    private GrantType grantType;
    private RoleCategory roleCategory;
    private String roleName;
    private RoleType roleType;
    private boolean readOnly;

}
