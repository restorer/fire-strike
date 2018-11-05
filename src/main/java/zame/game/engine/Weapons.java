package zame.game.engine;

import javax.microedition.khronos.opengles.GL10;
import zame.game.managers.SoundManager;

public class Weapons implements EngineObject {
    public static class WeaponParams {
        int[] cycle;
        int needAmmo;
        int hits;
        int hitTimeout; // на сколько времени "стопорится" враг после выстрела
        int textureBase;
        float xmult;
        float xoff;
        float hgt;
        int soundIdx;
        boolean isNear;
        int noHitSoundIdx;
        int hitsPerSecond;

        public int ammoIdx;
        public String name;
        public String description;

        WeaponParams(int[] cycle,
                int ammoIdx,
                int needAmmo,
                int hits,
                int hitTimeout,
                int textureBase,
                float xmult,
                float xoff,
                float hgt,
                int soundIdx,
                boolean isNear,
                int noHitSoundIdx,
                int hitsPerSecond,
                String name) {

            this.cycle = cycle;
            this.ammoIdx = ammoIdx;
            this.needAmmo = needAmmo;
            this.hits = hits;
            this.hitTimeout = hitTimeout;
            this.textureBase = textureBase;
            this.xmult = xmult;
            this.xoff = xoff;
            this.hgt = hgt;
            this.soundIdx = soundIdx;
            this.isNear = isNear;
            this.noHitSoundIdx = noHitSoundIdx;
            this.hitsPerSecond = hitsPerSecond;
            this.name = name;

            makeDescription();
        }

        void makeDescription() {
            StringBuilder sb = new StringBuilder(name);

            if (isNear) {
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
            sb.append(hitTimeout);

            if (needAmmo > 0) {
                sb.append(" / AMMO ");
                sb.append(needAmmo);
            }

            description = sb.toString();
        }
    }

    static final int AMMO_CLIP = 0;
    static final int AMMO_SHELL = 1;
    static final int AMMO_GRENADE = 2;
    static final int AMMO_LAST = 3;

    static final int[] AMMO_OBJ_TEX_MAP = { TextureLoader.OBJ_CLIP,
            TextureLoader.OBJ_SHELL,
            TextureLoader.OBJ_GRENADE, };

    public static final int WEAPON_KNIFE = 0; // required to be 0
    public static final int WEAPON_PISTOL = 1; // AMMO_CLIP
    public static final int WEAPON_DBLPISTOL = 2; // AMMO_CLIP
    public static final int WEAPON_AK47 = 3; // AMMO_SHELL
    public static final int WEAPON_TMP = 4; // AMMO_SHELL
    public static final int WEAPON_WINCHESTER = 5; // AMMO_SHELL
    public static final int WEAPON_GRENADE = 6; // AMMO_GRENADE
    @SuppressWarnings("WeakerAccess") public static final int WEAPON_LAST = 7;

    private static final float WALK_OFFSET_X = 1.0f / 8.0f;
    private static final float DEFAULT_HEIGHT = 1.5f;

    // 1 frame = 1s / Engine.FRAMES_PER_SECOND = 1s / 40 = 0.025s
    // 1s = 40 frames

