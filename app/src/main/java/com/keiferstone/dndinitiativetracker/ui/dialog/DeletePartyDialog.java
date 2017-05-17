package com.keiferstone.dndinitiativetracker.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.keiferstone.dndinitiativetracker.R;
import com.keiferstone.dndinitiativetracker.data.PartyManager;
import com.keiferstone.dndinitiativetracker.data.model.Party;

import static com.keiferstone.dndinitiativetracker.ui.activity.MainActivity.MODE_SIMPLE;

public class DeletePartyDialog extends DialogFragment {
    private static final String TAG = DeletePartyDialog.class.getSimpleName();

    protected static final String ARG_PARTY = "partyArg";

    public static void show(FragmentManager fragmentManager, Party party) {
        DeletePartyDialog dialog = new DeletePartyDialog();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARTY, party);
        dialog.setArguments(args);
        dialog.show(fragmentManager, TAG);
    }

    protected OnPartyDeletedListener onPartyDeletedListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            this.onPartyDeletedListener = (OnPartyDeletedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DeletePartyDialog.OnPartyDeletedListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        final Party party = args.getParcelable(ARG_PARTY);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.delete_party)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    if (party != null) {
                        PartyManager.deleteParty(party);
                        onPartyDeletedListener.onPartyDeleted(party);
                    }
                })
                .setNegativeButton(R.string.cancel, null);
        return builder.create();
    }

    public interface OnPartyDeletedListener {
        void onPartyDeleted(Party party);
    }
}
