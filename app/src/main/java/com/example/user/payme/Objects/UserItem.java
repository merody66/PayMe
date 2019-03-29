package com.example.user.payme.Objects;

import java.util.ArrayList;

public class UserItem {
    private Contact mContact;
    private ArrayList<ReceiptItem> receiptItems;
    private String total;

    // Firebase var
    private String mName;
    private String mNumber;
    private double mAmount;
    private boolean mIsPaid;

    public UserItem() {
        // Default constructor needed
    }

    public UserItem(Contact mContact, ArrayList<ReceiptItem> receiptItems, String total) {
        this.mContact = mContact;
        this.receiptItems = receiptItems;
        this.total = total;
    }

    // For firebase constructor and name
    public UserItem(String mName, String mNumber, double mAmount, boolean mIsPaid) {
        this.mName = mName;
        this.mNumber = mNumber;
        this.mAmount = mAmount;
        this.mIsPaid = mIsPaid;
    }

    public String getmName() {
        return mName;
    }

    public String getmNumber() {
        return mNumber;
    }

    public double getmAmount() {
        return mAmount;
    }

    public boolean ismIsPaid() {
        return mIsPaid;
    }

    public Contact getmContact() {
        return mContact;
    }

    public void setmContact(Contact mContact) {
        this.mContact = mContact;
    }

    public ArrayList<ReceiptItem> getReceiptItems() {
        return receiptItems;
    }

    public void setReceiptItems(ArrayList<ReceiptItem> receiptItems) {
        this.receiptItems = receiptItems;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }
}
