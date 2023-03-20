package com.wj.util

import android.content.Context
import java.lang.Exception
import kotlin.Throws
import java.io.InputStream
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import okhttp3.OkHttpClient
import javax.net.ssl.X509TrustManager
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import java.security.SecureRandom
import com.bumptech.glide.annotation.GlideModule
import okhttp3.Call
import java.security.cert.CertificateException
import java.security.cert.X509Certificate

/**
 * @author Admin
 * @version 1.0
 * @date 2017/9/7
 */
@GlideModule
class GlideModel : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        try {
            registry.replace(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(
                sSLOkHttpClient as Call.Factory))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 设置https 访问的时候对所有证书都进行信任
     *
     * @throws Exception
     */
    @get:Throws(Exception::class)
    private val sSLOkHttpClient: OkHttpClient
        get() {
            val trustManager: X509TrustManager = object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate?> {
                    return arrayOfNulls(0)
                }
            }
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, arrayOf<TrustManager>(trustManager), SecureRandom())
            val sslSocketFactory = sslContext.socketFactory
            return OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustManager)
                .hostnameVerifier { hostname, session -> true }
                .build()
        }
}