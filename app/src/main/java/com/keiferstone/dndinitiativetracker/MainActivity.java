package com.keiferstone.dndinitiativetracker;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_IDLE;

public class MainActivity extends AppCompatActivity implements
        RollInitiativeDialog.Callbacks,
        CharacterDialog.Callbacks,
        CharacterAdapter.OnCharacterClickListener {
    private CharacterStorage characterStorage;
    private List<Character> characters;
    private RecyclerView characterRecycler;
    private CharacterAdapter characterAdapter;
    private TextView emptyText;
    private ActionMode actionMode;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init data
        characterStorage = new CharacterStorage(this);
        characters = characterStorage.loadAllCharacters();
        deselectCharacters();
        sortCharacters();

        // Init views
        Toolbar toolbar = findViewById(R.id.toolbar);
        characterRecycler = findViewById(R.id.character_recycler);
        emptyText = findViewById(R.id.empty_text);

        // Setup toolbar
        setSupportActionBar(toolbar);

        // Bind data to views
        characterAdapter = new CharacterAdapter(characters, this);
        characterRecycler.setLayoutManager(new LinearLayoutManager(this));
        characterRecycler.setAdapter(characterAdapter);
        ((SimpleItemAnimator) characterRecycler.getItemAnimator()).setSupportsChangeAnimations(false);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchCallbacks(0, ItemTouchHelper.LEFT));
        itemTouchHelper.attachToRecyclerView(characterRecycler);
        emptyText.setVisibility(characters.isEmpty() ? View.VISIBLE : View.GONE);
        FloatingActionButton addCharacterButton = findViewById(R.id.add_character_button);
        addCharacterButton.setOnClickListener(v -> showAddCharacterDialog());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_roll:
                RollInitiativeDialog.show(getFragmentManager(), (ArrayList<Character>) characters);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        for (Character character : characters) {
            characterStorage.saveCharacter(character);
        }
    }

    @Override
    public void onInitiativeRolled(List<Character> characters) {
        this.characters.clear();
        this.characters.addAll(characters);
        sortCharacters();
        characterAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCharacterSaved(Character character) {
        if (characters.contains(character)) {
            characters.remove(character);
        }

        characters.add(character);
        sortCharacters();
        characterAdapter.notifyDataSetChanged();
        characterStorage.saveCharacter(character);

        emptyText.setVisibility(characters.isEmpty() ? View.VISIBLE : View.GONE);

        clearActionMode();
    }

    @Override
    public void onCharacterClicked(Character character, int position) {
        markCharacter(character);
    }

    @Override
    public void onCharacterLongClicked(Character character, int position) {
        startSupportActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(character.isDead()
                        ? R.menu.menu_dead_character_actions
                        : R.menu.menu_character_actions, menu);
                characters.get(position).setSelected(true);
                characterAdapter.notifyItemChanged(position);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                actionMode = mode;
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_revive:
                        reviveCharacter(character);
                        mode.finish();
                        return true;
                    case R.id.action_edit:
                        showEditCharacterDialog(character);
                        return true;
                    case R.id.action_delete:
                        showDeleteCharacterDialog(character);
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                actionMode = null;
                deselectCharacters();
                characterAdapter.notifyItemChanged(position);
            }
        });
    }

    private void clearActionMode() {
        if (actionMode != null) {
            actionMode.finish();
        }
    }

    private void deselectCharacters() {
        for (Character character : characters) {
            character.setSelected(false);
        }
    }

    private void showAddCharacterDialog() {
        CharacterDialog.show(getFragmentManager());
    }

    private void showEditCharacterDialog(Character character) {
        CharacterDialog.show(getFragmentManager(), character);
    }

    private void showDeleteCharacterDialog(Character character) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_character)
                .setMessage(getString(R.string.delete_character_message, character.getName()))
                .setPositiveButton(R.string.delete, null)
                .setNegativeButton(R.string.cancel, null);
        Dialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            Button deleteButton = ((AlertDialog) d).getButton(AlertDialog.BUTTON_POSITIVE);
            deleteButton.setOnClickListener(view -> {
                deleteCharacter(character);
                d.dismiss();
                clearActionMode();
            });
        });
        dialog.show();
    }

    private void sortCharacters() {
        Collections.sort(characters, (character1, character2) -> {
            if (character1.getInitiative() > character2.getInitiative()) {
                return -1;
            } else if (character1.getInitiative() < character2.getInitiative()) {
                return 1;
            } else {
                return character1.getName().compareTo(character2.getName());
            }
        });
    }

    private void markCharacter(Character character) {
        if (character.isDead()) {
            character.setMarked(false);
        } else {
            boolean alreadyMarked = character.isMarked();
            for (Character c : characters) {
                if (c.isMarked()) {
                    c.setMarked(false);
                    characterAdapter.notifyItemChanged(characters.indexOf(c));
                }
            }
            character.setMarked(!alreadyMarked);
        }
        characterAdapter.notifyItemChanged(characters.indexOf(character));
    }

    private void killCharacter(Character character) {
        character.setDead(true);
        character.setMarked(false);
        characterAdapter.notifyItemChanged(characters.indexOf(character));
    }

    private void reviveCharacter(Character character) {
        character.setDead(false);
        characterAdapter.notifyItemChanged(characters.indexOf(character));
    }

    private void deleteCharacter(final Character character) {
        int position = characters.indexOf(character);
        character.setSelected(false);
        characters.remove(character);
        characterStorage.deleteCharacter(character);
        characterAdapter.notifyItemRemoved(position);
        Snackbar.make(characterRecycler, R.string.character_removed, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, v -> {
                    characters.add(position, character);
                    sortCharacters();
                    characterStorage.saveCharacter(character);
                    characterAdapter.notifyItemInserted(position);
                    emptyText.setVisibility(characters.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .setActionTextColor(ContextCompat.getColor(MainActivity.this, R.color.white))
                .show();
    }

    private class ItemTouchCallbacks extends ItemTouchHelper.SimpleCallback {
        ItemTouchCallbacks(int dragDirs, int swipeDirs) {
            super(dragDirs, swipeDirs);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            killCharacter(characters.get(viewHolder.getAdapterPosition()));
        }

        @Override
        public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return characters.get(viewHolder.getAdapterPosition()).isDead() ? 0 : ItemTouchHelper.LEFT;
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            ((CharacterAdapter.CharacterViewHolder) viewHolder).background.setTranslationX(dX);
        }
    }
}
