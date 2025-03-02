package com.myattendance

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import java.io.IOException
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var searchView: SearchView
    private lateinit var listView: ListView
    private lateinit var placesClient: PlacesClient
    private lateinit var adapter: ArrayAdapter<String>
    private val suggestions = mutableListOf<String>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Initialize Google Places API
        Places.initialize(applicationContext, "AIzaSyD7xW1u1Y0gHhtjpoxkPobH2KX_SovfFWk")
        placesClient = Places.createClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        searchView = findViewById(R.id.searchView)
        listView = findViewById(R.id.listView)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, suggestions)
        listView.adapter = adapter

        // Search input listener
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    searchLocation(query)
                    listView.visibility = android.view.View.GONE
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    getSuggestions(newText)
                }
                return false
            }
        })

        // Handle user clicking on suggestions
        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedPlace = suggestions[position]
            searchLocation(selectedPlace)
            listView.visibility = android.view.View.GONE
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
            )
            return
        }

        mMap.isMyLocationEnabled = true
    }

    private fun searchLocation(placeName: String) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addressList = geocoder.getFromLocationName(placeName, 5)
            if (addressList != null && addressList.isNotEmpty()) {
                mMap.clear() // Remove old markers

                for (address in addressList) {
                    val location = LatLng(address.latitude, address.longitude)
                    mMap.addMarker(MarkerOptions().position(location).title(placeName))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12f))
                }

                Toast.makeText(this, "Showing results for: $placeName", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Place not found!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("MapsActivity", "Geocoder error: ${e.message}")
        }
    }

    private fun getSuggestions(query: String) {
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                suggestions.clear()
                for (prediction in response.autocompletePredictions) {
                    suggestions.add(prediction.getFullText(null).toString())
                }
                adapter.notifyDataSetChanged()
                listView.visibility = android.view.View.VISIBLE
            }
            .addOnFailureListener { exception ->
                Log.e("MapsActivity", "Error fetching predictions: ${exception.message}")
            }
    }
}
