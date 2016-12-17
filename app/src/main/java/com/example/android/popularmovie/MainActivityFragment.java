package com.example.android.popularmovie;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private ImageListAdapter imageViewArrayAdapter;
    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ArrayList<String> arr = new ArrayList<String>();
        ArrayList<String> sortByArr = new ArrayList<String>();
        sortByArr.add(getString(R.string.most_popular));
        sortByArr.add(getString(R.string.top_rated));
        imageViewArrayAdapter = new ImageListAdapter(getActivity(), arr);
        GridView gridView = (GridView) rootView.findViewById(R.id.gridView);
        Spinner spinner = (Spinner) rootView.findViewById(R.id.spinner);
        gridView.setAdapter(imageViewArrayAdapter);
        final ArrayAdapter<String> sortByAdapter = new ArrayAdapter<String>(getActivity(), R.layout.support_simple_spinner_dropdown_item, sortByArr);
        spinner.setAdapter(sortByAdapter);

        // update data based on the selection
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String str = sortByAdapter.getItem(position);
                updateMovieData(str);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str = (String) imageViewArrayAdapter.getItem(position);
                Log.v(LOG_TAG, str);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, str);
                startActivity(intent);
            }
        });
        return rootView;
    }

    private void updateMovieData(String str) {
        FetchData fd = new FetchData();
        fd.execute(str);
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    // Class to do the fetch data(URLs and info)
    private class FetchData extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = FetchData.class.getSimpleName();

        @Override
        protected void onPostExecute(String[] strings) {
            if (strings != null) {
                imageViewArrayAdapter.clear();
                for (String s : strings)
                    imageViewArrayAdapter.add(s);
                Log.v(LOG_TAG, "Updating imageViewArrayAdapter");
            }
            Log.v(LOG_TAG, "onPostExecute");
        }

        @Override
        protected String[] doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieData = null;
            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                // URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");
                final String baseUrl = "http://api.themoviedb.org/3/movie/";
                String sortBy;
                String sort = params[0];
                final String api_key = "api_key";
                final String popular = "popular";
                final String top_rated = "top_rated";

                if (sort.equalsIgnoreCase(getString(R.string.most_popular)))
                    sortBy = popular;
                else if (sort.equalsIgnoreCase(getString(R.string.top_rated)))
                    sortBy = top_rated;
                else
                    sortBy = "";

                Uri builtUri = Uri.parse(baseUrl).buildUpon().appendPath(sortBy).appendQueryParameter(api_key, BuildConfig.MOVIE_API_KEY).build();

                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "URL: " + url.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                int len = 0;
                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    len += line.length();
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    movieData = null;
                }
                movieData = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                String[] result = getMovieDataFromJSON(movieData);
                int count = result.length;
                for (int i = 0 ; i < count; i++) {
                    Log.v(LOG_TAG, "Movie: " + result[i].split("~~~")[3]);
                }
                return result;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        private String[] getMovieDataFromJSON(String movieData) throws JSONException {

            String[] result;
            final String M_POSTER_PATH = "poster_path";
            final String M_ADULT = "adult";
            final String M_OVERVIEW = "overview";
            final String M_RELEASE_DATE = "release_date";
            final String M_ORIGINAL_TITLE = "original_title";
            final String M_BACKDROP_PATH = "backdrop_path";
            final String M_VOTE_AVERAGE = "vote_average";
            final String M_ID = "id";
            final String M_RESULT = "results";
            final String base_url_movie_poster = "https://image.tmdb.org/t/p/w185";

            JSONObject movieDataJSON = new JSONObject(movieData);
            JSONArray movieArray = movieDataJSON.getJSONArray(M_RESULT);

            int count = movieArray.length();
            result = new String[count];
            Log.v(LOG_TAG, "number of movies: " + count);
            for (int i = 0; i < count; i++) {
                JSONObject movie = movieArray.getJSONObject(i);
                //Log.v(LOG_TAG, "movieData :" + movie.toString());
                String poster_path, overview, release_date, original_title, backdrop_path, id, rating;
                poster_path = base_url_movie_poster + movie.getString(M_POSTER_PATH);
                overview = movie.getString(M_OVERVIEW);
                release_date = movie.getString(M_RELEASE_DATE);
                original_title = movie.getString(M_ORIGINAL_TITLE);
                backdrop_path = base_url_movie_poster + movie.getString(M_BACKDROP_PATH);
                id = movie.getString(M_ID);
                rating = movie.getString(M_VOTE_AVERAGE);
                result[i] = original_title + "~~~" + overview + "~~~" + release_date + "~~~" + poster_path
                        + "~~~" + backdrop_path + "~~~" + id + "~~~" + rating;
//                result[i] = poster_path;
            }
            return result;
        }
    }


        public class ImageListAdapter extends ArrayAdapter {
            private Context context;
            private LayoutInflater inflater;

            private List<String> imageUrls;

            public ImageListAdapter(Context context, List<String> imageUrls) {
                super(context, R.layout.movie_poster, imageUrls);

                this.context = context;
                this.imageUrls = imageUrls;

                inflater = LayoutInflater.from(context);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (null == convertView) {
                    convertView = inflater.inflate(R.layout.movie_poster, parent, false);
                }

                Picasso
                        .with(context)
                        .load(imageUrls.get(position).split("~~~")[3])
                        .fit() // will explain later
                        .into((ImageView) convertView);

                return convertView;
            }
        }

}