    // @formatter:off
	public static final WeaponParams[] WEAPONS = {
		// WEAPON_KNIFE (2 hits per second)
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
			GameParams.HEALTH_HIT_KNIFE, GameParams.STUN_KNIFE,
			TextureLoader.TEXTURE_KNIFE, 1.0f, 0.0f, DEFAULT_HEIGHT,
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
			GameParams.HEALTH_HIT_PISTOL, GameParams.STUN_PISTOL,
			TextureLoader.TEXTURE_PISTOL, 1.0f, 0.0f, DEFAULT_HEIGHT,
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
			GameParams.HEALTH_HIT_DBLPISTOL, GameParams.STUN_DBLPISTOL,
			TextureLoader.TEXTURE_DBLPISTOL, 1.0f, 0.0f, DEFAULT_HEIGHT,
			SoundManager.SOUND_SHOOT_DBLPISTOL, false, 0,
			4, "DOUBLE PISTOL"
		),
		// WEAPON_AK47 (2 hits per second)
		new WeaponParams(
			new int[] {
				0,
				-1, 1,
				2, 2,
				3, 3,
				0
			},
			AMMO_SHELL, 1,
			GameParams.HEALTH_HIT_AK47, GameParams.STUN_AK47,
			TextureLoader.TEXTURE_AK47, 1.0f, 0.0f, DEFAULT_HEIGHT,
			SoundManager.SOUND_SHOOT_AK47, false, 0,
			2, "AK-47"
		),
		// WEAPON_TMP (4 hits per second)
		new WeaponParams(
			new int[] {
				0,
				-1,
				2,
				3,
			},
			AMMO_SHELL, 1,
			GameParams.HEALTH_HIT_TMP, GameParams.STUN_TMP,
			TextureLoader.TEXTURE_TMP, 1.0f, 0.0f, DEFAULT_HEIGHT,
			SoundManager.SOUND_SHOOT_TMP, false, 0,
			4, "TMP"
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
			AMMO_SHELL, 1,
			GameParams.HEALTH_HIT_WINCHESTER, GameParams.STUN_WINCHESTER,
			TextureLoader.TEXTURE_SHTG, 1.0f, 0.0f, DEFAULT_HEIGHT,
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
				0, 0, 0,
			},
			AMMO_GRENADE, 1,
			GameParams.HEALTH_HIT_GRENADE, GameParams.STUN_GRENADE,
			TextureLoader.TEXTURE_GRENADE,
            1.0f + WALK_OFFSET_X, // xmult
            WALK_OFFSET_X, // xoff
            DEFAULT_HEIGHT * (1.0f + WALK_OFFSET_X), // hgt
			SoundManager.SOUND_SHOOT_GRENADE, false, 0,
			1, "GRENADE"
		),
	};
    // @formatter:on

    private Engine engine;
    private State state;
    private Renderer renderer;
    private TextureLoader textureLoader;
    private int changeWeaponNext;
    private long changeWeaponTime;

    public WeaponParams currentParams;

    int[] currentCycle;
    int shootCycle;
    int changeWeaponDir;

    @Override
    public void setEngine(Engine engine) {
        this.engine = engine;
        this.state = engine.state;
        this.renderer = engine.renderer;
        this.textureLoader = engine.textureLoader;
    }

    public void init() {
        shootCycle = 0;
        changeWeaponDir = 0;
        updateWeapon();
    }

    void updateWeapon() {
        currentParams = WEAPONS[state.heroWeapon];
        currentCycle = currentParams.cycle;
        shootCycle = 0;
    }

    public void switchWeapon(int weaponIdx) {
        changeWeaponNext = weaponIdx;
        changeWeaponTime = engine.elapsedTime;
        changeWeaponDir = -1;

        if (state.heroWeapon != weaponIdx) {
            for (OnChangeWeaponAction onChangeWeaponAction = state.onChangeWeaponActions.first();
                    onChangeWeaponAction != null; ) {

                OnChangeWeaponAction nextOnChangeWeaponAction = (OnChangeWeaponAction)onChangeWeaponAction.next;
                engine.level.executeActions(onChangeWeaponAction.markId);
                state.onChangeWeaponActions.release(onChangeWeaponAction);

                //noinspection AssignmentToForLoopParameter
                onChangeWeaponAction = nextOnChangeWeaponAction;
            }
        }

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

            if (state.lastWeapons[state.lastWeaponIdx] != state.heroWeapon) {
                state.lastWeapons[state.lastWeaponIdx] = weaponIdx;
                return;
            }
        }
    }

    boolean hasNoAmmo(int weaponIdx) {
        return ((WEAPONS[weaponIdx].ammoIdx >= 0) && (state.heroAmmo[WEAPONS[weaponIdx].ammoIdx]
                < WEAPONS[weaponIdx].needAmmo));
    }

    public boolean canSwitch(int weaponIdx) {
        return (state.heroHasWeapon[weaponIdx] && !hasNoAmmo(weaponIdx));
    }

    void nextWeapon() {
        int resWeapon = (state.heroWeapon + 1) % WEAPON_LAST;

        while ((resWeapon != 0) && (!state.heroHasWeapon[resWeapon] || hasNoAmmo(resWeapon))) {
            resWeapon = (resWeapon + 1) % WEAPON_LAST;
        }

        switchWeapon(resWeapon);
    }

    int getBestWeapon() {
        int resWeapon = WEAPON_LAST - 1;

        while (resWeapon > 0 && (!state.heroHasWeapon[resWeapon]
                || hasNoAmmo(resWeapon)
                || WEAPONS[resWeapon].isNear
                || WEAPONS[resWeapon].ammoIdx == AMMO_GRENADE)) {

            resWeapon--;
        }

        if (resWeapon == 0) {
            resWeapon = WEAPON_LAST - 1;

            while (resWeapon > 0 && (!state.heroHasWeapon[resWeapon]
                    || hasNoAmmo(resWeapon)
                    || !(WEAPONS[resWeapon].isNear || WEAPONS[resWeapon].ammoIdx == AMMO_GRENADE))) {

                resWeapon--;
            }
        }

        return resWeapon;
    }

    void selectBestWeapon() {
        int bestWeapon = getBestWeapon();

        if (bestWeapon != state.heroWeapon) {
            switchWeapon(bestWeapon);
        }
    }

    @SuppressWarnings("MagicNumber")
    public void render(GL10 gl, long walkTime) {
        renderer.r1 = 1.0f;
        renderer.g1 = 1.0f;
        renderer.b1 = 1.0f;
        renderer.a1 = 1.0f;
        renderer.r2 = 1.0f;
        renderer.g2 = 1.0f;
        renderer.b2 = 1.0f;
        renderer.a2 = 1.0f;
        renderer.r3 = 1.0f;
        renderer.g3 = 1.0f;
        renderer.b3 = 1.0f;
        renderer.a3 = 1.0f;
        renderer.r4 = 1.0f;
        renderer.g4 = 1.0f;
        renderer.b4 = 1.0f;
        renderer.a4 = 1.0f;

        renderer.z1 = 0.0f;
        renderer.z2 = 0.0f;
        renderer.z3 = 0.0f;
        renderer.z4 = 0.0f;

        gl.glDisable(GL10.GL_DEPTH_TEST);
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);

        renderer.initOrtho(gl, true, false, -1.0f, 1.0f, 0.0f, 2.0f, 0.0f, 1.0f);
        gl.glDisable(GL10.GL_ALPHA_TEST);

        renderer.init();

        float yoff = 0;

        if (changeWeaponDir == -1) {
            yoff = (float)(engine.elapsedTime - changeWeaponTime) / 150.0f;

            if (yoff >= currentParams.hgt + 0.1f) {
                state.heroWeapon = changeWeaponNext;
                updateWeapon();

                changeWeaponDir = 1;
                changeWeaponTime = engine.elapsedTime;
            }
        } else if (changeWeaponDir == 1) {
            yoff = currentParams.hgt + 0.1f - (float)(engine.elapsedTime - changeWeaponTime) / 150.0f;

            if (yoff <= 0.0f) {
                yoff = 0.0f;
                changeWeaponDir = 0;
            }
        }

        float xoff = (float)Math.sin((float)walkTime / 150.0f) * WALK_OFFSET_X;
        float xlt = -currentParams.xmult + currentParams.xoff + xoff;
        float xrt = currentParams.xmult + currentParams.xoff + xoff;

        yoff += Math.abs((float)Math.sin((float)walkTime / 150.0f + GameMath.PI_F / 2.0f)) * 0.1f + 0.05f;
        float hgt = currentParams.hgt - yoff;

        renderer.x1 = xlt;
        renderer.y1 = -yoff;
        renderer.x2 = xlt;
        renderer.y2 = hgt;
        renderer.x3 = xrt;
        renderer.y3 = hgt;
        renderer.x4 = xrt;
        renderer.y4 = -yoff;

        renderer.u1 = 0;
        renderer.v1 = 1 << 16;
        renderer.u2 = 0;
        renderer.v2 = 0;
        renderer.u3 = 1 << 16;
        renderer.v3 = 0;
        renderer.u4 = 1 << 16;
        renderer.v4 = 1 << 16;

        renderer.drawQuad();

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

        renderer.bindTextureCtl(gl, textureLoader.textures[currentParams.textureBase + weaponTexture]);
        renderer.flush(gl);

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glPopMatrix();

        gl.glDisable(GL10.GL_ALPHA_TEST);
    }
}
