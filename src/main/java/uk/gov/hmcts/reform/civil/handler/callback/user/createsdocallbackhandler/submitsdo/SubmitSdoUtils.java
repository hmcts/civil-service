package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.submitsdo;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;

import static java.util.Objects.isNull;

@Component
public class SubmitSdoUtils {

    public DynamicList deleteLocationList(DynamicList list) {
        if (isNull(list)) {
            return null;
        }
        return DynamicList.builder().value(list.getValue()).build();
    }
}
