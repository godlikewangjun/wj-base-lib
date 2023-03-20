package com.wj.okhttp.cookies

import java.io.IOException
import kotlin.Throws
import okhttp3.Cookie
import java.io.ObjectOutputStream
import java.io.ObjectInputStream
import java.lang.ClassNotFoundException
import kotlin.jvm.Transient
import java.io.Serializable

/**
 * @author Admin
 * @version 1.0
 * @date 2017/4/19
 */
class SerializableOkHttpCookies(val cookies: Cookie) : Serializable {

    @Transient
    private var clientCookies: Cookie? = null


    @JvmName("getCookies1")
    fun getCookies(): Cookie {
        if (clientCookies != null) {
            clientCookies = cookies
        }
        return clientCookies!!
    }

    @Throws(IOException::class)
    private fun writeObject(out: ObjectOutputStream) {
        out.writeObject(cookies.name)
        out.writeObject(cookies.value)
        out.writeLong(cookies.expiresAt)
        out.writeObject(cookies.domain)
        out.writeObject(cookies.path)
        out.writeBoolean(cookies.secure)
        out.writeBoolean(cookies.httpOnly)
        out.writeBoolean(cookies.hostOnly)
        out.writeBoolean(cookies.persistent)
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(`in`: ObjectInputStream) {
        val name = `in`.readObject() as String
        val value = `in`.readObject() as String
        val expiresAt = `in`.readLong()
        val domain = `in`.readObject() as String
        val path = `in`.readObject() as String
        val secure = `in`.readBoolean()
        val httpOnly = `in`.readBoolean()
        val hostOnly = `in`.readBoolean()
        val persistent = `in`.readBoolean()
        var builder = Cookie.Builder()
        builder = builder.name(name)
        builder = builder.value(value)
        builder = builder.expiresAt(expiresAt)
        builder = if (hostOnly) builder.hostOnlyDomain(domain) else builder.domain(domain)
        builder = builder.path(path)
        builder = if (secure) builder.secure() else builder
        builder = if (httpOnly) builder.httpOnly() else builder
        clientCookies = builder.build()
    }
}