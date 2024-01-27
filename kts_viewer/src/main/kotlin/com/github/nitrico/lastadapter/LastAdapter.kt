/*
 * Copyright (C) 2016 Miguel Ángel Moreno
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("PrivatePropertyName", "MemberVisibilityCanBePrivate", "unused")

package com.github.nitrico.lastadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableList
import androidx.databinding.OnRebindCallback
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.contracts.ExperimentalContracts
import kotlin.reflect.KClass

/*
* **Based on v2.3.0**
* Migration to AndroidX: https://github.com/nitrico/LastAdapter/pull/48
* @see [https://github.com/nitrico/LastAdapter]
*/

/**
 * Adapter for creating mapper/ adapter for [RecyclerView]s
 *
 * @param variable Where the [list] data will be sent in the target's view binding
 * @param stableIds Indicates whether each item in the data set can be represented with a unique identifier of type [Long].
 * - Note: The data type MUST extend from [StableId] if set to true
 *
 * **Example**
 * ```kotlin
 * LastAdapter(listOfItems, BR.item)
 *   .map<Header>(R.layout.item_header)
 *   .layout { item, position ->
 *     when (item) {
 *       is Header -> if (position == 0) R.layout.item_header_first else R.layout.item_header
 *       else -> R.layout.item_point
 *     }
 *   }
 *   .into(recyclerView)
 * ```
 */
