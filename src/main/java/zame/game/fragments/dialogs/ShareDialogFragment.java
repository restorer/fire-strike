package zame.game.fragments.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import java.util.Locale;
import zame.game.App;
import zame.game.Common;
import zame.game.MainActivity;
import zame.game.R;
import zame.game.managers.SoundManager;
import zame.game.misc.IntentProvider;

public class ShareDialogFragment extends BaseDialogFragment {
    public static class ListItem {
        public String title;
        public Intent intent;

        ListItem(String title, Intent intent) {
            this.title = title;
            this.intent = intent;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    protected MainActivity activity;
    protected ArrayAdapter<ListItem> adapter;
    protected ArrayList<ListItem> items = new ArrayList<>();

    public static ShareDialogFragment newInstance() {
        return new ShareDialogFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.activity = (MainActivity)context;

        String title = getString(R.string.share_title);
        String url = Common.WEB_LINK + Locale.getDefault().getLanguage().toLowerCase();

        items.clear();
        items.add(new ListItem("Facebook", IntentProvider.getFacebookIntent(activity, title, url)));
        items.add(new ListItem("Twitter", IntentProvider.getTwitterIntent(activity, title, url)));
        items.add(new ListItem("Google+", IntentProvider.getGooglePlusIntent(activity, title, url)));
        items.add(new ListItem("VK", IntentProvider.getVkIntent(activity, title, url)));

        adapter = new ArrayAdapter<>(activity, android.R.layout.select_dialog_item, items);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(activity).setTitle(R.string.dlg_share)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which >= 0 && which < items.size()) {
                            App.self.trackerInst.send("Share", items.get(which).title);
                            Common.openExternalIntent(getActivity(), items.get(which).intent);
                        }
                    }
                })
                .setNegativeButton(R.string.dlg_cancel, null)
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        soundManager.setPlaylist(SoundManager.LIST_MAIN);
    }

    @Override
    public int getFocusMask() {
        return SoundManager.FOCUS_MASK_SHARE_DIALOG;
    }
}
