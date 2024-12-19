package com.techfathers.gifstorm.ui.fragments

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.net.Uri.decode
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64.DEFAULT
import android.util.Base64.decode
import android.view.View
import android.view.Window
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
import com.techfathers.gifstorm.R
import com.techfathers.gifstorm.data.db.entities.ResultModel
import com.techfathers.gifstorm.databinding.*
import com.techfathers.gifstorm.models.CategoryModel
import com.techfathers.gifstorm.models.SocialMediaTypeModel
import com.techfathers.gifstorm.ui.base.BaseCallback
import com.techfathers.gifstorm.ui.base.BaseCustomDialog
import com.techfathers.gifstorm.ui.base.BaseFragmentAdvance
import com.techfathers.gifstorm.ui.base.BaseItem
import com.techfathers.gifstorm.util.*
import com.techfathers.gifstorm.util.custom_classes.PaginationScrollListener
import com.techfathers.gifstorm.view_models.HomeViewModel
import com.techfathers.gifstorm.vm_factories.HomeViewModelFactory
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import java.io.*
import java.util.*


/**
 * Property of Techfathers, Inc @ 2021 All Rights Reserved.
 */

class HomeFragment : BaseFragmentAdvance<FragmentHomeBinding>(), KodeinAware {

    override val kodein by kodein()

    private val mFactory: HomeViewModelFactory by instance()

    private lateinit var mHomeViewModel: HomeViewModel

    private var mGifsAdapter = GroupAdapter<GroupieViewHolder>()
    private var mCategoriesAdapter = GroupAdapter<GroupieViewHolder>()
    private var mLikedGifAdapter = GroupAdapter<GroupieViewHolder>()
    private var mGifDialog: BaseCustomDialog<DialogGifBinding>? = null
    private var mShareAppBottomSheetDialog: BottomSheetDialog? = null

    override fun getFragmentLayout(): Int {
        return R.layout.fragment_home
    }

    override fun onFragmentCreateView(savedInstanceState: Bundle?) {
        super.onFragmentCreateView(savedInstanceState)
        binding?.let {
            mainBinding = it
            initView()
        }
    }

    private fun initView() {
        setBaseCallback(baseCallback)

        mainBinding.tvTitle.text = getString(R.string.trending)
        mHomeViewModel = ViewModelProvider(this, mFactory).get(HomeViewModel::class.java)
        mainBinding.rvGifs.adapter = mGifsAdapter
        mainBinding.rvLikedGifs.adapter = mLikedGifAdapter
        getTrendingGifsApi()
        val layoutManager = mainBinding.rvGifs.layoutManager as StaggeredGridLayoutManager

        mainBinding.rvGifs.addOnScrollListener(object : PaginationScrollListener(layoutManager) {
            override fun loadMoreItems() {
                mHomeViewModel.isLoading = true
                getTrendingGifsApi()
            }

            override val isLastPage: Boolean get() = mHomeViewModel.isLastPage
            override val isLoading: Boolean get() = mHomeViewModel.isLoading

        })
        loadLikedGifsFromLocal()
        initializeShareAppBottomSheet()
    }

    private val baseCallback = BaseCallback {
        when (it.id) {
            R.id.rl_trending -> selectBottomMenu(0)
            R.id.img_search -> selectBottomMenu(1)
            R.id.rl_liked_gifs -> selectBottomMenu(2)
            R.id.img_share_app -> askRequiredPermissions(true, 0)
        }
    }

