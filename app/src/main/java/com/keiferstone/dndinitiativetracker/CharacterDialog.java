package com.keiferstone.dndinitiativetracker;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import static com.keiferstone.dndinitiativetracker.MainActivity.MODE_SIMPLE;

public abstract class CharacterDialog extends DialogFragment {
    private static final String TAG = CharacterDialog.class.getSimpleName();

    protected static final String ARG_MODE = "modeArg";
    protected static final String ARG_CHARACTER = "characterArg";

    public static void show(FragmentManager fragmentManager, int mode) {
        CharacterDialog dialog;
        if (mode == MODE_SIMPLE) {
            dialog = new SimpleCharacterDialog();
        } else {
            dialog = new ComplexCharacterDialog();
        }
        Bundle args = new Bundle();
        args.putInt(ARG_MODE, mode);
        dialog.setArguments(args);
        dialog.show(fragmentManager, TAG);
    }

    public static void show(FragmentManager fragmentManager, Character character, int mode) {
        CharacterDialog dialog;
        if (mode == MODE_SIMPLE) {
            dialog = new SimpleCharacterDialog();
        } else {
            dialog = new ComplexCharacterDialog();
        }
        Bundle args = new Bundle();
        args.putParcelable(ARG_CHARACTER, character);
        args.putInt(ARG_MODE, mode);
        dialog.setArguments(args);
        dialog.show(fragmentManager, TAG);
    }

    protected Callbacks callbacks;
    protected EditText nameEntry;

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

    protected abstract View getCharacterView(@Nullable Character character);

    protected abstract boolean createCharacter(@Nullable Character character);

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


    protected InputFilter initiativeFilter = new InputFilter() {

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
