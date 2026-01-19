# Review Feature Documentation

## Overview
The review functionality allows customers to rate and review products, while admins can manage all reviews in the system.

## Features Implemented

### Customer Features
1. **View Product Reviews**
   - Select a product and click "View Reviews"
   - See all reviews with ratings, titles, comments, and helpful votes
   - View average rating and total review count
   - Mark reviews as helpful

2. **Add Product Review**
   - Select a product and click "Add Review"
   - Provide rating (1-5 stars), title, and comment
   - Reviews are linked to the user's account

### Admin Features
1. **Manage All Reviews**
   - View all reviews across all products
   - See product name, user name, rating, title, and comment
   - Delete inappropriate or spam reviews
   - Monitor helpful votes

## Database Schema

```sql
CREATE TABLE reviews (
    review_id SERIAL PRIMARY KEY,
    product_id INT NOT NULL,
    user_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title VARCHAR(255),
    comment TEXT,
    is_verified_purchase BOOLEAN DEFAULT FALSE,
    helpful_votes INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
```

## Architecture

### Model Layer
- **Review.java**: Entity class with validation (rating 1-5)
- Includes transient fields for display (productName, userName)
- Helper method: `getStarRating()` returns visual star representation

### DAO Layer
- **ReviewDAO.java**: Data access operations
  - `create()`: Add new review
  - `findByProductId()`: Get all reviews for a product
  - `findByUserId()`: Get all reviews by a user
  - `findAll()`: Get all reviews (admin)
  - `getAverageRating()`: Calculate average rating
  - `getReviewCount()`: Count reviews for a product
  - `incrementHelpfulVotes()`: Mark review as helpful
  - `update()`: Edit review
  - `delete()`: Remove review

### Service Layer
- **ReviewService.java**: Business logic
  - Wraps DAO operations
  - Can be extended with caching, validation, etc.

### Controller Layer
- **CustomerDashboardController.java**:
  - `handleViewReviews()`: Display product reviews
  - `handleAddReview()`: Add new review dialog
  - `showProductReviews()`: Review table with helpful votes
  - `showAddReviewDialog()`: Review input form

- **AdminDashboardController.java**:
  - `handleManageReviews()`: View and delete reviews

## UI Components

### Customer Dashboard
- **View Reviews Button**: Orange button to view product reviews
- **Add Review Button**: Purple button to write a review
- **Review Dialog**: Table showing all reviews with helpful votes
- **Add Review Dialog**: Form with rating dropdown, title, and comment fields

### Admin Dashboard
- **Manage Reviews Button**: Purple button in bottom action bar
- **Review Management Dialog**: Table with delete functionality

## Usage Examples

### Customer: Adding a Review
1. Login as customer
2. Select a product from the table
3. Click "Add Review"
4. Select rating (1-5 stars)
5. Enter title and comment
6. Click OK

### Customer: Viewing Reviews
1. Select a product
2. Click "View Reviews"
3. See average rating and review count
4. Select a review and click "Mark Helpful" to upvote

### Admin: Managing Reviews
1. Login as admin
2. Click "Manage Reviews" in bottom action bar
3. View all reviews across all products
4. Select inappropriate review
5. Click "Delete" and confirm

## Performance Considerations

### Indexes
```sql
CREATE INDEX idx_reviews_product ON reviews(product_id);
CREATE INDEX idx_reviews_user ON reviews(user_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);
```

### Optimization Opportunities
- Cache average ratings per product
- Implement pagination for large review lists
- Add full-text search on review comments (candidate for NoSQL)

## Future Enhancements
1. **Verified Purchase Badge**: Only allow reviews from customers who purchased
2. **Review Images**: Allow customers to upload product photos
3. **Review Responses**: Let sellers respond to reviews
4. **Sorting/Filtering**: Sort by rating, date, helpfulness
5. **Review Moderation**: Flag system for inappropriate content
6. **NoSQL Migration**: Store review comments in MongoDB for better text search

## Testing

Run the review test:
```bash
mvn compile exec:java -Dexec.mainClass="com.amalitech.smartecommerce.test.ReviewTest"
```

## Notes
- Reviews are automatically timestamped
- Rating validation ensures values between 1-5
- Cascade delete: Reviews are deleted when product/user is deleted
- Helpful votes can be incremented multiple times (future: track who voted)
