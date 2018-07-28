package com.example.user.payme.Objects;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Receipt implements Serializable, Comparable<Receipt> {
    private ArrayList<ReceiptItem> mItemList = new ArrayList<>();
    private String mShopname;
    private String mDate;
    private String mGstAmt;
    private String mServiceChargeAmt;
    private String mSubtotalAmt;
    private ArrayList<UserItem> payees;

    public Receipt() {
        // Default constructor needed
    }

    public Receipt(String mShopname, String mDate, String mGstAmt, String mServiceChargeAmt, String mSubtotalAmt, ArrayList<ReceiptItem> menuList) {
        this.mShopname = mShopname;
        this.mDate = mDate;
        this.mGstAmt = mGstAmt;
        this.mServiceChargeAmt = mServiceChargeAmt;
        this.mSubtotalAmt = mSubtotalAmt;
        this.mItemList = menuList;
    }

    public Receipt(ArrayList<UserItem> payees) {
        super();
    }

    public void setPayees(ArrayList<UserItem> payees) {
        this.payees = payees;
    }

    public ArrayList<UserItem> getPayees() {
        return payees;
    }

    public ArrayList<ReceiptItem> getmItemList() {
        return mItemList;
    }
    public void setmItemList(ArrayList<ReceiptItem> mItemList) {
        this.mItemList = mItemList;
    }

    public String getmShopname() { return mShopname; }
    public void setmShopname(String mShopname) { this.mShopname = mShopname; }

    public String getmDate() { return mDate; }
    public void setmDate(String mDate) { this.mDate = mDate; }

    public String getmGstAmt() { return mGstAmt; }
    public void setmGstAmt(String mGstAmt) { this.mGstAmt = mGstAmt; }

    public String getmServiceChargeAmt() { return mServiceChargeAmt; }
    public void setmServiceChargeAmt(String mServiceChargeAmt) { this.mServiceChargeAmt = mServiceChargeAmt; }

    public String getmSubtotalAmt() { return mSubtotalAmt; }
    public void setmSubtotalAmt(String mSubtotalAmt) {
        this.mSubtotalAmt = mSubtotalAmt;
    }

    @Override
    public int compareTo(@NonNull Receipt o) {
        Date date1 = null;
        Date date2 = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            date1 = dateFormat.parse(this.mDate);
            date2 = dateFormat.parse(o.mDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date1.compareTo(date2);
    }
}
