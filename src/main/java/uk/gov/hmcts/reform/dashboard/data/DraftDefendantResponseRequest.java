package uk.gov.hmcts.reform.dashboard.data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DraftDefendantResponseRequest {

    @NotBlank
    @Size(max = 200)
    private String caseId;

    @NotNull
    private Map<String, Object> payload;
}
