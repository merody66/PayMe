package com.example.user.payme.Objects;

import java.util.ArrayList;

public class UserItem {
    private Contact mContact;
    private ArrayList<ReceiptItem> receiptItems;
    private String total;

    public UserItem(Contact mContact, ArrayList<ReceiptItem> receiptItems, String total) {
        this.mContact = mContact;
        this.receiptItems = receiptItems;
        this.total = total;
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
