package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class DashboardNotificationsParamsMapper {

    public static final String ORDER_DOCUMENT = "orderDocument";
    public static final String HIDDEN_ORDER_DOCUMENT = "hiddenOrderDocument";

    private final List<DashboardNotificationsParamsBuilder> dashboardNotificationsParamsBuilders;
    private final FeatureToggleService featureToggleService;

    public HashMap<String, Object> mapCaseDataToParams(CaseData caseData) {
        return buildBaseParams(caseData);
    }

    public Map<String, Object> mapCaseDataToParams(CaseData caseData, CaseEvent caseEvent) {
        Map<String, Object> params = buildBaseParams(caseData);
        if (nonNull(caseData) && nonNull(caseEvent)) {
            addDocumentInfoToParams(params, caseData, caseEvent);
        }
        return params;
    }

    private HashMap<String, Object> buildBaseParams(CaseData caseData) {
        HashMap<String, Object> params = new HashMap<>();
        if (nonNull(caseData)) {
            dashboardNotificationsParamsBuilders.forEach(builder -> builder.addParams(caseData, params));
        }
        return params;
    }

    private void addDocumentInfoToParams(Map<String, Object> params, CaseData caseData, CaseEvent caseEvent) {
        Optional.ofNullable(addToMapDocumentInfo(caseData, caseEvent))
            .ifPresent(url -> params.put(ORDER_DOCUMENT, url));

        Optional.ofNullable(addToHiddenDocumentInfo(caseData, caseEvent))
            .ifPresent(url -> params.put(HIDDEN_ORDER_DOCUMENT, url));
    }

    private String addToMapDocumentInfo(CaseData caseData, CaseEvent caseEvent) {
        return Optional.ofNullable(caseEvent).map(event -> switch (event) {
            case CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_DEFENDANT, CREATE_DASHBOARD_NOTIFICATION_FINAL_ORDER_CLAIMANT -> {
                if (featureToggleService.isWelshEnabledForMainCase()
                    && (caseData.isClaimantBilingual() || caseData.isRespondentResponseBilingual())) {
                    yield null;
                } else {
                    yield getFirstFinalOrderUrlIfPresent(caseData);
                }
            }
            case UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_CLAIMANT, UPDATE_TASK_LIST_CONFIRM_ORDER_REVIEW_DEFENDANT -> getFirstFinalOrderUrlIfPresent(caseData);
            case CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_DEFENDANT, CREATE_DASHBOARD_NOTIFICATION_DJ_SDO_CLAIMANT ->
                Optional.ofNullable(caseData.getOrderSDODocumentDJCollection())
                    .filter(collection -> !collection.isEmpty())
                    .map(collection -> collection.get(0).getValue().getDocumentLink().getDocumentBinaryUrl())
                    .orElse(null);
            case CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT, CREATE_DASHBOARD_NOTIFICATION_SDO_CLAIMANT -> caseData.getSDODocument()
                .map(sdoDocument -> sdoDocument.getValue().getDocumentLink().getDocumentBinaryUrl())
                .orElse(null);
            default -> null;
        }).orElse(null);
    }

    private String getFirstFinalOrderUrlIfPresent(CaseData caseData) {
        return Optional.ofNullable(caseData.getFinalOrderDocumentCollection())
            .filter(collection -> !collection.isEmpty())
            .map(collection -> collection.get(0).getValue().getDocumentLink().getDocumentBinaryUrl())
            .orElse(null);
    }

    private String addToHiddenDocumentInfo(CaseData caseData, CaseEvent caseEvent) {
        return Optional.ofNullable(caseEvent).map(event -> switch (event) {
            case CREATE_DASHBOARD_NOTIFICATION_SDO_DEFENDANT, CREATE_DASHBOARD_NOTIFICATION_SDO_CLAIMANT -> caseData.getHiddenSDODocument()
                .map(sdoDocument -> sdoDocument.getValue().getDocumentLink().getDocumentBinaryUrl())
                .orElse(null);
            default -> null;
        }).orElse(null);
    }

}
