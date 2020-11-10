package com.eightsines.firestrike.procedural.config;

using Safety;

@SuppressWarnings("checkstyle:MagicNumber")
class Config {
    public static var healthMax(get, never) : Int;
    public static var healthPickOne(get, never) : Int;
    public static var healthPickBox(get, never) : Int;

    public static var armorMax(get, never) : Int;
    public static var armorPickOne(get, never) : Int;
    public static var armorPickBox(get, never) : Int;
    public static var armorHitTaker(get, never) : Float;
    public static var armorHealthSaver(get, never) : Float;

    public static var ammo(get, never) : Array<AmmoConfig>;
    public static var enemies(get, never) : Array<EnemyConfig>;
    public static var weapons(get, never) : Array<WeaponConfig>;

    private static var __ammo : Null<Array<AmmoConfig>> = null;
    private static var __enemies : Null<Array<EnemyConfig>> = null;
    private static var __weapons : Null<Array<WeaponConfig>> = null;

    private static function get_healthMax() : Int {
        return 100;
    }

    private static function get_healthPickOne() : Int {
        return 10;
    }

    private static function get_healthPickBox() : Int {
        return 50;
    }

    public static function get_armorMax() : Int {
        return 200;
    }

    public static function get_armorPickOne() : Int {
        return 100;
    }

    public static function get_armorPickBox() : Int {
        return 200;
    }

    public static function get_armorHitTaker() : Float {
        return 0.75;
    }

    public static function get_armorHealthSaver() : Float {
        return 0.25;
    }

    private static function get_ammo() : Array<AmmoConfig> {
        if (__ammo == null) {
            __ammo = [
                new AmmoConfig(0, 0, 0, 0, false, true), // Knife
                new AmmoConfig(50, 150, 5, 20), // Clip (Pistol, Double pistol)
                new AmmoConfig(25, 75, 5, 15), // Shell (AK47, Tmp, Winchester)
                new AmmoConfig(5, 50, 1, 5, false), // Grenade
            ];
        }

        return __ammo.unsafe();
    }

    private static function get_enemies() : Array<EnemyConfig> {
        if (__enemies == null) {
            __enemies = [
                new EnemyConfig(0, 8, 5, 1), // Soldier with pistol (1)
                new EnemyConfig(1, 15, 7, 2), // Soldier with rifle (2)
                new EnemyConfig(2, 21, 18, 0), // Soldier with knife (3)
                new EnemyConfig(3, 33, 25, 3), // Soldier with grenade (4)
                new EnemyConfig(4, 49, 10, 2), // Zombie (5)
            ];
        }

        return __enemies.unsafe();
    }

    private static function get_weapons() : Array<WeaponConfig> {
        if (__weapons == null) {
            // NB. в игре dps у ножа = 6, но тут только 2, чтоб не давать генератору шанса сгенерировать сверх-сложные уровни
            // (вообще, нож мало должен влиять, но на всякий случай...)

            __weapons = [
                new WeaponConfig(0, 2, 2, 0.75), // Knife (1)
                new WeaponConfig(1, 4, 2, 0.25, 1), // Pistol (2)
                new WeaponConfig(1, 8, 4, 0.4, 2, Weapon_DblPist), // Double pistol (3)
                new WeaponConfig(2, 12, 4, 0.3, 1, Weapon_AK47), // AK47 (4)
                new WeaponConfig(2, 20, 5, 0.5, 1, Weapon_Tmp), // Tmp (5)
                new WeaponConfig(2, 32, 2, 0.5, 0, Weapon_Winchester), // Winchester (6)
                new WeaponConfig(3, 52, 1, 0.625), // Grenade (7)
            ];
        }

        return __weapons.unsafe();
    }
}
