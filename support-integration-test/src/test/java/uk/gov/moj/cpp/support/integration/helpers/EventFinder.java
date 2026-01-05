package uk.gov.moj.cpp.support.integration.helpers;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static uk.gov.justice.services.test.utils.common.host.TestHostProvider.getHost;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.test.utils.core.jdbc.JdbcConnectionProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.Driver;

public class EventFinder {

    private static final String HOST = getHost();
    private static final String JDBC_POSTGRESQL_URL = "jdbc:postgresql://" + HOST + "/%seventstore";
    private final JdbcConnectionProvider jdbcConnectionProvider = new JdbcConnectionProvider();
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    public <T> Optional<List<T>> findEvent(final String eventName, final UUID streamId, final Class<T> eventClass, final String contextName) throws Exception {

        final String username = contextName;
        final String password = contextName;
        final String url = format(JDBC_POSTGRESQL_URL, contextName);
        final String driverClassName = Driver.class.getName();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        final List<T> events = new ArrayList<>();

        try {
            connection = jdbcConnectionProvider.getConnection(url, username, password, driverClassName);
            preparedStatement = connection.prepareStatement("SELECT payload FROM event_log where name = ? AND stream_id = ?");

            preparedStatement.setString(1, eventName);
            preparedStatement.setObject(2, streamId);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                final String payload = resultSet.getString("payload");

                final T event = objectMapper.readerFor(eventClass).readValue(payload);
                events.add(event);
            }

            if (events.isEmpty()) {
                return empty();
            }

            return of(events);

        } finally {
            close(resultSet);
            close(preparedStatement);
            close(connection);
        }
    }

    private void close(final AutoCloseable closeable) throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }
}
