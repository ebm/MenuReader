package com.example.menureader;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.jetbrains.annotations.NotNull;

public class ResultsFragment extends Fragment {
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater,
                             @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        LogHandler.m("View set to Results");
        View view = inflater.inflate(R.layout.results_fragment, container, false);
        //applyOffset(view);

        SharedViewModel svm = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        new Menu(svm.getBitmap(), new Menu.OnMenuReadyListener() {
            @Override
            public void onMenuReady(Menu menu) {
                LogHandler.m("Menu received from camera: " + menu.getText());
                // TODO: display current menu
            }

            @Override
            public void onMenuFailed(Exception e) {
                LogHandler.m("Camera: Menu failed", e);
            }
        });

        Button backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_results_to_menulist);
        });
        return view;
    }
//    private void applyOffset(View v) {
//        ViewCompat.setOnApplyWindowInsetsListener(v.findViewById(R.id.back_button), (view, insets) -> {
//            int navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
//            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
//            params.bottomMargin = navBarHeight - 50;  // nav bar height + your desired padding
//            view.setLayoutParams(params);
//            return insets;
//        });
//    }
}
