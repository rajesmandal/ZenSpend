package com.android.zenspend

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.android.zenspend.core.datastore.ThemeMode
import com.android.zenspend.core.datastore.ThemePreferences
import com.android.zenspend.core.household.InviteLinkHolder
import com.android.zenspend.core.navigation.Destination
import com.android.zenspend.core.navigation.Navigator
import com.android.zenspend.core.ui.theme.ZenSpendTheme
import com.android.zenspend.feature.main.presentation.ZenSpendApp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var inviteLinkHolder: InviteLinkHolder

    @Inject
    lateinit var themePreferences: ThemePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleInviteLink(intent)
        setContent {
            val themeMode by themePreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            // Keep the status / navigation bar icons readable on the chosen theme.
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme }
                )
                onDispose {}
            }
            ZenSpendTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ZenSpendApp(navigator = navigator)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleInviteLink(intent)
    }

    private fun handleInviteLink(intent: Intent?) {
        val link = intent?.data ?: return
        if (link.scheme != "zenspend" || link.host != "join") return
        link.getQueryParameter("token")?.takeIf { it.isNotBlank() }?.let { token ->
            inviteLinkHolder.offer(token)
            navigator.navigate(Destination.SharedCashbook)
        }
    }
}
