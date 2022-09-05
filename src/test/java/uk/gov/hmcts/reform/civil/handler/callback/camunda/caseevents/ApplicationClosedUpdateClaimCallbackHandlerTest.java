package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationDetailsBuilder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Long.parseLong;
import static org.springframework.util.CollectionUtils.isEmpty;

@ExtendWith(SpringExtension.class)
class ApplicationClosedUpdateClaimCallbackHandlerTest {
    private static final List<String> NON_LIVE_STATES = List.of(
            "Application Closed",
            "Proceeds In Heritage",
            "Order Made",
            "Listed for a Hearing",
            "Application Dismissed"
    );
    private static final String APPLICATION_CLOSED = "APPLICATION_CLOSED";
    private static final String APPLICATION_CLOSED_DESCRIPTION = "Application Closed";

    @Test
    public void testTest() {
        CaseData caseData = GeneralApplicationDetailsBuilder.builder()
                .getTestCaseDataWithDetails(CaseData.builder().build());
        List<Element<GeneralApplicationsDetails>> gaDetails = caseData.getGeneralApplicationsDetails();
        Map<Long, GeneralApplication> generalApplicationMap = getLatestStatusOfGeneralApplication(caseData);

        if (!isEmpty(gaDetails)) {
            gaDetails.forEach(gaDetails1 -> {
                if (applicationFilterCriteria(gaDetails1.getValue(), generalApplicationMap)) {
                    gaDetails1.getValue().setCaseState(APPLICATION_CLOSED_DESCRIPTION);
                }
            });
        }

        caseData.getGeneralApplicationsDetails().forEach(generalApplicationsDetailsElement -> {
            System.out.println(generalApplicationsDetailsElement.getValue().getCaseState());
        });
    }

    private boolean applicationFilterCriteria(GeneralApplicationsDetails gaDetails,
                                              Map<Long, GeneralApplication> generalApplicationMap) {
        if (gaDetails != null
                && gaDetails.getCaseLink() != null
                && !NON_LIVE_STATES.contains(gaDetails.getCaseState())) {
            long caseId = parseLong(gaDetails.getCaseLink().getCaseReference());
            return isGeneralApplicationCaseStatusUpdated(caseId, generalApplicationMap);
        }
        return false;
    }

    private boolean isGeneralApplicationCaseStatusUpdated(long caseId,
                                                          Map<Long, GeneralApplication> generalApplicationMap) {
        return generalApplicationMap.containsKey(caseId)
                && APPLICATION_CLOSED.equals(generalApplicationMap.get(caseId).getGeneralApplicationState())
                && (generalApplicationMap.get(caseId).getApplicationClosedDate() != null);
    }

    private Map<Long, GeneralApplication> getLatestStatusOfGeneralApplication(CaseData caseData) {
        Map<Long, GeneralApplication> latestStatus = new HashMap<>();
        latestStatus.put(1234L, getGenApp("APPLICATION_CLOSED"));
        latestStatus.put(2345L, getGenApp("ORDER_MADE"));
        latestStatus.put(3456L, getGenApp("APPLICATION_CLOSED"));
        latestStatus.put(4567L, getGenApp("APPLICATION_CLOSED"));
        latestStatus.put(5678L, getGenApp("APPLICATION_CLOSED"));
        latestStatus.put(6789L, getGenApp("APPLICATION_CLOSED"));
        latestStatus.put(7890L, getGenApp("APPLICATION_DISMISSED"));
        latestStatus.put(8910L, getGenApp("PROCEEDS_IN_HERITAGE"));
        latestStatus.put(1011L, getGenApp("LISTING_FOR_A_HEARING"));
        return latestStatus;
    }

    private GeneralApplication getGenApp(String generalApplicationState) {
        return GeneralApplication.builder()
                .generalApplicationState(generalApplicationState)
                .applicationClosedDate(LocalDateTime.now().minusMinutes(5))
                .build();
    }
}