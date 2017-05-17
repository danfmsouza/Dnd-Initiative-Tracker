package com.keiferstone.dndinitiativetracker.ui.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.keiferstone.dndinitiativetracker.R;
import com.keiferstone.dndinitiativetracker.data.model.Party;
import com.keiferstone.dndinitiativetracker.util.InputUtils;

import timber.log.Timber;

public class PartyView extends FrameLayout {
    private static final int STATE_EMPTY = 0;
    private static final int STATE_EDIT = 1;
    private static final int STATE_SET = 2;

    private boolean allowEmpty;

    private View focusGrabber;
    private TextView addPartyText;
    private EditText partyNameText;
    private ImageButton createPartyButton;
    private OnPartyCreatedListener onPartyCreatedListener;

    public PartyView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public PartyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public PartyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    public PartyView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.view_add_party, this);
        RelativeLayout partyViewContent = (RelativeLayout) findViewById(R.id.party_view_content);
        focusGrabber = findViewById(R.id.focus_grabber);
        addPartyText = (TextView) findViewById(R.id.add_party_text);
        partyNameText = (EditText) findViewById(R.id.party_name_text);
        createPartyButton = (ImageButton) findViewById(R.id.create_party_button);

        TypedArray attributes = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.PartyView, defStyleAttr, 0);
        try {
            setAllowEmpty(attributes.getBoolean(R.styleable.PartyView_allowEmpty, true));
            setPartyName(attributes.getText(R.styleable.PartyView_partyName));
            setTextColor(attributes.getColorStateList(R.styleable.PartyView_textColor));
            createPartyButton.setImageTintList(attributes.getColorStateList(R.styleable.PartyView_createPartyIconTint));
        } finally {
            attributes.recycle();
        }

        partyViewContent.setOnClickListener(this::onClick);
        partyNameText.setOnClickListener(this::onClick);
        partyNameText.setOnEditorActionListener(this::onPartyNameEditorAction);
        createPartyButton.setOnClickListener(this::onCreatePartyClick);
    }

    public void setAllowEmpty(boolean allowEmpty) {
        this.allowEmpty = allowEmpty;
    }

    public void setPartyName(CharSequence name) {
        if (allowEmpty || !TextUtils.isEmpty(name)) {
            partyNameText.setText(name);
            setState(TextUtils.isEmpty(name) ? STATE_EMPTY : STATE_SET);
        }
    }

    public void setTextColor(@ColorInt int color) {
        partyNameText.setTextColor(color);
    }

    public void setTextColor(ColorStateList colors) {
        if (colors != null) {
            partyNameText.setTextColor(colors);
        }
    }

    public void setOnPartyCreatedListener(OnPartyCreatedListener onPartyCreatedListener) {
        this.onPartyCreatedListener = onPartyCreatedListener;
    }

    private void onClick(View view) {
        Timber.d("Party view clicked");
        setState(STATE_EDIT);
    }

    private boolean onPartyNameEditorAction(TextView textView, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            onCreatePartyClick(textView);
            return true;
        }

        return false;
    }

    private void onCreatePartyClick(View view) {
        String name = partyNameText.getText().toString();
        if (!TextUtils.isEmpty(name)) {
            Party party = new Party();
            party.setName(name);
            notifyPartyCreated(party);
            setState(STATE_SET);
        } else {
            setState(STATE_EMPTY);
        }
    }

    private void setState(int state) {
        switch (state) {
            case STATE_EMPTY:
                addPartyText.setVisibility(VISIBLE);
                partyNameText.setVisibility(GONE);
                createPartyButton.setVisibility(GONE);
                InputUtils.hideKeyboard(partyNameText);
                break;

            case STATE_EDIT:
                addPartyText.setVisibility(GONE);
                partyNameText.setVisibility(VISIBLE);
                partyNameText.setInputType(InputType.TYPE_CLASS_TEXT);
                partyNameText.requestFocus();
                partyNameText.setSelection(partyNameText.getText().length());
                createPartyButton.setVisibility(VISIBLE);
                InputUtils.showKeyboard(partyNameText);
                break;

            case STATE_SET:
                focusGrabber.requestFocus();
                addPartyText.setVisibility(GONE);
                partyNameText.setVisibility(VISIBLE);
                partyNameText.setInputType(InputType.TYPE_NULL);
                createPartyButton.setVisibility(GONE);
                InputUtils.hideKeyboard(partyNameText);
                break;
        }
    }

    private void notifyPartyCreated(@NonNull Party party) {
        if (onPartyCreatedListener != null) {
            onPartyCreatedListener.onPartyCreated(party);
        }
    }

    public interface OnPartyCreatedListener {
        void onPartyCreated(Party party);
    }
}
