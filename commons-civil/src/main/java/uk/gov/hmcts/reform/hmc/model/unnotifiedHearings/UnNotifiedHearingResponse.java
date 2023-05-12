package uk.gov.hmcts.reform.hmc.model.unnotifiedHearings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnNotifiedHearingResponse {

    private List<String> hearingIds;

    private Long totalFound;
}
