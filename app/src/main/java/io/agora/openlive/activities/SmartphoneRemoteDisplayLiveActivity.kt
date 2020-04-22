package io.agora.openlive.activities

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Chronometer
import android.widget.RelativeLayout
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import com.google.android.gms.cast.CastDevice
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.CastRemoteDisplayLocalService
import com.google.android.gms.cast.CastRemoteDisplayLocalService.NotificationSettings
import com.google.android.gms.common.api.Status
import io.agora.openlive.Constants
import io.agora.openlive.R
import io.agora.openlive.service.CastRemoteDisplayService
import kotlinx.android.synthetic.main.activity_smartphone_remote_display_live.*

const val INTENT_EXTRA_CAST_DEVICE = "cast_device"

class SmartphoneRemoteDisplayLiveActivity : RtcBaseActivity() {
    private val TAG = SmartphoneRemoteDisplayLiveActivity::class.java.simpleName

    private var chronometer: Chronometer? = null
    private var rlUserPreview: RelativeLayout? = null
    private var castDevice: CastDevice? = null
    private var mediaRouter: MediaRouter? = null
    private var mediaRouteSelector: MediaRouteSelector? = null
    private var castRemoteDisplayServiceIntent: Intent? = null
    private var castRemoteDisplayService: CastRemoteDisplayLocalService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smartphone_remote_display_live)
        setupCastConfig()

        chronometer = cm_smart_remote_display_timer
        rlUserPreview = rl_smart_remote_display_preview

        iv_smart_remote_display_timer_leave.setOnClickListener {
            closeSession()
        }
    }

    private fun closeSession() {
        if (castRemoteDisplayService != null) {
            castRemoteDisplayService!!.onDismissPresentation()
            castRemoteDisplayService!!.stopService(castRemoteDisplayServiceIntent)
        }
        mediaRouter!!.removeCallback(mMediaRouterCallback)
        finish()
    }

    private fun showSessionStartedMode() {
        chronometer!!.base = SystemClock.elapsedRealtime();
        chronometer!!.start()
        chronometer!!.visibility = View.VISIBLE
        tv_smart_remote_display_loading.visibility = View.GONE
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
        stopUserPreview()
        chronometer!!.stop()
    }

    private fun stopUserPreview() {
        rlUserPreview!!.removeAllViews()
    }

    private fun isRemoteDisplaying(): Boolean {
        return CastRemoteDisplayLocalService.getInstance() != null
    }

    private fun setupCastConfig() {
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

        mediaRouter = MediaRouter.getInstance(applicationContext)
        mediaRouteSelector = MediaRouteSelector.Builder()
                .addControlCategory(
                        CastMediaControlIntent.categoryForCast(getString(R.string.cast_app_id)))
                .build()
        mediaRouter!!.addCallback(mediaRouteSelector!!, mMediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY)

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
        castRemoteDisplayServiceIntent = Intent(
                this@SmartphoneRemoteDisplayLiveActivity,
                SmartphoneRemoteDisplayLiveActivity::class.java)

        castRemoteDisplayServiceIntent!!.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val notificationPendingIntent = PendingIntent.getActivity(
                this@SmartphoneRemoteDisplayLiveActivity, 0, castRemoteDisplayServiceIntent, 0)
        val settings = NotificationSettings.Builder()
                .setNotificationPendingIntent(notificationPendingIntent).build()

        CastRemoteDisplayLocalService.startService(
                this@SmartphoneRemoteDisplayLiveActivity,
                CastRemoteDisplayService::class.java,
                getString(R.string.cast_app_id),
                castDevice,
                settings,
                object : CastRemoteDisplayLocalService.Callbacks {
                    override fun onServiceCreated(service: CastRemoteDisplayLocalService) {
                        Log.d(TAG, "onServiceCreated")
                    }

                    override fun onRemoteDisplaySessionEnded(p0: CastRemoteDisplayLocalService?) {
                        closeSession()
                    }

                    override fun onRemoteDisplaySessionStarted(service: CastRemoteDisplayLocalService) {
                        castRemoteDisplayService = service
                        showSessionStartedMode()
                    }

                    override fun onRemoteDisplaySessionError(errorReason: Status) {
                        val code = errorReason.statusCode
                        Log.d(TAG, "onServiceError: $code")

                    }
                })
    }
}
