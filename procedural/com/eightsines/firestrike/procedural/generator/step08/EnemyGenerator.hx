package com.eightsines.firestrike.procedural.generator.step08;

import com.eightsines.firestrike.procedural.config.Config;
import com.eightsines.firestrike.procedural.config.EnemyConfig;
import com.eightsines.firestrike.procedural.config.Settings;
import com.eightsines.firestrike.procedural.config.WeaponConfig;
import com.eightsines.firestrike.procedural.generator.AbstractSectionGenerator;
import com.eightsines.firestrike.procedural.generator.Generator;
import com.eightsines.firestrike.procedural.geom.Point;
import com.eightsines.firestrike.procedural.layer.IntLayer;
import com.eightsines.firestrike.procedural.section.Section; // {SectionEnemy, SectionItem}
import com.eightsines.firestrike.procedural.section.SectionItemType;
import com.eightsines.firestrike.procedural.util.GeneratorException;
import com.eightsines.firestrike.procedural.util.GeneratorUtils;
import com.eightsines.firestrike.procedural.util.IntMath;
import com.eightsines.firestrike.procedural.util.Pair;
import com.eightsines.firestrike.procedural.util.Random;
import com.eightsines.firestrike.procedural.view.Viewer;
import haxe.ds.BalancedTree;
import haxe.ds.Either;
import haxe.ds.Option;

using Lambda;
using Safety;
using org.zamedev.lib.LambdaExt;
using com.eightsines.firestrike.procedural.util.Tools;

class EnemyGeneratorFightResult {
    public var weaponConfig : WeaponConfig;
    public var healthLoss : Int;
    public var armorLoss : Int;
    public var ammoLoss : Int;
    public var debugInfo : String;

    public function new(weaponConfig : WeaponConfig, healthLoss : Int, armorLoss : Int, ammoLoss : Int, debugInfo : String) {
        this.weaponConfig = weaponConfig;
        this.healthLoss = healthLoss;
        this.armorLoss = armorLoss;
        this.ammoLoss = ammoLoss;
        this.debugInfo = debugInfo;
    }
}

class EnemyGenerator extends AbstractSectionGenerator implements Generator {
    private static inline var DIFFICULTY_EPSILON : Float = 0.0001;
    private static inline var NEED_MORE_ENEMIES_THRESHOLD : Int = 10;
    private static inline var HEALTH_GRANULARITY : Float = 2.0;

    private var settings : Settings;
    private var scenarioSections : SafeArray<SafeArray<Section>> = [];
    private var heroHealth : Int = 0;
    private var heroArmor : Int = 0;
    private var heroAmmo : SafeArray<Option<Int>> = [];
    private var heroWeapons : SafeArray<WeaponConfig> = []; // sorted from best to worst
    private var availEnemies : SafeArray<EnemyConfig> = []; // sorted from easiest to hardest
    private var sectionDifficulty : Float = 0.0;
    private var minHeroHealth : Int = 0;
    private var minHeroArmor : Int = 0;
    private var minHeroAmmo : SafeArray<Int> = [];
    private var generatedAmmoIndex : Int = 0;

    public function new(
        settings : Settings,
        random : Random,
        layer : IntLayer,
        viewer : Viewer,
        sections : SafeArray<Section>
    ) {
        super(random, layer, viewer, sections);
        this.settings = settings;
    }

    public function generate() : SafeArray<Section> {
        initialize();
        generateEverything();

        return sections;
    }

