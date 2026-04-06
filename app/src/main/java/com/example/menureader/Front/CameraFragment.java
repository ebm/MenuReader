package com.example.menureader.Front;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.example.menureader.Handling.Controller;
import com.example.menureader.LogHandler;
import com.example.menureader.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraFragment extends Fragment {
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;

    /**
     * Prompts user for camera permissions
     */
    public void launchCameraPermission() {
         registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
             if (granted) {
                 startCamera();
             } else {
                 Toast.makeText(requireContext(), "Camera permission needed", Toast.LENGTH_SHORT).show();
             }
         }).launch(Manifest.permission.CAMERA);
    }

    /**
     * Function gets called before fragment loads
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater,
                             @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        LogHandler.m("View set to Camera");
        View view = inflater.inflate(com.example.menureader.R.layout.camera_fragment, container, false);

        previewView = view.findViewById(com.example.menureader.R.id.previewView);
        cameraExecutor = Executors.newSingleThreadExecutor();

        Controller.applyOffset(view);

        // Check permission, then start camera
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            launchCameraPermission();
        }

        // FAB triggers photo capture
        FloatingActionButton fab = view.findViewById(com.example.menureader.R.id.fab);
        fab.setOnClickListener(v -> takePhoto());

        return view;
    }

    /**
     * Starts the camera
     */
    private void startCamera() {
        LogHandler.m("Camera started");
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Preview use case
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // ImageCapture use case
                imageCapture = new ImageCapture.Builder().build();

                // Use back camera
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // Unbind any previous use cases, then bind new ones
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview, imageCapture);

            } catch (Exception e) {
                LogHandler.m("Camera: Failed to start camera", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    /**
     * Takes photo
     */
    private void takePhoto() {
        LogHandler.m("Image capture attempted");
        if (imageCapture == null) return;
        // Fake camera capture
        try {
            InputStream is = requireContext().getAssets().open("McDonalds_Menu.jpg");
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            SharedViewModel svm = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
            svm.setBitmap(bitmap);
            Bundle args = new Bundle();
            args.putString("mode", "Photo");
            NavHostFragment.findNavController(this).navigate(R.id.action_camera_to_results, args);
        } catch (Exception e) {
            LogHandler.m("Failed to load test image", e);
        }
        // Real camera capture
//        imageCapture.takePicture(ContextCompat.getMainExecutor(requireContext()),
//                new ImageCapture.OnImageCapturedCallback() {
//                    @Override
//                    public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
//                        // Convert ImageProxy to Bitmap
//                        Bitmap bitmap = imageProxyToBitmap(imageProxy);
//                        imageProxy.close();  // MUST close or camera freezes
//
//                        SharedViewModel svm = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
//                        svm.setBitmap(bitmap);
//
//                        NavHostFragment.findNavController(CameraFragment.this).navigate(R.id.action_camera_to_results);
//                    }
//
//                    @Override
//                    public void onError(@NonNull ImageCaptureException e) {
//                        LogHandler.m("Camera: Photo capture failed", e);
//                    }
//                });
    }


    /**
     * Shuts down camera and any other essentials when fragment destructor called
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cameraExecutor.shutdown();
    }

}
