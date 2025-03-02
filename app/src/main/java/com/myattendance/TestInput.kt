package com.myattendance

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import android.widget.LinearLayout
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


class TestInput : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var dotsLayout: LinearLayout
    private lateinit var dots: Array<ImageView?>
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var recyclerView: RecyclerView
    private lateinit var menuadapter: MenuAdapter
    private var isExpanded = false
    private val menuList = mutableListOf<MenuItem>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ttt)

        viewPager = findViewById(R.id.viewPager)
        dotsLayout = findViewById(R.id.dotsLayout)

        val imageUrls = listOf(
            "https://res.cloudinary.com/nimg/image/upload/v1740481627/samples/coffee.jpg",
            "https://res.cloudinary.com/nimg/image/upload/v1740481628/cld-sample-4.jpg",
            "https://res.cloudinary.com/nimg/image/upload/v1740481627/samples/dessert-on-a-plate.jpg"
        )

        val adapter = ViewPagerAdapter(imageUrls)
        viewPager.adapter = adapter

        setupDots(imageUrls.size)
        autoSlideImages()

        recyclerView = findViewById(R.id.recyclerView)
        val expandButton = findViewById<Button>(R.id.expandButton)

        recyclerView.layoutManager = GridLayoutManager(this, 3)

        // Fetch menu items from API
        fetchMenuItems()

        expandButton.setOnClickListener {
            isExpanded = !isExpanded
            menuadapter.toggleExpand(isExpanded)
            expandButton.text = if (isExpanded) "Show Less" else "Show More"
        }
    }
    private fun fetchMenuItems() {
        // Simulate API call (replace with Ktor API request)
        menuList.addAll(
            listOf(
                MenuItem(1, "Needsiz", "https://example.com/icon1.png"),
                MenuItem(2, "Annual Report", "https://example.com/icon2.png"),
                MenuItem(3, "Sustainability Report", "https://example.com/icon3.png"),
                MenuItem(4, "Finance", "https://example.com/icon4.png"),
                MenuItem(5, "Careers", "https://example.com/icon5.png"),
                MenuItem(6, "News", "https://example.com/icon6.png"),
                MenuItem(7, "Investors", "https://example.com/icon7.png"),
                MenuItem(8, "CSR", "https://example.com/icon8.png"),
                MenuItem(9, "Leadership", "https://example.com/icon9.png"),
                MenuItem(10, "Products", "https://example.com/icon10.png")
            )
        )

        menuadapter = MenuAdapter(menuList, isExpanded)
        recyclerView.adapter = menuadapter
    }
    private fun setupDots(count: Int) {
        dots = arrayOfNulls(count)
        dotsLayout.removeAllViews()

        for (i in 0 until count) {
            dots[i] = ImageView(this)
            dots[i]?.setImageResource(R.drawable.dot_inactive)

            val params = LinearLayout.LayoutParams(20, 20)
            params.setMargins(8, 0, 8, 0)
            dotsLayout.addView(dots[i], params)
        }

        dots[0]?.setImageResource(R.drawable.dot_active)

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                for (i in dots.indices) {
                    dots[i]?.setImageResource(R.drawable.dot_inactive)
                }
                dots[position]?.setImageResource(R.drawable.dot_active)
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    private fun autoSlideImages() {
        val runnable = object : Runnable {
            override fun run() {
                val nextItem = (viewPager.currentItem + 1) % dots.size
                viewPager.setCurrentItem(nextItem, true)
                handler.postDelayed(this, 3000)
            }
        }
        handler.postDelayed(runnable, 3000)
    }
}
