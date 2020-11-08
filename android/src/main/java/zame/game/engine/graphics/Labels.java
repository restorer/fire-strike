package zame.game.engine.graphics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import java.util.HashMap;

import zame.game.R;
import zame.game.core.util.Common;
import zame.game.engine.Engine;
import zame.game.engine.EngineObject;
import zame.game.feature.achievements.Achievements;

public class Labels implements EngineObject {
    private static final int TEX_WIDTH = 1024;
    private static final int TEX_HEIGHT = 1024;
    private static final int TEX_WIDTH_LOW = 512;
    private static final int TEX_HEIGHT_LOW = 512;

    public static final int ALIGN_BL = 0; // bottom left
    public static final int ALIGN_CC = 1; // center center
    public static final int ALIGN_CL = 2; // center left
    public static final int ALIGN_CR = 3; // center left

    public static final int LABEL_FPS = 0;
    public static final int LABEL_CANT_OPEN = 1;
    public static final int LABEL_NEED_BLUE_KEY = 2;
    public static final int LABEL_NEED_RED_KEY = 3;
    public static final int LABEL_NEED_GREEN_KEY = 4;
    public static final int LABEL_SECRET_FOUND = 5;
    public static final int LABEL_ENDL_KILLS = 6;
    public static final int LABEL_ENDL_SECRETS = 7;
    public static final int LABEL_ENDL_TIME = 8;
    public static final int LABEL_GAMEOVER = 9;
    public static final int LABEL_GAMEOVER_SUBTITLE = 10;
    public static final int LABEL_GAMEOVER_SUBTITLE_LEFT_HAND_AIM = 11;
    public static final int LABEL_GAMEOVER_SUBTITLE_JUST_RESTART = 12;
    public static final int LABEL_ACHIEVEMENT_UNLOCKED = 13;

    public static final int LABEL_HELP_MOVE = 14;
    public static final int LABEL_HELP_ROTATE = 15;
    public static final int LABEL_HELP_FIRE = 16;
    public static final int LABEL_HELP_WEAPONS = 17;
    public static final int LABEL_HELP_QUICK_WEAPONS = 18;
    public static final int LABEL_HELP_MINIMAP = 19;
    public static final int LABEL_HELP_STATS_HEALTH = 20;
    public static final int LABEL_HELP_STATS_AMMO = 21;
    public static final int LABEL_HELP_STATS_ARMOR = 22;
    public static final int LABEL_HELP_STATS_KEYS = 23;
    public static final int LABEL_HELP_DO_NOT_ROTATE = 24;

    private static final int LABEL_MESSAGE_3 = 25;
    private static final int LABEL_MESSAGE_2 = 26;
    private static final int LABEL_MESSAGE_1 = 27;
    private static final int LABEL_MESSAGE_WELCOME_TO_TRAINING_AREA = 28;
    private static final int LABEL_MESSAGE_USE_MOVEMENT_PAD_TO_FOLLOW_ARROWS = 29;
    private static final int LABEL_MESSAGE_DOING_WELL = 30;
    private static final int LABEL_MESSAGE_SLIDE_TO_ROTATE = 31;
    private static final int LABEL_MESSAGE_MOVE_AND_ROTATE_TO_FOLLOW_ARROWS = 32;
    private static final int LABEL_MESSAGE_USE_MINIMAP_TO_FOLLOW_PATH = 33;
    private static final int LABEL_MESSAGE_TO_OPEN_DOOR_GO_THROUGH_IT = 34;
    private static final int LABEL_MESSAGE_NEXT_DOOR_IS_CLOSED_USE_SWITCH = 35;
    private static final int LABEL_MESSAGE_TO_ACTIVATE_SWITCH_GO_UP_TO_HIM = 36;
    private static final int LABEL_MESSAGE_THIS_IS_WINDOW = 37;
    private static final int LABEL_MESSAGE_TO_OPEN_DOOR_PICKUP_KEY = 38;
    private static final int LABEL_MESSAGE_PRESS_END_LEVEL = 39;
    private static final int LABEL_MESSAGE_USE_FIRE_AND_KILL_ENEMY = 40;
    private static final int LABEL_MESSAGE_PICKUP_MEDI = 41;
    private static final int LABEL_MESSAGE_OPEN_WEAPON_MENU_AND_SELECT_PISTOL = 42;
    private static final int LABEL_MESSAGE_PICKUP_AMMO = 43;
    private static final int LABEL_MESSAGE_QUICK_CHANGE_WEAPON = 44;
    private static final int LABEL_MESSAGE_AFTER_QUICK_CHANGE_WEAPON = 45;
    private static final int LABEL_MESSAGE_PICKUP_ARMOR = 46;
    private static final int LABEL_MESSAGE_AFTER_PICKUP_ARMOR = 47;

