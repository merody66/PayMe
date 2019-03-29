package com.example.user.payme.Objects;

public class Contact {
    private int mImageDrawable;
    private String mName;
    private String mPhoneNumber;

    public Contact(int mImageDrawable, String mName, String mPhoneNumber) {
        this.mImageDrawable = mImageDrawable;
        this.mName = mName;
        this.mPhoneNumber = mPhoneNumber;
    }

    public int getmImageDrawable() { return this.mImageDrawable; }
    public void setmImageDrawable(int mImageDrawable) { this.mImageDrawable = mImageDrawable; }

    public String getmName() { return this.mName; }
    public void setmName(String mName) { this.mName = mName; }

    public String getmPhoneNumber() { return this.mPhoneNumber; }
    public void setmPhoneNumber(String mPhoneNumber) { this.mPhoneNumber = mPhoneNumber; }
}
