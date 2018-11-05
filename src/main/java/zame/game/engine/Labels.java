package zame.game.engine;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.opengl.GLUtils;
import java.util.HashMap;
import javax.microedition.khronos.opengles.GL10;
import zame.game.Common;
import zame.game.App;
import zame.game.R;
import zame.game.store.Achievements;

public class Labels implements EngineObject {
	private static final int TEX_WIDTH = 1024;
	private static final int TEX_HEIGHT = 1024;
	private static final int TEX_WIDTH_LOW = 512;
	private static final int TEX_HEIGHT_LOW = 512;

	@SuppressWarnings("WeakerAccess") public static final int ALIGN_BL = 0; // bottom left
	public static final int ALIGN_CC = 1; // center center
	public static final int ALIGN_CL = 2; // center left
	public static final int ALIGN_CR = 3; // center left

	@SuppressWarnings("WeakerAccess") public static final int LABEL_FPS = 0;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_CANT_OPEN = 1;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_NEED_BLUE_KEY = 2;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_NEED_RED_KEY = 3;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_NEED_GREEN_KEY = 4;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_SECRET_FOUND = 5;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_ENDL_KILLS = 6;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_ENDL_ITEMS = 7;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_ENDL_SECRETS = 8;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_ENDL_TIME = 9;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_GAMEOVER = 10;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_GAMEOVER_LOAD_AUTOSAVE = 11;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_ACHIEVEMENT_UNLOCKED = 12;

	public static final int LABEL_HELP_MOVE = 13;
	public static final int LABEL_HELP_ROTATE = 14;
	public static final int LABEL_HELP_FIRE = 15;
	public static final int LABEL_HELP_WEAPONS = 16;
	public static final int LABEL_HELP_QUICK_WEAPONS = 17;
	public static final int LABEL_HELP_MINIMAP = 18;
	public static final int LABEL_HELP_STATS_HEALTH = 19;
	public static final int LABEL_HELP_STATS_AMMO = 20;
	public static final int LABEL_HELP_STATS_ARMOR = 21;
	public static final int LABEL_HELP_STATS_KEYS = 22;
	public static final int LABEL_HELP_DO_NOT_ROTATE = 23;

    @SuppressWarnings("WeakerAccess") public static final int LABEL_MESSAGE_USE_MOVE_PAD_TO_FOLLOW_ARROWS = 24;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_MESSAGE_DOING_WELL = 25;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_MESSAGE_SLIDE_TO_ROTATE_TO_LEFT = 26;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_MESSAGE_THAN_ROTATE_TO_RIGHT = 27;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_MESSAGE_MOVE_AND_ROTATE_TO_FOLLOW_ARROWS = 28;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_MESSAGE_FINE = 29;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_MESSAGE_USE_MINIMAP_TO_RETURN_TO_START = 30;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_MESSAGE_WELL_DONE = 31;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_MESSAGE_TO_OPEN_DOOR_GO_THROUGH_IT = 32;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_MESSAGE_NEXT_DOOR_IS_CLOSED_USE_SWITCH = 33;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_MESSAGE_TO_ACTIVATE_SWITCH_GO_UP_TO_HIM = 34;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_MESSAGE_THIS_IS_WINDOW = 35;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_MESSAGE_TO_OPEN_DOOR_PICKUP_KEY = 36;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_MESSAGE_USE_FIRE_AND_KILL_ENEMY = 37;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_MESSAGE_PICKUP_MEDI = 38;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_MESSAGE_OPEN_WEAPON_MENU_AND_SELECT_PISTOL = 39;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_MESSAGE_3 = 40;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_MESSAGE_2 = 41;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_MESSAGE_1 = 42;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_MESSAGE_PICKUP_AMMO = 43;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_MESSAGE_QUICK_CHANGE_WEAPON = 44;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_MESSAGE_AFTER_QUICK_CHANGE_WEAPON = 45;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_MESSAGE_PICKUP_ARMOR = 46;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_MESSAGE_AFTER_PICKUP_ARMOR = 47;
	@SuppressWarnings("WeakerAccess") public static final int LABEL_MESSAGE_PRESS_END_LEVEL = 48;

