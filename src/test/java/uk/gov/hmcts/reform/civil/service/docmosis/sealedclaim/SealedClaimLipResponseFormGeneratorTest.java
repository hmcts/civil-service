package uk.gov.hmcts.reform.civil.service.docmosis.sealedclaim;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.UnsecuredDocumentManagementService;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    SealedClaimLipResponseFormGenerator.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
public class SealedClaimLipResponseFormGeneratorTest {

    @MockBean
    private UnsecuredDocumentManagementService documentManagementService;
    @MockBean
    private DocumentGeneratorService documentGeneratorService;
    @Autowired
    private SealedClaimLipResponseFormGenerator generator;

    /*
    TODO
    admit and pay immediately
    admit and pay by date
    admit and pay by instalments
    part admit and not paid
    reject and pay
    reject and dispute
     */

    @Test
    public void rejectAndPay() {

    }
}
