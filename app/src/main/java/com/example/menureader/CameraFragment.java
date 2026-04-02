package com.example.menureader;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraFragment extends Fragment {
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startCamera();
                } else {
                    Toast.makeText(requireContext(), "Camera permission needed", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater,
                             @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        LogHandler.m("View set to Camera");
        View view = inflater.inflate(R.layout.camera_fragment, container, false);

        previewView = view.findViewById(R.id.previewView);
        cameraExecutor = Executors.newSingleThreadExecutor();

        applyOffset(view);

        // Check permission, then start camera
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }

        // FAB triggers photo capture
        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v -> takePhoto());

        return view;
    }

    private void applyOffset(View v) {
        ViewCompat.setOnApplyWindowInsetsListener(v.findViewById(R.id.fab), (view, insets) -> {
            int navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
            params.bottomMargin = navBarHeight + 24;  // nav bar height + your desired padding
            view.setLayoutParams(params);
            return insets;
        });
    }

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

    private void takePhoto() {
        LogHandler.m("Image capture attempted");
        if (imageCapture == null) return;

        imageCapture.takePicture(ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                        // Convert ImageProxy to Bitmap
                        Bitmap bitmap = imageProxyToBitmap(imageProxy);
                        imageProxy.close();  // MUST close or camera freezes

                        // Now pass bitmap to your Menu class
                        new Menu(bitmap, new Menu.OnMenuReadyListener() {
                            @Override
                            public void onMenuReady(Menu menu) {
                                LogHandler.m("Camera callback results are ready");
                                NavHostFragment.findNavController(CameraFragment.this).navigate(R.id.action_camera_to_results);
                            }

                            @Override
                            public void onMenuFailed(Exception e) {
                                LogHandler.m("Camera: Menu failed", e);
                            }
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException e) {
                        LogHandler.m("Camera: Photo capture failed", e);
                    }
                });
    }

    private Bitmap imageProxyToBitmap(ImageProxy image) {
        ImageProxy.PlaneProxy plane = image.getPlanes()[0];
        ByteBuffer buffer = plane.getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cameraExecutor.shutdown();
    }

}
