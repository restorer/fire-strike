package zame.game.engine;

@SuppressWarnings("PointlessArithmeticExpression")
final class GameParams {
    static final int COLOR_SKY = 0x45a7f2;

    static final int MAX_MONSTER_TYPES = 5;

    static final int HEALTH_MAX = 100;
    static final int HEALTH_ADD_STIM = 10;
    static final int HEALTH_ADD_MEDI = 50;

    static final int HEALTH_BARREL = 25;

    static final int HEALTH_HIT_KNIFE = 1; // 2 hits per second -> 2 dmg per second
    static final int HEALTH_HIT_PISTOL = 2; // 2 hits per second -> 4 dmg per second
    static final int HEALTH_HIT_DBLPISTOL = 2; // 4 hits per second -> 8 dmg per second
    static final int HEALTH_HIT_AK47 = 6; // 2 hits per second -> 12 dmg per second
    static final int HEALTH_HIT_TMP = 5; // 4 hits per second -> 20 dmg per second
    static final int HEALTH_HIT_WINCHESTER = 32; // 1 hit per second -> 32 dmg per second
    static final int HEALTH_HIT_GRENADE = 52; // 1 hit per second -> 52 dmg per second
    static final int HEALTH_HIT_BARREL = 40; // бочка мочит и ГГ и врагов, так что это некое такое оптимальное значение

    static final double ARMOR_HEALTH_SAVER = 0.25; // if hero has armor, than it take only (hits * saver) damage
    static final double ARMOR_HIT_TAKER = 0.75; // if hero has armor, than it loose (hits * taker) armor

    // 1 frame = 1s / Engine.FRAMES_PER_SECOND = 1s / 40 = 0.025s
    // 1s = 40 frames

    static final int STUN_KNIFE = 5; // 2 hits per second -> 5 * 2 / 40 -> 0.25 stn per second
    static final int STUN_PISTOL = 5; // 2 hits per second -> 5 * 2 / 40 -> 0.25 stn per second
    static final int STUN_DBLPISTOL = 5; // 4 hits per second -> 5 * 4 / 40 -> 0.5 stn per second
    static final int STUN_AK47 = 5; // 2 hits per second -> 5 * 2 / 40 -> 0.25 stn per second
    static final int STUN_TMP = 5; // 4 hits per second -> 5 * 4 / 40 -> 0.5 stn per second
    static final int STUN_WINCHESTER = 10; // 1 hit per second -> 10 / 40 -> 0.25 stn per second
    static final int STUN_GRENADE = 30; // 1 hit per second -> 25 / 40 -> 0.75 stn per second

    static final int ARMOR_MAX = 200;
    static final int ARMOR_ADD_GREEN = 100;
    static final int ARMOR_ADD_RED = 200;

    static final int AMMO_MAX_CLIP = 150;
    static final int AMMO_MAX_SHELL = 75;
    static final int AMMO_MAX_GRENADE = 50;

    static final int AMMO_ENSURED_CLIP = 50;
    static final int AMMO_ENSURED_SHELL = 25;
    static final int AMMO_ENSURED_GRENADE = 5;

    static final int AMMO_ADD_CLIP = 5;
    static final int AMMO_ADD_CBOX = 20;
    static final int AMMO_ADD_SHELL = 5;
    static final int AMMO_ADD_SBOX = 15;
    static final int AMMO_ADD_GRENADE = 1;
    static final int AMMO_ADD_GBOX = 5;

    static final int AMMO_ADD_DBLPIST = 20;
    static final int AMMO_ADD_AK47 = 30;
    static final int AMMO_ADD_TMP = 3;
    static final int AMMO_ADD_WINCHESTER = 6;

    static final int EXP_OPEN_DOOR = 1 * 5;
    static final int EXP_PICK_OBJECT = 5 * 5;
    static final int EXP_KILL_MONSTER = 25 * 5;
    static final int EXP_SECRET_FOUND = 50 * 5;
    static final int EXP_END_LEVEL = 100 * 5;

    private GameParams() {}
}
