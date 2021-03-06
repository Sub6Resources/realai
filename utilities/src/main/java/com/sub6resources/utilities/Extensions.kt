package com.sub6resources.utilities

import android.app.Activity
import android.app.Dialog
import android.app.NotificationManager
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.net.wifi.WifiManager
import android.os.Build
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import java.util.concurrent.TimeUnit
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty



fun Long.toShortTime(): String {
    val hr = TimeUnit.MILLISECONDS.toHours(this)
    val min = TimeUnit.MILLISECONDS.toMinutes(this)
    val sec = TimeUnit.MILLISECONDS.toSeconds(this)

    return "${if (hr != 0L) "$hr:" else ""}${if (min < 10L) "0$min" else min}:${if (sec < 10L) "0$sec" else sec}"
}

fun View.onClick(listener: View.OnClickListener) {
    setOnClickListener(listener)
}

fun View.onClick(onClick: (v: View) -> Unit) {
    setOnClickListener(onClick)
}

fun Context.getStringRes(@StringRes resId: Int) = resources.getString(resId)

fun Context.inflateLayout(layoutResId: Int, parent: ViewGroup? = null, attachToRoot: Boolean = false): View
        = LayoutInflater.from(this).inflate(layoutResId, parent, attachToRoot)

val Context.inputMethodManager: InputMethodManager
    get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

val Context.clipboardManager: ClipboardManager
    get() = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

val Context.layoutInflater: LayoutInflater
    get() = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

val Context.notificationManager: NotificationManager
    get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

val Context.wifiManager: WifiManager
    get() = applicationContext.wifiManager

private val version: Int
    get() = Build.VERSION.SDK_INT

inline fun apiOr(version: Int, action_greater: () -> Unit, action_lower: () -> Unit, inclusive: Boolean = false) {
    fromApi(version, action_greater, inclusive)
    toApi(version, action_lower, inclusive)
}

inline fun toApi(toVersion: Int, action: () -> Unit, inclusive: Boolean = false) {
    if (Build.VERSION.SDK_INT < toVersion || (inclusive && Build.VERSION.SDK_INT == toVersion)) action()
}

inline fun fromApi(fromVersion: Int, action: () -> Unit, inclusive: Boolean = true) {
    if (Build.VERSION.SDK_INT > fromVersion || (inclusive && Build.VERSION.SDK_INT == fromVersion)) action()
}

fun ImageView.tintCurrentDrawable(color: Int) {
    DrawableCompat.wrap(drawable!!).let {
        it.mutate()
        DrawableCompat.setTintList(it, ColorStateList.valueOf(color))
        setImageDrawable(it)
    }
}

fun String?.isNothing(): Boolean {
    val strLen: Int? = this?.length
    if (this == null || strLen == 0) return true
    for (i in 0..strLen!!.minus(1))
        if (Character.isWhitespace(this[i]) == false) return false
    return true
}

fun String.unescape(): String = this.replace("""\/""", "/")

fun View.bulkClick(ids: Array<Int>, _onClick: (View) -> Unit) {
    ids.forEach { findViewById<View>(it).apply { onClick { v -> _onClick.invoke(v) } } }
}

fun Array<View>.bulkClick(_onClick: (View) -> Unit) {
    forEach { it.apply { onClick { v -> _onClick.invoke(v) } } }
}

fun String?.nullSafe(default: String = ""): String = if (this == null) default else this
fun EditText.getString(): String = text.toString()

inline infix fun Any?.isNull(if_true: () -> Unit) {
    if (this == null) if_true.invoke()
}

inline infix fun Any?.isNotNull(if_true: (Any) -> Unit) {
    if (this != null) if_true.invoke(this)
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.disable() {
    isEnabled = false
}

fun View.enable() {
    isEnabled = true
}

fun EditText.isBlank(): Boolean = getString().isNullOrBlank()

// Find a view.  Not lazy.
inline fun <reified V : View> View.find(id: Int): V = findViewById<V>(id)

inline fun <reified V : View> Activity.find(id: Int): V = findViewById<V>(id)
inline fun <reified V : View> Dialog.find(id: Int): V = findViewById<V>(id)
inline fun <reified V : View> Fragment.find(id: Int): V = view!!.findViewById<V>(id)
inline fun <reified V : View> android.app.Fragment.find(id: Int): V = view!!.findViewById<V>(id)
inline fun <reified V : View> RecyclerView.ViewHolder.find(id: Int): V = itemView.findViewById<V>(id)

fun SpannableStringBuilder.appendWithSpan(str: String, ss: Any) {
    val start = this.length
    this.append(str)
    this.setSpan(ss, start, this.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
}


// Bind a view.
fun <V : View> View.bind(id: Int): ReadOnlyProperty<View, V> = required(id, { find(it) })
fun <V : View> Activity.bind(id: Int): ReadOnlyProperty<Activity, V> = required(id, { find(it) })
fun <V : View> Dialog.bind(id: Int): ReadOnlyProperty<Dialog, V> = required(id, { find(it) })
fun <V : View> android.app.Fragment.bind(id: Int): ReadOnlyProperty<android.app.Fragment, V> = required(id, { find(it) })
fun <V : View> Fragment.bind(id: Int): ReadOnlyProperty<Fragment, V> = required(id, { find(it) })
fun <V : View> RecyclerView.ViewHolder.bind(id: Int): ReadOnlyProperty<RecyclerView.ViewHolder, V> = required(id, { find(it) })
private fun viewNotFound(id: Int, desc: KProperty<*>): Nothing = throw IllegalStateException("View ID $id for '${desc.name}' not found.")

@Suppress("UNCHECKED_CAST")
private fun <T, V : View> required(id: Int, finder: T.(Int) -> View?) = ViewLazy { t: T, desc -> t.finder(id) as V? ?: viewNotFound(id, desc) }

private class ViewLazy<in T, out V>(private val initializer: (T, KProperty<*>) -> V) : ReadOnlyProperty<T, V> {
    private object EMPTY

    private var value: Any? = EMPTY

    override fun getValue(thisRef: T, property: KProperty<*>): V {
        if (value == EMPTY) {
            value = initializer(thisRef, property)
        }
        @Suppress("UNCHECKED_CAST")
        return value as V
    }
}