    private fun selectBottomMenu(position: Int) {
        when (position) {
            0 -> {
                mainBinding.imgTrending.setImageResource(R.drawable.ic_trend_selected)
                mainBinding.imgFavourite.setImageResource(R.drawable.ic_heart_light_gray)
                mainBinding.tvTitle.text = getString(R.string.trending)
                mainBinding.rvLikedGifs.visibility = View.GONE
                /*mainBinding.lottieAnimation.visibility = View.VISIBLE*/
                mainBinding.rvGifs.visibility = View.VISIBLE
                mainBinding.rvCategories.visibility = View.VISIBLE
                if (mGifsAdapter.itemCount == 0) {
                    /*playLottieAnimation(mainBinding.lottieAnimation)*/
                    getTrendingGifsApi()
                }
            }
            1 -> findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToSearchGifFragment())
            2 -> {
                mainBinding.tvTitle.text = getString(R.string.liked)
                mainBinding.imgTrending.setImageResource(R.drawable.ic_trend_un_selected)
                mainBinding.imgFavourite.setImageResource(R.drawable.ic_heart_dark_gray)
                mainBinding.rvGifs.visibility = View.GONE
                mainBinding.rvCategories.visibility = View.GONE
                mainBinding.rvLikedGifs.visibility = View.VISIBLE
                loadLikedGifsFromLocal()
                /*if (mLikedGifAdapter.itemCount > 0) {
                        pauseLottieAnimation(mainBinding.lottieAnimation)
                        mainBinding.rvLikedGifs.visibility = View.VISIBLE
                    } else {
                        playLottieAnimation(mainBinding.lottieAnimation)
                    }*/
            }
        }
    }

    private fun loadLikedGifsFromLocal() {
        lifecycleScope.launch {
            val allLikedGifs = mHomeViewModel.getAllLikedGifs()
            allLikedGifs?.let {
                it.toGifItems().let { items ->
                    mHomeViewModel.mLikedItems.clear()
                    mHomeViewModel.mLikedItems.addAll(items)
                    mLikedGifAdapter.clear()
                    mLikedGifAdapter.addAll(items)
                }
            }
        }
    }

    private fun getTrendingGifsApi() {

        lifecycleScope.launch {
            try {
                val queryMap = HashMap<String, String>()
                queryMap["key"] = getTenorApiKey()
                queryMap["limit"] = Constants.PAGE_LIMIT
                if (mHomeViewModel.nextPage.isNotEmpty()) queryMap["pos"] = mHomeViewModel.nextPage
                val response = mHomeViewModel.trendingGifs(queryMap)
                if (response.results.isNotEmpty()) {
                    mHomeViewModel.nextPage = response.next
                    setGifsData(response.results)
                }
            } catch (ex: MyApiException) {
                ex.printStackTrace()
            } catch (ex: NoInternetException) {
                ex.printStackTrace()
            } catch (ex: Exception) {
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

    private fun ResultModel.toGifItem(): BaseItem<ResultModel, ItemGifBinding> {
        return BaseItem(
            R.layout.item_gif,
            this,
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

    private fun setGifsData(gifList: List<ResultModel>) {
        mHomeViewModel.isLoading = false
        if (mGifsAdapter.itemCount == 0) {
            if (gifList.isEmpty()) {
                //Empty View
            } else {
                mGifsAdapter.addAll(gifList.toGifItems())
                //mHomeViewModel.isLoading = false
                getCategoriesApi()
            }
        } else {
            if (gifList.isNotEmpty()) {
                mGifsAdapter.addAll(gifList.toGifItems())
            } else {
                Timber.d("test")
                //mHomeViewModel.isLoading = true
            }
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
                                    HomeFragmentDirections.actionHomeFragmentToGifDetailsFragment(
                                        model
                                    )
                                )
                                mGifDialog?.dismiss()
                            }
                            R.id.rl_share_gif -> {
                                mHomeViewModel.mSharingGifUrl = model.media[0].gif.url
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
            mHomeViewModel.getLikedGif(model.id)?.let {
                mGifDialog?.getBinding()?.model = it
            }
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

    private fun likeDislikeGif(model: ResultModel) {
        lifecycleScope.launch {
            if (model.liked) {
                mGifDialog?.getBinding()?.imgLike?.setImageResource(R.drawable.ic_heart_dark_gray)
                model.liked = false
                mHomeViewModel.deleteLikedGif(model.id)
                removeFromAdapter(model.id)
            } else {
                mGifDialog?.getBinding()?.imgLike?.setImageResource(R.drawable.ic_heart_red)
                model.liked = true
                mHomeViewModel.insertLikedGif(model)
                model.toGifItem().let { gifItem ->
                    mLikedGifAdapter.add(gifItem)
                    mHomeViewModel.mLikedItems.add(gifItem)
                }
            }
        }
    }

    private fun removeFromAdapter(id: String) {
        val item = mHomeViewModel.mLikedItems.single { it.model.id == id }
        mLikedGifAdapter.remove(item)
        mHomeViewModel.mLikedItems.remove(item)
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
                        askRequiredPermissions(false, position)
                    }
                }
            )
        }
    }

    private fun askRequiredPermissions(isAppSharing: Boolean, position: Int) {

        /**Asking storage permission from user*/

        Dexter.withContext(requireContext())
            .withPermissions(*Constants.STORAGE_PERMISSIONS)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        when {
                            report.areAllPermissionsGranted() -> {
                                if (isAppSharing) {
                                    shareScreenshot()
                                } else {
                                    downloadImage(position)
                                }
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

    private fun getCategoriesApi() {
        lifecycleScope.launch {
            try {
                val response = mHomeViewModel.categories(getTenorApiKey())
                if (response.tags.isNotEmpty()) {
                    setCategoriesData(response.tags)
                }
            } catch (ex: MyApiException) {
                ex.printStackTrace()
            } catch (ex: NoInternetException) {
                ex.printStackTrace()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun setCategoriesData(tags: List<CategoryModel>) {
        mainBinding.tvPoweredBy.visibility = View.GONE

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

        for (categoryModel in tags.withIndex()) {
            categoryModel.value.background = colorsList[categoryModel.index]
        }
        mCategoriesAdapter.addAll(tags.toCategoryItems())
        mainBinding.rvCategories.adapter = mCategoriesAdapter
        mainBinding.rvCategories.visibility = View.VISIBLE
    }

    private fun List<CategoryModel>.toCategoryItems(): List<BaseItem<CategoryModel, ItemCategoryBinding>> {
        return this.map {
            BaseItem(
                R.layout.item_category,
                it,
                object : BaseItem.OnItemClickListener<CategoryModel> {
                    override fun onItemClick(
                        view: View,
                        model: CategoryModel,
                        position: Int
                    ) {
                        findNavController().navigate(
                            HomeFragmentDirections.actionHomeFragmentToSearchGifFragment(
                                model.searchterm
                            )
                        )
                    }
                }
            )
        }
    }

    private fun downloadImage(position: Int) {
        mShareAppBottomSheetDialog?.dismiss()
        Glide.with(requireContext()).asFile()
            .load(mHomeViewModel.mSharingGifUrl)
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
            /*var compressedFile: File? = null
            lifecycleScope.launch {
                compressedFile = Compressor.compress(requireContext(), resourceFile)
            }
            compressedFile?.let {

            }*/
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

//        val shareIntent = Intent(Intent.ACTION_SEND)
//        shareIntent.type = "text/plain"
//        shareIntent.putExtra(Intent.EXTRA_TEXT, requireContext().getPlayStoreAppLink())
//        val photoURI = FileProvider.getUriForFile(
//            requireContext(),
//            requireContext().packageName + ".provider",
//            pictureFile
//        )
//        shareIntent.setDataAndType(photoURI, "*/*")
//        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//        shareIntent.putExtra(Intent.EXTRA_STREAM, photoURI)
//
//        try {
//            when (position) {
//                0 -> shareIntent.setPackage("com.facebook.katana") //Facebook
//                1 -> shareIntent.setPackage("com.facebook.orca") //Fb messenger
//                2 -> shareIntent.setPackage("com.whatsapp") //Whatsapp
//                3 -> shareIntent.setPackage("com.instagram.android") //Instagram
//                4 -> shareIntent.setPackage("com.twitter.android") //Twitter
//                5 -> shareIntent.setPackage("com.reddit.frontpage") //Reddit
//                6 -> shareIntent.setPackage("com.tencent.mm") //We Chat
//                7 -> shareIntent.setPackage("com.google.android.talk") //Google Hangout
//                8 -> shareIntent.setPackage("org.telegram.messenger") //Telegram
//                9 -> shareIntent.setPackage("com.snapchat.android") //SnapChat
//                10 -> shareIntent.setPackage("com.viber.voip") //Viber
//                11 -> shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_content))
//                12 -> startActivity(Intent.createChooser(shareIntent, "Share Gif"))
//            }
//            startActivity(shareIntent)
//        } catch (ex: ActivityNotFoundException) {
//            requireActivity().showToast(getString(R.string.application_not_found))
//        }

        /*val photoURI = FileProvider.getUriForFile(
            requireContext(),
            requireContext().packageName + ".provider",
            pictureFile
        )

        val decodedString: ByteArray = Base64.decode(
            getString(R.string.share_app_content),
            Base64.DEFAULT
        ) //The string is named share_image.

        val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length)
        val imageToShare = Uri.parse(
            MediaStore.Images.Media.insertImage(
                requireActivity().contentResolver,
                decodedByte,
                "Share image",
                null
            )
        ) //MainActivity.this is the context in my app.

        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.setPackage("com.instagram.android")
        try {
            shareIntent.putExtra(Intent.EXTRA_TEXT, requireContext().getPlayStoreAppLink())
            shareIntent.putExtra(
                Intent.EXTRA_STREAM,
                photoURI
            )
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        shareIntent.type = "image/jpeg"
        startActivity(shareIntent)*/
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

    private fun shareScreenshot() {
        try {

            val inputStream = resources.openRawResource(R.raw.app_screenshot)

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
            val pictureFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .toString() + File.separator + "GifStorm",
                "app_screenshot.jpg"
            )
            val bis = BufferedInputStream(inputStream)
            val byteArrayOutputStream = ByteArrayOutputStream()
            var current: Int
            while (bis.read().also { current = it } != -1) {
                byteArrayOutputStream.write(current)
            }
            val fos = FileOutputStream(pictureFile)
            fos.write(byteArrayOutputStream.toByteArray())
            fos.flush()
            fos.close()
            inputStream.close()

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_content))
            val photoURI = FileProvider.getUriForFile(
                requireContext(),
                requireContext().packageName + ".provider",
                pictureFile
            )
            shareIntent.setDataAndType(photoURI, "*/*")
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            shareIntent.putExtra(Intent.EXTRA_STREAM, photoURI)
            startActivity(Intent.createChooser(shareIntent, getString(R.string.app_name)))

        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }
}