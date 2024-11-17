package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Nested;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.enums.sendandreply.RolePool;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.sendandreply.Message;
import uk.gov.hmcts.reform.civil.model.sendandreply.MessageReply;
import uk.gov.hmcts.reform.civil.model.sendandreply.SendMessageMetadata;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentResponse;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.sendandreply.RecipientOption.CIRCUIT_JUDGE;
import static uk.gov.hmcts.reform.civil.enums.sendandreply.SubjectOption.HEARING;
import static uk.gov.hmcts.reform.civil.enums.sendandreply.SubjectOption.OTHER;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

@ExtendWith(SpringExtension.class)
class SendAndReplyMessageServiceTest {

    private static String USER_AUTH = "auth";
    private static String USER_NAME = "Test User";

    private static final UserDetails USER_DETAILS = UserDetails.builder()
        .id("uid")
        .forename("Test")
        .surname("User")
        .build();

    private static SendMessageMetadata MESSAGE_METADATA = SendMessageMetadata.builder()
        .subject(OTHER)
        .subject(HEARING)
        .recipientRoleType(CIRCUIT_JUDGE)
        .isUrgent(YES)
        .build();
    private static String MESSAGE_CONTENT = "Message Content";
    private static LocalDateTime NOW = LocalDateTime.of(2014, 11, 1, 0, 0, 0);

    private Message message;

    @Mock
    private UserService userService;

    @Mock
    private RoleAssignmentsService roleAssignmentService;

    @Mock
    private TableMarkupService tableMarkupService;

    @Mock
    private Time time;

    @InjectMocks
    private SendAndReplyMessageService messageService;

    @BeforeEach
    public void setupTest() {
        when(time.now()).thenReturn(NOW);
        when(tableMarkupService.buildTableMarkUp(any())).thenReturn("<div>Some markup</div>");

        message = Message.builder()
            .updatedTime(LocalDateTime.of(2024, 1, 1, 0, 0, 0))
            .sentTime(LocalDateTime.of(2024, 1, 1, 0, 0, 0))
            .headerSubject("Subject")
            .contentSubject("Subject")
            .senderRoleType(RolePool.ADMIN)
            .messageContent("Existing message")
            .build();
    }

