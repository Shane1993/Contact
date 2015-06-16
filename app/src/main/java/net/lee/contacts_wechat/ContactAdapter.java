package net.lee.contacts_wechat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by LEE on 2015/6/16.
 */
public class ContactAdapter extends BaseAdapter implements PinnedSectionListView.PinnedSectionListAdapter{

    private Context context;
    private List<ContactBean> contacts;

    //用来存储所有的数据，一共有两种，一种是联系人，一种是首字母
    private List<Object> datas;
    //用来存储首字母，键是字母，值是该字母在List中的位置
    private Map<String, Integer> letterPosition;

    //设置两种数据类型常量
    private final static int VIEW_TYPE_CONTACT = 0;
    private final static int VIEW_TYPE_LETTER = 1;

    //初始化列表
    public void initList() {
        datas = new ArrayList<Object>();
        letterPosition = new HashMap<String, Integer>();

        Collections.sort(contacts, new Comparator<ContactBean>() {
            @Override
            public int compare(ContactBean lhs, ContactBean rhs) {

                String lhsName = PinyinUtils.getPingYin(lhs.getName()).toUpperCase();
                String rhsName = PinyinUtils.getPingYin(rhs.getName()).toUpperCase();

                return lhsName.compareTo(rhsName);
            }
        });

        for (int i = 0; i < contacts.size(); i++) {

            ContactBean contactBean = contacts.get(i);
            String firstLetter = getFirstLetter(contactBean.getName());
            //这里巧妙的利用datas.size()作为首字母第一次出现的位置，方便以后进行位置跳转
            if(!letterPosition.containsKey(firstLetter))
            {
                letterPosition.put(firstLetter, datas.size());
                datas.add(firstLetter);
            }

            datas.add(contactBean);
        }

    }

    /**
     * 获取首字母
     *
     * @param str
     * @return
     */
    public static String getFirstLetter(String str) {
        String firstLetter = "";
        char c = PinyinUtils.getPingYin(str).toUpperCase().charAt(0);

        //注意这里是用大写字母做范围，所以上面的字符串要使用toUpperCase()
        if(c >= 'A' && c <= 'Z')
        {
            firstLetter = String.valueOf(c);
        }

        return firstLetter;
    }

    /**
     * 刷新列表
     */
    public void updateList() {
        initList();
        notifyDataSetChanged();
    }

    /**
     * 获取某首字母在datas列表的位置以提供跳转
     * @param letter
     * @return
     */
    public int getLetterPosition(String letter)
    {
        Integer position = letterPosition.get(letter);
        return position == null ? -1 : position;
    }

    public ContactAdapter(Context context, List<ContactBean> contacts) {
        this.context = context;
        this.contacts = contacts;

        initList();
    }

    //声明List种共有2种数据
    @Override
    public int getViewTypeCount() {

        return 2;
    }

    //获取List中当前条目的数据类型
    @Override
    public int getItemViewType(int position) {
        return (datas.get(position) instanceof ContactBean) ? VIEW_TYPE_CONTACT : VIEW_TYPE_LETTER;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        return (getItemViewType(position) == VIEW_TYPE_CONTACT) ?
                getContactView(position, convertView) :
                getLetterView(position, convertView);
    }

    private View getContactView(int position, View convertView) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_contact, null);
            viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            viewHolder.tv_phone = (TextView) convertView.findViewById(R.id.tv_phone);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();

        }

        ContactBean contactBean = (ContactBean) getItem(position);
        viewHolder.tv_name.setText(contactBean.getName());
        viewHolder.tv_phone.setText(contactBean.getPhone());

        return convertView;
    }

    private View getLetterView(int position, View convertView) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_letter, null);
            viewHolder.tv_letter = (TextView) convertView.findViewById(R.id.tv_letter);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();

        }

        String letter = (String) getItem(position);
        viewHolder.tv_letter.setText(letter);

        return convertView;
    }

    //设置需要变为悬浮栏的数据类型（这里是首字母的数据变为悬浮栏）
    @Override
    public boolean isItemViewTypePinned(int viewType) {
        return viewType == VIEW_TYPE_LETTER;
    }

    static class ViewHolder {
        TextView tv_letter;
        TextView tv_name;
        TextView tv_phone;
    }
}
