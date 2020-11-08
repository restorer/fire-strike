<?php

defined('POPLR') or die();

return array(
	'core.fatalError' => 'Fatal error occurred',
	'core.unknownAction' => 'Unknown action',
	'core.loginRequired' => 'This action is available only for registered users',
	'core.noPermissions' => 'Current user has no permissions for current action',
	'core.uploadingDisabled' => 'Uploading images is temporarily disabled',
	'core.commentsDisabled' => 'Commenting is temporarily disabled',
	'db.connectionFailed' => 'Connection failed: %s',
	'db.sqlError' => 'SQL Error: %s',

	'app.layout.title' => 'Fire Strike Retro',
	'app.layout.subtitle' => 'An old&ndash;school <nobr>3d&ndash;shooter</nobr><br />with cartoon graphics<br />from creators of Gloomy Dungeons',
	'app.layout.privacy' => 'Privacy',
	'app.layout.licenses' => 'Licenses',

    'app.index.content' => '
<p>Fire Strike Retro is an old–school 3d–shooter with cartoon graphics from creators of Gloomy Dungeons.</p>
<p>Initially, we had an idea to take the Gloomy Dungeons 2 engine, use cool cartoon graphics (instead of 8-bit), change levels a little, and go on. But in the middle of the process it became clear that it would take much more time :)</p>
<ul>
    <li>We completely redesigned the tutorial. We hope that it will be not too complicated for beginners, and, on the other hand, it will not be too dull for experienced players in 3d–action games.</li>
    <li>The minimap has completely changed. Instead of turning it on and off (as in the original Gloomy), it is now always visible. In addition, it always shows where to go (instead of arrows on the floor, as it was before), so you won’t get lost in endless labyrinths.</li>
    <li>The game was carefully rebalanced (it is a shame, but earlier we did it almost randomly). It’s up to you to decide whether we’ve done it well or not, but to our taste it became much more fun to play.</li>
    <li>We’ve added the ability to quickly change weapons (instead of constantly entering the menu). It seems to us that this is a handy feature, especially at the final levels, in order to quickly switch between grenades and winchester.</li>
    <li>And finally, the main feature that distinguishes this game from our previous ones is the use of procedural generation as a base for creating levels. Of course, we did not put the generated levels as is - each of them was corrected by hands with love. Nevertheless, it was procedural generation that set the basic structure and "rhythm" of the level. Perhaps, because of it, on some levels you will have to run back and forth, but, in our opinion, this adds something special to the game.</li>
</ul>
<p>Compared to Gloomy, this game is much more dynamic. No more boring monsters - only hardcore! If you loved 3D first person shooters (FPS), give this game a chance :)</p>
<p>And briefly for the rest of the features:</p>
<ul>
    <li>35 levels.</li>
    <li>7 different weapons.</li>
    <li>More than 5000 enemies.</li>
    <li>About 5 hours of gameplay.</li>
    <li>24 achievements.</li>
    <li>Each level have one or more secrets.</li>
</ul>
<p>Have fun! :)</p>
    ',

	'app.help.howToPlay' => 'How to play',
	'app.help.howToPlayContent' => '
<h3><span>Main menu</h3></span>
<p>
	After you tap <strong>Play</strong> button, you will see the map, which will show your progress. Tap <strong>Continue</strong> button or the map to continue the game.
</p>
<p>
	Tap <strong>Options</strong> to set&ndash;up sound and controls. Tap <strong>Achievements</strong> to see available achievements.
</p>
	',
	'app.help.controls' => 'Controls',
	'app.help.controlsContent' => '
<p>
	In the <strong>lower left</strong> corner is on&ndash;screen joystick, with which you can move forward, backward, left and right. In the <strong>lower right</strong> corner is fire button.
</p>
<p>
	The entire <strong>right side</strong> of the screen can be used for rotating &mdash; you should put a finger on the screen and move it to the left / right. If you initially put your finger on the fire button, you can rotate and shoot at the same time.
</p>
<p>
	In the <strong>upper left</strong> corner is game menu button. Here, you can change your weapon, enter the game code or exit to the main menu. Below it the are indicators of health, armor and ammo. Next to them taken keys indicators will be shown.
</p>
<p>
	In the <strong>upper right</strong> corner there is minimap. The places you have visited and direction indicators are displayed on it.
</p>
	',
	'app.help.options' => 'Options',
	'app.help.optionsContent' => '
