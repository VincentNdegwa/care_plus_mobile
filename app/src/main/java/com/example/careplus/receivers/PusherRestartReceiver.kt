import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.careplus.data.SessionManager
import com.example.careplus.services.PusherService

class PusherRestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.example.careplus.RestartPusher") {
            val sessionManager = SessionManager(context)
            if (sessionManager.isLoggedIn()) {
                val serviceIntent = Intent(context, PusherService::class.java)
                serviceIntent.putExtra(PusherService.EXTRA_PATIENT_ID, 
                    sessionManager.getUser()?.patient?.id?.toString())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            }
        }
    }
} 