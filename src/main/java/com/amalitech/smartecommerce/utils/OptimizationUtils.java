package com.amalitech.smartecommerce.utils;

import com.amalitech.smartecommerce.models.Product;

import java.util.*;

/**
 * Utility class implementing Data Structures and Algorithms for optimization.
 * Includes sorting algorithms, searching algorithms, and caching utilities.
 */
public class OptimizationUtils {

    // ==========================================
    // SORTING ALGORITHMS
    // ==========================================

    /**
     * QuickSort implementation for products by price.
     * Time Complexity: O(n log n) average, O(n²) worst case
     * Space Complexity: O(log n) for recursion stack
     * 
     * @param products List to sort (modified in place)
     * @param low Starting index
     * @param high Ending index
     * @param ascending Sort order
     */
    public static void quickSortByPrice(List<Product> products, int low, int high, boolean ascending) {
        if (low < high) {
            int pivotIndex = partition(products, low, high, ascending);
            quickSortByPrice(products, low, pivotIndex - 1, ascending);
            quickSortByPrice(products, pivotIndex + 1, high, ascending);
        }
    }

    private static int partition(List<Product> products, int low, int high, boolean ascending) {
        double pivot = products.get(high).getPrice();
        int i = low - 1;
        
        for (int j = low; j < high; j++) {
            boolean shouldSwap = ascending 
                ? products.get(j).getPrice() < pivot
                : products.get(j).getPrice() > pivot;
            
            if (shouldSwap) {
                i++;
                Collections.swap(products, i, j);
            }
        }
        Collections.swap(products, i + 1, high);
        return i + 1;
    }

    /**
     * MergeSort implementation for products by name.
     * Time Complexity: O(n log n) - stable sort
     * Space Complexity: O(n)
     */
    public static List<Product> mergeSortByName(List<Product> products, boolean ascending) {
        if (products.size() <= 1) {
            return new ArrayList<>(products);
        }
        
        int mid = products.size() / 2;
        List<Product> left = mergeSortByName(products.subList(0, mid), ascending);
        List<Product> right = mergeSortByName(products.subList(mid, products.size()), ascending);
        
        return merge(left, right, ascending);
    }

    private static List<Product> merge(List<Product> left, List<Product> right, boolean ascending) {
        List<Product> result = new ArrayList<>();
        int i = 0, j = 0;
        
        while (i < left.size() && j < right.size()) {
            int comparison = left.get(i).getName().compareToIgnoreCase(right.get(j).getName());
            boolean takeLeft = ascending ? comparison <= 0 : comparison >= 0;
            
            if (takeLeft) {
                result.add(left.get(i++));
            } else {
                result.add(right.get(j++));
            }
        }
        
        while (i < left.size()) result.add(left.get(i++));
        while (j < right.size()) result.add(right.get(j++));
        
        return result;
    }

    /**
     * Insertion Sort - efficient for small or nearly sorted datasets.
     * Time Complexity: O(n²) worst, O(n) best (nearly sorted)
     */
    public static void insertionSortByStock(List<Product> products, boolean ascending) {
        for (int i = 1; i < products.size(); i++) {
            Product key = products.get(i);
            int j = i - 1;
            
            while (j >= 0 && shouldSwapStock(products.get(j), key, ascending)) {
                products.set(j + 1, products.get(j));
                j--;
            }
            products.set(j + 1, key);
        }
    }

    private static boolean shouldSwapStock(Product a, Product b, boolean ascending) {
        return ascending 
            ? a.getStockQuantity() > b.getStockQuantity()
            : a.getStockQuantity() < b.getStockQuantity();
    }

    // ==========================================
    // SEARCHING ALGORITHMS
    // ==========================================

