package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.dashboardnotifications.DashboardNotificationsParamsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class DashboardNotificationsParamsMapper {

    public static final String ORDER_DOCUMENT = "orderDocument";

    private final List<DashboardNotificationsParamsBuilder> dashboardNotificationsParamsBuilders;
    private final FeatureToggleService featureToggleService;

    public HashMap<String, Object> mapCaseDataToParams(CaseData caseData) {

        HashMap<String, Object> params = new HashMap<>();
        dashboardNotificationsParamsBuilders.forEach(builder -> builder.addParams(caseData, params));
        return params;
    }

    public Map<String, Object> mapCaseDataToParams(CaseData caseData, CaseEvent caseEvent) {

        Map<String, Object> params = mapCaseDataToParams(caseData);
        String orderDocumentUrl = addToMapDocumentInfo(caseData, caseEvent);
        if (nonNull(orderDocumentUrl)) {
            params.put(ORDER_DOCUMENT, orderDocumentUrl);
        }
        return params;
    }

    private String addToMapDocumentInfo(CaseData caseData, CaseEvent caseEvent) {

        if (nonNull(caseEvent)) {
            switch (caseEvent) {
                case CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT, CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_CLAIMANT -> {
                    if (featureToggleService.isWelshEnabledForMainCase()
                        && (caseData.isClaimantBilingual() || caseData.isRespondentResponseBilingual())) {
                        return null;
                    } else {
                        return caseData.getFinalOrderDocumentCollection()
                            .get(0).getValue().getDocumentLink().getDocumentBinaryUrl();
                    }
                }
                case UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_CLAIMANT, UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_DEFENDANT -> {
                    return caseData.getFinalOrderDocumentCollection()
                        .get(0).getValue().getDocumentLink().getDocumentBinaryUrl();
                }
                case CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_DEFENDANT, CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_CLAIMANT -> {
                    return caseData.getOrderSDODocumentDJCollection()
                        .get(0).getValue().getDocumentLink().getDocumentBinaryUrl();
                }
                case CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT, CREATE_DASHBOARD_NOTIFICATION_SDO_CLAIMANT -> {
                    Optional<Element<CaseDocument>> sdoDocument = caseData.getSDODocument();
                    if (sdoDocument.isPresent()) {
                        return sdoDocument.get().getValue().getDocumentLink().getDocumentBinaryUrl();
                    }
                }
                default -> {
                    return null;
                }
            }
        }
        return null;
    }

}
