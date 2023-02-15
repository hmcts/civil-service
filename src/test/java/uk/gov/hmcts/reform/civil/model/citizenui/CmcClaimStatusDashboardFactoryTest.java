package uk.gov.hmcts.reform.civil.model.citizenui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.cmc.model.CmcClaim;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
public class CmcClaimStatusDashboardFactoryTest {
    @InjectMocks
    private DashboardClaimStatusFactory ccdClaimStatusDashboardFactory;

    @Test
    void given_hasResponsePending_whenGetStatus_thenReturnNoResponse(){
        CmcClaim claim = CmcClaim.builder()
            .responseDeadline(LocalDate.now().plusDays(10)).build();
        DashboardClaimStatus status = ccdClaimStatusDashboardFactory.getDashboardClaimStatus(claim);
        assertThat(status).isEqualTo(DashboardClaimStatus.NO_RESPONSE);
    }

    @Test
    void given_isEligibleForCCJ_whenGetStatus_thenReturnEligibleForCCJStatus(){

    }

}
