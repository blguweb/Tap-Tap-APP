package com.kieronquinn.app.taptap.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.columbus.actions.*
import com.kieronquinn.app.taptap.utils.AccessibilityServiceGlobalAction
import com.kieronquinn.app.taptap.utils.LaunchCameraLocal
import com.kieronquinn.app.taptap.utils.minSdk

enum class TapAction(val clazz: Class<*>, val category: TapActionCategory, @StringRes val nameRes: Int, @StringRes val descriptionRes: Int, @DrawableRes val iconRes: Int, val isAvailable: Boolean, val isWhenAvailable: Boolean, @StringRes val formattableDescription: Int? = null, val dataType: ActionDataTypes? = null) {
    SEND_MESSAGE(LaunchApp::class.java, TapActionCategory.LAUNCH, R.string.action_launch_app, R.string.action_launch_app_desc, R.drawable.ic_action_category_launch, true, true, R.string.action_launch_app_desc_formattable, ActionDataTypes.PACKAGE_NAME)
}

enum class ActionDataTypes {
    PACKAGE_NAME,
    SHORTCUT
}