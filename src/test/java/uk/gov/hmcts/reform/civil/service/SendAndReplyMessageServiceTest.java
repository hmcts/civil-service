package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Nested;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.sendandreply.Message;
import uk.gov.hmcts.reform.civil.model.sendandreply.SendMessageMetadata;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentResponse;
import uk.gov.hmcts.reform.civil.ras.model.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.sendandreply.RecipientOption.CIRCUIT_JUDGE;
import static uk.gov.hmcts.reform.civil.enums.sendandreply.SubjectOption.HEARING;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

@ExtendWith(SpringExtension.class)
class SendAndReplyMessageServiceTest {

    private static String USER_AUTH = "auth";
    private static String USER_NAME = "Test User";
    private static SendMessageMetadata MESSAGE_METADATA = SendMessageMetadata.builder()
        .subject(HEARING)
        .recipientRoleType(CIRCUIT_JUDGE)
        .isUrgent(YES)
        .build();
    private static String MESSAGE_CONTENT = "Message Content";
    private static LocalDateTime NOW = LocalDateTime.of(2014, 11, 1, 0, 0, 0);
    public static final UserDetails USER_DETAILS = UserDetails.builder()
        .id("uid")
        .forename("Test")
        .surname("User")
        .build();

    private static final Message EXISTING_MESSAGE =
        Message.builder()
            .updatedTime(LocalDateTime.of(2024, 1, 1, 0, 0, 0))
            .sentTime(LocalDateTime.of(2024, 1, 1, 0, 0, 0))
            .subject("Subject")
            .contentSubject("Subject")
            .senderRoleType("Admin")
            .messageContent("Existing message")
            .build();

    @Mock
    private UserService userService;

    @Mock
    private RoleAssignmentsService roleAssignmentService;

    @Mock
    private Time time;

    @InjectMocks
    private SendAndReplyMessageService messageService;

