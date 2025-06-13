package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.client.WaTaskManagementApiClient;
import uk.gov.hmcts.reform.civil.enums.sendandreply.RecipientOption;
import uk.gov.hmcts.reform.civil.enums.sendandreply.RolePool;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.sendandreply.MessageReply;
import uk.gov.hmcts.reform.civil.model.sendandreply.Message;
import uk.gov.hmcts.reform.civil.model.sendandreply.MessageWaTaskDetails;
import uk.gov.hmcts.reform.civil.model.sendandreply.SendMessageMetadata;
import uk.gov.hmcts.reform.civil.model.wa.GetTasksResponse;
import uk.gov.hmcts.reform.civil.model.wa.RequestContext;
import uk.gov.hmcts.reform.civil.model.wa.SearchOperator;
import uk.gov.hmcts.reform.civil.model.wa.SearchParameterKey;
import uk.gov.hmcts.reform.civil.model.wa.SearchParameterList;
import uk.gov.hmcts.reform.civil.model.wa.SearchTaskRequest;
import uk.gov.hmcts.reform.civil.model.wa.Task;
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
import java.util.UUID;
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
    private static final Map<String, RolePool> SUPPORTED_ROLES_MAP = Map.ofEntries(
        entry("ctsc-team-leader", RolePool.ADMIN),
        entry("ctsc", RolePool.ADMIN),
        entry("hearing-centre-team-leader", RolePool.ADMIN),
        entry("hearing-centre-admin", RolePool.ADMIN),
        entry("senior-tribunal-caseworker", RolePool.LEGAL_OPERATIONS),
        entry("tribunal-caseworker", RolePool.LEGAL_OPERATIONS),
        entry("nbc-team-leader", RolePool.ADMIN),
        entry("national-business-centre", RolePool.ADMIN),
        entry("circuit-judge", RolePool.JUDICIAL_CIRCUIT),
        entry("district-judge", RolePool.JUDICIAL_DISTRICT),
        entry("judge", RolePool.JUDICIAL)
    );

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
        RecipientOption.DISTRICT_JUDGE, RolePool.JUDICIAL_DISTRICT,
        RecipientOption.CIRCUIT_JUDGE, RolePool.JUDICIAL_CIRCUIT
    );

    private final RoleAssignmentsService roleAssignmentsService;
    private final TableMarkupService tableMarkupService;
    private final UserService userService;
    private final Time time;
    private final WaTaskManagementApiClient waTaskManagementApiClient;
    private final AuthTokenGenerator authTokenGenerator;

    public List<Element<Message>> addMessage(
        List<Element<Message>> messages,
        SendMessageMetadata messageMetaData,
        String messageContent, String userAuth) {
        List<Element<Message>> messageList = ofNullable(messages).orElse(new ArrayList<>());

        messageList.add(element(
            createBaseMessageWithSenderDetails(userAuth)
                .toBuilder()
                .updatedTime(time.now())
                .sentTime(time.now())
                .recipientRoleType(ROLE_SELECTION_TO_POOL.get(messageMetaData.getRecipientRoleType()))
                .isUrgent(messageMetaData.getIsUrgent())
                .subjectType(messageMetaData.getSubjectType())
                .subject(messageMetaData.getSubject())
                .messageContent(messageContent)
                .messageID(UUID.randomUUID().toString().substring(0, 16))
                .build())
        );
        return messageList;
    }

    public Message createBaseMessageWithSenderDetails(String userAuth) {
        UserDetails details = userService.getUserDetails(userAuth);
        RoleAssignmentResponse role = getFirstSupportedRole(userAuth, details.getId());
        String senderName = String.format("%s, %s", details.getFullName(), role.getRoleLabel());

        return Message.builder()
            .senderName(senderName)
            .senderRoleType(SUPPORTED_ROLES_MAP.get(role.getRoleName()))
            .build();
    }

    public List<Element<Message>> addReplyToMessage(List<Element<Message>> messages, String messageId, MessageReply messageReply, String userAuth) {
        Element<Message> messageToReplace = getMessageById(messages, messageId);
        Message baseMessageDetails = createBaseMessageWithSenderDetails(userAuth);

        //Move current base message to history
        MessageReply messageForHistory = buildReplyOutOfMessage(messageToReplace.getValue());
        log.info("message for history id add reply " + messageForHistory.getMessageID());
        Element<MessageReply> newHistoryMessage = element(messageForHistory);

        //Switch out current base message with reply info
        messageToReplace.setValue(messageToReplace.getValue().buildNewFullReplyMessage(messageReply, baseMessageDetails, time));

        messageToReplace.getValue().getHistory().add(0, newHistoryMessage);

        return messages;
    }

    public MessageWaTaskDetails addTaskInfo(List<Element<Message>> messages, String messageId, String userAuth, CaseData caseData) {
        Element<Message> messageToReplace = getMessageById(messages, messageId);
        log.info("userAuth " + userAuth);
        MessageReply messageForHistory = buildReplyOutOfMessage(messageToReplace.getValue());
        log.info("message for history id  add task info " + messageForHistory.getMessageID());

        try {
            SearchTaskRequest request = new SearchTaskRequest(
                RequestContext.AVAILABLE_TASKS,
                List.of(new SearchParameterList(
                    SearchParameterKey.CASE_ID, SearchOperator.IN,
                    List.of(String.valueOf(caseData.getCcdCaseReference()))
                )));
            log.info("wa task search request " + request);
            ResponseEntity<GetTasksResponse<Task>> response = waTaskManagementApiClient.searchWithCriteria(
                authTokenGenerator.generate(),
                userAuth,
                request
            );
            log.info("response from wa api " + response);

            GetTasksResponse<Task> body = response.getBody();
            log.info("body " + body);
            if (body != null) {
                List<Task> tasks = body.getTasks();
                log.info("tasks from wa api " + tasks);
                if (!tasks.isEmpty()) {
                    List<Task> task = tasks.stream().filter(t -> t.getAdditionalProperties().entrySet().stream()
                        .anyMatch(e -> e.getKey().equals("messageId") && e.getValue().equals(
                            messageForHistory.getMessageID()))).toList();
                    log.info("filtered tasks " + task);
                    if (!task.isEmpty()) {
                        return MessageWaTaskDetails.builder()
                            .taskId(task.get(0).getId())
                            .messageID(messageForHistory.getMessageID())
                            .build();
                    }
                }
            }
        } catch (Exception e) {
            log.error("failed call wa api " + e.getMessage());
            throw e;
        }

        return null;
    }

    public Element<Message> getMessageById(List<Element<Message>> messages, String code) {
        return messages.stream().filter(message -> code.equals(message.getId().toString())).findFirst().orElse(null);
    }

    private RoleAssignmentResponse getFirstSupportedRole(String auth, String userId) {
        var roleAssignments = roleAssignmentsService.getRoleAssignmentsWithLabels(userId, auth, SUPPORTED_ROLES);

        RoleAssignmentResponse roleAssignment = roleAssignments.getRoleAssignmentResponse().stream()
            .filter(userRole -> SUPPORTED_ROLES.contains(userRole.getRoleName()))
            .min(Comparator.comparingInt(userRole -> SUPPORTED_ROLES.indexOf(userRole.getRoleName())))
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
            .messageID(message.getMessageID())
            .build();
    }
}