	@SuppressWarnings("WeakerAccess") public static final int LABEL_LAST = 49;

	public String[] map = new String[LABEL_LAST];

	private Engine engine;
	private Renderer renderer;
	private TextureLoader textureLoader;
	private Paint paint;
	private HashMap<Character, Rect> charMap = new HashMap<>();
	@SuppressWarnings("MagicNumber") private Rect[] numberMap = new Rect[11];
	private int lastTexX;
	private int lastTexY;
	private int textAscent;
	private int textHeight;
	private int spaceWidth;

	@Override
	public void setEngine(Engine engine) {
		this.engine = engine;
		this.renderer = engine.renderer;
		this.textureLoader = engine.textureLoader;
	}

	@SuppressWarnings("MagicNumber")
	public void init() {
		Typeface typeface = Common.loadIngameTypeface();

		if (typeface == null) {
			typeface = Typeface.DEFAULT;
		}

		paint = new Paint();
		paint.setTypeface(typeface);
		paint.setAntiAlias(true);
		paint.setARGB(0xFF, 0xFF, 0xFF, 0xFF);
		paint.setTextSize(64);
	}

	void createLabels(GL10 gl) {
		Bitmap bitmap = Common.createBitmap(TEX_WIDTH, TEX_HEIGHT, TEX_WIDTH_LOW, TEX_HEIGHT_LOW, "Can't alloc bitmap for labels");
		Canvas canvas = new Canvas(bitmap);
		canvas.setDensity(Bitmap.DENSITY_NONE);

		App context = App.self;
		charMap.clear();
		lastTexX = 0;
		lastTexY = 0;

		textAscent = (int)Math.ceil(-paint.ascent()); // Paint.ascent is negative, so negate it
		textHeight = textAscent + (int)Math.ceil(paint.descent());
		spaceWidth = (int)Math.ceil(paint.measureText(" "));

		appendChars(canvas, "0123456789-");

		map[LABEL_FPS] = appendChars(canvas, context.getString(R.string.lbl_fps));
		map[LABEL_CANT_OPEN] = appendChars(canvas, context.getString(R.string.lbl_cant_open_door));
		map[LABEL_NEED_BLUE_KEY] = appendChars(canvas, context.getString(R.string.lbl_need_blue_key));
		map[LABEL_NEED_RED_KEY] = appendChars(canvas, context.getString(R.string.lbl_need_red_key));
		map[LABEL_NEED_GREEN_KEY] = appendChars(canvas, context.getString(R.string.lbl_need_green_key));
		map[LABEL_SECRET_FOUND] = appendChars(canvas, context.getString(R.string.lbl_secret_found));
		map[LABEL_ENDL_KILLS] = appendChars(canvas, context.getString(R.string.lbl_endl_kills));
		map[LABEL_ENDL_ITEMS] = appendChars(canvas, context.getString(R.string.lbl_endl_items));
		map[LABEL_ENDL_SECRETS] = appendChars(canvas, context.getString(R.string.lbl_endl_secrets));
		map[LABEL_ENDL_TIME] = appendChars(canvas, context.getString(R.string.lbl_endl_time));
		map[LABEL_GAMEOVER] = appendChars(canvas, context.getString(R.string.lbl_gameover));
		map[LABEL_GAMEOVER_LOAD_AUTOSAVE] = appendChars(canvas, context.getString(R.string.lbl_gameover_load_autosave));
		map[LABEL_ACHIEVEMENT_UNLOCKED] = appendChars(canvas, context.getString(R.string.ac_unlocked));

        map[LABEL_HELP_MOVE] = appendChars(canvas, context.getString(R.string.lblh_move));
        map[LABEL_HELP_ROTATE] = appendChars(canvas, context.getString(R.string.lblh_rotate));
        map[LABEL_HELP_FIRE] = appendChars(canvas, context.getString(R.string.lblh_fire));
        map[LABEL_HELP_WEAPONS] = appendChars(canvas, context.getString(R.string.lblh_weapons));
        map[LABEL_HELP_QUICK_WEAPONS] = appendChars(canvas, context.getString(R.string.lblh_quick_weapons));
        map[LABEL_HELP_MINIMAP] = appendChars(canvas, context.getString(R.string.lblh_minimap));
        map[LABEL_HELP_STATS_HEALTH] = appendChars(canvas, context.getString(R.string.lblh_stats_health));
        map[LABEL_HELP_STATS_AMMO] = appendChars(canvas, context.getString(R.string.lblh_stats_ammo));
        map[LABEL_HELP_STATS_ARMOR] = appendChars(canvas, context.getString(R.string.lblh_stats_armor));
        map[LABEL_HELP_STATS_KEYS] = appendChars(canvas, context.getString(R.string.lblh_stats_keys));
        map[LABEL_HELP_DO_NOT_ROTATE] = appendChars(canvas, context.getString(R.string.lblh_do_not_rotate));

        map[LABEL_MESSAGE_USE_MOVE_PAD_TO_FOLLOW_ARROWS] = appendChars(canvas, context.getString(R.string.lblm_use_move_pad_to_follow_arrows));
        map[LABEL_MESSAGE_DOING_WELL] = appendChars(canvas, context.getString(R.string.lblm_doing_well));
        map[LABEL_MESSAGE_SLIDE_TO_ROTATE_TO_LEFT] = appendChars(canvas, context.getString(R.string.lblm_slide_to_rotate_to_left));
        map[LABEL_MESSAGE_THAN_ROTATE_TO_RIGHT] = appendChars(canvas, context.getString(R.string.lblm_than_rotate_to_right));
        map[LABEL_MESSAGE_MOVE_AND_ROTATE_TO_FOLLOW_ARROWS] = appendChars(canvas, context.getString(R.string.lblm_move_and_rotate_to_follow_arrows));
        map[LABEL_MESSAGE_FINE] = appendChars(canvas, context.getString(R.string.lblm_fine));
        map[LABEL_MESSAGE_USE_MINIMAP_TO_RETURN_TO_START] = appendChars(canvas, context.getString(R.string.lblm_use_minimap_to_return_to_start));
        map[LABEL_MESSAGE_WELL_DONE] = appendChars(canvas, context.getString(R.string.lblm_well_done));
        map[LABEL_MESSAGE_TO_OPEN_DOOR_GO_THROUGH_IT] = appendChars(canvas, context.getString(R.string.lblm_to_open_door_go_through_it));
        map[LABEL_MESSAGE_NEXT_DOOR_IS_CLOSED_USE_SWITCH] = appendChars(canvas, context.getString(R.string.lblm_next_door_is_closed_use_switch));
        map[LABEL_MESSAGE_TO_ACTIVATE_SWITCH_GO_UP_TO_HIM] = appendChars(canvas, context.getString(R.string.lblm_to_activate_switch_go_up_to_him));
        map[LABEL_MESSAGE_THIS_IS_WINDOW] = appendChars(canvas, context.getString(R.string.lblm_this_is_window));
        map[LABEL_MESSAGE_TO_OPEN_DOOR_PICKUP_KEY] = appendChars(canvas, context.getString(R.string.lblm_to_open_door_pickup_key));
        map[LABEL_MESSAGE_USE_FIRE_AND_KILL_ENEMY] = appendChars(canvas, context.getString(R.string.lblm_use_fire_and_kill_enemy));
        map[LABEL_MESSAGE_PICKUP_MEDI] = appendChars(canvas, context.getString(R.string.lblm_pickup_medi));
        map[LABEL_MESSAGE_OPEN_WEAPON_MENU_AND_SELECT_PISTOL] = appendChars(canvas, context.getString(R.string.lblm_open_weapon_menu_and_select_pistol));
        map[LABEL_MESSAGE_3] = appendChars(canvas, context.getString(R.string.lblm_3));
        map[LABEL_MESSAGE_2] = appendChars(canvas, context.getString(R.string.lblm_2));
        map[LABEL_MESSAGE_1] = appendChars(canvas, context.getString(R.string.lblm_1));
        map[LABEL_MESSAGE_PICKUP_AMMO] = appendChars(canvas, context.getString(R.string.lblm_pickup_ammo));
        map[LABEL_MESSAGE_QUICK_CHANGE_WEAPON] = appendChars(canvas, context.getString(R.string.lblm_quick_change_weapon));
        map[LABEL_MESSAGE_AFTER_QUICK_CHANGE_WEAPON] = appendChars(canvas, context.getString(R.string.lblm_after_quick_change_weapon));
        map[LABEL_MESSAGE_PICKUP_ARMOR] = appendChars(canvas, context.getString(R.string.lblm_pickup_armor));
        map[LABEL_MESSAGE_AFTER_PICKUP_ARMOR] = appendChars(canvas, context.getString(R.string.lblm_after_pickup_armor));
        map[LABEL_MESSAGE_PRESS_END_LEVEL] = appendChars(canvas, context.getString(R.string.lblm_press_end_level));

		for (int i = 0, len = Achievements.LIST.length; i < len; i++) {
			appendChars(canvas, Achievements.cleanupTitle(context.getString(Achievements.LIST[i].titleResourceId)));
		}

		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureLoader.textures[TextureLoader.TEXTURE_LABELS]);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

