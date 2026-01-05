package uk.gov.moj.cpp.support.query.api;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class QueryParamterValidatorTest {

    @InjectMocks
    private QueryParameterValidator queryParamterValidator;

    @Test
    public void shouldReturnTrueIfAQueryParameterIsAUuid() throws Exception {

        final String uuid = randomUUID().toString();

        assertThat(queryParamterValidator.isValidUuid(uuid), is(true));
    }

    @Test
    public void shouldReturnFalseIfAQueryParameterIsNotAUuid() throws Exception {

        final String somethingSilly = "Not a uuid";

        assertThat(queryParamterValidator.isValidUuid(somethingSilly), is(false));
    }

    @Test
    public void shouldReturnTrueIfAQuieryParameterIsAnIsoDate() throws Exception {

        assertThat(queryParamterValidator.isValidIsoDate("2018-02-23T17:00:43.511Z"), is(true));
    }

    @Test
    public void shouldReturnTrueIfAQuieryParameterIsNotAnIsoDate() throws Exception {

        assertThat(queryParamterValidator.isValidIsoDate("not a date"), is(false));
    }
}
