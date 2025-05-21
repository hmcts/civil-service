package uk.gov.hmcts.reform.civil.notification.handlers.createclaimspecafterpayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class ClaimContinuingOnlineRespondentSpecEmailGenerator extends AllPartiesEmailGenerator {

    public ClaimContinuingOnlineRespondentSpecEmailGenerator(
            ClaimContinuingOnlineRespondentSpecRespSolOneEmailDTOGenerator respOneGen,
            ClaimContinuingOnlineRespondentSpecRespSolTwoEmailDTOGenerator respTwoGen
    ) {
        super(List.of(
                respOneGen,
                respTwoGen
        ));
    }
}
