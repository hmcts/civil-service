package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MigrationTaskFactoryTest {

    private MigrationTaskFactory factory;

    private MigrationTask task1;
    private MigrationTask task2;

    @BeforeEach
    void setUp() {
        task1 = mock(MigrationTask.class);
        task2 = mock(MigrationTask.class);

        when(task1.getTaskName()).thenReturn("Task1");
        when(task2.getTaskName()).thenReturn("Task2");

        factory = new MigrationTaskFactory(task1, task2);
    }

    @Test
    void shouldReturnMigrationTaskForValidTaskName() {
        // Act
        Optional<MigrationTask> result = factory.getMigrationTask("Task1");

        // Assert
        assertEquals(Optional.of(task1), result);
    }

    @Test
    void shouldReturnEmptyOptionalForInvalidTaskName() {
        // Act
        Optional<MigrationTask> result = factory.getMigrationTask("InvalidTask");

        // Assert
        assertEquals(Optional.empty(), result);
    }

    @Test
    void shouldReturnEmptyOptionalForNullTaskName() {
        // Act
        Optional<MigrationTask> result = factory.getMigrationTask(null);

        // Assert
        assertEquals(Optional.empty(), result);
    }

    @Test
    void shouldReturnEmptyOptionalForEmptyTaskName() {
        // Act
        Optional<MigrationTask> result = factory.getMigrationTask("");

        // Assert
        assertEquals(Optional.empty(), result);
    }
}
