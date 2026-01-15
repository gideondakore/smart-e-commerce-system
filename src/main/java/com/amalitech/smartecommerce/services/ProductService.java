package com.amalitech.smartecommerce.services;

import com.amalitech.smartecommerce.dao.ProductDAO;
import com.amalitech.smartecommerce.models.Product;
import com.amalitech.smartecommerce.utils.OptimizationUtils;
import com.amalitech.smartecommerce.utils.PerformanceTimer;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service layer for Product operations.
 * Implements caching, searching, and sorting with DSA optimizations.
 */
public class ProductService {
    
    private final ProductDAO productDAO;
    private final Map<Integer, Product> productCache;
    private final Map<String, List<Product>> searchCache;
    private List<Product> allProductsCache;
    private long cacheTimestamp;
    private static final long CACHE_TTL_MS = 60000; // 1 minute TTL for products

    // Statistics for performance reporting
    private int cacheHits = 0;
    private int cacheMisses = 0;

    public ProductService() {
        this.productDAO = new ProductDAO();
        this.productCache = new ConcurrentHashMap<>();
        this.searchCache = new ConcurrentHashMap<>();
        this.cacheTimestamp = 0;
    }

    // For testing injection
    public ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
        this.productCache = new ConcurrentHashMap<>();
        this.searchCache = new ConcurrentHashMap<>();
        this.cacheTimestamp = 0;
    }

    /**
     * Creates a new product and invalidates relevant caches.
     */
    public void createProduct(Product product) throws SQLException {
        productDAO.create(product);
        invalidateCache();
        // Add to cache
        productCache.put(product.getProductId(), product);
        IO.println("[ProductService] Created and cached product: " + product.getName());
    }

    /**
     * Gets a product by ID with caching (HashMap-based O(1) lookup).
     */
    public Product getProductById(int id) throws SQLException {
        // 1. Check Cache (O(1) HashMap lookup)
        if (productCache.containsKey(id)) {
            cacheHits++;
            IO.println("[ProductService] Cache HIT for Product ID: " + id);
            return productCache.get(id);
        }

        // 2. Cache miss - Query DB
        cacheMisses++;
        IO.println("[ProductService] Cache MISS for Product ID: " + id + ". Querying DB...");
        Product product = productDAO.findById(id);

        // 3. Put in Cache
        if (product != null) {
            productCache.put(id, product);
        }
        return product;
    }

    /**
     * Gets all products with caching.
     */
    public List<Product> getAllProducts() throws SQLException {
        long now = System.currentTimeMillis();
        
        // Check if cache is valid
        if (allProductsCache != null && (now - cacheTimestamp) < CACHE_TTL_MS) {
            cacheHits++;
            IO.println("[ProductService] Returning cached products list");
            return new ArrayList<>(allProductsCache);
        }

        // Cache miss - query database
        cacheMisses++;
        IO.println("[ProductService] Loading products from database");
        allProductsCache = productDAO.findAll();
        cacheTimestamp = now;
        
        // Populate individual cache
        for (Product product : allProductsCache) {
            productCache.put(product.getProductId(), product);
        }
        
        return new ArrayList<>(allProductsCache);
    }

    /**
     * Gets products by category ID.
     */
    public List<Product> getProductsByCategory(int categoryId) throws SQLException {
        return productDAO.findByCategoryId(categoryId);
    }

    /**
     * Updates a product and invalidates cache.
     */
    public void updateProduct(Product product) throws SQLException {
        productDAO.update(product);
        invalidateCache();
        // Update cache
        productCache.put(product.getProductId(), product);
        IO.println("[ProductService] Updated and re-cached Product ID: " + product.getProductId());
    }

    /**
     * Deletes a product and removes from cache.
     */
    public void deleteProduct(int id) throws SQLException {
        productDAO.delete(id);
        productCache.remove(id);
        invalidateCache();
        IO.println("[ProductService] Deleted product ID: " + id);
    }

    // ==========================================
    // SEARCH OPERATIONS (DSA: Indexing, Hashing)
    // ==========================================

    /**
     * Searches products by name using SQL LIKE with index optimization.
     * Case-insensitive search leveraging database index.
     */
    public List<Product> searchProductsByName(String query) throws SQLException {
        if (query == null || query.trim().isEmpty()) {
            return getAllProducts();
        }
        
        String normalizedQuery = query.trim().toLowerCase();
        
        // Check search cache
        if (searchCache.containsKey(normalizedQuery)) {
            cacheHits++;
            IO.println("[ProductService] Search cache HIT for: " + query);
            return searchCache.get(normalizedQuery);
        }
        
        cacheMisses++;
        IO.println("[ProductService] Executing search for: " + query);
        
        // Use SQL-based search with LIKE (leverages idx_products_name_lower index)
        List<Product> results = productDAO.searchByName(query);
        
        // Cache search results
        searchCache.put(normalizedQuery, results);
        
        return results;
    }

    /**
     * Searches products in-memory using hash-based lookup for exact matches.
     * Demonstrates hashing algorithm for optimization.
     */
    public Product searchProductByExactName(String name) throws SQLException {
        // Build hash map for O(1) lookup
        Map<String, Product> nameHashMap = new HashMap<>();
        for (Product product : getAllProducts()) {
            nameHashMap.put(product.getName().toLowerCase(), product);
        }
        return nameHashMap.get(name.toLowerCase());
    }

    /**
     * Binary search for product by ID (requires sorted list).
     * Demonstrates binary search algorithm O(log n).
     */
    public Product binarySearchById(List<Product> sortedProducts, int targetId) {
        return OptimizationUtils.binarySearchById(sortedProducts, targetId);
    }

    // ==========================================
    // SORTING OPERATIONS (DSA: Sorting Algorithms)
    // ==========================================

    /**
     * Sorts products by price using Java's TimSort (via Comparator).
     * Time Complexity: O(n log n)
     */
    public List<Product> sortProductsByPrice(List<Product> products, boolean ascending) {
        PerformanceTimer timer = new PerformanceTimer();
        timer.start();
        
        Comparator<Product> priceComparator = Comparator.comparingDouble(Product::getPrice);
        if (!ascending) {
            priceComparator = priceComparator.reversed();
        }
        
        List<Product> sorted = products.stream()
                .sorted(priceComparator)
                .collect(Collectors.toList());
        
        long duration = timer.end();
        IO.println("[ProductService] Sorted " + products.size() + 
                           " products by price in " + PerformanceTimer.formatDuration(duration));
        
        return sorted;
    }

    /**
     * Sorts products by name alphabetically.
     */
    public List<Product> sortProductsByName(List<Product> products, boolean ascending) {
        Comparator<Product> nameComparator = Comparator.comparing(
            Product::getName, 
            String.CASE_INSENSITIVE_ORDER
        );
        if (!ascending) {
            nameComparator = nameComparator.reversed();
        }
        
        return products.stream()
                .sorted(nameComparator)
                .collect(Collectors.toList());
    }

    /**
     * Sorts products by stock quantity.
     */
    public List<Product> sortProductsByStock(List<Product> products, boolean ascending) {
        Comparator<Product> stockComparator = Comparator.comparingInt(Product::getStockQuantity);
        if (!ascending) {
            stockComparator = stockComparator.reversed();
        }
        
        return products.stream()
                .sorted(stockComparator)
                .collect(Collectors.toList());
    }

    /**
     * Custom QuickSort implementation for educational purposes.
     * Sorts products by price.
     */
    public List<Product> quickSortByPrice(List<Product> products, boolean ascending) {
        PerformanceTimer timer = new PerformanceTimer();
        timer.start();
        
        List<Product> sorted = new ArrayList<>(products);
        OptimizationUtils.quickSortByPrice(sorted, 0, sorted.size() - 1, ascending);
        
        long duration = timer.end();
        IO.println("[ProductService] QuickSort sorted " + products.size() + 
                           " products in " + PerformanceTimer.formatDuration(duration));
        
        return sorted;
    }

    // ==========================================
    // FILTERING OPERATIONS
    // ==========================================

    /**
     * Filters products by price range.
     */
    public List<Product> filterByPriceRange(List<Product> products, double minPrice, double maxPrice) {
        return products.stream()
                .filter(p -> p.getPrice() >= minPrice && p.getPrice() <= maxPrice)
                .collect(Collectors.toList());
    }

    /**
     * Filters products that are in stock.
     */
    public List<Product> filterInStock(List<Product> products) {
        return products.stream()
                .filter(p -> p.getStockQuantity() > 0)
                .collect(Collectors.toList());
    }

    /**
     * Filters products that are low on stock (threshold configurable).
     */
    public List<Product> filterLowStock(List<Product> products, int threshold) {
        return products.stream()
                .filter(p -> p.getStockQuantity() > 0 && p.getStockQuantity() <= threshold)
                .collect(Collectors.toList());
    }

    // ==========================================
    // CACHE MANAGEMENT
    // ==========================================

    /**
     * Invalidates all caches.
     */
    private void invalidateCache() {
        allProductsCache = null;
        searchCache.clear();
        cacheTimestamp = 0;
        IO.println("[ProductService] Cache invalidated");
    }

    /**
     * Clears all caches completely.
     */
    public void clearCache() {
        productCache.clear();
        searchCache.clear();
        allProductsCache = null;
        cacheTimestamp = 0;
        cacheHits = 0;
        cacheMisses = 0;
        IO.println("[ProductService] All caches cleared");
    }

    /**
     * Returns cache statistics for performance reporting.
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("productCacheSize", productCache.size());
        stats.put("searchCacheSize", searchCache.size());
        stats.put("allProductsCached", allProductsCache != null);
        stats.put("cacheHits", cacheHits);
        stats.put("cacheMisses", cacheMisses);
        stats.put("hitRate", cacheHits + cacheMisses > 0 
                ? (double) cacheHits / (cacheHits + cacheMisses) * 100 
                : 0);
        return stats;
    }

    /**
     * Preloads all products into cache.
     */
    public void preloadCache() throws SQLException {
        IO.println("[ProductService] Preloading product cache...");
        getAllProducts();
        IO.println("[ProductService] Preloaded " + productCache.size() + " products");
    }
}
