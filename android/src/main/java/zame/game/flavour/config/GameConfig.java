package zame.game.flavour.config;

@SuppressWarnings("PointlessArithmeticExpression")
public final class GameConfig {
    public static final int COLOR_SKY = 0x45a7f2;

    public static final int MAX_MONSTER_TYPES = 5;

    public static final int HEALTH_MAX = 100;
    public static final int HEALTH_ADD_STIM = 10;
    public static final int HEALTH_ADD_MEDI = 50;

    public static final int HEALTH_BARREL = 25;

    public static final int HEALTH_HIT_KNIFE = 3; // 2 hits per second -> 6 dmg per second
    public static final int HEALTH_HIT_PISTOL = 2; // 2 hits per second -> 4 dmg per second
    public static final int HEALTH_HIT_DBLPISTOL = 2; // 4 hits per second -> 8 dmg per second
    public static final int HEALTH_HIT_AK47 = 3; // 4 hits per second -> 12 dmg per second
    public static final int HEALTH_HIT_TMP = 4; // 5 hits per second -> 20 dmg per second
    public static final int HEALTH_HIT_WINCHESTER = 32; // 1 hit per second -> 32 dmg per second (but need 2 bullets)
    public static final int HEALTH_HIT_GRENADE = 52; // 1 hit per second -> 52 dmg per second
    public static final int HEALTH_HIT_BARREL = 40; // бочка мочит и ГГ и врагов, так что это некое такое оптимальное значение

    public static final double ARMOR_HEALTH_SAVER = 0.25; // if hero has armor, than it take only (hits * saver) damage
    public static final double ARMOR_HIT_TAKER = 0.75; // if hero has armor, than it loose (hits * taker) armor

    // 1 frame = 1s / Engine.FRAMES_PER_SECOND = 1s / 40 = 0.025s
    // 1s = 40 frames

    public static final int STUN_KNIFE = 15; // 2 hits per second -> 15 * 2 / 40 -> 0.75 stn per second
    public static final int STUN_PISTOL = 5; // 2 hits per second -> 5 * 2 / 40 -> 0.25 stn per second
    public static final int STUN_DBLPISTOL = 4; // 4 hits per second -> 4 * 4 / 40 -> 0.4 stn per second
    public static final int STUN_AK47 = 3; // 4 hits per second -> 3 * 4 / 40 -> 0.3 stn per second
    public static final int STUN_TMP = 4; // 5 hits per second -> 4 * 5 / 40 -> 0.5 stn per second
    public static final int STUN_WINCHESTER = 20; // 1 hit per second -> 20 * 1 / 40 -> 0.5 stn per second
    public static final int STUN_GRENADE = 25; // 1 hit per second -> 25 * 1 / 40 -> 0.625 stn per second

    public static final int ARMOR_MAX = 200;
    public static final int ARMOR_ADD_GREEN = 100;
    public static final int ARMOR_ADD_RED = 200;

    public static final int AMMO_MAX_CLIP = 150;
    public static final int AMMO_MAX_SHELL = 75;
    public static final int AMMO_MAX_GRENADE = 50;

    public static final int AMMO_ENSURED_CLIP = 50;
    public static final int AMMO_ENSURED_SHELL = 25;
    public static final int AMMO_ENSURED_GRENADE = 5;

    public static final int AMMO_ADD_CLIP = 5;
    public static final int AMMO_ADD_CBOX = 20;
    public static final int AMMO_ADD_SHELL = 5;
    public static final int AMMO_ADD_SBOX = 15;
    public static final int AMMO_ADD_GRENADE = 1;
    public static final int AMMO_ADD_GBOX = 5;

    public static final int AMMO_ADD_DBLPIST = 20;
    public static final int AMMO_ADD_AK47 = 30;
    public static final int AMMO_ADD_TMP = 3;
    public static final int AMMO_ADD_WINCHESTER = 6;

    public static final int EXP_OPEN_DOOR = 1 * 5;
    public static final int EXP_PICK_OBJECT = 5 * 5;
    public static final int EXP_KILL_MONSTER = 25 * 5;
    public static final int EXP_SECRET_FOUND = 50 * 5;
    public static final int EXP_END_LEVEL = 100 * 5;

    private GameConfig() {}
}
