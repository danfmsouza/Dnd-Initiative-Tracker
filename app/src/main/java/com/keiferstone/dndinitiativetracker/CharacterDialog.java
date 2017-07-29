package com.keiferstone.dndinitiativetracker;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;


public class CharacterDialog extends DialogFragment {
    private static final String TAG = CharacterDialog.class.getSimpleName();

    protected static final String ARG_CHARACTER = "characterArg";

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

    private Callbacks callbacks;
    private EditText nameEntry;
    private EditText modifierEntry;
    private Spinner d20Spinner;

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
                .setView(getCharacterView(character))
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null);
        Dialog dialog = builder.create();
        dialog.setOnShowListener(dialog1 -> {
            Button button = ((AlertDialog) dialog1).getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                if (createCharacter(character)) {
                    dialog1.dismiss();
                }
            });
        });
        return dialog;
    }

    private View getCharacterView(@Nullable Character character) {
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

    private boolean createCharacter(@Nullable Character character) {
        if (validateInputs()) {
            if (character != null) {
                character.setName(getName());
                character.setModifier(getModifier());
                character.setD20(getD20());
            } else {
                character = new Character(getName(), getModifier(), getD20());
            }

            if (callbacks != null) {
                callbacks.onCharacterCreated(character);
            }
            return true;
        }

        return false;
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

    protected InputFilter initiativeFilter = new InputFilter() {
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
    }
}
