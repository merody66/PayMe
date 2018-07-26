package com.example.user.payme.Objects;

public class Payment {
    private Double mAmount;
    private String mName;
    private String mNumber;
    private String mReceiptID;
    private String mStatus;
    private String mType;

    public Payment() {
        // Default constructor needed
    }

    public Payment(Double mAmount, String mName, String mNumber, String mReceiptID, String mStatus, String mType) {
        this.mAmount = mAmount;
        this.mName = mName;
        this.mNumber = mNumber;
        this.mReceiptID = mReceiptID;
        this.mStatus = mStatus;
        this.mType = mType;
    }

    // Getters & Setters
    public Double getmAmount() { return this.mAmount; }
    public void setmAmount(Double mAmount) { this.mAmount = mAmount; }

    public String getmName() { return this.mName; }
    public void setmName(String mName) { this.mName = mName; }

    public String getmNumber() { return this.mNumber; }
    public void setmNumber(String mNumber) { this.mNumber = mNumber; }

    public String getmReceiptID() { return this.mReceiptID; }
    public void setmReceiptID(String mReceiptID) { this.mReceiptID = mReceiptID; }

    public String getmStatus() { return this.mStatus; }
    public void setmStatus(String mStatus) { this.mStatus = mStatus; }

    public String getmType() { return this.mType; }
    public void setmType(String mType) { this.mType = mType; }
}
