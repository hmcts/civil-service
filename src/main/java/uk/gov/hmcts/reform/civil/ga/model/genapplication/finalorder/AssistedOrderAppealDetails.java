package uk.gov.hmcts.reform.civil.ga.model.genapplication.finalorder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.ga.enums.dq.AppealOriginTypes;
import uk.gov.hmcts.reform.civil.ga.enums.dq.PermissionToAppealTypes;

@Setter
@Data
@Builder(toBuilder = true)
public class AssistedOrderAppealDetails {

    private AppealOriginTypes appealOrigin;
    private String otherOriginText;
    private PermissionToAppealTypes permissionToAppeal;
    private AppealTypeChoices appealTypeChoicesForGranted;
    private AppealTypeChoices appealTypeChoicesForRefused;

    @JsonCreator
    AssistedOrderAppealDetails(@JsonProperty("appealOrigin") AppealOriginTypes appealOrigin,
                               @JsonProperty("otherOriginText") String otherOriginText,
                               @JsonProperty("permissionToAppeal") PermissionToAppealTypes permissionToAppeal,
                               @JsonProperty("assistedOrderAppealDropdownGranted") AppealTypeChoices appealTypeChoicesForGranted,
                               @JsonProperty("assistedOrderAppealDropdownRefused") AppealTypeChoices appealTypeChoicesForRefused
                               ) {

        this.appealOrigin = appealOrigin;
        this.otherOriginText = otherOriginText;
        this.permissionToAppeal = permissionToAppeal;
        this.appealTypeChoicesForGranted = appealTypeChoicesForGranted;
        this.appealTypeChoicesForRefused = appealTypeChoicesForRefused;
    }
}
