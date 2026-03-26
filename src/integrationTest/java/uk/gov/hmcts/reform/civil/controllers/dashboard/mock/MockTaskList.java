package uk.gov.hmcts.reform.civil.controllers.dashboard.mock;

import uk.gov.hmcts.reform.dashboard.data.TaskList;
import uk.gov.hmcts.reform.dashboard.data.TaskStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class MockTaskList {

    // Private constructor to prevent instantiation
    private MockTaskList() {
        // throw an AssertionError if the constructor is accidentally invoked from within the class
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static List<TaskList> getMediationTaskListMock(String role, String reference) {
        return List.of(
            task(reference,
                "<a href={VIEW_MEDIATION_SETTLEMENT_AGREEMENT} class=\"govuk-link\">Gweld cytundeb setlo o ran cyfryngu</a>",
                "<a href={VIEW_MEDIATION_SETTLEMENT_AGREEMENT} class=\"govuk-link\">View mediation settlement agreement</a>",
                "Cyfryngu",
                "Mediation",
                role,
                TaskStatus.AVAILABLE,
                TaskStatus.AVAILABLE,
                5),
            task(reference,
                "<a>Uwchlwytho dogfennau cyfryngu</a>",
                "<a>Upload mediation documents</a>",
                "Cyfryngu",
                "Mediation",
                role,
                TaskStatus.INACTIVE,
                TaskStatus.INACTIVE,
                6),
            task(reference,
                "<a>Gweld dogfennau cyfryngu</a>",
                "<a>View mediation documents</a>",
                "Cyfryngu",
                "Mediation",
                role,
                TaskStatus.INACTIVE,
                TaskStatus.INACTIVE,
                7),
            task(reference,
                "<a>Gweld y gwrandawiad</a>",
                "<a>View hearings</a>",
                "Gwrandawiad",
                "Hearing",
                role,
                TaskStatus.INACTIVE,
                TaskStatus.INACTIVE,
                8),
            task(reference,
                "<a>Llwytho dogfennau'r gwrandawiad</a>",
                "<a>Upload hearing documents</a>",
                "Gwrandawiad",
                "Hearing",
                role,
                TaskStatus.INACTIVE,
                TaskStatus.INACTIVE,
                9),
            task(reference,
                "<a>Ychwanegu trefniadau'r treial</a>",
                "<a>Add the trial arrangements</a>",
                "Gwrandawiad",
                "Hearing",
                role,
                TaskStatus.INACTIVE,
                TaskStatus.INACTIVE,
                10),
            task(reference,
                "<a>Talu ffi'r gwrandawiad</a>",
                "<a>Pay the hearing fee</a>",
                "Gwrandawiad",
                "Hearing",
                role,
                TaskStatus.INACTIVE,
                TaskStatus.INACTIVE,
                11),
            task(reference,
                "<a>Gweld y bwndel</a>",
                "<a>View the bundle</a>",
                "Gwrandawiad",
                "Hearing",
                role,
                TaskStatus.INACTIVE,
                TaskStatus.INACTIVE,
                12),
            task(reference,
                "<a>Gweld y Dyfarniad</a>",
                "<a>View the judgment</a>",
                "Dyfarniadau gan y llys",
                "Judgment from the court",
                role,
                TaskStatus.INACTIVE,
                TaskStatus.INACTIVE,
                13),
            task(reference,
                "<a>Gweld ceisiadau</a>",
                "<a>View applications</a>",
                "Ceisiadau",
                "Applications",
                role,
                TaskStatus.INACTIVE,
                TaskStatus.INACTIVE,
                14)
        );
    }

    public static List<TaskList> getMediationTaskListMockWithoutPayHearing(String role, String reference) {
        return List.of(
            task(reference,
                "<a href={VIEW_MEDIATION_SETTLEMENT_AGREEMENT} class=\"govuk-link\">Gweld cytundeb setlo o ran cyfryngu</a>",
                "<a href={VIEW_MEDIATION_SETTLEMENT_AGREEMENT} class=\"govuk-link\">View mediation settlement agreement</a>",
                "Cyfryngu",
                "Mediation",
                role,
                TaskStatus.AVAILABLE,
                TaskStatus.AVAILABLE,
                5),
            task(reference,
                "<a>Uwchlwytho dogfennau cyfryngu</a>",
                "<a>Upload mediation documents</a>",
                "Cyfryngu",
                "Mediation",
                role,
                TaskStatus.INACTIVE,
                TaskStatus.INACTIVE,
                6),
            task(reference,
                "<a>Gweld dogfennau cyfryngu</a>",
                "<a>View mediation documents</a>",
                "Cyfryngu",
                "Mediation",
                role,
                TaskStatus.INACTIVE,
                TaskStatus.INACTIVE,
                7),
            task(reference,
                "<a>Gweld y gwrandawiad</a>",
                "<a>View hearings</a>",
                "Gwrandawiad",
                "Hearing",
                role,
                TaskStatus.INACTIVE,
                TaskStatus.INACTIVE,
                8),
            task(reference,
                "<a>Llwytho dogfennau'r gwrandawiad</a>",
                "<a>Upload hearing documents</a>",
                "Gwrandawiad",
                "Hearing",
                role,
                TaskStatus.INACTIVE,
                TaskStatus.INACTIVE,
                9),
            task(reference,
                "<a>Ychwanegu trefniadau'r treial</a>",
                "<a>Add the trial arrangements</a>",
                "Gwrandawiad",
                "Hearing",
                role,
                TaskStatus.INACTIVE,
                TaskStatus.INACTIVE,
                10),
            task(reference,
                "<a>Gweld y bwndel</a>",
                "<a>View the bundle</a>",
                "Gwrandawiad",
                "Hearing",
                role,
                TaskStatus.INACTIVE,
                TaskStatus.INACTIVE,
                12),
            task(reference,
                "<a>Gweld y Dyfarniad</a>",
                "<a>View the judgment</a>",
                "Dyfarniadau gan y llys",
                "Judgment from the court",
                role,
                TaskStatus.INACTIVE,
                TaskStatus.INACTIVE,
                13),
            task(reference,
                "<a>Cadarnhewch eich bod wedi talu dyled dyfarniad (CCJ)</a>",
                "<a>Confirm you've paid a judgment (CCJ) debt</a>",
                "Dyfarniadau gan y llys",
                "Judgment from the court",
                role,
                TaskStatus.INACTIVE,
                TaskStatus.INACTIVE,
                14),
            task(reference,
                "<a>Gweld ceisiadau</a>",
                "<a>View applications</a>",
                "Ceisiadau",
                "Applications",
                role,
                TaskStatus.INACTIVE,
                TaskStatus.INACTIVE,
                15)
        );
    }

    public static List<TaskList> getMediationUnsuccessfulTaskListMock(String role, String reference) {
        return List.of(
            task(reference,
                "<a>Gweld cytundeb setlo o ran cyfryngu</a>",
                "<a>View mediation settlement agreement</a>",
                "Cyfryngu",
                "Mediation",
                role,
                TaskStatus.INACTIVE,
                TaskStatus.INACTIVE,
                5),
            task(reference,
                "<a href={UPLOAD_MEDIATION_DOCUMENTS} class=\"govuk-link\">Uwchlwytho dogfennau cyfryngu</a>",
                "<a href={UPLOAD_MEDIATION_DOCUMENTS} class=\"govuk-link\">Upload mediation documents</a>",
                "Cyfryngu",
                "Mediation",
                role,
                TaskStatus.ACTION_NEEDED,
                TaskStatus.ACTION_NEEDED,
                6),
            task(reference,
                "<a>Gweld dogfennau cyfryngu</a>",
                "<a>View mediation documents</a>",
                "Cyfryngu",
                "Mediation",
                role,
                TaskStatus.NOT_AVAILABLE_YET,
                TaskStatus.NOT_AVAILABLE_YET,
                7)
        );
    }

    public static List<TaskList> getUploadMediationTaskListMock(String role, String reference) {
        OffsetDateTime now = OffsetDateTime.now();
        UUID taskId = UUID.fromString("8c2712da-47ce-4050-bbee-650134a7b9e7");
        return List.of(
            task(reference,
                "<a href={UPLOAD_MEDIATION_DOCUMENTS} class=\"govuk-link\">Uwchlwytho dogfennau cyfryngu</a>",
                "<a href={UPLOAD_MEDIATION_DOCUMENTS} class=\"govuk-link\">Upload mediation documents</a>",
                "Cyfryngu",
                "Mediation",
                role,
                TaskStatus.IN_PROGRESS,
                TaskStatus.IN_PROGRESS,
                6,
                taskId,
                now,
                now),
            task(reference,
                "<a href={VIEW_MEDIATION_DOCUMENTS} class=\"govuk-link\">Gweld dogfennau cyfryngu</a>",
                "<a href={VIEW_MEDIATION_DOCUMENTS} class=\"govuk-link\">View mediation documents</a>",
                "Cyfryngu",
                "Mediation",
                role,
                TaskStatus.AVAILABLE,
                TaskStatus.AVAILABLE,
                7,
                taskId,
                now,
                now)
        );
    }

    public static List<TaskList> getMediationUnsuccessfulTaskListViewMediationNotAvailableYetMock(String role, String reference) {
        return List.of(
            task(reference,
                "<a>Gweld cytundeb setlo o ran cyfryngu</a>",
                "<a>View mediation settlement agreement</a>",
                "Cyfryngu",
                "Mediation",
                role,
                TaskStatus.INACTIVE,
                TaskStatus.INACTIVE,
                5),
            task(reference,
                "<a>Uwchlwytho dogfennau cyfryngu</a>",
                "<a>Upload mediation documents</a>",
                "Cyfryngu",
                "Mediation",
                role,
                TaskStatus.INACTIVE,
                TaskStatus.INACTIVE,
                6),
            task(reference,
                "<a>Gweld dogfennau cyfryngu</a>",
                "<a>View mediation documents</a>",
                "Cyfryngu",
                "Mediation",
                role,
                TaskStatus.NOT_AVAILABLE_YET,
                TaskStatus.NOT_AVAILABLE_YET,
                7)
        );
    }

    public static List<TaskList> getMediationTaskListWithInactive(String role, String reference) {
        return List.of(
            task(reference,
                "<a>Gweld cytundeb setlo o ran cyfryngu</a>",
                "<a>View mediation settlement agreement</a>",
                "Cyfryngu",
                "Mediation",
                role,
                TaskStatus.INACTIVE,
                TaskStatus.INACTIVE,
                5),
            task(reference,
                "<a>Uwchlwytho dogfennau cyfryngu</a>",
                "<a>Upload mediation documents</a>",
                "Cyfryngu",
                "Mediation",
                role,
                TaskStatus.INACTIVE,
                TaskStatus.INACTIVE,
                6),
            task(reference,
                "<a>Gweld dogfennau cyfryngu</a>",
                "<a>View mediation documents</a>",
                "Cyfryngu",
                "Mediation",
                role,
                TaskStatus.INACTIVE,
                TaskStatus.INACTIVE,
                7)
        );
    }

    public static List<TaskList> getUploadMediationTaskListViewMediationAvailableMock(String role, String reference) {
        OffsetDateTime now = OffsetDateTime.now();
        return List.of(
            task(reference,
                "<a href={VIEW_MEDIATION_DOCUMENTS} class=\"govuk-link\">Gweld dogfennau cyfryngu</a>",
                "<a href={VIEW_MEDIATION_DOCUMENTS} class=\"govuk-link\">View mediation documents</a>",
                "Cyfryngu",
                "Mediation",
                role,
                TaskStatus.AVAILABLE,
                TaskStatus.AVAILABLE,
                7,
                UUID.fromString("8c2712da-47ce-4050-bbee-650134a7b9e7"),
                now,
                now)
        );
    }

    private static TaskList task(String reference,
                                 String taskNameCy,
                                 String taskNameEn,
                                 String categoryCy,
                                 String categoryEn,
                                 String role,
                                 TaskStatus currentStatus,
                                 TaskStatus nextStatus,
                                 int taskOrder) {
        return task(reference, taskNameCy, taskNameEn, categoryCy, categoryEn, role, currentStatus, nextStatus, taskOrder, null, null, null);
    }

    private static TaskList task(String reference,
                                 String taskNameCy,
                                 String taskNameEn,
                                 String categoryCy,
                                 String categoryEn,
                                 String role,
                                 TaskStatus currentStatus,
                                 TaskStatus nextStatus,
                                 int taskOrder,
                                 UUID id,
                                 OffsetDateTime createdAt,
                                 OffsetDateTime updatedAt) {
        TaskList task = new TaskList();
        task.setReference(reference);
        task.setTaskNameCy(taskNameCy);
        task.setTaskNameEn(taskNameEn);
        task.setCategoryCy(categoryCy);
        task.setCategoryEn(categoryEn);
        task.setRole(role);
        task.setCurrentStatusEn(currentStatus.getName());
        task.setCurrentStatusCy(currentStatus.getWelshName());
        task.setNextStatusEn(nextStatus.getName());
        task.setNextStatusCy(nextStatus.getWelshName());
        task.setTaskOrder(taskOrder);
        task.setId(id);
        task.setCreatedAt(createdAt);
        task.setUpdatedAt(updatedAt);
        return task;
    }
}
