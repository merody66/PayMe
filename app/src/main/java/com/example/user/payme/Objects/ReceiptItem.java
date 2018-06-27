package com.example.user.payme.Objects;

public class ReceiptItem {
    private String mName;
    private String mPrice;

    public ReceiptItem(String mName, String mPrice) {
        this.mName = mName;
        this.mPrice = mPrice;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmPrice() {
        return mPrice;
    }

    public void setmPrice(String mPrice) {
        this.mPrice = mPrice;
    }
}