class LastAdapter(
    list: List<Any>?,
    val variable: Int? = null,
    val stableIds: Boolean = false
) : RecyclerView.Adapter<Holder<ViewDataBinding>>() {

    /**
     * Adapter which requires child functions to assign the binding [variable].
     *
     * **Example**
     * ```kotlin
     * LastAdapter(listOfItems)
     *   .map<Header>(R.layout.item_header, BR.item1)
     *   .map<Point>(R.layout.item_point, BR.item2)
     *   .type { item, position ->
     *     when (item) {
     *       is Body1 -> object : ItemType<Body1Binding>(R.layout.item_body1, BR.item) {...}
     *       is Body2 -> object : ItemType<Body1Binding>(R.layout.item_body2, BR.item) {...}
     *       else -> null
     *     }
     *   }
     *   .into(recyclerView)
     * ```
     */
    constructor(list: List<Any>?) : this(list.orEmpty(), null, false)

    /**
     * Adapter which requires child functions to assign the binding [variable].
     * [stableIds] can also be set independently.
     *
     * **Example**
     * ```kotlin
     * class XYZ : StableId {
     *   override val stableId: Long get() = ...
     * }
     *
     * LastAdapter(listOfItems, true)
     *   .map<Header>(R.layout.item_header, BR.item1)
     *   .map<Point>(R.layout.item_point, BR.item2)
     *   .type { item, position ->
     *      when (item) {
     *        is Body1 -> object : ItemType<Body1Binding>(R.layout.item_body1, BR.item) {...}
     *        is Body2 -> object : ItemType<Body2Binding>(R.layout.item_body2, BR.item) {...}
     *        else -> null
     *      }
     *   }
     *   .into(recyclerView)
     * ```
     */
    constructor(list: List<Any>?, stableIds: Boolean) : this(list.orEmpty(), null, stableIds)

    /**
     * Adapter which supports an input of [LiveData].
     * Whenever the live data is updated, the adapter will automatically update too.
     *
     * The data type of the live data is expected to be a type of list.
     *
     * **Example**
     * ```kotlin
     * val data = MutableLiveData(listOf<Int>())
     *
     * LastAdapter(data, BR.item)
     *   .map<Header>(R.layout.item_header)
     *   .map<Point>(R.layout.item_point)
     *   .into(recyclerView)
     * ...
     * data.value = listOf(...)
     * ```
     */
    constructor(
        liveData: MutableLiveData<*>,
        variable: Int? = null,
        stableIds: Boolean = false
    ) : this(listOf(), variable, stableIds) {
        liveList = liveData
    }

    /**
     * Adapter which queries the data every time it's used
     */
    @JvmOverloads
    @ExperimentalContracts
    private constructor(
        dynamicData: () -> List<Any>,
        variable: Int? = null,
        stableIds: Boolean = false
    ) : this(listOf(), variable, stableIds) {
        dynamicList = dynamicData
    }

    /**
     * Internal data for the adapter, containing all the items the adapter will display
     */
    var dataList: List<Any> = listOf()

    /**
     * Extension of [list] which is always non-null. Includes [dynamicList]
     */
    private val fullList get() = dynamicList?.invoke() ?: dataList

    /**
     * LiveData bound list.
     *
     * Items are updated to [list] whenever the backing of this field updates.
     */
    var liveList: MutableLiveData<*>? = null
        set(value) {
            @Suppress("UNCHECKED_CAST")
            val castValue = value as? MutableLiveData<List<Any>>

            field = castValue
            recyclerView?.findViewTreeLifecycleOwner()?.also { lifecycle ->
                castValue?.observe(lifecycle) {
                    dataList = it
                    notifyDataSetChanged()
                }
            }
        }

    private var dynamicList: (() -> List<Any>)? = null

    private val DATA_INVALIDATION = Any()
    private val callback = ObservableListCallback(this)
    private var recyclerView: RecyclerView? = null

    private val map = mutableMapOf<Class<*>, BaseType>()
    private var dynamicMap: ((Any) -> BaseType?)? = null
    private var layoutHandler: LayoutHandler? = null
    private var typeHandler: TypeHandler? = null

    init {
        dataList = list.orEmpty()
        setHasStableIds(stableIds)
    }

    /**
     * Links [clazz] to a layout resource.
     *
     * Data will be bound by [variable] or [adapter variable][LastAdapter.variable]
     *
     * **Example**
     * ```kotlin
     * LastAdapter(...)
     *   .map(Header::class.java, R.layout.item_header)
     *   .map(Body::class.java, R.layout.item_body, BR.body1)
     *   .into(recyclerView)
     * ```
     */
    @JvmOverloads
    fun <T : Any> map(clazz: Class<T>, layout: Int, variable: Int? = null) =
        apply { map[clazz] = BaseType(layout, variable) }

    /**
     * Links [clazz] to a layout resource.
     *
     * Data will be bound by [variable] or [adapter variable][LastAdapter.variable]
     *
     * **Example**
     * ```kotlin
     * LastAdapter(...)
     *   .map(Header::class, R.layout.item_header)
     *   .map(Body::class, R.layout.item_body, BR.body1)
     *   .into(recyclerView)
     * ```
     */
    @JvmOverloads
    fun <T : Any> map(clazz: KClass<T>, layout: Int, variable: Int? = null) =
        apply { map[clazz.java] = BaseType(layout, variable) }

    /**
     * Links [data][T] to a layout resource.
     *
     * Data will be bound by [variable] or [adapter variable][LastAdapter.variable]
     *
     * **Example**
     * ```kotlin
     * LastAdapter(...)
     *   .map<Header>(R.layout.item_header)
     *   .into(recyclerView)
     * ```
     */
    inline fun <reified T : Any> map(layout: Int, variable: Int? = null) =
        map(T::class.java, layout, variable)

    /**
     * Links a java class to a ViewBinding [AbsType]
     *
     * **Example**
     * ```kotlin
     * LastAdapter(...)
     *   .map(Header::class.java, headerType)
     *   .into(recyclerView)
     * ```
     */
    fun <T : Any> map(clazz: Class<T>, type: AbsType<*>) = apply { map[clazz] = type }

    /**
     * Links a kotlin class to a ViewBinding [AbsType]
     *
     * **Example**
     * ```kotlin
     * LastAdapter(...)
     *   .map(Header::class, headerType)
     *   .into(recyclerView)
     * ```
     */
    fun <T : Any> map(clazz: KClass<T>, type: AbsType<*>) = apply { map[clazz.java] = type }

    /**
     * Dynamic linking of [data][Any] to a [mapping]
     *
     * **Example**
     * ```kotlin
     * LastAdapter(...)
     *   .map { data: Any ->
     *     when (data) {
     *         is Header -> HeaderType
     *         is Body -> object : ItemType<ItemHeaderBinding>(R.layout.item_header) {...}
     *         else -> null
     *     }
     *   }
     *   .map<...>(...)
     * ```
     */
    fun map(mapping: (data: Any) -> BaseType?) = apply { dynamicMap = mapping }

    /**
     * Links [data][T] to a ViewBinding [AbsType].
     *
     * **Example**
     * ```kotlin
     * val customType = object : ItemType<ItemHeaderBinding>(R.layout.item_header) {...}
     *
     * LastAdapter(...)
     *   .map<Header>(customType)
     *   .into(recyclerView)
     * ```
     */
    inline fun <reified T : Any> map(type: AbsType<*>) = map(T::class.java, type)

    /**
     * Links a [data][T] to a ViewBinding [AbsType].
     *
     * **Example**
     * ```kotlin
     * LastAdapter(...)
     *   .map<Header> {
     *     object : ItemType<ItemHeaderBinding>(R.layout.item_header) {...}
     *   }
     *   .into(recyclerView)
     * ```
     */
    inline fun <reified T : Any> map(type: () -> AbsType<*>) = map(T::class.java, type())

    /**
     * Links [data][T] to a [ViewDataBinding] and [configuration][config].
     *
     * **Example**
     * ```kotlin
     * LastAdapter(...)
     *   .map<Header, ItemHeaderBinding>(R.layout.item_header)
     *   .map<Body, ItemBodyBinding>(R.layout.item_body){...}
     *   .into(recyclerView)
     * ```
     */
    inline fun <reified T : Any, B : ViewDataBinding> map(
        layout: Int,
        variable: Int? = null,
        noinline config: (Type<B>.() -> Unit)? = null
    ) = map(T::class.java, Type<B>(layout, variable).apply { config?.invoke(this) })

    /**
     * External function to supply a [LayoutHandler] or [TypeHandler].
     *
     * **Example**
     * ```kotlin
     * val customLayout: LayoutHandler = object : LayoutHandler {
     *   override fun getItemLayout(item: Any, position: Int): Int {
     *     return if (item is Header)
     *       if (position == 0) R.layout.item_header_first else R.layout.item_header
     *         else R.layout.item_point
     *     }
     * }
     *
     * LastAdapter(...)
     *   .handler(customLayout)
     *   .into(recyclerView)
     * ```
     */
    fun handler(handler: Handler) = apply {
        when (handler) {
            is LayoutHandler -> {
                if (variable == null)
                    throw IllegalStateException("No variable specified in LastAdapter constructor")
                layoutHandler = handler
            }

            is TypeHandler -> typeHandler = handler
        }
    }

    /**
     * Custom [layout][AbsType] based on item and position.
     *
     * **Example**
     * ```kotlin
     * LastAdapter(...)
     *   .layout { item, position ->
     *     when (item) {
     *       is Header -> if (position == 0) R.layout.item_header_big else R.layout.item_header
     *       is Tweet -> if (item.isRetweet) R.layout.item_retweet else R.layout.item_tweet
     *       is User -> R.layout.item_user
     *       else -> -1
     *   }
     * ```
     */
    inline fun layout(crossinline find: (Any, Int) -> Int) = handler(object : LayoutHandler {
        override fun getItemLayout(item: Any, position: Int) = find(item, position)
    })

    /**
     * Custom [type][AbsType] configuration based on item and position.
     *
     * **Example**
     * ```kotlin
     * LastAdapter(...)
     *   .type { item, position ->
     *     when (item) {
     *       is Header -> headerType
     *       is Tweet -> {
     *         if (item.isFavorited) favoritedTweetType
     *         else if (item.isRetweet) retweetType
     *         else tweetType
     *       }
     *       else -> null
     *   }
     * ```
     */
    inline fun <B : ViewDataBinding> type(crossinline find: (Any, Int) -> AbsType<B>?) =
        handler(object : TypeHandler {
            override fun getItemType(item: Any, position: Int) = find(item, position)
        })

    /**
     * Location of where this adapter will be applied.
     *
     * This is REQUIRED if choosing to use this adapter on a recyclerview.
     */
    fun into(recyclerView: RecyclerView) = apply {
        if (recyclerView.layoutManager == null) // if none is set, then default until otherwise
            recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.adapter = this
    }

    override fun onCreateViewHolder(view: ViewGroup, viewType: Int): Holder<ViewDataBinding> {
        val inflater = LayoutInflater.from(view.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(inflater, viewType, view, false)
        val holder = Holder(binding)
        binding.addOnRebindCallback(object : OnRebindCallback<ViewDataBinding>() {
            override fun onPreBind(binding: ViewDataBinding) =
                recyclerView?.isComputingLayout ?: false

            override fun onCanceled(binding: ViewDataBinding) {
                if (recyclerView?.isComputingLayout != false) return
                val position = holder.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    notifyItemChanged(position, DATA_INVALIDATION)
                }
            }
        })
        return holder
    }

    override fun onBindViewHolder(holder: Holder<ViewDataBinding>, position: Int) {
        val type = getType(position) ?: return
        try {
            // setVariable can fail if the value is not the expected type
            if (holder.binding.setVariable(getVariable(type), fullList[position]))
                holder.binding.executePendingBindings()
        } catch (_: Exception) {
        }

        @Suppress("UNCHECKED_CAST")
        if (type is AbsType<*>) {
            if (!holder.created) {
                notifyCreate(holder, type as AbsType<ViewDataBinding>)
                holder.created = true
            }
            notifyBind(holder, type as AbsType<ViewDataBinding>)
        }
    }

    override fun onBindViewHolder(
        holder: Holder<ViewDataBinding>,
        position: Int,
        payloads: List<Any>
    ) {
        if (isForDataBinding(payloads))
            holder.binding.executePendingBindings()
        else
            super.onBindViewHolder(holder, position, payloads)
    }

    override fun onViewRecycled(holder: Holder<ViewDataBinding>) {
        val position = holder.bindingAdapterPosition
        if (position != RecyclerView.NO_POSITION && position < fullList.size) {
            val type = getType(position)
            if (type is AbsType<*>) {
                @Suppress("UNCHECKED_CAST")
                notifyRecycle(holder, type as AbsType<ViewDataBinding>)
            }
        }
    }

    override fun getItemId(position: Int): Long {
        val item = fullList[position]

        return when {
            !stableIds -> super.getItemId(position)
            item is StableId -> item.stableId
            else -> throw IllegalStateException("${item.javaClass.simpleName} must implement StableId interface.")
        }
    }

    override fun getItemCount() = fullList.size

    override fun onAttachedToRecyclerView(rv: RecyclerView) {
        if (recyclerView == null)
            (dataList as? ObservableList)?.addOnListChangedCallback(callback)

        recyclerView = rv
        rv.findViewTreeLifecycleOwner()?.also { lifecycle ->
            @Suppress("UNCHECKED_CAST")
            (liveList as? MutableLiveData<List<Any>>)?.observe(lifecycle) {
                dataList = it
                notifyDataSetChanged()
            }
        }
    }

    override fun onDetachedFromRecyclerView(rv: RecyclerView) {
        if (recyclerView == null)
            (dataList as? ObservableList)?.removeOnListChangedCallback(callback)

        recyclerView = null
    }

    override fun getItemViewType(position: Int) =
        layoutHandler?.getItemLayout(fullList[position], position)
            ?: typeHandler?.getItemType(fullList[position], position)?.layout
            ?: getType(position)?.layout
            ?: throw RuntimeException("Invalid object at position $position: ${fullList[position].javaClass}")

    private fun getType(position: Int) = typeHandler?.getItemType(fullList[position], position)
        ?: dynamicMap?.invoke(fullList[position])
        ?: map[fullList[position].javaClass]

    private fun getVariable(type: BaseType) = type.variable
        ?: variable
        ?: throw IllegalStateException("No variable specified for type ${type.javaClass.simpleName}")

    private fun isForDataBinding(payloads: List<Any>): Boolean {
        return when {
            payloads.isEmpty() -> false
            payloads.any { it != DATA_INVALIDATION } -> false
            else -> true
        }
    }

    private fun notifyCreate(holder: Holder<ViewDataBinding>, type: AbsType<ViewDataBinding>) {
        when (type) {
            is Type -> {
                setClickListeners(holder, type)
                type.onCreate?.invoke(holder)
            }

            is ItemType -> type.onCreate(holder)
        }
    }

    private fun notifyBind(holder: Holder<ViewDataBinding>, type: AbsType<ViewDataBinding>) {
        when (type) {
            is Type -> type.onBind?.invoke(holder)
            is ItemType -> type.onBind(holder)
        }
    }

    private fun notifyRecycle(holder: Holder<ViewDataBinding>, type: AbsType<ViewDataBinding>) {
        when (type) {
            is Type -> type.onRecycle?.invoke(holder)
            is ItemType -> type.onRecycle(holder)
        }
    }

    private fun setClickListeners(holder: Holder<ViewDataBinding>, type: Type<ViewDataBinding>) {
        val onClick = type.onClick
        if (onClick != null) {
            holder.itemView.setOnClickListener {
                onClick(holder)
            }
        }
        val onLongClick = type.onLongClick
        if (onLongClick != null) {
            holder.itemView.setOnLongClickListener {
                onLongClick(holder)
                true
            }
        }
    }
}
