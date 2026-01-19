package com.amalitech.smartecommerce.models;

public class Product {
    private Integer productId;      
    private Integer categoryId;    
    private String name;
    private Double price;           
    private Integer stockQuantity;  

    public Product() {}

    public Product(Integer productId, Integer categoryId, String name, Double price, Integer stockQuantity) {
        this.productId = productId;
        this.categoryId = categoryId;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public Product(Integer categoryId, String name, Double price, Integer stockQuantity) {
        this.categoryId = categoryId;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    @Override
    public String toString() {
        return "Product{id=" + productId + ", name='" + name + "', price=" + price + ", stock=" + stockQuantity + "}";
    }
}