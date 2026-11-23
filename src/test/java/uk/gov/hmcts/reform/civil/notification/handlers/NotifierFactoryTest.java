package uk.gov.hmcts.reform.civil.notification.handlers;

import uk.gov.hmcts.reform.civil.model.CaseData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotifierFactoryTest {

    Notifier<CaseData> notifier;

    // mock() cannot carry the type argument, so the assignment to Notifier<CaseData> is unchecked
    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        notifier  = mock(Notifier.class);
        when(notifier.getTaskId()).thenReturn("validTaskId");
    }

    @Test
    void shouldReturnNotifier() {
        NotifierFactory factory = new NotifierFactory(notifier);
        assertThat(factory.getNotifier("validTaskId")).isEqualTo(notifier);
    }

    @Test
    void shouldReturnNullWhenTaskInvalid() {
        NotifierFactory factory = new NotifierFactory(notifier);
        assertThat(factory.getNotifier("invalidTaskId")).isEqualTo(null);
    }

    @Test
    void shouldReturnNullWhenTaskIdIsNull() {
        NotifierFactory factory = new NotifierFactory(notifier);
        assertThat(factory.getNotifier(null)).isEqualTo(null);
    }
}
