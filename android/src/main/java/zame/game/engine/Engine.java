package zame.game.engine;

import android.os.SystemClock;
import android.text.TextUtils;

import java.io.File;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

import zame.game.App;
import zame.game.core.util.Common;
import zame.game.engine.controller.HeroController;
import zame.game.engine.graphics.Labels;
import zame.game.engine.graphics.Renderer;
import zame.game.engine.graphics.TextureLoader;
import zame.game.engine.level.Level;
import zame.game.engine.level.LevelRenderer;
import zame.game.engine.state.Profile;
import zame.game.engine.state.ProfileLevel;
import zame.game.engine.state.State;
import zame.game.engine.visual.AutoMap;
import zame.game.engine.visual.EndLevel;
import zame.game.engine.visual.GameOver;
import zame.game.engine.visual.Overlay;
import zame.game.engine.visual.Stats;
import zame.game.engine.visual.Weapons;
import zame.game.feature.config.EventsConfig;
import zame.game.feature.main.MainActivity;
import zame.game.feature.sound.SoundManager;
import zame.game.flavour.config.AppConfig;

public class Engine {
    private static final long INTERSTITIAL_MIN_INTERVAL_MS = 3L * 60L * 1000L;

    static final int FRAMES_PER_SECOND = 40;
    public static final int FRAMES_PER_SECOND_D10 = FRAMES_PER_SECOND / 10; // must be >= 1
    private static final int UPDATE_INTERVAL = 1000 / FRAMES_PER_SECOND;
    private static final int FPS_AVG_LEN = 2;

    static final int VIEW_TYPE_GAME_MENU = 1;
    static final int VIEW_TYPE_SELECT_EPISODE = 2;
    public static final int VIEW_TYPE_REWARDED_VIDEO = 3;

    static final String DGB_SAVE_PREFIX = "debug_";

    public final Random random = new Random();
    public MainActivity activity;
    public boolean inWallpaperMode;

    public String instantName;
    String autosaveName;
    public Profile profile;
    public SoundManager soundManager;
    public HeroController heroController;
    public Config config = new Config();
    public Game game = new Game();
    public State state = new State();
    public Labels labels = new Labels();
    public Overlay overlay = new Overlay();
    private final TextureLoader textureLoader = new TextureLoader();
    public Level level = new Level();
    public Weapons weapons = new Weapons();
    public Renderer renderer = new Renderer();
    public LevelRenderer levelRenderer = new LevelRenderer();
    public Stats stats = new Stats();
    public AutoMap autoMap = new AutoMap();
    EndLevel endLevel = new EndLevel();
    GameOver gameOver = new GameOver();

    private boolean renderToTexture;
    private int screenWidth = 1;
    private int screenHeight = 1;
    public int width = 1;
    public int height = 1;
    public float ratio = 1.0f;

    public boolean interacted;
    public boolean gameViewActive = true;
    public boolean renderBlackScreen;
    private boolean callResumeAfterSurfaceCreated;
    private volatile boolean isPaused;
    private volatile long pausedTime;
    private long startTime;
    private long lastTime;
    public long elapsedTime;

    private int createdTexturesCount;
    private int totalTexturesCount;

    private int fpsFrames;
    private long fpsPrevRenderTime;
    private final int[] fpsList = new int[FPS_AVG_LEN];
    private int fpsCurrentIndex;

    private boolean isInterstitialPending;
    private boolean shouldShowInterstitial;
    private long lastInterstitialShownAt;
    public boolean canShowRewardedVideo;

    public float heroAr; // angle in radians
    public float heroCs; // cos of angle
    public float heroSn; // sin of angle
    boolean showFps;

    public int gameMenuPendingStep;

    private final Runnable showGameMenuRunnable = new Runnable() {
        @Override
        public void run() {
            if (activity != null) {
                activity.gameFragment.showGameMenu();
            }
        }
    };

    private final Runnable showSelectEpisodeRunnable = new Runnable() {
        @Override
        public void run() {
            if (activity == null) {
                return;
            }

            activity.showFragment(activity.selectEpisodeFragment);

            if (shouldShowInterstitial) {
                isInterstitialPending = false;
                lastInterstitialShownAt = SystemClock.elapsedRealtime();
                App.self.mediadtor.showInterstitial(activity);
            }
        }
    };

