/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.core;

import com.wynntils.McIf;
import com.wynntils.core.framework.enums.wynntils.WynntilsConflictContext;
import com.wynntils.core.framework.instances.PlayerInfo;

public class CoreManager {

    /**
     * Called before all modules are registered
     */
    public static void preModules() {

        McIf.mc().options.keyUp.setKeyConflictContext(WynntilsConflictContext.ALLOW_MOVEMENTS);
        McIf.mc().options.keyDown.setKeyConflictContext(WynntilsConflictContext.ALLOW_MOVEMENTS);
        McIf.mc().options.keyRight.setKeyConflictContext(WynntilsConflictContext.ALLOW_MOVEMENTS);
        McIf.mc().options.keyLeft.setKeyConflictContext(WynntilsConflictContext.ALLOW_MOVEMENTS);
    }

    /**
     * Called after all modules are registered
     */
    public static void afterModules() {
        PlayerInfo.setup();
    }

}
