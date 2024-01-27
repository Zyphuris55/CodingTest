package com.lasley.kts_viewer.helpers


import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType
import kotlin.jvm.Throws

// https://github.com/Jintin/BindingExtension
// Android ViewBinding extension to provide simpler usage in Activity, Fragment and ViewHolder.

/**
 * Returns an instance of [ViewBinding] from this View Binding based class.
 *
 * @exception ClassCastException Called when (any):
 * - [inflate][LayoutInflater.inflate] invoke failed, due to invalid parmeters
 * - Inflated binding is not the expected binding type
 */
@JvmOverloads
@Throws(ClassCastException::class)
fun <V : ViewBinding> Class<V>.getBinding(
    layoutInflater: LayoutInflater,
    container: ViewGroup? = null,
    attachToParent: Boolean = false
): V {
    val inflatedView = try {
        if (container == null)
            getMethod("inflate", LayoutInflater::class.java)
                .invoke(null, layoutInflater)
        else
            getMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
                .invoke(null, layoutInflater, container, attachToParent)
    } catch (e: Exception) {
        e.printStackTrace()
        throw ClassCastException("Unable invoke 'inflate' to create expected type")
    }

    // ClassCastException is likely to only occur if the inflated view
    // is not ViewBinding view, but just a standard view
    @Suppress("UNCHECKED_CAST")
    return inflatedView as? V
        ?: throw ClassCastException("Binding is not the expected type")
}

/**
 * Checks if [this] class supports calling the "inflate" function with an input of [LayoutInflater]
 *
 * @return Contains support for the requested function call
 */
internal fun Class<*>.checkMethod(): Boolean {
    return try {
        getMethod("inflate", LayoutInflater::class.java)
        true
    } catch (ex: Exception) {
        false
    }
}

/**
 * With the given input object,
 * find the first (parent) ParameterizedType class which can pass [checkMethod]
 *
 * @exception NoSuchElementException [this] object does not extend from a parameterized class
 * @exception ClassCastException The discovered parameterized class is not of the expected view binding type
 */
@Throws(NoSuchElementException::class, ClassCastException::class)
fun <V : ViewBinding> Any.findParameterizedClass(): Class<V> {
    var javaClass: Class<*> = this.javaClass
    var result: Class<*>? = null
    while (result == null || !result.checkMethod()) {
        result = (javaClass.genericSuperclass as? ParameterizedType)
            ?.actualTypeArguments?.firstOrNull {
                if (it is Class<*>)
                    it.checkMethod()
                else
                    false
            } as? Class<*>
        if (javaClass.superclass == null)
            throw NoSuchElementException("No Parameterized class found")
        javaClass = javaClass.superclass
    }

    @Suppress("UNCHECKED_CAST")
    return result as? Class<V>
        ?: throw ClassCastException("Binding is not the expected type")
}

/**
 * Creates a [ViewBinding][V] from [this]'s layout [LayoutInflater]
 *
 * @exception ClassCastException The discovered/ inflated view binding is not the expected view binding type
 */
@Throws(ClassCastException::class)
inline fun <reified V : ViewBinding> ViewGroup.createBinding(
    attachToParent: Boolean = false
): V = V::class.java.getBinding(LayoutInflater.from(context), this, attachToParent)

/**
 * Inflates and returns a layout of type [ViewBinding][V] from [this] activity
 *
 * @exception ClassCastException The discovered/ inflated view binding is not the expected view binding type
 */
@Throws(ClassCastException::class)
internal fun <V : ViewBinding> Activity.getBinding(): V =
    findParameterizedClass<V>().getBinding(layoutInflater)

/**
 * Inflates and returns a layout of type [ViewBinding][V] from [this] fragment
 *
 * @exception ClassCastException The discovered/ inflated view binding is not the expected view binding type
 */
@Throws(ClassCastException::class)
internal fun <V : ViewBinding> Fragment.getBinding(
    inflater: LayoutInflater,
    container: ViewGroup?
): V = findParameterizedClass<V>().getBinding(inflater, container)
