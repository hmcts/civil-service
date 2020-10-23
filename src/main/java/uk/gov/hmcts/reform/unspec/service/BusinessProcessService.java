package uk.gov.hmcts.reform.unspec.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.unspec.callback.CaseEvent;
import uk.gov.hmcts.reform.unspec.model.BusinessProcess;
import uk.gov.hmcts.reform.unspec.model.CaseData;

import static uk.gov.hmcts.reform.unspec.enums.BusinessProcessStatus.READY;

@Service
@RequiredArgsConstructor
public class BusinessProcessService {

    public CaseData updateBusinessProcess(CaseData caseData, CaseEvent caseEvent) {
        if (caseData.hasNoOngoingBusinessProcess()) {
            return caseData.toBuilder()
                .businessProcess(BusinessProcess.builder()
                                     .camundaEvent(caseEvent.name())
                                     .status(READY)
                                     .build())
                .build();
        }
        return caseData;
    }
}
