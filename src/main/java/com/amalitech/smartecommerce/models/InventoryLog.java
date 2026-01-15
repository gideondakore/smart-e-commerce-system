package com.amalitech.smartecommerce.models;

import java.time.LocalDateTime;

/**
 * InventoryLog model for tracking stock changes.
 * Useful for auditing and can be exported to NoSQL for log analysis.
 */
public class InventoryLog {
    private int logId;
    private int productId;
    private int changeAmount;
    private int previousQuantity;
    private int newQuantity;
    private ChangeType changeType;
    private LocalDateTime changeDate;
    private String reason;
    private Integer performedBy;

    // Transient fields for display
    private String productName;
    private String performedByName;

    public enum ChangeType {
        RESTOCK("restock"),
        SALE("sale"),
        ADJUSTMENT("adjustment"),
        RETURN("return"),
        DAMAGED("damaged");

        private final String value;

        ChangeType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static ChangeType fromString(String text) {
            for (ChangeType type : ChangeType.values()) {
                if (type.value.equalsIgnoreCase(text)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown change type: " + text);
        }
    }

    public InventoryLog() {
        this.changeDate = LocalDateTime.now();
    }

    public InventoryLog(int productId, int changeAmount, int previousQuantity, 
                        int newQuantity, ChangeType changeType, String reason, Integer performedBy) {
        this();
        this.productId = productId;
        this.changeAmount = changeAmount;
        this.previousQuantity = previousQuantity;
        this.newQuantity = newQuantity;
        this.changeType = changeType;
        this.reason = reason;
        this.performedBy = performedBy;
    }

    public InventoryLog(int logId, int productId, int changeAmount, int previousQuantity,
                        int newQuantity, ChangeType changeType, LocalDateTime changeDate,
                        String reason, Integer performedBy) {
        this.logId = logId;
        this.productId = productId;
        this.changeAmount = changeAmount;
        this.previousQuantity = previousQuantity;
        this.newQuantity = newQuantity;
        this.changeType = changeType;
        this.changeDate = changeDate;
        this.reason = reason;
        this.performedBy = performedBy;
    }

    // Getters and Setters
    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(int changeAmount) {
        this.changeAmount = changeAmount;
    }

    public int getPreviousQuantity() {
        return previousQuantity;
    }

    public void setPreviousQuantity(int previousQuantity) {
        this.previousQuantity = previousQuantity;
    }

    public int getNewQuantity() {
        return newQuantity;
    }

    public void setNewQuantity(int newQuantity) {
        this.newQuantity = newQuantity;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

    public LocalDateTime getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(LocalDateTime changeDate) {
        this.changeDate = changeDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(Integer performedBy) {
        this.performedBy = performedBy;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getPerformedByName() {
        return performedByName;
    }

    public void setPerformedByName(String performedByName) {
        this.performedByName = performedByName;
    }

    @Override
    public String toString() {
        return "InventoryLog{" +
                "logId=" + logId +
                ", productId=" + productId +
                ", changeAmount=" + changeAmount +
                ", changeType=" + changeType +
                ", changeDate=" + changeDate +
                ", reason='" + reason + '\'' +
                '}';
    }
}
