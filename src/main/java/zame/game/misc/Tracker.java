package zame.game.misc;

public interface Tracker {
    void send(String event);
    void send(String event, String param);
}