    private final Runnable showRewardedVideoRunnable = new Runnable() {
        @Override
        public void run() {
            if (activity != null) {
                lastInterstitialShownAt = SystemClock.elapsedRealtime();
                App.self.tracker.trackEvent(EventsConfig.EV_GAME_GAME_OVER_REWARDED_SHOWN);
                App.self.mediadtor.showRewardedVideo(activity);
            }
        }
    };

    public Engine(MainActivity activity) {
        this.activity = activity;
        inWallpaperMode = (activity == null);

        instantName = (inWallpaperMode ? "winstant" : "instant");
        autosaveName = (inWallpaperMode ? "" : "autosave");

        profile = App.self.profile;
        soundManager = SoundManager.getInstance(inWallpaperMode);
        heroController = HeroController.newInstance(inWallpaperMode);

        config.onCreate(this);
        game.onCreate(this);
        state.onCreate(this);
        labels.onCreate(this);
        overlay.onCreate(this);
        textureLoader.onCreate(this);
        level.onCreate(this);
        levelRenderer.onCreate(this);
        weapons.onCreate(this);
        renderer.onCreate(this);
        stats.onCreate(this);
        autoMap.onCreate(this);
        heroController.onCreate(this);
        endLevel.onCreate(this);
        gameOver.onCreate(this);
    }

    public void onActivated() {
        config.reload();
        heroController.reload();
        game.onActivated();

        interacted = false;
        gameViewActive = true;
        renderBlackScreen = false;
        callResumeAfterSurfaceCreated = true;
        startTime = SystemClock.elapsedRealtime();
    }

    public void updateAfterLevelLoadedOrCreated() {
        canShowRewardedVideo = App.self.mediadtor.isRewardedVideoEnabled() &&
                (profile.getLevel(state.levelName).adLevel >= ProfileLevel.AD_REWARDED);

        level.updateAfterLevelLoadedOrCreated();
        levelRenderer.updateAfterLevelLoadedOrCreated();
        heroController.updateAfterLevelLoadedOrCreated();
        weapons.setHeroWeaponImmediate(state.heroWeapon);
    }

    void createAutosave() {
        if (!TextUtils.isEmpty(autosaveName)) {
            state.save(autosaveName);
        }
    }

    public String getSavePathBySaveName(String name) {
        return App.self.internalRoot + name + ".save";
    }

    public boolean hasInstantSave() {
        return hasGameSave(instantName);
    }

    boolean hasGameSave(String saveName) {
        return (new File(getSavePathBySaveName(saveName))).exists();
    }

    public void deleteInstantSave() {
        File instant = new File(getSavePathBySaveName(instantName));

        if (instant.exists()) {
            //noinspection ResultOfMethodCallIgnored
            instant.delete();
        }

        // also delete wallpaper instant save
        instant = new File(App.self.internalRoot + "winstant.save");

        if (instant.exists()) {
            //noinspection ResultOfMethodCallIgnored
            instant.delete();
        }
    }

    public void changeView(int viewType) {
        switch (viewType) {
            case VIEW_TYPE_GAME_MENU: {
                gameMenuPendingStep = 1;
                break;
            }

            case VIEW_TYPE_SELECT_EPISODE: {
                gameViewActive = false;
                renderBlackScreen = true;

                String prevLevelName = profile.getLevel(state.levelName).getPrevLevelName();
                ProfileLevel prevProfileLevel = profile.getLevel(prevLevelName);

                shouldShowInterstitial = (isInterstitialPending
                        || prevProfileLevel.adLevel >= ProfileLevel.AD_INTERSTITIAL);

                long lastShowInterval = SystemClock.elapsedRealtime() - lastInterstitialShownAt;

                if (shouldShowInterstitial && (!App.self.mediadtor.isInterstitialLoaded()
                        || lastShowInterval < INTERSTITIAL_MIN_INTERVAL_MS)) {

                    shouldShowInterstitial = false;
                    isInterstitialPending = true;
                }

                if (AppConfig.DEBUG) {
                    Common.log("Interstitial: prevLevelName = "
                            + prevLevelName
                            + ", adLevel = "
                            + prevProfileLevel.adLevel
                            + ", isInterstitialPending = "
                            + isInterstitialPending
                            + ", isInterstitialLoaded = "
                            + App.self.mediadtor.isInterstitialLoaded()
                            + ", lastShowInterval = "
                            + lastShowInterval
                            + " (INTERSTITIAL_MIN_INTERVAL_MS = "
                            + INTERSTITIAL_MIN_INTERVAL_MS
                            + "), shouldShowInterstitial = "
                            + shouldShowInterstitial);
                }

                if (shouldShowInterstitial) {
                    forceStateSave();
                }

                App.self.handler.post(showSelectEpisodeRunnable);
                break;
            }

            case VIEW_TYPE_REWARDED_VIDEO: {
                if (activity != null && App.self.mediadtor.isRewardedVideoLoaded()) {
                    forceStateSave();
                    App.self.handler.post(showRewardedVideoRunnable);
                } else {
                    game.loadLevel(Game.LOAD_LEVEL_NORMAL);
                }
                break;
            }
        }
    }

