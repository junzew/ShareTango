package com.imran.wali.sharetango.UI.Fragments;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.imran.wali.sharetango.AudioManager.MusicData;
import com.imran.wali.sharetango.AudioManager.PlaybackController;
import com.imran.wali.sharetango.DashboardActivity;
import com.imran.wali.sharetango.R;
import com.imran.wali.sharetango.service.PlayService;
import com.squareup.picasso.Picasso;

import static com.imran.wali.sharetango.UI.Fragments.AlbumFragment.ARTWORK_URI;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PlayerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PlayerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayerFragment extends Fragment {


    private OnFragmentInteractionListener mListener;

    ImageView mPlayImage;
    ImageView mAlbumArtImage;
    TextView mAlbumTitle;
    SeekBar mSeekBar;
    ImageView mPreviousButton;
    ImageView mNextButton;
    boolean isSeeking = false;
    private UpdateSeekBarProgressTask task;


    private PlayService mService;

    public PlayerFragment() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PlayerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PlayerFragment newInstance() {
        return new PlayerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_player, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        initViews(view);
        mService = ((DashboardActivity)getActivity()).getPlayService();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    private void initViews(View view) {
        mAlbumArtImage = (ImageView) view.findViewById(R.id.album);
        mPlayImage = (ImageView) view.findViewById(R.id.play_button);
        mAlbumTitle = (TextView) view.findViewById(R.id.song_title);
        mSeekBar = (SeekBar) view.findViewById(R.id.progress);
        mPreviousButton = (ImageView) view.findViewById(R.id.previous);
        mNextButton = (ImageView) view.findViewById(R.id.next);

        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.playPrevious(true);
            }
        });
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.playNextOrStop(true);
            }
        });
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int mProgress = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    mProgress = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSeekBar.setProgress(mProgress);
                isSeeking = false;
                int position = (int) ((double) mProgress / 100 * mService.getDuration());
                mService.seekTo(position);
            }
        });
        mPlayImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mService.isPlaying()) {
                    mService.pause();
                    Log.i("PlayService", "pause");
                    mPlayImage.setImageResource(R.drawable.play);
                } else {
                    mService.resume();
                    mPlayImage.setImageResource(R.drawable.pause_button);
                    Log.i("PlayService", "resume");
                }
            }
        });

        PlaybackController.getInstance().addListener(new PlaybackController.IMusicStartListener() {
            @Override
            public void startMusic(MusicData music) {
                long albumId = music.albumId;
                Uri uri = ContentUris.withAppendedId(ARTWORK_URI, albumId);
                Picasso.with(getActivity())
                        .load(uri)
                        .placeholder(R.drawable.track_ablumart_placeholder)
                        .into(mAlbumArtImage);
                String songTitle = music.getTitle();
                mAlbumTitle.setText(songTitle);
                task = new UpdateSeekBarProgressTask();
                task.execute();
            }
        });
    }

    public class UpdateSeekBarProgressTask extends AsyncTask<Void, Integer, String> {

        @Override
        protected void onPreExecute() {
            mSeekBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {

            while (((DashboardActivity)getActivity()).isBound() && !isCancelled()) {
                if (!mService.isPlaying()) {
                    SystemClock.sleep(500);
                } else {
                    int p = (int) ((double)mService.currentPosition()/ (double)mService.getDuration() * 100);
                    if (!isSeeking) {
                        publishProgress(p);
                    }
                }
            }
            return "Complete";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mSeekBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }


    public void updateMusicData(MusicData data) {
        Uri uri = ContentUris.withAppendedId(ARTWORK_URI, data.albumId);
        Picasso.with(getActivity())
                .load(uri)
                .placeholder(R.drawable.track_ablumart_placeholder)
                .into(mAlbumArtImage);
        mAlbumTitle.setText(data.title);
        mSeekBar.setProgress(0);
        mPlayImage.setImageResource(R.drawable.pause_button);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (task != null) {
            task.cancel(true);
        }
    }
}
