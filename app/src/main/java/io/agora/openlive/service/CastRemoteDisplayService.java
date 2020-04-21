package io.agora.openlive.service;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.cast.CastPresentation;
import com.google.android.gms.cast.CastRemoteDisplayLocalService;

import io.agora.openlive.R;
import io.agora.openlive.activities.RemoteRtcBaseActivity;
import io.agora.openlive.stats.LocalStatsData;
import io.agora.openlive.stats.RemoteStatsData;
import io.agora.openlive.stats.StatsData;
import io.agora.openlive.ui.VideoGridContainer;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.video.VideoEncoderConfiguration;

import static io.agora.rtc.Constants.CLIENT_ROLE_BROADCASTER;

public class CastRemoteDisplayService extends CastRemoteDisplayLocalService {
    private static final String TAG = "CastRemoteDisplayServ";


    private CastPresentation mPresentation;

    @Override
    public void onCreatePresentation(Display display) {
        createPresentation(display);
    }

    @Override
    public void onDismissPresentation() {
        dismissPresentation();
    }

    private void dismissPresentation() {
        if (mPresentation != null) {
            mPresentation.dismiss();
            mPresentation = null;
        }
    }

    private void createPresentation(Display display) {
        dismissPresentation();
        mPresentation = new FirstScreenPresentation(this, display);

        try {
            mPresentation.show();
        } catch (WindowManager.InvalidDisplayException ex) {
            Log.e(TAG, "Unable to show presentation, display was removed.", ex);
            dismissPresentation();
        }
    }

    private static class FirstScreenPresentation extends RemoteRtcBaseActivity {
        private final Handler handler;
        private final String TAG = "FirstScreenPresentation";

        private VideoGridContainer mVideoGridContainer;
        private VideoEncoderConfiguration.VideoDimensions mVideoDimension;

        public FirstScreenPresentation(Context context, Display display) {
            super(context, display);
            handler = new Handler(Looper.getMainLooper());
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.layout_remote_screen);
            initUI();
            initData();
        }

        private void initUI() {
            mVideoGridContainer = findViewById(R.id.vgv_remote_live_video_grid);
            mVideoGridContainer.setStatsManager(statsManager());

            rtcEngine().setClientRole(CLIENT_ROLE_BROADCASTER);
            startBroadcast();
            joinChannelAsCoach();
        }

        private void initData() {
            mVideoDimension = io.agora.openlive.Constants.VIDEO_DIMENSIONS[
                    config().getVideoDimenIndex()];
        }

        private void startBroadcast() {
            rtcEngine().setClientRole(CLIENT_ROLE_BROADCASTER);
        }

        private void stopBroadcast() {
            rtcEngine().setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            // Do nothing at the moment

        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            // Do nothing at the moment
        }

        @Override
        public void onLastmileQuality(int quality) {

        }

        @Override
        public void onLastmileProbeResult(IRtcEngineEventHandler.LastmileProbeResult result) {

        }

        @Override
        public void onUserOffline(final int uid, int reason) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    removeRemoteUser(uid);
                }
            });
        }

        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    renderRemoteUser(uid);
                }
            });
        }

        @Override
        public void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats) {

        }

        private void renderRemoteUser(int uid) {
            SurfaceView surface = prepareRtcVideo(uid, false);
            mVideoGridContainer.addUserVideoSurface(uid, surface, false);
        }

        private void removeRemoteUser(int uid) {
            if (uid == io.agora.openlive.Constants.COACH_USER_ID) {
                closeSession();
            } else {
                removeRtcVideo(uid, false);
                mVideoGridContainer.removeUserVideo(uid, false);
            }
        }

        private void closeSession() {
            removeRtcEventHandler(this);
            rtcEngine().leaveChannel();
        }

        @Override
        public void onLocalVideoStats(IRtcEngineEventHandler.LocalVideoStats stats) {
            if (!statsManager().isEnabled()) return;

            LocalStatsData data = (LocalStatsData) statsManager().getStatsData(0);
            if (data == null) return;

            data.setWidth(mVideoDimension.width);
            data.setHeight(mVideoDimension.height);
            data.setFramerate(stats.sentFrameRate);
        }

        @Override
        public void onRtcStats(IRtcEngineEventHandler.RtcStats stats) {
            if (!statsManager().isEnabled()) return;

            LocalStatsData data = (LocalStatsData) statsManager().getStatsData(0);
            if (data == null) return;

            data.setLastMileDelay(stats.lastmileDelay);
            data.setVideoSendBitrate(stats.txVideoKBitRate);
            data.setVideoRecvBitrate(stats.rxVideoKBitRate);
            data.setAudioSendBitrate(stats.txAudioKBitRate);
            data.setAudioRecvBitrate(stats.rxAudioKBitRate);
            data.setCpuApp(stats.cpuAppUsage);
            data.setCpuTotal(stats.cpuAppUsage);
            data.setSendLoss(stats.txPacketLossRate);
            data.setRecvLoss(stats.rxPacketLossRate);
        }

        @Override
        public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
            if (!statsManager().isEnabled()) return;

            StatsData data = statsManager().getStatsData(uid);
            if (data == null) return;

            data.setSendQuality(statsManager().qualityToString(txQuality));
            data.setRecvQuality(statsManager().qualityToString(rxQuality));
        }

        @Override
        public void onRemoteVideoStats(IRtcEngineEventHandler.RemoteVideoStats stats) {
            if (!statsManager().isEnabled()) return;

            RemoteStatsData data = (RemoteStatsData) statsManager().getStatsData(stats.uid);
            if (data == null) return;

            data.setWidth(stats.width);
            data.setHeight(stats.height);
            data.setFramerate(stats.rendererOutputFrameRate);
            data.setVideoDelay(stats.delay);
        }

        @Override
        public void onRemoteAudioStats(IRtcEngineEventHandler.RemoteAudioStats stats) {
            if (!statsManager().isEnabled()) return;

            RemoteStatsData data = (RemoteStatsData) statsManager().getStatsData(stats.uid);
            if (data == null) return;

            data.setAudioNetDelay(stats.networkTransportDelay);
            data.setAudioNetJitter(stats.jitterBufferDelay);
            data.setAudioLoss(stats.audioLossRate);
            data.setAudioQuality(statsManager().qualityToString(stats.quality));
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            statsManager().clearAllData();
        }

    }

}
