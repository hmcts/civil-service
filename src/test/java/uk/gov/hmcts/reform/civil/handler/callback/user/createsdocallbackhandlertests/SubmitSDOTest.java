package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallTrack;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.SubmitSDO;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2Trial;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SubmitSDOTest {

    @Mock
    private FeatureToggleService featureToggleService;

    private ObjectMapper objectMapper;

    @InjectMocks
    private SubmitSDO submitSDO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        submitSDO = new SubmitSDO(featureToggleService, objectMapper);
    }

    @Test
    void whenSdoR2EnabledAndSmallClaimsHearing_thenResponseContainsSdoR2SmallClaimsHearing() {
        CaseLocationCivil caseLocation = CaseLocationCivil.builder().baseLocation("111000").region("2").build();
        SdoR2SmallClaimsHearing smallClaimsHearing = SdoR2SmallClaimsHearing.builder().build();
        CaseData caseData = CaseData.builder()
            .caseManagementLocation(caseLocation)
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .sdoR2SmallClaimsHearing(smallClaimsHearing)
            .drawDirectionsOrderRequired(YesOrNo.NO)
            .smallClaims(List.of(SmallTrack.smallClaimDisputeResolutionHearing))
            .build();
        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .build();

        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) submitSDO.execute(callbackParams);

        CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
        assertThat(responseCaseData.getSdoR2SmallClaimsHearing()).isNotNull();
    }

    @Test
    void whenSdoR2EnabledAndTrialHearingSelected_thenResponseIsNotNull() {
        DynamicList localOptions = DynamicList.builder()
            .listItems(List.of(
                           DynamicListElement.builder().code("00001").label("court 1 - 1 address - Y01 7RB").build(),
                           DynamicListElement.builder().code("00002").label("court 2 - 2 address - Y02 7RB").build(),
                           DynamicListElement.builder().code("00003").label("court 3 - 3 address - Y03 7RB").build()
                       )
            )
            .build();

        DynamicListElement selectedCourt = DynamicListElement.builder()
            .code("00002").label("court 2 - 2 address - Y02 7RB").build();

        CaseLocationCivil caseLocation = CaseLocationCivil.builder().baseLocation("111000").region("2").build();
        CaseData caseData = CaseData.builder()
            .caseManagementLocation(caseLocation)
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .sdoR2Trial(SdoR2Trial.builder()
                            .hearingCourtLocationList(localOptions.toBuilder().value(selectedCourt).build())
                            .altHearingCourtLocationList(localOptions.toBuilder().value(selectedCourt).build())
                            .build())
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .build();

        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) submitSDO.execute(callbackParams);

        assertThat(response).isNotNull();
    }

    @Test
    void whenSdoR2SmallClaimsHearingSelected_thenResponseIsNotNull() {
        DynamicList localOptions = DynamicList.builder()
            .listItems(List.of(
                           DynamicListElement.builder().code("00001").label("court 1 - 1 address - Y01 7RB").build(),
                           DynamicListElement.builder().code("00002").label("court 2 - 2 address - Y02 7RB").build(),
                           DynamicListElement.builder().code("00003").label("court 3 - 3 address - Y03 7RB").build()
                       )
            )
            .build();

        DynamicListElement selectedCourt = DynamicListElement.builder()
            .code("00002").label("court 2 - 2 address - Y02 7RB").build();
        CaseLocationCivil caseLocation = CaseLocationCivil.builder().baseLocation("111000").region("2").build();
        CaseData caseData = CaseData.builder()
            .caseManagementLocation(caseLocation)
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .sdoR2SmallClaimsHearing(SdoR2SmallClaimsHearing.builder().hearingCourtLocationList(localOptions.toBuilder().value(
                selectedCourt).build()).build())
            .build();

        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) submitSDO.execute(callbackParams);

        assertThat(response).isNotNull();
    }

    @Test
    void whenSdoR2EnabledAndNationalRollout_thenResponseIsNotNull() {
        CaseLocationCivil caseLocation = CaseLocationCivil.builder().baseLocation("111000").region("2").build();
        CaseData caseData = CaseData.builder()
            .caseManagementLocation(caseLocation)
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .build();
        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .build();

        when(featureToggleService.isSdoR2Enabled()).thenReturn(true);
        when(featureToggleService.isPartOfNationalRollout(caseData.getCaseManagementLocation().getBaseLocation())).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) submitSDO.execute(callbackParams);

        CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
        assertThat(responseCaseData).isNotNull();
    }

    @Test
    void whenSdoOrderDocumentIsNull_thenResponseSdoOrderDocumentIsNull() {
        CaseLocationCivil caseLocation = CaseLocationCivil.builder().baseLocation("111000").region("2").build();
        CaseData caseData = CaseData.builder()
            .caseManagementLocation(caseLocation)
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .sdoOrderDocument(null)
            .build();
        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) submitSDO.execute(callbackParams);

        CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
        assertThat(responseCaseData.getSdoOrderDocument()).isNull();
    }

    @Test
    void whenSdoOrderDocumentIsNotNull_thenSdoOrderDocumentIsSetToNullAndSystemGeneratedDocumentsArePopulated() {
        CaseLocationCivil caseLocation = CaseLocationCivil.builder().baseLocation("111000").region("2").build();
        CaseDocument document = CaseDocument.builder().build();
        CaseData caseData = CaseData.builder()
            .caseManagementLocation(caseLocation)
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .sdoOrderDocument(document)
            .build();
        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) submitSDO.execute(callbackParams);

        CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
        assertThat(responseCaseData.getSdoOrderDocument()).isNull();
        assertThat(responseCaseData.getSystemGeneratedCaseDocuments()).isNotEmpty();
    }

    @Test
    void whenHmcEnabledAndNoLiP_thenSetHmcEaCourtLocationToNo() {
        CaseLocationCivil caseLocation = CaseLocationCivil.builder().baseLocation("111000").region("2").build();
        CaseData caseData = CaseData.builder()
            .caseManagementLocation(caseLocation)
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .applicant1Represented(YesOrNo.YES)
            .respondent1Represented(YesOrNo.YES)
            .respondent2Represented(YesOrNo.YES)
            .build();
        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .build();

        lenient().when(featureToggleService.isHmcEnabled()).thenReturn(true);
        lenient().when(featureToggleService.isPartOfNationalRollout(caseData.getCaseManagementLocation().getBaseLocation())).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) submitSDO.execute(callbackParams);

        CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
        assertThat(responseCaseData.getHmcEaCourtLocation()).isEqualTo(YesOrNo.NO);
    }

    @Test
    void whenCaseProgressionDisabled_thenEaCourtLocationIsNo() {
        CaseLocationCivil caseLocation = CaseLocationCivil.builder().baseLocation("111000").region("2").build();
        CaseData caseData = CaseData.builder()
            .caseManagementLocation(caseLocation)
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .respondent1Represented(YesOrNo.YES)
            .respondent2Represented(YesOrNo.YES)
            .applicant1Represented(YesOrNo.NO)
            .build();
        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .build();

        when(featureToggleService.isCaseProgressionEnabled()).thenReturn(false);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) submitSDO.execute(callbackParams);

        CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
        assertThat(responseCaseData.getEaCourtLocation()).isEqualTo(YesOrNo.NO);
    }

    @Test
    void whenDisposalHearingMethodInPersonIsSet_thenItRemainsAfterSubmitSDOExecution() {
        CaseLocationCivil caseLocation = CaseLocationCivil.builder().baseLocation("111000").region("2").build();
        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("code").label("label").build()).build();
        CaseData caseData = CaseData.builder()
            .caseManagementLocation(caseLocation)
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .disposalHearingMethodInPerson(dynamicList)
            .build();
        CallbackParams callbackParams = CallbackParams.builder()
            .caseData(caseData)
            .build();

        when(featureToggleService.isPartOfNationalRollout("111000")).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) submitSDO.execute(callbackParams);

        CaseData responseCaseData = objectMapper.convertValue(response.getData(), CaseData.class);
        assertThat(responseCaseData.getDisposalHearingMethodInPerson()).isNotNull();
    }
}
