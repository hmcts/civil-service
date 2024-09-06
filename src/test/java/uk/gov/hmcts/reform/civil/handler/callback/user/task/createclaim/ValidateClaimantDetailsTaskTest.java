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
import uk.gov.hmcts.reform.civil.handler.callback.user.task.createclaim.ValidateClaimantDetailsTask;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.Party.Type;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.PartyValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ValidateClaimantDetailsTaskTest {

    @InjectMocks
    private ValidateClaimantDetailsTask validateClaimantDetailsTask;

    @Mock
    private PostcodeValidator postcodeValidator;

    @Mock
    private DateOfBirthValidator dateOfBirthValidator;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private PartyValidator partyValidator;

    String event = "Event";

    @BeforeEach
    public void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        validateClaimantDetailsTask = new ValidateClaimantDetailsTask(dateOfBirthValidator, partyValidator, postcodeValidator, featureToggleService, objectMapper);
    }

    @Test
    void shouldReturnNoErrors_whenClaimant1AddressValid() {
        Party applicant1 = PartyBuilder.builder().company().build();
        applicant1.setPrimaryAddress(Address.builder().addressLine1("Address line 1").build());

        validateClaimantDetailsTask.setGetApplicant(CaseData::getApplicant1);

        given(postcodeValidator.validate(any())).willReturn(List.of());
        given(dateOfBirthValidator.validate(any())).willReturn(List.of());

        CaseData caseData = CaseData.builder().applicant1(applicant1).build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateClaimantDetailsTask
            .validateClaimantDetails(caseData, event);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotNull();
        assertEquals(0, response.getErrors().size());
    }

    @Test
    void shouldReturnErrors_whenClaimant1AddressNotValid() {
        Party applicant1 = Party.builder().type(Type.ORGANISATION)
            .primaryAddress(Address.builder()
                                .addressLine1("Line 1 test again for more than 35 characters")
                                .addressLine2("Line 2 test again for more than 35 characters")
                                .addressLine3("Line 3 test again for more than 35 characters")
                                .county("County line test again for more than 35 characters")
                                .postCode("PostCode test more than 8 characters")
                                .postTown("Post town line test again for more than 35 characters").build())
            .build();

        validateClaimantDetailsTask.setGetApplicant(CaseData::getApplicant1);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft()
            .applicant1(applicant1).build();

        List<String> errorList = new ArrayList<>();
        given(postcodeValidator.validate(any())).willReturn(errorList);
        errorList.add("This is an error");

        var response = (AboutToStartOrSubmitCallbackResponse) validateClaimantDetailsTask.validateClaimantDetails(caseData,
                                                                                                                  event
        );

        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors()).hasSize(1);
    }

}
