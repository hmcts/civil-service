package uk.gov.hmcts.reform.dashboard.services;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.dashboard.data.TaskList;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;
import uk.gov.hmcts.reform.dashboard.entities.TaskItemTemplateEntity;
import uk.gov.hmcts.reform.dashboard.entities.TaskListEntity;
import uk.gov.hmcts.reform.dashboard.repositories.TaskItemTemplateRepository;
import uk.gov.hmcts.reform.dashboard.repositories.TaskListRepository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.dashboard.utils.DashboardNotificationsTestUtils.getTaskListEntity;
import static uk.gov.hmcts.reform.dashboard.utils.DashboardNotificationsTestUtils.getTaskListEntityList;
import static uk.gov.hmcts.reform.dashboard.utils.DashboardNotificationsTestUtils.getTaskListList;

@ExtendWith(MockitoExtension.class)
class TaskListServiceTest {

    @Mock
    private TaskListRepository taskListRepository;

    @Mock
    private TaskItemTemplateRepository taskItemTemplateRepository;

    @InjectMocks
    private TaskListService taskListService;

    @Test
    void shouldReturnEmpty_whenTaskListIsNotPresent() {

        //given
        when(taskListRepository.findByReferenceAndTaskItemTemplateRole(any(), any())).thenReturn(List.of());

        //when
        List<TaskList> actual = taskListService.getTaskList("123", "Claimant");

        //then
        verify(taskListRepository).findByReferenceAndTaskItemTemplateRole("123", "Claimant");
        assertThat(actual).isEqualTo(List.of());
    }

    @Test
    void shouldReturnTaskList_whenTaskListIsPresent() {

        //given
        when(taskListRepository.findByReferenceAndTaskItemTemplateRole(
            any(),
            any()
        )).thenReturn(getTaskListEntityList());

        //when
        List<TaskList> actual = taskListService.getTaskList("123", "Claimant");

        //then
        verify(taskListRepository).findByReferenceAndTaskItemTemplateRole("123", "Claimant");
        assertThat(actual).isEqualTo(getTaskListList());
    }

