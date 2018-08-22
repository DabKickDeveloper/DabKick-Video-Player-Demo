package sample.sdk.dabkick.sampleappdkvp.PlayVideos;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dabkick.dkvideoplayer.livesession.models.StageModel;
import com.dabkick.dkvideoplayer.livesession.videoplayer.DkVideoView;
import com.dabkick.dkvideoplayer.publicsettings.DabKickRegistration;
import com.dabkick.dkvideoplayer.publicsettings.NotifyStageVideoReceived;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import sample.sdk.dabkick.sampleappdkvp.R;
import sample.sdk.dabkick.sampleappdkvp.VideoDetails.VideoItemDetail;
import timber.log.Timber;

public class PlayerActivity extends AppCompatActivity {

    public static VideoItemDetail detail;
    TextView title;
    TextView desc;
    ListView recomended;
    //dabkick video view
    DkVideoView mVideoPlayer;
    public static boolean isRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        //if not registered registering in this activity
        if(!isRegistered)
            DabKickRegistration.newInstance().register(this);

        init();
    }

    void init(){

        title = findViewById(R.id.title);
        desc = findViewById(R.id.desc);
        recomended = findViewById(R.id.recomended);
        desc.setMovementMethod(new ScrollingMovementMethod());
        mVideoPlayer = findViewById(R.id.video_view);

        if(detail != null){
            title.setText(detail.getVideoTitle());
            desc.setText(detail.getDesc());
            final String detailUrl = detail.getUrl();

            String videoID = detailUrl.substring(detailUrl.lastIndexOf("=") + 1);

            LoadYoutubeVideos.getInstance().setOnFinishedDownload(new LoadYoutubeVideos.OnFinishedDownloadListener() {
                @Override
                public void onFinishedDownload(String fullStreamURL, boolean success) {
                    if(success){
                        //set the current video url you wish to play in the dabkick player
                        mVideoPlayer.setMediaItem(fullStreamURL);
                        //prepare the video along with 'true' for sending it to our backend/firebase
                        mVideoPlayer.prepare(true);

                    }else{

                        Toast.makeText(PlayerActivity.this, "Unable to play video", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            LoadYoutubeVideos.getInstance().loadYoutubeURL(PlayerActivity.this, videoID);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //release the player if not in use
        if(mVideoPlayer != null)
            mVideoPlayer.release();
    }

    //added to ensure that you receive any videos added by your friend in the other end
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(NotifyStageVideoReceived event) {

        Timber.d("onMessageEvent: " + event.url);

        if(event.url != null && mVideoPlayer != null) {

                mVideoPlayer.setMediaItem(event.url);
                mVideoPlayer.prepare(false);
                mVideoPlayer.showPopUp = false;

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //register with event bus for recieving NotifyStageVideoReceived event
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //unregister with event bus for NotifyStageVideoReceived event
        EventBus.getDefault().unregister(this);
    }


}
