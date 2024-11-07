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
    private static final String COURT_STAFF_ROLE = "caseworker-civil-admin";
    private static final String JUDGE_ROLE = "judge";
    private static final String LEGAL_ADVISOR_ROLE = "legal-advisor";
    private static final String OTHER_ROLE = "other";
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
            UserDetails userDetails = buildUserDetails(List.of(COURT_STAFF_ROLE));
            when(userService.getUserDetails(USER_AUTH)).thenReturn(userDetails);

            String expectedIdamRoleLabel = "Court Staff";
            String expectedUserRoleLabel = "Hearing Centre Administrator";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(userDetails.getId(), USER_AUTH)).thenReturn(
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

            assertEquals(List.of(buildExpectedMessage(expectedIdamRoleLabel, expectedUserRoleLabel)), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forHearingCentreAdmin() {
            UserDetails userDetails = buildUserDetails(List.of(OTHER_ROLE, COURT_STAFF_ROLE));
            when(userService.getUserDetails(USER_AUTH)).thenReturn(userDetails);

            String expectedIdamRoleLabel = "Court Staff";
            String expectedUserRoleLabel = "Hearing Centre Administrator";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(userDetails.getId(), USER_AUTH)).thenReturn(
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
                buildExpectedMessage(expectedIdamRoleLabel, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forHearingCentreTeamLeader() {
            UserDetails userDetails = buildUserDetails(List.of(OTHER_ROLE, COURT_STAFF_ROLE));
            when(userService.getUserDetails(USER_AUTH)).thenReturn(userDetails);

            String expectedIdamRoleLabel = "Court Staff";
            String expectedUserRoleLabel = "Hearing Centre Team Leader";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(userDetails.getId(), USER_AUTH)).thenReturn(
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
                buildExpectedMessage(expectedIdamRoleLabel, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forCtsc() {
            UserDetails userDetails = buildUserDetails(List.of(OTHER_ROLE, COURT_STAFF_ROLE));
            when(userService.getUserDetails(USER_AUTH)).thenReturn(userDetails);

            String expectedUserRoleLabel = "CTSC";
            String expectedIdamRoleLabel = "Court Staff";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(userDetails.getId(), USER_AUTH)).thenReturn(
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
                buildExpectedMessage(expectedIdamRoleLabel, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forCtscTeamLeader() {
            UserDetails userDetails = buildUserDetails(List.of(OTHER_ROLE, COURT_STAFF_ROLE));
            when(userService.getUserDetails(USER_AUTH)).thenReturn(userDetails);

            String expectedIdamRoleLabel = "Court Staff";
            String expectedUserRoleLabel = "CTSC Team Leader";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(userDetails.getId(), USER_AUTH)).thenReturn(
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
                buildExpectedMessage(expectedIdamRoleLabel, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forTribunalCaseworker() {
            UserDetails userDetails = buildUserDetails(List.of(OTHER_ROLE, LEGAL_ADVISOR_ROLE));
            when(userService.getUserDetails(USER_AUTH)).thenReturn(userDetails);

            String expectedIdamRoleLabel = "Legal Advisor";
            String expectedUserRoleLabel = "Tribunal Caseworker";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(userDetails.getId(), USER_AUTH)).thenReturn(
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
                buildExpectedMessage(expectedIdamRoleLabel, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forSeniorTribunalCaseworker() {
            UserDetails userDetails = buildUserDetails(List.of(OTHER_ROLE, LEGAL_ADVISOR_ROLE));
            when(userService.getUserDetails(USER_AUTH)).thenReturn(userDetails);

            String expectedIdamRoleLabel = "Legal Advisor";
            String expectedUserRoleLabel = "Senior Tribunal Caseworker";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(userDetails.getId(), USER_AUTH)).thenReturn(
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
                buildExpectedMessage(expectedIdamRoleLabel, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forNationalBusinessCentre() {
            UserDetails userDetails = buildUserDetails(List.of(OTHER_ROLE, COURT_STAFF_ROLE));
            when(userService.getUserDetails(USER_AUTH)).thenReturn(userDetails);

            String expectedIdamRoleLabel = "Court Staff";
            String expectedUserRoleLabel = "National Business Centre";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(userDetails.getId(), USER_AUTH)).thenReturn(
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
                buildExpectedMessage(expectedIdamRoleLabel, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forNationalBusinessCentreTeamLeader() {
            UserDetails userDetails = buildUserDetails(List.of(OTHER_ROLE, COURT_STAFF_ROLE));
            when(userService.getUserDetails(USER_AUTH)).thenReturn(userDetails);

            String expectedIdamRoleLabel = "Court Staff";
            String expectedUserRoleLabel = "NBC Team Leader";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(userDetails.getId(), USER_AUTH)).thenReturn(
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
                buildExpectedMessage(expectedIdamRoleLabel, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forDistrictJudge() {
            UserDetails userDetails = buildUserDetails(List.of(OTHER_ROLE, JUDGE_ROLE));
            when(userService.getUserDetails(USER_AUTH)).thenReturn(userDetails);

            String expectedIdamRoleLabel = "Judge";
            String expectedUserRoleLabel = "District Judge";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(userDetails.getId(), USER_AUTH)).thenReturn(
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
                buildExpectedMessage(expectedIdamRoleLabel, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forCircuitJudge() {
            UserDetails userDetails = buildUserDetails(List.of(OTHER_ROLE, JUDGE_ROLE));
            when(userService.getUserDetails(USER_AUTH)).thenReturn(userDetails);

            String expectedIdamRoleLabel = "Judge";
            String expectedUserRoleLabel = "Circuit Judge";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(userDetails.getId(), USER_AUTH)).thenReturn(
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
                buildExpectedMessage(expectedIdamRoleLabel, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        @Test
        public void should_returnExpectedMessage_forJudge() {
            UserDetails userDetails = buildUserDetails(List.of(OTHER_ROLE, JUDGE_ROLE));
            when(userService.getUserDetails(USER_AUTH)).thenReturn(userDetails);

            String expectedIdamRoleLabel = "Judge";
            String expectedUserRoleLabel = "Judge";
            when(roleAssignmentService.getRoleAssignmentsWithLabels(userDetails.getId(), USER_AUTH)).thenReturn(
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
                buildExpectedMessage(expectedIdamRoleLabel, expectedUserRoleLabel)
            ), unwrapElements(actual));
        }

        private UserDetails buildUserDetails(List<String> roles) {
            return UserDetails.builder()
                .id("uid")
                .forename("Test")
                .surname("User")
                .roles(roles)
                .build();
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