    /**
     * Binary Search for product by ID.
     * Requires the list to be sorted by product ID.
     * Time Complexity: O(log n)
     */
    public static Product binarySearchById(List<Product> sortedProducts, int targetId) {
        int left = 0;
        int right = sortedProducts.size() - 1;
        
        while (left <= right) {
            int mid = left + (right - left) / 2;
            int midId = sortedProducts.get(mid).getProductId();
            
            if (midId == targetId) {
                return sortedProducts.get(mid);
            } else if (midId < targetId) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        
        return null; // Not found
    }

    /**
     * Linear Search with early termination for sorted lists.
     * Time Complexity: O(n)
     */
    public static Product linearSearchByName(List<Product> products, String name) {
        for (Product product : products) {
            if (product.getName().equalsIgnoreCase(name)) {
                return product;
            }
        }
        return null;
    }

    /**
     * Hash-based lookup using a pre-built HashMap.
     * Time Complexity: O(1) average for lookup, O(n) for building map
     */
    public static Map<String, Product> buildNameHashMap(List<Product> products) {
        Map<String, Product> hashMap = new HashMap<>();
        for (Product product : products) {
            hashMap.put(product.getName().toLowerCase(), product);
        }
        return hashMap;
    }

    /**
     * Hash-based lookup by ID.
     * Time Complexity: O(1) average for lookup
     */
    public static Map<Integer, Product> buildIdHashMap(List<Product> products) {
        Map<Integer, Product> hashMap = new HashMap<>();
        for (Product product : products) {
            hashMap.put(product.getProductId(), product);
        }
        return hashMap;
    }

    // ==========================================
    // CACHING UTILITIES
    // ==========================================

    /**
     * LRU (Least Recently Used) Cache implementation.
     * Uses LinkedHashMap with access-order for O(1) operations.
     */
    public static class LRUCache<K, V> extends LinkedHashMap<K, V> {
        private final int maxSize;

        public LRUCache(int maxSize) {
            super(maxSize + 1, 1.0f, true); // accessOrder = true
            this.maxSize = maxSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > maxSize;
        }
    }

    /**
     * Creates an LRU cache with specified capacity.
     */
    public static <K, V> Map<K, V> createLRUCache(int capacity) {
        return Collections.synchronizedMap(new LRUCache<>(capacity));
    }

    // ==========================================
    // PERFORMANCE COMPARISON UTILITIES
    // ==========================================

    /**
     * Compares performance of different sorting algorithms.
     * Returns timing results in a map.
     */
    public static Map<String, Long> compareSortPerformance(List<Product> products) {
        Map<String, Long> results = new LinkedHashMap<>();
        PerformanceTimer timer = new PerformanceTimer();
        
        // Test QuickSort
        List<Product> quickSortList = new ArrayList<>(products);
        timer.start();
        quickSortByPrice(quickSortList, 0, quickSortList.size() - 1, true);
        results.put("QuickSort", timer.end());
        
        // Test MergeSort
        timer.start();
        mergeSortByName(new ArrayList<>(products), true);
        results.put("MergeSort", timer.end());
        
        // Test Java's TimSort (via Collections.sort)
        List<Product> timSortList = new ArrayList<>(products);
        timer.start();
        timSortList.sort(Comparator.comparingDouble(Product::getPrice));
        results.put("TimSort", timer.end());
        
        // Test InsertionSort (usually slower for large datasets)
        if (products.size() <= 1000) { // Skip for large datasets
            List<Product> insertionList = new ArrayList<>(products);
            timer.start();
            insertionSortByStock(insertionList, true);
            results.put("InsertionSort", timer.end());
        }
        
        return results;
    }

    /**
     * Compares performance of different search methods.
     */
    public static Map<String, Long> compareSearchPerformance(List<Product> products, int targetId, String targetName) {
        Map<String, Long> results = new LinkedHashMap<>();
        PerformanceTimer timer = new PerformanceTimer();
        
        // Linear Search
        timer.start();
        linearSearchByName(products, targetName);
        results.put("LinearSearch", timer.end());
        
        // Hash-based lookup (including map building time)
        timer.start();
        Map<String, Product> nameMap = buildNameHashMap(products);
        nameMap.get(targetName.toLowerCase());
        results.put("HashMapBuild+Lookup", timer.end());
        
        // Hash-based lookup (only lookup, assuming map is pre-built)
        timer.start();
        nameMap.get(targetName.toLowerCase());
        results.put("HashMapLookupOnly", timer.end());
        
        // Binary Search (requires sorted list)
        List<Product> sorted = new ArrayList<>(products);
        sorted.sort(Comparator.comparingInt(Product::getProductId));
        timer.start();
        binarySearchById(sorted, targetId);
        results.put("BinarySearch", timer.end());
        
        return results;
    }

    // ==========================================
    // UTILITY METHODS
    // ==========================================

    /**
     * Generates a hash code for a product based on multiple fields.
     * Useful for caching and comparison.
     */
    public static int generateProductHash(Product product) {
        return Objects.hash(
            product.getProductId(),
            product.getName(),
            product.getPrice(),
            product.getCategoryId()
        );
    }

    /**
     * Checks if a list is sorted by product ID.
     */
    public static boolean isSortedById(List<Product> products) {
        for (int i = 1; i < products.size(); i++) {
            if (products.get(i).getProductId() < products.get(i - 1).getProductId()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a list is sorted by price.
     */
    public static boolean isSortedByPrice(List<Product> products, boolean ascending) {
        for (int i = 1; i < products.size(); i++) {
            double current = products.get(i).getPrice();
            double previous = products.get(i - 1).getPrice();
            
            if (ascending && current < previous) return false;
            if (!ascending && current > previous) return false;
        }
        return true;
    }
}
