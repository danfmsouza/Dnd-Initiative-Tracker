package com.keiferstone.dndinitiativetracker;

import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class SimpleCharacterDialog extends CharacterDialog {
    private EditText initiativeEntry;

    @Override
    protected View getCharacterView(@Nullable Character character) {
        View view = getActivity().getLayoutInflater().inflate(
                R.layout.dialog_character_simple, (ViewGroup) getView(), false);

        nameEntry = (EditText) view.findViewById(R.id.name_entry);
        nameEntry.addTextChangedListener(clearErrorTextWatcher);

        initiativeEntry = (EditText) view.findViewById(R.id.initiative_entry);
        initiativeEntry.addTextChangedListener(clearErrorTextWatcher);
        initiativeEntry.setFilters(new InputFilter[]{initiativeFilter});

        if (character != null) {
            nameEntry.setText(character.getName());
            initiativeEntry.setText(String.valueOf(character.getInitiative()));
        }
        return view;
    }

    private int getInitiative() {
        if (initiativeEntry != null) {
            String initiativeText = initiativeEntry.getText().toString();
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

    @Override
    protected boolean validateInputs() {
        boolean valid = super.validateInputs();
        if (TextUtils.isEmpty(getName())) {
            nameEntry.setError("Name plz");
            valid = false;
        }

        if (getInitiative() < 1) {
            initiativeEntry.setError("Initiative plz");
            valid = false;
        }
        return valid;
    }

    @Override
    protected boolean createCharacter(@Nullable Character character) {
        if (validateInputs()) {
            if (character != null) {
                character.setName(getName());
                character.setModifier(0);
                character.setD20(getInitiative());
            } else {
                character = new Character(getName(), 0, getInitiative());
            }

            if (callbacks != null) {
                callbacks.onCharacterCreated(character);
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
            initiativeEntry.setError(null);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
}
