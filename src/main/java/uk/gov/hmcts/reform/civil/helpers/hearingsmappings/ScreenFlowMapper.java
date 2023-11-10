package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import uk.gov.hmcts.reform.civil.model.hearingvalues.NavigationModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.ScreenNavigationModel;

import java.util.ArrayList;
import java.util.List;

public class ScreenFlowMapper {

    private ScreenFlowMapper() {
        //NO-OP
    }

    public static List<ScreenNavigationModel> getScreenFlow() {
        List<ScreenNavigationModel> screenNavigationList = new ArrayList<>();
        screenNavigationList.add(ScreenNavigationModel.builder()
            .screenName("hearing-requirements")
            .navigation(List.of(NavigationModel.builder()
                            .resultValue("hearing-facilities")
                            .build()))
            .build());

        screenNavigationList.add(ScreenNavigationModel.builder()
                                     .screenName("hearing-facilities")
                                     .navigation(List.of(NavigationModel.builder()
                                                             .resultValue("hearing-stage")
                                                             .build()))
                                     .build());

        screenNavigationList.add(ScreenNavigationModel.builder()
                                     .screenName("hearing-stage")
                                     .navigation(List.of(NavigationModel.builder()
                                                             .resultValue("hearing-attendance")
                                                             .build()))
                                     .build());

        screenNavigationList.add(ScreenNavigationModel.builder()
                                     .screenName("hearing-attendance")
                                     .navigation(List.of(NavigationModel.builder()
                                                             .resultValue("hearing-venue")
                                                             .build()))
                                     .build());

        screenNavigationList.add(ScreenNavigationModel.builder()
                                     .screenName("hearing-venue")
                                     .navigation(List.of(NavigationModel.builder()
                                                             .resultValue("hearing-welsh")
                                                             .conditionOperator("INCLUDE")
                                                             .conditionValue(HearingDetailsMapper.WELSH_REGION_ID)
                                                             .build(),
                                                         NavigationModel.builder()
                                                             .resultValue("hearing-judge")
                                                             .conditionOperator("NOT INCLUDE")
                                                             .conditionValue(HearingDetailsMapper.WELSH_REGION_ID)
                                                             .build()))
                                     .conditionKey("regionId")
                                     .build());

        screenNavigationList.add(ScreenNavigationModel.builder()
                                     .screenName("hearing-welsh")
                                     .navigation(List.of(NavigationModel.builder()
                                                             .resultValue("hearing-judge")
                                                             .build()))
                                     .build());

        screenNavigationList.add(ScreenNavigationModel.builder()
                                     .screenName("hearing-judge")
                                     .navigation(List.of(NavigationModel.builder()
                                                             .resultValue("hearing-timing")
                                                             .build()))
                                     .build());

        screenNavigationList.add(ScreenNavigationModel.builder()
                                     .screenName("hearing-timing")
                                     .navigation(List.of(NavigationModel.builder()
                                                             .resultValue("hearing-additional-instructions")
                                                             .build()))
                                     .build());

        screenNavigationList.add(ScreenNavigationModel.builder()
                                     .screenName("hearing-additional-instructions")
                                     .navigation(List.of(NavigationModel.builder()
                                                             .resultValue("hearing-create-edit-summary")
                                                             .build()))
                                     .build());

        return screenNavigationList;
    }
}
