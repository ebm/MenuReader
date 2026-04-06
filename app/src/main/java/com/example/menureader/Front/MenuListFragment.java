package com.example.menureader.Front;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.menureader.*;
import com.example.menureader.Handling.Controller;
import com.example.menureader.Handling.Menu;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MenuListFragment extends Fragment {
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater,
                             @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        LogHandler.m("View set to Menu List");
        View view = inflater.inflate(R.layout.menulist_fragment, container, false);

        Controller.applyOffset(view);

        RecyclerView rv = view.findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        // Temporary initializer for menu list. TODO: Create saving functionality so menu list persists

        SharedViewModel svm = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        MenuListAdapter mla = new MenuListAdapter(svm.getMenuList(), menu -> {
            Bundle args = new Bundle();
            args.putString("mode", "MenuList");
            svm.setMenu(menu);
            NavHostFragment.findNavController(this).navigate(R.id.action_menulist_to_results, args);
        });
        rv.setAdapter(mla);

        FloatingActionButton fab = view.findViewById(R.id.fabCamera);
        fab.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_menulist_to_camera));
        return view;
    }

    public static class MenuListAdapter extends RecyclerView.Adapter<MenuListAdapter.ViewHolder> {
        public static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView thumbnail;
            TextView menuName;

            ViewHolder(View view) {
                super(view);
                thumbnail = view.findViewById(R.id.thumbnail);
                menuName = view.findViewById(R.id.menuName);
            }
        }
        public interface OnMenuListClickListener {
            void onMenuClick(Menu menu);
        }
        private List<Menu> menus;
        private final OnMenuListClickListener clickListener;
        public MenuListAdapter(List<Menu> menus, OnMenuListClickListener clickListener) {
            this.menus = menus;
            this.clickListener = clickListener;
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_menu, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Menu menu = menus.get(position);
            holder.menuName.setText("Menu " + (position + 1));
            holder.thumbnail.setImageBitmap(menu.getImageBitmap());

            holder.itemView.setOnClickListener(v -> {
                int index = holder.getAdapterPosition();
                if (index != RecyclerView.NO_POSITION) {
                    clickListener.onMenuClick(menus.get(index));
                }
            });
        }

        @Override
        public int getItemCount() {
            return menus.size();
        }
    }
}
