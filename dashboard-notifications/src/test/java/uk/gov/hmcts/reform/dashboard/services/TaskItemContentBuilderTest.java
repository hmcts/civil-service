package uk.gov.hmcts.reform.dashboard.services;

import org.apache.commons.text.StringSubstitutor;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TaskItemContentBuilderTest {

    public static final String TASK_ITEM =
        "<li class=\"app-task-list__item\">\n" +
        "              <div class=\"row\">\n" +
        "                <div class=\"column\">\n" +
        "                  <span class=\"app-task-list__task-name\">\n" +
        "                    {{  ${url} | safe }}\n" +
        "                  </span>\n" +
        "                </div>\n" +
        "                <div class=\"column\">\n" +
        "                  <strong class=\"govuk-tag app-task-list__tag {{ task.colour }}\">\n" +
        "                    {{ ${status} }}\n" +
        "                  </strong>\n" +
        "                </div>\n" +
        "                  {{  ${helpText} | safe }}\n" +
        "              </div>\n" +
        "            </li>";

    private TaskItemContentBuilder taskItemContentBuilder = new TaskItemContentBuilder();

    @Test
    void shouldBuildTaskItemContent() {

        StringSubstitutor stringSubstitutor = new StringSubstitutor(Map.of(
            "url",
            "http://testUrl",
            "status",
            "InProgress",
            "helpText",
            "Should be helpful!"

        ));

        String output = taskItemContentBuilder
            .buildTaskItemContent(stringSubstitutor, "Hearing", TASK_ITEM, "Hearing pay");
        System.out.println(output);
    }

}
