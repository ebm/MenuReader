package com.example.menureader;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MenuListAdapter extends RecyclerView.Adapter<MenuListAdapter.ViewHolder> {
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView menuName;

        ViewHolder(View view) {
            super(view);
            thumbnail = view.findViewById(R.id.thumbnail);
            menuName = view.findViewById(R.id.menuName);
        }
    }
    private List<Menu> menus;
    public MenuListAdapter(List<Menu> menus) {
        this.menus = menus;
    }
    @Override
    public MenuListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MenuListAdapter.ViewHolder holder, int position) {
        Menu menu = menus.get(position);
        holder.menuName.setText("Menu " + (position + 1));
        holder.thumbnail.setImageBitmap(menu.getImageBitmap());
    }

    @Override
    public int getItemCount() {
        return menus.size();
    }
}
