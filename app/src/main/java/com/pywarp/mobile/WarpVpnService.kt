package com.pywarp.mobile

import android.net.VpnService

/**
 * Android VPN service entry point.
 *
 * The packet tunnel is intentionally not started here: a real WARP-compatible
 * WireGuard/MASQUE engine must be integrated and configured before routing traffic.
 * This prevents the app from displaying a false "connected" state or black-holing traffic.
 */
class WarpVpnService : VpnService()
