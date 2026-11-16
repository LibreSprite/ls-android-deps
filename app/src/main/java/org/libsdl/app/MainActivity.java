package org.libsdl.app;

import org.libsdl.app.SDLActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends SDLActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissions();
        setFullscreen();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (MainActivity.mBrokenLibraries) {
            return;
        }
        MainActivity.nativeResume();
        setFullscreen();
    }

    void applySafeInsets(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

            mlp.topMargin = windowInsets.getStableInsetTop();
            mlp.leftMargin = windowInsets.getStableInsetLeft();
            mlp.rightMargin = windowInsets.getStableInsetRight();
            mlp.bottomMargin = windowInsets.getStableInsetBottom();

            v.setLayoutParams(mlp);
            return windowInsets;
        });
    }

    public void setFullscreen() {
        Window window = getWindow();
        if (window != null) {
            int flags = View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.INVISIBLE;
            window.getDecorView().setSystemUiVisibility(flags);
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
        this.applySafeInsets(mSurface);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected String[] getLibraries() {
        return new String[] { "SDL3", "SDL3_image", "main-lib" };
    }

    @Override
    protected String[] getArguments() {
        // ACTION_VIEW / ACTION_SEND -> onDropFile(...)
        String[] args = super.getArguments();
        Intent intent = getIntent();

        if (intent == null) return args;
        Uri uri = intent.getData();
        String path;
        if (uri == null && Intent.ACTION_SEND.equals(intent.getAction()))
            uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (uri == null) return args;
        try {
            String name = uri.getPath() == null ? "shared" : uri.getPath();
            name = name.substring(name.lastIndexOf('/') + 1);
            File out = new File(getContext().getExternalFilesDir(null), name);
            try (InputStream in = getContext().getContentResolver().openInputStream(uri);
                 FileOutputStream os = new FileOutputStream(out)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) os.write(buf, 0, n);
            }
            path = out.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return args;
        }
        String[] out = new String[args.length + 1];
        System.arraycopy(args, 0, out, 0, args.length);
        out[args.length] = path;
        return out;
    }

    private void requestPermissions() {
        // Check and request permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 1);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }

    }

    public static String getStorageDirectory() {
        return getContext().getExternalFilesDir(null).getAbsolutePath();
    }

    public static void shareFile(String path) { /* ... */ }
}
