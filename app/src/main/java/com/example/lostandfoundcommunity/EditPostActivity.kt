
package com.example.lostandfoundcommunity

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class EditPostActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var captionInput: EditText
    private lateinit var saveButton: Button

    private var selectedImageUri: Uri? = null
    private var postId: String = ""
    private var existingImageUrl: String = ""

    private val cloudName = "rm2tl5y2"
    private val uploadPreset = "lostfound_preset"

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                imageView.setImageURI(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_post)

        imageView = findViewById(R.id.editPostImage)
        captionInput = findViewById(R.id.editCaptionInput)
        saveButton = findViewById(R.id.saveChangesButton)

        val changeImageButton: Button =
            findViewById(R.id.changeImageButton)

        postId = intent.getStringExtra("postId") ?: ""
        existingImageUrl = intent.getStringExtra("imageUrl") ?: ""

        captionInput.setText(intent.getStringExtra("caption") ?: "")

        Glide.with(this)
            .load(existingImageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(imageView)

        changeImageButton.setOnClickListener {
            imagePicker.launch("image/*")
        }

        saveButton.setOnClickListener {
            saveChanges()
        }
    }

    private fun saveChanges() {
        val newCaption = captionInput.text.toString().trim()

        if (postId.isBlank()) {
            Toast.makeText(this, "Post ID missing", Toast.LENGTH_SHORT).show()
            return
        }

        if (newCaption.isBlank()) {
            captionInput.error = "Caption cannot be empty"
            return
        }

        if (newCaption.length > 1024) {
            captionInput.error = "Caption is too long"
            return
        }

        saveButton.isEnabled = false

        val pickedUri = selectedImageUri

        if (pickedUri == null) {
            updatePost(newCaption, existingImageUrl)
        } else {
            Thread {
                try {
                    val newUrl = uploadImageToCloudinary(pickedUri)

                    runOnUiThread {
                        updatePost(newCaption, newUrl)
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        saveButton.isEnabled = true
                        Toast.makeText(
                            this,
                            "Image upload failed: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }.start()
        }
    }

    private fun updatePost(caption: String, imageUrl: String) {
        val updates = mapOf(
            "caption" to caption,
            "imageUrl" to imageUrl
        )

        FirebaseDatabase.getInstance()
            .getReference("posts")
            .child(postId)
            .updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Post updated successfully",
                    Toast.LENGTH_SHORT
                ).show()

                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener { error ->
                saveButton.isEnabled = true
                Toast.makeText(
                    this,
                    "Update failed: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun uploadImageToCloudinary(uri: Uri): String {
        val boundary = "Boundary-${UUID.randomUUID()}"

        val connection = URL(
            "https://api.cloudinary.com/v1_1/$cloudName/image/upload"
        ).openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.connectTimeout = 15000
        connection.readTimeout = 60000
        connection.setRequestProperty(
            "Content-Type",
            "multipart/form-data; boundary=$boundary"
        )

        connection.outputStream.use { output ->

            fun writeText(name: String, value: String) {
                output.write("--$boundary\r\n".toByteArray())
                output.write(
                    "Content-Disposition: form-data; name=\"$name\"\r\n\r\n"
                        .toByteArray()
                )
                output.write("$value\r\n".toByteArray())
            }

            writeText("upload_preset", uploadPreset)

            output.write("--$boundary\r\n".toByteArray())
            output.write(
                "Content-Disposition: form-data; name=\"file\"; filename=\"image.jpg\"\r\n"
                    .toByteArray()
            )
            output.write("Content-Type: image/jpeg\r\n\r\n".toByteArray())

            contentResolver.openInputStream(uri)?.use {
                it.copyTo(output)
            } ?: throw Exception("Cannot read selected image")

            output.write("\r\n--$boundary--\r\n".toByteArray())
        }

        val code = connection.responseCode

        val response = (
                if (code in 200..299) connection.inputStream
                else connection.errorStream
                )?.bufferedReader()?.use { it.readText() }
            ?: "Cloudinary upload error"

        connection.disconnect()

        if (code !in 200..299) {
            throw Exception(response)
        }

        val json = JSONObject(response)
        return json.getString("secure_url")
    }
}
