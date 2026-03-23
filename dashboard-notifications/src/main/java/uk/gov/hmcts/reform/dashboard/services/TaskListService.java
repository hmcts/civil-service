package uk.gov.hmcts.reform.dashboard.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.dashboard.data.TaskList;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;
import uk.gov.hmcts.reform.dashboard.entities.TaskItemTemplateEntity;
import uk.gov.hmcts.reform.dashboard.entities.TaskListEntity;
import uk.gov.hmcts.reform.dashboard.repositories.TaskItemTemplateRepository;
import uk.gov.hmcts.reform.dashboard.repositories.TaskListRepository;
import uk.gov.hmcts.reform.dashboard.utilities.StringUtility;

import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class TaskListService {

    private final TaskListRepository taskListRepository;
    private final TaskItemTemplateRepository taskItemTemplateRepository;
    private final EntityManager entityManager;
    private static final String DOCUMENT_TEMPLATE_NAME = "Hearing.Document.View";
    private static final List<String> ROLES = List.of("CLAIMANT", "DEFENDANT");

    @Autowired
    public TaskListService(TaskListRepository taskListRepository,
                           TaskItemTemplateRepository taskItemTemplateRepository,
                           EntityManager entityManager) {
        this.taskListRepository = taskListRepository;
        this.taskItemTemplateRepository = taskItemTemplateRepository;
        this.entityManager = entityManager;
    }

    public List<TaskList> getTaskList(String ccdCaseIdentifier, String roleType) {

        List<TaskListEntity> taskListEntityList = taskListRepository.findByReferenceAndTaskItemTemplateRole(
            ccdCaseIdentifier,
            roleType
        );

        return taskListEntityList.stream()
            .sorted(Comparator.comparingInt(t -> t.getTaskItemTemplate().getTaskOrder()))
            .map(TaskList::from)
            .toList();
    }

    @Transactional
    public void updateTask(TaskListEntity taskList) {
        entityManager.joinTransaction();
        OffsetDateTime updatedAt = OffsetDateTime.now();

        Optional<TaskListEntity> existingEntity = taskListRepository.findById(taskList.getId());
        log.info("Dashboard task lookup completed id={} found={}", taskList.getId(), existingEntity.isPresent());

        if (existingEntity.isPresent()) {
            TaskListEntity beingUpdated = existingEntity.get();
            log.info(
                "Saving dashboard task id={} newCurrentStatus={} newNextStatus={} newUpdatedAt={} newUpdatedBy={}",
                beingUpdated.getId(),
                taskList.getCurrentStatus(),
                taskList.getNextStatus(),
                updatedAt,
                taskList.getUpdatedBy() != null ? taskList.getUpdatedBy() : beingUpdated.getUpdatedBy()
            );

            final String previousUpdatedBy = beingUpdated.getUpdatedBy();
            final OffsetDateTime previousUpdatedAt = beingUpdated.getUpdatedAt();
            final Integer previousCurrentStatus = beingUpdated.getCurrentStatus();
            final Integer previousNextStatus = beingUpdated.getNextStatus();
            beingUpdated.setCurrentStatus(taskList.getCurrentStatus());
            beingUpdated.setNextStatus(taskList.getNextStatus());
            beingUpdated.setUpdatedAt(updatedAt);
            if (taskList.getUpdatedBy() != null) {
                beingUpdated.setUpdatedBy(taskList.getUpdatedBy());
            }

            try {
                taskListRepository.save(beingUpdated);
                log.info("Dashboard task save invoked successfully id={}", beingUpdated.getId());
                entityManager.flush();
            } catch (RuntimeException exception) {
                log.error(
                    "Dashboard task persistence failed id={} reference={} templateId={} currentStatus={} nextStatus={} updatedAt={} updatedBy={}",
                    beingUpdated.getId(),
                    beingUpdated.getReference(),
                    beingUpdated.getTaskItemTemplate() != null ? beingUpdated.getTaskItemTemplate().getId() : null,
                    beingUpdated.getCurrentStatus(),
                    beingUpdated.getNextStatus(),
                    beingUpdated.getUpdatedAt(),
                    beingUpdated.getUpdatedBy(),
                    exception
                );
                throw exception;
            }
            log.info(
                "Updated dashboard task id={} currentStatus {}->{} nextStatus {}->{} updatedAt {}->{} updatedBy {}->{}",
                beingUpdated.getId(),
                previousCurrentStatus,
                beingUpdated.getCurrentStatus(),
                previousNextStatus,
                beingUpdated.getNextStatus(),
                previousUpdatedAt,
                beingUpdated.getUpdatedAt(),
                previousUpdatedBy,
                beingUpdated.getUpdatedBy()
            );
        } else {
            log.warn("Dashboard task not found for update id={}", taskList.getId());
        }
    }

    @Transactional
    public TaskListEntity saveOrUpdate(TaskListEntity taskList) {

        TaskItemTemplateEntity taskItemTemplate = taskList.getTaskItemTemplate();
        List<TaskListEntity> entities = taskListRepository
            .findByReferenceAndTaskItemTemplateRoleAndTaskItemTemplateTemplateName(
                taskList.getReference(),
                taskItemTemplate.getRole(),
                taskItemTemplate.getTemplateName()
            );

        Optional<TaskListEntity> latestEntity = entities.stream()
            .max(Comparator.comparing(TaskListEntity::getCreatedAt));

        TaskListEntity beingUpdated = taskList;
        if (latestEntity.isPresent()) {
            beingUpdated = copyTaskList(taskList);
            beingUpdated.setId(latestEntity.get().getId());
            entities.stream()
                .filter(e -> !e.equals(latestEntity.get()))
                .forEach(duplicateEntity -> taskListRepository.deleteById(duplicateEntity.getId()));
        }

        return taskListRepository.save(beingUpdated);
    }

    @Transactional
    public TaskListEntity updateTaskListItem(UUID taskItemIdentifier) {

        Optional<TaskListEntity> existingEntity = taskListRepository.findById(taskItemIdentifier);

        return existingEntity.map(taskListEntity -> {
            TaskListEntity updated = copyTaskList(taskListEntity);
            updated.setCurrentStatus(taskListEntity.getNextStatus());
            return taskListRepository.save(updated);
        }).orElseThrow(() -> new IllegalArgumentException("Invalid task item identifier " + taskItemIdentifier));
    }

    private void makeProgressAbleTasksInactiveForCaseIdentifierAndRole(String caseIdentifier, String role, String excludedCategory, String excludedTemplate) {
        log.info(
            "makeProgressAbleTasksInactiveForCaseIdentifierAndRole caseIdentifier:{} role: {} excludedCategory: {} excludedTemplate: {}",
            caseIdentifier,
            role,
            excludedCategory,
            excludedTemplate
        );
        List<TaskListEntity> tasks = new ArrayList<>();
        if (Objects.nonNull(excludedCategory)) {
            List<TaskItemTemplateEntity> categories = taskItemTemplateRepository.findByCategoryEnAndRole(
                excludedCategory,
                role
            );
            if (Objects.nonNull(categories)) {
                List<Long> catIds = categories.stream().map(TaskItemTemplateEntity::getId).toList();
                tasks = taskListRepository.findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotInAndTaskItemTemplate_IdNotIn(
                    caseIdentifier, role, List.of(
                        TaskStatus.AVAILABLE.getPlaceValue(), TaskStatus.DONE.getPlaceValue(),
                        TaskStatus.NOT_AVAILABLE_YET.getPlaceValue()
                    ), catIds
                );
            }
        } else if (Objects.nonNull(excludedTemplate)) {
            tasks = taskListRepository.findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotInAndTaskItemTemplateTemplateNameNot(
                caseIdentifier, role, List.of(
                    TaskStatus.AVAILABLE.getPlaceValue(), TaskStatus.DONE.getPlaceValue(),
                    TaskStatus.NOT_AVAILABLE_YET.getPlaceValue()
                ), excludedTemplate
            );
        } else {
            tasks = taskListRepository.findByReferenceAndTaskItemTemplateRoleAndCurrentStatusNotIn(
                caseIdentifier, role, List.of(
                    TaskStatus.AVAILABLE.getPlaceValue(), TaskStatus.DONE.getPlaceValue(),
                    TaskStatus.NOT_AVAILABLE_YET.getPlaceValue()
                )
            );
        }
        tasks.forEach(t -> {
            TaskListEntity task = copyTaskList(t);
            task.setCurrentStatus(TaskStatus.INACTIVE.getPlaceValue());
            task.setNextStatus(TaskStatus.INACTIVE.getPlaceValue());
            task.setHintTextCy("");
            task.setHintTextEn("");
            task.setUpdatedAt(OffsetDateTime.now());
            task.setTaskNameEn(StringUtility.removeAnchor(t.getTaskNameEn()));
            task.setTaskNameCy(StringUtility.removeAnchor(t.getTaskNameCy()));
            log.info("{} task made inactive for claim = {}", task.getTaskNameEn(), caseIdentifier);
            taskListRepository.save(task);
        });
        log.info("Total {} tasks made inactive for claim = {}", tasks.size(), caseIdentifier);
    }

    @Transactional
    public void makeProgressAbleTasksInactiveForCaseIdentifierAndRole(String caseIdentifier, String role) {
        log.info(
            "makeProgressAbleTasksInactiveForCaseIdentifierAndRole caseIdentifier: {} role: {}",
            caseIdentifier,
            role
        );
        makeProgressAbleTasksInactiveForCaseIdentifierAndRole(caseIdentifier, role, null, null);
    }

    @Transactional
    public void makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory(String caseIdentifier, String role, String excludedCategory) {
        log.info(
            "makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingCategory caseIdentifier:{} role: {} excludedCategory: {}",
            caseIdentifier, role, excludedCategory
        );
        makeProgressAbleTasksInactiveForCaseIdentifierAndRole(caseIdentifier, role, excludedCategory, null);
    }

    @Transactional
    public void makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingTemplate(String caseIdentifier, String role, String excludedTemplate) {
        log.info(
            "makeProgressAbleTasksInactiveForCaseIdentifierAndRoleExcludingTemplate caseIdentifier:{} role: {} excludedTemplate: {}",
            caseIdentifier, role, excludedTemplate
        );
        makeProgressAbleTasksInactiveForCaseIdentifierAndRole(caseIdentifier, role, null, excludedTemplate);
    }

    @Transactional
    public void makeViewDocumentTaskAvailable(String caseIdentifier) {
        ROLES.forEach(role -> makeViewDocumentTaskAvailable(caseIdentifier, role));
    }

    private void makeViewDocumentTaskAvailable(String caseIdentifier, String role) {
        List<TaskListEntity> tasks = taskListRepository.findByReferenceAndTaskItemTemplateRoleAndTaskItemTemplateTemplateName(
            caseIdentifier,
            role,
            DOCUMENT_TEMPLATE_NAME
        );
        tasks.stream().filter(task -> task.getCurrentStatus() == TaskStatus.NOT_AVAILABLE_YET.getPlaceValue()).toList()
            .forEach(t -> {
                TaskListEntity task = copyTaskList(t);
                task.setCurrentStatus(TaskStatus.AVAILABLE.getPlaceValue());
                task.setNextStatus(TaskStatus.AVAILABLE.getPlaceValue());
                task.setHintTextCy("");
                task.setHintTextEn("");
                task.setTaskNameEn(StringUtility.activateLink(t.getTaskNameEn()));
                task.setTaskNameCy(StringUtility.activateLink(t.getTaskNameCy()));
                taskListRepository.save(task);
            });
    }

    private TaskListEntity copyTaskList(TaskListEntity task) {
        return new TaskListEntity(
            task.getId(),
            task.getTaskItemTemplate(),
            task.getReference(),
            task.getCurrentStatus(),
            task.getNextStatus(),
            task.getTaskNameEn(),
            task.getHintTextEn(),
            task.getTaskNameCy(),
            task.getHintTextCy(),
            task.getCreatedAt(),
            task.getUpdatedAt(),
            task.getUpdatedBy(),
            task.getMessageParams()
        );
    }
}
