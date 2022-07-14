package uk.gov.hmcts.reform.civil.handler.tasks;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.utils.CaseDataContentConverter;

@SpringBootTest(classes = {
    UpdateFromGACaseEventTaskHandler.class,
    CaseDetailsConverter.class,
    CaseDataContentConverter.class,
    CoreCaseDataService.class
})
@ExtendWith(SpringExtension.class)
public class UpdateFromGACaseEventTaskHandlerTest  {

}
