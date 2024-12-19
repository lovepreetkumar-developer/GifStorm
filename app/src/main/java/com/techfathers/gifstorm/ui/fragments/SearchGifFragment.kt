package com.techfathers.gifstorm.ui.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.techfathers.gifstorm.BuildConfig
import com.techfathers.gifstorm.R
import com.techfathers.gifstorm.data.db.entities.ResultModel
import com.techfathers.gifstorm.databinding.*
import com.techfathers.gifstorm.models.SimpleStringModel
import com.techfathers.gifstorm.models.SocialMediaTypeModel
import com.techfathers.gifstorm.ui.adapters.AutoCompleteAdapter
import com.techfathers.gifstorm.ui.base.BaseCallback
import com.techfathers.gifstorm.ui.base.BaseCustomDialog
import com.techfathers.gifstorm.ui.base.BaseFragmentAdvance
import com.techfathers.gifstorm.ui.base.BaseItem
import com.techfathers.gifstorm.util.*
import com.techfathers.gifstorm.util.custom_classes.PaginationScrollListener
import com.techfathers.gifstorm.view_models.SearchGifViewModel
import com.techfathers.gifstorm.vm_factories.SearchGifViewModelFactory
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import java.io.*
import java.util.*
import kotlin.collections.HashMap

/**
 * Property of Techfathers, Inc @ 2021 All Rights Reserved.
 */

