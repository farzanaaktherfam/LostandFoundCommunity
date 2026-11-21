
package com.example.lostandfoundcommunity

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var currentPasswordInput: EditText
    private lateinit var newPasswordInput: EditText
    private lateinit var confirmNewPasswordInput: EditText
    private lateinit var resetPasswordButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        currentPasswordInput = findViewById(R.id.currentPasswordInput)
        newPasswordInput = findViewById(R.id.newPasswordInput)
        confirmNewPasswordInput = findViewById(R.id.confirmNewPasswordInput)
        resetPasswordButton = findViewById(R.id.resetPasswordButton)

        resetPasswordButton.setOnClickListener {
            updatePassword()
        }
    }

    private fun updatePassword() {

        val oldPassword = currentPasswordInput.text.toString()
        val newPassword = newPasswordInput.text.toString()
        val confirmPassword = confirmNewPasswordInput.text.toString()

        val user = FirebaseAuth.getInstance().currentUser

        if (oldPassword.isEmpty() || newPassword.isEmpty() ||
            confirmPassword.isEmpty()
        ) {
            Toast.makeText(
                this,
                "Please fill all fields",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (newPassword.length < 6) {
            Toast.makeText(
                this,
                "Password must be at least 6 characters",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (newPassword != confirmPassword) {
            Toast.makeText(
                this,
                "New passwords do not match",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (user == null || user.email.isNullOrEmpty()) {
            Toast.makeText(
                this,
                "Please login first",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        resetPasswordButton.isEnabled = false

        val credential = EmailAuthProvider.getCredential(
            user.email!!,
            oldPassword
        )

        user.reauthenticate(credential)
            .addOnSuccessListener {

                user.updatePassword(newPassword)
                    .addOnSuccessListener {

                        Toast.makeText(
                            this,
                            "Password updated successfully",
                            Toast.LENGTH_LONG
                        ).show()

                        finish()
                    }
                    .addOnFailureListener { error ->

                        resetPasswordButton.isEnabled = true

                        Toast.makeText(
                            this,
                            "Update failed: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener { error ->

                resetPasswordButton.isEnabled = true

                Toast.makeText(
                    this,
                    "Old password is incorrect: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}
