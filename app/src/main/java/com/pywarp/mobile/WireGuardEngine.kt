package com.pywarp.mobile

import android.content.Context
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import java.io.ByteArrayInputStream

/**
 * Real userspace WireGuard tunnel lifecycle.
 *
 * This accepts a standard WireGuard wg-quick configuration supplied by the user.
 * It does not provision Cloudflare WARP accounts or implement Cloudflare MASQUE.
 */
object WireGuardEngine {
    @Volatile private var backend: GoBackend? = null

    private val tunnel = object : Tunnel {
        override fun getName(): String = "pywarp"
        override fun onStateChange(newState: Tunnel.State) = Unit
    }

    @Synchronized
    fun connect(context: Context, configBytes: ByteArray) {
        val parsed = Config.parse(ByteArrayInputStream(configBytes))
        require(parsed.peers.isNotEmpty()) { "The configuration has no [Peer] section." }
        require(parsed.interface.addresses.isNotEmpty()) { "The configuration has no Interface Address." }

        val activeBackend = backend ?: GoBackend(context.applicationContext).also { backend = it }
        val result = activeBackend.setState(tunnel, Tunnel.State.UP, parsed)
        check(result == Tunnel.State.UP && activeBackend.getState(tunnel) == Tunnel.State.UP) {
            "WireGuard backend did not enter the UP state."
        }
    }

    @Synchronized
    fun disconnect() {
        val activeBackend = backend ?: return
        if (activeBackend.getState(tunnel) == Tunnel.State.UP) {
            activeBackend.setState(tunnel, Tunnel.State.DOWN, null)
        }
    }

    @Synchronized
    fun isConnected(): Boolean =
        backend?.getState(tunnel) == Tunnel.State.UP
}
