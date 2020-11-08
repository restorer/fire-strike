package zame.game.engine.level;

import android.content.res.AssetManager;

import java.util.Locale;

import zame.game.core.util.Common;

public class LevelConfig {
    public static final int HIT_TYPE_MEELE = 0;
    public static final int HIT_TYPE_CLIP = 1;
    public static final int HIT_TYPE_SHELL = 2;
    public static final int HIT_TYPE_GRENADE = 3;

    public static class MonsterConfig {
        public int health;
        public int hits;
        public int hitType;

        MonsterConfig(int health, int hits, int hitType) {
            this.health = health;
            this.hits = hits;
            this.hitType = hitType;
        }
    }

    @SuppressWarnings({ "FieldCanBeLocal", "unused", "RedundantSuppression" }) private final String levelName;
    public int graphicsSet;
    public MonsterConfig[] monsters;

    @SuppressWarnings("MagicNumber")
    private LevelConfig(String levelName) {
        this.levelName = levelName;
        this.graphicsSet = 1;

        this.monsters = new MonsterConfig[] { new MonsterConfig(8, 5, HIT_TYPE_CLIP), // Soldier with pistol
                new MonsterConfig(15, 7, HIT_TYPE_SHELL), // Soldier with rifle
                new MonsterConfig(21, 18, HIT_TYPE_MEELE), // Soldier with knife
                new MonsterConfig(33, 25, HIT_TYPE_GRENADE), // Soldier with grenade
                new MonsterConfig(49, 10, HIT_TYPE_SHELL), }; // Zombie
    }

    @SuppressWarnings("MagicNumber")
    public static LevelConfig read(AssetManager assetManager, String levelName) {
        LevelConfig res = new LevelConfig(levelName);

        try {
            byte[] data = Common.readBytes(assetManager.open(String.format(Locale.US, "levels/%s.map", levelName)));

            int pos = 0;
            res.graphicsSet = data[pos++];

            while (pos < data.length) {
                int idx = (int)data[pos] & 0xFF;

                if (idx < 1 || idx > res.monsters.length) {
                    break;
                }

                res.monsters[idx - 1].health = (int)data[pos + 1] & 0xFF;
                res.monsters[idx - 1].hits = (int)data[pos + 2] & 0xFF;
                res.monsters[idx - 1].hitType = (int)data[pos + 3] & 0xFF;
                pos += 4;
            }

            //noinspection UnusedAssignment
            data = null;
        } catch (Exception ex) {
            Common.log(ex);
            throw new RuntimeException(ex);
        }

        return res;
    }
}
