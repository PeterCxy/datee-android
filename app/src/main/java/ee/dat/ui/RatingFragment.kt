package ee.dat.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.squareup.picasso.Callback
import ee.dat.R
import ee.dat.api.DateeApi
import ee.dat.util.*
import kotlinx.android.synthetic.main.fragment_rate.*
import kotlinx.coroutines.*

class RatingFragment: Fragment() {
    private var initialized = false
    private var working = false
        set(value) {
            rating_progress.visibility = if (value) { View.VISIBLE } else { View.GONE }
            rating_bar.isEnabled = !value
            rating_photo.isEnabled = !value
            if (!value)
                rating_bar.progress = 0
            field = value
        }
    private lateinit var uid: String
    private lateinit var photos: List<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_rate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rating_photo.setOnClickListener { openGallery() }
        rating_bar.setOnRatingBarChangeListener { _, _, fromUser ->
            if (fromUser) {
                GlobalScope.launch(Dispatchers.Main) {
                    submitRating()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!initialized) {
            GlobalScope.launch(Dispatchers.Main) {
                nextPersonForRating()
                initialized = true
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(activity, GalleryActivity::class.java).apply {
            putStringArrayListExtra("photos", ArrayList(photos))
        }
        startActivity(intent)
    }

    private suspend fun nextPersonForRating() = coroutineScope {
        if (!working) working = true
        // TODO: maybe do not crash if failed?
        uid = withContext(Dispatchers.IO) {
            DateeApi.api.randomUser().executeMaybe()
        }.onErr { activity!!.showErrorToast(it); activity!!.finish(); return@coroutineScope }!!
        photos = withContext(Dispatchers.IO) {
            DateeApi.api.listPhotos(uid).executeMaybe()
        }.onErr { activity!!.showErrorToast(it); activity!!.finish(); return@coroutineScope }!!
        DateeApi.picasso
            .load(DateeApi.buildPhotoUrl(photos[0]))
            .fit()
            .centerCrop()
            .into(rating_photo, object : Callback {
                override fun onSuccess() {
                    rating_bar.progress = 0
                    working = false
                }

                override fun onError(e: Exception?) {
                    e?.message?.also {
                        Toast.makeText(activity!!, it, Toast.LENGTH_LONG).show()
                    }
                    activity!!.finish()
                }
            })
    }

    private suspend fun submitRating() = coroutineScope {
        if (!working) working = true
        val score = rating_bar.progress
        withContext(Dispatchers.IO) {
            DateeApi.api.rateUser(uid, score).executeMaybe()
        }.onErr { activity!!.showErrorToast(it) }
        nextPersonForRating()
    }
}