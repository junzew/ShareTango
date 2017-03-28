package com.imran.wali.sharetango.UI.activity;

/**
 * Created by junze on 2017-01-08.
 */

import android.support.v7.app.AppCompatActivity;

public class PlayActivity extends AppCompatActivity {
//
////    private PlayService mService = null;
//    private boolean mBound = false;
//    private boolean isPlaying = false;
//    ImageView mPlayImage;
//    ImageView mAlbumArtImage;
//    TextView mAlbumTitle;
//    SeekBar mSeekBar;
//    ImageView mPreviousButton;
//    ImageView mNextButton;
//    boolean isSeeking = false;
////    private UpdateSeekBarProgressTask task;
//    private BroadcastReceiver receiver;
//
//
//
////    private ServiceConnection mConnection = new ServiceConnection() {
////
////        @Override
////        public void onServiceConnected(ComponentName className, IBinder service) {
////            PlayService.PlayBinder binder = (PlayService.PlayBinder) service;
////            mService = (PlayService) binder.getService();
////            mBound = true;
//////            task = new UpdateSeekBarProgressTask();
//////            task.execute();
////        }
////
////        @Override
////        public void onServiceDisconnected(ComponentName arg0) {
////            mBound = false;
////        }
////    };
//
////    public class UpdateSeekBarProgressTask extends AsyncTask<Void, Integer, String> {
////
////        @Override
////        protected void onPreExecute() {
////            mSeekBar.setVisibility(ProgressBar.VISIBLE);
////        }
////
////        @Override
////        protected String doInBackground(Void... params) {
////
////            while (mBound && !isCancelled()) {
////                if (!mService.isPlaying()) {
////                    SystemClock.sleep(500);
////                } else {
////                    int p = (int) ((double)mService.currentPosition()/ (double)mService.getDuration() * 100);
////                    if (!isSeeking) {
////                        publishProgress(p);
////                    }
////                }
////            }
////            return "Complete";
////        }
////
////        @Override
////        protected void onProgressUpdate(Integer... values) {
////            mSeekBar.setProgress(values[0]);
////        }
////
////        @Override
////        protected void onPostExecute(String s) {
////            super.onPostExecute(s);
////            Toast.makeText(mService, "Complete", Toast.LENGTH_SHORT).show();
////        }
////    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        Log.d("PlayActivity", "onCreate");
//
////        setContentView(R.layout.activity_play);
////        initViews();
////        bindPlayService();
////        receiver = new BroadcastReceiver() {
////            @Override
////            public void onReceive(Context context, Intent intent) {
////                MusicData data = intent.getParcelableExtra(PlayService.BROADCAST_FILTER);
////                Uri uri = ContentUris.withAppendedId(ARTWORK_URI, data.albumId);
////                Picasso.with(PlayActivity.this)
////                        .load(uri)
////                        .placeholder(R.drawable.track_ablumart_placeholder)
////                        .into(mAlbumArtImage);
////                mAlbumTitle.setText(data.title);
////                mSeekBar.setProgress(0);
////            }
////        };
////
////        PlaybackController.getInstance().addListener(new IMusicStartListener() {
////            @Override
////            public void startMusic(MusicData music) {
////                long albumId = music.albumId;
////                Uri uri = ContentUris.withAppendedId(ARTWORK_URI, albumId);
////                Picasso.with(PlayActivity.this)
////                        .load(uri)
////                        .placeholder(R.drawable.track_ablumart_placeholder)
////                        .into(mAlbumArtImage);
////                String songTitle = music.getTitle();
////                mAlbumTitle.setText(songTitle);
////                task = new UpdateSeekBarProgressTask();
////                task.execute();
////            }
////        });
//
//    }
//    @Override
//    protected void onStart() {
//        super.onStart();
//        setVisible(true); // Android 6.0 bug
//        Log.d("PlayActivity", "onStart");
//        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
//                new IntentFilter(PlayService.BROADCAST_FILTER));
//    }
//
//    @Override
//    protected void onStop() {
//        Log.d("PlayActivity", "onStop");
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
//        super.onStop();
//    }
//
//    private void bindPlayService() {
//        Intent intent = new Intent(this, PlayService.class);
//        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
//        isPlaying = true;
//    }
//
//    private void initViews() {
//        Log.d("PlayActivity", "initViews");
//        mAlbumArtImage = mPlayerFragment.getAlbumArtImage();
//        mPlayImage = mPlayerFragment.getPlayImage();
//        mAlbumTitle = mPlayerFragment.getAlbumTitle();
//        mSeekBar = mPlayerFragment.getSeekBar();
//        mPreviousButton = mPlayerFragment.getPreviousButton();
//        mNextButton = mPlayerFragment.getNextButton();
//
//        mPreviousButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mService.playPrevious(true);
//            }
//        });
//        mNextButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mService.playNextOrStop(true);
//            }
//        });
//        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            int mProgress = 0;
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if(fromUser) {
//                    mProgress = progress;
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//                isSeeking = true;
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                mSeekBar.setProgress(mProgress);
//                isSeeking = false;
//                int position = (int) ((double) mProgress / 100 * mService.getDuration());
//                mService.seekTo(position);
//            }
//        });
//        mPlayImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(isPlaying) {
//                    mService.pause();
//                    Log.i("PlayActivity", "pause");
//                    isPlaying = false;
//                    mPlayImage.setImageResource(R.drawable.play);
//                } else {
//                    isPlaying = true;
//                    mService.resume();
//                    mPlayImage.setImageResource(R.drawable.pause_button);
//                    Log.i("PlayActivity", "resume");
//                }
//            }
//        });
//    }
//
//
//
//
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        Log.d("PlayActivity", "onDestroy");
//        if (mBound) {
//            mBound = false;
//            if (task != null) {
//                task.cancel(true);
//            }
//            mService.stopSelf();
//            unbindService(mConnection);
//        }
//    }
}
