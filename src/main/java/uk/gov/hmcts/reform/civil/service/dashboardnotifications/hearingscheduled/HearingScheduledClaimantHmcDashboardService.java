package uk.gov.hmcts.reform.civil.service.dashboardnotifications.hearingscheduled;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsMapper;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.HearingFeeUtils;
import uk.gov.hmcts.reform.dashboard.services.DashboardScenariosService;

import static uk.gov.hmcts.reform.civil.utils.HearingUtils.hearingFeeRequired;

@Service("hearingScheduledClaimantHmcDashboardService")
public class HearingScheduledClaimantHmcDashboardService extends HearingScheduledClaimantDashboardService {

    private final HearingNoticeCamundaService camundaService;
    private final HearingFeesService hearingFeesService;

    public HearingScheduledClaimantHmcDashboardService(DashboardScenariosService dashboardScenariosService,
                                                       DashboardNotificationsParamsMapper mapper,
                                                       LocationReferenceDataService locationRefDataService,
                                                       HearingNoticeCamundaService camundaService,
                                                       HearingFeesService hearingFeesService,
                                                       CourtLocationUtils courtLocationUtils) {
        super(dashboardScenariosService, mapper, locationRefDataService, courtLocationUtils);
        this.camundaService = camundaService;
        this.hearingFeesService = hearingFeesService;
    }

    @Override
    protected boolean isHearingFeeRequired(CaseData caseData) {
        // logic required for HMC-driven notifications
        boolean hasPaidFee = caseData.isHearingFeePaid();
        if (hasPaidFee) {
            return false;
        }

        boolean isHearingFeeRequired = hearingFeeRequired(
            camundaService.getProcessVariables(caseData.getBusinessProcess().getProcessInstanceId()).getHearingType());

        if (isHearingFeeRequired) {
            caseData.setHearingFee(HearingFeeUtils.calculateAndApplyFee(
                hearingFeesService,
                caseData,
                caseData.getAssignedTrack()
            ));
        }
        return isHearingFeeRequired;
    }
}
