package com.keiferstone.dndinitiativetracker;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements
        RollInitiativeDialog.Callbacks,
        CharacterDialog.Callbacks,
        CharacterAdapter.OnCharacterClickListener {
    private CharacterStorage characterStorage;
    private List<Character> characters;

    private RecyclerView characterRecycler;
    private CharacterAdapter characterAdapter;
    private TextView emptyText;

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
    public void onCharacterCreated(Character character) {
        if (characters.contains(character)) {
            characters.remove(character);
        }

        characters.add(character);
        sortCharacters();
        characterAdapter.notifyDataSetChanged();
        characterStorage.saveCharacter(character);

        emptyText.setVisibility(characters.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onCharacterDeleted(Character character) {
        deleteCharacter(character, characters.indexOf(character));
    }

    @Override
    public void onCharacterClicked(Character character, int position) {
        markCharacter(character);
    }

    @Override
    public void onCharacterLongClicked(Character character, int position) {
        showEditCharacterDialog(character);
    }

    private void showAddCharacterDialog() {
        CharacterDialog.show(getFragmentManager());
    }

    private void showEditCharacterDialog(Character character) {
        CharacterDialog.show(getFragmentManager(), character);
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
        boolean alreadyMarked = character.isMarked();
        for (Character c : characters) {
            if (c.isMarked()) {
                c.setMarked(false);
                characterAdapter.notifyItemChanged(characters.indexOf(c));
            }
        }
        character.setMarked(!alreadyMarked);
        characterAdapter.notifyItemChanged(characters.indexOf(character));
    }

    private void deleteCharacter(final Character character, final int position) {
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
}
