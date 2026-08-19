package com.example.control

import com.example.client.LinkXClientEngine
import com.example.protocol.ClickType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class RemoteInputMode {
    TRACKPAD_TOUCH,
    AIR_MOUSE_GYRO,
    GAMEPAD_CONTROLLER,
    PRESENTER_POINTER
}

data class GamepadState(
    val leftStickX: Float = 0f,
    val leftStickY: Float = 0f,
    val rightStickX: Float = 0f,
    val rightStickY: Float = 0f,
    val isDPadUpPressed: Boolean = false,
    val isDPadDownPressed: Boolean = false,
    val isDPadLeftPressed: Boolean = false,
    val isDPadRightPressed: Boolean = false,
    val isBtnAPressed: Boolean = false,
    val isBtnBPressed: Boolean = false,
    val isBtnXPressed: Boolean = false,
    val isBtnYPressed: Boolean = false,
    val isTriggerL1Pressed: Boolean = false,
    val isTriggerR1Pressed: Boolean = false,
    val triggerL2Depth: Float = 0f,
    val triggerR2Depth: Float = 0f,
    val activeLayout: String = "Xbox Standard Layout"
)

data class AirMouseState(
    val isGyroEnabled: Boolean = false,
    val gyroSensitivity: Float = 2.0f,
    val pitch: Float = 0f,
    val roll: Float = 0f,
    val yaw: Float = 0f
)

data class PresenterLaserState(
    val isLaserPointerActive: Boolean = false,
    val laserColorHex: String = "#FF0055",
    val laserX: Float = 0.5f,
    val laserY: Float = 0.5f,
    val timerElapsedSeconds: Int = 0,
    val isTimerRunning: Boolean = false
)

data class RemoteControlState(
    val activeInputMode: RemoteInputMode = RemoteInputMode.TRACKPAD_TOUCH,
    val gamepad: GamepadState = GamepadState(),
    val airMouse: AirMouseState = AirMouseState(),
    val presenterLaser: PresenterLaserState = PresenterLaserState(),
    val isVibrationFeedbackEnabled: Boolean = true,
    val activeMacroProfile: String = "Default Productivity"
)

object RemoteControlEngine {
    private val _state = MutableStateFlow(RemoteControlState())
    val state: StateFlow<RemoteControlState> = _state.asStateFlow()

    fun setInputMode(mode: RemoteInputMode) {
        _state.value = _state.value.copy(activeInputMode = mode)
    }

    // Touchpad Gesture Dispatcher
    fun processTouchpadGesture(dx: Float, dy: Float, fingersCount: Int) {
        when (fingersCount) {
            1 -> {
                // Standard 1-finger move
                LinkXClientEngine.sendMouseMove(dx, dy)
            }
            2 -> {
                // 2-finger scroll
                LinkXClientEngine.sendMouseScroll(dy * -0.1f)
            }
            3 -> {
                // 3-finger swipe window switch / Alt+Tab
                if (dx > 30f) {
                    LinkXClientEngine.sendKeyCombo(listOf("Alt", "Shift", "Tab"))
                } else if (dx < -30f) {
                    LinkXClientEngine.sendKeyCombo(listOf("Alt", "Tab"))
                } else if (dy < -30f) {
                    LinkXClientEngine.sendKeyCombo(listOf("Win", "Tab")) // Task View
                } else if (dy > 30f) {
                    LinkXClientEngine.sendKeyCombo(listOf("Win", "D")) // Show Desktop
                }
            }
        }
    }

    // Air Mouse / Gyro Sensor Handler
    fun toggleGyroAirMouse() {
        val curr = _state.value
        val updated = curr.airMouse.copy(isGyroEnabled = !curr.airMouse.isGyroEnabled)
        _state.value = curr.copy(airMouse = updated)
    }

    fun setGyroSensitivity(sens: Float) {
        val curr = _state.value
        val updated = curr.airMouse.copy(gyroSensitivity = sens)
        _state.value = curr.copy(airMouse = updated)
    }

    fun updateGyroAngles(deltaPitch: Float, deltaYaw: Float) {
        val curr = _state.value
        if (!curr.airMouse.isGyroEnabled) return
        val sens = curr.airMouse.gyroSensitivity
        LinkXClientEngine.sendMouseMove(deltaYaw * sens * 10f, deltaPitch * sens * 10f)
    }

    // Gamepad Controls Handler
    fun updateLeftJoystick(x: Float, y: Float) {
        val curr = _state.value
        val updatedGamepad = curr.gamepad.copy(leftStickX = x, leftStickY = y)
        _state.value = curr.copy(gamepad = updatedGamepad)

        // Map Left Analog Stick to WASD / Cursor Navigation
        val deadzone = 0.2f
        if (x < -deadzone) LinkXClientEngine.sendKeyPress("a")
        else if (x > deadzone) LinkXClientEngine.sendKeyPress("d")

        if (y < -deadzone) LinkXClientEngine.sendKeyPress("w")
        else if (y > deadzone) LinkXClientEngine.sendKeyPress("s")
    }

    fun updateRightJoystick(x: Float, y: Float) {
        val curr = _state.value
        val updatedGamepad = curr.gamepad.copy(rightStickX = x, rightStickY = y)
        _state.value = curr.copy(gamepad = updatedGamepad)

        // Right stick moves mouse aim in Game Mode
        LinkXClientEngine.sendMouseMove(x * 15f, y * 15f)
    }

    fun sendGamepadButton(buttonLabel: String, mappedKey: String) {
        LinkXClientEngine.sendKeyPress(mappedKey)
    }

    // Presenter Laser Pointer Logic
    fun toggleLaserPointer() {
        val curr = _state.value
        val updatedLaser = curr.presenterLaser.copy(isLaserPointerActive = !curr.presenterLaser.isLaserPointerActive)
        _state.value = curr.copy(presenterLaser = updatedLaser)
    }

    fun updateLaserCoordinates(xPct: Float, yPct: Float) {
        val curr = _state.value
        val updatedLaser = curr.presenterLaser.copy(laserX = xPct, laserY = yPct)
        _state.value = curr.copy(presenterLaser = updatedLaser)
    }
}
