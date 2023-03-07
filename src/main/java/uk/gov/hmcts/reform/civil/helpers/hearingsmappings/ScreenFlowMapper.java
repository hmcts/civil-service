package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import uk.gov.hmcts.reform.civil.model.hearingvalues.ScreenNavigationModel;

import java.util.List;

public class ScreenFlowMapper {

    private ScreenFlowMapper() {
        //NO-OP
    }

    public static List<ScreenNavigationModel> getScreenFlow() {
        return List.of(ScreenNavigationModel.builder().build());
    }
}
