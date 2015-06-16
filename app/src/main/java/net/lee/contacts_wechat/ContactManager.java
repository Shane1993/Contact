package net.lee.contacts_wechat;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LEE on 2015/6/15.
 */
public class ContactManager {


    public static List<ContactBean> getContacts(Context context) {
        List<ContactBean> contacts = new ArrayList<ContactBean>();

        /**
         * 以下部分为获取联系人的所有数据，具体步骤是：
         * 1.获取所有联系人
         * 2.利用resolver得到
         * 3.利用resolver通过_ID来查询到该联系人对应的多个Data信息
         */
        ContentResolver resolver = context.getContentResolver();
        /**
         * 获取每个联系人的RawContact._ID
         * 先得到RawContact的指针，参数分别是：
         * 1.路径（肯定要先声明你想在哪里找东西啦，这里填的是RawContacts的路径，也就是获得了该路径下的数据表）
         * 2.字段名称（因为某路径下有很多内容，相当于一个数据表，如果想获得全部的内容则填null,如果只是想获得该表下的
         *              某一字段则填写该字段的名称，比如这里只是想获取RawContacts表中的_ID字段）
         * 3.搜索（限定）条件（条件名称）（跟SQL中的where语句意思一样，获取字段下特定条件的数据，具体的数值由第4个参数决定）
         * 4.搜索条件（条件具体数值）
         * 5.排序条件
         */
        Cursor cRawContact = resolver.query(ContactsContract.RawContacts.CONTENT_URI,
                new String[]{ContactsContract.RawContacts._ID},
                null,
                null,
                null);

        ContactBean contactBean;
        //轮询表中的_ID这个字段下的所有数据，注意该字段下的数据还是一个数据表，所以要得到具体的数据还需一个cursor
        while (cRawContact.moveToNext()) {
            contactBean = new ContactBean();

            long rawContactId = cRawContact.getLong(
                    cRawContact.getColumnIndex(ContactsContract.RawContacts._ID));
            contactBean.setRawContactId(rawContactId);

            /**
             * 这次的cursor是指向每个RAW_CONTACT_ID所对应的联系人的多个数据的指针
             */
            Cursor cData = resolver.query(ContactsContract.Data.CONTENT_URI,
                    null,
                    ContactsContract.Data.RAW_CONTACT_ID + "=?",
                    new String[]{String.valueOf(rawContactId)},
                    null);
            /**
             *  轮询提取具体数据
             *  因为所有数据封装在DATA1里面，所以要先获取到DATA1
             *      并通过MIMETYPE判断当前数据是什么类型从而添加不一样的数据
             *  这里只添加了两种数据，名称（StructuredName.CONTENT_ITEM_TYPE)）和电话号码（Phone.CONTENT_ITEM_TYPE)）
             */
            while (cData.moveToNext()) {
                String data = cData.getString(cData.getColumnIndex(ContactsContract.Data.DATA1));
                String mimetype = cData.getString(cData.getColumnIndex(ContactsContract.Data.MIMETYPE));

                if (mimetype.equals(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)) {
                    contactBean.setName(data);
                } else if (mimetype.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
                    contactBean.setPhone(data);
                }
            }

            contacts.add(contactBean);
            cData.close();
        }

        cRawContact.close();
        return contacts;
    }

    /**
     *  添加联系人，还是通过resolver来添加
     *  注意一种数据的具体内容是通过一个ContentValues对象来添加的
     *  每个ContentValues对象一般包括id，内容类型，内容具体数据（跟上面查找的顺序相对应（id->类型->数据））
     * @param context
     * @param contactBean
     */
    public static void addContact(Context context, ContactBean contactBean)
    {
        ContentResolver resolver = context.getContentResolver();

        ContentValues contentValues = new ContentValues();
        //设置好路径（添加新数据当然要先指定好位置）
        Uri contactUri = resolver.insert(ContactsContract.RawContacts.CONTENT_URI, contentValues);
        //为该位置设置Id(通过id我才找得到该位置呀)
        long rawContactId = ContentUris.parseId(contactUri);

        //添加名称
        ContentValues contentValues1 = new ContentValues();
        contentValues1.put(ContactsContract.Data.RAW_CONTACT_ID,rawContactId);
        contentValues1.put(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        contentValues1.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,contactBean.getName());
        resolver.insert(ContactsContract.Data.CONTENT_URI,contentValues1);

        //添加电话
        ContentValues contentValues2 = new ContentValues();
        contentValues2.put(ContactsContract.Data.RAW_CONTACT_ID,rawContactId);
        contentValues2.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        contentValues2.put(ContactsContract.CommonDataKinds.Phone.NUMBER,contactBean.getPhone());
        resolver.insert(ContactsContract.Data.CONTENT_URI, contentValues2);

    }

    /**
     * 该方法是用于更新某条特定联系人的
     *  步骤与上面的add的差不多，只是是通过resolver的applyBatch批处理方法进行更新数据的
     *  与前面类似，更新一项数据都需要一此applyBatch
     *
     * @param context
     * @param contactBean
     */
    public static void updateContact(Context context, ContactBean contactBean)
    {

        ContentResolver resolver = context.getContentResolver();

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        //withSelection中的参数跟前面添加时的参数相对应
        ops.add(ContentProviderOperation
                .newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(ContactsContract.Data.RAW_CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + "=? ",
                        new String[]{String.valueOf(contactBean.getRawContactId()), ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE})
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                        contactBean.getName())
                .build());

        ops.add(ContentProviderOperation
                        .newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(ContactsContract.Data.RAW_CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + "=? ",
                                new String[]{String.valueOf(contactBean.getRawContactId()), ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE})
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
                                contactBean.getPhone())
                        .build());
        try {
            resolver.applyBatch(ContactsContract.AUTHORITY,ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    /**
     * 该方法用于删除指定的联系人，只需要通过id便可删除
     * @param context
     * @param contactBean
     */
    public static void deleteContact(Context context, ContactBean contactBean)
    {
        ContentResolver resolver = context.getContentResolver();
        resolver.delete(ContactsContract.RawContacts.CONTENT_URI,
                ContactsContract.RawContacts._ID + "=?",
                new String[]{String.valueOf(contactBean.getRawContactId())});
    }
}
