package uk.gov.hmcts.reform.civil.notification.handlers.generatehearingnotice;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.utils.HearingFeeUtils.calculateAndApplyFee;
import static uk.gov.hmcts.reform.civil.utils.HearingFeeUtils.calculateHearingDueDate;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.hearingFeeRequired;

@Component
public class GenerateHearingNoticeHMCAppSolEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE_HEARING = "notification-of-hearing-%s";
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String TIME_FORMAT = "hh:mma";

    private final NotificationsProperties notificationsProperties;
    private final HearingNoticeCamundaService camundaService;
    private final HearingFeesService hearingFeesService;

    public GenerateHearingNoticeHMCAppSolEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                           OrganisationService organisationService, HearingNoticeCamundaService camundaService1,
                                                           HearingFeesService hearingFeesService) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
        this.camundaService = camundaService1;
        this.hearingFeesService = hearingFeesService;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        HearingNoticeVariables camundaVars = camundaService.getProcessVariables(caseData.getBusinessProcess().getProcessInstanceId());
        boolean requiresHearingFee = hearingFeeRequired(camundaVars.getHearingType());

        if (!requiresHearingFee || (caseData.getHearingFeePaymentDetails() != null
                && SUCCESS.equals(caseData.getHearingFeePaymentDetails().getStatus()))) {
            return notificationsProperties.getHearingListedNoFeeClaimantLrTemplateHMC();
        } else {
            return notificationsProperties.getHearingListedFeeClaimantLrTemplateHMC();
        }
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE_HEARING;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        Fee fee = calculateAndApplyFee(hearingFeesService, caseData, caseData.getAssignedTrack());
        LocalDateTime hearingStartDateTime = camundaService
                .getProcessVariables(caseData.getBusinessProcess().getProcessInstanceId()).getHearingStartDateTime();

        LocalDate hearingDate = hearingStartDateTime.toLocalDate();
        LocalTime hearingTime = hearingStartDateTime.toLocalTime();

        properties.put(HEARING_FEE, fee == null ? "£0.00" : String.valueOf(fee.formData()));
        properties.put(HEARING_DATE, hearingDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        properties.put(HEARING_TIME, hearingTime.format(DateTimeFormatter.ofPattern(TIME_FORMAT))
                .replace("AM", "am").replace("PM", "pm"));
        properties.put(HEARING_DUE_DATE, calculateHearingDueDate(LocalDate.now(), hearingDate)
                .format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        return properties;
    }
}
