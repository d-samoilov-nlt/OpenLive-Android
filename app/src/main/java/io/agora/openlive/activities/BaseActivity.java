package io.agora.openlive.activities;

import android.os.Bundle;
import android.util.DisplayMetrics;

import androidx.appcompat.app.AppCompatActivity;

import io.agora.openlive.AgoraApplication;
import io.agora.openlive.rtc.EngineConfig;
import io.agora.openlive.rtc.EventHandler;
import io.agora.openlive.stats.StatsManager;
import io.agora.openlive.utils.WindowUtil;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;

public abstract class BaseActivity extends AppCompatActivity implements EventHandler {
    protected DisplayMetrics mDisplayMetrics = new DisplayMetrics();
    protected int mStatusBarHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowUtil.hideWindowStatusBar(getWindow());
        getDisplayMetrics();
        initStatusBarHeight();
    }

    private void getDisplayMetrics() {
        getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
    }

    private void initStatusBarHeight() {
        mStatusBarHeight = WindowUtil.getSystemStatusBarHeight(this);
    }

    protected AgoraApplication application() {
        return (AgoraApplication) getApplication();
    }

    protected RtcEngine rtcEngine() {
        return application().rtcEngine();
    }

    protected EngineConfig config() {
        return application().engineConfig();
    }

    protected StatsManager statsManager() {
        return application().statsManager();
    }

    protected void registerRtcEventHandler(EventHandler handler) {
        application().registerEventHandler(handler);
    }

    protected void removeRtcEventHandler(EventHandler handler) {
        application().removeEventHandler(handler);
    }

    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
        // Do nothing at the moment
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        // Do nothing at the moment
    }

    @Override
    public void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats) {
        // Do nothing at the moment
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        // Do nothing at the moment
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        // Do nothing at the moment
    }

    @Override
    public void onLastmileQuality(final int quality) {
        // Do nothing at the moment
    }

    @Override
    public void onLastmileProbeResult(final IRtcEngineEventHandler.LastmileProbeResult result) {
        // Do nothing at the moment
    }

    @Override
    public void onLocalVideoStats(IRtcEngineEventHandler.LocalVideoStats stats) {
        // Do nothing at the moment
    }

    @Override
    public void onRtcStats(IRtcEngineEventHandler.RtcStats stats) {
        // Do nothing at the moment
    }

    @Override
    public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
        // Do nothing at the moment
    }

    @Override
    public void onRemoteVideoStats(IRtcEngineEventHandler.RemoteVideoStats stats) {
        // Do nothing at the moment
    }

    @Override
    public void onRemoteAudioStats(IRtcEngineEventHandler.RemoteAudioStats stats) {
        // Do nothing at the moment
    }
}
