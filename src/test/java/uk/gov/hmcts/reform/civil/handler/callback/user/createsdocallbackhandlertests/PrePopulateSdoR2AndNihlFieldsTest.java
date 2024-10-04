package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.CreateSDOCallbackHandlerUtils;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.PrePopulateSdoR2AndNihlFields;
import uk.gov.hmcts.reform.civil.helpers.LocationHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class PrePopulateSdoR2AndNihlFieldsTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private LocationHelper locationHelper;

    @Mock
    private CreateSDOCallbackHandlerUtils createSDOCallbackHandlerUtils;

    @InjectMocks
    private PrePopulateSdoR2AndNihlFields prePopulateSdoR2AndNihlFields;

    @Test
    void shouldPopulateDRHFields_whenCalled() {
        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(CaseData.builder().build())
            .build();

        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        DynamicList hearingMethodList = DynamicList.builder()
            .listItems(List.of(
                DynamicListElement.builder().code("TELEPHONE").label("Telephone").build(),
                DynamicListElement.builder().code("IN_PERSON").label("In Person").build()
            ))
            .build();

        List<LocationRefData> locationRefDataList = List.of(
            LocationRefData.builder()
                .courtLocationCode("LOC001")
                .courtName("Test Court")
                .build()
        );

        RequestedCourt requestedCourt = RequestedCourt.builder()
            .responseCourtCode("123")
            .build();

        prePopulateSdoR2AndNihlFields.populateDRHFields(callbackParams, caseDataBuilder, Optional.of(requestedCourt), hearingMethodList, locationRefDataList);

        assertNotNull(caseDataBuilder.build().getSdoR2SmallClaimsHearing());
    }

    @Test
    void shouldPrePopulateNihlFields_whenCalled() {
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseData.builder();

        DynamicList hearingMethodList = DynamicList.builder()
            .listItems(List.of(
                DynamicListElement.builder().code("TELEPHONE").label("Telephone").build(),
                DynamicListElement.builder().code("IN_PERSON").label("In Person").build()
            ))
            .build();

        List<LocationRefData> locationRefDataList = List.of(
            LocationRefData.builder()
                .courtLocationCode("LOC001")
                .courtName("Test Court")
                .build()
        );

        RequestedCourt requestedCourt = RequestedCourt.builder()
            .responseCourtCode("123")
            .build();

        prePopulateSdoR2AndNihlFields.prePopulateNihlFields(caseDataBuilder, hearingMethodList, Optional.of(requestedCourt), locationRefDataList);

        assertNotNull(caseDataBuilder.build().getSdoR2Trial());
    }
}
