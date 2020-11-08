# Fire Strike Retro

An old–school 3d–shooter with cartoon graphics from creators of Gloomy Dungeons. [Web site](https://eightsines.com/fire-strike/).

<a href="https://f-droid.org/packages/com.eightsines.firestrike.opensource" target="_blank"><img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="100" /></a>

Credits:

  - Code: Viachaslau Tratsiak
  - Graphics: Gulamov Otabek (gulamovo2907@gmail.com), Viachaslau Tratsiak
  - Levels: Viachaslau Tratsiak
  - Sound:

This game is released under MIT License (http://www.opensource.org/licenses/mit-license.php).

# Product support

This product is already finished, so no further support is planned.

| Feature | Support status |
|---|---|
| New features | No |
| Non-critical bugfixes | No |
| Critical bugfixes | Yes, if it will be easy to understand where to fix |
| Pull requests | Accepted (after review) |
| Issues | Monitored, but if you want to change something - submit a pull request |
| Android version planned to support | Up to 11.x |
| Estimated end-of-life | Up to 2021 |

## Compile and install debug build

  - `./z-build debug install` or
  - `./gradlew installDebug`

## Compile release builds

To be able to compile release builds, create put your keystore file (or create new) to `_signing/signing.keystore` and create `_signing/signing.properties`:

```
keyAlias=put_key_alias_here
storePassword=put_keystore_password_here
keyPassword=put_key_password_here
```

  - `./z-build release` or
  - `./gradlew assembleRelease`

Look for the .apk files in `build/outputs/apk/`

## Other requirements

- To update the levels you need `ruby` and `natcmp` gem.
- To update the textures you need `imagemagick` 6.x (with 7.x you may get weird results), `ruby`, `rmagick` gem and `optipng` utility.
- To convert sounds and music you need the `sox` utility.