    private function initialize() : Void {
        scenarioSections = GeneratorUtils.computeScenarioSections(sections);

        heroHealth = Config.healthMax;
        heroArmor = Config.armorPickOne;
        heroAmmo = [for (i in 0 ... Config.ammo.length) None];
        heroWeapons = [];

        for (index in settings.availWeapons) {
            var weaponConfig = Config.weapons[index];
            heroWeapons.push(weaponConfig);

            switch (heroAmmo[weaponConfig.ammo]) {
                case None:
                    heroAmmo[weaponConfig.ammo] = Some(Config.ammo[weaponConfig.ammo].ensured);

                case Some(_):
            }
        }

        heroWeapons.sort((a, b) -> (b.dps - a.dps));
        availEnemies = [];

        for (index in settings.availEnemies) {
            availEnemies.push(Config.enemies[index]);
        }

        availEnemies.sort((a, b) -> (getEnemyCoolness(a) - getEnemyCoolness(b)));
    }

    private function getEnemyCoolness(enemyConfig : EnemyConfig) : Int {
        return (enemyConfig.health + enemyConfig.dps);
    }

    private function generateEverything() : Void {
        for (scenSectionIndex in 0 ... scenarioSections.length) {
            if (settings.argVerboseLevel >= 1) {
                dump(Actions, scenarioSections[scenSectionIndex]);
            }

            var placePoints : SafeArray<Pair<Point, Section>> = [];

            for (section in scenarioSections[scenSectionIndex]) {
                section.renderAvailInnerCells(layer, true);

                if (settings.argVerboseLevel >= 1) {
                    viewer.dumpIntLayer(layer);
                }

                layer.collect([Section.VAL_INNER_AVAIL], section.getBbox()).iter(
                    (point) -> placePoints.push(new Pair(point, section))
                );
            }

            sectionDifficulty = getSectionDifficulty(scenSectionIndex);

            for (item in generateForSection(Std.int(placePoints.length * 0.75))) {
                var placePoint = random.nextFromArray(placePoints);

                if (placePoint == null) {
                    // Can happen sometimes
                    break;
                }

                placePoints.remove(placePoint);

                switch (item) {
                    case Left(v):
                        placePoint.second.enemies.push(new SectionEnemy(placePoint.first, v));

                    case Right(v):
                        placePoint.second.items.push(new SectionItem(placePoint.first, v));
                }
            }

            dump(Actions, scenarioSections[scenSectionIndex]);
        }
    }

    private function generateForSection(desiredCount : Int) : SafeArray<Either<Int, SectionItemType>> {
        var result : SafeArray<Either<Int, SectionItemType>> = [];

        minHeroHealth = getDifficultedValue(Math.ceil(Config.healthMax * 0.5), Config.healthMax);
        minHeroArmor = getDifficultedValue(Math.ceil(Config.armorPickOne * 0.25), Config.armorPickOne);

        minHeroAmmo = Config.ammo.mapi((index, ammo) -> switch (heroAmmo[index]) {
            case Some(v): getDifficultedValue(
                Math.ceil(ammo.max * 0.25),
                IntMath.max(Math.ceil(ammo.max * 0.5), v + ammo.pickBox)
            );

            case None: 0;
        });

        var desiredEnemiesCount : Int = Math.ceil(desiredCount * sectionDifficulty);

        if (settings.argVerboseLevel >= 1) {
            viewer.log('[!!!!] Generating: desiredCount = ${desiredCount}, sectionDifficulty = ${sectionDifficulty}, heroHealth = ${heroHealth}, heroArmor = ${heroArmor}, heroAmmo = ${heroAmmo}, minHeroHealth = ${minHeroHealth}, minHeroArmor = ${minHeroArmor}, minHeroAmmo = ${minHeroAmmo}, desiredEnemiesCount = ${desiredEnemiesCount}');
        }

        var enemiesCount : Int = 0;

        while (result.length < desiredCount) {
            var hasChanges = false;

            if (sectionDifficulty >= DIFFICULTY_EPSILON
                && enemiesCount < desiredEnemiesCount
                && generateEnemyForSection(result, desiredEnemiesCount - enemiesCount)
            ) {
                hasChanges = true;
                enemiesCount++;
            }

            if (generateItemsForSection(result)) {
                hasChanges = true;
            }

            if (!hasChanges) {
                break;
            }
        }

        result = prettifyHealthBoxes(result);

        if (settings.argVerboseLevel >= 1) {
            var statsMap = new BalancedTree<String, Int>();

            result.iter((item) -> {
                var key = switch (item) {
                    case Left(v): 'enemy:${v + 1}';

                    case Right(v): switch (v) {
                        case Health(box): 'health:' + (box ? 'box' : 'one');
                        case Armor(box): 'armor:' + (box ? 'box' : 'one');
                        case Ammo(ammo, box): 'ammo:${ammo}:' + (box ? 'box' : 'one');
                        case Backpack: 'backpack';
                        case OpenMap: 'openmap';
                        case Weapon(weapon): 'weapon:${weapon}';
                    }
                };

                var val = statsMap.get(key);
                statsMap.set(key, val == null ? 1 : val + 1);
            });

            viewer.log('> generated ${result.length} things --> ${statsMap} --> heroHealth = ${heroHealth}, heroArmor = ${heroArmor}, heroAmmo = ${heroAmmo}');
        }

        return result;
    }

