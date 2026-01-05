package uk.gov.moj.cpp.support.command.api.accesscontrol;

import java.util.Collections;
import java.util.List;

public final class RuleConstants {

    private static final String GROUP_ONLINE_PLEA_SYSTEM_USERS = "Online Plea System Users";

    private RuleConstants() {
        throw new IllegalAccessError("Utility class");
    }

    public static List<String> getSendFeedbackGroup() {
        return Collections.singletonList(GROUP_ONLINE_PLEA_SYSTEM_USERS);
    }
}
