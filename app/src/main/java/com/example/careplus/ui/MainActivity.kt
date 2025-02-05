import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.careplus.services.PusherService
import com.example.careplus.utils.NotificationHelper

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ... existing code ...

        // Initialize notification channel
        NotificationHelper.createNotificationChannel(this)

        // Start Pusher service
        Intent(this, PusherService::class.java).also { intent ->
            startService(intent)
        }
    }
}