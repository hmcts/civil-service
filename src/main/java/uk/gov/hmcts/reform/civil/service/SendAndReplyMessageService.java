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

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendAndReplyMessageService {

    public static final String DATE_TIME_PATTERN = "dd MMM yyyy, hh:mm:ss a";

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

    private static final Map<RecipientOption, RolePool> ROLE_SELECTION_TO_POOL = Map.of(
        RecipientOption.COURT_STAFF, RolePool.ADMIN,
        RecipientOption.LEGAL_ADVISOR, RolePool.LEGAL_OPERATIONS,
        RecipientOption.DISTRICT_JUDGE, RolePool.JUDICIAL,
        RecipientOption.CIRCUIT_JUDGE, RolePool.JUDICIAL
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
        RoleAssignmentResponse role = getFirstSupportedRole(userAuth, details.getId());
        String senderName = String.format("%s, %s", details.getFullName(), role.getRoleLabel());

        return Message.builder()
            .sentTime(time.now())
            .senderName(senderName)
            .senderRoleType(RolePool.valueOf(role.getRoleCategory()))
            .build();
    }

    public List<Element<Message>> addReplyToMessage(List<Element<Message>> messages, String messageId, MessageReply messageReply, String userAuth) {
        Element<Message> messageToReplyTo = getMessageById(messages, messageId);
        Message baseMessageDetails = createBaseMessageWithSenderDetails(userAuth);
        Element<MessageReply> newMessage = element(messageReply.toBuilder()
                                                       .recipientRoleType(getLatestMessage(messageToReplyTo).getSenderRoleType())
                                                       .senderName(baseMessageDetails.getSenderName())
                                                       .senderRoleType(baseMessageDetails.getSenderRoleType())
                                                       .sentTime(baseMessageDetails.getSentTime()).build());
        messageToReplyTo.getValue().getHistory().add(newMessage);
        return messages;
    }

    public Element<Message> getMessageById(List<Element<Message>> messages, String code) {
        return messages.stream().filter(message -> code.equals(message.getId().toString())).findFirst().orElse(null);
    }

    private RoleAssignmentResponse getFirstSupportedRole(String auth, String userId) {
        var roleAssignments = roleAssignmentsService.getRoleAssignmentsWithLabels(userId, auth);
        return roleAssignments.getRoleAssignmentResponse().stream()
            .filter(userRole -> SUPPORTED_ROLES.contains(userRole.getRoleName()))
            .min(Comparator.comparingInt(userRole -> SUPPORTED_ROLES.indexOf(userRole.getRoleName())))
            .orElse(RoleAssignmentResponse.builder().roleLabel("").roleCategory("").build());
    }

    public String renderMessageTableList(Element<Message> message) {
        StringBuilder builder = new StringBuilder();
        retrieveFullMessageHistory(message)
            .forEach(messageItem -> builder.append(renderMessageTable(messageItem)));
        return builder.toString();
    }

    private List<Message> retrieveFullMessageHistory(Element<Message> message) {
        return Stream.concat(Stream.of(message.getValue()), message.getValue().getHistory().stream()
                .map(historyItem -> message.getValue().buildFullReplyMessage(historyItem.getValue())))
            .sorted(Comparator.comparing(Message::getSentTime).reversed())
            .toList();
    }

    private Message getLatestMessage(Element<Message> message) {
        return retrieveFullMessageHistory(message).stream().findFirst().orElse(null);
    }

    private String renderMessageTable(Message message) {
        Map<String, String> tableRows = new LinkedHashMap<>();
        tableRows.put("Date and time sent", formatDateTime(DATE_TIME_PATTERN, message.getSentTime()));
        tableRows.put("Sender's name", message.getSenderName());
        tableRows.put("Recipient role", message.getSenderRoleType().getLabel());
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
