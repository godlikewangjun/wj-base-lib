package com.wj.ui.view.weight

import android.content.Context
import androidx.appcompat.app.AlertDialog

class MyDialog : AlertDialog {
    constructor(context: Context?, theme: Int) : super(context!!, theme) {}
    constructor(context: Context?) : super(context!!) {}
}