package com.example.user.payme.Objects;

import java.io.Serializable;

public class ReceiptItem implements Serializable {
    private String mName;
    private String mPrice;
    private String mBelongsTo;
    private boolean mIsShared;

    public ReceiptItem(String mName, String mPrice) {
        this.mName = mName;
        this.mPrice = mPrice;
        this.mBelongsTo = null;
        this.mIsShared = true;
    }

    public String getmBelongsTo() {
        return mBelongsTo;
    }

    public void setmBelongsTo(String mBelongsTo) {
        this.mBelongsTo = mBelongsTo;
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