<h3><span>General</span></h3>
<p>
	<strong>Language</strong> &mdash; change in&ndash;game language.
</p>
<p>
	<strong>Help</strong> &mdash; open this help.
</p>
<p>
	<strong>About and licenses</strong> &mdash; authors and licenses for the free software used in the game.
</p>
<p>
	<strong>Restart game from the very beginning</strong> &mdash; if, for any reason, you will want to start over. Game progress and achievements will be lost. It is not a joke!
</p>
<h3><span>Sound</span></h3>
<p>
	<strong>Enable sound</strong> &mdash; if you play at night, turn off the sound in order not to frighten the neighbors :)
</p>
<p>
	<strong>Music volume</strong> &mdash; the volume of background music.
</p>
<p>
	<strong>Effects volume</strong> &mdash; the volume of shots and other sounds.
</p>
<h3><span>Controls</span></h3>
<p>
	<strong>Control scheme</strong> &mdash; here you can select control scheme if you do not like the current one.
</p>
<p>
	<strong>Controls setting</strong> &mdash; although we originally picked the most optimal settings, you may want to customize them for yourself.
</p>
<p>
	<strong>Key mappings</strong> &mdash; if there are hardware buttons in your phone or tablet, then you can map them here.
</p>
<h3><span>Screen</span></h3>
<p>
	<strong>Gamma</strong> &mdash; if even at maximum brightness of screen the image is too dark, here you can fix it.
</p>
<p>
	<strong>Show crosshair</strong> &mdash; although, following the canons of Doom you will not have to aim precisely, you can still turn on the sight.
</p>
<p>
	<strong>Rotate in–game screen</strong> &mdash; rotate the screen by 180 degrees. It is useful if there are hardware buttons (D–Pad or trackball) in your phone and you want to play with you left hand.
</p>
<h3><span>Analytics and ADs</span></h3>
<p>
	<strong>Personalize your AD experience</strong> &mdash; Fire Strike personalizes your advertising experience using Appodeal. Appodeal and its partners may collect and process personal data such as device identifiers, location data, and other demographic and interest data to provide advertising experience tailored to you. By consenting to this improved ad experience, you`ll see ads that Appodeal and its partners believe are more relevant to you. By agreeing, you confirm that you are over the age of 16 and would like a personalized ad experience. Learn more at <a href="https://www.appodeal.com/privacy-policy" target="_blank">https://www.appodeal.com/privacy-policy</a>. If you disagree, Appodeal won`t collect your data for personalized advertising in this app. However you will still see ads, but they may not be as relevant to your interests.
</p>
<p>
	<strong>Send in&ndash;game analytics</strong> &mdash; we will use this anonymous data to improve this and other our games.
</p>
<h3><span>Controls setting</span></h3>
<p>
	<strong>Move speed</strong> &mdash; move speed set&ndash;up (forward/backward).
</p>
<p>
	<strong>Strafe speed</strong> &mdash; strafe speed set&ndash;up (left/right).
</p>
<p>
	<strong>Rotate speed</strong> &mdash; rotate speed set&ndash;up.
</p>
<p>
	<strong>Invert vertical look</strong> &mdash; select to invert vertical look.
</p>
<p>
	<strong>Invert horizontal look</strong> &mdash; select to invert horizontal look (rotating).
</p>
<p>
	<strong>Left hand aim</strong> &mdash; select to place the fire button on the left (and on–screen joystick on the right).
</p>
<p>
	<strong>Fire button at top</strong> &mdash; select to place the fire button on the top (useful if there is a problem with multitouch).
</p>
<p>
	<strong>Controls scale</strong> &mdash; adjust the size of on–screen controls.
</p>
<p>
	<strong>Controls opacity</strong> &mdash; transparency of on–screen controls, from almost invisible to fully opaque.
</p>
<p>
	<strong>Enable accelerometer</strong> &mdash; select for rotation with the accelerometer.
</p>
<p>
	<strong>Accelerometer acceleration</strong> &mdash; how quickly a player will rotate using the accelerometer.
</p>
<p>
	<strong>Trackball acceleration</strong> &mdash; how quickly a player will move by controlling with the help of trackball (if it is in your phone).
</p>
	',

	'app.dislike.title' => 'Dislike? Tell us why',
	'app.dislike.placeholder' => 'Tell us what we can improve in the game',
	'app.dislike.send' => 'Click here to send response',
	'app.dislike.thankYou' => 'Thank you for your response',

	'app.privacy.title' => 'Privacy policy',
	'app.privacy.eightSines' => 'EightSines Studio privacy policy',
	'app.privacy.eightSinesContent' => '
