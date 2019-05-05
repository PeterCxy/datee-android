package ee.dat.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import com.github.chrisbanes.photoview.PhotoView
import ee.dat.R
import ee.dat.api.DateeApi
import kotlinx.android.synthetic.main.activity_gallery.*

class GalleryActivity: AppCompatActivity() {
    private lateinit var photoIds: ArrayList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE
        photoIds = intent.getStringArrayListExtra("photos")
        gallery_pager.adapter = PhotoAdapter()
        gallery_pager.offscreenPageLimit = 5
    }

    inner class PhotoAdapter: PagerAdapter() {
        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun getCount(): Int = photoIds.size

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val pv = PhotoView(container.context)
            pv.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
            pv.setOnClickListener { finish() }
            container.addView(pv)
            DateeApi.picasso
                .load(DateeApi.buildPhotoUrl(photoIds[position]))
                .into(pv)
            return pv
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeViewInLayout(`object` as View)
        }
    }
}