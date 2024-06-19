package com.capstone.agrovision.view.addGallery

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.capstone.agrovision.R
import com.capstone.agrovision.databinding.ActivityFiturCameraBinding
import com.capstone.agrovision.view.timeline.TimelineActivity
import com.capstone.agrovision.view.HomeActivity
import com.capstone.agrovision.view.result.ResultActivity
import com.capstone.agrovision.view.SettingsActivity
import com.capstone.agrovision.view.upload.Utils.reduceFileImage
import com.capstone.agrovision.view.upload.Utils.uriToFile
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File

class FeatureCameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFiturCameraBinding
    private lateinit var bottomNavigationView: BottomNavigationView
    private var currentImageUri: Uri? = null

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Permission request granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permission request denied", Toast.LENGTH_LONG).show()
            }
        }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityFiturCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.menuBar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setUpActionBar()
        setupBottomNavigation()

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        binding.cameraButton.setOnClickListener {
            startCameraX()
        }

        binding.galleryButton.setOnClickListener {
            startGallery()
        }

        binding.buttonAdd.setOnClickListener {
            analyzeImage()
        }
    }

    private fun setUpActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Analyze"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                currentImageUri = uri
                showImage()
            } ?: showToast("Failed to get image URI")
        }
    }

    private fun startCameraX() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val uriString = result.data?.getStringExtra(CameraActivity.EXTRA_CAMERAX_IMAGE)
            uriString?.let {
                currentImageUri = Uri.parse(it)
                showImage()
            } ?: showToast("Failed to get camera image URI")
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d(TAG, "Displaying image: $it")
            binding.previewImageView.setImageURI(it)
        } ?: Log.d(TAG, "No image to display")
    }

    private fun analyzeImage() {
        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, this).reduceFileImage()
            saveImageLocally(imageFile)
            navigateTo(ResultActivity::class.java)
        } ?: showToast(getString(R.string.empty_image_warning))
    }

    private fun saveImageLocally(file: File) {
    }

    private fun uploadImageToServer(file: File) {
    }

    private fun setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.menuBar)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    navigateTo(HomeActivity::class.java)
                    true
                }
                R.id.timeline -> {
                    navigateTo(TimelineActivity::class.java)
                    true
                }
                R.id.settings -> {
                    navigateTo(SettingsActivity::class.java)
                    true
                }
                else -> false
            }
        }
    }

    private fun navigateTo(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        currentImageUri?.let { uri ->
            intent.putExtra(ResultActivity.IMAGE_URI, uri.toString())
        }
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val TAG = "ImagePicker"
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}