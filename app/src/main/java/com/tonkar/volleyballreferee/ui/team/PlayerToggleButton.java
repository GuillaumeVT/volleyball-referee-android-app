package com.tonkar.volleyballreferee.ui.team;

import android.content.Context;

import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.google.android.material.button.MaterialButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

public class PlayerToggleButton extends MaterialButton {

    private boolean                      mChecked;
    private int                          mBackgroundColor;
    private int                          mTextColor;
    private int                          mCheckedBackgroundColor;
    private int                          mCheckedTextColor;
    private OnCheckedChangeListener      mListener;

    public PlayerToggleButton(Context context) {
        super(context);
        init(context);
    }

    public PlayerToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PlayerToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mChecked = false;
        mBackgroundColor = ContextCompat.getColor(context, R.color.colorDisabledButton);
        mTextColor = ContextCompat.getColor(context, R.color.colorDisabledText);
        mCheckedBackgroundColor = ContextCompat.getColor(context, R.color.colorPrimary);
        mCheckedTextColor = ContextCompat.getColor(context, R.color.colorEnabledText);
        setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.default_text_size));
        color();

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setChecked(!mChecked);
                if (mListener != null) {
                    mListener.onCheckedChanged(PlayerToggleButton.this, mChecked);
                }
            }
        });
    }

    private void color() {
        if (mChecked) {
            UiUtils.colorTeamButton(getContext(), mCheckedBackgroundColor, this);
            setTextColor(mCheckedTextColor);
        } else {
            UiUtils.colorTeamButton(getContext(), mBackgroundColor, this);
            setTextColor(mTextColor);
        }
    }

    public void setColor(Context context, int backgroundColor) {
        mCheckedBackgroundColor = backgroundColor;
        mCheckedTextColor = UiUtils.getTextColor(context, mCheckedBackgroundColor);
        color();
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
        color();
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mListener = listener;
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(PlayerToggleButton button, boolean isChecked);
    }

}
