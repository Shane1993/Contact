package net.lee.contacts_wechat;

/**
 * Created by LEE on 2015/6/15.
 */
public class ContactBean  {

    private String name;
    private String phone;
    private long rawContactId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public long getRawContactId() {
        return rawContactId;
    }

    public void setRawContactId(long rawContactId) {
        this.rawContactId = rawContactId;
    }
}
