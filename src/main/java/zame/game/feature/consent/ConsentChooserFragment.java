package zame.game.feature.consent;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import zame.game.App;
import zame.game.R;
import zame.game.core.app.BaseFragment;
import zame.game.feature.sound.SoundManager;

public class ConsentChooserFragment extends BaseFragment {
    public static ConsentChooserFragment newInstance() {
        return new ConsentChooserFragment();
    }

    private Button backButtonView;
    private Button nextButtonView;

    public ConsentChooserFragment() {
        super();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        ViewGroup viewGroup = (ViewGroup)inflater.inflate(R.layout.consent_fragment, container, false);

        backButtonView = viewGroup.findViewById(R.id.back);
        nextButtonView = viewGroup.findViewById(R.id.next);

        return viewGroup;
    }

    @Override
    public void onStart() {
        super.onStart();

        backButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.quitGame();
            }
        });

        nextButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.self.preferences.putBoolean(R.string.key_is_consent_chosen, true);
                App.self.applyConsent();

                activity.processNext();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        activity.soundManager.setPlaylist(SoundManager.LIST_MAIN);
    }

    @Override
    public void onStop() {
        backButtonView.setOnClickListener(null);
        nextButtonView.setOnClickListener(null);

        super.onStop();
    }
}
