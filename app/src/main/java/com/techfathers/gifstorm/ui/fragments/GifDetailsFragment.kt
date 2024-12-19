package com.techfathers.gifstorm.ui.fragments

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.RequestListener
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
import com.techfathers.gifstorm.databinding.BottomSheetAppShareBinding
import com.techfathers.gifstorm.databinding.FragmentGifDetailsBinding
import com.techfathers.gifstorm.databinding.ItemSocialMediaBinding
import com.techfathers.gifstorm.models.SocialMediaTypeModel
import com.techfathers.gifstorm.ui.base.BaseCallback
import com.techfathers.gifstorm.ui.base.BaseFragment
import com.techfathers.gifstorm.ui.base.BaseItem
import com.techfathers.gifstorm.util.*
import com.techfathers.gifstorm.view_models.GifDetailsViewModel
import com.techfathers.gifstorm.vm_factories.GifDetailsViewModelFactory
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import java.io.*


/**
 * Property of Techfathers, Inc @ 2021 All Rights Reserved.
 */

class GifDetailsFragment : BaseFragment<FragmentGifDetailsBinding>(), KodeinAware {

    override val kodein by kodein()

    private val mFactory: GifDetailsViewModelFactory by instance()

    private lateinit var mResultModel: ResultModel
    private lateinit var mGifDetailsViewModel: GifDetailsViewModel

    private var mShareAppBottomSheetDialog: BottomSheetDialog? = null


    override fun getFragmentLayout(): Int {
        return R.layout.fragment_gif_details
    }

    override fun onFragmentCreateView(savedInstanceState: Bundle?) {
        super.onFragmentCreateView(savedInstanceState)
        initView()
    }

    private fun initView() {
        setBaseCallback(baseCallback)
        mGifDetailsViewModel =
            ViewModelProvider(this, mFactory).get(GifDetailsViewModel::class.java)
        binding.toolbar.tvTitle.text = getString(R.string.hd)
        initializeShareAppBottomSheet()
        val args = arguments?.let { GifDetailsFragmentArgs.fromBundle(it) }
        args?.let {
            mResultModel = it.resultModel
            if (mResultModel.liked) binding.imgLike.setImageResource(R.drawable.ic_heart_red)
            loadHDGif(mResultModel.media[0].gif.url)
        }
    }

    private fun loadHDGif(url: String) {
        Glide.with(this).asGif().load(url)
            .transition(DrawableTransitionOptions.withCrossFade())
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .listener(object : RequestListener<GifDrawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<GifDrawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: GifDrawable?,
                    model: Any?,
                    target: Target<GifDrawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressCircular.visibility = View.GONE
                    return false
                }

            }).into(binding.imgGif)
    }

    private val baseCallback = BaseCallback {
        when (it.id) {
            R.id.img_back -> goBack()
            R.id.rl_like -> likeDislikeGif()
            R.id.rl_download_gif -> askDownloadFilePermissions()
            R.id.rl_share_gif -> mShareAppBottomSheetDialog?.show()
        }
    }

    private fun askDownloadFilePermissions() {

        /**Asking storage permission from user*/

        Dexter.withContext(requireContext())
            .withPermissions(*Constants.STORAGE_PERMISSIONS)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        when {
                            report.areAllPermissionsGranted() -> {
                                downloadFile()
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

    private fun downloadFile() {
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
            "downloaded" + System.currentTimeMillis() + ".gif"
        )

        val request = DownloadManager.Request(Uri.parse(mResultModel.media[0].gif.url))
            .setDestinationUri(Uri.fromFile(file)) // Uri of the destination file
            .setTitle(file.name) // Title of the Download Notification
            .setDescription("Downloading") // Description of the Download Notification
            //.setRequiresCharging(false) // Set if charging is required to begin the download
            .setAllowedOverMetered(true) // Set if download is allowed on Mobile network
            .setAllowedOverRoaming(true) // Set if download is allowed on roaming network

        val downloadManager =
            requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
        downloadManager?.enqueue(request) // enqueue puts the download request in the queue.
        requireContext().showToast(getString(R.string.gif_downloading))
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

    private fun likeDislikeGif() {
        lifecycleScope.launch {
            if (mResultModel.liked) {
                binding.imgLike.setImageResource(R.drawable.ic_heart_dark_gray)
                mResultModel.liked = false
                mGifDetailsViewModel.deleteLikedGif(mResultModel.id)
            } else {
                binding.imgLike.setImageResource(R.drawable.ic_heart_red)
                mResultModel.liked = true
                mGifDetailsViewModel.insertLikedGif(mResultModel)
            }
        }
    }

    private fun downloadImage(position: Int) {
        mShareAppBottomSheetDialog?.dismiss()
        Glide.with(requireContext()).asFile()
            .load(mResultModel.media[0].gif.url)
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