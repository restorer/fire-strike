package zame.game.feature.achievements;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import zame.game.R;
import zame.game.engine.state.Profile;
import zame.game.engine.state.State;
import zame.game.feature.main.MainActivity;

public class AchievementsAdapter extends BaseAdapter {
    public static class ItemViewHolder {
        ViewGroup viewGroup;
        TextView titleText;
        TextView descriptionText;
        TextView statusText;

        ItemViewHolder(ViewGroup viewGroup) {
            this.viewGroup = viewGroup;

            titleText = viewGroup.findViewById(R.id.title);
            descriptionText = viewGroup.findViewById(R.id.description);
            statusText = viewGroup.findViewById(R.id.status);
        }
    }

    private final MainActivity activity;
    private final Profile profile;
    private final State state;
    private final LayoutInflater layoutInflater;
    private final Achievement[] items;
    private final Drawable altBgDrawable;
    private final int colorLocked;
    private final int colorAchieved;
    private final String textAchieved;

    AchievementsAdapter(MainActivity activity, Profile profile) {
        super();

        this.activity = activity;
        this.profile = profile;
        this.state = activity.engine.state;
        this.layoutInflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        items = Achievements.LIST;
        Resources resources = activity.getResources();

        altBgDrawable = new ColorDrawable(resources.getColor(R.color.gloomy_achievements_altbg));
        colorLocked = resources.getColor(R.color.gloomy_achievements_locked);
        colorAchieved = resources.getColor(R.color.gloomy_achievements_achieved);
        textAchieved = resources.getString(R.string.achievements_item_achieved);

        activity.tryAndLoadInstantState();
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int position) {
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        return items[position].id;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewGroup viewGroup;
        ItemViewHolder holder;

        if (convertView != null) {
            viewGroup = (ViewGroup)convertView;
            holder = (ItemViewHolder)viewGroup.getTag();
        } else {
            viewGroup = (ViewGroup)layoutInflater.inflate(R.layout.achievements_item, parent, false);
            holder = new ItemViewHolder(viewGroup);
            viewGroup.setTag(holder);
        }

        Achievement item = items[position];

        holder.viewGroup.setBackground(item.isAltBackground ? altBgDrawable : null);
        holder.titleText.setText(Html.fromHtml(activity.getString(item.titleResourceId).toUpperCase()));
        holder.descriptionText.setText(Html.fromHtml(activity.getString(item.descriptionResourceId)));

        if (item.isAchieved(profile)) {
            holder.statusText.setTextColor(colorAchieved);
            holder.statusText.setText(textAchieved);
        } else {
            holder.statusText.setTextColor(colorLocked);
            holder.statusText.setText(item.getStatusText(profile, state));
        }

        return viewGroup;
    }
}
