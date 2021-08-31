package com.kieronquinn.app.taptap.models.store

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.kieronquinn.app.taptap.models.ActionInternal
import com.kieronquinn.app.taptap.models.EquipmentInternal
import com.kieronquinn.app.taptap.models.TapEquipment
import com.kieronquinn.app.taptap.utils.DEFAULT_ACTIONS
import com.kieronquinn.app.taptap.utils.DEFAULT_EQUIPMENT
import com.kieronquinn.app.taptap.utils.SHARED_PREFERENCES_KEY_ACTIONS_TIME
import java.io.File
import java.nio.charset.Charset

object EquipmentListFile {

    private fun getEquipmentListFile(context: Context): File {
        return File(context.filesDir, "equipments.json")
    }

    fun loadFromFile(context: Context): Array<EquipmentInternal> {
//        return DEFAULT_EQUIPMENT.map { createEquipmentInternalForAction(it) }.toTypedArray()
        val file = getEquipmentListFile(context)
        if(!file.exists()) return DEFAULT_EQUIPMENT.map { createEquipmentInternalForAction(it) }.toTypedArray()
        val fileData = file.readText(Charset.defaultCharset())
        if(fileData.isEmpty()) return DEFAULT_EQUIPMENT.map { createEquipmentInternalForAction(it) }.toTypedArray()
        return Gson().fromJson(fileData, Array<EquipmentInternal>::class.java)
    }

    private fun createEquipmentInternalForAction(action: TapEquipment): EquipmentInternal {
        return EquipmentInternal(action, ArrayList())
    }

    fun saveToFile(context: Context, actions: Array<EquipmentInternal>, sharedPreferences: SharedPreferences?){
        val file = getEquipmentListFile(context)
        val json = Gson().toJson(actions)
        file.writeText(json, Charset.defaultCharset())
        sharedPreferences?.run {
            //Put the current time in the shared prefs as a bit of a hacky way to trigger an update on the accessibility service
            edit().putLong(SHARED_PREFERENCES_KEY_ACTIONS_TIME, System.currentTimeMillis()).apply()
        }
    }

}