    private function prettifyHealthBoxes(result : SafeArray<Either<Int, SectionItemType>>) : SafeArray<Either<Int, SectionItemType>> {
        var smallHealthBoxes : Int = result.fold((item, carry) -> switch (item) {
            case Right(Health(false)): carry + 1;
            default: carry;
        }, 0);

        var bigHealthBoxes = Math.floor((smallHealthBoxes * Config.healthPickOne) / Config.healthPickBox);

        if (settings.argVerboseLevel >= 2) {
            viewer.log('>> smallHealthBoxes = ${smallHealthBoxes}, bigHealthBoxes = ${bigHealthBoxes}');
        }

        if (bigHealthBoxes <= 0) {
            return result;
        }

        smallHealthBoxes -= Math.floor((bigHealthBoxes * Config.healthPickBox) / Config.healthPickOne);

        result = result.filter(item -> switch (item) {
            case Right(Health(false)): (--smallHealthBoxes >= 0);
            default: true;
        });

        for (i in 0 ... bigHealthBoxes) {
            result.push(Right(SectionItemType.Health(true)));
        }

        return result;
    }

    private function generateEnemyForSection(result : SafeArray<Either<Int, SectionItemType>>, enemiesLeft : Int) : Bool {
        var applicableEnemies = availEnemies.map((enemyConfig) -> {
            return new Pair<EnemyConfig, Null<EnemyGeneratorFightResult>>(enemyConfig, computeFightResult(enemyConfig));
        }).filter((pair) -> {
            return (pair.second != null);
        });

        if (applicableEnemies.isEmpty()) {
            return false;
        }

        var maxIndex = applicableEnemies.length - 1;

        // Если надо ещё очень много врагов, то уменьшим maxIndex, чтоб генерить более слабых
        if (enemiesLeft >= availEnemies.length * 2) {
            var midIndex = IntMath.max(0, Std.int(maxIndex - enemiesLeft / (availEnemies.length * 2)));

            if (random.nextFloatEx() >= 0.8) {
                maxIndex = random.nextIntRangeIn(midIndex, maxIndex);
            } else {
                maxIndex = midIndex;
            }
        }

        var minIndex = IntMath.max(0, maxIndex - enemiesLeft);
        var pair = applicableEnemies[maxIndex - Std.int(random.nextFloatEx() * (maxIndex - minIndex + 1))];

        var enemyConfig = pair.first;
        var fightResult = pair.second.sure();

        heroHealth = IntMath.max(0, heroHealth - fightResult.healthLoss);
        heroArmor = IntMath.max(0, heroArmor - fightResult.armorLoss);

        switch (heroAmmo[fightResult.weaponConfig.ammo]) {
            case Some(v):
                heroAmmo[fightResult.weaponConfig.ammo] = Some(IntMath.max(0, v - fightResult.ammoLoss));

            case None:
        }

        result.push(Left(enemyConfig.type));

        // Будем считать что игрок поднял патроны, выпавшие из врага
        if (!Config.ammo[enemyConfig.ammo].infinite) {
            refillAmmo(enemyConfig.ammo, Config.ammo[enemyConfig.ammo].pickOne, true, fightResult);
        }

        if (settings.argVerboseLevel >= 2) {
            viewer.log('>> enemy = ${enemyConfig.type + 1} --> ${fightResult.debugInfo} --> heroHealth = ${heroHealth}, heroArmor = ${heroArmor}, heroAmmo = ${heroAmmo}');
        }

        return true;
    }

