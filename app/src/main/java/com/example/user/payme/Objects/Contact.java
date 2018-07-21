package com.example.user.payme.Objects;

import java.io.Serializable;

/**
 * Contact class to contain the following contact information:
 *  - Name
 *  - Profile Image Drawable
 *  - Phone Number (From Phone Directory)
 */
public class Contact implements Serializable {
    private int mImageDrawable;
    private String mName;
    private String mPhoneNumber;
    private boolean isSelected;

    public Contact() {
        // Default constructor needed
    }
    
    public Contact(int mImageDrawable, String mName, String mPhoneNumber) {
        this.mImageDrawable = mImageDrawable;
        this.mName = mName;
        this.mPhoneNumber = mPhoneNumber;
    }

    public boolean getIsSelected() { return isSelected; }

    public void toggleSelected() {
        this.isSelected = !isSelected;
    }

    public int getmImageDrawable() { return this.mImageDrawable; }
    public void setmImageDrawable(int mImageDrawable) { this.mImageDrawable = mImageDrawable; }

    public String getmName() { return this.mName; }
    public void setmName(String mName) { this.mName = mName; }

    public String getmPhoneNumber() { return this.mPhoneNumber; }
    public void setmPhoneNumber(String mPhoneNumber) { this.mPhoneNumber = mPhoneNumber.trim(); }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Contact)) {
            return false;
        }

        Contact con = (Contact) o;
        return con.mName.equals(mName);
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31 * hash + mName.hashCode();
        return hash;
    }
}
