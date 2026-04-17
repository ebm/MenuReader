package com.example.menureader.Front;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.example.menureader.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.List;

public class ImageViewerDialog extends BottomSheetDialogFragment {
    private static final String ARG_TITLE = "title";

    private final List<Bitmap> bitmaps = new ArrayList<>();
    private ImagePageAdapter adapter;
    private ViewPager2 viewPager;
    private ProgressBar progressBar;
    private TextView pageIndicator;
    private TextView errorText;

    public static ImageViewerDialog newInstance(String title) {
        ImageViewerDialog dialog = new ImageViewerDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        dialog.setArguments(args);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_image_viewer, container, false);

        ((TextView) view.findViewById(R.id.dialog_title))
                .setText(requireArguments().getString(ARG_TITLE, ""));

        viewPager = view.findViewById(R.id.view_pager);
        progressBar = view.findViewById(R.id.progress_bar);
        pageIndicator = view.findViewById(R.id.page_indicator);
        errorText = view.findViewById(R.id.error_text);

        adapter = new ImagePageAdapter(bitmaps);
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateIndicator(position);
            }
        });

        // Images may have arrived before the view was created (cache hit path)
        if (!bitmaps.isEmpty()) {
            showImages();
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewPager.setAdapter(null);
        adapter = null;
    }

    /**
     * Add an image to the gallery. Safe to call before the dialog is shown (images are queued)
     * and after it is dismissed (no-op once adapter is cleared).
     */
    public void addImage(Bitmap bitmap) {
        bitmaps.add(bitmap);
        if (adapter != null) {
            adapter.notifyItemInserted(bitmaps.size() - 1);
            showImages();
        }
    }

    public void showError() {
        if (progressBar == null) return; // view not created yet; nothing to show
        progressBar.setVisibility(View.GONE);
        errorText.setVisibility(View.VISIBLE);
    }

    private void showImages() {
        progressBar.setVisibility(View.GONE);
        viewPager.setVisibility(View.VISIBLE);
        updateIndicator(viewPager.getCurrentItem());
    }

    private void updateIndicator(int position) {
        if (bitmaps.size() > 1) {
            pageIndicator.setVisibility(View.VISIBLE);
            pageIndicator.setText((position + 1) + " / " + bitmaps.size());
        } else {
            pageIndicator.setVisibility(View.GONE);
        }
    }

    private static class ImagePageAdapter extends RecyclerView.Adapter<ImagePageAdapter.ViewHolder> {
        private final List<Bitmap> bitmaps;

        ImagePageAdapter(List<Bitmap> bitmaps) {
            this.bitmaps = bitmaps;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            return new ViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.imageView.setImageBitmap(bitmaps.get(position));
        }

        @Override
        public int getItemCount() {
            return bitmaps.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView imageView;

            ViewHolder(@NonNull ImageView itemView) {
                super(itemView);
                this.imageView = itemView;
            }
        }
    }
}
