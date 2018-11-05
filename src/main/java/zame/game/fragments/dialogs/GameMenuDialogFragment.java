package zame.game.fragments.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import zame.game.Common;
import zame.game.MainActivity;
import zame.game.R;
import zame.game.engine.Engine;
import zame.game.engine.State;
import zame.game.engine.Weapons;
import zame.game.engine.controls.Controls;
import zame.game.managers.SoundManager;

// http://stackoverflow.com/questions/12239886/how-to-migrate-from-gallery-to-horizontalscrollview-viewpager
// https://gist.github.com/8cbe094bb7a783e37ad1

@SuppressWarnings({ "deprecation" })
public class GameMenuDialogFragment extends BaseDialogFragment {
    public static class ImageAdapter extends BaseAdapter implements AdapterView.OnItemSelectedListener {
        // @formatter:off
        @SuppressWarnings({ "rawtypes", "unchecked" })
        static final Pair<Integer, Integer>[] mapping = new Pair[] {
                new Pair<>(Weapons.WEAPON_KNIFE, R.drawable.weapon_knife),
                new Pair<>(Weapons.WEAPON_PISTOL, R.drawable.weapon_pist),
                new Pair<>(Weapons.WEAPON_DBLPISTOL, R.drawable.weapon_dblpist),
                new Pair<>(Weapons.WEAPON_AK47, R.drawable.weapon_ak47),
                new Pair<>(Weapons.WEAPON_TMP, R.drawable.weapon_tmp),
                new Pair<>(Weapons.WEAPON_WINCHESTER, R.drawable.weapon_shtg),
                new Pair<>(Weapons.WEAPON_GRENADE, R.drawable.weapon_rocket), };
        // @formatter:on

        private Context context;
        private Weapons weapons;
        private android.widget.AbsSpinner gallery;
        private Drawable normalBackground;
        private Drawable selectedBackground;
        private ViewGroup.LayoutParams layoutParams;
        private ArrayList<Pair<Integer, Drawable>> imagesList;
        private TextView infoView;

        @SuppressWarnings({ "deprecation" })
        ImageAdapter(Context context,
                android.widget.AbsSpinner gallery,
                TextView infoView,
                Engine engine,
                int sizeInPx) {

            super();

            this.context = context;
            this.gallery = gallery;
            this.infoView = infoView;
            this.weapons = engine.weapons;

            Resources resources = context.getResources();
            imagesList = new ArrayList<>();

            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0.0f);

            //noinspection MagicNumber
            colorMatrix.setScale(0.25f, 0.25f, 0.25f, 1.0f);

            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);

            for (Pair<Integer, Integer> pair : mapping) {
                int weaponId = pair.first;
                Drawable drawable = resources.getDrawable(pair.second);

                if (weapons.canSwitch(weaponId)) {
                    drawable.setColorFilter(null);
                } else {
                    drawable.setColorFilter(filter);
                }

                imagesList.add(new Pair<>(weaponId, drawable));
            }

            layoutParams = new android.widget.Gallery.LayoutParams(sizeInPx, sizeInPx);
            normalBackground = resources.getDrawable(R.drawable.weapon_normal);
            selectedBackground = resources.getDrawable(R.drawable.weapon_selected);

