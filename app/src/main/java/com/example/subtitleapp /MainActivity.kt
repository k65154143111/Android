package com.example.subtitleapp

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
// تأكد من أن هذا الـ import صحيح بناءً على الـ namespace واسم ملف الـ layout
import com.example.subtitleapp.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.* // Required for Date and Locale

class MainActivity : AppCompatActivity() {

    // ViewBinding يتم إنشاؤه تلقائياً إذا كان buildFeatures { viewBinding true } مفعل
    private lateinit var binding: ActivityMainBinding
    private val viewModel: SubtitleViewModel by viewModels() // Requires 'androidx.activity:activity-ktx'

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.apply {
            // IDs must match exactly those in activity_main.xml
            generateButton.setOnClickListener {
                val apiUrl = apiUrlInput.text.toString()
                val videoUrl = videoUrlInput.text.toString()

                if (apiUrl.isNotEmpty() && videoUrl.isNotEmpty()) {
                    viewModel.generateSubtitles(apiUrl, videoUrl)
                    showLoading() // Show loading indicator when request starts
                } else {
                    showError("Please fill all fields")
                }
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is SubtitleState.Loading -> showLoading() // Already handled when button clicked? Decide behavior.
                    is SubtitleState.Success -> handleSuccess(state.srtContent)
                    is SubtitleState.Error -> showError(state.message)
                    SubtitleState.Idle -> hideLoading() // Hide loading when idle (initial state or after completion/error)
                }
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.isVisible = true
        binding.generateButton.isEnabled = false
    }

    private fun hideLoading() {
        binding.progressBar.isVisible = false
        binding.generateButton.isEnabled = true
    }

    private fun handleSuccess(srtContent: String) {
        hideLoading()
        saveSubtitleFile(srtContent)
    }

    private fun showError(message: String) {
        hideLoading()
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun saveSubtitleFile(content: String) {
        try {
            // Use SimpleDateFormat correctly
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val filename = "subtitle_$timestamp.srt"

            // Use getExternalFilesDir for app-specific storage (requires no special permission on newer Android)
            val storageDir = getExternalFilesDir(null)
            if (storageDir == null) {
                showError("Cannot access external storage directory.")
                return
            }
            val file = File(storageDir, filename)
            file.writeText(content)

            showSuccess("Subtitle saved: ${file.absolutePath}")

        } catch (e: Exception) {
            e.printStackTrace() // Print error details for debugging
            showError("Failed to save file: ${e.message}")
        }
    }

    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}
