package uk.gov.moj.cpp.support.query.reponse;

import java.util.List;

public class ResponseView {

    private final List<FeedbackView> searchResults;

    public ResponseView(final List<FeedbackView> searchResults) {
        this.searchResults = searchResults;
    }

    public List<FeedbackView> getSearchResults() {
        return searchResults;
    }
}
