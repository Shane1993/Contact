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
         * ���²���Ϊ��ȡ��ϵ�˵��������ݣ����岽���ǣ�
         * 1.��ȡ������ϵ��
         * 2.����resolver�õ�
         * 3.����resolverͨ��_ID����ѯ������ϵ�˶�Ӧ�Ķ��Data��Ϣ
         */
        ContentResolver resolver = context.getContentResolver();
        /**
         * ��ȡÿ����ϵ�˵�RawContact._ID
         * �ȵõ�RawContact��ָ�룬�����ֱ��ǣ�
         * 1.·�����϶�Ҫ�����������������Ҷ����������������RawContacts��·����Ҳ���ǻ���˸�·���µ����ݱ�
         * 2.�ֶ����ƣ���Ϊĳ·�����кܶ����ݣ��൱��һ�����ݱ��������ȫ������������null,���ֻ�����øñ��µ�
         *              ĳһ�ֶ�����д���ֶε����ƣ���������ֻ�����ȡRawContacts���е�_ID�ֶΣ�
         * 3.�������޶����������������ƣ�����SQL�е�where�����˼һ������ȡ�ֶ����ض����������ݣ��������ֵ�ɵ�4������������
         * 4.��������������������ֵ��
         * 5.��������
         */
        Cursor cRawContact = resolver.query(ContactsContract.RawContacts.CONTENT_URI,
                new String[]{ContactsContract.RawContacts._ID},
                null,
                null,
                null);

        ContactBean contactBean;
        //��ѯ���е�_ID����ֶ��µ��������ݣ�ע����ֶ��µ����ݻ���һ�����ݱ�����Ҫ�õ���������ݻ���һ��cursor
        while (cRawContact.moveToNext()) {
            contactBean = new ContactBean();

            long rawContactId = cRawContact.getLong(
                    cRawContact.getColumnIndex(ContactsContract.RawContacts._ID));
            contactBean.setRawContactId(rawContactId);

            /**
             * ��ε�cursor��ָ��ÿ��RAW_CONTACT_ID����Ӧ����ϵ�˵Ķ�����ݵ�ָ��
             */
            Cursor cData = resolver.query(ContactsContract.Data.CONTENT_URI,
                    null,
                    ContactsContract.Data.RAW_CONTACT_ID + "=?",
                    new String[]{String.valueOf(rawContactId)},
                    null);
            /**
             *  ��ѯ��ȡ��������
             *  ��Ϊ�������ݷ�װ��DATA1���棬����Ҫ�Ȼ�ȡ��DATA1
             *      ��ͨ��MIMETYPE�жϵ�ǰ������ʲô���ʹӶ���Ӳ�һ��������
             *  ����ֻ������������ݣ����ƣ�StructuredName.CONTENT_ITEM_TYPE)���͵绰���루Phone.CONTENT_ITEM_TYPE)��
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
     *  �����ϵ�ˣ�����ͨ��resolver�����
     *  ע��һ�����ݵľ���������ͨ��һ��ContentValues��������ӵ�
     *  ÿ��ContentValues����һ�����id���������ͣ����ݾ������ݣ���������ҵ�˳�����Ӧ��id->����->���ݣ���
     * @param context
     * @param contactBean
     */
    public static void addContact(Context context, ContactBean contactBean)
    {
        ContentResolver resolver = context.getContentResolver();

        ContentValues contentValues = new ContentValues();
        //���ú�·������������ݵ�ȻҪ��ָ����λ�ã�
        Uri contactUri = resolver.insert(ContactsContract.RawContacts.CONTENT_URI, contentValues);
        //Ϊ��λ������Id(ͨ��id�Ҳ��ҵõ���λ��ѽ)
        long rawContactId = ContentUris.parseId(contactUri);

        //�������
        ContentValues contentValues1 = new ContentValues();
        contentValues1.put(ContactsContract.Data.RAW_CONTACT_ID,rawContactId);
        contentValues1.put(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        contentValues1.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,contactBean.getName());
        resolver.insert(ContactsContract.Data.CONTENT_URI,contentValues1);

        //��ӵ绰
        ContentValues contentValues2 = new ContentValues();
        contentValues2.put(ContactsContract.Data.RAW_CONTACT_ID,rawContactId);
        contentValues2.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        contentValues2.put(ContactsContract.CommonDataKinds.Phone.NUMBER,contactBean.getPhone());
        resolver.insert(ContactsContract.Data.CONTENT_URI, contentValues2);

    }

    /**
     * �÷��������ڸ���ĳ���ض���ϵ�˵�
     *  �����������add�Ĳ�ֻ࣬����ͨ��resolver��applyBatch�����������и������ݵ�
     *  ��ǰ�����ƣ�����һ�����ݶ���Ҫһ��applyBatch
     *
     * @param context
     * @param contactBean
     */
    public static void updateContact(Context context, ContactBean contactBean)
    {

        ContentResolver resolver = context.getContentResolver();

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        //withSelection�еĲ�����ǰ�����ʱ�Ĳ������Ӧ
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
     * �÷�������ɾ��ָ������ϵ�ˣ�ֻ��Ҫͨ��id���ɾ��
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