    private function generateItemsForSection(result : SafeArray<Either<Int, SectionItemType>>) : Bool {
        var hasChanges = false;

        // Сначала генерируется броник
        if (generateArmorForSection(result)) {
            hasChanges = true;
        }

        // После броника (но перед генерацией аптечек) генерируем оружко, т.к. может сгенериться backpack, который подправит здоровье
        if (generateAmmoForSection(result)) {
            hasChanges = true;
        }

        // Самым последним генерим аптечки
        if (generateHealthForSection(result)) {
            hasChanges = true;
        }

        return hasChanges;
    }

    private function generateArmorForSection(result : SafeArray<Either<Int, SectionItemType>>) : Bool {
        if (heroArmor >= minHeroArmor) {
            return false;
        }

        if (heroArmor < minHeroArmor * 0.5 || (heroArmor < Config.armorPickOne * 0.5 && random.nextBool())) {
            result.push(Right(SectionItemType.Armor(true)));
            heroArmor = IntMath.min(Config.armorMax, heroArmor + Config.armorPickBox);

            if (settings.argVerboseLevel >= 2) {
                viewer.log('>> armor box, heroArmor = ${heroArmor}');
            }

            return true;
        }

        if (random.nextBool()) {
            result.push(Right(SectionItemType.Armor(false)));
            heroArmor = IntMath.min(Config.armorMax, heroArmor + Config.armorPickOne);

            if (settings.argVerboseLevel >= 2) {
                viewer.log('>> armor one, heroArmor = ${heroArmor}');
            }

            return true;
        }

        return false;
    }

    private function generateAmmoForSection(result : SafeArray<Either<Int, SectionItemType>>) : Bool {
        if (hasAnyAmmo()) {
            return false;
        }

        if (generateBackpackForSection(result)) {
            return true;
        }

        var iterations = heroAmmo.length;
        var ammoValue = 0;

        while (true) {
            generatedAmmoIndex = (generatedAmmoIndex + heroAmmo.length - 1) % heroAmmo.length;

            if (!Config.ammo[generatedAmmoIndex].infinite) {
                switch (heroAmmo[generatedAmmoIndex]) {
                    case Some(v):
                        ammoValue = v;

                        if (ammoValue < minHeroAmmo[generatedAmmoIndex]) {
                            break;
                        }

                    case None:
                }
            }

            iterations--;

            if (iterations < 0) {
                return false;
            }
        }

        if (minHeroAmmo[generatedAmmoIndex] - ammoValue > Config.ammo[generatedAmmoIndex].pickOne) {
            result.push(Right(SectionItemType.Ammo(generatedAmmoIndex, true)));
            refillAmmo(generatedAmmoIndex, Config.ammo[generatedAmmoIndex].pickBox);

            if (settings.argVerboseLevel >= 2) {
                viewer.log('>> ammo box = ${generatedAmmoIndex}, heroAmmo = ${heroAmmo}');
            }

            return true;
        }

        if (random.nextBool()) {
            result.push(Right(SectionItemType.Ammo(generatedAmmoIndex, false)));
            refillAmmo(generatedAmmoIndex, Config.ammo[generatedAmmoIndex].pickOne);

            if (settings.argVerboseLevel >= 2) {
                viewer.log('>> ammo one = ${generatedAmmoIndex}, heroAmmo = ${heroAmmo}');
            }

            return true;
        }

        return false;
    }

