package com.pywarp.mobile

import android.app.Activity
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private val Ink = Color(0xFF090B16)
private val Panel = Color(0xFF151A2B)
private val Violet = Color(0xFF8B5CF6)
private val Cyan = Color(0xFF38BDF8)
private const val OFFICIAL_WARP_PLAY_URL =
    "https://play.google.com/store/apps/details?id=com.cloudflare.onedotonedotonedotone"
private const val OFFICIAL_WARP_PACKAGE = "com.cloudflare.onedotonedotonedotone"

class MainActivity : ComponentActivity() {
    private var status by mutableStateOf("Import a WireGuard configuration to begin.")
    private var configured by mutableStateOf(false)
    private var connected by mutableStateOf(false)
    private val configFile by lazy { File(filesDir, "wireguard.conf") }

    private val importConfig = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            lifecycleScope.launch {
                status = "Validating configuration…"
                try {
                    val bytes = withContext(Dispatchers.IO) {
                        contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            ?: error("Unable to read selected file.")
                    }
                    withContext(Dispatchers.IO) {
                        com.wireguard.config.Config.parse(bytes.inputStream())
                        configFile.writeBytes(bytes)
                    }
                    configured = true
                    status = "Configuration imported and validated. Ready to connect."
                } catch (error: Exception) {
                    configured = configFile.exists()
                    status = "Import failed: ${error.message ?: error.javaClass.simpleName}"
                }
            }
        }
    }

    private val vpnPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) connectTunnel()
        else status = "VPN permission was denied."
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configured = configFile.exists()
        connected = WireGuardEngine.isConnected()
        if (connected) status = "WireGuard tunnel interface is active (backend state UP)."
        setContent {
            PyWarpTheme {
                Dashboard(
                    status = status,
                    configured = configured,
                    connected = connected,
                    onImport = { importConfig.launch(arrayOf("text/plain", "application/octet-stream", "*/*")) },
                    onConnect = {
                        val permissionIntent = VpnService.prepare(this)
                        if (permissionIntent == null) connectTunnel()
                        else vpnPermission.launch(permissionIntent)
                    },
                    onDisconnect = { disconnectTunnel() },
                    onOpenOfficialWarp = { openOfficialWarp() }
                )
            }
        }
    }

    private fun openOfficialWarp() {
        val launchIntent = packageManager.getLaunchIntentForPackage(OFFICIAL_WARP_PACKAGE)
        if (launchIntent != null) {
            startActivity(launchIntent)
        } else {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(OFFICIAL_WARP_PLAY_URL)))
        }
    }

    private fun connectTunnel() {
        if (!configFile.exists()) {
            status = "Import a WireGuard .conf file first."
            configured = false
            return
        }
        lifecycleScope.launch {
            status = "Starting WireGuard userspace tunnel…"
            try {
                withContext(Dispatchers.IO) {
                    WireGuardEngine.connect(this@MainActivity, configFile.readBytes())
                }
                connected = WireGuardEngine.isConnected()
                status = if (connected) "Tunnel interface is UP. Peer handshake and end-to-end internet reachability are not yet verified." else "Tunnel did not enter UP state."
            } catch (error: Exception) {
                connected = false
                status = "Connection failed: ${error.message ?: error.javaClass.simpleName}"
            }
        }
    }

    private fun disconnectTunnel() {
        lifecycleScope.launch {
            status = "Stopping tunnel…"
            try {
                withContext(Dispatchers.IO) { WireGuardEngine.disconnect() }
                connected = WireGuardEngine.isConnected()
                status = if (!connected) "Tunnel stopped." else "Tunnel is still active."
            } catch (error: Exception) {
                status = "Disconnect failed: ${error.message ?: error.javaClass.simpleName}"
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
private fun Dashboard(
    status: String,
    configured: Boolean,
    connected: Boolean,
    onImport: () -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onOpenOfficialWarp: () -> Unit
) {
    var showSettings by remember { mutableStateOf(false) }

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
                IconButton(onClick = { showSettings = true }) {
                    Icon(Icons.Rounded.Settings, contentDescription = "Open settings", tint = Color.LightGray)
                }
            }
            Spacer(Modifier.height(38.dp))
            Text("WIREGUARD CONNECTION", color = Color(0xFF9CA3B8), fontSize = 12.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier.size(190.dp).background(
                    Brush.radialGradient(listOf(Color(0x668B5CF6), Color(0x2238BDF8), Color.Transparent)),
                    CircleShape
                ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier.size(146.dp).background(
                        Brush.linearGradient(listOf(Color(0xFF29204A), Color(0xFF14283A))),
                        CircleShape
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Bolt, contentDescription = null,
                        tint = if (connected) Cyan else Violet, modifier = Modifier.size(68.dp))
                }
            }
            Spacer(Modifier.height(22.dp))
            Text(if (connected) "Tunnel active" else "Not connected",
                color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.SemiBold)
            Text(if (configured) "WireGuard configuration ready" else "No configuration imported",
                color = Color(0xFF9CA3B8), fontSize = 13.sp)
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = onImport,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Panel)
            ) {
                Icon(Icons.Rounded.FolderOpen, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (configured) "Replace WireGuard config" else "Import WireGuard .conf")
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = if (connected) onDisconnect else onConnect,
                enabled = configured || connected,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (connected) Color(0xFFB45353) else Violet)
            ) {
                Text(if (connected) "Disconnect" else "Connect", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(18.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Panel),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("ENGINE STATUS", color = Cyan, fontSize = 11.sp,
                        letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(status, color = Color(0xFFD6D8E3), fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("This build currently runs standard WireGuard configs. It does not yet contain Cloudflare account registration or a MASQUE tunnel. UP only confirms the local interface state.",
                color = Color(0xFF9CA3B8), fontSize = 11.sp, lineHeight = 16.sp)
            Spacer(Modifier.weight(1f))
            Text("WIREGUARD-GO • ANDROID", color = Color(0xFF697187), fontSize = 10.sp, letterSpacing = 1.4.sp)
            Text("Version 0.2.0 · Engine integration preview", color = Color(0xFF697187), fontSize = 11.sp)
        }
    }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            containerColor = Panel,
            title = { Text("Settings", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Connection engine", color = Cyan, fontWeight = FontWeight.SemiBold)
                    Text("WireGuard: imports a standard .conf file and starts a real Android VPN tunnel.",
                        color = Color(0xFFD6D8E3), fontSize = 13.sp)
                    Divider(color = Color(0xFF30384D))
                    Text("Cloudflare WARP", color = Cyan, fontWeight = FontWeight.SemiBold)
                    Text("A no-config WARP connection requires Cloudflare's account registration and MASQUE implementation. Those parts are not included in this build yet.",
                        color = Color(0xFFD6D8E3), fontSize = 13.sp)
                    Text("You can use Cloudflare's official app now without importing a config. It manages its own VPN permission and tunnel.",
                        color = Color(0xFF9CA3B8), fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showSettings = false
                    onOpenOfficialWarp()
                }) { Text("Open official WARP", color = Cyan) }
            },
            dismissButton = {
                TextButton(onClick = { showSettings = false }) { Text("Close", color = Color.LightGray) }
            }
        )
    }
}
