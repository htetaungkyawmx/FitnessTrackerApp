package org.hak.fitnesstrackerapp.databinding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewbinding.ViewBinding
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class ActivityMainBinding private constructor(
    @NonNull private val rootView: ConstraintLayout,
    @NonNull val bottomNavigation: BottomNavigationView,
    @NonNull val fragmentContainer: ConstraintLayout,
    @NonNull val toolbar: Toolbar
) : ViewBinding {
    @NonNull
    override fun getRoot(): View {
        return rootView
    }

    companion object {
        @NonNull
        fun inflate(@NonNull inflater: LayoutInflater): ActivityMainBinding {
            return inflate(inflater, null, false)
        }

        @NonNull
        fun inflate(@NonNull inflater: LayoutInflater, @Nullable parent: ViewGroup?, boolean: Boolean): ActivityMainBinding {
            val root = inflater.inflate(org.hak.fitnesstrackerapp.R.layout.activity_main, parent, false)
            if (boolean) {
                parent?.addView(root)
            }
            return bind(root)
        }

        @NonNull
        fun bind(@NonNull rootView: View): ActivityMainBinding {
            return ActivityMainBinding(
                rootView as ConstraintLayout,
                rootView.findViewById(org.hak.fitnesstrackerapp.R.id.bottom_navigation),
                rootView.findViewById(org.hak.fitnesstrackerapp.R.id.fragment_container),
                rootView.findViewById(org.hak.fitnesstrackerapp.R.id.toolbar)
            )
        }
    }
}