    private function hasAnyAmmo() : Bool {
        for (i in 0 ... heroAmmo.length) {
            if (Config.ammo[i].infinite) {
                continue;
            }

            switch (heroAmmo[i]) {
                case Some(v):
                    if (v >= minHeroAmmo[i]) {
                        return true;
                    }

                case None:
            }
        }

        return false;
    }

    private function generateBackpackForSection(result : SafeArray<Either<Int, SectionItemType>>) : Bool {
        if (heroHealth >= minHeroHealth) {
            return false;
        }

        var totalBackpackedAmmo : Int = 0;
        var canRefillFromBackpack : Int = 0;
        var suitableForRefillFromBackpack : Int = 0;

        for (i in 0 ... heroAmmo.length) {
            if (Config.ammo[i].infinite || !Config.ammo[i].inBackpack) {
                continue;
            }

            switch (heroAmmo[i]) {
                case Some(v):
                    totalBackpackedAmmo++;

                    if (v < minHeroAmmo[i]) {
                        canRefillFromBackpack++;

                        if (minHeroAmmo[i] - v <= Config.ammo[i].pickOne) {
                            suitableForRefillFromBackpack++;
                        }
                    }

                case None:
            }
        }

        if (totalBackpackedAmmo != canRefillFromBackpack) {
            return false;
        }

        var probability = random.nextFloatEx();

        if (probability < 0.5
            || (probability < 0.75
                && ((Config.healthMax - heroHealth > Config.healthPickOne)
                    || totalBackpackedAmmo != suitableForRefillFromBackpack
                )
            )
        ) {
            return false;
        }

        result.push(Right(SectionItemType.Backpack));
        heroHealth = IntMath.min(Config.healthMax, heroHealth + Config.healthPickOne);

        for (i in 0 ... heroAmmo.length) {
            if (Config.ammo[i].infinite || !Config.ammo[i].inBackpack) {
                continue;
            }

            refillAmmo(i, Config.ammo[i].pickOne, true);
        }

        if (settings.argVerboseLevel >= 2) {
            viewer.log('>> backpack, heroHealth = ${heroHealth}, heroAmmo = ${heroAmmo}');
        }

        return true;
    }

    private function generateHealthForSection(result : SafeArray<Either<Int, SectionItemType>>) : Bool {
        if (heroHealth >= minHeroHealth) {
            return false;
        }

        if (heroHealth < minHeroHealth * 0.5 || (Config.healthMax - heroHealth >= Config.healthPickBox && random.nextBool())) {
            result.push(Right(SectionItemType.Health(true)));
            heroHealth += Config.healthPickBox;

            if (settings.argVerboseLevel >= 2) {
                viewer.log('>> health box, heroHealth = ${heroHealth}');
            }

            return true;
        }

        if (random.nextBool()) {
            result.push(Right(SectionItemType.Health(false)));
            heroHealth += Config.healthPickOne;

            if (settings.argVerboseLevel >= 2) {
                viewer.log('>> health one, heroHealth = ${heroHealth}');
            }

            return true;
        }

        return false;
    }

    private function refillAmmo(index : Int, amount : Int, skipNonExisting : Bool = false, ?fightResult : EnemyGeneratorFightResult) {
        switch (heroAmmo[index]) {
            case Some(v):
                heroAmmo[index] = Some(IntMath.min(Config.ammo[index].max, v + amount));

                if (fightResult != null) {
                    fightResult.debugInfo += ' --> ammo:${index}:refill = ${amount}';
                }

            case None:
                if (!skipNonExisting) {
                    throw new GeneratorException("refillAmmo failed: hero has no such ammo");
                }
        }
    }

