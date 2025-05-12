package uk.gov.hmcts.reform.civil.notification.handlers.notifyclaimandclaimdetails.caseproceedsoffline;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

@Component
public class CaseProceedOfflineAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public CaseProceedOfflineAllPartiesEmailGenerator(CaseProceedOfflineAppSolEmailDTOGenerator caseProceedOfflineAppSolEmailDTOGenerator) {
        super(caseProceedOfflineAppSolEmailDTOGenerator,
              null,
              null,
              null,
              null);
    }
}
