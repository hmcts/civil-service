package uk.gov.hmcts.reform.civil.service.citizen.defendant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.citizen.events.CaseEventService;
import uk.gov.hmcts.reform.civil.service.citizen.events.EventSubmissionParams;
import uk.gov.hmcts.reform.civil.service.pininpost.DefendantPinToPostLRspecService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ASSIGN_LIP_DEFENDANT;

@Service
@Slf4j
public class LipDefendantCaseAssignmentService {

    private final UserService userService;
    private final CaseEventService caseEventService;
    private final DefendantPinToPostLRspecService defendantPinToPostLRspecService;
    private final CaseDetailsConverter caseDetailsConverter;
    private boolean caseFlagsLoggingEnabled;

    public LipDefendantCaseAssignmentService(
        UserService userService,
        CaseEventService caseEventService,
        DefendantPinToPostLRspecService defendantPinToPostLRspecService,
        CaseDetailsConverter caseDetailsConverter,
        @Value("${case-flags.logging.enabled:false}") boolean caseFlagsLoggingEnabled
    ) {
        this.userService = userService;
        this.caseEventService = caseEventService;
        this.defendantPinToPostLRspecService = defendantPinToPostLRspecService;
        this.caseDetailsConverter = caseDetailsConverter;
        this.caseFlagsLoggingEnabled = caseFlagsLoggingEnabled;
    }

    public void addLipDefendantToCaseDefendantUserDetails(String authorisation, String caseId,
                                                          Optional<CaseRole> caseRole,
                                                          Optional<CaseDetails> caseDetails) {
        UserDetails defendantIdamUserDetails = userService.getUserDetails(authorisation);
        IdamUserDetails defendantUserDetails = IdamUserDetails.builder()
                .id(defendantIdamUserDetails.getId())
                .email(defendantIdamUserDetails.getEmail())
                .build();
        Map<String, Object> data = new HashMap<>();
        data.put("defendantUserDetails", defendantUserDetails);
        if (caseDetails.isPresent()) {
            CaseData caseData = caseDetailsConverter.toCaseData(caseDetails.get());
            Party respondent1 = caseData.getRespondent1();
            respondent1 = respondent1.toBuilder().partyEmail(defendantIdamUserDetails.getEmail()).build();
            data.put("respondent1", respondent1);
            if (caseFlagsLoggingEnabled) {
                log.info(
                    "case id: {}, respondent flags start of event submission: {}",
                    caseId,
                    respondent1.getFlags()
                );
            }
        }
        if (caseRole.isPresent() && caseRole.get() == CaseRole.DEFENDANT && caseDetails.isPresent()) {
            Map<String, Object> pinPostData = defendantPinToPostLRspecService.removePinInPostData(caseDetails.get());
            data.putAll(pinPostData);
        }
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
