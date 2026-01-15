package com.amalitech.smartecommerce.models;

import java.time.LocalDateTime;

/**
 * Review model representing customer reviews for products.
 * This is a candidate for NoSQL storage due to unstructured comment data.
 */
public class Review {
    private int reviewId;
    private int productId;
    private int userId;
    private int rating;
    private String title;
    private String comment;
    private boolean verifiedPurchase;
    private int helpfulVotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Transient fields for display
    private String productName;
    private String userName;

    public Review() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Review(int productId, int userId, int rating, String title, String comment) {
        this();
        this.productId = productId;
        this.userId = userId;
        this.rating = rating;
        this.title = title;
        this.comment = comment;
        this.verifiedPurchase = false;
        this.helpfulVotes = 0;
    }

    public Review(int reviewId, int productId, int userId, int rating, String title, 
                  String comment, boolean verifiedPurchase, int helpfulVotes,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.reviewId = reviewId;
        this.productId = productId;
        this.userId = userId;
        this.rating = rating;
        this.title = title;
        this.comment = comment;
        this.verifiedPurchase = verifiedPurchase;
        this.helpfulVotes = helpfulVotes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getReviewId() {
        return reviewId;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        this.rating = rating;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isVerifiedPurchase() {
        return verifiedPurchase;
    }

    public void setVerifiedPurchase(boolean verifiedPurchase) {
        this.verifiedPurchase = verifiedPurchase;
    }

    public int getHelpfulVotes() {
        return helpfulVotes;
    }

    public void setHelpfulVotes(int helpfulVotes) {
        this.helpfulVotes = helpfulVotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void incrementHelpfulVotes() {
        this.helpfulVotes++;
    }

    @Override
    public String toString() {
        return "Review{" +
                "reviewId=" + reviewId +
                ", productId=" + productId +
                ", userId=" + userId +
                ", rating=" + rating +
                ", title='" + title + '\'' +
                ", verifiedPurchase=" + verifiedPurchase +
                ", helpfulVotes=" + helpfulVotes +
                '}';
    }

    /**
     * Returns a star rating representation.
     */
    public String getStarRating() {
        return "★".repeat(rating) + "☆".repeat(5 - rating);
    }
}
