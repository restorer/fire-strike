package zame.game.engine.state;

public class ProfileLevel {
    public static final int AD_NONE = 0;
    public static final int AD_REWARDED = 1; // only rewarded video (on gameover)
    public static final int AD_INTERSTITIAL = 2; // rewarded video + interstitial

    private ProfileLevel prev;
    private ProfileLevel next;

    public String name;
    public int episode;
    public int characterResId;
    public int adLevel;
    public int episodeIndex;
    public int episodeLevelsCount = 1;

    public ProfileLevel(String name, int episode, int characterResId, int adLevel) {
        this.name = name;
        this.episode = episode;
        this.characterResId = characterResId;
        this.adLevel = adLevel;
    }

    public void update(int episodeIndex, ProfileLevel prev, ProfileLevel next) {
        this.episodeIndex = episodeIndex;
        this.prev = prev;
        this.next = next;
    }

    public String getPrevLevelName() {
        return (prev == null ? "" : prev.name);
    }

    public String getNextLevelName() {
        return (next == null ? "" : next.name);
    }
}
