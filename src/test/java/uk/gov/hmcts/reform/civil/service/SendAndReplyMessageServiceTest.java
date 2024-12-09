package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.stream.Stream;

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
import static uk.gov.hmcts.reform.civil.enums.sendandreply.SubjectOption.OTHER;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

@ExtendWith(SpringExtension.class)
class SendAndReplyMessageServiceTest {

    private static final String USER_AUTH = "auth";
    private static final String USER_NAME = "Test User";

    private static final UserDetails USER_DETAILS = UserDetails.builder()
        .id("uid")
        .forename("Test")
        .surname("User")
        .build();

    private static final SendMessageMetadata MESSAGE_METADATA = SendMessageMetadata.builder()
        .subjectType(OTHER)
        .subject(OTHER.getLabel())
        .recipientRoleType(CIRCUIT_JUDGE)
        .isUrgent(YES)
        .build();
    private static final String MESSAGE_CONTENT = "Message Content";
    private static final LocalDateTime NOW = LocalDateTime.of(2014, 11, 1, 0, 0, 0);

    private Message message;

    @Mock
    private UserService userService;

    @Mock
    private RoleAssignmentsService roleAssignmentService;

    @Mock
    private TableMarkupService tableMarkupService;

    @Mock
    private Time time;

    private LocalDateTime updatedDateTime = LocalDateTime.of(2024, 1, 1, 0, 0, 0);

    @InjectMocks
    private SendAndReplyMessageService messageService;

    @BeforeEach
    void setupTest() {
        when(time.now()).thenReturn(NOW);
        when(tableMarkupService.buildTableMarkUp(any())).thenReturn("<div>Some markup</div>");

        message = Message.builder()
            .updatedTime(updatedDateTime)
            .sentTime(LocalDateTime.of(2024, 1, 1, 0, 0, 0))
            .subject("Subject")
            .senderRoleType(RolePool.ADMIN)
            .messageContent("Existing message")
            .build();
    }

