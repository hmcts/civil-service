package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DashboardResponse {

    private List<DashboardClaimInfo> claims;
    private double totalPages;
}
