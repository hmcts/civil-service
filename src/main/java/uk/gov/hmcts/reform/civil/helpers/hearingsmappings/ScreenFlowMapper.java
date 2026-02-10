package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import uk.gov.hmcts.reform.civil.model.hearingvalues.NavigationModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.ScreenNavigationModel;

import java.util.ArrayList;
import java.util.List;

public class ScreenFlowMapper {

    private ScreenFlowMapper() {
        //NO-OP
    }

    static final String HEARING_JUDGE = "hearing-judge";

    public static List<ScreenNavigationModel> getScreenFlow() {
        NavigationModel navigation1 = new NavigationModel();
        navigation1.setResultValue("hearing-facilities");
        ScreenNavigationModel screen1 = new ScreenNavigationModel();
        screen1.setScreenName("hearing-requirements");
        screen1.setNavigation(List.of(navigation1));
        List<ScreenNavigationModel> screenNavigationList = new ArrayList<>();
        screenNavigationList.add(screen1);

        NavigationModel navigation2 = new NavigationModel();
        navigation2.setResultValue("hearing-stage");
        ScreenNavigationModel screen2 = new ScreenNavigationModel();
        screen2.setScreenName("hearing-facilities");
        screen2.setNavigation(List.of(navigation2));
        screenNavigationList.add(screen2);

        NavigationModel navigation3 = new NavigationModel();
        navigation3.setResultValue("hearing-attendance");
        ScreenNavigationModel screen3 = new ScreenNavigationModel();
        screen3.setScreenName("hearing-stage");
        screen3.setNavigation(List.of(navigation3));
        screenNavigationList.add(screen3);

        NavigationModel navigation4 = new NavigationModel();
        navigation4.setResultValue("hearing-venue");
        ScreenNavigationModel screen4 = new ScreenNavigationModel();
        screen4.setScreenName("hearing-attendance");
        screen4.setNavigation(List.of(navigation4));
        screenNavigationList.add(screen4);

        NavigationModel navigation5a = new NavigationModel();
        navigation5a.setResultValue("hearing-welsh");
        navigation5a.setConditionOperator("INCLUDE");
        navigation5a.setConditionValue(HearingDetailsMapper.WELSH_REGION_ID);
        NavigationModel navigation5b = new NavigationModel();
        navigation5b.setResultValue(HEARING_JUDGE);
        navigation5b.setConditionOperator("NOT INCLUDE");
        navigation5b.setConditionValue(HearingDetailsMapper.WELSH_REGION_ID);
        ScreenNavigationModel screen5 = new ScreenNavigationModel();
        screen5.setScreenName("hearing-venue");
        screen5.setNavigation(List.of(navigation5a, navigation5b));
        screen5.setConditionKey("regionId");
        screenNavigationList.add(screen5);

        NavigationModel navigation6 = new NavigationModel();
        navigation6.setResultValue(HEARING_JUDGE);
        ScreenNavigationModel screen6 = new ScreenNavigationModel();
        screen6.setScreenName("hearing-welsh");
        screen6.setNavigation(List.of(navigation6));
        screenNavigationList.add(screen6);

        NavigationModel navigation7 = new NavigationModel();
        navigation7.setResultValue("hearing-timing");
        ScreenNavigationModel screen7 = new ScreenNavigationModel();
        screen7.setScreenName(HEARING_JUDGE);
        screen7.setNavigation(List.of(navigation7));
        screenNavigationList.add(screen7);

        NavigationModel navigation8 = new NavigationModel();
        navigation8.setResultValue("hearing-additional-instructions");
        ScreenNavigationModel screen8 = new ScreenNavigationModel();
        screen8.setScreenName("hearing-timing");
        screen8.setNavigation(List.of(navigation8));
        screenNavigationList.add(screen8);

        NavigationModel navigation9 = new NavigationModel();
        navigation9.setResultValue("hearing-create-edit-summary");
        ScreenNavigationModel screen9 = new ScreenNavigationModel();
        screen9.setScreenName("hearing-additional-instructions");
        screen9.setNavigation(List.of(navigation9));
        screenNavigationList.add(screen9);

        return screenNavigationList;
    }
}
