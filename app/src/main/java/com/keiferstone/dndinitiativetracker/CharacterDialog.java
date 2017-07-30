package com.keiferstone.dndinitiativetracker;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


public class CharacterDialog extends DialogFragment {
    private static final String TAG = CharacterDialog.class.getSimpleName();

    protected static final String ARG_CHARACTER = "characterArg";

    private Callbacks callbacks;
    private EditText nameEntry;
    private EditText modifierEntry;

    public static void show(FragmentManager fragmentManager) {
        CharacterDialog dialog = new CharacterDialog();
        Bundle args = new Bundle();
        dialog.setArguments(args);
        dialog.show(fragmentManager, TAG);
    }

    public static void show(FragmentManager fragmentManager, Character character) {
        CharacterDialog dialog = new CharacterDialog();
        Bundle args = new Bundle();
        args.putParcelable(ARG_CHARACTER, character);
        dialog.setArguments(args);
        dialog.show(fragmentManager, TAG);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.callbacks = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement CharacterDialog.Callbacks");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        final Character character = args != null ? (Character) args.getParcelable(ARG_CHARACTER) : null;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(character == null ? R.string.add_character : R.string.edit_character)
                .setView(createView(character))
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null);
        if (character != null) {
            builder.setNeutralButton(R.string.delete, null);
        }
        Dialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            Button saveButton = ((AlertDialog) d).getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(view -> {
                if (createCharacter(character)) {
                    d.dismiss();
                }
            });
            Button deleteButton = ((AlertDialog) d).getButton(AlertDialog.BUTTON_NEUTRAL);
            deleteButton.setOnClickListener(view -> {
                if (character != null) {
                    deleteCharacter(character);
                }
                d.dismiss();
            });
        });
        return dialog;
    }

    private View createView(@Nullable Character character) {
        View view = getActivity().getLayoutInflater().inflate(
                R.layout.dialog_character, (ViewGroup) getView(), false);

        nameEntry = view.findViewById(R.id.name_entry);
        nameEntry.addTextChangedListener(clearErrorTextWatcher);

        modifierEntry = view.findViewById(R.id.modifier_entry);
        modifierEntry.setFilters(new InputFilter[]{modifierFilter});

        if (character != null) {
            nameEntry.setText(character.getName());
            modifierEntry.setText(String.valueOf(character.getModifier()));
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

    private boolean createCharacter(@Nullable Character character) {
        if (validateInputs()) {
            if (character != null) {
                character.setName(getName());
                character.setModifier(getModifier());
            } else {
                character = new Character(getName(), getModifier());
            }

            if (callbacks != null) {
                callbacks.onCharacterCreated(character);
            }
            return true;
        }

        return false;
    }

    private void deleteCharacter(@NonNull Character character) {
        if (callbacks != null) {
            callbacks.onCharacterDeleted(character);
        }
    }

    protected String getName() {
        if (nameEntry != null) {
            return nameEntry.getText().toString();
        }

        return null;
    }

    protected boolean validateInputs() {
        boolean valid = true;
        if (TextUtils.isEmpty(getName())) {
            if (nameEntry != null) {
                nameEntry.setError("Name plz");
                valid = false;
            }
        }

        return valid;
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

    protected InputFilter modifierFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if ("-".equals(source)) {
                return source;
            }
            try {
                int input = Integer.parseInt(dest.toString() + source.toString());
                if (isInRange(-99, 99, input)) {
                    return null;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            return "";
        }

        private boolean isInRange(int min, int max, int input) {
            return input >= min && input <= max;
        }
    };

    interface Callbacks {
        void onCharacterCreated(Character character);
        void onCharacterDeleted(Character character);
    }
}