            gallery.setOnItemSelectedListener(this);
        }

        void setSelectedWeapon(int weaponId) {
            for (int i = 0, len = imagesList.size(); i < len; i++) {
                if (imagesList.get(i).first == weaponId) {
                    gallery.setSelection(i);
                    break;
                }
            }
        }

        @Override
        public int getCount() {
            return imagesList.size();
        }

        @Override
        public Object getItem(int position) {
            return imagesList.get(position).first;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        @SuppressWarnings({ "deprecation" })
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;

            if (convertView instanceof ImageView) {
                imageView = (ImageView)convertView;
            } else {
                imageView = new ImageView(context);
            }

            imageView.setImageDrawable(imagesList.get(position).second);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setLayoutParams(layoutParams);
            setSelectedState(imageView, false);

            return imageView;
        }

        @SuppressWarnings({ "deprecation" })
        void setSelectedState(ImageView imageView, boolean selected) {
            imageView.setBackgroundDrawable(selected ? selectedBackground : normalBackground);
            imageView.setPadding(0, 0, 0, 0);
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            View selectedView = gallery.getSelectedView();
            int selectedIndex = (Integer)gallery.getSelectedItem();
            boolean canSwitch = weapons.canSwitch(selectedIndex);

            for (int i = 0, len = gallery.getChildCount(); i < len; i++) {
                ImageView imageView = (ImageView)gallery.getChildAt(i);

                if (imageView != null) {
                    setSelectedState(imageView, (imageView == selectedView) && canSwitch);
                }
            }

            infoView.setText(Weapons.WEAPONS[selectedIndex].description);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    private MainActivity activity;
    private Engine engine;
    private Weapons weapons;
    private State state;
    private android.widget.AbsSpinner gallery;
    @SuppressWarnings("BooleanVariableAlwaysNegated") private boolean ignoreDismissHandlerOnce;
    private int minItemSizeInPx;
    private int maxItemSizeInPx;
    private int paddingInPx;

    public static GameMenuDialogFragment newInstance() {
        return new GameMenuDialogFragment();
    }

    @SuppressWarnings("MagicNumber")
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.activity = (MainActivity)context;
        this.engine = this.activity.engine;
        this.weapons = this.engine.weapons;
        this.state = this.engine.state;

        minItemSizeInPx = Common.dpToPx(activity, 80);
        maxItemSizeInPx = Common.dpToPx(activity, 160);
        paddingInPx = Common.dpToPx(activity, (20 + 10) * 2);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);
    }

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    @SuppressWarnings("deprecation")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup)LayoutInflater.from(activity).inflate(R.layout.dialog_game_menu, null);

        int maxGalleryWidth = engine.width - paddingInPx;

        int itemSize = Math.max(minItemSizeInPx,
                Math.min(maxItemSizeInPx, Math.min(engine.height / 3, maxGalleryWidth / 5)));

        gallery = (android.widget.Gallery)viewGroup.findViewById(R.id.gallery);
        gallery.setMinimumHeight(itemSize);
        gallery.setMinimumWidth(Math.min(maxGalleryWidth, itemSize * 5));

        ImageAdapter adapter = new ImageAdapter(activity,
                gallery,
                (TextView)viewGroup.findViewById(R.id.info),
                engine,
                itemSize);

        gallery.setAdapter(adapter);
        adapter.setSelectedWeapon(engine.state.heroWeapon);

        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int weaponIdx = (Integer)gallery.getItemAtPosition(position);

                if (weapons.canSwitch(weaponIdx)) {
                    if (engine.state.heroWeapon != weaponIdx) {
                        weapons.switchWeapon(weaponIdx);
                        engine.state.autoSelectWeapon = false;
                        engine.interacted = true;
                    }

                    ignoreDismissHandlerOnce = true;
                    GameMenuDialogFragment.this.dismiss();
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        if ((state.disabledControlsMask & Controls.CONTROL_WEAPONS) != 0) {
            gallery.setVisibility(View.GONE);
            builder = builder.setTitle(R.string.dlg_menu);
        } else {
            builder = builder.setTitle(R.string.dlg_change_weapon);
        }

        return builder.setView(viewGroup)
                .setNeutralButton(R.string.dlg_exit_to_menu, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        GameMenuDialogFragment.this.dismiss();
                        engine.gameViewActive = false;
                        engine.renderBlackScreen = true;
                        activity.showFragment(activity.menuFragment);
                    }
                })
                .setNegativeButton(R.string.dlg_game_code, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        GameMenuDialogFragment.this.dismiss();
                        activity.gameFragment.showGameCodeDialog();
                    }
                })
                .setPositiveButton(R.string.dlg_close, null)
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        soundManager.setPlaylist(SoundManager.LIST_MAIN);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (!ignoreDismissHandlerOnce) {
            int weaponId = (Integer)gallery.getSelectedItem();

            if ((engine.state.heroWeapon != weaponId) && weapons.canSwitch(weaponId)) {
                weapons.switchWeapon(weaponId);
                engine.state.autoSelectWeapon = false;
            }
        } else {
            ignoreDismissHandlerOnce = false;
        }

        super.onDismiss(dialog);
    }

    @Override
    public int getFocusMask() {
        return SoundManager.FOCUS_MASK_GAME_MENU_DIALOG;
    }
}
