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
import java.util.List;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendAndReplyMessageService {

    // Order is important here. As we only use the first matching role when mapping against a users role assignments.
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
        String senderRoleType = getRoleTypeLabel(userAuth, details.getId());
        String senderName = String.format("%s, %s", details.getFullName(), senderRoleType);

        List<Element<Message>> messageList = ofNullable(messages).orElse(new ArrayList<>());
        messageList.add(element(
            Message.from(messageMetaData)
                .toBuilder()
                .senderRoleType(senderRoleType).senderName(senderName)
                .sentTime(time.now())
                .updatedTime(time.now())
                .messageContent(messageContent)
                .build())
        );
        return messageList;
    }

    private String getRoleTypeLabel(String auth, String userId) {
        var roleAssignments = roleAssignmentsService.getRoleAssignmentsWithLabels(userId, auth);
        return SUPPORTED_ROLES.stream()
            .flatMap(supportedRole -> roleAssignments.getRoleAssignmentResponse().stream()
                .filter(userRole -> supportedRole.equals(userRole.getRoleName()))
                .map(RoleAssignmentResponse::getRoleLabel))
            .findFirst()
            .orElse("");
    }
}
