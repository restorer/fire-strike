package zame.game.engine.visual;

import zame.game.engine.Engine;
import zame.game.engine.EngineObject;
import zame.game.engine.entity.Bullet;
import zame.game.engine.entity.OnChangeWeaponAction;
import zame.game.engine.graphics.Renderer;
import zame.game.engine.graphics.TextureLoader;
import zame.game.engine.state.State;
import zame.game.engine.util.GameMath;
import zame.game.feature.sound.SoundManager;
import zame.game.flavour.config.GameConfig;

public class Weapons implements EngineObject {
    public static class WeaponParams {
        int[] cycle;
        int needAmmo;
        public int hits;
        int stunTimeout; // на сколько времени "стопорится" враг после выстрела
        int textureBase;
        float multX;
        float offX;
        float hgt;
        int soundIdx;
        boolean isMelee;
        int noHitSoundIdx;
        int hitsPerSecond;

        public int ammoIdx;
        public String name;
        public String description;

        WeaponParams(
                int[] cycle,
                int ammoIdx,
                int needAmmo,
                int hits,
                int stunTimeout,
                int textureBase,
                float multX,
                float offX,
                float hgt,
                int soundIdx,
                boolean isMelee,
                int noHitSoundIdx,
                int hitsPerSecond,
                String name) {

            this.cycle = cycle;
            this.ammoIdx = ammoIdx;
            this.needAmmo = needAmmo;
            this.hits = hits;
            this.stunTimeout = stunTimeout;
            this.textureBase = textureBase;
            this.multX = multX;
            this.offX = offX;
            this.hgt = hgt;
            this.soundIdx = soundIdx;
            this.isMelee = isMelee;
            this.noHitSoundIdx = noHitSoundIdx;
            this.hitsPerSecond = hitsPerSecond;
            this.name = name;

            makeDescription();
        }

        void makeDescription() {
            StringBuilder sb = new StringBuilder(name);

            if (isMelee) {
                sb.append(" / MELEE");
            }

            int damage = 0;

            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < cycle.length; i++) {
                if (cycle[i] < 0) {
                    damage += hits;
                }
            }

            sb.append(" / DPS ");
            sb.append(hitsPerSecond * damage);
            sb.append(" / STUN ");
            sb.append(stunTimeout);

            if (needAmmo > 0) {
                sb.append(" / AMMO ");
                sb.append(needAmmo);
            }

