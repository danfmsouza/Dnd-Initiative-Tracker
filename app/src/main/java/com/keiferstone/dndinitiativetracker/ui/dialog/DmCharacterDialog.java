package com.keiferstone.dndinitiativetracker.ui.dialog;

import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.keiferstone.dndinitiativetracker.data.model.Character;
import com.keiferstone.dndinitiativetracker.R;


public class DmCharacterDialog extends CharacterDialog {
    private EditText modifierEntry;
    private Spinner d20Spinner;

    @Override
    protected View getCharacterView(@Nullable Character character) {
        View view = getActivity().getLayoutInflater().inflate(
                R.layout.dialog_character_dm, (ViewGroup) getView(), false);

        nameEntry = (EditText) view.findViewById(R.id.name_entry);
        nameEntry.addTextChangedListener(clearErrorTextWatcher);

        modifierEntry = (EditText) view.findViewById(R.id.modifier_entry);
        modifierEntry.setFilters(new InputFilter[]{initiativeFilter});

        d20Spinner = (Spinner) view.findViewById(R.id.d20_spinner);
        ArrayAdapter<CharSequence> d20Adapter = ArrayAdapter.createFromResource(getActivity(), R.array.d20, R.layout.spinner_item);
        d20Adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        d20Spinner.setAdapter(d20Adapter);

        if (character != null) {
            nameEntry.setText(character.getName());
            modifierEntry.setText(String.valueOf(character.getModifier()));
            int d20 = character.getD20();
            d20 = d20 < 1 ? 1 : d20;
            d20 = d20 > 20 ? 20 : d20;
            d20Spinner.setSelection(d20 - 1);
        }
        return view;
    }

    private int getModifier() {
        if (modifierEntry != null) {
            String initiativeText = modifierEntry.getText().toString();
            if (!TextUtils.isEmpty(initiativeText)) {
                try {
                    return Integer.parseInt(initiativeText);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        return 0;
    }

    private int getD20() {
        if (d20Spinner != null) {
            try {
                return Integer.parseInt((String) d20Spinner.getSelectedItem());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return 1;
    }

    @Override
    protected boolean createCharacter(@Nullable Character character) {
        if (validateInputs()) {
            if (character != null) {
                character.setName(getName());
                character.setModifier(getModifier());
                character.setD20(getD20());
            } else {
                character = new Character(getName(), getModifier(), getD20());
            }

            if (onCharacterCreatedListener != null) {
                onCharacterCreatedListener.onCharacterCreated(character);
            }
            return true;
        }

        return false;
    }

    private TextWatcher clearErrorTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            nameEntry.setError(null);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
}
