package com.amalitech.smartecommerce.services;

import com.amalitech.smartecommerce.dao.ReviewDAO;
import com.amalitech.smartecommerce.models.Review;

import java.sql.SQLException;
import java.util.List;

public class ReviewService {
    private final ReviewDAO reviewDAO = new ReviewDAO();

    public void addReview(Review review) throws SQLException {
        reviewDAO.create(review);
    }

    public List<Review> getProductReviews(int productId) throws SQLException {
        return reviewDAO.findByProductId(productId);
    }

    public double getAverageRating(int productId) throws SQLException {
        return reviewDAO.getAverageRating(productId);
    }

    public int getReviewCount(int productId) throws SQLException {
        return reviewDAO.getReviewCount(productId);
    }

    public void markHelpful(int reviewId) throws SQLException {
        reviewDAO.incrementHelpfulVotes(reviewId);
    }
}
