package zame.game.feature.consent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isForgottenFragment && !App.self.preferences.getBoolean(R.string.key_is_consent_chosen)) {
            App.self.preferences.putBoolean(
                    R.string.key_consent_ad_personalization,
                    !App.self.isLimitAdTrackingEnabled);
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        ViewGroup viewGroup = (ViewGroup)inflater.inflate(R.layout.consent_fragment, container, false);

        if (!isForgottenFragment) {
            backButtonView = viewGroup.findViewById(R.id.back);
            nextButtonView = viewGroup.findViewById(R.id.next);
        }

        return viewGroup;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (isForgottenFragment) {
            return;
        }

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

        if (!isForgottenFragment) {
            activity.soundManager.setPlaylist(SoundManager.LIST_MAIN);
        }
    }

    @Override
    public void onStop() {
        if (isForgottenFragment) {
            return;
        }

        backButtonView.setOnClickListener(null);
        nextButtonView.setOnClickListener(null);

        super.onStop();
    }
}
