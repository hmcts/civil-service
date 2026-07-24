package uk.gov.hmcts.reform.dashboard.data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DraftClaimRequest {

    @Size(max = 200)
    private String caseId;

    @NotNull
    private Map<String, Object> payload;
}
