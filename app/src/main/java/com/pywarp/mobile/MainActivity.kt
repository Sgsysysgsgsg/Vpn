package com.pywarp.mobile

import android.app.Activity
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF090B16)
private val Panel = Color(0xFF151A2B)
private val Violet = Color(0xFF8B5CF6)
private val Cyan = Color(0xFF38BDF8)

class MainActivity : ComponentActivity() {
    private var permissionResult by mutableStateOf("VPN engine not configured")

    private val vpnPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        permissionResult = if (result.resultCode == Activity.RESULT_OK) {
            "Permission granted — tunnel engine is the next integration step"
        } else "VPN permission was not granted"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PyWarpTheme {
                Dashboard(
                    status = permissionResult,
                    onPrepare = {
                        val intent = VpnService.prepare(this)
                        if (intent == null) permissionResult = "Permission already granted — engine not configured"
                        else vpnPermission.launch(intent)
                    }
                )
            }
        }
    }
}

@Composable
private fun PyWarpTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme(
        background = Ink, surface = Panel, primary = Violet,
        secondary = Cyan, onBackground = Color.White, onSurface = Color.White
    ), content = content)
}

@Composable
private fun Dashboard(status: String, onPrepare: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = Ink) {
        Column(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color(0xFF17132D), Ink, Color(0xFF071725)))
            ).padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.Bolt, contentDescription = null, tint = Cyan, modifier = Modifier.size(30.dp))
                Spacer(Modifier.width(8.dp))
                Text("PyWarp", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.weight(1f))
                Icon(Icons.Rounded.Settings, contentDescription = "Settings", tint = Color.LightGray)
            }
            Spacer(Modifier.height(54.dp))
            Text("YOUR CONNECTION", color = Color(0xFF9CA3B8), fontSize = 12.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier.size(218.dp).background(
                    Brush.radialGradient(listOf(Color(0x668B5CF6), Color(0x2238BDF8), Color.Transparent)),
                    CircleShape
                ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier.size(164.dp).background(
                        Brush.linearGradient(listOf(Color(0xFF29204A), Color(0xFF14283A))),
                        CircleShape
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Bolt, contentDescription = null, tint = Violet, modifier = Modifier.size(76.dp))
                }
            }
            Spacer(Modifier.height(25.dp))
            Text("Not connected", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
            Text("WARP tunnel engine is not integrated yet", color = Color(0xFF9CA3B8), fontSize = 13.sp)
            Spacer(Modifier.height(34.dp))
            Button(
                onClick = onPrepare,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Violet)
            ) { Text("Prepare VPN permission", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            Spacer(Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Panel),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("ENGINE STATUS", color = Cyan, fontSize = 11.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(status, color = Color(0xFFD6D8E3), fontSize = 14.sp)
                }
            }
            Spacer(Modifier.weight(1f))
            Text("PRIVATE NETWORK • ANDROID", color = Color(0xFF697187), fontSize = 10.sp, letterSpacing = 1.4.sp)
            Text("Version 0.1.0 · Preview", color = Color(0xFF697187), fontSize = 11.sp)
        }
    }
}
