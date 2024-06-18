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
            TaskList.builder()
                .reference(reference)
                .taskNameCy(
                    "<a href={VIEW_MEDIATION_SETTLEMENT_AGREEMENT} class=\"govuk-link\">Gweld cytundeb setlo o ran cyfryngu</a>")
                .taskNameEn(
                    "<a href={VIEW_MEDIATION_SETTLEMENT_AGREEMENT} class=\"govuk-link\">View mediation settlement agreement</a>")
                .categoryCy("Cyfryngu")
                .categoryEn("Mediation")
                .role(role)
                .currentStatusEn(TaskStatus.AVAILABLE.getName())
                .currentStatusCy(TaskStatus.AVAILABLE.getWelshName())
                .nextStatusEn(TaskStatus.AVAILABLE.getName())
                .nextStatusCy(TaskStatus.AVAILABLE.getWelshName())
                .taskOrder(5)
                .build(),
            TaskList.builder()
                .reference(reference)
                .taskNameCy("<a>Uwchlwytho dogfennau cyfryngu</a>")
                .taskNameEn("<a>Upload mediation documents</a>")
                .categoryCy("Cyfryngu")
                .categoryEn("Mediation")
                .role(role)
                .currentStatusEn(TaskStatus.INACTIVE.getName())
                .currentStatusCy(TaskStatus.INACTIVE.getWelshName())
                .nextStatusEn(TaskStatus.INACTIVE.getName())
                .nextStatusCy(TaskStatus.INACTIVE.getWelshName())
                .taskOrder(6)
                .build(),
            TaskList.builder()
                .reference(reference)
                .taskNameCy("<a>Gweld dogfennau cyfryngu</a>")
                .taskNameEn("<a>View mediation documents</a>")
                .categoryCy("Cyfryngu")
                .categoryEn("Mediation")
                .role(role)
                .currentStatusEn(TaskStatus.INACTIVE.getName())
                .currentStatusCy(TaskStatus.INACTIVE.getWelshName())
                .nextStatusEn(TaskStatus.INACTIVE.getName())
                .nextStatusCy(TaskStatus.INACTIVE.getWelshName())
                .taskOrder(7)
                .build(),
            TaskList.builder()
                .reference(reference)
                .taskNameCy("<a>Gweld y gwrandawiad</a>")
                .taskNameEn("<a>View hearings</a>")
                .categoryCy("Gwrandawiad")
                .categoryEn("Hearing")
                .role(role)
                .currentStatusEn(TaskStatus.INACTIVE.getName())
                .currentStatusCy(TaskStatus.INACTIVE.getWelshName())
                .nextStatusEn(TaskStatus.INACTIVE.getName())
                .nextStatusCy(TaskStatus.INACTIVE.getWelshName())
                .taskOrder(8)
                .build(),
            TaskList.builder()
                .reference(reference)
                .taskNameCy("<a>Llwytho dogfennau'r gwrandawiad</a>")
                .taskNameEn("<a>Upload hearing documents</a>")
                .categoryCy("Gwrandawiad")
                .categoryEn("Hearing")
                .role(role)
                .currentStatusEn(TaskStatus.INACTIVE.getName())
                .currentStatusCy(TaskStatus.INACTIVE.getWelshName())
                .nextStatusEn(TaskStatus.INACTIVE.getName())
                .nextStatusCy(TaskStatus.INACTIVE.getWelshName())
                .taskOrder(9)
                .build(),
            TaskList.builder()
                .reference(reference)
                .taskNameCy("<a>Ychwanegu trefniadau'r treial</a>")
                .taskNameEn("<a>Add the trial arrangements</a>")
                .categoryCy("Gwrandawiad")
                .categoryEn("Hearing")
                .role(role)
                .currentStatusEn(TaskStatus.INACTIVE.getName())
                .currentStatusCy(TaskStatus.INACTIVE.getWelshName())
                .nextStatusEn(TaskStatus.INACTIVE.getName())
                .nextStatusCy(TaskStatus.INACTIVE.getWelshName())
                .taskOrder(10)
                .build(),
            TaskList.builder()
                .reference(reference)
                .taskNameCy("<a>Talu ffi'r gwrandawiad</a>")
                .taskNameEn("<a>Pay the hearing fee</a>")
                .categoryCy("Gwrandawiad")
                .categoryEn("Hearing")
                .role(role)
                .currentStatusEn(TaskStatus.INACTIVE.getName())
                .currentStatusCy(TaskStatus.INACTIVE.getWelshName())
                .nextStatusEn(TaskStatus.INACTIVE.getName())
                .nextStatusCy(TaskStatus.INACTIVE.getWelshName())
                .taskOrder(11)
                .build(),
            TaskList.builder()
                .reference(reference)
                .taskNameCy("<a>Gweld y bwndel</a>")
                .taskNameEn("<a>View the bundle</a>")
                .categoryCy("Gwrandawiad")
                .categoryEn("Hearing")
                .role(role)
                .currentStatusEn(TaskStatus.INACTIVE.getName())
                .currentStatusCy(TaskStatus.INACTIVE.getWelshName())
                .nextStatusEn(TaskStatus.INACTIVE.getName())
                .nextStatusCy(TaskStatus.INACTIVE.getWelshName())
                .taskOrder(12)
                .build(),
            TaskList.builder()
                .reference(reference)
                .taskNameCy("<a>Gweld y Dyfarniad</a>")
                .taskNameEn("<a>View the judgment</a>")
                .categoryCy("Dyfarniad gan y llys")
                .categoryEn("Judgment from the court")
                .role(role)
                .currentStatusEn(TaskStatus.INACTIVE.getName())
                .currentStatusCy(TaskStatus.INACTIVE.getWelshName())
                .nextStatusEn(TaskStatus.INACTIVE.getName())
                .nextStatusCy(TaskStatus.INACTIVE.getWelshName())
                .taskOrder(13)
                .build(),
            TaskList.builder()
                .reference(reference)
                .taskNameCy("<a>Gweld y cais i gyd</a>")
                .taskNameEn("<a>View applications</a>")
                .categoryCy("Ceisiadau")
                .categoryEn("Applications")
                .role(role)
                .currentStatusEn(TaskStatus.INACTIVE.getName())
                .currentStatusCy(TaskStatus.INACTIVE.getWelshName())
                .nextStatusEn(TaskStatus.INACTIVE.getName())
                .nextStatusCy(TaskStatus.INACTIVE.getWelshName())
                .taskOrder(14)
                .build()
        );
    }

    public static List<TaskList> getMediationUnsuccessfulTaskListMock(String role, String reference) {
        return List.of(
            TaskList.builder()
                .reference(reference)
                .taskNameCy(
                    "<a>Gweld cytundeb setlo o ran cyfryngu</a>")
                .taskNameEn(
                    "<a>View mediation settlement agreement</a>")
                .categoryCy("Cyfryngu")
                .categoryEn("Mediation")
                .role(role)
                .currentStatusEn(TaskStatus.INACTIVE.getName())
                .currentStatusCy(TaskStatus.INACTIVE.getWelshName())
                .nextStatusEn(TaskStatus.INACTIVE.getName())
                .nextStatusCy(TaskStatus.INACTIVE.getWelshName())
                .taskOrder(5)
                .build(),
            TaskList.builder()
                .reference(reference)
                .taskNameCy("<a href={UPLOAD_MEDIATION_DOCUMENTS} class=\"govuk-link\">Uwchlwytho dogfennau cyfryngu</a>")
                .taskNameEn("<a href={UPLOAD_MEDIATION_DOCUMENTS} class=\"govuk-link\">Upload mediation documents</a>")
                .categoryCy("Cyfryngu")
                .categoryEn("Mediation")
                .role(role)
                .currentStatusEn(TaskStatus.ACTION_NEEDED.getName())
                .currentStatusCy(TaskStatus.ACTION_NEEDED.getWelshName())
                .nextStatusEn(TaskStatus.ACTION_NEEDED.getName())
                .nextStatusCy(TaskStatus.ACTION_NEEDED.getWelshName())
                .taskOrder(6)
                .build(),
            TaskList.builder()
                .reference(reference)
                .taskNameCy("<a>Gweld dogfennau cyfryngu</a>")
                .taskNameEn("<a>View mediation documents</a>")
                .categoryCy("Cyfryngu")
                .categoryEn("Mediation")
                .role(role)
                .currentStatusEn(TaskStatus.NOT_AVAILABLE_YET.getName())
                .currentStatusCy(TaskStatus.NOT_AVAILABLE_YET.getWelshName())
                .nextStatusEn(TaskStatus.NOT_AVAILABLE_YET.getName())
                .nextStatusCy(TaskStatus.NOT_AVAILABLE_YET.getWelshName())
                .taskOrder(7)
                .build());
    }

    public static List<TaskList> getUploadMediationTaskListMock(String role, String reference) {
        return List.of(
            TaskList.builder()
                .reference(reference)
                .taskNameCy("<a href={UPLOAD_MEDIATION_DOCUMENTS} class=\"govuk-link\">Uwchlwytho dogfennau cyfryngu</a>")
                .taskNameEn("<a href={UPLOAD_MEDIATION_DOCUMENTS} class=\"govuk-link\">Upload mediation documents</a>")
                .categoryCy("Cyfryngu")
                .categoryEn("Mediation")
                .role(role)
                .currentStatusEn(TaskStatus.IN_PROGRESS.getName())
                .currentStatusCy(TaskStatus.IN_PROGRESS.getWelshName())
                .nextStatusEn(TaskStatus.IN_PROGRESS.getName())
                .nextStatusCy(TaskStatus.IN_PROGRESS.getWelshName())
                .taskOrder(6)
                .updatedAt(OffsetDateTime.now())
                .createdAt(OffsetDateTime.now())
                .id(UUID.fromString("8c2712da-47ce-4050-bbee-650134a7b9e7"))
                .build(),
            TaskList.builder()
                .reference(reference)
                .taskNameCy("<a href={VIEW_MEDIATION_DOCUMENTS} class=\"govuk-link\">Gweld dogfennau cyfryngu</a>")
                .taskNameEn("<a href={VIEW_MEDIATION_DOCUMENTS} class=\"govuk-link\">View mediation documents</a>")
                .categoryCy("Cyfryngu")
                .categoryEn("Mediation")
                .role(role)
                .currentStatusEn(TaskStatus.AVAILABLE.getName())
                .currentStatusCy(TaskStatus.AVAILABLE.getWelshName())
                .nextStatusEn(TaskStatus.AVAILABLE.getName())
                .nextStatusCy(TaskStatus.AVAILABLE.getWelshName())
                .updatedAt(OffsetDateTime.now())
                .createdAt(OffsetDateTime.now())
                .id(UUID.fromString("8c2712da-47ce-4050-bbee-650134a7b9e7"))
                .taskOrder(7)
                .build());
    }

    public static List<TaskList> getMediationUnsuccessfulTaskListViewMediationNotAvailableYetMock(String role, String reference) {
        return List.of(
            TaskList.builder()
                .reference(reference)
                .taskNameCy(
                    "<a>Gweld cytundeb setlo o ran cyfryngu</a>")
                .taskNameEn(
                    "<a>View mediation settlement agreement</a>")
                .categoryCy("Cyfryngu")
                .categoryEn("Mediation")
                .role(role)
                .currentStatusEn(TaskStatus.INACTIVE.getName())
                .currentStatusCy(TaskStatus.INACTIVE.getWelshName())
                .nextStatusEn(TaskStatus.INACTIVE.getName())
                .nextStatusCy(TaskStatus.INACTIVE.getWelshName())
                .taskOrder(5)
                .build(),
            TaskList.builder()
                .reference(reference)
                .taskNameCy("<a>Uwchlwytho dogfennau cyfryngu</a>")
                .taskNameEn("<a>Upload mediation documents</a>")
                .categoryCy("Cyfryngu")
                .categoryEn("Mediation")
                .role(role)
                .currentStatusEn(TaskStatus.INACTIVE.getName())
                .currentStatusCy(TaskStatus.INACTIVE.getWelshName())
                .nextStatusEn(TaskStatus.INACTIVE.getName())
                .nextStatusCy(TaskStatus.INACTIVE.getWelshName())
                .taskOrder(6)
                .build(),
            TaskList.builder()
                .reference(reference)
                .taskNameCy("<a>Gweld dogfennau cyfryngu</a>")
                .taskNameEn("<a>View mediation documents</a>")
                .categoryCy("Cyfryngu")
                .categoryEn("Mediation")
                .role(role)
                .currentStatusEn(TaskStatus.NOT_AVAILABLE_YET.getName())
                .currentStatusCy(TaskStatus.NOT_AVAILABLE_YET.getWelshName())
                .nextStatusEn(TaskStatus.NOT_AVAILABLE_YET.getName())
                .nextStatusCy(TaskStatus.NOT_AVAILABLE_YET.getWelshName())
                .taskOrder(7)
                .build());
    }

    public static List<TaskList> getMediationTaskListWithInactive(String role, String reference) {
        return List.of(
            TaskList.builder()
                .reference(reference)
                .taskNameCy(
                    "<a>Gweld cytundeb setlo o ran cyfryngu</a>")
                .taskNameEn(
                    "<a>View mediation settlement agreement</a>")
                .categoryCy("Cyfryngu")
                .categoryEn("Mediation")
                .role(role)
                .currentStatusEn(TaskStatus.INACTIVE.getName())
                .currentStatusCy(TaskStatus.INACTIVE.getWelshName())
                .nextStatusEn(TaskStatus.INACTIVE.getName())
                .nextStatusCy(TaskStatus.INACTIVE.getWelshName())
                .taskOrder(5)
                .build(),
            TaskList.builder()
                .reference(reference)
                .taskNameCy("<a>Uwchlwytho dogfennau cyfryngu</a>")
                .taskNameEn("<a>Upload mediation documents</a>")
                .categoryCy("Cyfryngu")
                .categoryEn("Mediation")
                .role(role)
                .currentStatusEn(TaskStatus.INACTIVE.getName())
                .currentStatusCy(TaskStatus.INACTIVE.getWelshName())
                .nextStatusEn(TaskStatus.INACTIVE.getName())
                .nextStatusCy(TaskStatus.INACTIVE.getWelshName())
                .taskOrder(6)
                .build(),
            TaskList.builder()
                .reference(reference)
                .taskNameCy("<a>Gweld dogfennau cyfryngu</a>")
                .taskNameEn("<a>View mediation documents</a>")
                .categoryCy("Cyfryngu")
                .categoryEn("Mediation")
                .role(role)
                .currentStatusEn(TaskStatus.INACTIVE.getName())
                .currentStatusCy(TaskStatus.INACTIVE.getWelshName())
                .nextStatusEn(TaskStatus.INACTIVE.getName())
                .nextStatusCy(TaskStatus.INACTIVE.getWelshName())
                .taskOrder(7)
                .build()
        );
    }

    public static List<TaskList> getUploadMediationTaskListViewMediationAvailableMock(String role, String reference) {
        return List.of(
            TaskList.builder()
                .reference(reference)
                .taskNameCy("<a href={VIEW_MEDIATION_DOCUMENTS} class=\"govuk-link\">Gweld dogfennau cyfryngu</a>")
                .taskNameEn("<a href={VIEW_MEDIATION_DOCUMENTS} class=\"govuk-link\">View mediation documents</a>")
                .categoryCy("Cyfryngu")
                .categoryEn("Mediation")
                .role(role)
                .currentStatusEn(TaskStatus.AVAILABLE.getName())
                .currentStatusCy(TaskStatus.AVAILABLE.getWelshName())
                .nextStatusEn(TaskStatus.AVAILABLE.getName())
                .nextStatusCy(TaskStatus.AVAILABLE.getWelshName())
                .updatedAt(OffsetDateTime.now())
                .createdAt(OffsetDateTime.now())
                .id(UUID.fromString("8c2712da-47ce-4050-bbee-650134a7b9e7"))
                .taskOrder(7)
                .build());
    }
}
