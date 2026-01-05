package uk.gov.moj.cpp.support.query.api.accesscontrol;

import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.when;

import uk.gov.moj.cpp.accesscontrol.common.providers.UserAndGroupProvider;
import uk.gov.moj.cpp.accesscontrol.drools.Action;
import uk.gov.moj.cpp.accesscontrol.test.utils.BaseDroolsAccessControlTest;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.kie.api.runtime.ExecutionResults;
import org.mockito.Mock;

public class QuerySupportApiAccessControlTest extends BaseDroolsAccessControlTest {

    @Mock
    private UserAndGroupProvider userAndGroupProvider;

    public QuerySupportApiAccessControlTest() {
        super("QUERY_API_SESSION");
    }

    @Override
    protected Map<Class<?>, Object> getProviderMocks() {
        return singletonMap(UserAndGroupProvider.class, userAndGroupProvider);
    }

    @Test
    public void shouldAllowSystemUserToSearchForFeedback() {

        final Action action = createActionFor("support.find-feedback");

        when(userAndGroupProvider.isSystemUser(action)).thenReturn(true);

        final ExecutionResults results = executeRulesWith(action);
        assertSuccessfulOutcome(results);
    }
}
