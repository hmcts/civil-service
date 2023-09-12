package uk.gov.hmcts.reform.civil.service.citizen.defendant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.service.citizen.events.CaseEventService;
import uk.gov.hmcts.reform.civil.service.citizen.events.EventSubmissionParams;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ASSIGN_LIP_DEFENDANT;

@Service
@RequiredArgsConstructor
public class LipDefendantCaseAssignmentService {

    private final IdamClient idamClient;
    private final CaseEventService caseEventService;

    public void addLipDefendantToCaseDefendantUserDetails(String authorisation, String caseId) {
        UserDetails defendantIdamUserDetails = idamClient.getUserDetails(authorisation);
        IdamUserDetails defendantUserDetails = IdamUserDetails.builder()
                .id(defendantIdamUserDetails.getId())
                .email(defendantIdamUserDetails.getEmail())
                .build();
        Map<String, Object> data = Map.of("defendantUserDetails", defendantUserDetails);
        caseEventService.submitEventForClaim(EventSubmissionParams
                .builder()
                .userId(defendantIdamUserDetails.getId())
                .authorisation(authorisation)
                .caseId(caseId)
                .updates(data)
                .event(ASSIGN_LIP_DEFENDANT)
                .build());
    }
}
