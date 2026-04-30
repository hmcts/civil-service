package uk.gov.hmcts.reform.civil.notification.handlers.settleclaimpaidinfullnotification;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class SettleClaimPaidInFullNotificationAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public SettleClaimPaidInFullNotificationAllPartiesEmailGenerator(
        SettleClaimPaidInFullNotificationRespSolOneEmailDTOGenerator respSolOneEmailDTOGenerator,
        SettleClaimPaidInFullNotificationRespSolTwoEmailDTOGenerator respSolTwoEmailDTOGenerator
    ) {
        super(List.of(respSolOneEmailDTOGenerator, respSolTwoEmailDTOGenerator));
    }
}