            description = sb.toString();
        }
    }

    public static final int AMMO_CLIP = 0;
    public static final int AMMO_SHELL = 1;
    public static final int AMMO_GRENADE = 2;
    public static final int AMMO_LAST = 3;

    public static final int[] AMMO_OBJ_TEX_MAP = { TextureLoader.OBJ_CLIP,
            TextureLoader.OBJ_SHELL,
            TextureLoader.OBJ_GRENADE, };

    public static final int WEAPON_KNIFE = 0; // required to be 0
    public static final int WEAPON_PISTOL = 1; // AMMO_CLIP
    public static final int WEAPON_DBLPISTOL = 2; // AMMO_CLIP
    public static final int WEAPON_AK47 = 3; // AMMO_SHELL
    public static final int WEAPON_TMP = 4; // AMMO_SHELL
    public static final int WEAPON_WINCHESTER = 5; // AMMO_SHELL
    public static final int WEAPON_GRENADE = 6; // AMMO_GRENADE
    public static final int WEAPON_LAST = 7;

    private static final float WALK_OFFSET_X = 1.0f / 8.0f;
    private static final float DEFAULT_HEIGHT = 1.5f;

    // 1 frame = 1s / Engine.FRAMES_PER_SECOND = 1s / 40 = 0.025s
    // 1s = 40 frames

    // @formatter:off
	public static final WeaponParams[] WEAPONS = {
		// WEAPON_KNIFE (2 hits per second) -- must be first
		new WeaponParams(
			new int[] {
				0,
                3, 3, 3,
                2, 2, 2,
                -1, 1, 1,
                3, 3, 3,
                0, 0, 0, 0, 0, 0, 0
			},
			-1, 0,
			GameConfig.HEALTH_HIT_KNIFE, GameConfig.STUN_KNIFE,
			Renderer.TEXTURE_KNIFE, 1.0f, 0.0f, DEFAULT_HEIGHT,
			SoundManager.SOUND_SHOOT_KNIFE, true, SoundManager.SOUND_NO_WAY,
			2, "KNIFE"
		),
		// WEAPON_PISTOL (2 hits per second)
		new WeaponParams(
			new int[] {
				0,
				-1, 1, 1, 1, 1,
				2, 2, 2, 2, 2,
				3, 3, 3, 3, 3,
				0, 0, 0, 0
			},
			AMMO_CLIP, 1,
			GameConfig.HEALTH_HIT_PISTOL, GameConfig.STUN_PISTOL,
			Renderer.TEXTURE_PISTOL, 1.0f, 0.0f, DEFAULT_HEIGHT,
			SoundManager.SOUND_SHOOT_PISTOL, false, 0,
			2, "PISTOL"
		),
		// WEAPON_DBLPISTOL (4 hits per second = 2 cycles per second)
		new WeaponParams(
			new int[] {
				0,
				-1, 1, 1, 1, 1,
				-2, 2, 2, 2, 2,
				3, 3, 3, 3, 3,
				0, 0, 0, 0
			},
			AMMO_CLIP, 1,
			GameConfig.HEALTH_HIT_DBLPISTOL, GameConfig.STUN_DBLPISTOL,
			Renderer.TEXTURE_DBLPISTOL, 1.0f, 0.0f, DEFAULT_HEIGHT,
			SoundManager.SOUND_SHOOT_DBLPISTOL, false, 0,
			4, "DOUBLE PISTOL"
		),
		// WEAPON_AK47 (4 hits per second)
		new WeaponParams(
			new int[] {
				0,
				-1, 1,
				2, 2,
				3, 3,
				0, 0, 0
			},
			AMMO_SHELL, 1,
			GameConfig.HEALTH_HIT_AK47, GameConfig.STUN_AK47,
			Renderer.TEXTURE_AK47, 1.0f, 0.0f, DEFAULT_HEIGHT,
			SoundManager.SOUND_SHOOT_AK47, false, 0,
			4, "AK-47"
		),
		// WEAPON_TMP (5 hits per second)
		new WeaponParams(
			new int[] {
				0,
				-1, 2, 3,
                0,
                1, 2, 3
			},
			AMMO_SHELL, 1,
			GameConfig.HEALTH_HIT_TMP, GameConfig.STUN_TMP,
			Renderer.TEXTURE_TMP, 1.0f, 0.0f, DEFAULT_HEIGHT,
			SoundManager.SOUND_SHOOT_TMP, false, 0,
			5, "TMP"
		),
		// WEAPON_WINCHESTER (1 hits per second)
		new WeaponParams(
			new int[] {
				0, 0, 0, 0, 0,
				1, 1, 1, 1, 1,
				-2, 2, 2, 2, 2,
				3, 3, 3, 3, 3,
				4, 4, 4, 4, 4,
				0, 0, 0, 0, 0,
				0, 0, 0, 0, 0,
				0, 0, 0, 0, 0,
			},
			AMMO_SHELL, 2,
			GameConfig.HEALTH_HIT_WINCHESTER, GameConfig.STUN_WINCHESTER,
			Renderer.TEXTURE_SHTG, 1.0f, 0.0f, DEFAULT_HEIGHT,
			SoundManager.SOUND_SHOOT_WINCHESTER, false, 0,
			1, "WINCHESTER"
		),
		// WEAPON_GRENADE (1 hit per second)
		new WeaponParams(
			new int[] {
				0,
				1, 1, 1, 1,
				2, 2, 2, 2,
				3, 3, 3, 3,
				4, 4, 4, 4,
				5, 5, 5, 5,
				6, 6, 6, 6,
				-7, 7, 7, 7,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
			},
			AMMO_GRENADE, 1,
			GameConfig.HEALTH_HIT_GRENADE, GameConfig.STUN_GRENADE,
			Renderer.TEXTURE_GRENADE,
            1.0f + WALK_OFFSET_X, // multX
            WALK_OFFSET_X, // offX
            DEFAULT_HEIGHT * (1.0f + WALK_OFFSET_X), // hgt
			SoundManager.SOUND_SHOOT_GRENADE, false, 0,
			1, "GRENADE"
		),
	};
    // @formatter:on

    private Engine engine;
    private State state;
    private Renderer renderer;
    WeaponParams currentParams;
    private int[] currentCycle;
    private int shootCycle;
    private int changeWeaponNext;
    private long changeWeaponTime;
    private int changeWeaponDir;

    @Override
    public void onCreate(Engine engine) {
        this.engine = engine;
        this.state = engine.state;
        this.renderer = engine.renderer;
    }

    public void reload() {
        changeWeaponDir = 0;
        setHeroWeaponImmediate(state.heroWeapon); // update params after reload
    }

    public void setHeroWeaponImmediate(int weaponIdx) {
        int prevHeroWeapon = state.heroWeapon;
        state.heroWeapon = weaponIdx;

        currentParams = WEAPONS[weaponIdx];
        currentCycle = currentParams.cycle;
        shootCycle = 0;

        int lastWeaponsLen = state.lastWeapons.length;

        for (int i = 0; i < lastWeaponsLen; i++) {
            if (state.lastWeapons[i] == weaponIdx) {
                return;
            }
        }

        for (int i = 0; i < lastWeaponsLen; i++) {
            state.lastWeaponIdx = (state.lastWeaponIdx + 1) % lastWeaponsLen;

            if (state.lastWeapons[state.lastWeaponIdx] < 0) {
                state.lastWeapons[state.lastWeaponIdx] = weaponIdx;
                return;
            }
        }

        for (int i = 0; i < lastWeaponsLen; i++) {
            state.lastWeaponIdx = (state.lastWeaponIdx + 1) % lastWeaponsLen;

            if (state.lastWeapons[state.lastWeaponIdx] != prevHeroWeapon) {
                state.lastWeapons[state.lastWeaponIdx] = weaponIdx;
                return;
            }
        }
    }

    public void switchWeapon(int weaponIdx) {
        if (state.heroWeapon == weaponIdx) {
            return;
        }

        engine.interacted = true;

        changeWeaponNext = weaponIdx;
        changeWeaponTime = engine.elapsedTime;
        changeWeaponDir = -1;

        for (OnChangeWeaponAction onChangeWeaponAction = state.onChangeWeaponActions.first();
                onChangeWeaponAction != null; ) {

            OnChangeWeaponAction nextOnChangeWeaponAction = onChangeWeaponAction.next;
            engine.level.executeActions(onChangeWeaponAction.markId);
            state.onChangeWeaponActions.release(onChangeWeaponAction);

            onChangeWeaponAction = nextOnChangeWeaponAction;
        }
    }

    public boolean hasNoAmmo(int weaponIdx) {
        int ammoIdx = WEAPONS[weaponIdx].ammoIdx;
        return (ammoIdx >= 0 && state.heroAmmo[ammoIdx] < WEAPONS[weaponIdx].needAmmo);
    }

    public boolean canSwitch(int weaponIdx) {
        return (state.heroHasWeapon[weaponIdx] && !hasNoAmmo(weaponIdx));
    }

    public void update(boolean canShoot) {
        int tex = currentCycle[shootCycle];

        // "canSwitch" just for case
        if (canShoot && tex < 0 && canSwitch(state.heroWeapon)) {
            //noinspection BooleanVariableAlwaysNegated
            boolean hitOrShoot = Bullet.shootOrPunch(
                    state,
                    state.heroX,
                    state.heroY,
                    engine.heroAr,
                    null,
                    currentParams.ammoIdx,
                    currentParams.hits,
                    currentParams.stunTimeout);

            if (tex > -1000) {
                engine.soundManager.playSound((currentParams.noHitSoundIdx != 0 && !hitOrShoot)
                        ? currentParams.noHitSoundIdx
                        : currentParams.soundIdx);
            }

            if (currentParams.ammoIdx >= 0) {
                state.heroAmmo[currentParams.ammoIdx] -= currentParams.needAmmo;

                if (state.heroAmmo[currentParams.ammoIdx] < currentParams.needAmmo) {
                    if (state.heroAmmo[currentParams.ammoIdx] < 0) {
                        state.heroAmmo[currentParams.ammoIdx] = 0;
                    }

                    selectBestWeapon(-1);
                }
            }
        }

        if (shootCycle > 0) {
            shootCycle = (shootCycle + 1) % currentCycle.length;
        }
    }

    public void fire() {
        if (shootCycle == 0 && changeWeaponDir == 0) {
            shootCycle++;
        }
    }

    public boolean switchToNextWeapon() {
        if (shootCycle != 0 || changeWeaponDir != 0) {
            return false;
        }

        int resWeapon = (state.heroWeapon + 1) % WEAPON_LAST;

        while (resWeapon != 0 && !canSwitch(resWeapon)) {
            resWeapon = (resWeapon + 1) % WEAPON_LAST;
        }

        switchWeapon(resWeapon);
        return true;
    }

    public int getBestWeapon(int desiredAmmo) {
        // At first try to find beat weapon with desired ammo (but grenade is never desired)
        if (desiredAmmo >= 0 && desiredAmmo != AMMO_GRENADE) {
            for (int i = WEAPON_LAST - 1; i > 0; i--) {
                if (canSwitch(i) && !WEAPONS[i].isMelee && WEAPONS[i].ammoIdx == desiredAmmo) {
                    return i;
                }
            }
        }

        // At second try find anything except grenade or melee weapon
        for (int i = WEAPON_LAST - 1; i > 0; i--) {
            if (canSwitch(i) && !WEAPONS[i].isMelee && WEAPONS[i].ammoIdx != AMMO_GRENADE) {
                return i;
            }
        }

        // At last try to find anything appropriate
        for (int i = WEAPON_LAST - 1; i > 0; i--) {
            if (canSwitch(i)) {
                return i;
            }
        }

        // Use knife as last resort
        return 0;
    }

    public void selectBestWeapon(int desiredAmmo) {
        int bestWeapon = getBestWeapon(desiredAmmo);

        if (bestWeapon == state.heroWeapon) {
            return;
        }

        if (desiredAmmo < 0) {
            switchWeapon(bestWeapon);
            return;
        }

        if (WEAPONS[bestWeapon].ammoIdx == WEAPONS[state.heroWeapon].ammoIdx) {
            return;
        }

        if (bestWeapon > state.heroWeapon) {
            switchWeapon(bestWeapon);
        }
    }

    @SuppressWarnings("MagicNumber")
    public void render(long walkTime) {
        float offY = 0;

        if (changeWeaponDir < 0) {
            offY = (float)(engine.elapsedTime - changeWeaponTime) / 150.0f;

            if (offY >= currentParams.hgt + 0.1f) {
                setHeroWeaponImmediate(changeWeaponNext);
                changeWeaponDir = 1;
                changeWeaponTime = engine.elapsedTime;
            }
        } else if (changeWeaponDir > 0) {
            offY = currentParams.hgt + 0.1f - (float)(engine.elapsedTime - changeWeaponTime) / 150.0f;

            if (offY <= 0.0f) {
                offY = 0.0f;
                changeWeaponDir = 0;
            }
        }

        float offX = (float)Math.sin((float)walkTime / 150.0f) * WALK_OFFSET_X;
        float lfX = -currentParams.multX + currentParams.offX + offX;
        float rtX = currentParams.multX + currentParams.offX + offX;

        offY += Math.abs((float)Math.sin((float)walkTime / 150.0f + GameMath.PI_F / 2.0f)) * 0.1f + 0.05f;

        renderer.startBatch();
        renderer.setColorQuadRGBA(1.0f, 1.0f, 1.0f, 1.0f);
        renderer.setCoordsQuadRectFlat(lfX, -offY, rtX, currentParams.hgt - offY);
        renderer.setTexRect(0, 1 << 16, 1 << 16, 0);
        renderer.batchQuad();

        // just for case
        if (shootCycle > currentCycle.length) {
            shootCycle = 0;
        }

        int weaponTexture = currentCycle[shootCycle];

        if (weaponTexture < -1000) {
            weaponTexture = -1000 - weaponTexture;
        } else if (weaponTexture < 0) {
            weaponTexture = -weaponTexture;
        }

        renderer.useOrtho(-1.0f, 1.0f, 0.0f, 2.0f, 0.0f, 1.0f);
        renderer.renderBatch(Renderer.FLAG_BLEND, currentParams.textureBase + weaponTexture);
    }
}