    @BeforeEach
    public void setupTest() {
        when(time.now()).thenReturn(NOW);
        when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);
    }

    @Nested
    class AddMessage {
        @Test
        public void should_returnExpectedMessage_whenExistingMessagesAreNull() {
            String expectedUserRoleLabel = "Hearing Centre Administrator";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(Map.of(
                    "hearing-centre-admin", expectedUserRoleLabel,
                    "some-other-role", "Some Other Role"
                ))
            );

            List<Element<Message>> actual = messageService.addMessage(
                null,
                MESSAGE_METADATA,
                MESSAGE_CONTENT,
                USER_AUTH
            );

            assertEquals(List.of(buildExpectedMessage(expectedUserRoleLabel)), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forHearingCentreAdmin() {
            String expectedUserRoleLabel = "Hearing Centre Administrator";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(Map.of(
                    "hearing-centre-admin", expectedUserRoleLabel,
                    "some-other-role", "Some Other Role"
                ))
            );

            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(EXISTING_MESSAGE));

            List<Element<Message>> actual = messageService.addMessage(
                messages,
                MESSAGE_METADATA,
                MESSAGE_CONTENT,
                USER_AUTH
            );

            assertEquals(List.of(
                EXISTING_MESSAGE,
                buildExpectedMessage(expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forHearingCentreTeamLeader() {
            String expectedUserRoleLabel = "Hearing Centre Team Leader";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(Map.of(
                    "hearing-centre-admin", "Hearing Centre Administrator",
                    "hearing-centre-team-leader", expectedUserRoleLabel,
                    "some-other-role", "Some Other Role"
                ))
            );

            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(EXISTING_MESSAGE));

            List<Element<Message>> actual = messageService.addMessage(
                messages,
                MESSAGE_METADATA,
                MESSAGE_CONTENT,
                USER_AUTH
            );

            assertEquals(List.of(
                EXISTING_MESSAGE,
                buildExpectedMessage(expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forCtsc() {
            String expectedUserRoleLabel = "CTSC";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(Map.of(
                    "ctsc", expectedUserRoleLabel,
                    "some-other-role", "Some Other Role"
                ))
            );

            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(EXISTING_MESSAGE));

            List<Element<Message>> actual = messageService.addMessage(
                messages,
                MESSAGE_METADATA,
                MESSAGE_CONTENT,
                USER_AUTH
            );

            assertEquals(List.of(
                EXISTING_MESSAGE,
                buildExpectedMessage(expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forCtscTeamLeader() {
            String expectedUserRoleLabel = "CTSC Team Leader";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(Map.of(
                    "ctsc", "CTSC",
                    "ctsc-team-leader", expectedUserRoleLabel,
                    "some-other-role", "Some Other Role"
                ))
            );

            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(EXISTING_MESSAGE));

            List<Element<Message>> actual = messageService.addMessage(
                messages,
                MESSAGE_METADATA,
                MESSAGE_CONTENT,
                USER_AUTH
            );

            assertEquals(List.of(
                EXISTING_MESSAGE,
                buildExpectedMessage(expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forTribunalCaseworker() {
            String expectedUserRoleLabel = "Tribunal Caseworker";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(Map.of(
                    "tribunal-caseworker", expectedUserRoleLabel,
                    "some-other-role", "Some Other Role"
                ))
            );

            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(EXISTING_MESSAGE));

            List<Element<Message>> actual = messageService.addMessage(
                messages,
                MESSAGE_METADATA,
                MESSAGE_CONTENT,
                USER_AUTH
            );

            assertEquals(List.of(
                EXISTING_MESSAGE,
                buildExpectedMessage(expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forSeniorTribunalCaseworker() {
            String expectedUserRoleLabel = "Senior Tribunal Caseworker";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(Map.of(
                    "tribunal-caseworker", "Tribunal Caseworker",
                    "senior-tribunal-caseworker", expectedUserRoleLabel,
                    "some-other-role", "Some Other Role"
                ))
            );

            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(EXISTING_MESSAGE));

            List<Element<Message>> actual = messageService.addMessage(
                messages,
                MESSAGE_METADATA,
                MESSAGE_CONTENT,
                USER_AUTH
            );

            assertEquals(List.of(
                EXISTING_MESSAGE,
                buildExpectedMessage(expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forNationalBusinessCentre() {
            String expectedUserRoleLabel = "National Business Centre";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(Map.of(
                    "national-business-centre", expectedUserRoleLabel,
                    "some-other-role", "Some Other Role"
                ))
            );

            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(EXISTING_MESSAGE));

            List<Element<Message>> actual = messageService.addMessage(
                messages,
                MESSAGE_METADATA,
                MESSAGE_CONTENT,
                USER_AUTH
            );

            assertEquals(List.of(
                EXISTING_MESSAGE,
                buildExpectedMessage(expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forNationalBusinessCentreTeamLeader() {
            String expectedUserRoleLabel = "NBC Team Leader";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(Map.of(
                    "national-business-centre", "National Business Centre",
                    "nbc-team-leader", expectedUserRoleLabel,
                    "some-other-role", "Some Other Role"
                ))
            );

            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(EXISTING_MESSAGE));

            List<Element<Message>> actual = messageService.addMessage(
                messages,
                MESSAGE_METADATA,
                MESSAGE_CONTENT,
                USER_AUTH
            );

            assertEquals(List.of(
                EXISTING_MESSAGE,
                buildExpectedMessage(expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forDistrictJudge() {
            String expectedUserRoleLabel = "District Judge";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(Map.of(
                    "judge", "Judge",
                    "district-judge", expectedUserRoleLabel,
                    "some-other-role", "Some Other Role"
                ))
            );

            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(EXISTING_MESSAGE));

            List<Element<Message>> actual = messageService.addMessage(
                messages,
                MESSAGE_METADATA,
                MESSAGE_CONTENT,
                USER_AUTH
            );

            assertEquals(List.of(
                EXISTING_MESSAGE,
                buildExpectedMessage(expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forCircuitJudge() {
            String expectedUserRoleLabel = "Circuit Judge";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(Map.of(
                    "judge", "Judge",
                    "circuit-judge", expectedUserRoleLabel,
                    "some-other-role", "Some Other Role"
                ))
            );

            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(EXISTING_MESSAGE));

            List<Element<Message>> actual = messageService.addMessage(
                messages,
                MESSAGE_METADATA,
                MESSAGE_CONTENT,
                USER_AUTH
            );

            assertEquals(List.of(
                EXISTING_MESSAGE,
                buildExpectedMessage(expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forJudge() {
            String expectedUserRoleLabel = "Judge";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(Map.of(
                    "judge", "Judge",
                    "some-other-role", "Some Other Role"
                ))
            );

            List<Element<Message>> messages = new ArrayList<>();
            messages.add(element(EXISTING_MESSAGE));

            List<Element<Message>> actual = messageService.addMessage(
                messages,
                MESSAGE_METADATA,
                MESSAGE_CONTENT,
                USER_AUTH
            );

            assertEquals(List.of(
                EXISTING_MESSAGE,
                buildExpectedMessage(expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        private RoleAssignmentServiceResponse buildRoleAssignmentsResponse(Map<String, String> roleToLabelMap) {
            return RoleAssignmentServiceResponse.builder()
                .roleAssignmentResponse(
                    roleToLabelMap.entrySet().stream()
                        .map(roleLabelMap -> RoleAssignmentResponse.builder().roleName(roleLabelMap.getKey()).roleLabel(
                            roleLabelMap.getValue()).build()).toList()
                )
                .build();
        }

        private Message buildExpectedMessage(String expectedUserRoleLabel) {
            return Message.builder()
                .messageContent(MESSAGE_CONTENT)
                .sentTime(NOW)
                .updatedTime(NOW)
                .subject(MESSAGE_METADATA.getSubject().getLabel())
                .contentSubject(MESSAGE_METADATA.getSubject().getLabel())
                .isUrgent(MESSAGE_METADATA.getIsUrgent())
                .recipientRoleType(MESSAGE_METADATA.getRecipientRoleType().getLabel())
                .senderName(String.format("%s, %s", USER_NAME, expectedUserRoleLabel))
                .senderRoleType(expectedUserRoleLabel)
                .build();
        }
    }
}
