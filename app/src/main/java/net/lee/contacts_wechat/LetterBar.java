package net.lee.contacts_wechat;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by LEE on 2015/6/16.
 */
public class LetterBar extends LinearLayout {

    public LetterBar(Context context) {
        super(context);
        init(context);
    }

    public LetterBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);

    }

    public LetterBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);

    }

    private void init(Context context) {

        setBackgroundColor(Color.GRAY);
        setAlpha(0.7f);
        setOrientation(VERTICAL);

        for (int i = 0; i < 26; i++) {
            TextView tv = new TextView(context);
            ViewGroup.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0, 1);
            tv.setLayoutParams(lp);

            tv.setText((char)('A' + i) + "");
            tv.setTextColor(Color.WHITE);

            addView(tv);
        }

    }

    private OnLetterSelectedListener listener;

    public void setOnLetterSelectedListener(OnLetterSelectedListener listener)
    {
        this.listener = listener;
    }

    /**
     * 该接口的功能是将当前选择的字母暴露出去
     */
    public interface OnLetterSelectedListener
    {
        void onLetterSelected(String letter);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:

                //每个TextView的高度
                int defSize = getHeight() / getChildCount();
                //当前触摸的位置
                float y = event.getY();
                //当前索引的位置
                int index = (int) (y / defSize);
                //当前字母
                TextView textView = (TextView) getChildAt(index);

                if(textView != null && listener != null)
                {
                    listener.onLetterSelected(textView.getText().toString());
                }

                break;
            case MotionEvent.ACTION_UP:

                if(listener != null)
                {
                    listener.onLetterSelected("");
                }
                break;
        }

        return true;
    }
}
