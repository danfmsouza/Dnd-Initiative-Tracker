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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements CharacterDialog.Callbacks, CharacterAdapter.OnCharacterClickListener {
    public static final int MODE_SIMPLE = 0;
    public static final int MODE_DM = 1;

    private int mode;
    private CharacterStorage characterStorage;
    private List<Character> characters;

    private ActionBarDrawerToggle drawerToggle;
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
        mode = Preferences.getMode(this);
        characterStorage = new CharacterStorage(this);
        characters = characterStorage.loadAllCharacters();
        sortCharacters();

        // Init views
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        DrawerLayout navigationDrawer = (DrawerLayout) findViewById(R.id.navigation_drawer);
        characterRecycler = (RecyclerView) findViewById(R.id.character_recycler);
        emptyText = (TextView) findViewById(R.id.empty_text);

        // Setup toolbar
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        drawerToggle = new ActionBarDrawerToggle(this, navigationDrawer, R.string.open_drawer, R.string.close_drawer);
        navigationDrawer.addDrawerListener(drawerToggle);

        // Bind data to views
        characterAdapter = new CharacterAdapter(characters, this, mode);
        characterRecycler.setLayoutManager(new LinearLayoutManager(this));
        characterRecycler.setAdapter(characterAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchCallbacks(0, ItemTouchHelper.LEFT));
        itemTouchHelper.attachToRecyclerView(characterRecycler);
        emptyText.setVisibility(characters.isEmpty() ? View.VISIBLE : View.GONE);
        FloatingActionButton addCharacterButton = (FloatingActionButton) findViewById(R.id.add_character_button);
        addCharacterButton.setOnClickListener(v -> showAddCharacterDialog());
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.getItem(0).setVisible(mode == MODE_DM);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_roll:
                confirmRollInitiative();
                return true;
            case R.id.mode_simple:
                setMode(MODE_SIMPLE);
                return true;
            case R.id.mode_dm:
                setMode(MODE_DM);
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
    public void onCharacterCreated(Character character) {
        if (characters.contains(character)) {
            characters.remove(character);
        }

        characters.add(character);
        sortCharacters();
        characterRecycler.getAdapter().notifyDataSetChanged();
        characterStorage.saveCharacter(character);

        emptyText.setVisibility(characters.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onCharacterClicked(Character character, int position) {
        markCharacter(character);
        characterAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCharacterLongClicked(Character character, int position) {
        showEditCharacterDialog(character);
    }

    private void setMode(int mode) {
        this.mode = mode;
        invalidateOptionsMenu();
        characterAdapter.setMode(mode);
        Preferences.setMode(this, mode);
    }

    private void showAddCharacterDialog() {
        CharacterDialog.show(getFragmentManager(), mode);
    }

    private void showEditCharacterDialog(Character character) {
        CharacterDialog.show(getFragmentManager(), character, mode);
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
            }
        }
        character.setMarked(!alreadyMarked);
    }

    private void confirmRollInitiative() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.roll_initiative_confirmation_message)
                .setPositiveButton(R.string.roll, (dialog, which) -> rollInitiative())
                .setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    private void rollInitiative() {
        for (Character character : characters) {
            characterStorage.saveCharacter(character);
            character.setD20(rollD20());
        }
        sortCharacters();
        characterAdapter.notifyDataSetChanged();
        Snackbar.make(characterRecycler, R.string.initiative_rolled, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, v -> {
                    characters.clear();
                    characters.addAll(characterStorage.loadAllCharacters());
                    sortCharacters();
                    characterAdapter.notifyDataSetChanged();
                })
                .setActionTextColor(ContextCompat.getColor(MainActivity.this, R.color.white))
                .show();
    }

    private int rollD20() {
        return ThreadLocalRandom.current().nextInt(1, 21);
    }

    private void deleteCharacter(final Character character, final int position) {
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
            final int adapterPosition = viewHolder.getAdapterPosition();
            deleteCharacter(characters.remove(adapterPosition), adapterPosition);
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            getDefaultUIUtil().clearView(((CharacterAdapter.CharacterViewHolder) viewHolder).getSwipeableView());
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            getDefaultUIUtil().onDraw(c, recyclerView, ((CharacterAdapter.CharacterViewHolder) viewHolder).getSwipeableView(), dX, dY, actionState, isCurrentlyActive);
        }
    }
}
