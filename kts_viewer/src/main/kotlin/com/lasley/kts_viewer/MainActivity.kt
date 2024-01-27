package com.lasley.kts_viewer

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.widget.doAfterTextChanged
import androidx.viewbinding.ViewBinding
import com.github.nitrico.lastadapter.LastAdapter
import com.google.android.material.snackbar.Snackbar
import com.lasley.kts_viewer.data.ActionColor
import com.lasley.kts_viewer.data.Album
import com.lasley.kts_viewer.data.Artist
import com.lasley.kts_viewer.data.CommonInf
import com.lasley.kts_viewer.data.LocalDatabase
import com.lasley.kts_viewer.data.SaveResult
import com.lasley.kts_viewer.databinding.ActivityMainBinding
import com.lasley.kts_viewer.databinding.AddalbumDialogBinding
import com.lasley.kts_viewer.databinding.AddartistDialogBinding
import com.lasley.kts_viewer.databinding.ConfigDataBinding
import com.lasley.kts_viewer.databinding.ContentViewBinding
import com.lasley.kts_viewer.extensions.showWithLifecycle
import com.lasley.kts_viewer.helpers.BindingDialog
import com.lasley.kts_viewer.helpers.cast
import com.lasley.kts_viewer.helpers.collapse
import com.lasley.kts_viewer.helpers.expand

class MainActivity : AppCompatActivity() {

    private val animationSpeed = 2.5f
    private lateinit var binding: ActivityMainBinding

    private val viewModel: ActivityViewModel by viewModels()