    @Nested
    class AddMessage {
        @Test
        void should_returnExpectedMessage_whenExistingMessagesAreNull() {
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
        void should_returnExpectedMessage_forHearingCentreTeamLeader() {
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
        void should_returnExpectedMessage_forCtsc() {
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
        void should_returnExpectedMessage_forCtscTeamLeader() {
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
        void should_returnExpectedMessage_forTribunalCaseworker() {
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
        void should_returnExpectedMessage_forSeniorTribunalCaseworker() {
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
        void should_returnExpectedMessage_forNationalBusinessCentre() {
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
        void should_returnExpectedMessage_forNationalBusinessCentreTeamLeader() {
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
        void should_returnExpectedMessage_forDistrictJudge() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            RolePool expectedSenderRoleCategory = RolePool.JUDICIAL_DISTRICT;
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
        void should_returnExpectedMessage_forCircuitJudge() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            RolePool expectedSenderRoleCategory = RolePool.JUDICIAL_CIRCUIT;
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
        void should_returnExpectedMessage_forJudge() {
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
                .subjectType(MESSAGE_METADATA.getSubjectType())
                .subject(MESSAGE_METADATA.getSubjectType().getLabel())
                .isUrgent(MESSAGE_METADATA.getIsUrgent())
                .recipientRoleType(RolePool.JUDICIAL_CIRCUIT)
                .senderName(String.format("%s, %s", USER_NAME, expectedUserRoleLabel))
                .senderRoleType(expectedUserRole)
                .build();
        }

    }

    @Nested
    class AddReplyToMessage {

        static Stream<Arguments> provideUserData() {
            return Stream.of(
                Arguments.of(RolePool.JUDICIAL,
                             RolePool.ADMIN,
                             List.of(RoleAssignmentResponse.builder().roleName("hearing-centre-admin").roleLabel("Hearing Centre Administrator").roleCategory("ADMIN").build()),
                             "Hearing Centre Administrator",
                             "Judge"
                ),
                Arguments.of(RolePool.JUDICIAL,
                             RolePool.ADMIN,
                             List.of(
                                 RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                                 RoleAssignmentResponse.builder().roleName("hearing-centre-admin").roleLabel(
                                     "Hearing Centre Administrator").roleCategory("ADMIN").build(),
                                 RoleAssignmentResponse.builder().roleName("hearing-centre-team-leader").roleLabel(
                                     "Hearing Centre Team Leader").roleCategory("ADMIN").build()
                             ),
                             "Hearing Centre Team Leader",
                             "Judge"),
                Arguments.of(RolePool.JUDICIAL,
                             RolePool.ADMIN,
                             List.of(
                                 RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                                 RoleAssignmentResponse.builder().roleName("ctsc").roleLabel("CTSC").roleCategory("ADMIN").build()
                             ),
                             "CTSC",
                             "Judge"
                ),
                Arguments.of(RolePool.JUDICIAL,
                             RolePool.ADMIN,
                             List.of(
                                 RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                                 RoleAssignmentResponse.builder().roleName("ctsc").roleLabel("CTSC").roleCategory("ADMIN").build(),
                                 RoleAssignmentResponse.builder().roleName("ctsc-team-leader").roleLabel("CTSC Team Leader").roleCategory(
                                     "ADMIN").build()
                             ),
                             "CTSC Team Leader",
                             "Judge"),
                Arguments.of(RolePool.JUDICIAL,
                             RolePool.LEGAL_OPERATIONS,
                             List.of(
                                 RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                                 RoleAssignmentResponse.builder().roleName("tribunal-caseworker").roleLabel("Tribunal Caseworker").roleCategory(
                                     "LEGAL_OPERATIONS").build()
                             ),
                             "Tribunal Caseworker",
                             "Judge"),
                Arguments.of(RolePool.JUDICIAL,
                             RolePool.LEGAL_OPERATIONS,
                             List.of(
                                 RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                                 RoleAssignmentResponse.builder().roleName("tribunal-caseworker").roleLabel("Tribunal Caseworker").roleCategory(
                                     "LEGAL_OPERATIONS").build(),
                                 RoleAssignmentResponse.builder().roleName("senior-tribunal-caseworker").roleLabel(
                                     "Senior Tribunal Caseworker").roleCategory("LEGAL_OPERATIONS").build()
                             ),
                             "Senior Tribunal Caseworker",
                             "Judge"),
                Arguments.of(RolePool.ADMIN,
                             RolePool.JUDICIAL_DISTRICT,
                             List.of(
                                 RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                                 RoleAssignmentResponse.builder().roleName("judge").roleLabel("Judge").roleCategory("JUDICIAL").build(),
                                 RoleAssignmentResponse.builder().roleName("district-judge").roleLabel("District Judge").roleCategory(
                                     "JUDICIAL").build()
                             ),
                             "District Judge",
                             "Hearing centre admin"),
                Arguments.of(RolePool.ADMIN,
                             RolePool.JUDICIAL_CIRCUIT,
                             List.of(
                                 RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                                 RoleAssignmentResponse.builder().roleName("judge").roleLabel("Judge").roleCategory("JUDICIAL").build(),
                                 RoleAssignmentResponse.builder().roleName("circuit-judge").roleLabel("Circuit Judge").roleCategory(
                                     "JUDICIAL").build()
                             ),
                             "Circuit Judge",
                             "Hearing centre admin"),
                Arguments.of(RolePool.ADMIN,
                             RolePool.JUDICIAL,
                             List.of(
                                 RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                                 RoleAssignmentResponse.builder().roleName("judge").roleLabel("Judge").roleCategory("JUDICIAL").build()
                             ),
                             "Judge",
                             "Hearing centre admin")
            );
        }

        @ParameterizedTest
        @MethodSource("provideUserData")
        void shouldAddMessageReplyasBaseAndBaseToMessageHistory(RolePool originalSender,
                                                                                     RolePool currentSender,
                                                                                     List<RoleAssignmentResponse> roleAssignmentResponses,
                                                                                     String newRoleLabel, String oldRoleLabel) {
            LocalDateTime updatedTime = LocalDateTime.of(2025, 1, 1, 10, 10);
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);
            when(time.now()).thenReturn(updatedTime);

            String originalUserRoleLabel = oldRoleLabel;
            String newUserRoleLabel = newRoleLabel;
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(roleAssignmentResponses)
            );

            Element<Message> existingMessageToBeChanged = element(message.toBuilder()
                                                           .senderName(String.format("%s, %s", USER_NAME, originalUserRoleLabel))
                                                           .senderRoleType(originalSender)
                                                           .recipientRoleType(currentSender)
                                                           .sentTime(NOW)
                                                           .isUrgent(YES)
                                                           .build());

            List<Element<Message>> messages = new ArrayList<>();
            messages.add(existingMessageToBeChanged);

            MessageReply messageReply = MessageReply.builder().isUrgent(NO)
                .messageContent("This is a reply message")
                .isUrgent(NO)
                .sentTime(updatedDateTime)
                .build();

            Message expectedMessage = message.toBuilder()
                .senderName(String.format("%s, %s", USER_NAME, newUserRoleLabel))
                .senderRoleType(currentSender)
                .recipientRoleType(originalSender)
                .sentTime(NOW)
                .isUrgent(messageReply.getIsUrgent())
                .messageContent(messageReply.getMessageContent())
                .updatedTime(updatedTime)
                .build();

            List<Message> actualMessages = unwrapElements(messageService.addReplyToMessage(
                messages,
                existingMessageToBeChanged.getId().toString(),
                messageReply,
                USER_AUTH
            ));

            List<MessageReply> actualMessageHistory = unwrapElements(actualMessages.get(0).getHistory());

            assertEquals(List.of(MessageReply.builder()
                                     .messageContent("Existing message")
                                     .isUrgent(YES)
                                     .senderName(String.format("%s, %s", USER_NAME, originalUserRoleLabel))
                                     .senderRoleType(originalSender)
                                     .recipientRoleType(currentSender)
                                     .subject("Subject")
                                     .sentTime(updatedDateTime)
                                     .build()), actualMessageHistory);
            assertEquals(expectedMessage, existingMessageToBeChanged.getValue());
        }

        @Test
        void shouldAddMessageReplyToExistingHistory_withTwoExistingReplies() {
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
            MessageReply baseMessage = MessageReply.builder()
                .messageContent("First reply")
                .isUrgent(NO)
                .senderName("John Doe, Hearing Centre Team Leader")
                .senderRoleType(expectedSenderRoleCategory)
                .sentTime(NOW.minusDays(2))
                .build();

            MessageReply firstReply = MessageReply.builder()
                .messageContent("Second reply")
                .isUrgent(YES)
                .senderName("Jane Smith, Hearing Centre Team Leader")
                .senderRoleType(expectedSenderRoleCategory)
                .sentTime(NOW.minusDays(1))
                .build();

            List<Element<MessageReply>> history = new ArrayList<>();
            history.add(element(firstReply));
            history.add(element(baseMessage));

            Message secondReply = message.toBuilder()
                .history(history).build();

            Element<Message> existingMessageElement = element(secondReply);
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
                messageService.buildReplyOutOfMessage(secondReply),
                firstReply,
                baseMessage
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
        void shouldReturnMessage_whenMessageIdMatches() {
            String messageId = message1.getId().toString();

            Element<Message> result = messageService.getMessageById(messages, messageId);

            assertEquals(message1, result);
        }

        @Test
        void shouldReturnNull_whenMessageIdDoesNotMatch() {
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
                                   .subject("Subject 1")
                                   .sentTime(LocalDateTime.of(2024, 11, 14, 10, 30, 0))
                                   .build());

            message2 = element(Message.builder()
                                   .subject("Subject 2")
                                   .sentTime(LocalDateTime.of(2024, 11, 14, 12, 45, 0))
                                   .build());

            messages = List.of(message1, message2);
        }

        @Test
        void shouldCreateMessageSelectionList_withCorrectFormat() {
            DynamicList result = messageService.createMessageSelectionList(messages);

            assertEquals(DynamicList.builder()
                             .listItems(List.of(
                                 DynamicListElement.builder()
                                     .code(message1.getId().toString())
                                     .label(
                                         String.format(
                                             "%s, 14 Nov 2024, 10:30:00 AM",
                                             message1.getValue().getSubject()
                                         ))
                                     .build(),
                                 DynamicListElement.builder()
                                     .code(message2.getId().toString())
                                     .label(String.format(
                                         "%s, 14 Nov 2024, 12:45:00 PM",
                                         message2.getValue().getSubject()
                                     ))
                                     .build()
                             ))
                             .build(), result);
        }

        @Test
        void shouldReturnEmptyList_whenNoMessages() {
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
                                  .updatedTime(LocalDateTime.of(2024, 10, 10, 10, 10, 10))
                                  .senderName("Sender 1")
                                  .senderRoleType(RolePool.ADMIN)
                                  .recipientRoleType(RolePool.JUDICIAL)
                                  .isUrgent(YES)
                                  .subjectType(OTHER)
                                  .subject("Subject 1")
                                  .messageContent("This is the base message.")
                                  .build());
        }

        @Test
        void shouldCallTableMarkupService_withExpectedRowData_withoutReplies() {
            messageService.renderMessageTableList(message);

            verify(tableMarkupService, times(1)).buildTableMarkUp(Map.of(
                "Date and time sent", "10 Oct 2024, 10:10:10 AM",
                "Sender's name", "Sender 1",
                "Recipient role", RolePool.JUDICIAL.getLabel(),
                "Urgency", "Yes",
                "What is it about", "Other",
                "Subject", "Subject 1",
                "Message details", "This is the base message."
            ));
        }

        @Test
        void shouldCallTableMarkupService_withExpectedRowData_withReplies() {
            message.getValue().getHistory().add(element(MessageReply.builder()
                                                            .messageContent("This is the original message")
                                                            .isUrgent(NO)
                                                            .senderName("Sender 2")
                                                            .senderRoleType(RolePool.JUDICIAL)
                                                            .recipientRoleType(RolePool.ADMIN)
                                                            .sentTime(NOW.minusHours(5))
                                                            .build()));

            messageService.renderMessageTableList(message);

            verify(tableMarkupService, times(1)).buildTableMarkUp(Map.of(
                "Date and time sent", "10 Oct 2024, 10:10:10 AM",
                "Sender's name", "Sender 1",
                "Recipient role", RolePool.JUDICIAL.getLabel(),
                "Urgency", "Yes",
                "What is it about", "Other",
                "Subject", "Subject 1",
                "Message details", "This is the base message."
            ));
        }
    }

    private RoleAssignmentServiceResponse buildRoleAssignmentsResponse(List<RoleAssignmentResponse> roleAssignments) {
        return RoleAssignmentServiceResponse.builder()
            .roleAssignmentResponse(roleAssignments)
            .build();
    }
}
