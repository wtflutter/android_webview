package com.wttec.android_webview.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Date:       2019-04-26
 * Author:     Su Xing
 * Describe:
 */
public class ContactsUtil {

    public static List<ContactsBean> getContacts(Context context) {
        List<ContactsBean> contactsBeans = new ArrayList<>();
        //联系人的Uri，也就是content://com.android.contacts/contacts
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        //指定获取_id和display_name两列数据，display_name即为姓名
        String[] projection = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.LAST_TIME_CONTACTED,
                ContactsContract.Contacts.TIMES_CONTACTED
        };
        //根据Uri查询相应的ContentProvider，cursor为获取到的数据集
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                ContactsBean contactsBean = new ContactsBean();
                Long id = cursor.getLong(0);
                String name = cursor.getString(1);
                long lastContactTime = cursor.getLong(2);
                int contactTimes = cursor.getInt(3);
                contactsBean.id = id;
                contactsBean.name = name;
                contactsBean.lastContactTime = lastContactTime;
                contactsBean.contactTimes = contactTimes;
                //指定获取NUMBER这一列数据
                String[] phoneProjection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
                //根据联系人的ID获取此人的电话号码
                Cursor phonesCursor = context.getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        phoneProjection,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id,
                        null,
                        null);
                if (phonesCursor != null) {
                    contactsBean.numList = new ArrayList<>();
                    //因为每个联系人可能有多个电话号码，所以需要遍历
                    if (phonesCursor.moveToFirst()) {
                        do {
                            String num = phonesCursor.getString(0);
                            contactsBean.numList.add(num);
                        } while (phonesCursor.moveToNext());
                    }
                    phonesCursor.close();
                    contactsBeans.add(contactsBean);
                }

            } while (cursor.moveToNext());
            cursor.close();
        }
        return contactsBeans;
    }
}
