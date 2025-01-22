package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.generatesdoorder.GenerateSdoOrder;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.prepopulateorderdetailspages.PrePopulateOrderDetailsPages;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SetOrderDetailsFlags;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.submitsdo.SubmitSDO;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CallbackVersion.V_1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.CREATE_SDO;
import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.CONFIRMATION_HEADER_SDO;
import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.CONFIRMATION_SUMMARY_1_V_1;
import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.CONFIRMATION_SUMMARY_1_V_2;
import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.CONFIRMATION_SUMMARY_2_V_1;
import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.FEEDBACK_LINK;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateSDOCallbackHandler extends CallbackHandler {

    private final List<CaseEvent> handlerEvents = Collections.singletonList(CREATE_SDO);
    private final GenerateSdoOrder generateSdoOrder;
    private final PrePopulateOrderDetailsPages prePopulateOrderDetailsPages;
    private final SubmitSDO submitSDO;
    private final SetOrderDetailsFlags setOrderDetailsFlags;

    @Value("${court-location.unspecified-claim.epimms-id}")
    public String ccmccEpimsId;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_START), this::prePopulateOrderDetailsPages)
            .put(callbackKey(V_1, ABOUT_TO_START), this::prePopulateOrderDetailsPages)
            .put(callbackKey(MID, "order-details-navigation"), this::setOrderDetailsFlags)
            .put(callbackKey(MID, "generate-sdo-order"), this::generateSdoOrder)
            .put(callbackKey(V_1, MID, "generate-sdo-order"), this::generateSdoOrder)
            .put(callbackKey(ABOUT_TO_SUBMIT), this::submitSDO)
            .put(callbackKey(SUBMITTED), this::buildConfirmation)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return handlerEvents;
    }

    private CallbackResponse prePopulateOrderDetailsPages(CallbackParams callbackParams) {
        return prePopulateOrderDetailsPages.execute(callbackParams);
    }

    private CallbackResponse setOrderDetailsFlags(CallbackParams callbackParams) {
        return setOrderDetailsFlags.execute(callbackParams);
    }

    private CallbackResponse generateSdoOrder(CallbackParams callbackParams) {
        return generateSdoOrder.execute(callbackParams);
    }

    private CallbackResponse submitSDO(CallbackParams callbackParams) {
        return submitSDO.execute(callbackParams);
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(getHeader(caseData))
            .confirmationBody(getBody(caseData))
            .build();
    }

    private String getHeader(CaseData caseData) {
        return format(
            CONFIRMATION_HEADER_SDO,
            caseData.getLegacyCaseReference()
        );
    }

    private String getBody(CaseData caseData) {
        String applicant1Name = caseData.getApplicant1().getPartyName();
        String respondent1Name = caseData.getRespondent1().getPartyName();
        Party applicant2 = caseData.getApplicant2();
        Party respondent2 = caseData.getRespondent2();

        String initialBody = format(
            CONFIRMATION_SUMMARY_1_V_1,
            applicant1Name,
            respondent1Name
        );

        if (applicant2 != null) {
            initialBody = format(
                CONFIRMATION_SUMMARY_2_V_1,
                applicant1Name,
                applicant2.getPartyName(),
                respondent1Name
            );
        } else if (respondent2 != null) {
            initialBody = format(
                CONFIRMATION_SUMMARY_1_V_2,
                applicant1Name,
                respondent1Name,
                respondent2.getPartyName()
            );
        }
        return initialBody + format(FEEDBACK_LINK, "Feedback: Please provide judicial feedback");
    }
}
