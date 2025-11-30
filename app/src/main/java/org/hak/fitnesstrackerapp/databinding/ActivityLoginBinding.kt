package org.hak.fitnesstrackerapp.databinding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.cardview.widget.CardView
import androidx.viewbinding.ViewBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ActivityLoginBinding private constructor(
    @NonNull private val rootView: View,
    @NonNull val cardView: CardView,
    @NonNull val forgotPasswordTextView: TextView,
    @NonNull val loginButton: Button,
    @NonNull val logoImageView: ImageView,
    @NonNull val passwordEditText: TextInputEditText,
    @NonNull val passwordTextInputLayout: TextInputLayout,
    @NonNull val progressBar: ProgressBar,
    @NonNull val registerTextView: TextView,
    @NonNull val usernameEditText: TextInputEditText,
    @NonNull val usernameTextInputLayout: TextInputLayout
) : ViewBinding {
    @NonNull
    override fun getRoot(): View {
        return rootView
    }

    companion object {
        @NonNull
        fun inflate(@NonNull inflater: LayoutInflater): ActivityLoginBinding {
            return inflate(inflater, null, false)
        }

        @NonNull
        fun inflate(@NonNull inflater: LayoutInflater, @Nullable parent: ViewGroup?, boolean: Boolean): ActivityLoginBinding {
            val root = inflater.inflate(org.hak.fitnesstrackerapp.R.layout.activity_login, parent, false)
            if (boolean) {
                parent?.addView(root)
            }
            return bind(root)
        }

        @NonNull
        fun bind(@NonNull rootView: View): ActivityLoginBinding {
            return ActivityLoginBinding(
                rootView,
                rootView.findViewById(org.hak.fitnesstrackerapp.R.id.cardView),
                rootView.findViewById(org.hak.fitnesstrackerapp.R.id.forgotPasswordTextView),
                rootView.findViewById(org.hak.fitnesstrackerapp.R.id.loginButton),
                rootView.findViewById(org.hak.fitnesstrackerapp.R.id.logoImageView),
                rootView.findViewById(org.hak.fitnesstrackerapp.R.id.passwordEditText),
                rootView.findViewById(org.hak.fitnesstrackerapp.R.id.passwordTextInputLayout),
                rootView.findViewById(org.hak.fitnesstrackerapp.R.id.progressBar),
                rootView.findViewById(org.hak.fitnesstrackerapp.R.id.registerTextView),
                rootView.findViewById(org.hak.fitnesstrackerapp.R.id.usernameEditText),
                rootView.findViewById(org.hak.fitnesstrackerapp.R.id.usernameTextInputLayout)
            )
        }
    }
}
