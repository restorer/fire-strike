package zame.game.engine.controls;

import zame.game.engine.Config;
import zame.game.engine.Engine;
import zame.game.engine.EngineObject;
import zame.game.engine.State;

public class AccelerometerController implements EngineObject {
    private Engine engine;
    private Config config;
    private State state;
    private float accelerometerX;

    @Override
    public void setEngine(Engine engine) {
        this.engine = engine;
        this.config = engine.config;
        this.state = engine.state;
    }

    public void reload() {
        accelerometerX = 0.0f;
    }

    public void setAccelerometerValues(float accelerometerX, @SuppressWarnings("unused") float accelerometerY) {
        this.accelerometerX = accelerometerX;
    }

    public void updateHero() {
        //noinspection MagicNumber
        if (Math.abs(accelerometerX) >= 0.1f && (state.disabledControlsMask & Controls.CONTROL_ROTATE) == 0) {
            state.setHeroA(state.heroA
                    + accelerometerX
                    * config.accelerometerAcceleration
                    * config.horizontalLookMult
                    * config.rotateSpeed);

            engine.interacted = true;
        }
    }
}
