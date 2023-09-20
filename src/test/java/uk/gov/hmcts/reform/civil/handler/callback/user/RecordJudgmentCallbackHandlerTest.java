package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    RecordJudgementCallbackHandler.class,
})
class RecordJudgementCallbackHandlerTest extends BaseCallbackHandlerTest {


}
