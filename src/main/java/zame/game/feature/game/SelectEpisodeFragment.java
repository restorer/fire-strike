package zame.game.feature.game;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Timer;
import java.util.TimerTask;
import zame.game.App;
import zame.game.core.util.Common;
import zame.game.R;
import zame.game.core.app.BaseFragment;
import zame.game.engine.state.State;
import zame.game.feature.sound.SoundManager;
import zame.game.engine.state.Profile;
import zame.game.engine.state.ProfileLevel;

public class SelectEpisodeFragment extends BaseFragment {
    private static final SparseArrayCompat<Integer> EPISODE_NAME_MAP = new SparseArrayCompat<>();

    static {
        EPISODE_NAME_MAP.put(-1, R.string.game_pt_tutorial);
        EPISODE_NAME_MAP.put(1, R.string.game_pt_episode_1);
        EPISODE_NAME_MAP.put(2, R.string.game_pt_episode_2);
        EPISODE_NAME_MAP.put(3, R.string.game_pt_episode_3);
    }

    public static SelectEpisodeFragment newInstance() {
        return new SelectEpisodeFragment();
    }

    private ViewGroup viewGroup;
    private ImageView[] images = new ImageView[2];
    private int currentImageIdx;
    private final Handler handler = new Handler();
    private Timer switchImagesTimer;
    private TimerTask switchImagesTimerTask;
    private Profile profile;
    private State state;
    private MapImageGenerator.MapImageBitmaps mapImageBitmaps;
    private SparseArrayCompat<MapImageGenerator.MapPath> mapPathsHash = new SparseArrayCompat<>();

    private final Runnable switchImagesRunnable = new Runnable() {
        @Override
        public void run() {
            images[currentImageIdx].setVisibility(View.INVISIBLE);
            currentImageIdx = (currentImageIdx + 1) % images.length;
            images[currentImageIdx].setVisibility(View.VISIBLE);
        }
    };

    private final View.OnClickListener onContinueClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            activity.soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
            activity.continueGame();
        }
    };

    public SelectEpisodeFragment() {
        super();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.state = this.activity.engine.state;
        this.profile = App.self.profile;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewGroup = (ViewGroup)inflater.inflate(R.layout.game_fragment_select_episode, container, false);

        images[0] = viewGroup.findViewById(R.id.image_1);
        images[1] = viewGroup.findViewById(R.id.image_2);

        viewGroup.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.soundManager.playSound(SoundManager.SOUND_BTN_PRESS);
                activity.showFragment(activity.menuFragment);
            }
        });

        viewGroup.findViewById(R.id.kontinue).setOnClickListener(onContinueClick);
        images[0].setOnClickListener(onContinueClick);
        images[1].setOnClickListener(onContinueClick);

        currentImageIdx = 0;
        updateImages();

        return viewGroup;
    }

    @Override
    protected void onShowBanner() {
        App.self.mediadtor.showBanner(activity, (ViewGroup)viewGroup.findViewById(R.id.banner_wrapper));
    }

    @Override
    public void onResume() {
        super.onResume();
        activity.soundManager.setPlaylist(SoundManager.LIST_MAIN);
        startTask();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTask();
    }

    public void updateImages() {
        if (mapImageBitmaps == null) {
            mapImageBitmaps = new MapImageGenerator.MapImageBitmaps(getResources());
        }

        ProfileLevel level = activity.tryAndLoadInstantState()
                ? profile.getLevel(state.levelName)
                : profile.getLevel(State.LEVEL_INITIAL);

        ((TextView)viewGroup.findViewById(R.id.info)).setText(String.format(getString(R.string.game_se_info),
                state.overallMonsters,
                state.overallSecrets,
                Common.getTimeString(state.overallSeconds),
                state.overallDeaths,
                state.overallResurrects));

        if (level.characterResId != 0) {
            ((ImageView)viewGroup.findViewById(R.id.character)).setImageResource(level.characterResId);
        }

        Integer episodeNameResId = EPISODE_NAME_MAP.get(level.episode);
        String episodeName = getString(episodeNameResId == null ? R.string.core_app_name : episodeNameResId);

        ((TextView)viewGroup.findViewById(R.id.episode)).setText(episodeName);
        MapImageGenerator.MapPath mapPath = mapPathsHash.get(level.episode);

        if (mapPath == null) {
            mapPath = MapImageGenerator.generateMapPath(level.episode, level.episodeLevelsCount);
            mapPathsHash.put(level.episode, mapPath);
        }

        images[0].setImageBitmap(MapImageGenerator.generateMapImage(mapPath, level.episodeIndex, mapImageBitmaps));
        images[1].setImageBitmap(MapImageGenerator.generateMapImage(mapPath, level.episodeIndex + 1, mapImageBitmaps));
    }

    private void startTask() {
        if (switchImagesTimerTask == null) {
            switchImagesTimerTask = new TimerTask() {
                @Override
                public void run() {
                    handler.post(switchImagesRunnable);
                }
            };

            if (switchImagesTimer != null) {
                switchImagesTimer.cancel();
            }

            switchImagesTimer = new Timer();

            //noinspection MagicNumber
            switchImagesTimer.schedule(switchImagesTimerTask, 250, 250);
        }
    }

    private void stopTask() {
        if (switchImagesTimerTask != null) {
            switchImagesTimerTask.cancel();
            switchImagesTimerTask = null;
        }

        if (switchImagesTimer != null) {
            switchImagesTimer.cancel();
            switchImagesTimer = null;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);

        if (hasWindowFocus) {
            startTask();
        } else {
            stopTask();
        }
    }
}
