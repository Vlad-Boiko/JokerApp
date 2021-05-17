package com.joker.it.apps.jokerapp.activitys.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.joker.it.apps.jokerapp.R
import com.joker.it.apps.jokerapp.ShowingInterface
import kotlinx.android.synthetic.main.start_joker_screen.*

class StartScreenFragment : Fragment(R.layout.start_joker_screen) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_ok.setOnClickListener {
            (activity as ShowingInterface).showWebView()

        }
    }

}