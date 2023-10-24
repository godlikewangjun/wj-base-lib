package com.wj.ktolin.baselibrary

import android.app.Application
import android.widget.TextView
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.bindSingleton

/**
 * 测试注入框架
 */
class App :Application(), DIAware {
    override val di by DI.lazy {
       bindSingleton { TextView(this@App) }

    }
}