    private val adapter by lazy { LastAdapter(viewModel.liveAlbumData, BR.item) }
    private val rootContext: Context
        get() = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater, null, false)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)


        if (!viewModel.resolverExists)
            dialogProviderMissing()
        else {
            binding.loadingText.visibility = View.VISIBLE
            binding.loadingSpinner.visibility = View.VISIBLE

            viewModel.loadData {
                binding.loadingText.visibility = View.GONE
                binding.loadingSpinner.visibility = View.GONE
                binding.contents.visibility = View.VISIBLE

                adapter.map<Album, ContentViewBinding>(R.layout.content_view) {
                    onBind { contentController(it.bindingAdapterPosition, it.binding) }
                }.into(binding.contents)
            }
        }

        binding.reloadLayout.actionReload.setOnClickListener {
            viewModel.reloadReady.postValue(false)
            binding.loadingSpinner.visibility = View.VISIBLE
            viewModel.loadData {
                binding.loadingSpinner.visibility = View.GONE
            }
        }
        viewModel.reloadReady.observe(this) {
            with(binding.reloadLayout.root) {
                if (it && visibility == View.GONE)
                    expand() else collapse()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.homepage, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.homepage, menu)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_addAlbum -> addAlbumDialog()
            R.id.action_addArtist -> addArtistDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addAlbumDialog() {
        BindingDialog<AddalbumDialogBinding>(this) { diag ->
            val root = this
            val artistList = viewModel.artists

            configContents.selectArtist.adapter = ArrayAdapter(
                diag.context,
                android.R.layout.simple_spinner_item,
                artistList.map { it.first }
            ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

            fun saveData() {
                with(configContents) {
                    // returns data, which is found to be duplicated
                    // only applies to the "add artist" state
                    val duplicateItem = when {
                        !addArtistSwitch.isChecked -> null
                        else -> artistList.firstOrNull {
                            it.first == editArtist.text.toString()
                        }
                    }

                    fun saveAction() {
                        val useArtist: Artist = when {
                            duplicateItem != null -> LocalDatabase[duplicateItem.second]

                            !addArtistSwitch.isChecked ->
                                LocalDatabase[artistList[selectArtist.selectedItemPosition].second]

                            else -> Artist.create {
                                name = editArtist.text.toString()
                            }
                        }.cast()!!

                        val updateData = Album.create(editAlbum.text.toString(), useArtist)

                        diag.setCancelable(false)
                        root.root.background = ColorDrawable(ActionColor.Loading.color)
                        loadingSpinner.visibility = View.VISIBLE
                        controlState(root, false)

                        val resultAction: (SaveResult, String) -> Unit = { result, message ->
                            loadingSpinner.visibility = View.VISIBLE
                            controlState(root, true)
                            diag.setCancelable(true)

                            when (result) {
                                SaveResult.Success -> {
                                    root.root.background = ColorDrawable(Color.WHITE)
                                    diag.dismiss()
                                }

                                SaveResult.Error -> {
                                    root.root.background = ColorDrawable(ActionColor.Error.color)
                                }

                                else -> Unit
                            }
                        }

                        viewModel.updateData(listOf(useArtist, updateData), resultAction)
                    }

                    saveAction()
                }
            }

            fun updateSaveBtn() {
                with(configContents) {
                    actionAdd.isEnabled = when {
                        editAlbum.length() == 0 -> false
                        !addArtistSwitch.isChecked -> true // as "select artist"
                        editArtist.length() == 0 -> false
                        else -> true
                    }
                }
            }

            with(configContents) {
                editAlbum.doAfterTextChanged {
                    editAlbumLayout.error = if (it.isNullOrBlank())
                        getString(R.string.error_emptyInput) else ""
                    updateSaveBtn()
                }
                editArtist.doAfterTextChanged {
                    editArtistLayout.error = if (it.isNullOrBlank())
                        getString(R.string.error_emptyInput) else ""
                    updateSaveBtn()
                }

                addArtistSwitch.apply {
                    fun updateSwitchText(checked: Boolean) {
                        switchArtistText.text = if (checked)
                            getString(R.string.switch_AddArtist)
                        else
                            getString(R.string.switch_EditArtist)

                    }
                    updateSwitchText(isChecked)

                    setOnCheckedChangeListener { _, isChecked ->
                        updateSaveBtn()
                        updateSwitchText(isChecked)
                        if (isChecked) {
                            editArtistLayout.visibility = View.VISIBLE
                            selectArtistLayout.visibility = View.GONE
                        } else {
                            editArtistLayout.visibility = View.GONE
                            selectArtistLayout.visibility = View.VISIBLE
                        }
                    }
                }
            }

            actionAdd.setOnClickListener { saveData() }
            actionCancel.setOnClickListener { diag.dismiss() }
        }.showWithLifecycle()
    }

    private fun addArtistDialog() {
        BindingDialog<AddartistDialogBinding>(this) { diag ->
            val root = this
            val artistList = viewModel.artists

            fun saveData() {
                val chosenName = editArtist.text.toString()
                val duplicate = artistList.any { it.first == chosenName }

                if (duplicate) {
                    AlertDialog.Builder(rootContext)
                        .setTitle("Duplicate name")
                        .setMessage("The chosen name already exists.")
                        .setPositiveButton(R.string.dialog_ok) { dupDiag, _ -> dupDiag.dismiss() }
                        .create().show()
                    return
                }

                val newData = Artist.create { name = chosenName }

                diag.setCancelable(false)
                controlState(root, false)
                root.root.background = ColorDrawable(ActionColor.Loading.color)
                loadingSpinner.visibility = View.VISIBLE

                viewModel.updateData(listOf(newData)) { result, message ->
                    loadingSpinner.visibility = View.VISIBLE
                    controlState(root, true)
                    diag.setCancelable(true)
                    when (result) {
                        SaveResult.Success -> {
                            diag.dismiss()
                            root.root.background = ColorDrawable(Color.WHITE)
                        }

                        SaveResult.Error -> {
                            root.root.background = ColorDrawable(ActionColor.Error.color)
                        }

                        else -> Unit
                    }
                }
            }

            editArtist.doAfterTextChanged {
                editArtistLayout.error = if (it.isNullOrBlank())
                    getString(R.string.error_emptyInput) else ""
                actionAdd.isEnabled = editArtist.length() > 0
            }
            actionAdd.setOnClickListener { saveData() }
            actionCancel.setOnClickListener { diag.dismiss() }
        }.showWithLifecycle()
    }

    private fun contentController(
        dataIndex: Int,
        content: ContentViewBinding
    ) {
        val bindingItem: Album = content.item ?: return
        var updatingContent = false
        // [Name] to [uuid]
        val artistList = viewModel.artists

        fun resetEditing() {
            val artistID = bindingItem.artistID
            val artistData = artistList.firstOrNull { it.second == artistID }
                ?: return
            val artistIndex = artistList.indexOf(artistData)

            with(content.configContents) {
                editAlbum.setText(bindingItem.name.orEmpty())
                editArtist.setText(artistData.first)
                selectArtist.setSelection(artistIndex)
            }
        }

        fun saveData() {
            updatingContent = true
            controlState(content, false)

            fun saveAction(updateData: List<CommonInf>) {
                with(content) {
                    loadingSpinner.visibility = View.VISIBLE
                    layoutEdit.background = ColorDrawable(ActionColor.Loading.color)

                    viewModel.updateData(updateData) { result, message ->
                        updatingContent = false
                        loadingSpinner.visibility = View.GONE
                        controlState(content, true)

                        when (result) {
                            SaveResult.Success -> {
                                layoutEdit.background = ColorDrawable(ActionColor.Edit.color)
                                layoutEdit.collapse {
                                    layoutControls.collapse {
                                        // Livedata will update everything anyway,
                                        // .. until more updates into lastAdapter
//                                        val newIndex =
//                                            viewModel.albums.indexOfFirst { it.uuid == bindingItem.uuid }
//                                        val fromIndex = min(newIndex, dataIndex)
//                                        val range = max(newIndex,dataIndex) - fromIndex
//                                        adapter.notifyItemRangeChanged(fromIndex, range)
                                        // we can't guarantee dataIndex will be the active index anyway
                                        //   adapter.notifyItemChanged(dataIndex)
                                    }
                                }
                            }

                            SaveResult.Error -> {
                                WaitTimer(3000).onFinish {
                                    layoutEdit.background = ColorDrawable(ActionColor.Edit.color)
                                }.start()
                                layoutEdit.background = ColorDrawable(ActionColor.Error.color)
                                // Toast.makeText(baseContext, message, Toast.LENGTH_SHORT).show()
                            }

                            else -> Unit
                        }
                    }
                }
            }

            with(content.configContents) {
                // selected, or new, artist name
                val artistName = when {
                    !addArtistSwitch.isChecked -> selectArtist.selectedItem.toString()
                    else -> editArtist.text.toString()
                }

                val artistID = if (!addArtistSwitch.isChecked)
                // we're selecting a known artist
                    artistList[selectArtist.selectedItemPosition].second
                else {
                    // did they type the contents of a known artist?
                    artistList.firstOrNull {
                        it.first == editArtist.text.toString()
                    }?.second
                }

                val useArtist = if (artistID != null)
                    LocalDatabase[artistID] as Artist
                else
                    Artist.create {
                        name = editArtist.text.toString()
                    }

                val newData = bindingItem.copy(
                    name = editAlbum.text.toString(),
                    artistID = useArtist.uuid
                )
                useArtist.addAlbum(newData)

                if (addArtistSwitch.isChecked && useArtist.uuid != bindingItem.artistID) {
                    val oldArtist = bindingItem.artist
                    oldArtist?.removeAlbum(bindingItem)
                    saveAction(listOfNotNull(oldArtist, useArtist, newData))
                } else
                    saveAction(listOf(newData))
            }
        }

        fun removeData() {
            updatingContent = true

            with(content) {
                controlState(this, false)
                loadingSpinner.visibility = View.VISIBLE
                layoutEdit.background = ColorDrawable(ActionColor.Loading.color)
                viewModel.removeData(bindingItem) { result, message ->
                    updatingContent = false
                    loadingSpinner.visibility = View.GONE
                    controlState(this, true)

                    when (result) {
                        SaveResult.Success -> {
                            layoutEdit.background = ColorDrawable(ActionColor.Edit.color)
                            layoutEdit.collapse {
                                layoutControls.collapse {
//                                    val itemIndex = viewModel.listCache.indexOf(bindingItem)
                                    adapter.notifyItemRemoved(dataIndex)
                                }
                            }
                        }

                        SaveResult.Error -> {
                            WaitTimer(3000).onFinish {
                                layoutEdit.background = ColorDrawable(ActionColor.Edit.color)
                            }.start()
                            layoutEdit.background = ColorDrawable(ActionColor.Error.color)
                            // Toast.makeText(baseContext, message, Toast.LENGTH_SHORT).show()
                        }

                        else -> Unit
                    }
                }
            }
        }

        fun updateSaveBtn() {
            with(content.configContents) {
                content.actionSave.isEnabled = when {
                    editAlbum.length() == 0 -> false
                    !addArtistSwitch.isChecked -> true // as "select artist"
                    editArtist.length() == 0 -> false
                    else -> true
                }
            }
        }

        val editingAction = View.OnClickListener {
            with(content) {
                if (layoutEdit.visibility == View.GONE)
                    layoutEdit.expand(animationSpeed)
                else
                    layoutEdit.collapse(animationSpeed) {
                        if (it == actionCancel) resetEditing()
                    }
            }
        }

        with(content) {
            contentLayout.setOnClickListener {
                if (layoutControls.visibility == View.GONE)
                    layoutControls.expand(animationSpeed)
                else {
                    layoutControls.collapse(animationSpeed)
                    if (!updatingContent)
                        layoutEdit.collapse(animationSpeed)
                }
            }

            actionRemove.setOnClickListener { dialogRemove { removeData() } }

            actionEdit.setOnClickListener(editingAction)
            actionCancel.setOnClickListener(editingAction)
            actionSave.setOnClickListener { saveData() }

            layoutEdit.background = ColorDrawable(ActionColor.Edit.color)

            with(configContents) {
                editAlbum.doAfterTextChanged {
                    editAlbumLayout.error = if (it.isNullOrBlank())
                        getString(R.string.error_emptyInput) else ""
                    updateSaveBtn()
                }
                editArtist.doAfterTextChanged {
                    editArtistLayout.error = if (it.isNullOrBlank())
                        getString(R.string.error_emptyInput) else ""
                    updateSaveBtn()
                }

                addArtistSwitch.apply {
                    fun updateSwitchText(checked: Boolean) {
                        switchArtistText.text = if (checked)
                            getString(R.string.switch_AddArtist)
                        else
                            getString(R.string.switch_EditArtist)

                    }
                    updateSwitchText(isChecked)

                    setOnCheckedChangeListener { _, isChecked ->
                        updateSaveBtn()
                        updateSwitchText(isChecked)
                        if (isChecked) {
                            editArtistLayout.visibility = View.VISIBLE
                            selectArtistLayout.visibility = View.GONE
                        } else {
                            editArtistLayout.visibility = View.GONE
                            selectArtistLayout.visibility = View.VISIBLE
                        }
                    }
                }

                selectArtist.apply {
                    adapter = ArrayAdapter(
                        context,
                        android.R.layout.simple_spinner_item,
                        artistList.map { it.first }
                    ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

                    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            itemArtist.text = artistList[position].first
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
                }
            }
        }

        resetEditing()
    }

    private fun dialogProviderMissing() {
        AlertDialog.Builder(rootContext)
            .setTitle(R.string.error_error_title)
            .setMessage(R.string.error_noProvider_mgs)
            .setPositiveButton(R.string.dialog_ok) { _, _ -> finish() }
            .create().show()
    }

    private fun dialogRemove(confirmed: () -> Unit) {
        AlertDialog.Builder(rootContext)
            .setTitle(R.string.dialog_delete_title)
            .setMessage(R.string.dialog_delete_msg)
            .setCancelable(true)
            .setPositiveButton(R.string.dialog_yes) { _, _ -> confirmed() }
            .setNegativeButton(R.string.dialog_no) { dialog, _ -> dialog.dismiss() }
            .create().show()
    }

    private fun dialogMerge(confirmed: () -> Unit) {
        AlertDialog.Builder(rootContext)
            .setTitle(R.string.dialog_merge_title)
            .setMessage(R.string.dialog_merge_msg)
            .setCancelable(true)
            .setPositiveButton(R.string.dialog_ok) { _, _ -> confirmed() }
            .setNegativeButton(R.string.dialog_cancel) { dialog, _ -> dialog.dismiss() }
            .create().show()
    }

    /**
     * Quick action to enable/ disable all the controls of a container.
     *
     * @param state Enabled state of the content controls
     * - true: Enabled
     * - false: Disabled
     */
    private fun controlState(
        content: ViewBinding,
        state: Boolean
    ) {
        when (content) {
            is AddartistDialogBinding -> with(content) {
                actionAdd.isEnabled = state
                actionCancel.isEnabled = state
            }

            is AddalbumDialogBinding -> with(content) {
                actionAdd.isEnabled = state
                actionCancel.isEnabled = state
                controlState(content.configContents, state)
            }

            is ContentViewBinding -> with(content) {
                actionEdit.isEnabled = state
                actionRemove.isEnabled = state
                actionSave.isEnabled = state
                actionCancel.isEnabled = state
                controlState(content.configContents, state)
            }

            is ConfigDataBinding -> with(content) {
                editAlbum.isEnabled = state
                selectArtist.isEnabled = state
                editArtist.isEnabled = state
                addArtistSwitch.isEnabled = state
                addArtistSwitch.isEnabled = state
            }
        }
    }
}