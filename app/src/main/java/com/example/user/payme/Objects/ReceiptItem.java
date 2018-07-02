package com.example.user.payme.Objects;

public class ReceiptItem {
    private String mName;
    private String mPrice;
    private boolean mIsShared;

    public ReceiptItem(String mName, String mPrice) {
        this.mName = mName;
        this.mPrice = mPrice;
        this.mIsShared = false;
    }

    public boolean getmIsShared() {
        return mIsShared;
    }

    public void setmIsShared(boolean mIsShared) {
        this.mIsShared = mIsShared;
    }

    public boolean toggleIsShared() {
        if (mIsShared) {
            this.mIsShared = false;
        } else {
            this.mIsShared = true;
        }

        return mIsShared;
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
