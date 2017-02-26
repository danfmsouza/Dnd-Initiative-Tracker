package com.keiferstone.dndinitiativetracker;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import static com.keiferstone.dndinitiativetracker.MainActivity.MODE_SIMPLE;


class CharacterAdapter extends RecyclerView.Adapter<CharacterAdapter.CharacterViewHolder> {
    private List<Character> characters;
    private OnCharacterClickListener listener;
    private int mode;

    CharacterAdapter(@NonNull List<Character> characters, OnCharacterClickListener listener, int mode) {
        this.characters = characters;
        this.listener = listener;
        this.mode = mode;
    }

    @Override
    public CharacterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_character, parent, false);
        return new CharacterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CharacterViewHolder holder, int position) {
        final Character character = getItem(position);
        holder.container.setBackgroundColor(getBackgroundColor(holder.itemView.getContext(), position));
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onCharacterClicked(character, holder.getAdapterPosition());
                }
            }
        });
        holder.container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    listener.onCharacterLongClicked(character, holder.getAdapterPosition());
                    return true;
                } else {
                    return false;
                }
            }
        });
        holder.name.setText(character.getName());
        holder.initiative.setText(String.valueOf(character.getInitiative()));
        holder.initiativeBreakdown.setVisibility(mode == MODE_SIMPLE ? View.GONE : View.VISIBLE);
        holder.initiativeBreakdown.setText(getInitiativeBreakdown(holder.itemView.getContext(), character));
        holder.marker.setVisibility(character.isMarked() ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return characters.size();
    }

    void setMode(int mode) {
        this.mode = mode;
        notifyDataSetChanged();
    }

    private Character getItem(int position) {
        return characters.get(position);
    }

    @ColorInt
    private int getBackgroundColor(Context context, int position) {
        if (position % 2 == 0) {
            return ContextCompat.getColor(context, R.color.white);
        } else {
            return ContextCompat.getColor(context, R.color.grey);
        }
    }

    private String getInitiativeBreakdown(Context context, Character character) {
        return context.getString(R.string.initiative_breakdown, character.getD20(), character.getModifier());
    }

    class CharacterViewHolder extends RecyclerView.ViewHolder {
        View deleteIcon;
        View container;
        TextView name;
        TextView initiative;
        TextView initiativeBreakdown;
        View marker;

        CharacterViewHolder(View itemView) {
            super(itemView);
            deleteIcon = itemView.findViewById(R.id.delete_icon);
            container = itemView.findViewById(R.id.character_container);
            name = (TextView) itemView.findViewById(R.id.name);
            initiative = (TextView) itemView.findViewById(R.id.initiative);
            initiativeBreakdown = (TextView) itemView.findViewById(R.id.initiative_breakdown);
            marker = itemView.findViewById(R.id.marker);
        }

        View getSwipeableView() {
            return container;
        }
    }

    interface OnCharacterClickListener {
        void onCharacterClicked(Character character, int position);
        void onCharacterLongClicked(Character character, int position);
    }
}