		//noinspection UnusedAssignment
		canvas = null;

		bitmap.recycle();

		//noinspection UnusedAssignment
		bitmap = null;

		System.gc();
	}

	private String appendChars(Canvas canvas, String str) {
		for (int i = 0, len = str.length(); i < len; i++) {
			char ch = str.charAt(i);

			if (ch != ' ' && !charMap.containsKey(ch)) {
				String chStr = String.valueOf(ch);
				int textWidth = (int)Math.ceil(paint.measureText(chStr));

				if ((lastTexX + textWidth + 1) >= TEX_WIDTH) {
					if ((lastTexY + textHeight + 1) >= TEX_HEIGHT) {
						Common.log("Labels.appendChars: no free texture space");
						continue;
					}

					lastTexX = 0;
					lastTexY += (textHeight + 2);
				}

				Rect rect = new Rect(lastTexX + 1, lastTexY + 1, lastTexX + textWidth + 1, lastTexY + textHeight + 1);
				canvas.drawText(chStr, (float)(lastTexX + 1), (float)(lastTexY + textAscent + 1), paint);
				lastTexX += textWidth + 2;

				charMap.put(ch, rect);

				if (ch == '-') {
					numberMap[10] = rect;
				} else if (ch >= '0' && ch <= '9') {
					numberMap[Character.digit(ch, 10)] = rect;
				}
			}
		}

		return str;
	}

	private float drawCharacter(Rect rect, float xpos, float ypos, float scale) {
		if (rect == null) {
			return (float)spaceWidth * scale;
		}

		//noinspection MagicNumber
		renderer.setQuadTexCoords(
			(rect.left << 16) / TEX_WIDTH,
			(rect.bottom << 16) / TEX_HEIGHT,
			(rect.right << 16) / TEX_WIDTH,
			(rect.top << 16) / TEX_HEIGHT
		);

		renderer.setQuadOrthoCoords(
			xpos,
			ypos,
			xpos + (float)(rect.width() - 1) * scale,
			ypos + (float)(textHeight - 1) * scale
		);

		renderer.drawQuad();
		return (float)rect.width() * scale;
	}

	void beginDrawing(GL10 gl) {
		beginDrawing(gl, false);
	}

	public void beginDrawing(GL10 gl, boolean customGlConfig) {
		if (!customGlConfig) {
			renderer.initOrtho(gl, true, true, -engine.ratio, engine.ratio, -1.0f, 1.0f, 0.0f, 1.0f);
			gl.glShadeModel(GL10.GL_FLAT);
			gl.glEnable(GL10.GL_BLEND);
			gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			gl.glDisable(GL10.GL_DEPTH_TEST);
		}

		renderer.init();
	}

	@SuppressWarnings("MagicNumber")
	public void draw(
		@SuppressWarnings("unused") GL10 gl,
		float sx, float sy, float ex, float ey,
		String str, float desiredHeight, int align
	) {
		float scale = desiredHeight / (float)textHeight;
		float width = (float)getWidth(str);

		if ((width * scale) > (ex - sx)) {
			scale = (ex - sx) / width;
		}

		if (align == ALIGN_CC || align == ALIGN_CL || align == ALIGN_CR) {
			sy += (ey - sy - (float)textHeight * scale) * 0.5f;
		}

		if (align == ALIGN_CC) {
			sx += (ex - sx - width * scale) * 0.5f;
		} else if (align == ALIGN_CR) {
			sx = ex - width * scale;
		}

		for (int i = 0, len = str.length(); i < len; i++) {
			char ch = str.charAt(i);
			sx += drawCharacter((ch == ' ' ? null : charMap.get(ch)), sx, sy, scale);
		}
	}

	@SuppressWarnings("MagicNumber")
	public void draw(
		@SuppressWarnings("unused") GL10 gl,
		float sx, float sy, float ex, float ey,
		int value, float desiredHeight, int align
	) {
		float scale = desiredHeight / (float)textHeight;
		float width = (float)getWidth(value);

		if ((width * scale) > (ex - sx)) {
			scale = (ex - sx) / width;
		}

		if (align == ALIGN_CC || align == ALIGN_CL || align == ALIGN_CR) {
			sy += (ey - sy - (float)textHeight * scale) * 0.5f;
		}

		if (align == ALIGN_CC) {
			sx += (ex - sx - width * scale) * 0.5f;
		} else if (align == ALIGN_CR) {
			sx = ex - width * scale;
		}

		if (value == 0) {
			drawCharacter(numberMap[0], sx, sy, scale);
			return;
		}

		if (value < 0) {
			sx += drawCharacter(numberMap[10], sx, sy, scale);
			value = -value;
		}

		int divider = 1;
		int tmpValue = value / 10;

		while (tmpValue > 0) {
			divider *= 10;
			tmpValue /= 10;
		}

		while (divider > 0) {
			sx += drawCharacter(numberMap[(value / divider) % 10], sx, sy, scale);
			divider /= 10;
		}
	}

	void endDrawing(GL10 gl) {
		endDrawing(gl, false);
	}

	public void endDrawing(GL10 gl, boolean customGlConfig) {
		renderer.bindTextureBlur(gl, textureLoader.textures[TextureLoader.TEXTURE_LABELS]);
		renderer.flush(gl);

		if (!customGlConfig) {
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glPopMatrix();

			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glPopMatrix();
		}
	}

	public float getScaledWidth(String str, @SuppressWarnings("SameParameterValue") float desiredHeight) {
		float scale = desiredHeight / (float)textHeight;
		float width = (float)getWidth(str);

		return width * scale;
	}

	private int getWidth(String str) {
		int result = 0;

		for (int i = 0, len = str.length(); i < len; i++) {
			char ch = str.charAt(i);

			if (ch == ' ') {
				result += spaceWidth;
			} else {
				Rect rect = charMap.get(ch);
				result += (rect == null ? spaceWidth : rect.width());
			}
		}

		return result;
	}

	@SuppressWarnings("unused")
	public float getScaledWidth(int value, float desiredHeight) {
		float scale = desiredHeight / (float)textHeight;
		float width = (float)getWidth(value);

		return width * scale;
	}

	private int getWidth(int value) {
		if (value == 0) {
			return numberMap[0].width();
		}

		int result = 0;

		if (value < 0) {
			result += numberMap[10].width();
			value = -value;
		}

		while (value > 0) {
			result += numberMap[value % 10].width();
			value /= 10;
		}

		return result;
	}
}
