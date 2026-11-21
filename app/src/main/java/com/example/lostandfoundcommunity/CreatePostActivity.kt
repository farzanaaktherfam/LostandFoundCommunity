
package com.example.lostandfoundcommunity

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class CreatePostActivity : AppCompatActivity() {

    private lateinit var selectedImage: ImageView
    private lateinit var placeholderIcon: ImageView
    private lateinit var captionInput: EditText
    private lateinit var captionCounter: TextView
    private lateinit var selectImageButton: Button
    private lateinit var uploadPostButton: Button

    private var imageUri: Uri? = null

    private val cloudName = "rm2tl5y2"
    private val uploadPreset = "lostfound_preset"
    private val maxCaptionLength = 1024

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                imageUri = uri
                selectedImage.setImageURI(uri)
                placeholderIcon.visibility = View.GONE
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        selectedImage = findViewById(R.id.selectedImage)
        placeholderIcon = findViewById(R.id.placeholderIcon)
        captionInput = findViewById(R.id.captionInput)
        captionCounter = findViewById(R.id.captionCounter)
        selectImageButton = findViewById(R.id.selectImageButton)
        uploadPostButton = findViewById(R.id.uploadPostButton)

        captionInput.filters = arrayOf(
            android.text.InputFilter.LengthFilter(maxCaptionLength)
        )

        captionInput.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                captionCounter.text =
                    "${s?.length ?: 0}/$maxCaptionLength"
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        selectImageButton.setOnClickListener {
            imagePicker.launch("image/*")
        }

        uploadPostButton.setOnClickListener {
            uploadPost()
        }
    }

    private fun uploadPost() {

        val uri = imageUri
        val caption = captionInput.text.toString().trim()
        val user = FirebaseAuth.getInstance().currentUser

        if (uri == null) {
            Toast.makeText(
                this,
                "Please select an image",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (caption.isEmpty()) {
            Toast.makeText(
                this,
                "Please write a caption",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (user == null) {
            Toast.makeText(
                this,
                "Please login first",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        uploadPostButton.isEnabled = false

        val database = FirebaseDatabase.getInstance()

        val userRef = database.getReference("users")
            .child(user.uid)

        userRef.child("username").get()
            .addOnSuccessListener { snapshot ->

                val username = snapshot.getValue(String::class.java)

                if (username.isNullOrBlank()) {
                    uploadPostButton.isEnabled = true

                    Toast.makeText(
                        this,
                        "Username not found in Firebase",
                        Toast.LENGTH_LONG
                    ).show()

                    return@addOnSuccessListener
                }

                Thread {
                    try {

                        val (imageUrl, publicId) =
                            uploadImageToCloudinary(uri)

                        val post = hashMapOf<String, Any>(
                            "imageUrl" to imageUrl,
                            "publicId" to publicId,
                            "username" to username,
                            "uid" to user.uid,
                            "caption" to caption,
                            "timestamp" to ServerValue.TIMESTAMP
                        )

                        database.getReference("posts")
                            .push()
                            .setValue(post)
                            .addOnSuccessListener {

                                Toast.makeText(
                                    this,
                                    "Post uploaded successfully",
                                    Toast.LENGTH_SHORT
                                ).show()

                                finish()
                            }
                            .addOnFailureListener {

                                uploadPostButton.isEnabled = true

                                Toast.makeText(
                                    this,
                                    "Firebase error: ${it.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                    } catch (e: Exception) {

                        runOnUiThread {
                            uploadPostButton.isEnabled = true

                            Toast.makeText(
                                this,
                                "Upload failed: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                }.start()
            }
            .addOnFailureListener {

                uploadPostButton.isEnabled = true

                Toast.makeText(
                    this,
                    "Could not get username: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    // Upload image to Cloudinary
    private fun uploadImageToCloudinary(
        uri: Uri
    ): Pair<String, String> {

        val boundary = "Boundary-${UUID.randomUUID()}"

        val connection = URL(
            "https://api.cloudinary.com/v1_1/$cloudName/image/upload"
        ).openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.connectTimeout = 15000
        connection.readTimeout = 60000

        connection.setChunkedStreamingMode(0)

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

            output.write(
                "Content-Type: image/jpeg\r\n\r\n".toByteArray()
            )

            contentResolver.openInputStream(uri)?.use { input ->
                input.copyTo(output)
            } ?: throw Exception("Cannot read selected image")

            output.write(
                "\r\n--$boundary--\r\n".toByteArray()
            )
        }

        val responseCode = connection.responseCode

        val response = if (responseCode in 200..299) {

            connection.inputStream.bufferedReader().use {
                it.readText()
            }

        } else {

            connection.errorStream?.bufferedReader()?.use {
                it.readText()
            } ?: "Cloudinary upload error"
        }

        connection.disconnect()

        if (responseCode !in 200..299) {
            throw Exception(response)
        }

        val json = JSONObject(response)

        return Pair(
            json.getString("secure_url"),
            json.getString("public_id")
        )
    }
}
