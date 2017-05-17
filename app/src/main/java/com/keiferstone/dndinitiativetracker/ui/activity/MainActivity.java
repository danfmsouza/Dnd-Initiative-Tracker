package com.keiferstone.dndinitiativetracker.ui.activity;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.keiferstone.dndinitiativetracker.data.PartyManager;
import com.keiferstone.dndinitiativetracker.data.model.Party;
import com.keiferstone.dndinitiativetracker.data.model.Character;
import com.keiferstone.dndinitiativetracker.ui.adapter.CharacterAdapter;
import com.keiferstone.dndinitiativetracker.R;
import com.keiferstone.dndinitiativetracker.data.Preferences;
import com.keiferstone.dndinitiativetracker.ui.dialog.CharacterDialog;
import com.keiferstone.dndinitiativetracker.ui.dialog.DeletePartyDialog;
import com.keiferstone.dndinitiativetracker.ui.view.PartyView;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements
        DeletePartyDialog.OnPartyDeletedListener,
        CharacterDialog.OnCharacterCreatedListener,
        CharacterAdapter.OnCharacterClickListener {
    public static final int MODE_SIMPLE = 0;
    public static final int MODE_DM = 1;

    private Preferences preferences;
    private List<Party> parties;
    private Party activeParty;

    private ActionBarDrawerToggle drawerToggle;
    private PartyView activePartyView;
    private PartyView addPartyView;
    private ListView partyList;
    private ArrayAdapter partyAdapter;
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
        preferences = new Preferences(this);
        parties = PartyManager.getAllParties();
        activeParty = PartyManager.getActiveParty();
        sortCharacters();

        // Init views
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        DrawerLayout navigationDrawer = (DrawerLayout) findViewById(R.id.navigation_drawer);
        activePartyView = (PartyView) findViewById(R.id.active_party);
        addPartyView = (PartyView) findViewById(R.id.add_party);
        partyList = (ListView) findViewById(R.id.party_list);
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
        activePartyView.setPartyName(PartyManager.getActiveParty().getName());
        activePartyView.setOnPartyCreatedListener(this::addOrUpdateParty);
        addPartyView.setOnPartyCreatedListener(this::addParty);
        partyList.setOnItemClickListener((parent, view, position, id) -> {
            navigationDrawer.closeDrawers();
            setActiveParty((Party) partyAdapter.getItem(position));
        });
        partyList.setOnItemLongClickListener((parent, view, position, id) -> {
            DeletePartyDialog.show(getFragmentManager(), (Party) partyAdapter.getItem(position));
            return true;
        });
        updatePartyAdapter();
        characterRecycler.setLayoutManager(new LinearLayoutManager(this));
        updateCharacterAdapter();
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchCallbacks(0, ItemTouchHelper.LEFT));
        itemTouchHelper.attachToRecyclerView(characterRecycler);
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
        menu.getItem(0).setVisible(preferences.getMode() == MODE_DM);
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
    public void onPartyDeleted(Party party) {
        parties.remove(party);
        sortParties();
        setActiveParty(PartyManager.getActiveParty());
        addOrUpdateParty(activeParty);
        updatePartyAdapter();
    }

    @Override
    public void onCharacterCreated(Character character) {
        activeParty.addOrUpdateCharacter(character);
        sortCharacters();
        PartyManager.addOrUpdateParty(activeParty);
        updateCharacterAdapter();
        emptyText.setVisibility(activeParty.hasCharacters() ? View.GONE : View.VISIBLE);
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
        invalidateOptionsMenu();
        characterAdapter.setMode(mode);
        preferences.setMode(mode);
    }

    private void addParty(Party party) {
        addPartyView.setPartyName(null);
        addOrUpdateParty(party);
        sortParties();
        updatePartyAdapter();
        setActiveParty(party);
    }

    private void setActiveParty(Party party) {
        activeParty = party;
        activePartyView.setPartyName(activeParty.getName());
        updateCharacterAdapter();
    }

    private void addOrUpdateParty(Party party) {
        parties.remove(party);
        parties.add(party);
        PartyManager.addOrUpdateParty(party);
    }

    private void updatePartyAdapter() {
        partyAdapter = new ArrayAdapter<Party>(
                this, android.R.layout.simple_list_item_1, PartyManager.getAllParties()) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setText(getItem(position).getName());
                return textView;
            }
        };
        partyList.setAdapter(partyAdapter);
    }

    private void sortParties() {
        Collections.sort(parties, (party1, party2) -> party1.getName().compareTo(party2.getName()));
    }

    private void updateCharacterAdapter() {
        characterAdapter = new CharacterAdapter(activeParty.getCharacters(), this, preferences.getMode());
        characterRecycler.setAdapter(characterAdapter);
        emptyText.setVisibility(activeParty.hasCharacters() ? View.GONE : View.VISIBLE);
    }

    private void showAddCharacterDialog() {
        CharacterDialog.show(getFragmentManager(), preferences.getMode());
    }

    private void showEditCharacterDialog(Character character) {
        CharacterDialog.show(getFragmentManager(), character, preferences.getMode());
    }

    private void sortCharacters() {
        Collections.sort(activeParty.getCharacters(), (character1, character2) -> {
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
        for (Character c : activeParty.getCharacters()) {
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
        final Party oldParty = activeParty;
        for (Character character : activeParty.getCharacters()) {
            character.setD20(rollD20());
        }
        sortCharacters();
        PartyManager.addOrUpdateParty(activeParty);
        characterAdapter.notifyDataSetChanged();
        Snackbar.make(characterRecycler, R.string.initiative_rolled, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, v -> {
                    activeParty = oldParty;
                    PartyManager.addOrUpdateParty(activeParty);
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
        activeParty.deleteCharacter(character);
        PartyManager.addOrUpdateParty(activeParty);
        characterAdapter.notifyItemRemoved(position);
        Snackbar.make(characterRecycler, R.string.character_removed, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, v -> {
                    activeParty.getCharacters().add(position, character);
                    sortCharacters();
                    PartyManager.addOrUpdateParty(activeParty);
                    characterAdapter.notifyItemInserted(position);
                    emptyText.setVisibility(activeParty.hasCharacters() ? View.VISIBLE : View.GONE);
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
            deleteCharacter(characterAdapter.getItem(adapterPosition), adapterPosition);
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
