package uk.gov.hmcts.reform.civil.service.robotics.builders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.robotics.EventHistory;
import uk.gov.hmcts.reform.civil.service.robotics.dto.EventHistoryDTO;

import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;

@Slf4j
@Component
@RequiredArgsConstructor
public class InformAgreedExtensionDateSpecBuilder {

    private final ClaimDetailsNotifiedTimeExtensionBuilder notifiedTimeExtensionBuilder;

    public void buildEvent(EventHistoryDTO eventHistoryDTO) {
        log.info("Building event: {} for case id: {} ", eventHistoryDTO.getEventType(), eventHistoryDTO.getCaseData().getCcdCaseReference());
        EventHistory.EventHistoryBuilder builder = eventHistoryDTO.getBuilder();
        CaseData caseData = eventHistoryDTO.getCaseData();
        buildInformAgreedExtensionDateForSpec(builder, caseData);
    }

    private void buildInformAgreedExtensionDateForSpec(EventHistory.EventHistoryBuilder builder, CaseData caseData) {
        if (SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
            && (caseData.getRespondentSolicitor1AgreedDeadlineExtension() != null
            || caseData.getRespondentSolicitor2AgreedDeadlineExtension() != null)) {
            notifiedTimeExtensionBuilder.buildConsentExtensionFilingDefence(builder, caseData);
        }
    }
}
