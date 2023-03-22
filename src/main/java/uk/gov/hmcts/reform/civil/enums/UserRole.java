package uk.gov.hmcts.reform.civil.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasRole;

@AllArgsConstructor
@Getter
public enum UserRole implements HasRole {
    CASE_WORKER("caseworker-civil-admin", "CRU"),
    CASE_WORKER_CAA("caseworker-caa", "CRUD"),
    SOLICITOR("caseworker-civil-solicitor", "CRU"),
    CREATOR("[CREATOR]", "CRU");

    @JsonValue
    private final String role;
    private final String caseTypePermissions;

    public static UserRole fromString(String value) {
        for (UserRole role : UserRole.values()) {
            if (role.getRole().equals(value)) {
                return role;
            }
        }
        return null;
    }
}
