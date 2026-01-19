# Quick Start: Review Feature

## How to Use Reviews (Customer)

### Step 1: Login as Customer
```
Email: customer@shop.com
Password: customer123
```

### Step 2: View Product Reviews
1. Select any product from the product table
2. Click the **"View Reviews"** button (orange)
3. A dialog will show:
   - Average rating (e.g., "4.5 ★")
   - Total review count
   - All reviews with user names, ratings, titles, comments
   - Helpful votes for each review
4. To mark a review helpful:
   - Select the review
   - Click **"Mark Helpful"**

### Step 3: Add Your Own Review
1. Select a product from the product table
2. Click the **"Add Review"** button (purple)
3. Fill in the form:
   - **Rating**: Select 1-5 stars from dropdown
   - **Title**: Short summary (e.g., "Great product!")
   - **Comment**: Detailed review (optional)
4. Click **OK** to submit
5. Success message will confirm the review was added

## How to Manage Reviews (Admin)

### Step 1: Login as Admin
```
Email: admin@shop.com
Password: admin123
```

### Step 2: Access Review Management
1. Click **"Manage Reviews"** button (purple, bottom action bar)
2. A dialog shows all reviews across all products

### Step 3: Delete Inappropriate Reviews
1. Select a review from the table
2. Click **"Delete"** button
3. Confirm deletion
4. Review is permanently removed

## Review Display Format

### In Review Table:
```
User Name | Rating | Title | Comment | Helpful Votes
John Doe  |   5    | Great | Amazing product! | 12
```

### Star Rating Display:
- 5 stars: ★★★★★
- 4 stars: ★★★★☆
- 3 stars: ★★★☆☆
- 2 stars: ★★☆☆☆
- 1 star:  ★☆☆☆☆

## Tips

### For Customers:
- ✅ Be honest and detailed in your reviews
- ✅ Rate based on product quality, not shipping
- ✅ Mark helpful reviews to help other shoppers
- ❌ Don't spam or post inappropriate content

### For Admins:
- ✅ Monitor reviews regularly
- ✅ Remove spam and inappropriate content
- ✅ Keep genuine negative reviews (they build trust)
- ❌ Don't delete reviews just because they're negative

## Troubleshooting

### "No reviews found"
- This is normal for new products
- Be the first to review!

### "Please select a product"
- Click on a product row in the table first
- Then click the review button

### Review not appearing
- Refresh by clicking "View Reviews" again
- Check database connection

## Testing the Feature

Run the automated test:
```bash
mvn compile exec:java -Dexec.mainClass="com.amalitech.smartecommerce.test.ReviewTest"
```

Expected output:
```
=== Review Functionality Test ===

Test 1: Adding a review...
✓ Review added successfully

Test 2: Fetching reviews for product ID 1...
✓ Found X review(s)

Test 3: Getting average rating...
✓ Average rating: 4.5 stars

Test 4: Getting review count...
✓ Total reviews: X

=== All Tests Passed! ===
```

## Database Verification

Check reviews in PostgreSQL:
```sql
-- View all reviews
SELECT * FROM reviews;

-- View reviews with product and user names
SELECT r.*, p.name as product_name, 
       CONCAT(u.first_name, ' ', u.last_name) as user_name
FROM reviews r
LEFT JOIN products p ON r.product_id = p.product_id
LEFT JOIN users u ON r.user_id = u.user_id
ORDER BY r.created_at DESC;

-- Get average rating for a product
SELECT AVG(rating) as avg_rating 
FROM reviews 
WHERE product_id = 1;
```

## Feature Highlights

🎯 **Simple**: Just 2 buttons for customers
📊 **Informative**: Shows average rating and count
👍 **Interactive**: Mark reviews as helpful
🛡️ **Secure**: Admins can moderate content
⚡ **Fast**: Indexed queries for performance
