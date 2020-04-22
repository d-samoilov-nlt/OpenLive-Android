package io.agora.openlive.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.mediarouter.app.MediaRouteButton;
import androidx.mediarouter.media.MediaRouteSelector;
import androidx.mediarouter.media.MediaRouter;

import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import io.agora.openlive.R;
import io.agora.openlive.ui.MediaRouterButtonView;

import static io.agora.openlive.Constants.COACH_USER_ID;
import static io.agora.openlive.activities.SmartphoneRemoteDisplayLiveActivityKt.INTENT_EXTRA_CAST_DEVICE;
import static io.agora.rtc.Constants.CLIENT_ROLE_BROADCASTER;

public class CoachLiveTypeActivity extends RtcBaseActivity {

    private static final String TAG = "MainActivity";

    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private MediaRouteButton mMediaRouteButton;
    private MediaRouterButtonView mMediaRouterButtonView;
    private int mRouteCount = 0;

    private RelativeLayout rlUserPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkGooglePlayServices();
        setContentView(R.layout.activity_coach_live_type);

        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(
                        CastMediaControlIntent.categoryForCast(getString(R.string.cast_app_id)))
                .build();
        mMediaRouter = MediaRouter.getInstance(getApplicationContext());

        mMediaRouterButtonView = (MediaRouterButtonView) findViewById(R.id.mrb_coach_live_type_tv_cast);
        if (mMediaRouterButtonView != null) {
            mMediaRouteButton = mMediaRouterButtonView.getMediaRouteButton();
            mMediaRouteButton.setRouteSelector(mMediaRouteSelector);
        }

        rlUserPreview = findViewById(R.id.rl_coach_live_type_preview);

        TextView useSmartphoneTv = findViewById(R.id.tv_coach_live_type_smartphone);
        useSmartphoneTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CoachLiveTypeActivity.this, LiveActivity.class);
                intent.putExtra(io.agora.openlive.Constants.KEY_CLIENT_ROLE, CLIENT_ROLE_BROADCASTER);
                startActivity(intent);
            }
        });
        joinChannelAsCoach();
    }

    private void startUserPreview() {
        rtcEngine().setClientRole(CLIENT_ROLE_BROADCASTER);
        rlUserPreview.addView(prepareRtcVideo(COACH_USER_ID, true));
    }

    @Override
    protected void onStart() {
        super.onStart();
        startUserPreview();
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
    }

    @Override
    protected void onStop() {
        super.onStop();
        rlUserPreview.removeAllViews();
        mMediaRouter.removeCallback(mMediaRouterCallback);
    }

    private final MediaRouter.Callback mMediaRouterCallback =
            new MediaRouter.Callback() {
                @Override
                public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo route) {
                    if (++mRouteCount == 1) {
                        // Show the button when a device is discovered.
                        if (mMediaRouterButtonView != null) {
                            mMediaRouterButtonView.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onRouteRemoved(MediaRouter router, MediaRouter.RouteInfo route) {
                    if (--mRouteCount == 0) {
                        // Hide the button if there are no devices discovered.
                        if (mMediaRouterButtonView != null) {
                            mMediaRouterButtonView.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo info) {
                    Log.d(TAG, "onRouteSelected");
                    CastDevice castDevice = CastDevice.getFromBundle(info.getExtras());
                    if (castDevice != null) {
                        Intent intent = new Intent(CoachLiveTypeActivity.this, SmartphoneRemoteDisplayLiveActivity.class);
                        intent.putExtra(INTENT_EXTRA_CAST_DEVICE, castDevice);
                        startActivity(intent);
                    }
                }

                @Override
                public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo info) {
                }
            };

    /**
     * A utility method to validate that the appropriate version of the Google Play Services is
     * available on the device. If not, it will open a dialog to address the issue. The dialog
     * displays a localized message about the error and upon user confirmation (by tapping on
     * dialog) will direct them to the Play Store if Google Play services is out of date or
     * missing, or to system settings if Google Play services is disabled on the device.
     */
    private boolean checkGooglePlayServices() {
        int googlePlayServicesCheck = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (googlePlayServicesCheck == ConnectionResult.SUCCESS) {
            return true;
        }
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(googlePlayServicesCheck, this, 0);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                finish();
            }
        });
        dialog.show();
        return false;
    }

}