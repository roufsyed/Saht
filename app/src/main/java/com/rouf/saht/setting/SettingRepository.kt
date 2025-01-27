package com.rouf.saht.setting

import android.util.Log
import com.rouf.saht.common.model.HeartRateMonitorSensitivity
import com.rouf.saht.common.model.HeartRateMonitorSettings
import com.rouf.saht.common.model.PedometerSettings
import com.rouf.saht.common.model.PersonalInformation
import io.paperdb.Paper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.log

@Singleton
class SettingRepository @Inject constructor() {

    private val TAG: String? = SettingRepository::class.java.simpleName

    suspend fun savePersonalInformation(personalInformation: PersonalInformation) = withContext(Dispatchers.IO){
        Paper.book().write("personal_information", personalInformation)
    }

    suspend fun getPersonalInformation(): PersonalInformation? = withContext(Dispatchers.IO) {
            Paper.book().read("personal_information")
    }

    suspend fun savePedometerSettings(pedometerSettings: PedometerSettings) = withContext(Dispatchers.IO) {
        Paper.book().write("pedometer_settings", pedometerSettings)
    }

    suspend fun getPedometerSettings(): PedometerSettings? = withContext(Dispatchers.IO) {
        Paper.book().read("pedometer_settings")
    }

    suspend fun saveHeartMonitorSettings(heartRateMonitorSettings: HeartRateMonitorSettings) = withContext(Dispatchers.IO) {
        Paper.book().write("heart_rate_monitor_settings", heartRateMonitorSettings)
    }

    suspend fun getHeartRateMonitorSettings(): HeartRateMonitorSettings? = withContext(Dispatchers.IO) {
        Paper.book().read("heart_rate_monitor_settings")
    }

}