    public void onRewardedVideoClosed(boolean shouldGiveReward) {
        game.isRewardedVideoWatched = shouldGiveReward;
        gameViewActive = false;
        renderBlackScreen = true;
    }

    public int getRealHits(int maxHits, float dist) {
        //noinspection MagicNumber
        float div = Math.max(1.0f, dist * 0.35f);

        int minHits = Math.max(1, (int)((float)maxHits / div));
        return (random.nextInt(maxHits - minHits + 1) + minHits);
    }

    public void onSurfaceCreated(GL10 gl) {
        renderer.onSurfaceCreated(gl);

        createdTexturesCount = 0;
        totalTexturesCount = TextureLoader.TEXTURES_TO_LOAD.length + 1;
        textureLoader.onSurfaceCreated();

        if (callResumeAfterSurfaceCreated) {
            callResumeAfterSurfaceCreated = false;
            onResume();
        }
    }

    @SuppressWarnings("ManualMinMaxCalculation")
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.screenWidth = (width < 1 ? 1 : width); // just for case
        this.screenHeight = (height < 1 ? 1 : height); // just for case

        renderer.onSurfaceChanged(gl);
        renderToTexture = (inWallpaperMode && width < height);

        if (renderToTexture) {
            renderer.prepareFramebuffer();
            this.width = renderer.getRenderToTextureSize();

            //noinspection SuspiciousNameCombination
            this.height = this.width;
        } else {
            this.width = screenWidth;
            this.height = screenHeight;
        }

        renderer.useViewport(this.width, this.height);
        ratio = (float)(this.width < 1 ? 1 : this.width) / (float)(this.height < 1 ? 1 : this.height);

