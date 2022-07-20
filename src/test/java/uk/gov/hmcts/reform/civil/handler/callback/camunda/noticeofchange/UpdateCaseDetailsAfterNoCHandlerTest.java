package uk.gov.hmcts.reform.civil.handler.callback.camunda.noticeofchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SolicitorOrganisationDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.prd.model.ContactInformation;
import uk.gov.hmcts.reform.prd.model.DxAddress;
import uk.gov.hmcts.reform.prd.model.Organisation;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@SpringBootTest(classes = {
    UpdateCaseDetailsAfterNoCHandler.class,
    JacksonAutoConfiguration.class,
})
public class UpdateCaseDetailsAfterNoCHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private UpdateCaseDetailsAfterNoCHandler handler;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    @MockBean
    private OrganisationService organisationService;

    private static final String NEW_ORG_ID = "1234";

    @Nested
    class AboutToSubmit {

        @Test
        void shouldUpdateSolicitorDetails_afterNoCSubmittedByApplicantSolicitor1v1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .changeOfRepresentation(true, false, "1234", "QWERTY A")
                .updateOrgPolicyAfterNoC(true, false)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            Organisation newOrg = getOrg(NEW_ORG_ID);

            when(organisationService.findOrganisationById(NEW_ORG_ID)).thenReturn(Optional.of(newOrg));

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getApplicantSolicitor1ServiceAddress())
                .isEqualTo(getNewOrgDetails().getAddress());

            assertThat(updatedCaseData.getApplicantSolicitor1PbaAccounts().getListItems().get(0).getLabel())
                .isEqualTo("account");
            assertThat(updatedCaseData.getApplicantSolicitor1PbaAccountsIsEmpty()).isEqualTo(NO);
            assertSolicitorReferences(true, false, caseData, updatedCaseData);
            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();

            //TODO update this after CCD-3538
            assertThat(updatedCaseData.getApplicantSolicitor1UserDetails()).isNull();
        }

        @Test
        void shouldUpdateSolicitorDetails_afterNoCSubmittedByRespondentSolicitor1v1() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .changeOfRepresentation(false, false, "1234", "QWERTY R")
                .updateOrgPolicyAfterNoC(true, false)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            Organisation newOrg = getOrg(NEW_ORG_ID);

            when(organisationService.findOrganisationById(NEW_ORG_ID)).thenReturn(Optional.of(newOrg));

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor1OrganisationDetails()).isEqualTo(getNewOrgDetails());
            assertThat(updatedCaseData.getRespondentSolicitor1ServiceAddress())
                .isEqualTo(getNewOrgDetails().getAddress());
            assertSolicitorReferences(false, false, caseData, updatedCaseData);
            assertThat(updatedCaseData.getRespondent1OrganisationIDCopy()).isEqualTo("1234");
            assertThat(updatedCaseData.getRespondent1Represented()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondent1OrgRegistered()).isEqualTo(YES);
            //TODO update this after CCD-3538
            assertThat(updatedCaseData.getRespondentSolicitor1EmailAddress()).isNull();
        }

        @Test
        void shouldUpdateSolicitorDetails_afterNoCSubmittedByRespondentSolicitor2In1v2DSToDS() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .multiPartyClaimTwoDefendantSolicitors()
                .changeOfRepresentation(false, true, "1234", "QWERTY R2")
                .updateOrgPolicyAfterNoC(false, true)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            Organisation newOrg = getOrg(NEW_ORG_ID);

            when(organisationService.findOrganisationById(NEW_ORG_ID)).thenReturn(Optional.of(newOrg));

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor2OrganisationDetails()).isEqualTo(getNewOrgDetails());
            assertThat(updatedCaseData.getRespondentSolicitor2ServiceAddress())
                .isEqualTo(getNewOrgDetails().getAddress());
            assertSolicitorReferences(false, true, caseData, updatedCaseData);
            assertThat(updatedCaseData.getRespondent2OrganisationIDCopy()).isEqualTo("1234");
            assertThat(updatedCaseData.getRespondent2Represented()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondent2OrgRegistered()).isEqualTo(YES);
            assertThat(getMultiPartyScenario(updatedCaseData)).isEqualTo(ONE_V_TWO_TWO_LEGAL_REP);
            //TODO update this after CCD-3538
            assertThat(updatedCaseData.getRespondentSolicitor2EmailAddress()).isNull();
        }

        @Test
        void shouldUpdateSolicitorDetails_afterNoCSubmittedByRespondentSolicitor2In1v2DSToSS() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .multiPartyClaimTwoDefendantSolicitors()
                .changeOfRepresentation(false, true, "QWERTY R", "QWERTY R2")
                .updateOrgPolicyAfterNoC(false, true)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            Organisation newOrg = getOrg("QWERTY R");

            when(organisationService.findOrganisationById("QWERTY R")).thenReturn(Optional.of(newOrg));

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor2OrganisationDetails()).isEqualTo(getNewOrgDetails());
            assertThat(updatedCaseData.getRespondentSolicitor2ServiceAddress())
                .isEqualTo(getNewOrgDetails().getAddress());
            assertSolicitorReferences(false, true, caseData, updatedCaseData);
            assertThat(updatedCaseData.getRespondent2OrganisationIDCopy()).isEqualTo("QWERTY R");
            assertThat(updatedCaseData.getRespondent2Represented()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondent2OrgRegistered()).isEqualTo(YES);
            //TODO update this after CCD-3538
            assertThat(updatedCaseData.getRespondentSolicitor2EmailAddress()).isNull();
            //TODO uncomment after CIV-3227
            //assertThat(getMultiPartyScenario(updatedCaseData)).isEqualTo(ONE_V_TWO_ONE_LEGAL_REP);
        }

        @Test
        void shouldUpdateSolicitorDetails_afterNoCSubmittedByRespondentSolicitor2In1v2SSToDS() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimIssued()
                .multiPartyClaimOneDefendantSolicitor()
                .changeOfRepresentation(false, true, "1234", "QWERTY R")
                .updateOrgPolicyAfterNoC(false, true)
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            Organisation newOrg = getOrg(NEW_ORG_ID);

            when(organisationService.findOrganisationById(NEW_ORG_ID)).thenReturn(Optional.of(newOrg));

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

            assertThat(updatedCaseData.getChangeOrganisationRequestField()).isNull();
            assertThat(updatedCaseData.getRespondentSolicitor2OrganisationDetails()).isEqualTo(getNewOrgDetails());
            assertThat(updatedCaseData.getRespondentSolicitor2ServiceAddress())
                .isEqualTo(getNewOrgDetails().getAddress());
            assertSolicitorReferences(false, true, caseData, updatedCaseData);
            assertThat(updatedCaseData.getRespondent2OrganisationIDCopy()).isEqualTo("1234");
            assertThat(updatedCaseData.getRespondent2Represented()).isEqualTo(YES);
            assertThat(updatedCaseData.getRespondent2OrgRegistered()).isEqualTo(YES);
            //TODO update this after CCD-3538
            assertThat(updatedCaseData.getRespondentSolicitor2EmailAddress()).isNull();
            //TODO uncomment after CIV-3227
            //assertThat(getMultiPartyScenario(updatedCaseData)).isEqualTo(ONE_V_TWO_TWO_LEGAL_REP);
        }

        private void assertSolicitorReferences(boolean isApplicant, boolean respondent2Exists,
                                               CaseData caseData, CaseData updatedCaseData) {
            if (isApplicant) {
                assertThat(updatedCaseData.getSolicitorReferences().getApplicantSolicitor1Reference()).isNull();
                assertThat(updatedCaseData.getSolicitorReferences().getRespondentSolicitor1Reference())
                    .isEqualTo(caseData.getSolicitorReferences().getRespondentSolicitor1Reference());

                if (caseData.getSolicitorReferencesCopy() != null) {
                    assertThat(updatedCaseData.getSolicitorReferencesCopy().getApplicantSolicitor1Reference()).isNull();
                    assertThat(updatedCaseData.getSolicitorReferencesCopy().getRespondentSolicitor1Reference())
                        .isEqualTo(caseData.getSolicitorReferencesCopy().getRespondentSolicitor1Reference());
                }
                if (respondent2Exists) {
                    assertThat(updatedCaseData.getSolicitorReferences().getRespondentSolicitor2Reference())
                        .isEqualTo(caseData.getSolicitorReferences().getRespondentSolicitor2Reference());
                    assertThat(updatedCaseData.getSolicitorReferencesCopy().getRespondentSolicitor2Reference())
                        .isEqualTo(caseData.getSolicitorReferencesCopy().getRespondentSolicitor2Reference());
                }
            } else {
                if (respondent2Exists) {
                    assertThat(updatedCaseData.getSolicitorReferences().getApplicantSolicitor1Reference())
                        .isEqualTo(caseData.getSolicitorReferences().getApplicantSolicitor1Reference());
                    assertThat(updatedCaseData.getSolicitorReferences().getRespondentSolicitor1Reference())
                        .isEqualTo(caseData.getSolicitorReferences().getRespondentSolicitor1Reference());
                    assertThat(updatedCaseData.getSolicitorReferences().getRespondentSolicitor2Reference()).isNull();

                    if (caseData.getSolicitorReferencesCopy() != null) {
                        assertThat(updatedCaseData.getSolicitorReferencesCopy().getApplicantSolicitor1Reference())
                            .isEqualTo(caseData.getSolicitorReferencesCopy().getApplicantSolicitor1Reference());
                        assertThat(updatedCaseData.getSolicitorReferencesCopy().getRespondentSolicitor1Reference())
                            .isEqualTo(caseData.getSolicitorReferencesCopy().getRespondentSolicitor1Reference());
                        assertThat(updatedCaseData.getSolicitorReferencesCopy().getRespondentSolicitor2Reference())
                            .isNull();
                    }
                } else {
                    assertThat(updatedCaseData.getSolicitorReferences().getApplicantSolicitor1Reference())
                        .isEqualTo(caseData.getSolicitorReferences().getApplicantSolicitor1Reference());
                    assertThat(updatedCaseData.getSolicitorReferences().getRespondentSolicitor1Reference()).isNull();

                    if (caseData.getSolicitorReferencesCopy() != null) {
                        assertThat(updatedCaseData.getSolicitorReferencesCopy().getApplicantSolicitor1Reference())
                            .isEqualTo(caseData.getSolicitorReferencesCopy().getApplicantSolicitor1Reference());
                        assertThat(updatedCaseData.getSolicitorReferencesCopy().getRespondentSolicitor1Reference())
                            .isNull();
                    }
                    if (respondent2Exists) {
                        assertThat(updatedCaseData.getSolicitorReferences().getRespondentSolicitor2Reference())
                            .isEqualTo(caseData.getSolicitorReferences().getRespondentSolicitor2Reference());
                        assertThat(updatedCaseData.getSolicitorReferencesCopy().getRespondentSolicitor2Reference())
                            .isEqualTo(caseData.getSolicitorReferencesCopy().getRespondentSolicitor2Reference());
                    }
                }
            }
        }

        private SolicitorOrganisationDetails getNewOrgDetails() {
            return SolicitorOrganisationDetails.builder()
                .address(Address.fromContactInformation(getNewOrgContactInfo()))
                .phoneNumber("1234")
                .organisationName("new org")
                .dx(getDxAddress().toString())
                .build();
        }

        private Organisation getOrg(String orgId) {
            return Organisation.builder()
                .contactInformation(Collections.singletonList(getNewOrgContactInfo()))
                .companyNumber("1234")
                .name("new org")
                .paymentAccount(Collections.singletonList("account"))
                .companyUrl("url")
                .organisationIdentifier(orgId)
                .build();
        }

        private ContactInformation getNewOrgContactInfo() {
            return ContactInformation.builder()
                .country("country")
                .addressLine1("aa")
                .dxAddress(getDxAddress())
                .postCode("postcode").build();
        }

        private List<DxAddress> getDxAddress() {
            return Collections.singletonList(DxAddress.builder()
                .dxNumber("dxnumber")
                .dxExchange("exchange")
                .build());
        }
    }
}