    private function getSectionDifficulty(index : Int) : Float {
        if (index == 0 || scenarioSections.length < 2) {
            return 0.0;
        }

        var result = if (scenarioSections.length <= 2) {
            random.nextFloatRangeIn(0.5, 1.0);
        } else {
            var x = (index - 1) / (scenarioSections.length - 2);
            (x <= 0.5) ? (x * 1.5 + 0.25) : ((1.0 - x) * 0.5 + 0.75);
        };

        return Math.floor(result * 100000000.0) / 100000000.0;
    }

    private function computeFightResult(enemyConfig : EnemyConfig) : Null<EnemyGeneratorFightResult> {
        // При settings.difficulty = 0.75 играть можно, но очень сложно,
        // тогда как при settings.difficulty = 1.0 играть почти нереально.
        // Делаем поправку (небольшую, чтобы "сгладить" это).
        var fightDifficulty = (settings.difficulty > 0.75) ? (settings.difficulty * 1.25) : settings.difficulty;

        var selectedWeaponConfig : Null<WeaponConfig> = null;
        var fightTime : Float = 0.0;
        var ammoLoss : Int = 0;

        for (weaponConfig in heroWeapons) {
            var isInfinite = Config.ammo[weaponConfig.ammo].infinite;

            fightTime = Math.floor(enemyConfig.health * Math.max(1.0, fightDifficulty)) / weaponConfig.dps;
            ammoLoss = (isInfinite ? 0 : Math.ceil(fightTime * weaponConfig.spd) + weaponConfig.additionalLoss);

            if (isInfinite) {
                selectedWeaponConfig = weaponConfig;
                break;
            }

            switch (heroAmmo[weaponConfig.ammo]) {
                case Some(v):
                    if (v >= ammoLoss) {
                        selectedWeaponConfig = weaponConfig;
                        break;
                    }

                case None:
            }
        }

        if (selectedWeaponConfig == null) {
            return null;
        }

        var enemyDps : Int = Math.floor(enemyConfig.dps * Math.min(1.0, fightDifficulty));

        // Если есть в игрока стреляют несколько врагов, то он потеряет больше здоровья, чем было рассчитано.
        // Для поправки считаем что враг стреляет в игрока на 0.5 сек дольше.
        var enemyFightTime : Float = fightTime * Math.max(0.0, 1.0 - selectedWeaponConfig.stun) + 0.5;

        var hits : Int = Math.ceil(Math.ceil(enemyFightTime * HEALTH_GRANULARITY) / HEALTH_GRANULARITY * enemyDps);
        var armorLoss : Int = IntMath.min(heroArmor, Math.ceil(hits * Config.armorHitTaker));
        var armorSavedHits = IntMath.min(hits, Math.floor(armorLoss / Config.armorHitTaker));
        var healthLoss : Int = (hits - armorSavedHits) + Math.ceil(armorSavedHits * Config.armorHealthSaver);

        if (healthLoss >= heroHealth) {
            return null;
        }

        var debugInfo = 'heroHealth = ${heroHealth}, heroArmor = ${heroArmor}, heroAmmo = ${heroAmmo}, enemyConfig = ${enemyConfig}, selectedWeaponConfig = ${selectedWeaponConfig}, fightTime = ${fightTime}, ammoLoss = ${ammoLoss}, enemyDps = ${enemyDps}, enemyFightTime = ${enemyFightTime}, hits = ${hits}, armorLoss = ${armorLoss}, armorSavedHits = ${armorSavedHits}, healthLoss = ${healthLoss}';

        if (settings.argVerboseLevel >= 3) {
            viewer.log('Fight: ${debugInfo}');
        }

        return new EnemyGeneratorFightResult(selectedWeaponConfig, healthLoss, armorLoss, ammoLoss, debugInfo);
    }

    private function getDifficultedValue(minValue : Int, maxValue : Int) : Int {
        var difficultedValue : Int = Math.ceil(minValue + (maxValue - minValue) * (1.0 - sectionDifficulty));
        return IntMath.max(minValue, IntMath.min(maxValue, difficultedValue));
    }
}
