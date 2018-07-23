package com.example.user.payme.Objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * User class to contain the following user information:
 *  - Name
 *  - Profile Image Link
 *  - Email Address
 *  - PayLah! Number
 *  - Notification Setting
 *  - Group List (i.e. Friends, School)
 */
public class User {
    private String name;
    private String profileURL;
    private String email;
    private String number;
    private Boolean notificationSetting;
    private HashMap<String, ArrayList<Contact>> groupList;

    public User() {
        // Default constructor needed
    }

    public User(String name, String number, String email) {
        this.name = name;
        this.email = email;
        this.number = number;
    }

    // Getters & Setters
    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return this.email; }
    public void setEmail(String email) { this.email = email; }

    public String getNumber() { return this.number; }
    public void setNumber(String number) { this.number = number; }

    public String getProfileURL() { return this.profileURL; }
    public void setProfileURL(String URL) { this.profileURL = URL; }

    public Boolean getNotificationSetting() { return this.notificationSetting; }
    public void setNotificationSetting(Boolean bool) { this.notificationSetting = bool; }

    public HashMap<String, ArrayList<Contact>> getGroupList() { return this.groupList; }
    public void setGroupList(HashMap<String, ArrayList<Contact>> groupList) { this.groupList = groupList; }


    public void addGroup(String grpName, ArrayList<Contact> contacts) {
        if (this.groupList == null) {
            this.groupList = new HashMap<>();
        }
        if (!groupList.containsKey(grpName)) {
            this.groupList.put(grpName, contacts);
        }
    }
    
    public void getGroupNames() {
        Iterator iterator = groupList.keySet().iterator();

        while (iterator.hasNext()) {
            String key = iterator.next().toString();
            System.out.println(key);
        }
    }

}
