package uk.gov.hmcts.reform.civil.scheduler.gaordermade;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAApproveConsentOrder;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.scheduler.common.DefaultBackPressureConfiguration;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTask;
import uk.gov.hmcts.reform.civil.scheduler.common.ScheduledTaskBackPressureConfiguration;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CaseEvent.END_SCHEDULER_CHECK_STAY_ORDER_DEADLINE;

@Component
@RequiredArgsConstructor
@Slf4j
public class GAOrderMadeScheduledTask implements ScheduledTask<CaseDetails, Long> {

    private final GaCoreCaseDataService coreCaseDataService;
    private final ObjectMapper mapper;
    private final DefaultBackPressureConfiguration defaultBackPressureConfiguration;
    private final CaseDetailsConverter caseDetailsConverter;

    @Override
    public Long getItemId(CaseDetails caseDetails) {
        return caseDetails.getId();
    }

    @Override
    public void accept(CaseDetails caseDetails) {
        Long caseId = caseDetails.getId();
        log.info("GAOrderMadeScheduledTask::accept case {}", caseId);

        GeneralApplicationCaseData caseData = caseDetailsConverter.toGeneralApplicationCaseData(caseDetails);

        coreCaseDataService.triggerGaEvent(
            caseId,
            END_SCHEDULER_CHECK_STAY_ORDER_DEADLINE,
            getUpdatedCaseDataMapper(updateCaseData(caseData))
        );
    }

    private GeneralApplicationCaseData updateCaseData(GeneralApplicationCaseData caseData) {
        if (caseData.getApproveConsentOrder() != null) {
            GAApproveConsentOrder consentOrder = caseData.getApproveConsentOrder();
            GAApproveConsentOrder updatedConsentOrder = new GAApproveConsentOrder()
                .setConsentOrderDescription(consentOrder.getConsentOrderDescription())
                .setConsentOrderDateToEnd(consentOrder.getConsentOrderDateToEnd())
                .setShowConsentOrderDate(consentOrder.getShowConsentOrderDate())
                .setIsOrderProcessedByStayScheduler(YesOrNo.YES);
            return caseData.copy()
                .approveConsentOrder(updatedConsentOrder)
                .build();
        }

        GAJudicialMakeAnOrder judicialDecisionMakeOrder = caseData.getJudicialDecisionMakeOrder();
        return caseData.copy()
            .judicialDecisionMakeOrder(
                judicialDecisionMakeOrder.copy().setIsOrderProcessedByStayScheduler(YesOrNo.YES))
            .build();
    }

    private Map<String, Object> getUpdatedCaseDataMapper(GeneralApplicationCaseData caseData) {
        return caseData.toMap(mapper);
    }

    @Override
    public ScheduledTaskBackPressureConfiguration backPressureConfiguration() {
        return defaultBackPressureConfiguration.getDefaultBackPressure();
    }
}
