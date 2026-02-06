package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Objects;

/**
 * Encapsulates role-based checks required by DJ templates (e.g. judge authorship).
 */
@Service
public class DjAuthorisationFieldService {

    public boolean isJudge(UserDetails userDetails) {
        if (userDetails == null || userDetails.getRoles() == null) {
            return false;
        }
        return userDetails.getRoles().stream()
            .filter(Objects::nonNull)
            .anyMatch(role -> role.toLowerCase().contains("judge"));
    }
}
