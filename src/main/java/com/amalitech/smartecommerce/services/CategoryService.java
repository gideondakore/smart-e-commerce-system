package com.amalitech.smartecommerce.services;

import com.amalitech.smartecommerce.dao.CategoryDAO;
import com.amalitech.smartecommerce.models.Category;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service layer for Category operations.
 * Implements caching for frequently accessed categories.
 */
public class CategoryService {
    
    private final CategoryDAO categoryDAO;
    private final Map<Integer, Category> categoryCache;
    private final Map<String, Category> categoryNameCache;
    private List<Category> allCategoriesCache;
    private long cacheTimestamp;
    private static final long CACHE_TTL_MS = 300000; // 5 minutes TTL

    public CategoryService() {
        this.categoryDAO = new CategoryDAO();
        this.categoryCache = new ConcurrentHashMap<>();
        this.categoryNameCache = new ConcurrentHashMap<>();
        this.cacheTimestamp = 0;
    }

    // For testing injection
    public CategoryService(CategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
        this.categoryCache = new ConcurrentHashMap<>();
        this.categoryNameCache = new ConcurrentHashMap<>();
        this.cacheTimestamp = 0;
    }

    /**
     * Creates a new category and invalidates cache.
     */
    public void createCategory(Category category) throws SQLException {
        categoryDAO.create(category);
        invalidateCache();
        // Add to cache
        categoryCache.put(category.getCategoryId(), category);
        categoryNameCache.put(category.getName().toLowerCase(), category);
        System.out.println("[CategoryService] Created and cached category: " + category.getName());
    }

    /**
     * Gets a category by ID with caching.
     */
    public Category getCategoryById(int id) throws SQLException {
        // Check cache first
        if (categoryCache.containsKey(id)) {
            System.out.println("[CategoryService] Cache HIT for category ID: " + id);
            return categoryCache.get(id);
        }

        // Cache miss - query database
        System.out.println("[CategoryService] Cache MISS for category ID: " + id);
        Category category = categoryDAO.findById(id);
        
        if (category != null) {
            categoryCache.put(id, category);
            categoryNameCache.put(category.getName().toLowerCase(), category);
        }
        
        return category;
    }

    /**
     * Gets a category by name with caching (case-insensitive).
     */
    public Category getCategoryByName(String name) throws SQLException {
        String lowerName = name.toLowerCase();
        
        // Check cache first
        if (categoryNameCache.containsKey(lowerName)) {
            System.out.println("[CategoryService] Cache HIT for category name: " + name);
            return categoryNameCache.get(lowerName);
        }

        // Cache miss - load all categories to populate cache
        System.out.println("[CategoryService] Cache MISS for category name: " + name);
        List<Category> categories = getAllCategories();
        
        for (Category category : categories) {
            if (category.getName().equalsIgnoreCase(name)) {
                return category;
            }
        }
        
        return null;
    }

    /**
     * Gets all categories with caching.
     * Uses TTL-based cache invalidation.
     */
    public List<Category> getAllCategories() throws SQLException {
        long now = System.currentTimeMillis();
        
        // Check if cache is valid
        if (allCategoriesCache != null && (now - cacheTimestamp) < CACHE_TTL_MS) {
            System.out.println("[CategoryService] Returning cached categories list");
            return allCategoriesCache;
        }

        // Cache miss or expired - query database
        System.out.println("[CategoryService] Loading categories from database");
        allCategoriesCache = categoryDAO.findAll();
        cacheTimestamp = now;
        
        // Populate individual caches
        for (Category category : allCategoriesCache) {
            categoryCache.put(category.getCategoryId(), category);
            categoryNameCache.put(category.getName().toLowerCase(), category);
        }
        
        return allCategoriesCache;
    }

    /**
     * Updates a category and invalidates cache.
     */
    public void updateCategory(Category category) throws SQLException {
        categoryDAO.update(category);
        invalidateCache();
        // Update cache
        categoryCache.put(category.getCategoryId(), category);
        categoryNameCache.put(category.getName().toLowerCase(), category);
        System.out.println("[CategoryService] Updated and re-cached category: " + category.getName());
    }

    /**
     * Deletes a category and invalidates cache.
     */
    public void deleteCategory(int id) throws SQLException {
        Category category = categoryCache.get(id);
        categoryDAO.delete(id);
        
        // Remove from caches
        categoryCache.remove(id);
        if (category != null) {
            categoryNameCache.remove(category.getName().toLowerCase());
        }
        invalidateCache();
        System.out.println("[CategoryService] Deleted category ID: " + id);
    }

    /**
     * Invalidates the all-categories cache.
     */
    private void invalidateCache() {
        allCategoriesCache = null;
        cacheTimestamp = 0;
        System.out.println("[CategoryService] Cache invalidated");
    }

    /**
     * Clears all caches completely.
     */
    public void clearCache() {
        categoryCache.clear();
        categoryNameCache.clear();
        allCategoriesCache = null;
        cacheTimestamp = 0;
        System.out.println("[CategoryService] All caches cleared");
    }

    /**
     * Returns cache statistics.
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("idCacheSize", categoryCache.size());
        stats.put("nameCacheSize", categoryNameCache.size());
        stats.put("allCategoriesCached", allCategoriesCache != null);
        stats.put("cacheAge", System.currentTimeMillis() - cacheTimestamp);
        stats.put("cacheTTL", CACHE_TTL_MS);
        return stats;
    }

    /**
     * Preloads all categories into cache.
     * Useful for application startup.
     */
    public void preloadCache() throws SQLException {
        System.out.println("[CategoryService] Preloading category cache...");
        getAllCategories();
        System.out.println("[CategoryService] Preloaded " + categoryCache.size() + " categories");
    }
}
