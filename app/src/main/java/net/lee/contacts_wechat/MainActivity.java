package net.lee.contacts_wechat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    private Button btn_add;
    private PinnedSectionListView lv_contacts;
    private ContactAdapter adapter;
    private List<ContactBean> contacts;

    private LetterBar lb;
    private TextView tv_overLay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

    }

    private void initView() {

        btn_add = (Button) findViewById(R.id.btn_add);
        lv_contacts = (PinnedSectionListView) findViewById(R.id.lv_contacts);
        //ȥ�����Դ�����ӰЧ��
        lv_contacts.setShadowVisible(false);

        contacts = new ArrayList<ContactBean>();
        adapter = new ContactAdapter(this, contacts);
        lv_contacts.setAdapter(adapter);

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAddDialog();
            }
        });

        lv_contacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = adapter.getItem(position);
                if (item instanceof ContactBean) {
                    showUpdateDialog((ContactBean) item);
                }
            }
        });

        lv_contacts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                Object item = adapter.getItem(position);
                if (item instanceof ContactBean) {
                    showLongClickDialog((ContactBean) item);
                }
                return true;
            }
        });

        //��LetterBar�йصĲ���
        lb = (LetterBar) findViewById(R.id.lb);
        tv_overLay = (TextView) findViewById(R.id.tv_overlay);

        lb.setOnLetterSelectedListener(new LetterBar.OnLetterSelectedListener() {
            @Override
            public void onLetterSelected(String letter) {

                if (TextUtils.isEmpty(letter))
                {
                    tv_overLay.setVisibility(View.GONE);
                }
                else
                {
                    tv_overLay.setText(letter);
                    tv_overLay.setVisibility(View.VISIBLE);
                    //��ȡ��ǰ���LetterBar��¶��������ĸ���жϸ���ĸ�Ƿ���ڣ�������ڱ���ת
                    int index = adapter.getLetterPosition(letter);
                    if(index != -1)
                    {
                        lv_contacts.setSelection(index);
                    }

                }

            }
        });

        setContactsData();

    }

    /**
     * ��ʾ��ѡ���Dialog
     */
    private void showLongClickDialog(final ContactBean contactBean) {

        new AlertDialog.Builder(this)
                .setItems(new String[]{"����绰","���Ͷ���","ɾ��"},
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                switch (which)
                                {
                                    case 0:

                                        //�ǵ����Ȩ��
                                        //��ת������绰���棬����һ���̶�д������ס����
                                        Intent intentCall = new Intent();
                                        intentCall.setAction(Intent.ACTION_CALL);
                                        intentCall.setData(Uri.parse("tel:" + contactBean.getPhone()));
                                        startActivity(intentCall);
                                        break;
                                    case 1:
                                        //��ת�����Ͷ��Ž��棬����һ���̶�д������ס����
                                        Intent intentSend = new Intent();
                                        intentSend.setAction(Intent.ACTION_SENDTO);
                                        intentSend.setData(Uri.parse("smsto://" + contactBean.getPhone()));
                                        startActivity(intentSend);

                                        break;
                                    case 2:

                                        ContactManager.deleteContact(MainActivity.this,contactBean);
                                        setContactsData();
                                        break;
                                    default:
                                        break;
                                }
                            }
                        })
                .show();
    }

    private void setContactsData() {
        contacts.clear();
        contacts.addAll(ContactManager.getContacts(this));
        adapter.updateList();
        System.out.println(contacts);
    }

    private void showAddDialog() {

        //������Ҫһ��������дname��phone�Ĳ����ļ�
        View view = View.inflate(this, R.layout.dialog_contact, null);

        final EditText et_name = (EditText) view.findViewById(R.id.et_name);
        final EditText et_phone = (EditText) view.findViewById(R.id.et_phone);

        new AlertDialog.Builder(this)
                .setTitle("Add Contacts")
                .setView(view)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ContactBean contactBean = new ContactBean();
                        contactBean.setName(et_name.getText().toString());
                        contactBean.setPhone(et_phone.getText().toString());

                        ContactManager.addContact(MainActivity.this, contactBean);
                        setContactsData();
                    }
                })
                .setNegativeButton("Cancle", null)
                .show();
    }

    private void showUpdateDialog(final ContactBean oldContact) {

        //������Ҫһ��������дname��phone�Ĳ����ļ�
        View view = View.inflate(this, R.layout.dialog_contact, null);

        final EditText et_name = (EditText) view.findViewById(R.id.et_name);
        final EditText et_phone = (EditText) view.findViewById(R.id.et_phone);

        et_name.setText(oldContact.getName());
        et_phone.setText(oldContact.getPhone());

        new AlertDialog.Builder(this)
                .setTitle("Update Contacts")
                .setView(view)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ContactBean contactBean = new ContactBean();
                        contactBean.setRawContactId(oldContact.getRawContactId());
                        contactBean.setName(et_name.getText().toString());
                        contactBean.setPhone(et_phone.getText().toString());

                        ContactManager.updateContact(MainActivity.this, contactBean);
                        setContactsData();
                    }
                })
                .setNegativeButton("Cancle", null)
                .show();
    }

}
