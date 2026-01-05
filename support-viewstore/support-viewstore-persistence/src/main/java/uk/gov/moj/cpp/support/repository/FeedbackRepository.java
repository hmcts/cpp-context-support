package uk.gov.moj.cpp.support.repository;

import uk.gov.moj.cpp.support.entity.Feedback;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@Repository
public interface FeedbackRepository extends EntityRepository<Feedback, UUID> {

    List<Feedback> findBycaseId(final UUID caseId);
    List<Feedback> findByDateReceivedBetween(final ZonedDateTime from, final ZonedDateTime to);
    List<Feedback> findByCaseIdAndDateReceivedBetween(final UUID caseId, final ZonedDateTime from, final ZonedDateTime to);
}
