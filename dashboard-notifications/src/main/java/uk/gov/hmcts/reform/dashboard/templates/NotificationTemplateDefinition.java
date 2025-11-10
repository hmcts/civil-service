package uk.gov.hmcts.reform.dashboard.templates;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
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
        return NotificationTemplateDefinition.builder()
            .name(trim(name))
            .role(trim(role))
            .titleEn(trim(titleEn))
            .titleCy(trim(titleCy))
            .descriptionEn(trim(descriptionEn))
            .descriptionCy(trim(descriptionCy))
            .timeToLive(trim(timeToLive))
            .deadlineParam(trim(deadlineParam))
            .markedForDeletion(markedForDeletion)
            .build();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
