package uk.gov.hmcts.reform.dashboard.data;

public enum TaskStatus {

    NOT_AVAILABLE_YET("Not available yet", 1, "Not available yet"),
    INACTIVE("Inactive", 2, "Inactive"),
    AVAILABLE("Available", 3, "Available"),

    OPTIONAL("Optional", 4, "Optional"),

    ACTION_NEEDED("Action needed", 5, "Action needed"),

    IN_PROGRESS("In progress", 6, "In progress"),

    DONE("Done", 7, "Done");

    private String name;
    private String welshName;
    private int placeValue;

    TaskStatus(String name, int placeValue, String welshName) {
        this.name = name;
        this.placeValue = placeValue;
        this.welshName = welshName;
    }

    public String getName() {
        return this.name;
    }

    public int getPlaceValue() {
        return this.placeValue;
    }

    public String getWelshName() {
        return this.welshName;
    }

    public static TaskStatus getTaskStatusByPlaceValue(int status) {
        for (TaskStatus taskStatus : values()) {
            if (taskStatus.getPlaceValue() == status) {
                return taskStatus;
            }
        }
        throw new IllegalArgumentException("Invalid status place value " + status);
    }

    public static TaskStatus getTaskStatusByName(String name) {
        for (TaskStatus taskStatus : values()) {
            if (taskStatus.getName().equals(name)) {
                return taskStatus;
            }
        }
        throw new IllegalArgumentException("Invalid task status name " + name);
    }

    public static TaskStatus getTaskStatusByWelshName(String welshName) {
        for (TaskStatus taskStatus : values()) {
            if (taskStatus.getWelshName().equals(welshName)) {
                return taskStatus;
            }
        }
        throw new IllegalArgumentException("Invalid task status welsh name " + welshName);
    }

}
