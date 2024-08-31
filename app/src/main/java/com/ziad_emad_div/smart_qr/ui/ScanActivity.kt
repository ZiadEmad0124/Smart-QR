package com.ziad_emad_div.smart_qr.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.ziad_emad_div.smart_qr.R
import com.ziad_emad_div.smart_qr.databinding.ActivityScanBinding
import java.io.File
import java.io.FileOutputStream

class ScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanBinding

    private var isFlashOn = false

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = getColor(R.color.dark)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setUpFlash()
        startScanning()
    }

    private fun setUpFlash() {
        binding.flash.setOnClickListener {
            if (!isFlashOn) {
                isFlashOn = true
                binding.scanner.setTorchOn()
                binding.flash.setImageResource(R.drawable.ic_flash_off)
            } else {
                isFlashOn = false
                binding.scanner.setTorchOff()
                binding.flash.setImageResource(R.drawable.ic_flash_on)
            }
        }
    }

    private fun startScanning() {
        binding.scanner.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult) {
                val intent = Intent(this@ScanActivity, ResultActivity::class.java)
                intent.putExtra("result", result.text)
                val imageUri: Uri = saveBitmapToFile(result.bitmap)
                intent.putExtra("Image", imageUri)
                startActivity(intent)
            }

            override fun possibleResultPoints(resultPoints: List<com.google.zxing.ResultPoint>) {

            }
        })
    }

    private fun saveBitmapToFile(bitmap: Bitmap): Uri {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File(storageDir, "scanned_image.jpg")
        val outputStream = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        return Uri.fromFile(imageFile)
    }

    override fun onResume() {
        super.onResume()
        binding.scanner.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.scanner.pause()
    }
}