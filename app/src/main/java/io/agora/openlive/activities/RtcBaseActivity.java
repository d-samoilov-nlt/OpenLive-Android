package io.agora.openlive.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.SurfaceView;

import io.agora.openlive.Constants;
import io.agora.openlive.R;
import io.agora.openlive.rtc.EventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

import static io.agora.openlive.Constants.COACH_USER_ID;

public abstract class RtcBaseActivity extends BaseActivity implements EventHandler {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerRtcEventHandler(this);
        configVideo();
    }

    private void configVideo() {
        VideoEncoderConfiguration configuration = new VideoEncoderConfiguration(
                Constants.VIDEO_DIMENSIONS[config().getVideoDimenIndex()],
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
        );
        configuration.mirrorMode = Constants.VIDEO_MIRROR_MODES[config().getMirrorEncodeIndex()];
        rtcEngine().setVideoEncoderConfiguration(configuration);
    }

    public void joinChannelAsStudent() {
        // Initialize token, extra info here before joining channel
        // 1. Users can only see each other after they join the
        // same channel successfully using the same app id.
        // 2. One token is only valid for the channel name and uid that
        // you use to generate this token.
//        String token = getString(R.string.agora_access_token);
//        if (TextUtils.isEmpty(token.trim()) || TextUtils.equals(token, "#YOUR ACCESS TOKEN#")) {
//            token = null; // default, no token
//        }
        rtcEngine().joinChannel(null, config().getChannelName(), "", 0);
    }

    public void joinChannelAsCoach() {
        // Initialize token, extra info here before joining channel
        // 1. Users can only see each other after they join the
        // same channel successfully using the same app id.
        // 2. One token is only valid for the channel name and uid that
        // you use to generate this token.
//        String token = getString(R.string.agora_access_token);
//        if (TextUtils.isEmpty(token.trim()) || TextUtils.equals(token, "#YOUR ACCESS TOKEN#")) {
//            token = null; // default, no token
//        }
        rtcEngine().joinChannel(null, config().getChannelName(), "", COACH_USER_ID);
    }

    protected SurfaceView prepareRtcVideo(int uid, boolean local) {
        SurfaceView surface = RtcEngine.CreateRendererView(getApplicationContext());
        if (local) {
            rtcEngine().setupLocalVideo(
                    new VideoCanvas(
                            surface,
                            VideoCanvas.RENDER_MODE_HIDDEN,
                            0,
                            Constants.VIDEO_MIRROR_MODES[config().getMirrorLocalIndex()]
                    )
            );
        } else {
            rtcEngine().setupRemoteVideo(
                    new VideoCanvas(
                            surface,
                            VideoCanvas.RENDER_MODE_HIDDEN,
                            uid,
                            Constants.VIDEO_MIRROR_MODES[config().getMirrorRemoteIndex()]
                    )
            );
        }
        return surface;
    }

    protected void removeRtcVideo(int uid, boolean local) {
        if (local) {
            rtcEngine().setupLocalVideo(null);
        } else {
            rtcEngine().setupRemoteVideo(new VideoCanvas(null, VideoCanvas.RENDER_MODE_HIDDEN, uid));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeRtcEventHandler(this);
        rtcEngine().leaveChannel();
    }
}
