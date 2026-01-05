package uk.gov.moj.cpp.support.entity;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @Column(name = "feedback_id", nullable = false)
    private UUID feedbackId;

    @Column(name = "comment")
    private String comment;

    @Column(name = "ref_url")
    private String refUrl;

    @Column(name = "ref_service")
    private String refService;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "date_received")
    private ZonedDateTime dateReceived;

    @Column(name = "case_id")
    private UUID caseId;

    public Feedback() {

    }

    public Feedback(final UUID feedbackId, final String comment, final String refUrl, final String refService,
                    final Integer rating, final ZonedDateTime dateReceived, final UUID caseId) {
        this.feedbackId = feedbackId;
        this.comment = comment;
        this.refUrl = refUrl;
        this.refService = refService;
        this.rating = rating;
        this.dateReceived = dateReceived;
        this.caseId = caseId;
    }

    public Feedback(final UUID feedbackId, final Integer rating, final ZonedDateTime dateReceived) {
        this.feedbackId = feedbackId;
        this.rating = rating;
        this.dateReceived = dateReceived;
    }

    public UUID getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(UUID feedbackId) {
        this.feedbackId = feedbackId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getRefUrl() {
        return refUrl;
    }

    public void setRefUrl(String refUrl) {
        this.refUrl = refUrl;
    }

    public String getRefService() {
        return refService;
    }

    public void setRefService(String refService) {
        this.refService = refService;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public ZonedDateTime getDateReceived() {
        return dateReceived;
    }

    public void setDateReceived(ZonedDateTime dateReceived) {
        this.dateReceived = dateReceived;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public void setCaseId(UUID caseId) {
        this.caseId = caseId;
    }
}
