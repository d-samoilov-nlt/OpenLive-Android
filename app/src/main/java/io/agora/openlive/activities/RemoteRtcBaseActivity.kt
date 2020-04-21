package io.agora.openlive.activities

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.Display
import android.view.SurfaceView
import com.google.android.gms.cast.CastPresentation
import io.agora.openlive.AgoraApplication
import io.agora.openlive.Constants
import io.agora.openlive.R
import io.agora.openlive.rtc.EngineConfig
import io.agora.openlive.rtc.EventHandler
import io.agora.openlive.stats.StatsManager
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration

abstract class RemoteRtcBaseActivity(context: Context, p1: Display?) : CastPresentation(context, p1), EventHandler {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AgoraApplication.registerEventHandler(this)
        configVideo()
    }


    private fun configVideo() {
        val configuration = VideoEncoderConfiguration(
                Constants.VIDEO_DIMENSIONS[AgoraApplication.engineConfig().videoDimenIndex],
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
        )
        configuration.mirrorMode = Constants.VIDEO_MIRROR_MODES[AgoraApplication.engineConfig().mirrorEncodeIndex]
        rtcEngine().setVideoEncoderConfiguration(configuration)
    }

    protected open fun prepareRtcVideo(uid: Int, local: Boolean): SurfaceView? {
        val surface = RtcEngine.CreateRendererView(context)
        if (local) {
            rtcEngine().setupLocalVideo(
                    VideoCanvas(
                            surface,
                            VideoCanvas.RENDER_MODE_HIDDEN,
                            0,
                            Constants.VIDEO_MIRROR_MODES[AgoraApplication.engineConfig().mirrorLocalIndex]
                    )
            )
        } else {
            rtcEngine().setupRemoteVideo(
                    VideoCanvas(
                            surface,
                            VideoCanvas.RENDER_MODE_HIDDEN,
                            uid,
                            Constants.VIDEO_MIRROR_MODES[AgoraApplication.engineConfig().mirrorRemoteIndex]
                    )
            )
        }
        return surface
    }

    protected open fun removeRtcVideo(uid: Int, local: Boolean) {
        if (local) {
            rtcEngine().setupLocalVideo(null)
        } else {
            rtcEngine().setupRemoteVideo(VideoCanvas(null, VideoCanvas.RENDER_MODE_HIDDEN, uid))
        }
    }

    open fun joinChannelAsCoach() {
        var token: String? = context.getString(R.string.agora_access_token)
        if (TextUtils.isEmpty(token) || TextUtils.equals(token, "#YOUR ACCESS TOKEN#")) {
            token = null // default, no token
        }
        rtcEngine().joinChannel(token, config().channelName, "", Constants.COACH_USER_ID)
    }


    protected open fun registerRtcEventHandler(handler: EventHandler?) {
        AgoraApplication.registerEventHandler(handler)
    }

    protected open fun removeRtcEventHandler(handler: EventHandler?) {
        AgoraApplication.removeEventHandler(handler)
    }

    protected open fun onDestroy() {
        AgoraApplication.removeEventHandler(this)
        rtcEngine().leaveChannel()
    }

    protected fun rtcEngine(): RtcEngine {
        return AgoraApplication.rtcEngine()
    }

    protected fun config(): EngineConfig {
        return AgoraApplication.engineConfig()
    }

    protected fun statsManager(): StatsManager {
        return AgoraApplication.statsManager()
    }
}