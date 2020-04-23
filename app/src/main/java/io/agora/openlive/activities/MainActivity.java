package io.agora.openlive.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import io.agora.openlive.R;

import static io.agora.openlive.Constants.TEST_CHANNEL_NAME;

public class MainActivity extends BaseActivity {
    private static final int PERMISSION_REQ_CODE = 1 << 4;

    private String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private Rect mVisibleRect = new Rect();
    private int mLastVisibleHeight = 0;
    private EditText mTopicEdit;
    private TextView mStartBtn;

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // Do nothing
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // Do nothing
        }

        @Override
        public void afterTextChanged(Editable editable) {
            mStartBtn.setEnabled(!TextUtils.isEmpty(editable));
        }
    };

    private ViewTreeObserver.OnGlobalLayoutListener mLayoutObserverListener =
            new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    checkInputMethodWindowState();
                }
            };

    private void checkInputMethodWindowState() {
        getWindow().getDecorView().getRootView().getWindowVisibleDisplayFrame(mVisibleRect);
        int visibleHeight = mVisibleRect.bottom - mVisibleRect.top;
        if (visibleHeight == mLastVisibleHeight) return;

        mLastVisibleHeight = visibleHeight;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
    }

    private void initUI() {
        mTopicEdit = findViewById(R.id.topic_edit);
        mTopicEdit.setText(TEST_CHANNEL_NAME);
        mTopicEdit.addTextChangedListener(mTextWatcher);

        mStartBtn = findViewById(R.id.start_broadcast_button);
        if (TextUtils.isEmpty(mTopicEdit.getText())) mStartBtn.setEnabled(false);
    }

    public void onSettingClicked(View view) {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    public void onStartBroadcastClicked(View view) {
        checkPermission();
    }

    private void checkPermission() {
        boolean granted = true;
        for (String per : PERMISSIONS) {
            if (!permissionGranted(per)) {
                granted = false;
                break;
            }
        }

        if (granted) {
            resetLayoutAndForward();
        } else {
            requestPermissions();
        }
    }

    private boolean permissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(
                this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQ_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQ_CODE) {
            boolean granted = true;
            for (int result : grantResults) {
                granted = (result == PackageManager.PERMISSION_GRANTED);
                if (!granted) break;
            }

            if (granted) {
                resetLayoutAndForward();
            } else {
                toastNeedPermissions();
            }
        }
    }

    private void resetLayoutAndForward() {
        closeImeDialogIfNeeded();
        gotoRoleActivity();
    }

    private void closeImeDialogIfNeeded() {
        InputMethodManager manager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(mTopicEdit.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void gotoRoleActivity() {
        Intent intent = new Intent(MainActivity.this, RoleActivity.class);
        String room = mTopicEdit.getText().toString();
        config().setChannelName(room);
        startActivity(intent);
    }

    private void toastNeedPermissions() {
        Toast.makeText(this, R.string.need_necessary_permissions, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetUI();
        registerLayoutObserverForSoftKeyboard();
    }

    private void resetUI() {
        closeImeDialogIfNeeded();
    }

    private void registerLayoutObserverForSoftKeyboard() {
        View view = getWindow().getDecorView().getRootView();
        ViewTreeObserver observer = view.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(mLayoutObserverListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeLayoutObserverForSoftKeyboard();
    }

    private void removeLayoutObserverForSoftKeyboard() {
        View view = getWindow().getDecorView().getRootView();
        view.getViewTreeObserver().removeOnGlobalLayoutListener(mLayoutObserverListener);
    }
}
