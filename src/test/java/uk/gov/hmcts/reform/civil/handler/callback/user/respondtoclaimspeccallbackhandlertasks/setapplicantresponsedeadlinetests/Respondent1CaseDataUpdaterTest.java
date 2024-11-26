package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinetests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec.Respondent1CaseDataUpdater;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@ExtendWith(MockitoExtension.class)
class Respondent1CaseDataUpdaterTest {

    @InjectMocks
    private Respondent1CaseDataUpdater updater;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder()
                .specAoSApplicantCorrespondenceAddressRequired(NO)
                .specAoSApplicantCorrespondenceAddressdetails(Address.builder().postCode("postcode").build())
                .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).partyName("RESPONDENT_INDIVIDUAL").build())
                .respondent1Copy(Party.builder().partyName("Party 2").primaryAddress(
                                Address
                                        .builder()
                                        .addressLine1("Triple street")
                                        .postCode("Postcode")
                                        .build())
                        .build())
                .build();
    }

    @Test
    void shouldUpdateCaseDataWhenCorrespondenceAddressIsNotRequired() {
        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();
        updater.update(caseData, updatedData);

        Party updatedRespondent1 = updatedData.build().getRespondent1();
        assertThat(updatedRespondent1).isNotNull();
    }

    @Test
    void shouldUpdateCaseDataWhenCorrespondenceAddressIsRequired() {
        caseData = caseData.toBuilder()
                .specAoSApplicantCorrespondenceAddressRequired(null)
                .build();

        CaseData.CaseDataBuilder<?, ?> updatedData = CaseData.builder();
        updater.update(caseData, updatedData);

        Party updatedRespondent1 = updatedData.build().getRespondent1();
        assertThat(updatedRespondent1).isNotNull();
    }
}