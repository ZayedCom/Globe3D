package com.app.nfusion.globe3d;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

    private SurfaceView glSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Renderer(this);

        // Use the constructor that takes only Context
        glSurfaceView = new SurfaceView(this);
        setContentView(glSurfaceView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure GLSurfaceView's onResume is called
        glSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Ensure GLSurfaceView's onPause is called
        glSurfaceView.onPause();
    }
}
