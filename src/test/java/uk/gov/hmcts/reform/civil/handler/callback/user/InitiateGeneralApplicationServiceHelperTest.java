package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationServiceHelper;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.EXTEND_TIME;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.SUMMARY_JUDGEMENT;
import static uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest.APPLICANT_EMAIL_ID_CONSTANT;
import static uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest.RESPONDENT_EMAIL_ID_CONSTANT;
import static uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationDetailsBuilder.STRING_CONSTANT;

@SpringBootTest(classes = {
    InitiateGeneralApplicationServiceHelper.class,
    JacksonAutoConfiguration.class,
})
public class InitiateGeneralApplicationServiceHelperTest {

    private static final String TEST_USER_EMAILID = "test@gmail.com";

    @Autowired
    private InitiateGeneralApplicationServiceHelper helper;

    @MockBean
    protected IdamClient idamClient;

    CaseData caseData = GeneralApplicationDetailsBuilder.builder()
        .getTestCaseData(CaseData.builder().build());

    public UserDetails getUserDetails(String email) {
        return UserDetails.builder().id(STRING_CONSTANT)
            .email(email)
            .build();
    }

    public CaseData getTestCaseData(CaseData caseData) {
        return caseData.toBuilder()
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                .id(STRING_CONSTANT)
                                                .email(APPLICANT_EMAIL_ID_CONSTANT).build())
            .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                              .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                                                                .organisationID(STRING_CONSTANT).build())
                                              .orgPolicyReference(STRING_CONSTANT).build())
            .generalApplications(ElementUtils.wrapElements(getGeneralApplication()))
            .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                               .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                                                                 .organisationID(STRING_CONSTANT).build())
                                               .orgPolicyReference(STRING_CONSTANT).build())
            .respondentSolicitor1EmailAddress(RESPONDENT_EMAIL_ID_CONSTANT)
            .build();
    }

    public GeneralApplication getGeneralApplication() {
        GeneralApplication.GeneralApplicationBuilder builder = GeneralApplication.builder();
        return builder.generalAppType(GAApplicationType.builder()
                                          .types(singletonList(SUMMARY_JUDGEMENT))
                                          .build())
            .build();
    }

    @Test
    void shouldReturnTrueIfGA_ApplicantSameAsPC_Applicant() {

        Boolean result = helper.isGA_ApplicantSameAsPC_Applicant(caseData, getUserDetails(APPLICANT_EMAIL_ID_CONSTANT));
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseIfGA_ApplicantNotSameAsPC_Applicant() {

        Boolean result = helper.isGA_ApplicantSameAsPC_Applicant(caseData, getUserDetails(TEST_USER_EMAILID));
        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnTrueIfGA_ApplicantSameAsPC_Respondent() {

        Boolean result = helper
            .isGA_ApplicantSameAsPC_Respondent(caseData, getUserDetails(RESPONDENT_EMAIL_ID_CONSTANT));
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseIfGA_ApplicantNotSameAsPC_Respondent() {

        Boolean result = helper.isGA_ApplicantSameAsPC_Respondent(caseData, getUserDetails(TEST_USER_EMAILID));
        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnRespondent1SolicitorUserDetails() {

        IdamUserDetails result = helper
            .constructRespondent1SolicitorUserDetails(getUserDetails(APPLICANT_EMAIL_ID_CONSTANT));

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(APPLICANT_EMAIL_ID_CONSTANT);
        assertThat(result.getId()).isEqualTo(STRING_CONSTANT);
    }

    @Test
    void shouldReturnsApplicantAndRespondentDetails() {

        GeneralApplication result = helper
            .setApplicantAndRespondentDetailsIfExits(GeneralApplication.builder().build(),
                                                    getTestCaseData(CaseData.builder().build()),
                                                    getUserDetails(APPLICANT_EMAIL_ID_CONSTANT));

        assertThat(result).isNotNull();
        assertThat(result.getApplicantSolicitor1UserDetails()).isNotNull();
        assertThat(result.getApplicantSolicitor1UserDetails().getEmail()).isEqualTo(APPLICANT_EMAIL_ID_CONSTANT);
        assertThat(result.getRespondentSolicitor1EmailAddress()).isNotNull();
        assertThat(result.getRespondentSolicitor1EmailAddress()).isEqualTo(RESPONDENT_EMAIL_ID_CONSTANT);
        assertThat(result.getRespondent1OrganisationPolicy()).isNotNull();
        assertThat(result.getRespondent1OrganisationPolicy().getOrganisation().getOrganisationID())
            .isEqualTo(STRING_CONSTANT);
        assertThat(result.getApplicant1OrganisationPolicy()).isNotNull();
        assertThat(result.getApplicant1OrganisationPolicy().getOrganisation().getOrganisationID())
            .isEqualTo(STRING_CONSTANT);
    }
}
