package com.wj.util

import android.Manifest
import android.content.Context
import java.lang.Exception
import android.content.pm.PackageManager
import kotlin.Throws
import java.lang.Class
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import android.text.TextUtils
import kotlin.jvm.Synchronized

/**
 * @author Admin
 * @version 1.0
 * @date 2018/1/24
 */
class CTelephoneInfo {
    var imeiSIM1 // IMEI
            : String? = null
        private set
    var imeiSIM2 //IMEI
            : String? = null
        private set
    var iNumeric1 //sim1 code number
            : String? = null
        private set
    var iNumeric2 //sim2 code number
            : String? = null
        private set
    var isSIM1Ready //sim1
            = false
        private set
    var isSIM2Ready //sim2
            = false
        private set
    private var iDataConnected1: String? = "0" //sim1 0 no, 1 connecting, 2 connected, 3 suspended.
    private var iDataConnected2: String? = "0" //sim2
    val isDualSim: Boolean
        get() = imeiSIM2 != null
    val isDataConnected1: Boolean
        get() = TextUtils.equals(iDataConnected1, "2") || TextUtils.equals(iDataConnected1,
                "1")
    val isDataConnected2: Boolean
        get() = TextUtils.equals(iDataConnected2, "2") || TextUtils.equals(iDataConnected2,
                "1")
    val iNumeric: String?
        get() {
            if (imeiSIM2 != null) {
                if (iNumeric1 != null && iNumeric1!!.length > 1) return iNumeric1
                if (iNumeric2 != null && iNumeric2!!.length > 1) return iNumeric2
            }
            return iNumeric1
        }

    fun setCTelephoneInfo(mContext: Context) {
        val telephonyManager =
            mContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
        ) {
            try {
                throw Exception("permission is null")
            } catch (e: Exception) {
            }
        }
        CTelephoneInfo!!.imeiSIM1 = telephonyManager.deviceId
        CTelephoneInfo!!.imeiSIM2 = null
        try {
            CTelephoneInfo!!.imeiSIM1 = getOperatorBySlot(mContext, "getDeviceIdGemini", 0)
            CTelephoneInfo!!.imeiSIM2 = getOperatorBySlot(mContext, "getDeviceIdGemini", 1)
            CTelephoneInfo!!.iNumeric1 = getOperatorBySlot(mContext, "getSimOperatorGemini", 0)
            CTelephoneInfo!!.iNumeric2 = getOperatorBySlot(mContext, "getSimOperatorGemini", 1)
            CTelephoneInfo!!.iDataConnected1 = getOperatorBySlot(mContext, "getDataStateGemini", 0)
            CTelephoneInfo!!.iDataConnected2 = getOperatorBySlot(mContext, "getDataStateGemini", 1)
        } catch (e: GeminiMethodNotFoundException) {
            try {
                CTelephoneInfo!!.imeiSIM1 = getOperatorBySlot(mContext, "getDeviceId", 0)
                CTelephoneInfo!!.imeiSIM2 = getOperatorBySlot(mContext, "getDeviceId", 1)
                CTelephoneInfo!!.iNumeric1 = getOperatorBySlot(mContext, "getSimOperator", 0)
                CTelephoneInfo!!.iNumeric2 = getOperatorBySlot(mContext, "getSimOperator", 1)
                CTelephoneInfo!!.iDataConnected1 = getOperatorBySlot(mContext, "getDataState", 0)
                CTelephoneInfo!!.iDataConnected2 = getOperatorBySlot(mContext, "getDataState", 1)
            } catch (e1: GeminiMethodNotFoundException) {
                //Call here for next manufacturer's predicted method name if you wish
            }
        }
        CTelephoneInfo!!.isSIM1Ready = telephonyManager.simState == TelephonyManager.SIM_STATE_READY
        CTelephoneInfo!!.isSIM2Ready = false
        try {
            CTelephoneInfo!!.isSIM1Ready = getSIMStateBySlot(mContext, "getSimStateGemini", 0)
            CTelephoneInfo!!.isSIM2Ready = getSIMStateBySlot(mContext, "getSimStateGemini", 1)
        } catch (e: GeminiMethodNotFoundException) {
            try {
                CTelephoneInfo!!.isSIM1Ready = getSIMStateBySlot(mContext, "getSimState", 0)
                CTelephoneInfo!!.isSIM2Ready = getSIMStateBySlot(mContext, "getSimState", 1)
            } catch (e1: GeminiMethodNotFoundException) {
                //Call here for next manufacturer's predicted method name if you wish
            }
        }
    }

    private class GeminiMethodNotFoundException(info: String?) : Exception(info) {
        companion object {
            /**
             *
             */
            private const val serialVersionUID = -3241033488141442594L
        }
    }

    companion object {
        private var CTelephoneInfo: CTelephoneInfo? = null

        @get:Synchronized
        val instance: CTelephoneInfo?
            get() {
                if (CTelephoneInfo == null) {
                    CTelephoneInfo = CTelephoneInfo()
                }
                return CTelephoneInfo
            }

        @Throws(GeminiMethodNotFoundException::class)
        private fun getOperatorBySlot(
            context: Context,
            predictedMethodName: String,
            slotID: Int
        ): String? {
            var inumeric: String? = null
            val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            try {
                val telephonyClass = Class.forName(telephony.javaClass.name)
                val parameter = arrayOfNulls<Class<*>?>(1)
                parameter[0] = Int::class.javaPrimitiveType
                val getSimID = telephonyClass.getMethod(predictedMethodName, *parameter)
                val obParameter = arrayOfNulls<Any>(1)
                obParameter[0] = slotID
                val ob_phone = getSimID.invoke(telephony, *obParameter)
                if (ob_phone != null) {
                    inumeric = ob_phone.toString()
                }
            } catch (e: Exception) {
                throw GeminiMethodNotFoundException(predictedMethodName)
            }
            return inumeric
        }

        @Throws(GeminiMethodNotFoundException::class)
        private fun getSIMStateBySlot(
            context: Context,
            predictedMethodName: String,
            slotID: Int
        ): Boolean {
            var isReady = false
            val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            try {
                val telephonyClass = Class.forName(telephony.javaClass.name)
                val parameter = arrayOfNulls<Class<*>?>(1)
                parameter[0] = Int::class.javaPrimitiveType
                val getSimStateGemini = telephonyClass.getMethod(predictedMethodName, *parameter)
                val obParameter = arrayOfNulls<Any>(1)
                obParameter[0] = slotID
                val ob_phone = getSimStateGemini.invoke(telephony, *obParameter)
                if (ob_phone != null) {
                    val simState = ob_phone.toString().toInt()
                    if (simState == TelephonyManager.SIM_STATE_READY) {
                        isReady = true
                    }
                }
            } catch (e: Exception) {
                throw GeminiMethodNotFoundException(predictedMethodName)
            }
            return isReady
        }
    }
}