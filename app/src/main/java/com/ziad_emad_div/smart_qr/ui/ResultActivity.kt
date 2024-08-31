package com.ziad_emad_div.smart_qr.ui

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import com.ziad_emad_div.smart_qr.databinding.ActivityResultBinding

@Suppress("DEPRECATION")
class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.back.setOnClickListener { finish() }

        val result = intent.getStringExtra("result")!!

        show(result)

        go(result)
        copy(result)
        share(result)
    }

    private fun show(result: String) {
        binding.image.setImageURI(intent.getParcelableExtra("Image"))
        binding.result.text = if (result.startsWith("WIFI:")) {
            "WiFi: ${result.substringAfter("S:").substringBefore(";")}" +
                    "\nPassword: ${result.substringAfter("P:").substringBefore(";")}"
        } else {
            result
        }
    }

    private fun go(result: String) {
        binding.go.setOnClickListener {
            lateinit var intent: Intent
            when {
                result.startsWith("http://") || result.startsWith("https://") -> {
                    val url: Uri = Uri.parse(result)
                    intent = Intent(Intent.ACTION_VIEW, url)
                }

                result.matches(Regex("^\\+?[0-9]{10,13}\$")) -> {
                    val phoneNumber: Uri = Uri.parse("tel:$result")
                    intent = Intent(Intent.ACTION_DIAL, phoneNumber)
                }

                result.startsWith("WIFI:") -> {
                    val password = Uri.parse("https://${result.substringAfter("P:").substringBefore(";")}")
                    intent = Intent(Intent.ACTION_VIEW, password)
                }

                else -> {
                    val url: Uri = Uri.parse("https://$result")
                    intent = Intent(Intent.ACTION_VIEW, url)
                }
            }
            startActivity(intent)
        }
    }

    private fun copy(result: String) {
        binding.copy.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = if (result.startsWith("WIFI:")) {
                ClipData.newPlainText("password", result.substringAfter("P:").substringBefore(";"))
            } else {
                ClipData.newPlainText("result", result)
            }
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun share(result: String) {
        binding.share.setOnClickListener {
            val shareIntent = ShareCompat.IntentBuilder.from(this)
                .setText(
                    if (result.startsWith("WIFI:")) {
                        "WiFi: ${result.substringAfter("S:").substringBefore(";")}" +
                                "\nPassword: ${result.substringAfter("P:").substringBefore(";")}"
                    } else {
                        result
                    }
                )
                .setType("text/plain")
                .intent
            try {
                startActivity(shareIntent)
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(this, "Sharing not available", Toast.LENGTH_LONG).show()
            }
        }
    }
}