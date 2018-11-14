/*
 * Copyright 2018 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.layers.mainMenu;

import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.engine.GameEngine;
import org.terasology.engine.modes.StateLoading;
import org.terasology.engine.paths.PathManager;
import org.terasology.game.GameManifest;
import org.terasology.network.NetworkMode;
import org.terasology.recording.RecordAndReplayCurrentStatus;
import org.terasology.recording.RecordAndReplayStatus;
import org.terasology.recording.RecordAndReplayUtils;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameInfo;
import org.terasology.rendering.nui.layers.mainMenu.savedGames.GameProvider;
import org.terasology.rendering.nui.widgets.UIButton;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Screen for the record menu.
 */
public class RecordScreen extends SelectionScreen {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:recordScreen");

    private static final Logger logger = LoggerFactory.getLogger(RecordScreen.class);

    private RecordAndReplayUtils recordAndReplayUtils;

    @In
    private RecordAndReplayCurrentStatus recordAndReplayCurrentStatus;

    // widgets
    private UIButton load;
    private UIButton close;


    @Override
    public void initialise() {
        initWidgets();

        if (isValidScreen()) {

            initSaveGamePathWidget(PathManager.getInstance().getSavesPath());

            getGameInfos().subscribeSelection((widget, item) -> {
                load.setEnabled(item != null);
                updateDescription(item);
            });

            getGameInfos().subscribe((widget, item) -> loadGame(item));

            load.subscribe(button -> {
                final GameInfo gameInfo = getGameInfos().getSelection();
                if (gameInfo != null) {
                    loadGame(gameInfo);
                }
            });

            close.subscribe(button -> {
                recordAndReplayCurrentStatus.setStatus(RecordAndReplayStatus.NOT_ACTIVATED);
                triggerBackAnimation();
            });
        }
    }

    @Override
    public void onOpened() {
        if (isValidScreen()) {
            refreshGameInfoList(GameProvider.getSavedGames());
        } else {
            final MessagePopup popup = getManager().createScreen(MessagePopup.ASSET_URI, MessagePopup.class);
            popup.setMessage(translationSystem.translate("${engine:menu#game-details-errors-message-title}"), translationSystem.translate("${engine:menu#game-details-errors-message-body}"));
            popup.subscribeButton(e -> triggerBackAnimation());
            getManager().pushScreen(popup);
            // disable child widgets
            setEnabled(false);
        }
    }

    @Override
    protected void initWidgets() {
        super.initWidgets();
        load = find("load", UIButton.class);
        close = find("close", UIButton.class);
    }

    private void loadGame(GameInfo item) {
        try {
            final GameManifest manifest = item.getManifest();

            String oldTitle = manifest.getTitle();
            String newTitle = oldTitle;

            copySaveDirectoryToRecordingLibrary(oldTitle, newTitle);
            recordAndReplayUtils.setGameTitle(newTitle);
            config.getWorldGeneration().setDefaultSeed(manifest.getSeed());
            config.getWorldGeneration().setWorldTitle(newTitle);
            CoreRegistry.get(GameEngine.class).changeState(new StateLoading(manifest, NetworkMode.NONE));
        } catch (Exception e) {
            logger.error("Failed to load saved game", e);
            getManager().pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage("Error Loading Game", e.getMessage());
        }
    }

    private void copySaveDirectoryToRecordingLibrary(String oldTitle, String newTitle) {
        File saveDirectory = new File(PathManager.getInstance().getSavePath(oldTitle).toString());
        Path destinationPath = PathManager.getInstance().getRecordingPath(newTitle);
        File destDirectory = new File(destinationPath.toString());
        try {
            FileUtils.copyDirectoryStructure(saveDirectory, destDirectory);
            if(oldTitle != newTitle) {
                rewriteGameTitle(destinationPath, newTitle);
            }
        } catch (Exception e) {
            logger.error("Error trying to copy the save directory:", e);
        }
    }

    private void rewriteGameTitle(Path destinationPath, String newTitle) throws IOException {
        GameManifest manifest = GameManifest.load(destinationPath.resolve(GameManifest.DEFAULT_FILE_NAME));
        manifest.setTitle(newTitle);
        GameManifest.save(destinationPath.resolve(GameManifest.DEFAULT_FILE_NAME), manifest);
    }


    void setRecordAndReplayUtils(RecordAndReplayUtils recordAndReplayUtils) {
        this.recordAndReplayUtils = recordAndReplayUtils;
    }

    @Override
    protected boolean isValidScreen() {
        if (Stream.of(load, close)
                .anyMatch(Objects::isNull) ||
                !super.isValidScreen()) {
            logger.error("Can't initialize screen correctly. At least one widget was missed!");
            return false;
        }
        return true;
    }

}
