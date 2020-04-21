package io.agora.openlive.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import io.agora.openlive.R;
import io.agora.rtc.Constants;

import static io.agora.rtc.Constants.CLIENT_ROLE_BROADCASTER;
import static io.agora.rtc.IRtcEngineEventHandler.ClientRole.CLIENT_ROLE_AUDIENCE;

public class RoleActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_role);
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        RelativeLayout layout = findViewById(R.id.role_title_layout);
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) layout.getLayoutParams();
        params.height += mStatusBarHeight;
        layout.setLayoutParams(params);

        layout = findViewById(R.id.role_content_layout);
        params = (RelativeLayout.LayoutParams) layout.getLayoutParams();
        params.topMargin = (mDisplayMetrics.heightPixels -
                layout.getMeasuredHeight()) * 3 / 7;
        layout.setLayoutParams(params);
    }

    public void onJoinAsBroadcaster(View view) {
        gotoLiveActivity(CLIENT_ROLE_BROADCASTER);
    }

    public void onJoinAsAudience(View view) {
        gotoLiveActivity(Constants.CLIENT_ROLE_AUDIENCE);
    }

    private void gotoLiveActivity(int role) {
        Intent intent = new Intent(getIntent());
        if (role == CLIENT_ROLE_AUDIENCE) {
            intent.setClass(getApplicationContext(), LiveActivity.class);
        } else if (role == CLIENT_ROLE_BROADCASTER) {
            intent.setClass(getApplicationContext(), CoachLiveTypeActivity.class);
        }
        startActivity(intent);
    }

    public void onBackArrowPressed(View view) {
        finish();
    }
}
