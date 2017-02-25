package com.keiferstone.dndinitiativetracker;

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

    private static final String ARG_CHARACTER = "characterArg";

    public static void show(FragmentManager fragmentManager) {
        CharacterDialog dialog = new CharacterDialog();
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
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            this.callbacks = (Callbacks) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement AddCharacterDialog.Callbacks");
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
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (createCharacter(character)) {
                            dialog.dismiss();
                        }
                    }
                });
            }
        });
        return dialog;
    }

    private View getCharacterView(@Nullable Character character) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_character, (ViewGroup) getView(), false);
        nameEntry = (EditText) view.findViewById(R.id.name_entry);
        nameEntry.addTextChangedListener(clearErrorTextWatcher);

        modifierEntry = (EditText) view.findViewById(R.id.modifier_entry);
        modifierEntry.addTextChangedListener(clearErrorTextWatcher);
        modifierEntry.setFilters(new InputFilter[] {initiativeFilter});

        d20Spinner = (Spinner) view.findViewById(R.id.d20_spinner);
        ArrayAdapter<CharSequence> d20Adapter = ArrayAdapter.createFromResource(getActivity(), R.array.d20, android.R.layout.simple_spinner_item);
        d20Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        d20Spinner.setAdapter(d20Adapter);

        if (character != null) {
            nameEntry.setText(character.getName());
            modifierEntry.setText(String.valueOf(character.getModifier()));
            d20Spinner.setSelection(character.getD20() - 1);
        }
        return view;
    }

    private String getName() {
        return nameEntry.getText().toString();
    }

    private int getModifier() {
        String initiativeText = modifierEntry.getText().toString();
        if (!TextUtils.isEmpty(initiativeText)) {
            try {
                return Integer.parseInt(initiativeText);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return 0;
    }

    private int getD20() {
        try {
            return Integer.parseInt((String) d20Spinner.getSelectedItem());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return 1;
    }

    private boolean validateInputs() {
        return !TextUtils.isEmpty(getName()) && getModifier() > 0;
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
        } else {
            nameEntry.setError("Name plz");
            modifierEntry.setError("Modifier plz");
            return false;
        }
    }

    private TextWatcher clearErrorTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            nameEntry.setError(null);
            modifierEntry.setError(null);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private InputFilter initiativeFilter = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                int input = Integer.parseInt(dest.toString() + source.toString());
                if (isInRange(1, 99, input)) {
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
