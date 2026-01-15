package com.amalitech.smartecommerce.utils;

import com.amalitech.smartecommerce.dao.ProductDAO;
import com.amalitech.smartecommerce.models.Product;
import com.amalitech.smartecommerce.services.ProductService;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Performance Test Runner for measuring and reporting optimization gains.
 * Tests database indexing, caching, and DSA algorithm performance.
 */
public class PerformanceTestRunner {

    private static final int PRODUCT_COUNT = 10000;
    private static final String SEARCH_TERM = "Performance Product 5000";
    private static final String REPORT_PATH = "doc/reports/performance-reports.md";

    private final List<TestResult> results = new ArrayList<>();

    public static void main(String[] args) {
        PerformanceTestRunner runner = new PerformanceTestRunner();
        runner.runAllTests();
    }

    public void runAllTests() {
        IO.println("╔══════════════════════════════════════════════════════════════╗");
        IO.println("║         SMART E-COMMERCE PERFORMANCE TEST SUITE              ║");
        IO.println("╚══════════════════════════════════════════════════════════════╝\n");

        try {
            ProductService productService = new ProductService();
            ProductDAO productDAO = new ProductDAO();

            // 1. Setup: Populate test data
            IO.println("📦 Phase 1: Data Population");
            IO.println("─".repeat(50));
            populateTestData();
            IO.println();

            // 2. Index Performance Tests
            IO.println("🔍 Phase 2: Index Performance Comparison");
            IO.println("─".repeat(50));
            testIndexPerformance(productService);
            IO.println();

            // 3. Cache Performance Tests
            IO.println("💾 Phase 3: Cache Performance Comparison");
            IO.println("─".repeat(50));
            testCachePerformance(productService);
            IO.println();

            // 4. Sorting Algorithm Comparison
            IO.println("📊 Phase 4: Sorting Algorithm Comparison");
            IO.println("─".repeat(50));
            testSortingAlgorithms(productService);
            IO.println();

            // 5. Search Algorithm Comparison
            IO.println("🔎 Phase 5: Search Algorithm Comparison");
            IO.println("─".repeat(50));
            testSearchAlgorithms(productService);
            IO.println();

            // 6. Generate Report
            IO.println("📝 Phase 6: Generating Performance Report");
            IO.println("─".repeat(50));
            generateReport();

            // 7. Cleanup
            IO.println("\n🧹 Cleaning up test data...");
            cleanupTestData();

            IO.println("\n✅ All tests completed successfully!");
            IO.println("📄 Report saved to: " + REPORT_PATH);

        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void populateTestData() throws SQLException {
        IO.println("Populating database with " + PRODUCT_COUNT + " test products...");
        
        PerformanceTimer timer = new PerformanceTimer();
        timer.start();

        String sql = "INSERT INTO products (name, price, stock_quantity, category_id, description) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);
            Random rand = new Random(42); // Fixed seed for reproducibility

            for (int i = 0; i < PRODUCT_COUNT; i++) {
                stmt.setString(1, "Performance Product " + i);
                stmt.setDouble(2, 10.0 + rand.nextDouble() * 990.0);
                stmt.setInt(3, rand.nextInt(500));
                stmt.setInt(4, 1 + rand.nextInt(8)); // Categories 1-8
                stmt.setString(5, "Test product description for performance testing #" + i);
                stmt.addBatch();

                if (i % 1000 == 0) {
                    stmt.executeBatch();
                    IO.print(".");
                }
            }
            stmt.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
        }

        long duration = timer.end();
        IO.println("\n✓ Populated " + PRODUCT_COUNT + " products in " + 
                          PerformanceTimer.formatDuration(duration));
        
        results.add(new TestResult("Data Population", PRODUCT_COUNT + " records", duration, 0, 0));
    }

    private void testIndexPerformance(ProductService productService) throws SQLException {
        // Test WITHOUT index (drop it first)
        IO.println("Testing search WITHOUT index...");
        dropIndex("idx_products_name_lower");
        
        // Clear cache to ensure DB query
        productService.clearCache();
        
        // Run multiple searches and average
        long totalNoIndex = 0;
        int iterations = 5;
        for (int i = 0; i < iterations; i++) {
            productService.clearCache();
            PerformanceTimer timer = new PerformanceTimer();
            timer.start();
            productService.searchProductsByName("Performance Product 500");
            totalNoIndex += timer.end();
        }
        long avgNoIndex = totalNoIndex / iterations;
        IO.println("  Average time (No Index): " + PerformanceTimer.formatDuration(avgNoIndex));

        // Test WITH index
        IO.println("Testing search WITH index...");
        createIndex("idx_products_name_lower", "LOWER(name)");
        
        long totalWithIndex = 0;
        for (int i = 0; i < iterations; i++) {
            productService.clearCache();
            PerformanceTimer timer = new PerformanceTimer();
            timer.start();
            productService.searchProductsByName("Performance Product 500");
            totalWithIndex += timer.end();
        }
        long avgWithIndex = totalWithIndex / iterations;
        IO.println("  Average time (With Index): " + PerformanceTimer.formatDuration(avgWithIndex));

        double improvement = avgNoIndex > 0 ? ((double)(avgNoIndex - avgWithIndex) / avgNoIndex) * 100 : 0;
        System.out.printf("  📈 Improvement: %.2f%%\n", improvement);
        
        results.add(new TestResult("Search (No Index)", SEARCH_TERM, avgNoIndex, 0, 0));
        results.add(new TestResult("Search (With Index)", SEARCH_TERM, avgWithIndex, avgNoIndex, improvement));
    }

    private void testCachePerformance(ProductService productService) throws SQLException {
        // Get a product ID for testing
        List<Product> products = productService.getAllProducts();
        if (products.isEmpty()) {
            IO.println("  ⚠ No products found for cache test");
            return;
        }
        
        int testId = products.get(products.size() / 2).getProductId();
        
        // Clear cache and test DB fetch
        productService.clearCache();
        
        IO.println("Testing fetch from DATABASE (cache miss)...");
        PerformanceTimer timer = new PerformanceTimer();
        timer.start();
        productService.getProductById(testId);
        long dbFetchTime = timer.end();
        IO.println("  DB Fetch Time: " + PerformanceTimer.formatDuration(dbFetchTime));

        // Test cache hit (multiple times for accuracy)
        IO.println("Testing fetch from CACHE (cache hit)...");
        long totalCacheTime = 0;
        int iterations = 100;
        for (int i = 0; i < iterations; i++) {
            timer.start();
            productService.getProductById(testId);
            totalCacheTime += timer.end();
        }
        long avgCacheTime = totalCacheTime / iterations;
        IO.println("  Cache Fetch Time (avg): " + PerformanceTimer.formatDuration(avgCacheTime));

        double improvement = dbFetchTime > 0 ? ((double)(dbFetchTime - avgCacheTime) / dbFetchTime) * 100 : 0;
        System.out.printf("  📈 Improvement: %.2f%%\n", improvement);
        System.out.printf("  ⚡ Cache is %.1fx faster\n", dbFetchTime > 0 ? (double)dbFetchTime / avgCacheTime : 0);
        
        results.add(new TestResult("Fetch (Database)", "Single Product", dbFetchTime, 0, 0));
        results.add(new TestResult("Fetch (Cache)", "Single Product", avgCacheTime, dbFetchTime, improvement));
    }

    private void testSortingAlgorithms(ProductService productService) throws SQLException {
        List<Product> products = productService.getAllProducts();
        IO.println("Testing with " + products.size() + " products...\n");

        // QuickSort
        PerformanceTimer timer = new PerformanceTimer();
        timer.start();
        productService.quickSortByPrice(new ArrayList<>(products), true);
        long quickSortTime = timer.end();
        IO.println("  QuickSort: " + PerformanceTimer.formatDuration(quickSortTime));
        results.add(new TestResult("QuickSort (Price)", products.size() + " items", quickSortTime, 0, 0));

        // Java TimSort (via stream sort)
        timer.start();
        productService.sortProductsByPrice(new ArrayList<>(products), true);
        long timSortTime = timer.end();
        IO.println("  TimSort:   " + PerformanceTimer.formatDuration(timSortTime));
        results.add(new TestResult("TimSort (Price)", products.size() + " items", timSortTime, 0, 0));

        // MergeSort by name
        timer.start();
        OptimizationUtils.mergeSortByName(new ArrayList<>(products), true);
        long mergeSortTime = timer.end();
        IO.println("  MergeSort: " + PerformanceTimer.formatDuration(mergeSortTime));
        results.add(new TestResult("MergeSort (Name)", products.size() + " items", mergeSortTime, 0, 0));

        // Comparison output
        IO.println("\n  Algorithm Comparison:");
        System.out.printf("  • QuickSort vs TimSort: %.2fx\n", (double)quickSortTime / timSortTime);
        System.out.printf("  • MergeSort vs TimSort: %.2fx\n", (double)mergeSortTime / timSortTime);
    }

    private void testSearchAlgorithms(ProductService productService) throws SQLException {
        List<Product> products = productService.getAllProducts();
        int targetId = products.get(products.size() / 2).getProductId();
        String targetName = products.get(products.size() / 2).getName();
        
        IO.println("Testing search algorithms with " + products.size() + " products...\n");

        // Linear Search
        PerformanceTimer timer = new PerformanceTimer();
        timer.start();
        OptimizationUtils.linearSearchByName(products, targetName);
        long linearTime = timer.end();
        IO.println("  Linear Search:   " + PerformanceTimer.formatDuration(linearTime));

        // Binary Search (requires sorted list)
        List<Product> sortedById = new ArrayList<>(products);
        sortedById.sort(Comparator.comparingInt(Product::getProductId));
        timer.start();
        OptimizationUtils.binarySearchById(sortedById, targetId);
        long binaryTime = timer.end();
        IO.println("  Binary Search:   " + PerformanceTimer.formatDuration(binaryTime));

        // HashMap Lookup
        Map<String, Product> hashMap = OptimizationUtils.buildNameHashMap(products);
        timer.start();
        hashMap.get(targetName.toLowerCase());
        long hashTime = timer.end();
        IO.println("  HashMap Lookup:  " + PerformanceTimer.formatDuration(hashTime));

        results.add(new TestResult("Linear Search", targetName, linearTime, 0, 0));
        results.add(new TestResult("Binary Search", "ID: " + targetId, binaryTime, 0, 0));
        results.add(new TestResult("HashMap Lookup", targetName, hashTime, 0, 0));

        IO.println("\n  Search Comparison:");
        System.out.printf("  • HashMap is %.1fx faster than Linear Search\n",
                         hashTime > 0 ? (double)linearTime / hashTime : linearTime);
        System.out.printf("  • Binary Search is %.1fx faster than Linear Search\n",
                         binaryTime > 0 ? (double)linearTime / binaryTime : linearTime);
    }

    private void generateReport() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(REPORT_PATH))) {
            writer.println("# Performance Test Report");
            writer.println();
            writer.println("**Generated:** " + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.println();
            writer.println("**Test Environment:**");
            writer.println("- Database: PostgreSQL");
            writer.println("- Test Data Size: " + PRODUCT_COUNT + " products");
            writer.println("- Java Version: " + System.getProperty("java.version"));
            writer.println();
            
            writer.println("## Summary Results");
            writer.println();
            writer.println("| Test | Input | Time (ms) | Baseline (ms) | Improvement |");
            writer.println("|------|-------|-----------|---------------|-------------|");
            
            for (TestResult result : results) {
                writer.printf("| %s | %s | %.3f | %s | %s |\n",
                    result.testName,
                    result.input,
                    result.duration / 1_000_000.0,
                    result.baseline > 0 ? String.format("%.3f", result.baseline / 1_000_000.0) : "-",
                    result.improvement > 0 ? String.format("%.2f%%", result.improvement) : "-"
                );
            }
            
            writer.println();
            writer.println("## Key Findings");
            writer.println();
            writer.println("### 1. Database Indexing");
            writer.println("- Adding an index on `LOWER(name)` significantly improves search performance");
            writer.println("- Index-based searches are recommended for production workloads");
            writer.println();
            writer.println("### 2. Caching Strategy");
            writer.println("- In-memory caching (HashMap) provides near-instant lookups");
            writer.println("- Cache hit rates should be monitored in production");
            writer.println("- TTL-based cache invalidation prevents stale data");
            writer.println();
            writer.println("### 3. Sorting Algorithms");
            writer.println("- Java's TimSort (used by default) performs well on real-world data");
            writer.println("- QuickSort is efficient for random data");
            writer.println("- MergeSort provides stable sorting with O(n log n) guarantee");
            writer.println();
            writer.println("### 4. Search Algorithms");
            writer.println("- HashMap provides O(1) lookup after initial build");
            writer.println("- Binary Search provides O(log n) for sorted data");
            writer.println("- Linear Search is O(n) - avoid for large datasets");
            writer.println();
            writer.println("## Recommendations");
            writer.println();
            writer.println("1. **Always use database indexes** for frequently searched columns");
            writer.println("2. **Implement caching** for frequently accessed data");
            writer.println("3. **Use HashMap** for exact-match lookups");
            writer.println("4. **Use Binary Search** when data is already sorted");
            writer.println("5. **Monitor cache hit rates** to ensure caching effectiveness");
            
            IO.println("✓ Report generated successfully");
            
        } catch (IOException e) {
            System.err.println("Failed to generate report: " + e.getMessage());
        }
    }

    private void cleanupTestData() throws SQLException {
        String sql = "DELETE FROM products WHERE name LIKE 'Performance Product%'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            int deleted = stmt.executeUpdate(sql);
            IO.println("✓ Deleted " + deleted + " test products");
        }
    }

    private void dropIndex(String indexName) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            // PostgreSQL syntax
            stmt.execute("DROP INDEX IF EXISTS " + indexName);
            IO.println("  ✓ Dropped index: " + indexName);
        } catch (SQLException e) {
            IO.println("  ⚠ Could not drop index: " + e.getMessage());
        }
    }

    private void createIndex(String indexName, String expression) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            // PostgreSQL syntax for expression index
            stmt.execute("CREATE INDEX IF NOT EXISTS " + indexName + " ON products (" + expression + ")");
            IO.println("  ✓ Created index: " + indexName);
        } catch (SQLException e) {
            IO.println("  ⚠ Could not create index: " + e.getMessage());
        }
    }

    /**
     * Inner class to hold test results.
     */
    private static class TestResult {
        String testName;
        String input;
        long duration;
        long baseline;
        double improvement;

        TestResult(String testName, String input, long duration, long baseline, double improvement) {
            this.testName = testName;
            this.input = input;
            this.duration = duration;
            this.baseline = baseline;
            this.improvement = improvement;
        }
    }
}
