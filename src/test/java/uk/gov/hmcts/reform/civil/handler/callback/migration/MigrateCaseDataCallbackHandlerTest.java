package uk.gov.hmcts.reform.civil.handler.callback.migration;

import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.utils.CaseMigrationUtility;

@SpringBootTest(classes = {
    MigrateCaseDataCallbackHandler.class,
    CaseMigrationUtility.class,
    JacksonAutoConfiguration.class})
public class MigrateCaseDataCallbackHandlerTest extends BaseCallbackHandlerTest {


}
