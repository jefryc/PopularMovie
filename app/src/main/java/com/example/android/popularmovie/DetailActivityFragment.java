package com.example.android.popularmovie;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

/**
 * Created by Cheli.jefry on 10/1/2016.
 */
    public class DetailActivityFragment extends Fragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setHasOptionsMenu(true);
    }
        private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

        public DetailActivityFragment() {}
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Intent intent = getActivity().getIntent();
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            rootView.setBackgroundColor(Color.DKGRAY);
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                TextView synopsisView = (TextView) rootView.findViewById(R.id.detail_synopsis);
                TextView ratingView = (TextView) rootView.findViewById(R.id.detail_rating);
                TextView releaseDateView = (TextView) rootView.findViewById(R.id.detail_release_date);
                ImageView imageView = (ImageView) rootView.findViewById(R.id.detail_poster);
                TextView titleView = (TextView) rootView.findViewById(R.id.detail_title);
                String str = intent.getStringExtra(Intent.EXTRA_TEXT);
                String[] movieData = str.split("~~~");
                Log.v(LOG_TAG, movieData[3]);
                // movieData[0] = title, movieData[1] = overview, movieData[2] = release date, movieData[3] = poster path
                // movieData[4] = backdrop path, movieData[5] = movie id
                Picasso.with(getContext()).load(movieData[3]).fit().into(imageView);
                //titleView.setBackgroundColor(Color.DKGRAY);
                titleView.setText(movieData[0]);
                titleView.setTextColor(Color.WHITE);
                //ratingView.setBackgroundColor(Color.DKGRAY);
                ratingView.setText(movieData[6]);
                ratingView.setTextColor(Color.WHITE);
                //releaseDateView.setBackgroundColor(Color.DKGRAY);
                releaseDateView.setText(movieData[2]);
                releaseDateView.setTextColor(Color.WHITE);
                //synopsisView.setBackgroundColor(Color.DKGRAY);
                synopsisView.setText(movieData[1]);
                synopsisView.setTextColor(Color.WHITE);

            }
            return rootView;
        }

}
