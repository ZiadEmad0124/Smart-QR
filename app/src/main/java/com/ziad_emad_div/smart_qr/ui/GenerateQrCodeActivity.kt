package com.ziad_emad_div.smart_qr.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.ziad_emad_div.smart_qr.databinding.ActivityGenerateQrCodeBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@Suppress("DEPRECATION")
class GenerateQrCodeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGenerateQrCodeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenerateQrCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener { finish() }
        binding.go.setOnClickListener { generatingQrCode() }
        binding.download.setOnClickListener { downloadQrCode() }
        binding.share.setOnClickListener { shareQrCode() }
        binding.delete.setOnClickListener { deleteQrCode() }
    }

    private fun generatingQrCode() {
        binding.contentLayout.clearFocus()
        val content = binding.content.text.toString().trim()
        if (content.isNotEmpty()) {
            startGeneratingQrCode(content)
        } else {
            showToast("Please enter a link")
        }
    }

    private fun startGeneratingQrCode(content: String) {
        val qrCodeWriter = QRCodeWriter()
        try {
            val bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 300, 300)
            val bitmap = createBitmapFromBitMatrix(bitMatrix)
            binding.qrLayout.visibility = android.view.View.VISIBLE
            binding.image.setImageBitmap(bitmap)
            hideKeyboard()
        } catch (e: WriterException) {
            e.printStackTrace()
            showToast("Failed to generate QR code")
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = currentFocus ?: binding.root
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun createBitmapFromBitMatrix(bitMatrix: com.google.zxing.common.BitMatrix): Bitmap {
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bitmap
    }

    private fun getBitmapFromImageView(): Bitmap? {
        val drawable = binding.image.drawable as? BitmapDrawable
        return drawable?.bitmap
    }

    private fun downloadQrCode() {
        val bitmap = getBitmapFromImageView()
        if (bitmap != null) {
            saveImageToExternalStorage(bitmap)
        } else {
            showToast("No QR code to download")
        }
    }

    private fun saveImageToExternalStorage(bitmap: Bitmap) {
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            val directory = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "SmartQr")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val fileName = "QRCode_${System.currentTimeMillis()}.png"
            val file = File(directory, fileName)
            try {
                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    showToast("QR code saved successfully")
                }
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                val contentUri = Uri.fromFile(file)
                mediaScanIntent.data = contentUri
                sendBroadcast(mediaScanIntent)
            } catch (e: IOException) {
                e.printStackTrace()
                showToast("Failed to save QR code")
            }
        } else {
            showToast("External storage not available")
        }
    }

    private fun shareQrCode() {
        val bitmap = getBitmapFromImageView()
        if (bitmap != null) {
            val fileUri = saveImageToCache(bitmap)
            if (fileUri != null) {
                shareImage(fileUri)
            } else {
                showToast("Failed to share QR code")
            }
        } else {
            showToast("No QR code to share")
        }
    }

    private fun saveImageToCache(bitmap: Bitmap): Uri? {
        val cachePath = File(cacheDir, "qrCodes")
        return try {
            cachePath.mkdirs()
            val file = File(cachePath, "shared_qr_code.png")
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun shareImage(uri: Uri) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "qrCode/png"
        }
        startActivity(Intent.createChooser(shareIntent, "Share QR code"))
    }

    private fun deleteQrCode() {
        binding.qrLayout.visibility = android.view.View.GONE
        binding.content.text?.clear()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}