    @Nested
    class AddMessage {
        @Test
        public void should_returnExpectedMessage_whenExistingMessagesAreNull() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            RolePool expectedSenderRoleCategory = RolePool.ADMIN;
            String expectedUserRoleLabel = "Hearing Centre Administrator";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                    RoleAssignmentResponse.builder().roleName("hearing-centre-admin").roleLabel(
                        "Hearing Centre Administrator").roleCategory("ADMIN").build())
                )
            );

            List<Element<Message>> actual = messageService.addMessage(
                null,
                MESSAGE_METADATA,
                MESSAGE_CONTENT,
                USER_AUTH
            );

            assertEquals(
                List.of(buildExpectedMessage(expectedSenderRoleCategory, expectedUserRoleLabel)),
                unwrapElements(actual)
            );
        }

        @Test
        public void shouldAddMessageReplyToMessageHistory_forHearingCentreAdmin() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            RolePool expectedSenderRoleCategory = RolePool.ADMIN;
            String expectedUserRoleLabel = "Hearing Centre Admin";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                                                 RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                                                 RoleAssignmentResponse.builder().roleName("hearing-centre-admin").roleLabel("Hearing Centre Admin").roleCategory(
                                                     "ADMIN").build()
                                             )
                )
            );

            Element<Message> existingMessage = element(message);
            List<Element<Message>> messages = new ArrayList<>();
            messages.add(existingMessage);

            MessageReply messageReply = MessageReply.builder().isUrgent(YES).messageContent("This is a reply message").build();

            List<Message> actualMessages = unwrapElements(messageService.addReplyToMessage(
                messages,
                existingMessage.getId().toString(),
                messageReply,
                USER_AUTH
            ));

            List<MessageReply> actualMessageHistory = unwrapElements(actualMessages.get(0).getHistory());

            assertEquals(List.of(MessageReply.builder()
                                     .messageContent("This is a reply message")
                                     .isUrgent(YES)
                                     .senderName(String.format("%s, %s", USER_NAME, expectedUserRoleLabel))
                                     .senderRoleType(expectedSenderRoleCategory)
                                     .recipientRoleType(RolePool.ADMIN)
                                     .sentTime(NOW)
                                     .build()), actualMessageHistory);
        }

        @Test
        public void should_returnExpectedMessage_forHearingCentreTeamLeader() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            RolePool expectedSenderRoleCategory = RolePool.ADMIN;
            String expectedUserRoleLabel = "Hearing Centre Team Leader";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                                                 RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                                                 RoleAssignmentResponse.builder().roleName("hearing-centre-admin").roleLabel(
                                                     "Hearing Centre Administrator").roleCategory("ADMIN").build(),
                                                 RoleAssignmentResponse.builder().roleName("hearing-centre-team-leader").roleLabel(
                                                     "Hearing Centre Team Leader").roleCategory("ADMIN").build()
                                             )
                )
            );

            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(message));

            List<Element<Message>> actual = messageService.addMessage(
                messages,
                MESSAGE_METADATA,
                MESSAGE_CONTENT,
                USER_AUTH
            );

            assertEquals(List.of(
                message,
                buildExpectedMessage(expectedSenderRoleCategory, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forCtsc() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            String expectedUserRoleLabel = "CTSC";
            RolePool expectedSenderRoleCategory = RolePool.ADMIN;
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                                                 RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                                                 RoleAssignmentResponse.builder().roleName("ctsc").roleLabel("CTSC").roleCategory("ADMIN").build()
                                             )
                ));

            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(message));

            List<Element<Message>> actual = messageService.addMessage(
                messages,
                MESSAGE_METADATA,
                MESSAGE_CONTENT,
                USER_AUTH
            );

            assertEquals(List.of(
                message,
                buildExpectedMessage(expectedSenderRoleCategory, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forCtscTeamLeader() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            RolePool expectedSenderRoleCategory = RolePool.ADMIN;
            String expectedUserRoleLabel = "CTSC Team Leader";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                                                 RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                                                 RoleAssignmentResponse.builder().roleName("ctsc").roleLabel("CTSC").roleCategory("ADMIN").build(),
                                                 RoleAssignmentResponse.builder().roleName("ctsc-team-leader").roleLabel("CTSC Team Leader").roleCategory(
                                                     "ADMIN").build()
                                             )
                )
            );

            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(message));

            List<Element<Message>> actual = messageService.addMessage(
                messages,
                MESSAGE_METADATA,
                MESSAGE_CONTENT,
                USER_AUTH
            );

            assertEquals(List.of(
                message,
                buildExpectedMessage(expectedSenderRoleCategory, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forTribunalCaseworker() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            RolePool expectedSenderRoleCategory = RolePool.LEGAL_OPERATIONS;
            String expectedUserRoleLabel = "Tribunal Caseworker";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                                                 RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                                                 RoleAssignmentResponse.builder().roleName("tribunal-caseworker").roleLabel("Tribunal Caseworker").roleCategory(
                                                     "LEGAL_OPERATIONS").build()
                                             )
                )
            );

            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(message));

            List<Element<Message>> actual = messageService.addMessage(
                messages,
                MESSAGE_METADATA,
                MESSAGE_CONTENT,
                USER_AUTH
            );

            assertEquals(List.of(
                message,
                buildExpectedMessage(expectedSenderRoleCategory, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forSeniorTribunalCaseworker() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            RolePool expectedSenderRoleCategory = RolePool.LEGAL_OPERATIONS;
            String expectedUserRoleLabel = "Senior Tribunal Caseworker";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                                                 RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                                                 RoleAssignmentResponse.builder().roleName("tribunal-caseworker").roleLabel("Tribunal Caseworker").roleCategory(
                                                     "LEGAL_OPERATIONS").build(),
                                                 RoleAssignmentResponse.builder().roleName("senior-tribunal-caseworker").roleLabel(
                                                     "Senior Tribunal Caseworker").roleCategory("LEGAL_OPERATIONS").build()
                                             )
                )
            );

            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(message));

            List<Element<Message>> actual = messageService.addMessage(
                messages,
                MESSAGE_METADATA,
                MESSAGE_CONTENT,
                USER_AUTH
            );

            assertEquals(List.of(
                message,
                buildExpectedMessage(expectedSenderRoleCategory, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forNationalBusinessCentre() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            RolePool expectedSenderRoleCategory = RolePool.ADMIN;
            String expectedUserRoleLabel = "National Business Centre";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                                                 RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                                                 RoleAssignmentResponse.builder().roleName("national-business-centre").roleLabel(
                                                     "National Business Centre").roleCategory("ADMIN").build()
                                             )
                )
            );

            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(message));

            List<Element<Message>> actual = messageService.addMessage(
                messages,
                MESSAGE_METADATA,
                MESSAGE_CONTENT,
                USER_AUTH
            );

            assertEquals(List.of(
                message,
                buildExpectedMessage(expectedSenderRoleCategory, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forNationalBusinessCentreTeamLeader() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            RolePool expectedSenderRoleCategory = RolePool.ADMIN;
            String expectedUserRoleLabel = "NBC Team Leader";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                                                 RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                                                 RoleAssignmentResponse.builder().roleName("national-business-centre").roleLabel(
                                                     "National Business Centre").roleCategory("ADMIN").build(),
                                                 RoleAssignmentResponse.builder().roleName("nbc-team-leader").roleLabel("NBC Team Leader").roleCategory(
                                                     "ADMIN").build()
                                             )
                )
            );

            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(message));

            List<Element<Message>> actual = messageService.addMessage(
                messages,
                MESSAGE_METADATA,
                MESSAGE_CONTENT,
                USER_AUTH
            );

            assertEquals(List.of(
                message,
                buildExpectedMessage(expectedSenderRoleCategory, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forDistrictJudge() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            RolePool expectedSenderRoleCategory = RolePool.JUDICIAL;
            String expectedUserRoleLabel = "District Judge";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                                                 RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                                                 RoleAssignmentResponse.builder().roleName("judge").roleLabel("Judge").roleCategory("JUDICIAL").build(),
                                                 RoleAssignmentResponse.builder().roleName("district-judge").roleLabel("District Judge").roleCategory(
                                                     "JUDICIAL").build()
                                             )
                )
            );

            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(message));

            List<Element<Message>> actual = messageService.addMessage(
                messages,
                MESSAGE_METADATA,
                MESSAGE_CONTENT,
                USER_AUTH
            );

            assertEquals(List.of(
                message,
                buildExpectedMessage(expectedSenderRoleCategory, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forCircuitJudge() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            RolePool expectedSenderRoleCategory = RolePool.JUDICIAL;
            String expectedUserRoleLabel = "Circuit Judge";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                                                 RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                                                 RoleAssignmentResponse.builder().roleName("judge").roleLabel("Judge").roleCategory("JUDICIAL").build(),
                                                 RoleAssignmentResponse.builder().roleName("circuit-judge").roleLabel("Circuit Judge").roleCategory(
                                                     "JUDICIAL").build()
                                             )
                )
            );

            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(message));

            List<Element<Message>> actual = messageService.addMessage(
                messages,
                MESSAGE_METADATA,
                MESSAGE_CONTENT,
                USER_AUTH
            );

            assertEquals(List.of(
                message,
                buildExpectedMessage(expectedSenderRoleCategory, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forJudge() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            RolePool expectedSenderRoleCategory = RolePool.JUDICIAL;
            String expectedUserRoleLabel = "Judge";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                                                 RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                                                 RoleAssignmentResponse.builder().roleName("judge").roleLabel("Judge").roleCategory("JUDICIAL").build()
                                             )
                )
            );

            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(message));

            List<Element<Message>> actual = messageService.addMessage(
                messages,
                MESSAGE_METADATA,
                MESSAGE_CONTENT,
                USER_AUTH
            );

            assertEquals(List.of(
                message,
                buildExpectedMessage(expectedSenderRoleCategory, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        private RoleAssignmentServiceResponse buildRoleAssignmentsResponse(List<RoleAssignmentResponse> roleAssignments) {
            return RoleAssignmentServiceResponse.builder()
                .roleAssignmentResponse(roleAssignments)
                .build();
        }

        private Message buildExpectedMessage(RolePool expectedUserRole, String expectedUserRoleLabel) {
            return Message.builder()
                .messageContent(MESSAGE_CONTENT)
                .sentTime(NOW)
                .updatedTime(NOW)
                .subjectType(MESSAGE_METADATA.getSubject())
                .headerSubject(MESSAGE_METADATA.getSubject().getLabel())
                .contentSubject(MESSAGE_METADATA.getSubject().getLabel())
                .isUrgent(MESSAGE_METADATA.getIsUrgent())
                .recipientRoleType(RolePool.JUDICIAL)
                .senderName(String.format("%s, %s", USER_NAME, expectedUserRoleLabel))
                .senderRoleType(expectedUserRole)
                .build();
        }

    }

    @Nested
    class AddReplyToMessage {

        @Test
        public void shouldAddMessageReplyToMessageHistory_asHearingCentreAdmin() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            RolePool expectedSenderRoleCategory = RolePool.ADMIN;
            String expectedUserRoleLabel = "Hearing Centre Administrator";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                    RoleAssignmentResponse.builder().roleName("hearing-centre-admin").roleLabel(
                        "Hearing Centre Administrator").roleCategory("ADMIN").build())
                )
            );

            Element<Message> existingMessage = element(message);
            List<Element<Message>> messages = new ArrayList<>();
            messages.add(existingMessage);

            MessageReply messageReply = MessageReply.builder().isUrgent(YES).messageContent("This is a reply message").build();

            List<Message> actualMessages = unwrapElements(messageService.addReplyToMessage(
                messages,
                existingMessage.getId().toString(),
                messageReply,
                USER_AUTH
            ));

            List<MessageReply> actualMessageHistory = unwrapElements(actualMessages.get(0).getHistory());

            assertEquals(List.of(MessageReply.builder()
                                     .messageContent("This is a reply message")
                                     .isUrgent(YES)
                                     .senderName(String.format("%s, %s", USER_NAME, expectedUserRoleLabel))
                                     .senderRoleType(expectedSenderRoleCategory)
                                     .recipientRoleType(RolePool.ADMIN)
                                     .sentTime(NOW)
                                     .build()), actualMessageHistory);
        }

        @Test
        public void shouldAddMessageReplyToMessageHistory_forHearingCentreTeamLeader() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            RolePool expectedSenderRoleCategory = RolePool.ADMIN;
            String expectedUserRoleLabel = "Hearing Centre Team Leader";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                                                 RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                                                 RoleAssignmentResponse.builder().roleName("hearing-centre-admin").roleLabel(
                                                     "Hearing Centre Administrator").roleCategory("ADMIN").build(),
                                                 RoleAssignmentResponse.builder().roleName("hearing-centre-team-leader").roleLabel(
                                                     "Hearing Centre Team Leader").roleCategory("ADMIN").build()
                                             )
                )
            );

            Element<Message> existingMessage = element(message);
            List<Element<Message>> messages = new ArrayList<>();
            messages.add(existingMessage);

            MessageReply messageReply = MessageReply.builder().isUrgent(YES).messageContent("This is a reply message").build();

            List<Message> actualMessages = unwrapElements(messageService.addReplyToMessage(
                messages,
                existingMessage.getId().toString(),
                messageReply,
                USER_AUTH
            ));

            List<MessageReply> actualMessageHistory = unwrapElements(actualMessages.get(0).getHistory());

            assertEquals(List.of(MessageReply.builder()
                                     .messageContent("This is a reply message")
                                     .isUrgent(YES)
                                     .senderName(String.format("%s, %s", USER_NAME, expectedUserRoleLabel))
                                     .senderRoleType(expectedSenderRoleCategory)
                                     .recipientRoleType(RolePool.ADMIN)
                                     .sentTime(NOW)
                                     .build()), actualMessageHistory);
        }

        @Test
        public void shouldAddMessageReplyToMessageHistory_forCtsc() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            RolePool expectedSenderRoleCategory = RolePool.ADMIN;
            String expectedUserRoleLabel = "CTSC";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                                                 RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                                                 RoleAssignmentResponse.builder().roleName("ctsc").roleLabel("CTSC").roleCategory("ADMIN").build()
                                             )
                )
            );

            Element<Message> existingMessage = element(message);
            List<Element<Message>> messages = new ArrayList<>();
            messages.add(existingMessage);

            MessageReply messageReply = MessageReply.builder().isUrgent(YES).messageContent("This is a reply message").build();

            List<Message> actualMessages = unwrapElements(messageService.addReplyToMessage(
                messages,
                existingMessage.getId().toString(),
                messageReply,
                USER_AUTH
            ));

            List<MessageReply> actualMessageHistory = unwrapElements(actualMessages.get(0).getHistory());

            assertEquals(List.of(MessageReply.builder()
                                     .messageContent("This is a reply message")
                                     .isUrgent(YES)
                                     .senderName(String.format("%s, %s", USER_NAME, expectedUserRoleLabel))
                                     .senderRoleType(expectedSenderRoleCategory)
                                     .recipientRoleType(RolePool.ADMIN)
                                     .sentTime(NOW)
                                     .build()), actualMessageHistory);
        }

        @Test
        public void shouldAddMessageReplyToMessageHistory_forCtscTeamLeader() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            RolePool expectedSenderRoleCategory = RolePool.ADMIN;
            String expectedUserRoleLabel = "CTSC Team Leader";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                                                 RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                                                 RoleAssignmentResponse.builder().roleName("ctsc").roleLabel("CTSC").roleCategory("ADMIN").build(),
                                                 RoleAssignmentResponse.builder().roleName("ctsc-team-leader").roleLabel("CTSC Team Leader").roleCategory(
                                                     "ADMIN").build()
                                             )
                )
            );

            Element<Message> existingMessage = element(message);
            List<Element<Message>> messages = new ArrayList<>();
            messages.add(existingMessage);

            MessageReply messageReply = MessageReply.builder().isUrgent(YES).messageContent("This is a reply message").build();

            List<Message> actualMessages = unwrapElements(messageService.addReplyToMessage(
                messages,
                existingMessage.getId().toString(),
                messageReply,
                USER_AUTH
            ));

            List<MessageReply> actualMessageHistory = unwrapElements(actualMessages.get(0).getHistory());

            assertEquals(List.of(MessageReply.builder()
                                     .messageContent("This is a reply message")
                                     .isUrgent(YES)
                                     .senderName(String.format("%s, %s", USER_NAME, expectedUserRoleLabel))
                                     .senderRoleType(expectedSenderRoleCategory)
                                     .recipientRoleType(RolePool.ADMIN)
                                     .sentTime(NOW)
                                     .build()), actualMessageHistory);
        }

        @Test
        public void shouldAddMessageReplyToMessageHistory_forTribunalCaseworker() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            RolePool expectedSenderRoleCategory = RolePool.LEGAL_OPERATIONS;
            String expectedUserRoleLabel = "Tribunal Caseworker";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                                                 RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                                                 RoleAssignmentResponse.builder().roleName("tribunal-caseworker").roleLabel("Tribunal Caseworker").roleCategory(
                                                     "LEGAL_OPERATIONS").build()
                                             )
                )
            );

            Element<Message> existingMessage = element(message);
            List<Element<Message>> messages = new ArrayList<>();
            messages.add(existingMessage);

            MessageReply messageReply = MessageReply.builder().isUrgent(YES).messageContent("This is a reply message").build();

            List<Message> actualMessages = unwrapElements(messageService.addReplyToMessage(
                messages,
                existingMessage.getId().toString(),
                messageReply,
                USER_AUTH
            ));

            List<MessageReply> actualMessageHistory = unwrapElements(actualMessages.get(0).getHistory());

            assertEquals(List.of(MessageReply.builder()
                                     .messageContent("This is a reply message")
                                     .isUrgent(YES)
                                     .senderName(String.format("%s, %s", USER_NAME, expectedUserRoleLabel))
                                     .senderRoleType(expectedSenderRoleCategory)
                                     .recipientRoleType(RolePool.ADMIN)
                                     .sentTime(NOW)
                                     .build()), actualMessageHistory);
        }

        @Test
        public void shouldAddMessageReplyToMessageHistory_forSeniorTribunalCaseworker() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            RolePool expectedSenderRoleCategory = RolePool.LEGAL_OPERATIONS;
            String expectedUserRoleLabel = "Senior Tribunal Caseworker";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                                                 RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                                                 RoleAssignmentResponse.builder().roleName("tribunal-caseworker").roleLabel("Tribunal Caseworker").roleCategory(
                                                     "LEGAL_OPERATIONS").build(),
                                                 RoleAssignmentResponse.builder().roleName("senior-tribunal-caseworker").roleLabel(
                                                     "Senior Tribunal Caseworker").roleCategory("LEGAL_OPERATIONS").build()
                                             )
                )
            );

            Element<Message> existingMessage = element(message);
            List<Element<Message>> messages = new ArrayList<>();
            messages.add(existingMessage);

            MessageReply messageReply = MessageReply.builder().isUrgent(YES).messageContent("This is a reply message").build();

            List<Message> actualMessages = unwrapElements(messageService.addReplyToMessage(
                messages,
                existingMessage.getId().toString(),
                messageReply,
                USER_AUTH
            ));

            List<MessageReply> actualMessageHistory = unwrapElements(actualMessages.get(0).getHistory());

            assertEquals(List.of(MessageReply.builder()
                                     .messageContent("This is a reply message")
                                     .isUrgent(YES)
                                     .senderName(String.format("%s, %s", USER_NAME, expectedUserRoleLabel))
                                     .senderRoleType(expectedSenderRoleCategory)
                                     .recipientRoleType(RolePool.ADMIN)
                                     .sentTime(NOW)
                                     .build()), actualMessageHistory);
        }

        @Test
        public void shouldAddMessageReplyToMessageHistory_forDistrictJudge() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            RolePool expectedSenderRoleCategory = RolePool.JUDICIAL;
            String expectedUserRoleLabel = "District Judge";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                                                 RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                                                 RoleAssignmentResponse.builder().roleName("judge").roleLabel("Judge").roleCategory("JUDICIAL").build(),
                                                 RoleAssignmentResponse.builder().roleName("district-judge").roleLabel("District Judge").roleCategory(
                                                     "JUDICIAL").build()
                                             )
                )
            );

            Element<Message> existingMessage = element(message);
            List<Element<Message>> messages = new ArrayList<>();
            messages.add(existingMessage);

            MessageReply messageReply = MessageReply.builder().isUrgent(YES).messageContent("This is a reply message").build();

            List<Message> actualMessages = unwrapElements(messageService.addReplyToMessage(
                messages,
                existingMessage.getId().toString(),
                messageReply,
                USER_AUTH
            ));

            List<MessageReply> actualMessageHistory = unwrapElements(actualMessages.get(0).getHistory());

            assertEquals(List.of(MessageReply.builder()
                                     .messageContent("This is a reply message")
                                     .isUrgent(YES)
                                     .senderName(String.format("%s, %s", USER_NAME, expectedUserRoleLabel))
                                     .senderRoleType(expectedSenderRoleCategory)
                                     .recipientRoleType(RolePool.ADMIN)
                                     .sentTime(NOW)
                                     .build()), actualMessageHistory);
        }

        @Test
        public void shouldAddMessageReplyToMessageHistory_forCircuitJudge() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            RolePool expectedSenderRoleCategory = RolePool.JUDICIAL;
            String expectedUserRoleLabel = "Circuit Judge";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                                                 RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                                                 RoleAssignmentResponse.builder().roleName("judge").roleLabel("Judge").roleCategory("JUDICIAL").build(),
                                                 RoleAssignmentResponse.builder().roleName("circuit-judge").roleLabel("Circuit Judge").roleCategory(
                                                     "JUDICIAL").build()
                                             )
                )
            );

            Element<Message> existingMessage = element(message);
            List<Element<Message>> messages = new ArrayList<>();
            messages.add(existingMessage);

            MessageReply messageReply = MessageReply.builder().isUrgent(YES).messageContent("This is a reply message").build();

            List<Message> actualMessages = unwrapElements(messageService.addReplyToMessage(
                messages,
                existingMessage.getId().toString(),
                messageReply,
                USER_AUTH
            ));

            List<MessageReply> actualMessageHistory = unwrapElements(actualMessages.get(0).getHistory());

            assertEquals(List.of(MessageReply.builder()
                                     .messageContent("This is a reply message")
                                     .isUrgent(YES)
                                     .senderName(String.format("%s, %s", USER_NAME, expectedUserRoleLabel))
                                     .senderRoleType(expectedSenderRoleCategory)
                                     .recipientRoleType(RolePool.ADMIN)
                                     .sentTime(NOW)
                                     .build()), actualMessageHistory);
        }

        @Test
        public void shouldAddMessageReplyToMessageHistory_forJudge() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            RolePool expectedSenderRoleCategory = RolePool.JUDICIAL;
            String expectedUserRoleLabel = "Judge";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                                                 RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                                                 RoleAssignmentResponse.builder().roleName("judge").roleLabel("Judge").roleCategory("JUDICIAL").build()
                                             )
                )
            );

            Element<Message> existingMessage = element(message);
            List<Element<Message>> messages = new ArrayList<>();
            messages.add(existingMessage);

            MessageReply messageReply = MessageReply.builder().isUrgent(YES).messageContent("This is a reply message").build();

            List<Message> actualMessages = unwrapElements(messageService.addReplyToMessage(
                messages,
                existingMessage.getId().toString(),
                messageReply,
                USER_AUTH
            ));

            List<MessageReply> actualMessageHistory = unwrapElements(actualMessages.get(0).getHistory());

            assertEquals(List.of(MessageReply.builder()
                                     .messageContent("This is a reply message")
                                     .isUrgent(YES)
                                     .senderName(String.format("%s, %s", USER_NAME, expectedUserRoleLabel))
                                     .senderRoleType(expectedSenderRoleCategory)
                                     .recipientRoleType(RolePool.ADMIN)
                                     .sentTime(NOW)
                                     .build()), actualMessageHistory);
        }

        @Test
        public void shouldAddMessageReplyToExistingHistory_withTwoExistingReplies() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                    RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                    RoleAssignmentResponse.builder().roleName("hearing-centre-admin").roleLabel(
                        "Hearing Centre Administrator").roleCategory("ADMIN").build(),
                    RoleAssignmentResponse.builder().roleName("hearing-centre-team-leader").roleLabel(
                        "Hearing Centre Team Leader").roleCategory("ADMIN").build()
                ))
            );

            RolePool expectedSenderRoleCategory = RolePool.ADMIN;
            MessageReply firstReply = MessageReply.builder()
                .messageContent("First reply")
                .isUrgent(NO)
                .senderName("John Doe, Hearing Centre Team Leader")
                .senderRoleType(expectedSenderRoleCategory)
                .sentTime(NOW.minusDays(2))
                .build();

            MessageReply secondReplyReply = MessageReply.builder()
                .messageContent("Second reply")
                .isUrgent(YES)
                .senderName("Jane Smith, Hearing Centre Team Leader")
                .senderRoleType(expectedSenderRoleCategory)
                .sentTime(NOW.minusDays(1))
                .build();

            List<Element<MessageReply>> history = new ArrayList<>();
            history.add(element(firstReply));
            history.add(element(secondReplyReply));
            Message existingMessage = message.toBuilder()
                .history(history).build();

            Element<Message> existingMessageElement = element(existingMessage);
            List<Element<Message>> messages = new ArrayList<>();
            messages.add(existingMessageElement);

            MessageReply newReply = MessageReply.builder().isUrgent(YES).messageContent("This is a new reply message").build();

            List<Message> actualMessages = unwrapElements(messageService.addReplyToMessage(
                messages,
                existingMessageElement.getId().toString(),
                newReply,
                USER_AUTH
            ));

            List<MessageReply> actualMessageHistory = unwrapElements(actualMessages.get(0).getHistory());

            List<MessageReply> expectedMessageHistory = List.of(
                firstReply,
                secondReplyReply,
                MessageReply.builder()
                    .messageContent("This is a new reply message")
                    .isUrgent(YES)
                    .senderName(String.format("%s, %s", USER_NAME, "Hearing Centre Team Leader"))
                    .senderRoleType(expectedSenderRoleCategory)
                    .recipientRoleType(RolePool.ADMIN)
                    .sentTime(NOW)
                    .build()
            );

            assertEquals(expectedMessageHistory, actualMessageHistory);
        }

    }

    @Nested
    class GetMessageByIdTests {

        private List<Element<Message>> messages;
        private Element<Message> message1;
        private Element<Message> message2;

        @BeforeEach
        void setUp() {
            message1 = element(Message.builder().messageContent("Message content 1").build());
            message2 = element(Message.builder().messageContent("Message content 2").build());
            messages = List.of(message1, message2);
        }

        @Test
        public void shouldReturnMessage_whenMessageIdMatches() {
            String messageId = message1.getId().toString();

            Element<Message> result = messageService.getMessageById(messages, messageId);

            assertEquals(message1, result);
        }

        @Test
        public void shouldReturnNull_whenMessageIdDoesNotMatch() {
            String nonExistentMessageId = UUID.randomUUID().toString();

            Element<Message> result = messageService.getMessageById(messages, nonExistentMessageId);

            assertNull(result);
        }
    }

    @Nested
    class CreateMessageSelectionListTests {

        private List<Element<Message>> messages;
        private Element<Message> message1;
        private Element<Message> message2;

        @BeforeEach
        void setUp() {
            message1 = element(Message.builder()
                                   .headerSubject("Subject 1")
                                   .sentTime(LocalDateTime.of(2024, 11, 14, 10, 30, 0))
                                   .build());

            message2 = element(Message.builder()
                                   .headerSubject("Subject 2")
                                   .sentTime(LocalDateTime.of(2024, 11, 14, 12, 45, 0))
                                   .build());

            messages = List.of(message1, message2);
        }

        @Test
        public void shouldCreateMessageSelectionList_withCorrectFormat() {
            DynamicList result = messageService.createMessageSelectionList(messages);

            assertEquals(DynamicList.builder()
                             .listItems(List.of(
                                 DynamicListElement.builder()
                                     .code(message1.getId().toString())
                                     .label(
                                         String.format(
                                             "%s, 14 Nov 2024, 10:30:00 am",
                                             message1.getValue().getHeaderSubject()
                                         ))
                                     .build(),
                                 DynamicListElement.builder()
                                     .code(message2.getId().toString())
                                     .label(String.format(
                                         "%s, 14 Nov 2024, 12:45:00 pm",
                                         message2.getValue().getHeaderSubject()
                                     ))
                                     .build()
                             ))
                             .build(), result);
        }

        @Test
        public void shouldReturnEmptyList_whenNoMessages() {
            List<Element<Message>> emptyMessages = new ArrayList<>();

            DynamicList result = messageService.createMessageSelectionList(emptyMessages);

            assertNotNull(result);
            assertTrue(result.getListItems().isEmpty());
        }

    }

    @Nested
    class RenderMessageTableListTests {

        private Element<Message> message;

        @BeforeEach
        void setUp() {

            message = element(Message.builder()
                                  .sentTime(LocalDateTime.of(2024, 11, 14, 10, 30, 0))
                                  .senderName("Sender 1")
                                  .senderRoleType(RolePool.ADMIN)
                                  .isUrgent(YES)
                                  .subjectType(OTHER)
                                  .contentSubject("Subject 1")
                                  .messageContent("This is the original message.")
                                  .build());
        }

        @Test
        public void shouldCallTableMarkupService_withExpectedRowData_withoutReplies() {
            messageService.renderMessageTableList(message);

            verify(tableMarkupService, times(1)).buildTableMarkUp(Map.of(
                "Date and time sent", "14 Nov 2024, 10:30:00 AM",
                "Sender's name", "Sender 1",
                "Recipient role", "Court staff",
                "Urgency", "Yes",
                "What is it about", "Other",
                "Subject", "Subject 1",
                "Message details", "This is the original message."
            ));
        }

        @Test
        public void shouldCallTableMarkupService_withExpectedRowData_withReplies() {
            message.getValue().getHistory().add(element(MessageReply.builder()
                                                            .messageContent("This is a reply message")
                                                            .isUrgent(NO)
                                                            .senderName("Sender 2")
                                                            .senderRoleType(RolePool.ADMIN)
                                                            .sentTime(NOW.plusHours(5))
                                                            .build()));

            messageService.renderMessageTableList(message);

            verify(tableMarkupService, times(1)).buildTableMarkUp(Map.of(
                "Date and time sent", "14 Nov 2024, 10:30:00 AM",
                "Sender's name", "Sender 1",
                "Recipient role", RolePool.ADMIN.getLabel(),
                "Urgency", "Yes",
                "What is it about", "Other",
                "Subject", "Subject 1",
                "Message details", "This is the original message."
            ));
        }
    }

    private RoleAssignmentServiceResponse buildRoleAssignmentsResponse(List<RoleAssignmentResponse> roleAssignments) {
        return RoleAssignmentServiceResponse.builder()
            .roleAssignmentResponse(roleAssignments)
            .build();
    }
}
