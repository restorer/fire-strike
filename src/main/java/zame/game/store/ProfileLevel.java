package zame.game.store;

public class ProfileLevel {
    private ProfileLevel next;

    public String name;
    public int episode;
    public int characterResId;
    public int episodeIndex;
    public int episodeLevelsCount = 1;

    ProfileLevel(String name, int episode, int characterResId) {
        this.name = name;
        this.episode = episode;
        this.characterResId = characterResId;
    }

    public void update(int episodeIndex, ProfileLevel next) {
        this.episodeIndex = episodeIndex;
        this.next = next;
    }

    public String getNextLevelName() {
        return (next == null ? "" : next.name);
    }
}