    public static final int LABEL_LAST = 48;

    public String[] map = new String[LABEL_LAST];

    private Engine engine;
    private Renderer renderer;
    private Paint paint;
    private final HashMap<Character, Rect> charMap = new HashMap<>();
    @SuppressWarnings({ "MagicNumber", "RedundantSuppression" }) private final Rect[] numberMap = new Rect[11];
    private int lastTexX;
    private int lastTexY;
    private int textAscent;
    private int textHeight;
    private int spaceWidth;

    @Override
    public void onCreate(Engine engine) {
        this.engine = engine;
        this.renderer = engine.renderer;
    }

    @SuppressWarnings("MagicNumber")
    public void reload() {
        Typeface typeface = Common.loadIngameTypeface(engine.activity);

        if (typeface == null) {
            typeface = Typeface.DEFAULT;
        }

        paint = new Paint();
        paint.setTypeface(typeface);
        paint.setAntiAlias(true);
        paint.setARGB(0xFF, 0xFF, 0xFF, 0xFF);
        paint.setTextSize(64);
    }

    public void createLabels() {
        Bitmap bitmap = Common.createBitmap(
                TEX_WIDTH,
                TEX_HEIGHT,
                TEX_WIDTH_LOW,
                TEX_HEIGHT_LOW,
                "Can't alloc bitmap for labels");

        Canvas canvas = new Canvas(bitmap);
        canvas.setDensity(Bitmap.DENSITY_NONE);

        Context context = engine.activity;
        charMap.clear();
        lastTexX = 0;
        lastTexY = 0;

        textAscent = (int)Math.ceil(-paint.ascent()); // Paint.ascent is negative, so negate it
        textHeight = textAscent + (int)Math.ceil(paint.descent());
        spaceWidth = (int)Math.ceil(paint.measureText(" "));

        appendChars(canvas, "0123456789-");

        map[LABEL_FPS] = appendChars(canvas, context.getString(R.string.engine_lbl_fps));
        map[LABEL_CANT_OPEN] = appendChars(canvas, context.getString(R.string.engine_lbl_cant_open_door));
        map[LABEL_NEED_BLUE_KEY] = appendChars(canvas, context.getString(R.string.engine_lbl_need_blue_key));
        map[LABEL_NEED_RED_KEY] = appendChars(canvas, context.getString(R.string.engine_lbl_need_red_key));
        map[LABEL_NEED_GREEN_KEY] = appendChars(canvas, context.getString(R.string.engine_lbl_need_green_key));
        map[LABEL_SECRET_FOUND] = appendChars(canvas, context.getString(R.string.engine_lbl_secret_found));
        map[LABEL_ENDL_KILLS] = appendChars(canvas, context.getString(R.string.engine_lbl_endl_kills));
        map[LABEL_ENDL_SECRETS] = appendChars(canvas, context.getString(R.string.engine_lbl_endl_secrets));
        map[LABEL_ENDL_TIME] = appendChars(canvas, context.getString(R.string.engine_lbl_endl_time));
        map[LABEL_GAMEOVER] = appendChars(canvas, context.getString(R.string.engine_lbl_gameover));

        if (engine.canShowRewardedVideo) {
            map[LABEL_GAMEOVER_SUBTITLE] = appendChars(
                    canvas,
                    context.getString(R.string.engine_lbl_gameover_restart)
                            + " / "
                            + context.getString(R.string.engine_lbl_gameover_continue));

            map[LABEL_GAMEOVER_SUBTITLE_LEFT_HAND_AIM] = appendChars(
                    canvas,
                    context.getString(R.string.engine_lbl_gameover_continue)
                            + " / "
                            + context.getString(R.string.engine_lbl_gameover_restart));
        } else {
            map[LABEL_GAMEOVER_SUBTITLE] = appendChars(canvas, context.getString(R.string.engine_lbl_gameover_restart));
            map[LABEL_GAMEOVER_SUBTITLE_LEFT_HAND_AIM] = map[LABEL_GAMEOVER_SUBTITLE];
        }

        map[LABEL_GAMEOVER_SUBTITLE_JUST_RESTART] = appendChars(
                canvas,
                context.getString(R.string.engine_lbl_gameover_restart));

        map[LABEL_ACHIEVEMENT_UNLOCKED] = appendChars(
                canvas,
                context.getString(R.string.engine_lbl_achievement_unlocked));

        map[LABEL_HELP_MOVE] = appendChars(canvas, context.getString(R.string.engine_lblh_move));
        map[LABEL_HELP_ROTATE] = appendChars(canvas, context.getString(R.string.engine_lblh_rotate));
        map[LABEL_HELP_FIRE] = appendChars(canvas, context.getString(R.string.engine_lblh_fire));
        map[LABEL_HELP_WEAPONS] = appendChars(canvas, context.getString(R.string.engine_lblh_weapons));
        map[LABEL_HELP_QUICK_WEAPONS] = appendChars(canvas, context.getString(R.string.engine_lblh_quick_weapons));
        map[LABEL_HELP_MINIMAP] = appendChars(canvas, context.getString(R.string.engine_lblh_minimap));
        map[LABEL_HELP_STATS_HEALTH] = appendChars(canvas, context.getString(R.string.engine_lblh_stats_health));
        map[LABEL_HELP_STATS_AMMO] = appendChars(canvas, context.getString(R.string.engine_lblh_stats_ammo));
        map[LABEL_HELP_STATS_ARMOR] = appendChars(canvas, context.getString(R.string.engine_lblh_stats_armor));
        map[LABEL_HELP_STATS_KEYS] = appendChars(canvas, context.getString(R.string.engine_lblh_stats_keys));
        map[LABEL_HELP_DO_NOT_ROTATE] = appendChars(canvas, context.getString(R.string.engine_lblh_do_not_rotate));

        map[LABEL_MESSAGE_3] = appendChars(canvas, context.getString(R.string.engine_lblm_3));
        map[LABEL_MESSAGE_2] = appendChars(canvas, context.getString(R.string.engine_lblm_2));
        map[LABEL_MESSAGE_1] = appendChars(canvas, context.getString(R.string.engine_lblm_1));

        map[LABEL_MESSAGE_WELCOME_TO_TRAINING_AREA] = appendChars(
                canvas,
                context.getString(R.string.engine_lblm_welcome_to_training_area));

        map[LABEL_MESSAGE_USE_MOVEMENT_PAD_TO_FOLLOW_ARROWS] = appendChars(
                canvas,
                context.getString(R.string.engine_lblm_use_move_pad_to_follow_arrows));

        map[LABEL_MESSAGE_DOING_WELL] = appendChars(canvas, context.getString(R.string.engine_lblm_doing_well));

        map[LABEL_MESSAGE_SLIDE_TO_ROTATE] = appendChars(
                canvas,
                context.getString(R.string.engine_lblm_slide_to_rotate));

        map[LABEL_MESSAGE_MOVE_AND_ROTATE_TO_FOLLOW_ARROWS] = appendChars(
                canvas,
                context.getString(R.string.engine_lblm_move_and_rotate_to_follow_arrows));

        map[LABEL_MESSAGE_USE_MINIMAP_TO_FOLLOW_PATH] = appendChars(
                canvas,
                context.getString(R.string.engine_lblm_use_minimap_to_follow_path));

        map[LABEL_MESSAGE_TO_OPEN_DOOR_GO_THROUGH_IT] = appendChars(
                canvas,
                context.getString(R.string.engine_lblm_to_open_door_go_through_it));

        map[LABEL_MESSAGE_NEXT_DOOR_IS_CLOSED_USE_SWITCH] = appendChars(
                canvas,
                context.getString(R.string.engine_lblm_next_door_is_closed_use_switch));

        map[LABEL_MESSAGE_TO_ACTIVATE_SWITCH_GO_UP_TO_HIM] = appendChars(
                canvas,
                context.getString(R.string.engine_lblm_to_activate_switch_go_up_to_him));

        map[LABEL_MESSAGE_THIS_IS_WINDOW] = appendChars(canvas, context.getString(R.string.engine_lblm_this_is_window));

        map[LABEL_MESSAGE_TO_OPEN_DOOR_PICKUP_KEY] = appendChars(
                canvas,
                context.getString(R.string.engine_lblm_to_open_door_pickup_key));

        map[LABEL_MESSAGE_PRESS_END_LEVEL] = appendChars(
                canvas,
                context.getString(R.string.engine_lblm_press_end_level));

        map[LABEL_MESSAGE_USE_FIRE_AND_KILL_ENEMY] = appendChars(
                canvas,
                context.getString(R.string.engine_lblm_use_fire_and_kill_enemy));

        map[LABEL_MESSAGE_PICKUP_MEDI] = appendChars(canvas, context.getString(R.string.engine_lblm_pickup_medi));

        map[LABEL_MESSAGE_OPEN_WEAPON_MENU_AND_SELECT_PISTOL] = appendChars(
                canvas,
                context.getString(R.string.engine_lblm_open_weapon_menu_and_select_pistol));

        map[LABEL_MESSAGE_PICKUP_AMMO] = appendChars(canvas, context.getString(R.string.engine_lblm_pickup_ammo));

        map[LABEL_MESSAGE_QUICK_CHANGE_WEAPON] = appendChars(
                canvas,
                context.getString(R.string.engine_lblm_quick_change_weapon));

        map[LABEL_MESSAGE_AFTER_QUICK_CHANGE_WEAPON] = appendChars(
                canvas,
                context.getString(R.string.engine_lblm_after_quick_change_weapon));

        map[LABEL_MESSAGE_PICKUP_ARMOR] = appendChars(canvas, context.getString(R.string.engine_lblm_pickup_armor));

        map[LABEL_MESSAGE_AFTER_PICKUP_ARMOR] = appendChars(
                canvas,
                context.getString(R.string.engine_lblm_after_pickup_armor));

        for (int i = 0, len = Achievements.LIST.length; i < len; i++) {
            appendChars(canvas, Achievements.cleanupTitle(context.getString(Achievements.LIST[i].titleResourceId)));
        }

        renderer.bitmapToTexture(Renderer.TEXTURE_LABELS, bitmap);

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

    private float batchCharacter(Rect rect, float xpos, float ypos, float scale) {
        if (rect == null) {
            return (float)spaceWidth * scale;
        }

        //noinspection MagicNumber
        renderer.setTexRect(
                (rect.left << 16) / TEX_WIDTH,
                (rect.bottom << 16) / TEX_HEIGHT,
                (rect.right << 16) / TEX_WIDTH,
                (rect.top << 16) / TEX_HEIGHT
        );

        renderer.setCoordsQuadRectFlat(
                xpos,
                ypos,
                xpos + (float)(rect.width() - 1) * scale,
                ypos + (float)(textHeight - 1) * scale
        );

        renderer.batchQuad();
        return (float)rect.width() * scale;
    }

    public void startBatch() {
        startBatch(false);
    }

    public void startBatch(boolean customMatrices) {
        if (!customMatrices) {
            renderer.useOrtho(-engine.ratio, engine.ratio, -1.0f, 1.0f, 0.0f, 1.0f);
        }

        renderer.startBatch();
    }

    @SuppressWarnings("MagicNumber")
    public void batch(
            float sx,
            float sy,
            float ex,
            float ey,
            String str,
            float desiredHeight,
            int align) {

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
            sx += batchCharacter((ch == ' ' ? null : charMap.get(ch)), sx, sy, scale);
        }
    }

    @SuppressWarnings("MagicNumber")
    public void batch(
            float sx,
            float sy,
            float ex,
            float ey,
            int value,
            float desiredHeight,
            int align) {

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
            batchCharacter(numberMap[0], sx, sy, scale);
            return;
        }

        if (value < 0) {
            sx += batchCharacter(numberMap[10], sx, sy, scale);
            value = -value;
        }

        int divider = 1;
        int tmpValue = value / 10;

        while (tmpValue > 0) {
            divider *= 10;
            tmpValue /= 10;
        }

        while (divider > 0) {
            sx += batchCharacter(numberMap[(value / divider) % 10], sx, sy, scale);
            divider /= 10;
        }
    }

    public void renderBatch() {
        renderer.renderBatch(Renderer.FLAG_BLEND, Renderer.TEXTURE_LABELS);
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

    @SuppressWarnings({ "unused", "RedundantSuppression" })
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
