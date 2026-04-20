package uk.gov.hmcts.reform.dashboard.templates;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationTemplateDefinition {

    private String name;
    private String role;
    private String titleEn;
    private String titleCy;
    private String descriptionEn;
    private String descriptionCy;
    private String timeToLive;
    private String deadlineParam;
    @JsonProperty("delete")
    private boolean markedForDeletion;

    public NotificationTemplateDefinition sanitise() {
        return new NotificationTemplateDefinition(
            trim(name),
            trim(role),
            trim(titleEn),
            trim(titleCy),
            trim(descriptionEn),
            trim(descriptionCy),
            trim(timeToLive),
            trim(deadlineParam),
            markedForDeletion
        );
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
