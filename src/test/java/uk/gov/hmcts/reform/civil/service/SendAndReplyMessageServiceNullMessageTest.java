package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SendAndReplyMessageServiceNullMessageTest {

    @Mock
    private UserService userService;
    @Mock
    private RoleAssignmentsService roleAssignmentsService;
    @Mock
    private TableMarkupService tableMarkupService;
    @Mock
    private Time time;

    @InjectMocks
    private SendAndReplyMessageService messageService;

    @Test
    void shouldThrowNullPointerExceptionWhenMessageToReplyToIsNull() {
        assertThatThrownBy(() -> messageService.renderMessageTableList(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("message to reply to must not be null");

        verifyNoInteractions(tableMarkupService);
    }
}
