package uk.gov.hmcts.reform.hearings.hearingnotice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnNotifiedPartiesResponse {

    private List<String> hearingIds;

    private Long totalFound;
}
