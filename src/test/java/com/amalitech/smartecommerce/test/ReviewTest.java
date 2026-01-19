package com.amalitech.smartecommerce.test;

import com.amalitech.smartecommerce.models.Review;
import com.amalitech.smartecommerce.services.ReviewService;

import java.sql.SQLException;
import java.util.List;

public class ReviewTest {
    public static void main(String[] args) {
        ReviewService reviewService = new ReviewService();
        
        System.out.println("=== Review Functionality Test ===\n");
        
        try {
            // Test 1: Add a review
            System.out.println("Test 1: Adding a review...");
            Review newReview = new Review(1, 1, 5, "Excellent Product!", "This product exceeded my expectations.");
            reviewService.addReview(newReview);
            System.out.println("✓ Review added successfully\n");
            
            // Test 2: Get product reviews
            System.out.println("Test 2: Fetching reviews for product ID 1...");
            List<Review> reviews = reviewService.getProductReviews(1);
            System.out.println("✓ Found " + reviews.size() + " review(s)\n");
            
            // Test 3: Get average rating
            System.out.println("Test 3: Getting average rating...");
            double avgRating = reviewService.getAverageRating(1);
            System.out.println("✓ Average rating: " + String.format("%.1f", avgRating) + " stars\n");
            
            // Test 4: Get review count
            System.out.println("Test 4: Getting review count...");
            int count = reviewService.getReviewCount(1);
            System.out.println("✓ Total reviews: " + count + "\n");
            
            // Test 5: Display reviews
            if (!reviews.isEmpty()) {
                System.out.println("Test 5: Displaying reviews...");
                for (Review review : reviews) {
                    System.out.println("  - " + review.getStarRating() + " | " + review.getTitle());
                    System.out.println("    " + review.getComment());
                    System.out.println("    Helpful votes: " + review.getHelpfulVotes());
                }
            }
            
            System.out.println("\n=== All Tests Passed! ===");
            
        } catch (SQLException e) {
            System.err.println("✗ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
