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

    private static final UserDetails USER_DETAILS = UserDetails.builder()
        .id("uid")
        .forename("Test")
        .surname("User")
        .build();

    private static SendMessageMetadata MESSAGE_METADATA = SendMessageMetadata.builder()
        .subject(HEARING)
        .recipientRoleType(CIRCUIT_JUDGE)
        .isUrgent(YES)
        .build();
    private static String MESSAGE_CONTENT = "Message Content";
    private static LocalDateTime NOW = LocalDateTime.of(2014, 11, 1, 0, 0, 0);

    private static final Message EXISTING_MESSAGE =
        Message.builder()
            .updatedTime(LocalDateTime.of(2024, 1, 1, 0, 0, 0))
            .sentTime(LocalDateTime.of(2024, 1, 1, 0, 0, 0))
            .headerSubject("Subject")
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
    }

    @Nested
    class AddMessage {
        @Test
        public void should_returnExpectedMessage_whenExistingMessagesAreNull() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            String expectedRoleCategoryLabel = "Court staff";
            String expectedUserRoleLabel = "Hearing Centre Administrator";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                    RoleAssignmentResponse.builder().roleName("hearing-centre-admin").roleLabel("Hearing Centre Administrator").roleCategory("ADMIN").build())
                )
            );

            List<Element<Message>> actual = messageService.addMessage(
                null,
                MESSAGE_METADATA,
                MESSAGE_CONTENT,
                USER_AUTH
            );

            assertEquals(List.of(buildExpectedMessage(expectedRoleCategoryLabel, expectedUserRoleLabel)), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forHearingCentreAdmin() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            String expectedRoleCategoryLabel = "Court staff";
            String expectedUserRoleLabel = "Hearing Centre Administrator";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                    RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                    RoleAssignmentResponse.builder().roleName("hearing-centre-admin").roleLabel("Hearing Centre Administrator").roleCategory("ADMIN").build())
                )
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
                buildExpectedMessage(expectedRoleCategoryLabel, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forHearingCentreTeamLeader() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            String expectedRoleCategoryLabel = "Court staff";
            String expectedUserRoleLabel = "Hearing Centre Team Leader";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                    RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                    RoleAssignmentResponse.builder().roleName("hearing-centre-admin").roleLabel("Hearing Centre Administrator").roleCategory("ADMIN").build(),
                    RoleAssignmentResponse.builder().roleName("hearing-centre-team-leader").roleLabel("Hearing Centre Team Leader").roleCategory("ADMIN").build())
                )
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
                buildExpectedMessage(expectedRoleCategoryLabel, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forCtsc() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            String expectedUserRoleLabel = "CTSC";
            String expectedRoleCategoryLabel = "Court staff";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                RoleAssignmentResponse.builder().roleName("ctsc").roleLabel("CTSC").roleCategory("ADMIN").build())
            ));

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
                buildExpectedMessage(expectedRoleCategoryLabel, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forCtscTeamLeader() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            String expectedRoleCategoryLabel = "Court staff";
            String expectedUserRoleLabel = "CTSC Team Leader";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                    RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                    RoleAssignmentResponse.builder().roleName("ctsc").roleLabel("CTSC").roleCategory("ADMIN").build(),
                    RoleAssignmentResponse.builder().roleName("ctsc-team-leader").roleLabel("CTSC Team Leader").roleCategory("ADMIN").build())
                )
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
                buildExpectedMessage(expectedRoleCategoryLabel, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forTribunalCaseworker() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            String expectedRoleCategoryLabel = "Legal advisor";
            String expectedUserRoleLabel = "Tribunal Caseworker";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                    RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                    RoleAssignmentResponse.builder().roleName("tribunal-caseworker").roleLabel("Tribunal Caseworker").roleCategory("LEGAL_OPERATIONS").build())
                )
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
                buildExpectedMessage(expectedRoleCategoryLabel, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forSeniorTribunalCaseworker() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            String expectedRoleCategoryLabel = "Legal advisor";
            String expectedUserRoleLabel = "Senior Tribunal Caseworker";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                    RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                    RoleAssignmentResponse.builder().roleName("tribunal-caseworker").roleLabel("Tribunal Caseworker").roleCategory("LEGAL_OPERATIONS").build(),
                    RoleAssignmentResponse.builder().roleName("senior-tribunal-caseworker").roleLabel("Senior Tribunal Caseworker").roleCategory("LEGAL_OPERATIONS").build())
                )
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
                buildExpectedMessage(expectedRoleCategoryLabel, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forNationalBusinessCentre() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            String expectedRoleCategoryLabel = "Court staff";
            String expectedUserRoleLabel = "National Business Centre";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                    RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                    RoleAssignmentResponse.builder().roleName("national-business-centre").roleLabel("National Business Centre").roleCategory("ADMIN").build())
                )
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
                buildExpectedMessage(expectedRoleCategoryLabel, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forNationalBusinessCentreTeamLeader() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            String expectedRoleCategoryLabel = "Court staff";
            String expectedUserRoleLabel = "NBC Team Leader";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                    RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                    RoleAssignmentResponse.builder().roleName("national-business-centre").roleLabel("National Business Centre").roleCategory("ADMIN").build(),
                    RoleAssignmentResponse.builder().roleName("nbc-team-leader").roleLabel("NBC Team Leader").roleCategory("ADMIN").build())
                )
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
                buildExpectedMessage(expectedRoleCategoryLabel, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forDistrictJudge() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            String expectedRoleCategoryLabel = "Judge";
            String expectedUserRoleLabel = "District Judge";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                    RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                    RoleAssignmentResponse.builder().roleName("judge").roleLabel("Judge").roleCategory("JUDICIAL").build(),
                    RoleAssignmentResponse.builder().roleName("district-judge").roleLabel("District Judge").roleCategory("JUDICIAL").build())
                )
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
                buildExpectedMessage(expectedRoleCategoryLabel, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forCircuitJudge() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            String expectedRoleCategoryLabel = "Judge";
            String expectedUserRoleLabel = "Circuit Judge";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                    RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                    RoleAssignmentResponse.builder().roleName("judge").roleLabel("Judge").roleCategory("JUDICIAL").build(),
                    RoleAssignmentResponse.builder().roleName("circuit-judge").roleLabel("Circuit Judge").roleCategory("JUDICIAL").build())
                )
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
                buildExpectedMessage(expectedRoleCategoryLabel, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forJudge() {
            when(userService.getUserDetails(USER_AUTH)).thenReturn(USER_DETAILS);

            String expectedRoleCategoryLabel = "Judge";
            String expectedUserRoleLabel = "Judge";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(USER_DETAILS.getId(), USER_AUTH)).thenReturn(
                buildRoleAssignmentsResponse(List.of(
                    RoleAssignmentResponse.builder().roleName("other").roleLabel("Other").roleCategory("OTHER").build(),
                    RoleAssignmentResponse.builder().roleName("judge").roleLabel("Judge").roleCategory("JUDICIAL").build())
                )
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
                buildExpectedMessage(expectedRoleCategoryLabel, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        private RoleAssignmentServiceResponse buildRoleAssignmentsResponse(List<RoleAssignmentResponse> roleAssignments) {
            return RoleAssignmentServiceResponse.builder()
                .roleAssignmentResponse(roleAssignments)
                .build();
        }

        private Message buildExpectedMessage(String expectedUserIdemRoleLabel, String expectedUserRoleLabel) {
            return Message.builder()
                .messageContent(MESSAGE_CONTENT)
                .sentTime(NOW)
                .updatedTime(NOW)
                .headerSubject(MESSAGE_METADATA.getSubject().getLabel())
                .contentSubject(MESSAGE_METADATA.getSubject().getLabel())
                .isUrgent(MESSAGE_METADATA.getIsUrgent())
                .recipientRoleType(MESSAGE_METADATA.getRecipientRoleType().getLabel())
                .senderName(String.format("%s, %s", USER_NAME, expectedUserRoleLabel))
                .senderRoleType(expectedUserIdemRoleLabel)
                .build();
        }

    }
}
