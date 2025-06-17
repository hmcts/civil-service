package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sendandreply.RecipientOption;
import uk.gov.hmcts.reform.civil.enums.sendandreply.RolePool;
import uk.gov.hmcts.reform.civil.model.CaseData;
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
    private final FeatureToggleService featureToggleService;

    // Order is important here as we only use the first matching role when mapping against a users role assignments.
    // Senior roles should always be before their non-senior counterpart.
    Map<String, RolePool> buildSupportedRolesMap(CaseData caseData) {
        Map<String, RolePool> supportedRolesMap = new LinkedHashMap<>();
        if (featureToggleService.isPublicQueryManagementEnabled(caseData)) {
            supportedRolesMap.put("wlu-admin", RolePool.WLU_ADMIN);
        }
        supportedRolesMap.put("ctsc-team-leader", RolePool.ADMIN);
        supportedRolesMap.put("ctsc", RolePool.ADMIN);
        supportedRolesMap.put("hearing-centre-team-leader", RolePool.ADMIN);
        supportedRolesMap.put("hearing-centre-admin", RolePool.ADMIN);
        supportedRolesMap.put("senior-tribunal-caseworker", RolePool.LEGAL_OPERATIONS);
        supportedRolesMap.put("tribunal-caseworker", RolePool.LEGAL_OPERATIONS);
        supportedRolesMap.put("nbc-team-leader", RolePool.ADMIN);
        supportedRolesMap.put("national-business-centre", RolePool.ADMIN);
        supportedRolesMap.put("circuit-judge", RolePool.JUDICIAL_CIRCUIT);
        supportedRolesMap.put("district-judge", RolePool.JUDICIAL_DISTRICT);
        supportedRolesMap.put("judge", RolePool.JUDICIAL);
        return supportedRolesMap;
    }

    List<String> buildSupportedRolesList(CaseData caseData) {
        List<String> supportedRolesList = new ArrayList<>();
        if (featureToggleService.isPublicQueryManagementEnabled(caseData)) {
            supportedRolesList.add("wlu-admin");
        }
        supportedRolesList.add("ctsc-team-leader");
        supportedRolesList.add("ctsc");
        supportedRolesList.add("hearing-centre-team-leader");
        supportedRolesList.add("hearing-centre-admin");
        supportedRolesList.add("senior-tribunal-caseworker");
        supportedRolesList.add("tribunal-caseworker");
        supportedRolesList.add("nbc-team-leader");
        supportedRolesList.add("national-business-centre");
        supportedRolesList.add("circuit-judge");
        supportedRolesList.add("district-judge");
        supportedRolesList.add("judge");
        return supportedRolesList;
    }

    private static final Map<RecipientOption, RolePool> ROLE_SELECTION_TO_POOL = Map.of(
        RecipientOption.COURT_STAFF, RolePool.ADMIN,
        RecipientOption.LEGAL_ADVISOR, RolePool.LEGAL_OPERATIONS,
        RecipientOption.DISTRICT_JUDGE, RolePool.JUDICIAL_DISTRICT,
        RecipientOption.CIRCUIT_JUDGE, RolePool.JUDICIAL_CIRCUIT,
        RecipientOption.WELSH_LANGUAGE_UNIT, RolePool.WLU_ADMIN
    );

    private final RoleAssignmentsService roleAssignmentsService;
    private final TableMarkupService tableMarkupService;
    private final UserService userService;
    private final Time time;

    public List<Element<Message>> addMessage(
        List<Element<Message>> messages,
        SendMessageMetadata messageMetaData,
        String messageContent, String userAuth, CaseData caseData) {
        List<Element<Message>> messageList = ofNullable(messages).orElse(new ArrayList<>());

        messageList.add(element(
            createBaseMessageWithSenderDetails(userAuth, caseData)
                .toBuilder()
                .updatedTime(time.now())
                .sentTime(time.now())
                .recipientRoleType(ROLE_SELECTION_TO_POOL.get(messageMetaData.getRecipientRoleType()))
                .isUrgent(messageMetaData.getIsUrgent())
                .subjectType(messageMetaData.getSubjectType())
                .subject(messageMetaData.getSubject())
                .messageContent(messageContent)
                .build())
        );
        return messageList;
    }

    public Message createBaseMessageWithSenderDetails(String userAuth, CaseData caseData) {
        UserDetails details = userService.getUserDetails(userAuth);
        RoleAssignmentResponse role = getFirstSupportedRole(userAuth, details.getId(), caseData);
        String senderName = String.format("%s, %s", details.getFullName(), role.getRoleLabel());
        Map<String, RolePool> supportRoleMap = buildSupportedRolesMap(caseData);

        return Message.builder()
            .senderName(senderName)
            .senderRoleType(supportRoleMap.get(role.getRoleName()))
            .build();
    }

    public List<Element<Message>> addReplyToMessage(List<Element<Message>> messages, String messageId, MessageReply messageReply,
                                                    String userAuth, CaseData caseData) {
        Element<Message> messageToReplace = getMessageById(messages, messageId);
        Message baseMessageDetails = createBaseMessageWithSenderDetails(userAuth, caseData);

        //Move current base message to history
        MessageReply messageForHistory = buildReplyOutOfMessage(messageToReplace.getValue());
        Element<MessageReply> newHistoryMessage = element(messageForHistory);

        //Switch out current base message with reply info
        messageToReplace.setValue(messageToReplace.getValue().buildNewFullReplyMessage(messageReply, baseMessageDetails, time));

        messageToReplace.getValue().getHistory().add(0, newHistoryMessage);

        return messages;
    }

    public Element<Message> getMessageById(List<Element<Message>> messages, String code) {
        return messages.stream().filter(message -> code.equals(message.getId().toString())).findFirst().orElse(null);
    }

    private RoleAssignmentResponse getFirstSupportedRole(String auth, String userId, CaseData caseData) {
        List<String> supportedRolesList = buildSupportedRolesList(caseData);
        var roleAssignments = roleAssignmentsService.getRoleAssignmentsWithLabels(userId, auth, supportedRolesList);

        RoleAssignmentResponse roleAssignment = roleAssignments.getRoleAssignmentResponse().stream()
            .filter(userRole -> supportedRolesList.contains(userRole.getRoleName()))
            .min(Comparator.comparingInt(userRole -> supportedRolesList.indexOf(userRole.getRoleName())))
            .orElse(RoleAssignmentResponse.builder().roleLabel("").roleCategory("").build());
        return roleAssignment;
    }

    public String renderMessageTableList(Element<Message> message) {
        StringBuilder builder = new StringBuilder();
        retrieveFullMessageHistory(message)
            .forEach(messageItem -> builder.append(renderMessageTable(messageItem)));
        return builder.toString();
    }

    private List<Message> retrieveFullMessageHistory(Element<Message> message) {
        Message baseMessage = message.getValue().toBuilder().sentTime(message.getValue().getUpdatedTime()).build();
        return Stream.concat(Stream.of(baseMessage), message.getValue().getHistory().stream()
                .map(historyItem -> message.getValue().buildFullReplyMessageForTable(historyItem.getValue())))
                .sorted(Comparator.comparing(Message::getSentTime).reversed())
                .toList();
    }

    private String renderMessageTable(Message message) {
        Map<String, String> tableRows = new LinkedHashMap<>();
        tableRows.put("Date and time sent", formatDateTime(DATE_TIME_PATTERN, message.getSentTime()));
        tableRows.put("Sender's name", message.getSenderName());
        tableRows.put("Recipient role", message.getRecipientRoleType().getLabel());
        tableRows.put("Urgency", message.getIsUrgent().getLabel());
        tableRows.put("What is it about", message.getSubjectType().getLabel());
        tableRows.put("Subject", message.getSubject());
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
            String.format("%s, %s", message.getValue().getSubject(), formattedSentDate)
        );
    }

    private static String formatDateTime(String pattern, LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH))
            .replace("am", "AM").replace("pm", "PM");
    }

    public MessageReply buildReplyOutOfMessage(Message message) {
        return MessageReply.builder()
            .sentTime(message.getUpdatedTime())
            .isUrgent(message.getIsUrgent())
            .senderName(message.getSenderName())
            .senderRoleType(message.getSenderRoleType())
            .messageContent(message.getMessageContent())
            .recipientRoleType(message.getRecipientRoleType())
            .subject(message.getSubject())
            .subjectType(message.getSubjectType())
            .build();
    }
}
