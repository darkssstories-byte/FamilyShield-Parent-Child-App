package com.familyshield.parent

data class AppUsage(val name: String, val minutes: Int)

data class ChildDevice(
    val id: String,
    val name: String,
    val batteryPercent: Int?,
    val charging: Boolean,
    val lastSyncMillis: Long?,
    val screenTimeTodayMinutes: Int?,
    val screenTimeWeekMinutes: Int?,
    val topApps: List<AppUsage>,
    val isDemo: Boolean = false
)

data class PairingCode(val code: String, val expiresAtMillis: Long)

sealed interface PairingUi {
    object Idle : PairingUi
    object Loading : PairingUi
    data class Ready(val code: PairingCode) : PairingUi
    data class Error(val message: String) : PairingUi
}

data class UiState(
    val configured: Boolean,
    val signedIn: Boolean,
    val demo: Boolean = false,
    val devices: List<ChildDevice> = emptyList(),
    val devicesLoading: Boolean = false,
    val devicesError: String? = null,
    val authBusy: Boolean = false,
    val authError: String? = null,
    val pairing: PairingUi = PairingUi.Idle,
    val message: String? = null
) {
    val visibleDevices: List<ChildDevice>
        get() = if (demo) listOf(DemoData.device()) else devices
}

object DemoData {
    fun device(): ChildDevice = ChildDevice(
        id = "demo-device",
        name = "Demo phone (sample data)",
        batteryPercent = 72,
        charging = false,
        lastSyncMillis = System.currentTimeMillis() - 5 * 60 * 1000L,
        screenTimeTodayMinutes = 145,
        screenTimeWeekMinutes = 980,
        topApps = listOf(
            AppUsage("Sample Video App", 55),
            AppUsage("Sample Game", 40),
            AppUsage("Sample Browser", 30),
            AppUsage("Sample Chat App", 20)
        ),
        isDemo = true
    )
}
