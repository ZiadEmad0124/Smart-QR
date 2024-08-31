package com.ziad_emad_div.smart_qr.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.ziad_emad_div.smart_qr.databinding.ActivityHomeBinding
import java.io.File
import java.io.FileOutputStream

@Suppress("DEPRECATION")
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    private val cameraPermissionRequestCode = 100
    private val imagePickerRequestCode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        generateQrCode()
        scanQrCode()
        scanFromImage()
    }

    private fun generateQrCode() {
        binding.generateQrCode.setOnClickListener {
            startActivity(Intent(this, GenerateQrCodeActivity::class.java))
        }
    }

    private fun scanQrCode() {
        binding.scanQrCode.setOnClickListener {
            if (checkCameraPermission()) {
                startActivity(Intent(this, ScanActivity::class.java))
            } else {
                requestCameraPermission()
            }
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), cameraPermissionRequestCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == cameraPermissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(Intent(this, ScanActivity::class.java))
            }
        }
    }

    private fun scanFromImage() {
        binding.scanFromImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, imagePickerRequestCode)
        }
    }

    private fun decodeQrCodeFromImage(bitmap: Bitmap) {
        val intArray = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val source = RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        try {
            val result = MultiFormatReader().decode(binaryBitmap)
            val intent = Intent(this@HomeActivity, ResultActivity::class.java)
            intent.putExtra("result", result.text)
            val imageUri: Uri = saveBitmapToFile(bitmap)
            intent.putExtra("Image", imageUri)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "QR code not found in the image", Toast.LENGTH_SHORT).show()
        }
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

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n" +
            "which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n" +
            "contracts for common intents available in\n" +
            "{@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n" +
            "testing, and allow receiving results in separate, testable classes independent from your\n" +
            "activity. Use\n" +
            "{@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n" +
            "with the appropriate {@link ActivityResultContract} and handling the result in the\n" +
            "{@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == imagePickerRequestCode && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                decodeQrCodeFromImage(bitmap)
            }
        }
    }
}