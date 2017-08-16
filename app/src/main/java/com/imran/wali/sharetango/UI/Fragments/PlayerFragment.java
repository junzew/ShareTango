package com.imran.wali.sharetango.UI.Fragments;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.drawable.Drawable;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.imran.wali.sharetango.DashboardActivity;
import com.imran.wali.sharetango.R;
import com.imran.wali.sharetango.Services.PlayService;
import com.imran.wali.sharetango.Utility.FastBlurUtil;
import com.imran.wali.sharetango.audiomanager.MusicData;
import com.imran.wali.sharetango.audiomanager.PlaybackController;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import static com.imran.wali.sharetango.UI.Fragments.AlbumFragment.ARTWORK_URI;

//import static com.imran.wali.sharetango.UI.Fragments.AlbumFragment.ARTWORK_URI;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnPlayerStatusChangeListener} interface
 * to handle interaction events.
 * Use the {@link PlayerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayerFragment extends Fragment {


    private OnPlayerStatusChangeListener mListener;

    LinearLayout mRootLayout;

    ImageView mPlayImage;
    ImageView mAlbumArtImage;
    TextView mAlbumTitle;
    SeekBar mSeekBar;
    ImageView mPreviousButton;
    ImageView mNextButton;

    ImageView mRepeatButton;
    SeekBar mVolumeBar;
    ImageView mVolumeImage; // mute

    float maxVolume;
    boolean isMute = false;
    boolean isRepeat = false;
    boolean isShuffle = false;
    boolean isNormal = true;

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
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mListener = (OnPlayerStatusChangeListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
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
    public interface OnPlayerStatusChangeListener {
        // TODO: Update argument type and name
        // notify DashboardActivity player status changed to playing/paused

        void onPlayerStatusChange(boolean isPlaying);
    }


    private void initViews(View view) {
        mAlbumArtImage = (ImageView) view.findViewById(R.id.album);
        mPlayImage = (ImageView) view.findViewById(R.id.play_button);
        mAlbumTitle = (TextView) view.findViewById(R.id.song_title);
        mSeekBar = (SeekBar) view.findViewById(R.id.progress);
        mPreviousButton = (ImageView) view.findViewById(R.id.previous);
        mNextButton = (ImageView) view.findViewById(R.id.next);
        mRootLayout = (LinearLayout) view.findViewById(R.id.dragView);

        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRepeat) {
                    mService.playPrevious(true);
                } else {
                    mService.restart();
                    mSeekBar.setProgress(0);
                }
                mPlayImage.setImageResource(R.drawable.ic_pause);
            }
        });
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNormal) {
                    mService.playNextOrStop(true);
                } else if (isRepeat) {
                    mService.restart();
                    mSeekBar.setProgress(0);
                } else {
                    mService.shuffle(true);
                }
                mPlayImage.setImageResource(R.drawable.ic_pause);
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
                    mPlayImage.setImageResource(R.drawable.ic_play);
                    mListener.onPlayerStatusChange(false);
                } else {
                    mService.resume();
                    mPlayImage.setImageResource(R.drawable.ic_pause);
                    Log.i("PlayService", "resume");
                    mListener.onPlayerStatusChange(true);
                }
            }
        });

        PlaybackController.getInstance().addListener(new PlaybackController.IMusicStartListener() {
            @Override
            public void startMusic(MusicData music, boolean isFromUser) {
                mPlayImage.setImageResource(R.drawable.ic_pause);
                long albumId = music.albumId;
                Uri uri = ContentUris.withAppendedId(ARTWORK_URI, albumId);
                Picasso.with(getActivity())
                        .load(uri)
                        .placeholder(R.drawable.default_album_art)
                        .into(mAlbumArtImage);
                String songTitle = music.getTitle();
                mAlbumTitle.setText(songTitle);
                mAlbumTitle.setSelected(true); // marquee text
                task = new UpdateSeekBarProgressTask();
                task.execute();

                // TODO Update background of root layout
                try {
                    Drawable background = FastBlurUtil.getBlurredBackgroundDrawable(uri, getActivity());
                    mRootLayout.setBackground(background);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mRepeatButton = (ImageView) view.findViewById(R.id.repeat);

        // ic_play mode
        mRepeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNormal) {
                    isRepeat = true;
                    isNormal = false;
                    isShuffle = false;
                    mRepeatButton.setImageResource(R.drawable.repeat_one);
                    Log.i("PlayActivity", "repeat one ic_play");
                } else if (isRepeat) {
                    isRepeat = false;
                    isNormal = false;
                    isShuffle = true;
                    mRepeatButton.setImageResource(R.drawable.shuffle);
                    Log.i("PlayActivity", "shuffle ic_play");
                } else {
                    isRepeat = false;
                    isNormal = true;
                    isShuffle = false;
                    mRepeatButton.setImageResource(R.drawable.repeat);
                    Log.i("PlayActivity", "normal ic_play");
                }
            }
        });

        // Audio handler
        mVolumeImage = (ImageView) view.findViewById(R.id.volume);
        mVolumeBar = (SeekBar) view.findViewById(R.id.volume_bar);
        maxVolume = (float) mVolumeBar.getMax(); // default is 100

        // change volume
        mVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // progress is the user input
                if (fromUser) {
                    // if user is updating the volume, then change mProgress
                    // http://stackoverflow.com/questions/5215459/android-mediaplayer-setvolume-function
                    float volume = (float) (1 - (Math.log(100 - progress) / Math.log(100)));
                    if (!isMute) {
                        mService.volume(volume, volume);
                    }
                    mService.setCurrVolume(volume);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        mVolumeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMute) {
                    isMute = false;
                    float vol = mService.getCurrVolume();
                    mService.volume(vol, vol);
                    mVolumeImage.setImageResource(R.drawable.volume);
                } else {
                    mService.volume(0, 0);
                    isMute = true;
                    mVolumeImage.setImageResource(R.drawable.mute);
                }
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
            DashboardActivity activity = (DashboardActivity) getActivity();
            while (activity != null && activity.isPlayServiceBound() && !isCancelled()) {
                if (!mService.isPlaying()) {
                    SystemClock.sleep(300);
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
            int from = mSeekBar.getProgress();
            int to = values[0];
            if (Math.abs(from - to) <= 1)
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
                .placeholder(R.drawable.default_album_art)
                .into(mAlbumArtImage);
        mAlbumTitle.setText(data.title);
        mSeekBar.setProgress(0);
        mPlayImage.setImageResource(R.drawable.ic_pause);
    }

    // change icon of play/pause
    public void updatePlayerStatus(boolean isPlaying) {
        if (isPlaying) {
            mPlayImage.setImageResource(R.drawable.ic_pause);
        } else {
            mPlayImage.setImageResource(R.drawable.ic_play);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (task != null) {
            task.cancel(true);
        }
    }
}