We collect anonimized data about your game experience and achievements. Also we hope this does not happen, but if the game will crash, we collect anonimized information about crash, which may include device model and manufacturer.
	',
	'app.privacy.thirdParty' => 'Third&ndash;party libraries',

	'app.licenses.title' => 'Licenses',
	'app.licenses.content' => '
<h3><span>Banana Brick Font</span></h3>
<p>SIL OPEN FONT LICENSE</p>
<p>Version 1.1 - 26 February 2007</p>
<p>
    <strong>PREAMBLE</strong><br />
    The goals of the Open Font License (OFL) are to stimulate worldwide development of collaborative font projects, to support the font creation efforts of academic and linguistic communities, and to provide a free and open framework in which fonts may be shared and improved in partnership with others.<br />
    <br />
    The OFL allows the licensed fonts to be used, studied, modified and redistributed freely as long as they are not sold by themselves. The fonts, including any derivative works, can be bundled, embedded, redistributed and/or sold with any software provided that any reserved names are not used by derivative works. The fonts and derivatives, however, cannot be released under any other type of license. The requirement for fonts to remain under this license does not apply to any document created using the fonts or their derivatives.
</p>
<p>
    <strong>DEFINITIONS</strong><br />
    "Font Software" refers to the set of files released by the Copyright Holder(s) under this license and clearly marked as such. This may include source files, build scripts and documentation.<br />
    <br />
    "Reserved Font Name" refers to any names specified as such after the copyright statement(s).<br />
    <br />
    "Original Version" refers to the collection of Font Software components as distributed by the Copyright Holder(s).<br />
    <br />
    "Modified Version" refers to any derivative made by adding to, deleting, or substituting — in part or in whole — any of the components of the Original Version, by changing formats or by porting the Font Software to a new environment.<br />
    <br />
    "Author" refers to any designer, engineer, programmer, technical writer or other person who contributed to the Font Software.
</p>
<p>
    <strong>PERMISSION &amp; CONDITIONS</strong><br />
    Permission is hereby granted, free of charge, to any person obtaining a copy of the Font Software, to use, study, copy, merge, embed, modify, redistribute, and sell modified and unmodified copies of the Font Software, subject to the following conditions:
</p>
<ol>
    <li>Neither the Font Software nor any of its individual components, in Original or Modified Versions, may be sold by itself.</li>
    <li>Original or Modified Versions of the Font Software may be bundled, redistributed and/or sold with any software, provided that each copy contains the above copyright notice and this license. These can be included either as stand-alone text files, human-readable headers or in the appropriate machine-readable metadata fields within text or binary files as long as those fields can be easily viewed by the user.</li>
    <li>No Modified Version of the Font Software may use the Reserved Font Name(s) unless explicit written permission is granted by the corresponding Copyright Holder. This restriction only applies to the primary font name as presented to the users.</li>
    <li>The name(s) of the Copyright Holder(s) or the Author(s) of the Font Software shall not be used to promote, endorse or advertise any Modified Version, except to acknowledge the contribution(s) of the Copyright Holder(s) and the Author(s) or with their explicit written permission.</li>
    <li>The Font Software, modified or unmodified, in part or in whole, must be distributed entirely under this license, and must not be distributed under any other license. The requirement for fonts to remain under this license does not apply to any document created using the Font Software.</li>
</ol>
<p>
    <strong>TERMINATION</strong><br />
    This license becomes null and void if any of the above conditions are not met.
</p>
<p>
    <strong>DISCLAIMER</strong><br />
    THE FONT SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT OF COPYRIGHT, PATENT, TRADEMARK, OR OTHER RIGHT. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, INCLUDING ANY GENERAL, SPECIAL, INDIRECT, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF THE USE OR INABILITY TO USE THE FONT SOFTWARE OR FROM OTHER DEALINGS IN THE FONT SOFTWARE.
</p>
<p>
    (C) 2003-2011 SIL International, all rights reserved, unless otherwise noted elsewhere on this page.<br />
    Provided by SIL’s Non-Roman Script Initiative. Contact us at <a href="mailto:nrsi@sil.org">nrsi@sil.org</a>.
</p>
    ',

	'app.viewdislikes.title' => 'View dislikes',
	'app.viewdislikes.code' => 'Secret code',
	'app.viewdislikes.send' => 'View',
);
