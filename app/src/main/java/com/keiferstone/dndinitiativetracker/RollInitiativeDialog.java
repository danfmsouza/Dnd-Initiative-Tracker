package com.keiferstone.dndinitiativetracker;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class RollInitiativeDialog extends DialogFragment {
    private static final String TAG = RollInitiativeDialog.class.getSimpleName();

    private static final String ARG_CHARACTERS = "charactersArg";

    private List<Character> characters;
    private Callbacks callbacks;

    public static void show(FragmentManager fragmentManager, @NonNull ArrayList<Character> characters) {
        RollInitiativeDialog dialog = new RollInitiativeDialog();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_CHARACTERS, characters);
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
                    + " must implement RollInitiativeDialog.Callbacks");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        characters = getArguments().getParcelableArrayList(ARG_CHARACTERS);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.roll_initiative)
                .setView(createView())
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, null);
        Dialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            Button saveButton = ((AlertDialog) d).getButton(AlertDialog.BUTTON_POSITIVE);
            saveButton.setOnClickListener(view -> {
                if (callbacks != null) {
                    callbacks.onInitiativeRolled();
                }
                d.dismiss();
            });
            Button deleteButton = ((AlertDialog) d).getButton(AlertDialog.BUTTON_NEUTRAL);
            deleteButton.setOnClickListener(view -> d.dismiss());
        });
        return dialog;
    }

    private View createView() {
        View view = getActivity().getLayoutInflater().inflate(
                R.layout.dialog_roll_initiative, (ViewGroup) getView(), false);
        RecyclerView recycler = view.findViewById(R.id.roll_initiative_recycler);
        recycler.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recycler.setAdapter(new RollInitiativeAdapter(characters));
        return view;
    }

    private class RollInitiativeAdapter extends RecyclerView.Adapter<RollInitiativeAdapter.RollInitiativeViewHolder> {
        private List<Character> characters;

        RollInitiativeAdapter(@NonNull List<Character> characters) {
            this.characters = characters;
        }

        @Override
        public RollInitiativeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_roll_initiative, parent, false);
            return new RollInitiativeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final RollInitiativeViewHolder holder, int position) {
            final Character character = getItem(position);
            holder.name.setText(character.getName());

            ArrayAdapter<CharSequence> d20Adapter = ArrayAdapter.createFromResource(getActivity(), R.array.d20, R.layout.spinner_item);
            d20Adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            holder.initiative.setAdapter(d20Adapter);
            int d20 = character.getD20();
            d20 = d20 < 1 ? 1 : d20;
            d20 = d20 > 20 ? 20 : d20;
            holder.initiative.setSelection(d20 - 1);
            holder.initiative.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    character.setD20(holder.getD20());
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        @Override
        public int getItemCount() {
            return characters.size();
        }

        private Character getItem(int position) {
            return characters.get(position);
        }

        class RollInitiativeViewHolder extends RecyclerView.ViewHolder {
            TextView name;
            Spinner initiative;

            RollInitiativeViewHolder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.name);
                initiative = itemView.findViewById(R.id.initiative);
            }

            int getD20() {
                try {
                    return Integer.parseInt((String) initiative.getSelectedItem());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                return 1;
            }
        }
    }

    interface Callbacks {
        void onInitiativeRolled();
    }
}
