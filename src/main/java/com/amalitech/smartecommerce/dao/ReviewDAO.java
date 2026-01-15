package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.models.Review;
import com.amalitech.smartecommerce.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Review entity.
 * Reviews contain unstructured data (comments) - candidate for NoSQL migration.
 */
public class ReviewDAO {

    /**
     * Creates a new review.
     */
    public void create(Review review) throws SQLException {
        String sql = "INSERT INTO reviews (product_id, user_id, rating, title, comment, is_verified_purchase) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, review.getProductId());
            stmt.setInt(2, review.getUserId());
            stmt.setInt(3, review.getRating());
            stmt.setString(4, review.getTitle());
            stmt.setString(5, review.getComment());
            stmt.setBoolean(6, review.isVerifiedPurchase());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    review.setReviewId(generatedKeys.getInt(1));
                }
            }
        }
    }

    /**
     * Finds a review by ID.
     */
    public Review findById(int id) throws SQLException {
        String sql = "SELECT r.*, p.name as product_name, " +
                     "CONCAT(u.first_name, ' ', u.last_name) as user_name " +
                     "FROM reviews r " +
                     "LEFT JOIN products p ON r.product_id = p.product_id " +
                     "LEFT JOIN users u ON r.user_id = u.user_id " +
                     "WHERE r.review_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReview(rs);
                }
            }
        }
        return null;
    }

    /**
     * Finds all reviews for a specific product.
     */
    public List<Review> findByProductId(int productId) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT r.*, p.name as product_name, " +
                     "CONCAT(u.first_name, ' ', u.last_name) as user_name " +
                     "FROM reviews r " +
                     "LEFT JOIN products p ON r.product_id = p.product_id " +
                     "LEFT JOIN users u ON r.user_id = u.user_id " +
                     "WHERE r.product_id = ? ORDER BY r.created_at DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reviews.add(mapResultSetToReview(rs));
                }
            }
        }
        return reviews;
    }

    /**
     * Finds all reviews by a specific user.
     */
    public List<Review> findByUserId(int userId) throws SQLException {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT r.*, p.name as product_name, " +
                     "CONCAT(u.first_name, ' ', u.last_name) as user_name " +
                     "FROM reviews r " +
                     "LEFT JOIN products p ON r.product_id = p.product_id " +
                     "LEFT JOIN users u ON r.user_id = u.user_id " +
                     "WHERE r.user_id = ? ORDER BY r.created_at DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reviews.add(mapResultSetToReview(rs));
                }
            }
        }
        return reviews;
    }

    /**
     * Returns all reviews.
     */
    public List<Review> findAll() throws SQLException {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT r.*, p.name as product_name, " +
                     "CONCAT(u.first_name, ' ', u.last_name) as user_name " +
                     "FROM reviews r " +
                     "LEFT JOIN products p ON r.product_id = p.product_id " +
                     "LEFT JOIN users u ON r.user_id = u.user_id " +
                     "ORDER BY r.created_at DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                reviews.add(mapResultSetToReview(rs));
            }
        }
        return reviews;
    }

    /**
     * Gets average rating for a product.
     */
    public double getAverageRating(int productId) throws SQLException {
        String sql = "SELECT AVG(rating) as avg_rating FROM reviews WHERE product_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("avg_rating");
                }
            }
        }
        return 0.0;
    }

    /**
     * Gets review count for a product.
     */
    public int getReviewCount(int productId) throws SQLException {
        String sql = "SELECT COUNT(*) as review_count FROM reviews WHERE product_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("review_count");
                }
            }
        }
        return 0;
    }

    /**
     * Updates a review.
     */
    public void update(Review review) throws SQLException {
        String sql = "UPDATE reviews SET rating = ?, title = ?, comment = ? WHERE review_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, review.getRating());
            stmt.setString(2, review.getTitle());
            stmt.setString(3, review.getComment());
            stmt.setInt(4, review.getReviewId());

            stmt.executeUpdate();
        }
    }

    /**
     * Increments the helpful votes for a review.
     */
    public void incrementHelpfulVotes(int reviewId) throws SQLException {
        String sql = "UPDATE reviews SET helpful_votes = helpful_votes + 1 WHERE review_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reviewId);
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes a review.
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Maps a ResultSet row to a Review object.
     */
    private Review mapResultSetToReview(ResultSet rs) throws SQLException {
        Review review = new Review(
            rs.getInt("review_id"),
            rs.getInt("product_id"),
            rs.getInt("user_id"),
            rs.getInt("rating"),
            rs.getString("title"),
            rs.getString("comment"),
            rs.getBoolean("is_verified_purchase"),
            rs.getInt("helpful_votes"),
            rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
            rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null
        );
        
        // Set transient fields if available
        try {
            review.setProductName(rs.getString("product_name"));
            review.setUserName(rs.getString("user_name"));
        } catch (SQLException ignored) {
            // These columns may not be present in all queries
        }
        
        return review;
    }
}
