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
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.enums.sendandreply.RolePool;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.sendandreply.Message;
import uk.gov.hmcts.reform.civil.model.sendandreply.MessageReply;
import uk.gov.hmcts.reform.civil.model.sendandreply.SendMessageMetadata;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentResponse;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
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
    private static final UUID MESSAGE_ID = UUID.randomUUID();

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

    private static final List<String> SUPPORTED_ROLES = List.of(
        "wlu-team-leader",
        "wlu-admin",
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

    @Mock
    private UserService userService;

    @Mock
    private RoleAssignmentsService roleAssignmentService;

    @Mock
    private TableMarkupService tableMarkupService;

    @Mock
    private Time time;

    @Mock
    private FeatureToggleService featureToggleService;

    private LocalDateTime updatedDateTime = LocalDateTime.of(2024, 1, 1, 0, 0, 0);

    @InjectMocks
    private SendAndReplyMessageService messageService;

    private CaseData caseData;

    @BeforeEach
    void setupTest() {
        when(time.now()).thenReturn(NOW);
        when(tableMarkupService.buildTableMarkUp(any())).thenReturn("<div>Some markup</div>");
        caseData = CaseDataBuilder.builder().build();

        message = new Message()
            .setUpdatedTime(updatedDateTime)
            .setSentTime(LocalDateTime.of(2024, 1, 1, 0, 0, 0))
            .setSubject("Subject")
            .setSenderRoleType(RolePool.ADMIN)
            .setMessageContent("Existing message");
    }

    @Nested
    class AddMessage {

        @Test
        void should_returnExpectedMessage_whenExistingMessagesAreNull() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            RolePool expectedSenderRoleCategory = RolePool.ADMIN;
            String expectedUserRoleLabel = "Hearing Centre Administrator";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH, SUPPORTED_ROLES)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                    new RoleAssignmentResponse().setRoleName("hearing-centre-admin").setRoleLabel(
                        "Hearing Centre Administrator").setRoleCategory("ADMIN"))
                )
            );

            List<Element<Message>> actual = null;
            try (MockedStatic<UUID> mockedStatic = Mockito.mockStatic(UUID.class)) {
                mockedStatic.when(UUID::randomUUID).thenReturn(MESSAGE_ID);

                actual = messageService.addMessage(
                    null,
                    MESSAGE_METADATA,
                    MESSAGE_CONTENT,
                    USER_AUTH
                );
            }

            assertEquals(
                List.of(buildExpectedMessage(expectedSenderRoleCategory, expectedUserRoleLabel)),
                unwrapElements(actual)
            );
        }

        @Test
        void should_returnExpectedMessage_forHearingCentreTeamLeader() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH, SUPPORTED_ROLES)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                                                 new RoleAssignmentResponse().setRoleName("other").setRoleLabel("Other").setRoleCategory("OTHER"),
                                                 new RoleAssignmentResponse().setRoleName("hearing-centre-admin").setRoleLabel(
                                                     "Hearing Centre Administrator").setRoleCategory("ADMIN"),
                                                 new RoleAssignmentResponse().setRoleName("hearing-centre-team-leader").setRoleLabel(
                                                     "Hearing Centre Team Leader").setRoleCategory("ADMIN")
                                             )
                )
            );
            String expectedUserRoleLabel = "Hearing Centre Team Leader";
            RolePool expectedSenderRoleCategory = RolePool.ADMIN;

            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(message));

            List<Element<Message>> actual = null;
            try (MockedStatic<UUID> mockedStatic = Mockito.mockStatic(UUID.class)) {
                mockedStatic.when(UUID::randomUUID).thenReturn(MESSAGE_ID);

                actual = messageService.addMessage(
                    messages,
                    MESSAGE_METADATA,
                    MESSAGE_CONTENT,
                    USER_AUTH
                );
            }

            assertEquals(List.of(
                message,
                buildExpectedMessage(expectedSenderRoleCategory, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        void should_returnExpectedMessage_forCtsc() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH, SUPPORTED_ROLES)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                                                 new RoleAssignmentResponse().setRoleName("other").setRoleLabel("Other").setRoleCategory("OTHER"),
                                                 new RoleAssignmentResponse().setRoleName("ctsc").setRoleLabel("CTSC").setRoleCategory("ADMIN")
                                             )
                ));
            String expectedUserRoleLabel = "CTSC";
            RolePool expectedSenderRoleCategory = RolePool.ADMIN;
            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(message));

            List<Element<Message>> actual = null;
            try (MockedStatic<UUID> mockedStatic = Mockito.mockStatic(UUID.class)) {
                mockedStatic.when(UUID::randomUUID).thenReturn(MESSAGE_ID);

                actual = messageService.addMessage(
                    messages,
                    MESSAGE_METADATA,
                    MESSAGE_CONTENT,
                    USER_AUTH
                );
            }

            assertEquals(List.of(
                message,
                buildExpectedMessage(expectedSenderRoleCategory, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        void should_returnExpectedMessage_forCtscTeamLeader() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH, SUPPORTED_ROLES)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                                                 new RoleAssignmentResponse().setRoleName("other").setRoleLabel("Other").setRoleCategory("OTHER"),
                                                 new RoleAssignmentResponse().setRoleName("ctsc").setRoleLabel("CTSC").setRoleCategory("ADMIN"),
                                                 new RoleAssignmentResponse().setRoleName("ctsc-team-leader").setRoleLabel("CTSC Team Leader").setRoleCategory(
                                                     "ADMIN")
                                             )
                )
            );
            RolePool expectedSenderRoleCategory = RolePool.ADMIN;
            String expectedUserRoleLabel = "CTSC Team Leader";
            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(message));

            List<Element<Message>> actual = null;
            try (MockedStatic<UUID> mockedStatic = Mockito.mockStatic(UUID.class)) {
                mockedStatic.when(UUID::randomUUID).thenReturn(MESSAGE_ID);

                actual = messageService.addMessage(
                    messages,
                    MESSAGE_METADATA,
                    MESSAGE_CONTENT,
                    USER_AUTH
                );
            }

            assertEquals(List.of(
                message,
                buildExpectedMessage(expectedSenderRoleCategory, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        void should_returnExpectedMessage_forTribunalCaseworker() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH, SUPPORTED_ROLES)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                                                 new RoleAssignmentResponse().setRoleName("other").setRoleLabel("Other").setRoleCategory("OTHER"),
                                                 new RoleAssignmentResponse().setRoleName("tribunal-caseworker").setRoleLabel("Tribunal Caseworker").setRoleCategory(
                                                     "LEGAL_OPERATIONS")
                                             )
                )
            );
            RolePool expectedSenderRoleCategory = RolePool.LEGAL_OPERATIONS;
            String expectedUserRoleLabel = "Tribunal Caseworker";
            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(message));

            List<Element<Message>> actual = null;
            try (MockedStatic<UUID> mockedStatic = Mockito.mockStatic(UUID.class)) {
                mockedStatic.when(UUID::randomUUID).thenReturn(MESSAGE_ID);

                actual = messageService.addMessage(
                    messages,
                    MESSAGE_METADATA,
                    MESSAGE_CONTENT,
                    USER_AUTH
                );
            }

            assertEquals(List.of(
                message,
                buildExpectedMessage(expectedSenderRoleCategory, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        void should_returnExpectedMessage_forSeniorTribunalCaseworker() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH, SUPPORTED_ROLES)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                                                 new RoleAssignmentResponse().setRoleName("other").setRoleLabel("Other").setRoleCategory("OTHER"),
                                                 new RoleAssignmentResponse().setRoleName("tribunal-caseworker").setRoleLabel("Tribunal Caseworker").setRoleCategory(
                                                     "LEGAL_OPERATIONS"),
                                                 new RoleAssignmentResponse().setRoleName("senior-tribunal-caseworker").setRoleLabel(
                                                     "Senior Tribunal Caseworker").setRoleCategory("LEGAL_OPERATIONS")
                                             )
                )
            );
            RolePool expectedSenderRoleCategory = RolePool.LEGAL_OPERATIONS;
            String expectedUserRoleLabel = "Senior Tribunal Caseworker";
            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(message));

            List<Element<Message>> actual = null;
            try (MockedStatic<UUID> mockedStatic = Mockito.mockStatic(UUID.class)) {
                mockedStatic.when(UUID::randomUUID).thenReturn(MESSAGE_ID);

                actual = messageService.addMessage(
                    messages,
                    MESSAGE_METADATA,
                    MESSAGE_CONTENT,
                    USER_AUTH
                );
            }

            assertEquals(List.of(
                message,
                buildExpectedMessage(expectedSenderRoleCategory, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        void should_returnExpectedMessage_forNationalBusinessCentre() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH, SUPPORTED_ROLES)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                                                 new RoleAssignmentResponse().setRoleName("other").setRoleLabel("Other").setRoleCategory("OTHER"),
                                                 new RoleAssignmentResponse().setRoleName("national-business-centre").setRoleLabel(
                                                     "National Business Centre").setRoleCategory("ADMIN")
                                             )
                )
            );

            RolePool expectedSenderRoleCategory = RolePool.ADMIN;
            String expectedUserRoleLabel = "National Business Centre";
            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(message));

            List<Element<Message>> actual = null;
            try (MockedStatic<UUID> mockedStatic = Mockito.mockStatic(UUID.class)) {
                mockedStatic.when(UUID::randomUUID).thenReturn(MESSAGE_ID);

                actual = messageService.addMessage(
                    messages,
                    MESSAGE_METADATA,
                    MESSAGE_CONTENT,
                    USER_AUTH
                );
            }

            assertEquals(List.of(
                message,
                buildExpectedMessage(expectedSenderRoleCategory, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        void should_returnExpectedMessage_forNationalBusinessCentreTeamLeader() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH, SUPPORTED_ROLES)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                                                 new RoleAssignmentResponse().setRoleName("other").setRoleLabel("Other").setRoleCategory("OTHER"),
                                                 new RoleAssignmentResponse().setRoleName("national-business-centre").setRoleLabel(
                                                     "National Business Centre").setRoleCategory("ADMIN"),
                                                 new RoleAssignmentResponse().setRoleName("nbc-team-leader").setRoleLabel("NBC Team Leader").setRoleCategory(
                                                     "ADMIN")
                                             )
                )
            );
            RolePool expectedSenderRoleCategory = RolePool.ADMIN;
            String expectedUserRoleLabel = "NBC Team Leader";
            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(message));

            List<Element<Message>> actual = null;
            try (MockedStatic<UUID> mockedStatic = Mockito.mockStatic(UUID.class)) {
                mockedStatic.when(UUID::randomUUID).thenReturn(MESSAGE_ID);

                actual = messageService.addMessage(
                    messages,
                    MESSAGE_METADATA,
                    MESSAGE_CONTENT,
                    USER_AUTH
                );
            }

            assertEquals(List.of(
                message,
                buildExpectedMessage(expectedSenderRoleCategory, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        void should_returnExpectedMessage_forDistrictJudge() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH, SUPPORTED_ROLES)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                                                 new RoleAssignmentResponse().setRoleName("other").setRoleLabel("Other").setRoleCategory("OTHER"),
                                                 new RoleAssignmentResponse().setRoleName("judge").setRoleLabel("Judge").setRoleCategory("JUDICIAL"),
                                                 new RoleAssignmentResponse().setRoleName("district-judge").setRoleLabel("District Judge").setRoleCategory(
                                                     "JUDICIAL")
                                             )
                )
            );
            RolePool expectedSenderRoleCategory = RolePool.JUDICIAL_DISTRICT;
            String expectedUserRoleLabel = "District Judge";
            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(message));

            List<Element<Message>> actual = null;
            try (MockedStatic<UUID> mockedStatic = Mockito.mockStatic(UUID.class)) {
                mockedStatic.when(UUID::randomUUID).thenReturn(MESSAGE_ID);

                actual = messageService.addMessage(
                    messages,
                    MESSAGE_METADATA,
                    MESSAGE_CONTENT,
                    USER_AUTH
                );
            }

            assertEquals(List.of(
                message,
                buildExpectedMessage(expectedSenderRoleCategory, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        void should_returnExpectedMessage_forCircuitJudge() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH, SUPPORTED_ROLES)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                                                 new RoleAssignmentResponse().setRoleName("other").setRoleLabel("Other").setRoleCategory("OTHER"),
                                                 new RoleAssignmentResponse().setRoleName("judge").setRoleLabel("Judge").setRoleCategory("JUDICIAL"),
                                                 new RoleAssignmentResponse().setRoleName("circuit-judge").setRoleLabel("Circuit Judge").setRoleCategory(
                                                     "JUDICIAL")
                                             )
                )
            );
            RolePool expectedSenderRoleCategory = RolePool.JUDICIAL_CIRCUIT;
            String expectedUserRoleLabel = "Circuit Judge";
            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(message));

            List<Element<Message>> actual = null;
            try (MockedStatic<UUID> mockedStatic = Mockito.mockStatic(UUID.class)) {
                mockedStatic.when(UUID::randomUUID).thenReturn(MESSAGE_ID);

                actual = messageService.addMessage(
                    messages,
                    MESSAGE_METADATA,
                    MESSAGE_CONTENT,
                    USER_AUTH
                );
            }

            assertEquals(List.of(
                message,
                buildExpectedMessage(expectedSenderRoleCategory, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        void should_returnExpectedMessage_forJudge() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH, SUPPORTED_ROLES)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                                                 new RoleAssignmentResponse().setRoleName("other").setRoleLabel("Other").setRoleCategory("OTHER"),
                                                 new RoleAssignmentResponse().setRoleName("judge").setRoleLabel("Judge").setRoleCategory("JUDICIAL")
                                             )
                )
            );
            RolePool expectedSenderRoleCategory = RolePool.JUDICIAL;
            String expectedUserRoleLabel = "Judge";
            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(message));

            List<Element<Message>> actual = null;
            try (MockedStatic<UUID> mockedStatic = Mockito.mockStatic(UUID.class)) {
                mockedStatic.when(UUID::randomUUID).thenReturn(MESSAGE_ID);

                actual = messageService.addMessage(
                    messages,
                    MESSAGE_METADATA,
                    MESSAGE_CONTENT,
                    USER_AUTH
                );
            }

            assertEquals(List.of(
                message,
                buildExpectedMessage(expectedSenderRoleCategory, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        void should_returnExpectedMessage_forWluAdmin() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH, SUPPORTED_ROLES)).thenReturn(buildRoleAssignmentsResponse(List.of(
                                                 new RoleAssignmentResponse().setRoleName("other").setRoleLabel("Other").setRoleCategory("OTHER"),
                                                 new RoleAssignmentResponse().setRoleName("wlu-admin").setRoleLabel(
                                                     "WLU Administrator").setRoleCategory("ADMIN")
                                             )
                )
            );

            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(message));

            List<Element<Message>> actual = null;
            RolePool expectedSenderRoleCategory = RolePool.WLU_ADMIN;
            String expectedUserRoleLabel = "WLU Administrator";

            try (MockedStatic<UUID> mockedStatic = Mockito.mockStatic(UUID.class)) {
                mockedStatic.when(UUID::randomUUID).thenReturn(MESSAGE_ID);

                actual = messageService.addMessage(
                     messages,
                     MESSAGE_METADATA,
                     MESSAGE_CONTENT,
                     USER_AUTH
                );
            }

            assertEquals(List.of(
                message,
                buildExpectedMessage(expectedSenderRoleCategory, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        private RoleAssignmentServiceResponse buildRoleAssignmentsResponse(List<RoleAssignmentResponse> roleAssignments) {
            return new RoleAssignmentServiceResponse()
                .setRoleAssignmentResponse(roleAssignments);
        }

        private Message buildExpectedMessage(RolePool expectedUserRole, String expectedUserRoleLabel) {
            return new Message()
                .setMessageId(MESSAGE_ID.toString())
                .setMessageContent(MESSAGE_CONTENT)
                .setSentTime(NOW)
                .setUpdatedTime(NOW)
                .setSubjectType(MESSAGE_METADATA.getSubjectType())
                .setSubject(MESSAGE_METADATA.getSubjectType().getLabel())
                .setIsUrgent(MESSAGE_METADATA.getIsUrgent())
                .setRecipientRoleType(RolePool.JUDICIAL_CIRCUIT)
                .setSenderName(String.format("%s, %s", USER_NAME, expectedUserRoleLabel))
                .setSenderRoleType(expectedUserRole);
        }

    }

    @Nested
    class AddReplyToMessage {

        static Stream<Arguments> provideUserData() {
            return Stream.of(
                Arguments.of(RolePool.JUDICIAL,
                             RolePool.ADMIN,
                             List.of(new RoleAssignmentResponse().setRoleName("hearing-centre-admin").setRoleLabel("Hearing Centre Administrator").setRoleCategory("ADMIN")),
                             "Hearing Centre Administrator",
                             "Judge"
                ),
                Arguments.of(RolePool.JUDICIAL,
                             RolePool.ADMIN,
                             List.of(
                                 new RoleAssignmentResponse().setRoleName("other").setRoleLabel("Other").setRoleCategory("OTHER"),
                                 new RoleAssignmentResponse().setRoleName("hearing-centre-admin").setRoleLabel(
                                     "Hearing Centre Administrator").setRoleCategory("ADMIN"),
                                 new RoleAssignmentResponse().setRoleName("hearing-centre-team-leader").setRoleLabel(
                                     "Hearing Centre Team Leader").setRoleCategory("ADMIN")
                             ),
                             "Hearing Centre Team Leader",
                             "Judge"),
                Arguments.of(RolePool.JUDICIAL,
                             RolePool.ADMIN,
                             List.of(
                                 new RoleAssignmentResponse().setRoleName("other").setRoleLabel("Other").setRoleCategory("OTHER"),
                                 new RoleAssignmentResponse().setRoleName("ctsc").setRoleLabel("CTSC").setRoleCategory("ADMIN")
                             ),
                             "CTSC",
                             "Judge"
                ),
                Arguments.of(RolePool.JUDICIAL,
                             RolePool.ADMIN,
                             List.of(
                                 new RoleAssignmentResponse().setRoleName("other").setRoleLabel("Other").setRoleCategory("OTHER"),
                                 new RoleAssignmentResponse().setRoleName("ctsc").setRoleLabel("CTSC").setRoleCategory("ADMIN"),
                                 new RoleAssignmentResponse().setRoleName("ctsc-team-leader").setRoleLabel("CTSC Team Leader").setRoleCategory(
                                     "ADMIN")
                             ),
                             "CTSC Team Leader",
                             "Judge"),
                Arguments.of(RolePool.JUDICIAL,
                             RolePool.LEGAL_OPERATIONS,
                             List.of(
                                 new RoleAssignmentResponse().setRoleName("other").setRoleLabel("Other").setRoleCategory("OTHER"),
                                 new RoleAssignmentResponse().setRoleName("tribunal-caseworker").setRoleLabel("Tribunal Caseworker").setRoleCategory(
                                     "LEGAL_OPERATIONS")
                             ),
                             "Tribunal Caseworker",
                             "Judge"),
                Arguments.of(RolePool.JUDICIAL,
                             RolePool.LEGAL_OPERATIONS,
                             List.of(
                                 new RoleAssignmentResponse().setRoleName("other").setRoleLabel("Other").setRoleCategory("OTHER"),
                                 new RoleAssignmentResponse().setRoleName("tribunal-caseworker").setRoleLabel("Tribunal Caseworker").setRoleCategory(
                                     "LEGAL_OPERATIONS"),
                                 new RoleAssignmentResponse().setRoleName("senior-tribunal-caseworker").setRoleLabel(
                                     "Senior Tribunal Caseworker").setRoleCategory("LEGAL_OPERATIONS")
                             ),
                             "Senior Tribunal Caseworker",
                             "Judge"),
                Arguments.of(RolePool.ADMIN,
                             RolePool.JUDICIAL_DISTRICT,
                             List.of(
                                 new RoleAssignmentResponse().setRoleName("other").setRoleLabel("Other").setRoleCategory("OTHER"),
                                 new RoleAssignmentResponse().setRoleName("judge").setRoleLabel("Judge").setRoleCategory("JUDICIAL"),
                                 new RoleAssignmentResponse().setRoleName("district-judge").setRoleLabel("District Judge").setRoleCategory(
                                     "JUDICIAL")
                             ),
                             "District Judge",
                             "Hearing centre admin"),
                Arguments.of(RolePool.ADMIN,
                             RolePool.JUDICIAL_CIRCUIT,
                             List.of(
                                 new RoleAssignmentResponse().setRoleName("other").setRoleLabel("Other").setRoleCategory("OTHER"),
                                 new RoleAssignmentResponse().setRoleName("judge").setRoleLabel("Judge").setRoleCategory("JUDICIAL"),
                                 new RoleAssignmentResponse().setRoleName("circuit-judge").setRoleLabel("Circuit Judge").setRoleCategory(
                                     "JUDICIAL")
                             ),
                             "Circuit Judge",
                             "Hearing centre admin"),
                Arguments.of(RolePool.ADMIN,
                             RolePool.JUDICIAL,
                             List.of(
                                 new RoleAssignmentResponse().setRoleName("other").setRoleLabel("Other").setRoleCategory("OTHER"),
                                 new RoleAssignmentResponse().setRoleName("judge").setRoleLabel("Judge").setRoleCategory("JUDICIAL")
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
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH, SUPPORTED_ROLES)).thenReturn(
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

            MessageReply messageReply = new MessageReply()
                .setIsUrgent(NO)
                .setMessageContent("This is a reply message")
                .setSentTime(updatedDateTime);

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
                USER_AUTH,
                caseData
            ));

            List<MessageReply> actualMessageHistory = unwrapElements(actualMessages.get(0).getHistory());

            assertEquals(List.of(new MessageReply()
                                     .setMessageContent("Existing message")
                                     .setIsUrgent(YES)
                                     .setSenderName(String.format("%s, %s", USER_NAME, originalUserRoleLabel))
                                     .setSenderRoleType(originalSender)
                                     .setRecipientRoleType(currentSender)
                                     .setSubject("Subject")
                                     .setSentTime(updatedDateTime)), actualMessageHistory);
            assertEquals(expectedMessage, existingMessageToBeChanged.getValue());
        }

        @Test
        void shouldAddMessageReplyToExistingHistory_withTwoExistingReplies() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH, SUPPORTED_ROLES)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                    new RoleAssignmentResponse().setRoleName("other").setRoleLabel("Other").setRoleCategory("OTHER"),
                    new RoleAssignmentResponse().setRoleName("hearing-centre-admin").setRoleLabel(
                        "Hearing Centre Administrator").setRoleCategory("ADMIN"),
                    new RoleAssignmentResponse().setRoleName("hearing-centre-team-leader").setRoleLabel(
                        "Hearing Centre Team Leader").setRoleCategory("ADMIN")
                ))
            );

            RolePool expectedSenderRoleCategory = RolePool.ADMIN;
            MessageReply baseMessage = new MessageReply()
                .setMessageContent("First reply")
                .setIsUrgent(NO)
                .setSenderName("John Doe, Hearing Centre Team Leader")
                .setSenderRoleType(expectedSenderRoleCategory)
                .setSentTime(NOW.minusDays(2));

            MessageReply firstReply = new MessageReply()
                .setMessageContent("Second reply")
                .setIsUrgent(YES)
                .setSenderName("Jane Smith, Hearing Centre Team Leader")
                .setSenderRoleType(expectedSenderRoleCategory)
                .setSentTime(NOW.minusDays(1));

            List<Element<MessageReply>> history = new ArrayList<>();
            history.add(element(firstReply));
            history.add(element(baseMessage));

            Message secondReply = message.toBuilder()
                .history(history).build();

            Element<Message> existingMessageElement = element(secondReply);
            List<Element<Message>> messages = new ArrayList<>();
            messages.add(existingMessageElement);

            MessageReply newReply = new MessageReply().setIsUrgent(YES).setMessageContent("This is a new reply message");

            List<Message> actualMessages = unwrapElements(messageService.addReplyToMessage(
                messages,
                existingMessageElement.getId().toString(),
                newReply,
                USER_AUTH,
                caseData
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
            message1 = element(new Message().setMessageContent("Message content 1"));
            message2 = element(new Message().setMessageContent("Message content 2"));
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
            message1 = element(new Message()
                                   .setSubject("Subject 1")
                                   .setSentTime(LocalDateTime.of(2024, 11, 14, 10, 30, 0)));

            message2 = element(new Message()
                                   .setSubject("Subject 2")
                                   .setSentTime(LocalDateTime.of(2024, 11, 14, 12, 45, 0)));

            messages = List.of(message1, message2);
        }

        @Test
        void shouldCreateMessageSelectionList_withCorrectFormat() {
            DynamicListElement element1 = new DynamicListElement();
            element1.setCode(message1.getId().toString());
            element1.setLabel(String.format("%s, 14 Nov 2024, 10:30:00 AM", message1.getValue().getSubject()));

            DynamicListElement element2 = new DynamicListElement();
            element2.setCode(message2.getId().toString());
            element2.setLabel(String.format("%s, 14 Nov 2024, 12:45:00 PM", message2.getValue().getSubject()));

            DynamicList expected = new DynamicList();
            expected.setListItems(List.of(element1, element2));
            DynamicList result = messageService.createMessageSelectionList(messages);
            assertEquals(expected, result);
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

            message = element(new Message()
                                  .setSentTime(LocalDateTime.of(2024, 11, 14, 10, 30, 0))
                                  .setUpdatedTime(LocalDateTime.of(2024, 10, 10, 10, 10, 10))
                                  .setSenderName("Sender 1")
                                  .setSenderRoleType(RolePool.ADMIN)
                                  .setRecipientRoleType(RolePool.JUDICIAL)
                                  .setIsUrgent(YES)
                                  .setSubjectType(OTHER)
                                  .setSubject("Subject 1")
                                  .setMessageContent("This is the base message."));
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
            message.getValue().getHistory().add(element(new MessageReply()
                                                            .setMessageContent("This is the original message")
                                                            .setIsUrgent(NO)
                                                            .setSenderName("Sender 2")
                                                            .setSenderRoleType(RolePool.JUDICIAL)
                                                            .setRecipientRoleType(RolePool.ADMIN)
                                                            .setSentTime(NOW.minusHours(5))));

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
        return new RoleAssignmentServiceResponse()
            .setRoleAssignmentResponse(roleAssignments);
    }
}
