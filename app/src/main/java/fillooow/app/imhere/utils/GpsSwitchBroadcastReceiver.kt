package fillooow.app.imhere.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager

class GpsSwitchBroadcastReceiver(

    private val onGPSEnabledAction: () -> Unit,
    private val onGPSDisabledAction: () -> Unit

) : BroadcastReceiver() {

    private var isConnectionFailed = false

    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action!!.matches(Regex("android.location.PROVIDERS_CHANGED"))) {

            val locationManager: LocationManager =
                context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

            when (isGpsEnabled) {

                true -> {

                    onGPSEnabledAction.invoke()
                    isConnectionFailed = false
                }
                false -> {
                    
                    if (isConnectionFailed.not()) {
                        onGPSDisabledAction.invoke()
                    }
                    isConnectionFailed = true
                }
            }
        }
    }
}