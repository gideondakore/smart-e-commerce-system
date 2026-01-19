# Review Feature Architecture

## System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         PRESENTATION LAYER                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌──────────────────────────┐  ┌──────────────────────────┐    │
│  │  CustomerDashboard.fxml  │  │   AdminDashboard.fxml    │    │
│  │  ┌────────────────────┐  │  │  ┌────────────────────┐  │    │
│  │  │ [View Reviews]     │  │  │  │ [Manage Reviews]   │  │    │
│  │  │ [Add Review]       │  │  │  │                    │  │    │
│  │  └────────────────────┘  │  │  └────────────────────┘  │    │
│  └──────────────────────────┘  └──────────────────────────┘    │
│              ↓                              ↓                    │
│  ┌──────────────────────────┐  ┌──────────────────────────┐    │
│  │ CustomerDashboard        │  │  AdminDashboard          │    │
│  │ Controller               │  │  Controller              │    │
│  │ ┌────────────────────┐   │  │ ┌────────────────────┐   │    │
│  │ │handleViewReviews() │   │  │ │handleManageReviews()│   │    │
│  │ │handleAddReview()   │   │  │ │                    │   │    │
│  │ │showProductReviews()│   │  │ │                    │   │    │
│  │ └────────────────────┘   │  │ └────────────────────┘   │    │
│  └──────────────────────────┘  └──────────────────────────┘    │
│                                                                   │
└───────────────────────────┬───────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│                         SERVICE LAYER                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│                    ┌──────────────────────┐                      │
│                    │   ReviewService      │                      │
│                    │ ┌──────────────────┐ │                      │
│                    │ │ addReview()      │ │                      │
│                    │ │ getProductReviews│ │                      │
│                    │ │ getAverageRating │ │                      │
│                    │ │ getReviewCount   │ │                      │
│                    │ │ markHelpful()    │ │                      │
│                    │ └──────────────────┘ │                      │
│                    └──────────────────────┘                      │
│                                                                   │
└───────────────────────────┬───────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│                          DAO LAYER                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│                    ┌──────────────────────┐                      │
│                    │     ReviewDAO        │                      │
│                    │ ┌──────────────────┐ │                      │
│                    │ │ create()         │ │                      │
│                    │ │ findById()       │ │                      │
│                    │ │ findByProductId()│ │                      │
│                    │ │ findByUserId()   │ │                      │
│                    │ │ findAll()        │ │                      │
│                    │ │ getAverageRating │ │                      │
│                    │ │ getReviewCount   │ │                      │
│                    │ │ update()         │ │                      │
│                    │ │ incrementHelpful │ │                      │
│                    │ │ delete()         │ │                      │
│                    │ └──────────────────┘ │                      │
│                    └──────────────────────┘                      │
│                                                                   │
└───────────────────────────┬───────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│                         MODEL LAYER                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│                    ┌──────────────────────┐                      │
│                    │      Review          │                      │
│                    │ ┌──────────────────┐ │                      │
│                    │ │ reviewId         │ │                      │
│                    │ │ productId        │ │                      │
│                    │ │ userId           │ │                      │
│                    │ │ rating (1-5)     │ │                      │
│                    │ │ title            │ │                      │
│                    │ │ comment          │ │                      │
│                    │ │ verifiedPurchase │ │                      │
│                    │ │ helpfulVotes     │ │                      │
│                    │ │ createdAt        │ │                      │
│                    │ │ updatedAt        │ │                      │
│                    │ │ productName*     │ │                      │
│                    │ │ userName*        │ │                      │
│                    │ └──────────────────┘ │                      │
│                    │ * transient fields   │                      │
│                    └──────────────────────┘                      │
│                                                                   │
└───────────────────────────┬───────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│                       DATABASE LAYER                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│                    PostgreSQL Database                           │
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    reviews TABLE                         │   │
│  ├──────────────────────────────────────────────────────────┤   │
│  │ review_id (PK)          | SERIAL                         │   │
│  │ product_id (FK)         | INT → products.product_id      │   │
│  │ user_id (FK)            | INT → users.user_id            │   │
│  │ rating                  | INT (1-5)                      │   │
│  │ title                   | VARCHAR(255)                   │   │
│  │ comment                 | TEXT                           │   │
│  │ is_verified_purchase    | BOOLEAN                        │   │
│  │ helpful_votes           | INT                            │   │
│  │ created_at              | TIMESTAMP                      │   │
│  │ updated_at              | TIMESTAMP                      │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                   │
│  Indexes:                                                         │
│  • idx_reviews_product ON reviews(product_id)                    │
│  • idx_reviews_user ON reviews(user_id)                          │
│  • idx_reviews_rating ON reviews(rating)                         │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

## Data Flow

### Customer: Adding a Review
```
User clicks "Add Review"
    ↓
CustomerDashboardController.handleAddReview()
    ↓
showAddReviewDialog() - displays form
    ↓
User fills: rating, title, comment
    ↓
ReviewService.addReview(review)
    ↓
ReviewDAO.create(review)
    ↓
INSERT INTO reviews (...)
    ↓
Success message displayed
```

### Customer: Viewing Reviews
```
User clicks "View Reviews"
    ↓
CustomerDashboardController.handleViewReviews()
    ↓
showProductReviews(product)
    ↓
ReviewService.getProductReviews(productId)
ReviewService.getAverageRating(productId)
    ↓
ReviewDAO.findByProductId(productId)
ReviewDAO.getAverageRating(productId)
    ↓
SELECT r.*, p.name, CONCAT(u.first_name, ' ', u.last_name)
FROM reviews r
LEFT JOIN products p ON r.product_id = p.product_id
LEFT JOIN users u ON r.user_id = u.user_id
WHERE r.product_id = ?
    ↓
Display reviews in TableView
```

### Admin: Managing Reviews
```
Admin clicks "Manage Reviews"
    ↓
AdminDashboardController.handleManageReviews()
    ↓
ReviewDAO.findAll()
    ↓
SELECT r.*, p.name, CONCAT(u.first_name, ' ', u.last_name)
FROM reviews r
LEFT JOIN products p ON r.product_id = p.product_id
LEFT JOIN users u ON r.user_id = u.user_id
    ↓
Display all reviews in TableView
    ↓
Admin selects review and clicks "Delete"
    ↓
ReviewDAO.delete(reviewId)
    ↓
DELETE FROM reviews WHERE review_id = ?
    ↓
Review removed from table
```

## Component Interactions

```
┌──────────┐     uses      ┌──────────┐     uses      ┌──────────┐
│Controller│ ────────────> │ Service  │ ────────────> │   DAO    │
└──────────┘               └──────────┘               └──────────┘
     │                          │                           │
     │ displays                 │ validates                 │ queries
     ↓                          ↓                           ↓
┌──────────┐               ┌──────────┐               ┌──────────┐
│   FXML   │               │  Model   │               │ Database │
└──────────┘               └──────────┘               └──────────┘
```

## Key Design Patterns

1. **MVC Pattern**: Separation of concerns
   - Model: Review.java
   - View: FXML files
   - Controller: Dashboard controllers

2. **DAO Pattern**: Data access abstraction
   - ReviewDAO handles all database operations
   - Hides SQL complexity from service layer

3. **Service Layer Pattern**: Business logic
   - ReviewService provides clean API
   - Can add caching, validation, etc.

4. **Dependency Injection**: Loose coupling
   - Controllers use services
   - Services use DAOs
   - Easy to test and maintain