        heroController.surfaceSizeChanged();
        stats.surfaceSizeChanged();
    }

    public void onResume() {
        if (callResumeAfterSurfaceCreated) {
            // wait for created surface
            return;
        }

        if (isPaused) {
            isPaused = false;
            gameMenuPendingStep = 0;
            elapsedTime = state.tempElapsedTime;
            lastTime = state.tempLastTime;
            startTime = SystemClock.elapsedRealtime() - elapsedTime;
        }
    }

    public void onPause() {
        if (!isPaused) {
            pausedTime = SystemClock.elapsedRealtime();
            isPaused = true;
            forceStateSave();
        }
    }

    private void forceStateSave() {
        state.tempElapsedTime = elapsedTime;
        state.tempLastTime = lastTime;
        state.save(instantName);
    }

    public void onDrawFrame(GL10 gl) {
        //noinspection MagicNumber
        if (isPaused && gameMenuPendingStep < 0 && (SystemClock.elapsedRealtime() - pausedTime) < 250L) {
            // Fix visual glitch when game menu appears
            return;
        }

        renderer.onDrawFrame(gl);

        if (isPaused || gameMenuPendingStep > 0) {
            render(gl);

            // 2 rendered frames should be enough
            if (gameMenuPendingStep >= 2) {
                onPause(); // force pause
                gameMenuPendingStep = -1;
                App.self.handler.post(showGameMenuRunnable);
            } else if (gameMenuPendingStep > 0) {
                gameMenuPendingStep++;
            }

            return;
        }

        heroController.onDrawFrame();
        elapsedTime = SystemClock.elapsedRealtime() - startTime;

        if (lastTime > elapsedTime) {
            lastTime = elapsedTime;
        }

        if (elapsedTime - lastTime > UPDATE_INTERVAL) {
            long count = (elapsedTime - lastTime) / UPDATE_INTERVAL;

            if (count > 10) {
                count = 10;
                lastTime = elapsedTime;
            } else {
                lastTime += UPDATE_INTERVAL * count;
            }

            if (createdTexturesCount >= totalTexturesCount) {
                while (count > 0 && !renderBlackScreen) {
                    game.update();
                    count--;
                }
            }
        }

        render(gl);
    }

    @SuppressWarnings("MagicNumber")
    private void renderPreloader() {
        renderer.useOrtho(-ratio, ratio, 1.0f, -1.0f, 0.0f, 1.0f);
        renderer.startBatch();
        renderer.setColorQuadRGBA(1.0f, 1.0f, 1.0f, 1.0f);
        renderer.setCoordsQuadRectFlat(-0.5f, -0.5f, 0.5f, 0.5f);
        renderer.setTexRect(0, 0, 1 << 16, 1 << 16);
        renderer.batchQuad();
        renderer.renderBatch(0, Renderer.TEXTURE_LOADING);
    }

    private void renderDimLayer() {
        renderer.useOrtho(0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f);
        renderer.startBatch();
        renderer.setColorQuadRGBA(0.0f, 0.0f, 0.0f, config.wpDim);
        renderer.setCoordsQuadRectFlat(0.0f, 0.0f, 1.0f, 1.0f);
        renderer.batchQuad();
        renderer.renderBatch(Renderer.FLAG_BLEND);
    }

    @SuppressWarnings("MagicNumber")
    protected void render(GL10 gl) {
        if (renderBlackScreen) {
            renderer.clear();
            return;
        }

        if (renderToTexture) {
            renderer.startRenderToTexture();
        } else {
            renderer.clear();
        }

        if (createdTexturesCount < totalTexturesCount) {
            renderPreloader();

            if (createdTexturesCount == 0) {
                labels.createLabels();
                createdTexturesCount++;
            } else if (App.self.cachedTexturesReady) {
                textureLoader.loadTexture(createdTexturesCount - 1);
                createdTexturesCount++;
            }

            if (createdTexturesCount >= totalTexturesCount) {
                System.gc();
            }
        } else {
            game.render();
        }

        if (inWallpaperMode || isPaused) {
            try {
                //noinspection MagicNumber
                Thread.sleep(40);
            } catch (InterruptedException e) {
                // ignored
            }
        }

        if (inWallpaperMode) {
            renderDimLayer();
        }

        // https://stackoverflow.com/questions/10729352/framebuffer-fbo-render-to-texture-is-very-slow-using-opengl-es-2-0-on-android
        // http://www.java2s.com/Code/Android/File/DemonstratetheFrameBufferObjectOpenGLESextension.htm
        // https://www.gamedev.net/forums/topic/590324-fbo-set-up-on-android/

        if (renderToTexture) {
            renderer.finishRenderToTexture(width, height);

            float halfScreenWidth = (float)screenWidth * 0.5f;
            float halfScreenHeight = (float)screenHeight * 0.5f;

            renderer.startBatch();
            renderer.setColorQuadRGBA(1.0f, 1.0f, 1.0f, 1.0f);
            renderer.setTexRect(0, 0, 1 << 16, 1 << 16);
            renderer.setCoordsQuadRectFlat(-halfScreenHeight, -halfScreenHeight, halfScreenHeight, halfScreenHeight);
            renderer.batchQuad();

            gl.glViewport(0, 0, screenWidth, screenHeight);
            renderer.useOrtho(-halfScreenWidth, halfScreenWidth, -halfScreenHeight, halfScreenHeight, 0.0f, 1.0f);
            renderer.renderBatch(0, Renderer.TEXTURE_RTT);
            gl.glViewport(0, 0, width, height);
        }
    }

    void renderFps() {
        labels.startBatch();
        renderer.setColorQuadRGBA(1.0f, 1.0f, 1.0f, 1.0f);

        //noinspection MagicNumber
        labels.batch(
                -ratio + 0.01f,
                -1.0f + 0.01f,
                ratio,
                1.0f,
                String.format(labels.map[Labels.LABEL_FPS], getAvgFps()),
                0.125f,
                Labels.ALIGN_BL);

        labels.renderBatch();
    }

    @SuppressWarnings("MagicNumber")
    private int getAvgFps() {
        fpsFrames++;

        long time = SystemClock.elapsedRealtime();
        long diff = time - fpsPrevRenderTime;

        if (diff > 1000) {
            int seconds = (int)(diff / 1000L);
            fpsPrevRenderTime += (long)seconds * 1000L;

            fpsList[fpsCurrentIndex] = fpsFrames / seconds;
            fpsCurrentIndex = (fpsCurrentIndex + 1) % FPS_AVG_LEN;

            fpsFrames = 0;
        }

        int sum = 0;

        for (int v : fpsList) {
            sum += v;
        }

        return (sum / FPS_AVG_LEN);
    }
}
