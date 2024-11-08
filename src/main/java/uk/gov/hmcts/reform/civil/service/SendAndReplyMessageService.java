package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.sendandreply.Message;
import uk.gov.hmcts.reform.civil.model.sendandreply.SendMessageMetadata;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendAndReplyMessageService {

    private static final Map<String, String> ROLE_CATEGORY_LABEL_MAP = Map.of(
            "ADMIN", "Court staff",
            "JUDICIAL", "Judge",
            "LEGAL_OPERATIONS", "Legal advisor"
    );

    // Order is important here as we only use the first matching role when mapping against a users role assignments.
    // Senior roles should always be before their non-senior counterpart.
    private static final List<String> SUPPORTED_ROLES = List.of(
        "ctsc-team-leader",
        "ctsc",
        "hearing-centre-team-leader",
        "hearing-centre-admin",
        "senior-tribunal-caseworker",
        "tribunal-caseworker",
        "nbc-team-leader",
        "national-business-centre",
        "circuit-judge",
        "district-judge",
        "judge"
    );

    private final RoleAssignmentsService roleAssignmentsService;
    private final UserService userService;
    private final Time time;

    public List<Element<Message>> addMessage(
        List<Element<Message>> messages,
        SendMessageMetadata messageMetaData,
        String messageContent, String userAuth) {

        UserDetails details = userService.getUserDetails(userAuth);
        RoleAssignmentResponse role = getFirstSupportedRole(userAuth, details.getId());
        String senderRoleCategory = ROLE_CATEGORY_LABEL_MAP.getOrDefault(role.getRoleCategory(), "");
        String senderAssignedRole = role.getRoleLabel();
        String senderName = String.format("%s, %s", details.getFullName(), senderAssignedRole);

        List<Element<Message>> messageList = ofNullable(messages).orElse(new ArrayList<>());
        messageList.add(element(
            Message.from(messageMetaData)
                .toBuilder()
                .senderRoleType(senderRoleCategory)
                .senderName(senderName)
                .sentTime(time.now())
                .updatedTime(time.now())
                .messageContent(messageContent)
                .build())
        );
        return messageList;
    }

    private RoleAssignmentResponse getFirstSupportedRole(String auth, String userId) {
        var roleAssignments = roleAssignmentsService.getRoleAssignmentsWithLabels(userId, auth);
        return roleAssignments.getRoleAssignmentResponse().stream()
            .filter(userRole -> SUPPORTED_ROLES.contains(userRole.getRoleName()))
            .min(Comparator.comparingInt(userRole -> SUPPORTED_ROLES.indexOf(userRole.getRoleName())))
            .orElse(RoleAssignmentResponse.builder().roleLabel("").roleCategory("").build());
    }
}