    @Test
    void shouldReturnTaskListEntity_whenTaskListEntityIsUpdated() {
        //given
        UUID taskItemIdentifier = UUID.randomUUID();
        TaskListEntity taskListEntity = getTaskListEntity(taskItemIdentifier);
        when(taskListRepository.findById(taskItemIdentifier)).thenReturn(Optional.of(taskListEntity));
        TaskListEntity expected = taskListEntity.toBuilder().currentStatus(taskListEntity.getNextStatus()).build();
        when(taskListRepository.save(expected)).thenReturn(expected);

        //when
        TaskListEntity actual = taskListService.updateTaskListItem(taskItemIdentifier);

        //then
        verify(taskListRepository).findById(taskItemIdentifier);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldReturnEmptyTaskListEntity_whenTaskListEntityIsNotUpdated() {

        //given
        UUID taskItemIdentifier = UUID.randomUUID();
        when(taskListRepository.findById(taskItemIdentifier)).thenReturn(Optional.empty());

        //when
        assertThatThrownBy(() -> taskListService.updateTaskListItem(taskItemIdentifier))
            .isInstanceOf(IllegalArgumentException.class)
            .hasNoCause()
            .hasMessage("Invalid task item identifier " + taskItemIdentifier);

        //then
        verify(taskListRepository).findById(taskItemIdentifier);
    }

    @Test
    void shouldThrowExceptionWhenExceptionInGetTaskList() {

        //given
        when(taskListRepository.findByReferenceAndTaskItemTemplateRole(any(), any()))
            .thenThrow(new RuntimeException());

        //then
        assertThrows(
            RuntimeException.class,
            () -> taskListRepository.findByReferenceAndTaskItemTemplateRole(any(), any())
        );

    }

    @Test
    void shouldMakeProgressAbleTaskListInactive_whenTaskListIsPresent() {

        //given
        List<TaskListEntity> tasks = new ArrayList<>();
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .taskNameEn("<a>Link name</a>")
                      .taskNameCy("<a>Link name Welsh</a>")
                      .currentStatus(TaskStatus.NOT_AVAILABLE_YET.getPlaceValue())
                      .build());
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.INACTIVE.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>").build());
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.AVAILABLE.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>").build());
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.OPTIONAL.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>").build());
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.ACTION_NEEDED.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>")
                      .build());
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.IN_PROGRESS.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>")
                      .build());
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.DONE.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>")
                      .build());

        when(taskListRepository.findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotIn(
            "123", "Claimant",
            List.of(
                TaskStatus.AVAILABLE.getPlaceValue(),
                TaskStatus.DONE.getPlaceValue(),
                TaskStatus.NOT_AVAILABLE_YET.getPlaceValue()
            )
        ))
            .thenReturn(tasks);

        //when
        taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRole("123", "Claimant");

        //then
        verify(taskListRepository).findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotIn(
            "123", "Claimant",
            List.of(
                TaskStatus.AVAILABLE.getPlaceValue(),
                TaskStatus.DONE.getPlaceValue(),
                TaskStatus.NOT_AVAILABLE_YET.getPlaceValue()
            )
        );

        verify(taskListRepository, atLeast(4)).save(ArgumentMatchers.argThat(
            a -> {
                Assertions.assertEquals(
                    a.getCurrentStatus(),
                    TaskStatus.INACTIVE.getPlaceValue()
                );
                Assertions.assertEquals(
                    a.getNextStatus(),
                    TaskStatus.INACTIVE.getPlaceValue()
                );
                Assertions.assertTrue(StringUtils.isBlank(a.getHintTextCy()));
                Assertions.assertTrue(StringUtils.isBlank(a.getHintTextEn()));
                Assertions.assertEquals("Link name", a.getTaskNameEn());
                Assertions.assertEquals("Link name Welsh", a.getTaskNameCy());
                return true;
            }
        ));
    }

    @Test
    void shouldMakeProgressAbleTaskListInactiveExcludingTemplate_whenTaskListIsPresent() {

        //given
        List<TaskListEntity> tasks = new ArrayList<>();
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>")
                      .currentStatus(TaskStatus.NOT_AVAILABLE_YET.getPlaceValue())
                      .build());
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.INACTIVE.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>").build());
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.AVAILABLE.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>").build());
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.OPTIONAL.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>").build());
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.ACTION_NEEDED.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>")
                      .build());
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.IN_PROGRESS.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>")
                      .build());
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.DONE.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>")
                      .build());

        when(taskListRepository.findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotInAndTaskItemTemplateTemplateNameNot(
            "123", "Claimant",
            List.of(
                TaskStatus.AVAILABLE.getPlaceValue(),
                TaskStatus.DONE.getPlaceValue(),
                TaskStatus.NOT_AVAILABLE_YET.getPlaceValue()
            ),
            "Template"
        ))
            .thenReturn(tasks);

        //when
        taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingTemplate(
            "123",
            "Claimant",
            "Template"
        );

        //then
        verify(taskListRepository).findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotInAndTaskItemTemplateTemplateNameNot(
            "123", "Claimant",
            List.of(
                TaskStatus.AVAILABLE.getPlaceValue(),
                TaskStatus.DONE.getPlaceValue(),
                TaskStatus.NOT_AVAILABLE_YET.getPlaceValue()
            ),
            "Template"
        );

        verify(taskListRepository, atLeast(4)).save(ArgumentMatchers.argThat(
            a -> {
                Assertions.assertEquals(
                    a.getCurrentStatus(),
                    TaskStatus.INACTIVE.getPlaceValue()
                );
                Assertions.assertEquals(
                    a.getNextStatus(),
                    TaskStatus.INACTIVE.getPlaceValue()
                );
                Assertions.assertTrue(StringUtils.isBlank(a.getHintTextCy()));
                Assertions.assertTrue(StringUtils.isBlank(a.getHintTextEn()));
                Assertions.assertEquals("Link name", a.getTaskNameEn());
                Assertions.assertEquals("Link name Welsh", a.getTaskNameCy());
                return true;
            }
        ));
    }

    @Test
    void shouldMakeProgressAbleTaskListInactive_Except_Ga_whenTaskListIsPresent() {

        //given
        List<TaskListEntity> tasks = new ArrayList<>();
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>")
                      .currentStatus(TaskStatus.NOT_AVAILABLE_YET.getPlaceValue())
                      .build());
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.INACTIVE.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>").build());
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.AVAILABLE.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>").build());
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.OPTIONAL.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>").build());
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.ACTION_NEEDED.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>")
                      .build());
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.IN_PROGRESS.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>")
                      .build());
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.DONE.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>")
                      .build());

        when(taskListRepository.findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotInAndTaskItemTemplate_IdNotIn(
            "123", "Claimant",
            List.of(
                TaskStatus.AVAILABLE.getPlaceValue(), TaskStatus.DONE.getPlaceValue(),
                TaskStatus.NOT_AVAILABLE_YET.getPlaceValue()
            ),
            List.of(123L)
        ))
            .thenReturn(tasks);

        List<TaskItemTemplateEntity> categories = new ArrayList<>();
        categories.add(TaskItemTemplateEntity.builder()
                           .id(123L).taskNameCy("TaskNameCy")
                           .taskNameEn("TaskNameEn")
                           .scenarioName("Scenario.hearing")
                           .templateName("Hearing.view")
                           .taskOrder(1).hintTextCy("HintCY")
                           .hintTextEn("HintEn").role("Claimant")
                           .categoryCy("CategoryCy").categoryEn("CategoryEn")
                           .build());

        when(taskItemTemplateRepository.findByCategoryEnAndRole(
            "CategoryEn", "Claimant"))
            .thenReturn(categories);

        //when
        taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory(
            "123",
            "Claimant",
            "CategoryEn"
        );

        //then
        verify(taskItemTemplateRepository).findByCategoryEnAndRole(
            "CategoryEn", "Claimant");
        verify(taskListRepository).findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotInAndTaskItemTemplate_IdNotIn(
            "123", "Claimant",
            List.of(
                TaskStatus.AVAILABLE.getPlaceValue(), TaskStatus.DONE.getPlaceValue(),
                TaskStatus.NOT_AVAILABLE_YET.getPlaceValue()
            ),
            List.of(123L)
        );

        verify(taskListRepository, atLeast(5)).save(ArgumentMatchers.argThat(
            a -> {
                Assertions.assertEquals(
                    a.getCurrentStatus(),
                    TaskStatus.INACTIVE.getPlaceValue()
                );
                Assertions.assertEquals(
                    a.getNextStatus(),
                    TaskStatus.INACTIVE.getPlaceValue()
                );
                Assertions.assertTrue(StringUtils.isBlank(a.getHintTextCy()));
                Assertions.assertTrue(StringUtils.isBlank(a.getHintTextEn()));
                Assertions.assertEquals("Link name", a.getTaskNameEn());
                Assertions.assertEquals("Link name Welsh", a.getTaskNameCy());
                return true;
            }
        ));
    }

    @Test
    void shouldMakeProgressAbleTaskListInactiveForMultipleCategories_whenTaskListIsPresent() {

        List<TaskListEntity> tasks = new ArrayList<>();
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.ACTION_NEEDED.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>")
                      .build());

        when(taskListRepository.findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotInAndTaskItemTemplate_IdNotIn(
            "123", "Claimant",
            List.of(
                TaskStatus.AVAILABLE.getPlaceValue(), TaskStatus.DONE.getPlaceValue(),
                TaskStatus.NOT_AVAILABLE_YET.getPlaceValue()
            ),
            List.of(123L, 456L)
        )).thenReturn(tasks);

        List<TaskItemTemplateEntity> categoryA = List.of(TaskItemTemplateEntity.builder().id(123L).role("Claimant").build());
        List<TaskItemTemplateEntity> categoryB = List.of(TaskItemTemplateEntity.builder().id(456L).role("Claimant").build());

        when(taskItemTemplateRepository.findByCategoryEnAndRole("CategoryA", "Claimant")).thenReturn(categoryA);
        when(taskItemTemplateRepository.findByCategoryEnAndRole("CategoryB", "Claimant")).thenReturn(categoryB);

        taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory(
            "123",
            "Claimant",
            "CategoryA",
            "CategoryB"
        );

        verify(taskItemTemplateRepository).findByCategoryEnAndRole("CategoryA", "Claimant");
        verify(taskItemTemplateRepository).findByCategoryEnAndRole("CategoryB", "Claimant");
        verify(taskListRepository).findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotInAndTaskItemTemplate_IdNotIn(
            "123", "Claimant",
            List.of(
                TaskStatus.AVAILABLE.getPlaceValue(), TaskStatus.DONE.getPlaceValue(),
                TaskStatus.NOT_AVAILABLE_YET.getPlaceValue()
            ),
            List.of(123L, 456L)
        );
    }

    @Test
    void shouldFallbackToDefaultProgressQueryWhenCategoryTemplatesMissing() {
        List<TaskListEntity> tasks = List.of(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.IN_PROGRESS.getPlaceValue())
                      .build());

        when(taskItemTemplateRepository.findByCategoryEnAndRole("CategoryEn", "Claimant")).thenReturn(List.of());
        when(taskListRepository.findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotIn(
            "123",
            "Claimant",
            List.of(
                TaskStatus.AVAILABLE.getPlaceValue(),
                TaskStatus.DONE.getPlaceValue(),
                TaskStatus.NOT_AVAILABLE_YET.getPlaceValue()
            )
        )).thenReturn(tasks);

        taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory(
            "123",
            "Claimant",
            "CategoryEn"
        );

        verify(taskItemTemplateRepository).findByCategoryEnAndRole("CategoryEn", "Claimant");
        verify(taskListRepository).findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotIn(
            "123",
            "Claimant",
            List.of(
                TaskStatus.AVAILABLE.getPlaceValue(),
                TaskStatus.DONE.getPlaceValue(),
                TaskStatus.NOT_AVAILABLE_YET.getPlaceValue()
            )
        );
        verify(taskListRepository, never()).findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotInAndTaskItemTemplate_IdNotIn(
            any(),
            any(),
            any(),
            any()
        );
        verify(taskListRepository, atLeast(1)).save(ArgumentMatchers.argThat(
            a -> a.getCurrentStatus() == TaskStatus.INACTIVE.getPlaceValue()
        ));
    }

    @Test
    void shouldTreatEmptyExcludedCategoriesAsNoExclusions() {
        List<TaskListEntity> tasks = List.of(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.ACTION_NEEDED.getPlaceValue())
                      .build());

        when(taskListRepository.findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotIn(
            "123",
            "Claimant",
            List.of(
                TaskStatus.AVAILABLE.getPlaceValue(),
                TaskStatus.DONE.getPlaceValue(),
                TaskStatus.NOT_AVAILABLE_YET.getPlaceValue()
            )
        )).thenReturn(tasks);

        taskListService.makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory(
            "123",
            "Claimant",
            null,
            "   "
        );

        verify(taskItemTemplateRepository, never()).findByCategoryEnAndRole(any(), any());
        verify(taskListRepository).findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotIn(
            "123",
            "Claimant",
            List.of(
                TaskStatus.AVAILABLE.getPlaceValue(),
                TaskStatus.DONE.getPlaceValue(),
                TaskStatus.NOT_AVAILABLE_YET.getPlaceValue()
            )
        );
        verify(taskListRepository, never()).findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotInAndTaskItemTemplate_IdNotIn(
            any(),
            any(),
            any(),
            any()
        );
        verify(taskListRepository, atLeast(1)).save(ArgumentMatchers.argThat(
            a -> a.getCurrentStatus() == TaskStatus.INACTIVE.getPlaceValue()
        ));
    }

    @Test
    public void shouldDeleteWhenThereWereDuplicateEntriesInTheRepository() {
        TaskListEntity task = getTaskListEntity(UUID.randomUUID()).toBuilder()
            .currentStatus(TaskStatus.AVAILABLE.getPlaceValue())
            .taskNameEn("<a href=\"somewhere\">Link name</A >")
            .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>")
            .createdAt(OffsetDateTime.MAX)
            .build();

        TaskListEntity task2 = getTaskListEntity(UUID.randomUUID()).toBuilder()
            .taskNameEn("<a href=\"somewhere\">Link name</A >")
            .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>")
            .currentStatus(TaskStatus.NOT_AVAILABLE_YET.getPlaceValue())
            .createdAt(OffsetDateTime.MIN.plusDays(99L))
            .build();

        TaskListEntity task3 = getTaskListEntity(UUID.randomUUID()).toBuilder()
            .currentStatus(TaskStatus.INACTIVE.getPlaceValue())
            .taskNameEn("<a href=\"somewhere\">Link name</A >")
            .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>")
            .createdAt(OffsetDateTime.MIN.plusDays(20L))
            .build();

        List<TaskListEntity> tasks = new ArrayList<>();
        tasks.add(task);
        tasks.add(task2);
        tasks.add(task3);

        when(taskListRepository
                 .findByReferenceAndTaskItemTemplateRoleAndTaskItemTemplateTemplateName(
                     any(),
                     any(),
                     any()
                 )).thenReturn(tasks);

        taskListService.saveOrUpdate(task);

        verify(taskListRepository).deleteById(task2.getId());
        verify(taskListRepository).deleteById(task3.getId());
        verify(taskListRepository).save(task);
    }

    @Test
    void shouldMakeProgressAbleTaskListActiveExcludingTemplate() {

        //given
        List<TaskListEntity> tasks = new ArrayList<>();
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>")
                      .currentStatus(TaskStatus.NOT_AVAILABLE_YET.getPlaceValue())
                      .build());
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.INACTIVE.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>").build());
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.AVAILABLE.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>").build());
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.OPTIONAL.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>").build());
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.ACTION_NEEDED.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>")
                      .build());
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.IN_PROGRESS.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>")
                      .build());
        tasks.add(getTaskListEntity(UUID.randomUUID()).toBuilder()
                      .currentStatus(TaskStatus.DONE.getPlaceValue())
                      .taskNameEn("<a href=\"somewhere\">Link name</A >")
                      .taskNameCy("<A  href=\"somewhere\">Link name Welsh</A>")
                      .build());

        when(taskListRepository.findByReferenceAndTaskItemTemplateRoleAndTaskItemTemplateTemplateName(
            any(),
            any(),
            any()
        ))
            .thenReturn(tasks);

        //when
        taskListService.makeViewDocumentTaskAvailable("123");

        //then
        verify(taskListRepository, times(2)).findByReferenceAndTaskItemTemplateRoleAndTaskItemTemplateTemplateName(
            any(),
            any(),
            any()
        );

        ArgumentCaptor<TaskListEntity> captor = ArgumentCaptor.forClass(TaskListEntity.class);
        verify(taskListRepository, times(2)).save(captor.capture());
        List<TaskListEntity> savedTasks = captor.getAllValues();
        savedTasks.forEach(saved -> {
            assertThat(saved.getCurrentStatus()).isEqualTo(TaskStatus.AVAILABLE.getPlaceValue());
            assertThat(saved.getNextStatus()).isEqualTo(TaskStatus.AVAILABLE.getPlaceValue());
            assertThat(saved.getHintTextCy()).isEmpty();
            assertThat(saved.getHintTextEn()).isEmpty();
            assertThat(saved.getTaskNameEn()).isEqualTo("<a class=\"govuk-link\" href=\"{VIEW_EVIDENCE_UPLOAD_DOCUMENTS}\">Link name</a>");
            assertThat(saved.getTaskNameCy()).isEqualTo("<a class=\"govuk-link\" href=\"{VIEW_EVIDENCE_UPLOAD_DOCUMENTS}\">Link name Welsh</a>");
        });
    }

}
