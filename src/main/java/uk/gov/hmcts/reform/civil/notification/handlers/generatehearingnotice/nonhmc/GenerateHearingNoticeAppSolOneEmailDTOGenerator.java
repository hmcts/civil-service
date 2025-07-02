package uk.gov.hmcts.reform.civil.notification.handlers.generatehearingnotice.nonhmc;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingNoticeList;
import uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.utils.NotificationUtils;

import java.util.Map;

import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;

@Component
public class GenerateHearingNoticeAppSolOneEmailDTOGenerator extends AppSolOneEmailDTOGenerator {

    private static final String REFERENCE_TEMPLATE_HEARING = "notification-of-hearing-%s";
    private final NotificationsProperties notificationsProperties;

    public GenerateHearingNoticeAppSolOneEmailDTOGenerator(NotificationsProperties notificationsProperties,
                                                           OrganisationService organisationService) {
        super(organisationService);
        this.notificationsProperties = notificationsProperties;
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        if (shouldSendNoFeeHearingNotice(caseData)) {
            return notificationsProperties.getHearingListedNoFeeClaimantLrTemplate();
        } else {
            return notificationsProperties.getHearingListedFeeClaimantLrTemplate();
        }
    }

    @Override
    protected String getReferenceTemplate() {
        return REFERENCE_TEMPLATE_HEARING;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.put(HEARING_DATE, NotificationUtils.getFormattedHearingTime(caseData.getHearingTimeHourMinute()));
        properties.put(HEARING_TIME, NotificationUtils.getFormattedHearingTime(caseData.getHearingTimeHourMinute()));

        if (caseData.getHearingFeePaymentDetails() == null
                || !SUCCESS.equals(caseData.getHearingFeePaymentDetails().getStatus())) {
            properties.put(HEARING_FEE, caseData.getHearingFee() == null
                    ? "£0.00" : String.valueOf(caseData.getHearingFee().formData()));
            properties.put(HEARING_DUE_DATE, caseData.getHearingDueDate() == null
                    ? "" : NotificationUtils.getFormattedHearingDate(caseData.getHearingDueDate()));
        }

        return properties;
    }

    private static boolean shouldSendNoFeeHearingNotice(CaseData caseData) {
        return (caseData.getHearingFeePaymentDetails() != null
                && SUCCESS.equals(caseData.getHearingFeePaymentDetails().getStatus()))
                || caseData.getHearingNoticeList().equals(HearingNoticeList.OTHER)
                || caseData.getListingOrRelisting().equals(ListingOrRelisting.RELISTING);
    }
}