class SearchGifFragment : BaseFragmentAdvance<FragmentSearchGifBinding>(), KodeinAware,
    TextWatcher {

    override val kodein by kodein()

    private val mFactory: SearchGifViewModelFactory by instance()

    private lateinit var mSearchGifViewModel: SearchGifViewModel

    private var mTrendingTerms1Adapter = GroupAdapter<GroupieViewHolder>()
    private var mTrendingTerms2Adapter = GroupAdapter<GroupieViewHolder>()
    private var mGifAdapter = GroupAdapter<GroupieViewHolder>()
    private var mGifDialog: BaseCustomDialog<DialogGifBinding>? = null
    private var mShareAppBottomSheetDialog: BottomSheetDialog? = null

    override fun getFragmentLayout(): Int {
        return R.layout.fragment_search_gif
    }

    override fun onFragmentCreateView(savedInstanceState: Bundle?) {
        super.onFragmentCreateView(savedInstanceState)
        binding?.let {
            mainBinding = it
            initView()
        }
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(charSeq: CharSequence?, p1: Int, p2: Int, p3: Int) {
        charSeq?.let {
            if (it.isNotEmpty()) {
                mainBinding.llTrendingTerms.visibility = View.GONE
                autoCompleteSearchApi(it.toString())
            } else {
                hideSoftKeyboard(requireActivity())
                mainBinding.llTrendingTerms.visibility = View.VISIBLE
                mainBinding.rvGifs.visibility = View.GONE
            }
        }
    }

    override fun afterTextChanged(p0: Editable?) {

    }

    private fun initView() {
        setBaseCallback(baseCallback)
        initializeShareAppBottomSheet()
        mainBinding.toolbar.llSearchView.visibility = View.VISIBLE
        mSearchGifViewModel = ViewModelProvider(this, mFactory).get(SearchGifViewModel::class.java)
        mainBinding.rvTerms1.adapter = mTrendingTerms1Adapter
        mainBinding.rvTerms2.adapter = mTrendingTerms2Adapter
        mainBinding.rvGifs.adapter = mGifAdapter

        val layoutManager = mainBinding.rvGifs.layoutManager as StaggeredGridLayoutManager

        mainBinding.rvGifs.addOnScrollListener(object : PaginationScrollListener(layoutManager) {
            override fun loadMoreItems() {
                mSearchGifViewModel.isLoading = true
                searchGifsApi(mainBinding.toolbar.autoCompleteTextView.text.toString())
            }

            override val isLastPage: Boolean get() = mSearchGifViewModel.isLastPage
            override val isLoading: Boolean get() = mSearchGifViewModel.isLoading

        })

        mainBinding.toolbar.autoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            val selectedPoi = parent.adapter.getItem(position) as SimpleStringModel?
            selectedPoi?.let {
                hideSoftKeyboard(requireActivity())
                mGifAdapter.clear()
                mainBinding.toolbar.autoCompleteTextView.setText(it.name)
                searchGifsApi(it.name)
            }
        }

        mainBinding.toolbar.autoCompleteTextView.addTextChangedListener(this)

        mainBinding.toolbar.autoCompleteTextView.setOnEditorActionListener(object :
            TextView.OnEditorActionListener {
            override fun onEditorAction(
                textView: TextView?,
                actionId: Int,
                event: KeyEvent?
            ): Boolean {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mGifAdapter.clear()
                    hideSoftKeyboard(requireActivity())
                    mainBinding.toolbar.autoCompleteTextView.dismissDropDown()
                    searchGifsApi(mainBinding.toolbar.autoCompleteTextView.text.toString())
                    return true
                }
                return false
            }
        })

        getTrendingTermsApi()

        val args = arguments?.let { SearchGifFragmentArgs.fromBundle(it) }
        args?.let { it ->
            it.searchTerm?.let {
                mainBinding.toolbar.autoCompleteTextView.setText(it)
                searchGifsApi(it)
            }
        }
    }

    private val baseCallback = BaseCallback {
        when (it.id) {
            R.id.img_back -> goBack()
        }
    }

    private fun initializeShareAppBottomSheet() {
        mShareAppBottomSheetDialog =
            BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialogTheme)
        val shareSheetBinding: BottomSheetAppShareBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.bottom_sheet_app_share,
            null,
            false
        )

        mShareAppBottomSheetDialog?.setCanceledOnTouchOutside(true)
        mShareAppBottomSheetDialog?.setContentView(shareSheetBinding.root)
        val groupAdapter = GroupAdapter<GroupieViewHolder>()
        val list = mutableListOf<SocialMediaTypeModel>()
        for (index in Constants.SOCIAL_ICONS_LABEL.indices) {
            list.add(
                SocialMediaTypeModel(
                    Constants.SOCIAL_ICONS_LABEL[index],
                    Constants.SOCIAL_ICONS[index]
                )
            )
        }
        shareSheetBinding.rvItems.adapter = groupAdapter
        groupAdapter.addAll(list.toSocialMediaTypeItems())
    }

    private fun searchGifsApi(query: String) {

        mainBinding.progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val queryMap = HashMap<String, String>()
                queryMap["key"] = getTenorApiKey()
                queryMap["limit"] = Constants.PAGE_LIMIT
                queryMap["q"] = query
                if (mSearchGifViewModel.nextPage.isNotEmpty()) queryMap["pos"] =
                    mSearchGifViewModel.nextPage
                val response = mSearchGifViewModel.search(queryMap)
                mainBinding.progress.visibility = View.GONE
                if (response.results.isNotEmpty()) {
                    setGifsData(response.results)
                }
            } catch (ex: MyApiException) {
                mainBinding.progress.visibility = View.GONE
                ex.printStackTrace()
            } catch (ex: NoInternetException) {
                mainBinding.progress.visibility = View.GONE
                ex.printStackTrace()
            } catch (ex: Exception) {
                mainBinding.progress.visibility = View.GONE
                ex.printStackTrace()
            }
        }
    }

    private fun List<ResultModel>.toGifItems(): List<BaseItem<ResultModel, ItemGifBinding>> {
        return this.map {
            BaseItem(
                R.layout.item_gif,
                it,
                object : BaseItem.OnItemClickListener<ResultModel> {
                    override fun onItemClick(
                        view: View,
                        model: ResultModel,
                        position: Int
                    ) {
                        showGifDialog(model)
                    }
                }
            )
        }
    }

    private fun setGifsData(gifList: List<ResultModel>) {
        mSearchGifViewModel.isLoading = false
        if (mGifAdapter.itemCount == 0) {
            if (gifList.isEmpty()) {
                //mainBinding.lottieAnimation.playAnimation()
            } else {
                //mainBinding.lottieAnimation.pauseAnimation()
                mGifAdapter.addAll(gifList.toGifItems())
                mainBinding.rvGifs.visibility = View.VISIBLE
            }
        } else {
            //mainBinding.lottieAnimation.pauseAnimation()
            mGifAdapter.addAll(gifList.toGifItems())
            mainBinding.rvGifs.visibility = View.VISIBLE
        }
    }

    private fun showGifDialog(model: ResultModel) {
        mGifDialog = BaseCustomDialog(
            requireContext(),
            R.layout.dialog_gif,
            object : BaseCustomDialog.DialogListener {
                override fun onViewClick(view: View?) {
                    view?.let {
                        when (it.id) {
                            R.id.rl_like -> likeDislikeGif(model)
                            R.id.rl_hd -> {
                                findNavController().navigate(
                                    SearchGifFragmentDirections.actionSearchGifFragmentToGifDetailsFragment(
                                        model
                                    )
                                )
                                mGifDialog?.dismiss()
                            }
                            R.id.rl_share_gif -> {
                                mSearchGifViewModel.mSharingGifUrl = model.media[0].gif.url
                                mShareAppBottomSheetDialog?.show()
                            }
                            else -> mGifDialog?.dismiss()
                        }
                    }
                }
            })

        Objects.requireNonNull<Window>(mGifDialog?.window).setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        mGifDialog?.getBinding()?.model = model
        mGifDialog?.setCancelable(true)
        mGifDialog?.show()

        lifecycleScope.launch {
            mSearchGifViewModel.getLikedGif(model.id)?.let {
                Timber.d("" + it)
                //mGifDialog?.getBinding()?.model = it
            }
        }
    }

    private fun likeDislikeGif(model: ResultModel) {
        lifecycleScope.launch {
            if (model.liked) {
                mGifDialog?.getBinding()?.imgLike?.setImageResource(R.drawable.ic_heart_dark_gray)
                model.liked = false
                mSearchGifViewModel.deleteLikedGif(model.id)
            } else {
                mGifDialog?.getBinding()?.imgLike?.setImageResource(R.drawable.ic_heart_red)
                model.liked = true
                mSearchGifViewModel.insertLikedGif(model)
            }
        }
    }

    private fun autoCompleteSearchApi(query: String) {
        lifecycleScope.launch {
            try {
                val queryMap = HashMap<String, String>()
                queryMap["key"] = getTenorApiKey()
                queryMap["q"] = query
                val response = mSearchGifViewModel.autoCompleteSearch(queryMap)
                if (response.results.isNotEmpty()) setSearchResultsToAdapter(response.results)
            } catch (ex: MyApiException) {
                ex.printStackTrace()
            } catch (ex: NoInternetException) {
                ex.printStackTrace()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun setSearchResultsToAdapter(results: List<String>) {
        val listOfResults = mutableListOf<SimpleStringModel>()
        for (string in results) {
            listOfResults.add(SimpleStringModel(0, string, 0))
        }
        val adapter = AutoCompleteAdapter(requireContext(), listOfResults)
        mainBinding.toolbar.autoCompleteTextView.setAdapter(adapter)
        mainBinding.toolbar.autoCompleteTextView.threshold = 3
    }

    private fun getTrendingTermsApi() {

        mainBinding.progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = mSearchGifViewModel.trendingTerms(getTenorApiKey())
                mainBinding.progress.visibility = View.GONE
                if (response.results.isNotEmpty()) {
                    setTrendingTermsData(response.results)
                }
            } catch (ex: MyApiException) {
                mainBinding.progress.visibility = View.GONE
                ex.printStackTrace()
            } catch (ex: NoInternetException) {
                mainBinding.progress.visibility = View.GONE
                ex.printStackTrace()
            } catch (ex: Exception) {
                mainBinding.progress.visibility = View.GONE
                ex.printStackTrace()
            }
        }
    }

    private fun setTrendingTermsData(tags: List<String>) {
        val colorsList = mutableListOf<Int>()
        val colorsListStaticSize = tags.size / 10

        for (i in 0..colorsListStaticSize) {
            if ((colorsList.size + 9) > tags.size) {
                val restOfColors = tags.size - colorsList.size

                for (colorIndex in 0 until restOfColors) {
                    colorsList.add(colorsList[colorIndex])
                }
            } else {
                colorsList.add(R.drawable.bg_gradient_0)
                colorsList.add(R.drawable.bg_gradient_1)
                colorsList.add(R.drawable.bg_gradient_2)
                colorsList.add(R.drawable.bg_gradient_3)
                colorsList.add(R.drawable.bg_gradient_4)
                colorsList.add(R.drawable.bg_gradient_5)
                colorsList.add(R.drawable.bg_gradient_6)
                colorsList.add(R.drawable.bg_gradient_7)
                colorsList.add(R.drawable.bg_gradient_8)
                colorsList.add(R.drawable.bg_gradient_9)
            }
        }

        val listOfResults = mutableListOf<SimpleStringModel>()
        for (trendingTerm in tags.withIndex()) {
            listOfResults.add(
                SimpleStringModel(
                    0,
                    trendingTerm.value,
                    colorsList[trendingTerm.index]
                )
            )
        }

        mTrendingTerms1Adapter.addAll(
            listOfResults.subList(
                0,
                listOfResults.size / 2
            ).toTrendingTermItems()
        )
        mTrendingTerms2Adapter.addAll(
            listOfResults.subList(
                listOfResults.size / 2,
                listOfResults.size
            ).toTrendingTermItems()
        )

        if (mTrendingTerms2Adapter.itemCount > 5)
            mainBinding.rvTerms2.layoutManager?.scrollToPosition(5)
    }

    private fun List<SimpleStringModel>.toTrendingTermItems(): List<BaseItem<SimpleStringModel, ItemTrendingTermBinding>> {
        return this.map {
            BaseItem(
                R.layout.item_trending_term,
                it,
                object : BaseItem.OnItemClickListener<SimpleStringModel> {
                    override fun onItemClick(
                        view: View,
                        model: SimpleStringModel,
                        position: Int
                    ) {
                        hideSoftKeyboard(requireActivity())
                        mainBinding.toolbar.autoCompleteTextView.setText(it.name)
                        mainBinding.toolbar.autoCompleteTextView.isFocusableInTouchMode = true
                        mainBinding.toolbar.autoCompleteTextView.requestFocus()
                        mainBinding.llTrendingTerms.visibility = View.GONE
                        mGifAdapter.clear()
                        searchGifsApi(model.name)
                    }
                }
            )
        }
    }

    private fun List<SocialMediaTypeModel>.toSocialMediaTypeItems(): List<BaseItem<SocialMediaTypeModel, ItemSocialMediaBinding>> {
        return this.map {
            BaseItem(
                R.layout.item_social_media,
                it,
                object : BaseItem.OnItemClickListener<SocialMediaTypeModel> {
                    override fun onItemClick(
                        view: View,
                        model: SocialMediaTypeModel,
                        position: Int
                    ) {
                        askRequiredPermissions(position)
                    }
                }
            )
        }
    }

    private fun askRequiredPermissions(position: Int) {

        /**Asking storage permission from user*/

        Dexter.withContext(requireContext())
            .withPermissions(*Constants.STORAGE_PERMISSIONS)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        when {
                            report.areAllPermissionsGranted() -> {
                                downloadImage(position)
                            }
                            report.isAnyPermissionPermanentlyDenied -> {
                                showPermissionsAlert()
                            }
                            else -> {
                                requireContext().showToast(getString(R.string.required_permissions_not_granted))
                            }
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    // Remember to invoke this method when the custom rationale is closed
                    // or just by default if you don't want to use any custom rationale.
                    token?.continuePermissionRequest()
                }
            })
            .withErrorListener {
                requireContext().showToast(it.name)
            }
            .check()
    }

    private fun downloadImage(position: Int) {
        mShareAppBottomSheetDialog?.dismiss()
        Glide.with(requireContext()).asFile()
            .load(mSearchGifViewModel.mSharingGifUrl)
            .apply(
                RequestOptions()
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .override(Target.SIZE_ORIGINAL)
            )
            .into(object : Target<File> {
                override fun onStart() {}
                override fun onStop() {}
                override fun onDestroy() {}
                override fun onLoadFailed(errorDrawable: Drawable?) {}
                override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                    storeImage(resource, position)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
                override fun getSize(cb: SizeReadyCallback) {}
                override fun removeCallback(cb: SizeReadyCallback) {}
                override fun setRequest(request: Request?) {}
                override fun onLoadStarted(placeholder: Drawable?) {
                    requireContext().showToast(getString(R.string.please_wait_for_a_second))
                }

                override fun getRequest(): Request? {
                    return null
                }
            })
    }

    private fun storeImage(resourceFile: File, position: Int) {
        try {
            val mediaStorageDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .toString() + File.separator + "GifStorm"
            )
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    requireContext().showToast("Not having write permission")
                    return
                }
            }
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .toString() + File.separator + "GifStorm",
                "shared_gif" + System.currentTimeMillis() + ".gif"
            )
            val inputStream = FileInputStream(resourceFile)
            val bis = BufferedInputStream(inputStream)
            val byteArrayOutputStream = ByteArrayOutputStream()
            var current: Int
            while (bis.read().also { current = it } != -1) {
                byteArrayOutputStream.write(current)
            }
            val fos = FileOutputStream(file)
            fos.write(byteArrayOutputStream.toByteArray())
            fos.flush()
            fos.close()
            inputStream.close()
            //finally share image
            shareImage(file, position)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    private fun shareImage(pictureFile: File, position: Int) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, requireContext().getPlayStoreAppLink())
        val photoURI = FileProvider.getUriForFile(
            requireContext(),
            requireContext().packageName + ".provider",
            pictureFile
        )
        shareIntent.setDataAndType(photoURI, "*/*")
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        shareIntent.putExtra(Intent.EXTRA_STREAM, photoURI)

        try {
            when (position) {
                0 -> shareIntent.setPackage("com.facebook.katana") //Facebook
                1 -> shareIntent.setPackage("com.facebook.orca") //Fb messenger
                2 -> shareIntent.setPackage("com.whatsapp") //Whatsapp
                3 -> shareIntent.setPackage("com.instagram.android") //Instagram
                4 -> shareIntent.setPackage("com.twitter.android") //Twitter
                5 -> shareIntent.setPackage("com.reddit.frontpage") //Reddit
                6 -> shareIntent.setPackage("com.tencent.mm") //We Chat
                7 -> shareIntent.setPackage("com.google.android.talk") //Google Hangout
                8 -> shareIntent.setPackage("org.telegram.messenger") //Telegram
                9 -> shareIntent.setPackage("com.snapchat.android") //SnapChat
                10 -> shareIntent.setPackage("com.viber.voip") //Viber
                11 -> shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_content))
                12 -> startActivity(Intent.createChooser(shareIntent, "Share Gif"))
            }
            startActivity(shareIntent)
        } catch (ex: ActivityNotFoundException) {
            requireActivity().showToast(getString(R.string.application_not_found))
        }
    }

    private fun showPermissionsAlert() {
        val title = getString(R.string.permission_required)
        val message = getString(R.string.permission_msg)
        val positiveText = getString(R.string.permission_goto)
        val negativeText = getString(R.string.cancel)
        requireContext().showAlert(
            title,
            message,
            positiveText,
            negativeText,
            "",
            object : OnPositive {
                override fun onYes() {
                    openSettings()
                }
            })
    }

    private fun openSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.fromParts("package", requireContext().packageName, null)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}