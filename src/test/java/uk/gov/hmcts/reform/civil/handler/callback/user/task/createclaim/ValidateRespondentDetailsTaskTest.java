package uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.ValidateRespondentDetailsTask;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.Party.Type;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.validation.PartyValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidateRespondentDetailsTaskTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private ValidateRespondentDetailsTask validateRespondentDetailsTask;

    @Mock
    private PostcodeValidator postcodeValidator;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private PartyValidator partyValidator;

    @BeforeEach
    public void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        validateRespondentDetailsTask = new ValidateRespondentDetailsTask(postcodeValidator, featureToggleService, partyValidator, objectMapper);
        when(featureToggleService.isJudgmentOnlineLive()).thenReturn(true);
    }

    @Test
    void shouldReturnNoErrors_whenRespondent1AddressValid() {
        Party respondent1 = PartyBuilder.builder().company().build();
        respondent1.setPrimaryAddress(Address.builder().addressLine1("Address line 1").build());

        List<String> errorList = List.of();
        given(postcodeValidator.validate(any())).willReturn(errorList);
        given(partyValidator.validateAddress(respondent1.getPrimaryAddress(), errorList)).willReturn(errorList);

        CaseData caseData = CaseData.builder().respondent1(respondent1).build();
        validateRespondentDetailsTask.setGetRespondent(CaseData::getRespondent1);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateRespondentDetailsTask
            .validateRespondentDetails(caseData);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotNull();
        assertEquals(0, response.getErrors().size());
    }

    @Test
    void shouldReturnErrors_whenRespondent1AddressNotValid() {
        Party respondent1 = Party.builder().type(Type.ORGANISATION)
            .primaryAddress(Address.builder()
                                .addressLine1("Line 1 test again for more than 35 characters")
                                .addressLine2("Line 2 test again for more than 35 characters")
                                .addressLine3("Line 3 test again for more than 35 characters")
                                .county("County line test again for more than 35 characters")
                                .postCode("PostCode test more than 8 characters")
                                .postTown("Post town line test again for more than 35 characters").build())
            .build();

        validateRespondentDetailsTask.setGetRespondent(CaseData::getRespondent1);

        List<String> errorList = new ArrayList<>();
        given(partyValidator.validateAddress(respondent1.getPrimaryAddress(), errorList)).willReturn(errorList);
        given(postcodeValidator.validate(any())).willReturn(errorList);
        errorList.add("This is an error");

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
            .respondent1(respondent1).build();

        var response = (AboutToStartOrSubmitCallbackResponse) validateRespondentDetailsTask.validateRespondentDetails(caseData);

        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors()).hasSize(1);
    }
}
