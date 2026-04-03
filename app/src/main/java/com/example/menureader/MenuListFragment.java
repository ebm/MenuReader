package com.example.menureader;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MenuListFragment extends Fragment {
    private List<Menu> menus;
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater,
                             @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        LogHandler.m("View set to Menu List");
        View view = inflater.inflate(R.layout.menulist_fragment, container, false);

        applyOffset(view);

        RecyclerView rv = view.findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        // Temporary initializer for menu list. TODO: Create saving functionality so menu list persists
        menus = new ArrayList<>();
        MenuListAdapter mla = new MenuListAdapter(menus);
        rv.setAdapter(mla);

        FloatingActionButton fab = view.findViewById(R.id.fabCamera);
        fab.setOnClickListener(v -> NavHostFragment.findNavController(this).navigate(R.id.action_menulist_to_camera));
        return view;
    }
    private void applyOffset(View v) {
        ViewCompat.setOnApplyWindowInsetsListener(v.findViewById(R.id.fabCamera), (view, insets) -> {
            int navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            params.bottomMargin = navBarHeight + 24;  // nav bar height + your desired padding
            view.setLayoutParams(params);
            return insets;
        });
    }
}
