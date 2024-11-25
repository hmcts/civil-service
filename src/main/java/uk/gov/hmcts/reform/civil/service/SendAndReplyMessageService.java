package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sendandreply.RecipientOption;
import uk.gov.hmcts.reform.civil.enums.sendandreply.RolePool;
import uk.gov.hmcts.reform.civil.enums.sendandreply.SubjectOption;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.sendandreply.MessageReply;
import uk.gov.hmcts.reform.civil.model.sendandreply.Message;
import uk.gov.hmcts.reform.civil.model.sendandreply.SendMessageMetadata;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendAndReplyMessageService {

    public static final String DATE_TIME_PATTERN = "dd MMM yyyy, hh:mm:ss a";

    // Order is important here as we only use the first matching role when mapping against a users role assignments.
    // Senior roles should always be before their non-senior counterpart.
    private static final Map<String, RolePool> SUPPORTED_ROLES = Map.ofEntries(
        entry("ctsc-team-leader", RolePool.ADMIN),
        entry("ctsc", RolePool.ADMIN),
        entry("hearing-centre-team-leader", RolePool.ADMIN),
        entry("hearing-centre-admin", RolePool.ADMIN),
        entry("senior-tribunal-caseworker",RolePool.LEGAL_OPERATIONS),
        entry("tribunal-caseworker", RolePool.LEGAL_OPERATIONS),
        entry("nbc-team-leader", RolePool.ADMIN),
        entry("national-business-centre", RolePool.ADMIN),
        entry("circuit-judge", RolePool.JUDICIAL_CIRCUIT),
        entry("district-judge", RolePool.JUDICIAL_DISTRICT),
        entry("judge", RolePool.JUDICIAL)
    );

    private static final Map<RecipientOption, RolePool> ROLE_SELECTION_TO_POOL = Map.of(
        RecipientOption.COURT_STAFF, RolePool.ADMIN,
        RecipientOption.LEGAL_ADVISOR, RolePool.LEGAL_OPERATIONS,
        RecipientOption.DISTRICT_JUDGE, RolePool.JUDICIAL_DISTRICT,
        RecipientOption.CIRCUIT_JUDGE, RolePool.JUDICIAL_CIRCUIT
    );

    private final RoleAssignmentsService roleAssignmentsService;
    private final TableMarkupService tableMarkupService;
    private final UserService userService;
    private final Time time;

    public List<Element<Message>> addMessage(
        List<Element<Message>> messages,
        SendMessageMetadata messageMetaData,
        String messageContent, String userAuth) {
        List<Element<Message>> messageList = ofNullable(messages).orElse(new ArrayList<>());

        String subject = SubjectOption.OTHER.equals(messageMetaData.getSubject())
            ? messageMetaData.getOtherSubjectName() : messageMetaData.getSubject().getLabel();

        messageList.add(element(
            createBaseMessageWithSenderDetails(userAuth)
                .toBuilder()
                .updatedTime(time.now())
                .recipientRoleType(ROLE_SELECTION_TO_POOL.get(messageMetaData.getRecipientRoleType()))
                .isUrgent(messageMetaData.getIsUrgent())
                .subjectType(messageMetaData.getSubject())
                .headerSubject(subject)
                .contentSubject(subject)
                .messageContent(messageContent)
                .build())
        );
        return messageList;
    }

    public Message createBaseMessageWithSenderDetails(String userAuth) {
        UserDetails details = userService.getUserDetails(userAuth);
        String role = getFirstSupportedRole(userAuth, details.getId());
        String senderName = String.format("%s, %s", details.getFullName(), role);

        return Message.builder()
            .senderName(senderName)
            .senderRoleType(SUPPORTED_ROLES.get(role))
            .build();
    }

    public List<Element<Message>> addReplyToMessage(List<Element<Message>> messages, String messageId, Message messageReply, String userAuth) {
        Element<Message> messageToReplace = getMessageById(messages, messageId);
        Message baseMessageDetails = createBaseMessageWithSenderDetails(userAuth);

        //Move current base message to history
        Element<Message> newHistoryMessage = element(messageToReplace.getValue().builder()
                                                         .history(null)
                                                         .updatedTime(null)
                                                         .sentTime(messageToReplace.getValue().getUpdatedTime())
                                                         .build());

        //Switch out current base message with reply info
        messageToReplace.setValue(messageToReplace.getValue()
                                      .toBuilder()
                                      .recipientRoleType(ofNullable(messageReply)
                                                             .orElse(Message.builder().build()).getSenderRoleType())
                                      .senderName(baseMessageDetails.getSenderName())
                                      .senderRoleType(baseMessageDetails.getSenderRoleType())
                                      .isUrgent(messageReply.getIsUrgent())
                                      .messageContent(messageReply.getMessageContent())
                                      .updatedTime(time.now())
                                      .build());
        messageToReplace.getValue().getHistory().add(newHistoryMessage);

        return messages;
    }

    public Element<Message> getMessageById(List<Element<Message>> messages, String code) {
        return messages.stream().filter(message -> code.equals(message.getId().toString())).findFirst().orElse(null);
    }

    private String getFirstSupportedRole(String auth, String userId) {
        var roleAssignments = roleAssignmentsService.getRoleAssignmentsWithLabels(userId, auth);
        RoleAssignmentResponse roleAssignment = roleAssignments.getRoleAssignmentResponse().stream()
            .filter(userRole -> SUPPORTED_ROLES.containsKey(userRole.getRoleName()))
            .min(Comparator.comparingInt(userRole -> SUPPORTED_ROLES.keySet().stream().toList().indexOf(userRole.getRoleName())))
            .orElse(RoleAssignmentResponse.builder().roleLabel("").roleCategory("").build());
        return roleAssignment.getRoleLabel();
    }

    public String renderMessageTableList(Element<Message> message) {
        StringBuilder builder = new StringBuilder();
        retrieveFullMessageHistory(message)
            .forEach(messageItem -> builder.append(renderMessageTable(messageItem)));
        return builder.toString();
    }

    private List<Message> retrieveFullMessageHistory(Element<Message> message) {
        return Stream.concat(Stream.of(message.getValue()), message.getValue().getHistory().stream()
                .map(historyItem -> message.getValue())).toList();
    }

    private String renderMessageTable(Message message) {
        Map<String, String> tableRows = new LinkedHashMap<>();
        tableRows.put("Date and time sent", formatDateTime(DATE_TIME_PATTERN, message.getSentTime()));
        tableRows.put("Sender's name", message.getSenderName());
        tableRows.put("Recipient role", message.getRecipientRoleType().getLabel());
        tableRows.put("Urgency", message.getIsUrgent().getLabel());
        tableRows.put("What is it about", message.getSubjectType().getLabel());
        tableRows.put("Subject", message.getContentSubject());
        tableRows.put("Message details", message.getMessageContent());
        return tableMarkupService.buildTableMarkUp(tableRows);
    }

    public DynamicList createMessageSelectionList(List<Element<Message>> messages) {
        return DynamicList.builder()
            .listItems(messages.stream().map(this::createMessageListItems).toList())
            .build();
    }

    private DynamicListElement createMessageListItems(Element<Message> message) {
        String formattedSentDate =
            formatDateTime(DATE_TIME_PATTERN, message.getValue().getSentTime());
        return DynamicListElement.dynamicElementFromCode(
            message.getId().toString(),
            String.format("%s, %s", message.getValue().getHeaderSubject(), formattedSentDate)
        );
    }

    private static String formatDateTime(String pattern, LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH))
            .replace("am", "AM").replace("pm", "PM");
    }

}
