package com.kieronquinn.app.taptap.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.utils.AccessibilityServiceGlobalAction


enum class TapEquipment (val clazz: Class<*>,  @StringRes val nameRes: Int, @StringRes val descriptionRes: Int, @DrawableRes val iconRes: Int, val isAvailable: Boolean, val isWhenAvailable: Boolean, @StringRes val formattableDescription: Int? = null, val dataType: EquipmentDataTypes? = null) {
    TV(AccessibilityServiceGlobalAction::class.java,R.string.television, R.string.television, R.drawable.ic_action_home, true, true),
    AIR(AccessibilityServiceGlobalAction::class.java,R.string.air_conditioner, R.string.air_conditioner, R.drawable.ic_action_home, true, true),
    AUDIO(AccessibilityServiceGlobalAction::class.java,R.string.audio, R.string.audio, R.drawable.ic_action_home, true, true),
    FAN(AccessibilityServiceGlobalAction::class.java,R.string.fan, R.string.fan, R.drawable.ic_action_home, true, true),
    SWEEPER(AccessibilityServiceGlobalAction::class.java,R.string.sweeper, R.string.sweeper, R.drawable.ic_action_home, true, true)
}

enum class EquipmentDataTypes {
    PACKAGE_NAME
}