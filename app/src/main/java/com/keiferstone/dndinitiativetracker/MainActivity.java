package com.keiferstone.dndinitiativetracker;

import android.content.Context;
import android.graphics.Canvas;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements CharacterDialog.Callbacks, CharacterAdapter.OnCharacterClickListener {
    public static final int MODE_SIMPLE = 0;
    public static final int MODE_COMPLEX = 1;

    private int mode;

    private CharacterStorage characterStorage;
    private CharacterAdapter characterAdapter;
    private List<Character> characters;
    private RecyclerView characterRecycler;
    private TextView emptyText;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mode = Preferences.getMode(this);

        characterStorage = new CharacterStorage(this);
        characters = characterStorage.loadAllCharacters();
        sortCharacters();

        characterAdapter = new CharacterAdapter(characters, this, mode);

        characterRecycler = (RecyclerView) findViewById(R.id.character_recycler);
        characterRecycler.setLayoutManager(new LinearLayoutManager(this));
        characterRecycler.setAdapter(characterAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchCallbacks(0, ItemTouchHelper.LEFT));
        itemTouchHelper.attachToRecyclerView(characterRecycler);

        emptyText = (TextView) findViewById(R.id.empty_text);
        emptyText.setVisibility(characters.isEmpty() ? View.VISIBLE : View.GONE);

        FloatingActionButton addCharacterButton = (FloatingActionButton) findViewById(R.id.add_character_button);
        addCharacterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddCharacterDialog();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.getItem(0).setVisible(mode == MODE_COMPLEX);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_roll:
                rollInitiative();
                return true;
            case R.id.mode_simple:
                setMode(MODE_SIMPLE);
                return true;
            case R.id.mode_complex:
                setMode(MODE_COMPLEX);
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
        Collections.sort(characters, new Comparator<Character>() {
            @Override
            public int compare(Character character1, Character character2) {
                if (character1.getInitiative() > character2.getInitiative()) {
                    return -1;
                } else if (character1.getInitiative() < character2.getInitiative()) {
                    return 1;
                } else {
                    return character1.getName().compareTo(character2.getName());
                }
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

    private void rollInitiative() {
        for (Character character : characters) {
            characterStorage.saveCharacter(character);
            character.setD20(rollD20());
        }
        sortCharacters();
        characterAdapter.notifyDataSetChanged();
        Snackbar.make(characterRecycler, R.string.initiative_rolled, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        characters.clear();
                        characters.addAll(characterStorage.loadAllCharacters());
                        sortCharacters();
                        characterAdapter.notifyDataSetChanged();
                    }
                })
                .setActionTextColor(ContextCompat.getColor(MainActivity.this, R.color.white))
                .show();
    }

    private int rollD20() {
        return ThreadLocalRandom.current().nextInt(1, 21);
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
            final Character character = characters.remove(adapterPosition);
            characterStorage.deleteCharacter(character);
            characterAdapter.notifyItemRemoved(adapterPosition);
            Snackbar.make(characterRecycler, R.string.character_removed, Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            characters.add(adapterPosition, character);
                            sortCharacters();
                            characterStorage.saveCharacter(character);
                            characterAdapter.notifyItemInserted(adapterPosition);
                            emptyText.setVisibility(characters.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    })
                    .setActionTextColor(ContextCompat.getColor(MainActivity.this, R.color.white))
                    .show();
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
