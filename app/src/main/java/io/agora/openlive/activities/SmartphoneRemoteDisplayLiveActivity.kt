package io.agora.openlive.activities

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.SurfaceView
import android.widget.Chronometer
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import com.google.android.gms.cast.CastDevice
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.CastRemoteDisplayLocalService
import com.google.android.gms.cast.CastRemoteDisplayLocalService.NotificationSettings
import com.google.android.gms.common.api.Status
import io.agora.openlive.R
import io.agora.openlive.service.CastRemoteDisplayService
import kotlinx.android.synthetic.main.activity_smartphone_remote_display_live.*

const val INTENT_EXTRA_CAST_DEVICE = "cast_device"

class SmartphoneRemoteDisplayLiveActivity : RtcBaseActivity() {
    private val TAG = SmartphoneRemoteDisplayLiveActivity::class.java.simpleName

    private var chronometer: Chronometer? = null
    private var castDevice: CastDevice? = null
    private var mediaRouter: MediaRouter? = null
    private var mediaRouteSelector: MediaRouteSelector? = null
    private var castRemoteDisplayServiceIntent: Intent? = null

    override fun getSurfaceView(): SurfaceView = sfv_smart_remote_display_preview

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smartphone_remote_display_live)
        setupCastConfig()

        chronometer = cm_smart_remote_display_timer
        chronometer!!.base = SystemClock.elapsedRealtime();
        chronometer!!.start()

        iv_smart_remote_display_timer_leave.setOnClickListener {
            closeSession()
        }
    }

    private fun closeSession() {
        if (castRemoteDisplayServiceIntent != null) {
            stopService(castRemoteDisplayServiceIntent)
        }
        mediaRouter!!.removeCallback(mMediaRouterCallback)
        finish()
    }

    override fun onResume() {
        super.onResume()
        if (!isRemoteDisplaying()) {
            if (castDevice != null) {
                startCastService(castDevice!!)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        chronometer!!.stop()
        closeSession()
    }

    private fun isRemoteDisplaying(): Boolean {
        return CastRemoteDisplayLocalService.getInstance() != null
    }

    private fun setupCastConfig() {
        castRemoteDisplayServiceIntent = Intent(
                this@SmartphoneRemoteDisplayLiveActivity,
                SmartphoneRemoteDisplayLiveActivity::class.java)

        if (isRemoteDisplaying()) {
            val castDevice = CastDevice
                    .getFromBundle(mediaRouter!!.selectedRoute.extras)
            this.castDevice = castDevice

        } else {
            val extras = intent.extras
            if (extras != null) {
                castDevice = extras.getParcelable(INTENT_EXTRA_CAST_DEVICE)
            }
        }

        mediaRouter!!.addCallback(mediaRouteSelector!!, mMediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY)
        mediaRouter = MediaRouter.getInstance(applicationContext)
        mediaRouteSelector = MediaRouteSelector.Builder()
                .addControlCategory(
                        CastMediaControlIntent.categoryForCast(getString(R.string.cast_app_id)))
                .build()
    }

    private val mMediaRouterCallback: MediaRouter.Callback = object : MediaRouter.Callback() {
        override fun onRouteSelected(router: MediaRouter?, info: MediaRouter.RouteInfo?) {
        }

        override fun onRouteUnselected(router: MediaRouter?, info: MediaRouter.RouteInfo?) {
            if (isRemoteDisplaying()) {
                CastRemoteDisplayLocalService.stopService()
            }
            castDevice = null
        }
    }

    private fun startCastService(castDevice: CastDevice) {

        castRemoteDisplayServiceIntent!!.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val notificationPendingIntent = PendingIntent.getActivity(
                this@SmartphoneRemoteDisplayLiveActivity, 0, castRemoteDisplayServiceIntent, 0)
        val settings = NotificationSettings.Builder()
                .setNotificationPendingIntent(notificationPendingIntent).build()

        CastRemoteDisplayLocalService.startService(
                this@SmartphoneRemoteDisplayLiveActivity,
                CastRemoteDisplayService::class.java, getString(R.string.cast_app_id),
                castDevice, settings,
                object : CastRemoteDisplayLocalService.Callbacks {
                    override fun onServiceCreated(
                            service: CastRemoteDisplayLocalService) {
                        Log.d(TAG, "onServiceCreated")
                    }

                    override fun onRemoteDisplaySessionEnded(p0: CastRemoteDisplayLocalService?) {
                        Log.d(TAG, "onRemoteDisplaySessionEnded")
                    }

                    override fun onRemoteDisplaySessionStarted(
                            service: CastRemoteDisplayLocalService) {
                        Log.d(TAG, "onServiceStarted")
                    }

                    override fun onRemoteDisplaySessionError(errorReason: Status) {
                        val code = errorReason.statusCode
                        Log.d(TAG, "onServiceError: $code")

                    }
                })
    }
}
