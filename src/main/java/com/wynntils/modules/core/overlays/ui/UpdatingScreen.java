/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.overlays.ui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.wynntils.McIf;
import com.wynntils.Reference;
import com.wynntils.modules.core.CoreModule;
import com.wynntils.modules.core.config.CoreDBConfig;
import com.wynntils.modules.core.overlays.UpdateOverlay;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

public class UpdatingScreen extends Screen {

    private static final int DOT_TIME = 200;  // ms between "." -> ".." -> "..."

    private boolean failed = false;
    private boolean complete = false;
    private Button backButton;
    private float progress = 0f;

    public UpdatingScreen(boolean restartNow) {
    	super(new StringTextComponent("Updating Screen"));
        doUpdate(restartNow);
    }

    @Override
    public void init() {
    	ITextComponent buttonText = new StringTextComponent("");
        this.buttons.add(backButton = new Button(this.width / 2 - 100, this.height / 4 + 132, 200, 20, buttonText, (button) -> {McIf.mc().setScreen(null);}));
        updateText();
    }

    private void updateText() {
        backButton.setMessage((failed || complete) ? new StringTextComponent("Back") : new StringTextComponent("Cancel"));
    }

    private void doUpdate(boolean restartNow) {
        try {
            File directory = new File(Reference.MOD_STORAGE_ROOT, "updates");
            String url = UpdateOverlay.getUpdateDownloadUrl();
            String jarName = UpdateOverlay.getJarNameFromUrl(url);

            new Thread(() -> {
                downloadUpdate(url, directory, jarName);
                if (!failed) {
                    UpdateOverlay.scheduleCopyUpdateAtShutdown(jarName);
                    if (restartNow) {
                        McIf.mc().close();
                    }
                    complete = true;
                    updateText();
                }
            }, "Wynntils-update-downloader-thread").start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void downloadUpdate(String url, File directory, String jarName) {
        try {
            HttpURLConnection st = (HttpURLConnection) new URL(url).openConnection();
            st.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
            st.connect();

            if (st.getResponseCode() != HttpURLConnection.HTTP_OK) {
                failed = true;
                setChangelogs();
                updateText();
                Reference.LOGGER.error(url + " returned status code " + st.getResponseCode());
                return;
            }

            if (!directory.exists() && !directory.mkdirs()) {
                failed = true;
                setChangelogs();
                updateText();
                Reference.LOGGER.error("Couldn't create update file directory");
                return;
            }

            String[] urlParts = url.split("/");

            float fileLength = st.getContentLength();

            File fileSaved = new File(directory, URLDecoder.decode(urlParts[urlParts.length - 1], "UTF-8"));

            InputStream fis = st.getInputStream();
            try (OutputStream fos = new FileOutputStream(fileSaved)) {

                byte[] data = new byte[1024];
                long total = 0;
                int count;

                while ((count = fis.read(data)) != -1) {
                    if (McIf.mc().screen != UpdatingScreen.this) {
                        // Cancelled
                        fos.close();
                        fis.close();
                        failed = true;
                        setChangelogs();
                        return;
                    }

                    total += count;
                    progress = total / fileLength;
                    fos.write(data, 0, count);
                }

            }
            fis.close();

            if (McIf.mc().screen != UpdatingScreen.this) {
                failed = true;
                setChangelogs();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            failed = true;
            setChangelogs();
            updateText();
        }
    }

    private static final String[] DOTS = { ".", "..", "...", "...", "..." };

    private void setChangelogs() {
        if (failed && CoreDBConfig.INSTANCE.showChangelogs) {
            CoreDBConfig.INSTANCE.showChangelogs = false;
            CoreDBConfig.INSTANCE.saveSettings(CoreModule.getModule());
        }
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.renderBackground(matrix);

        if (failed) {
            setChangelogs();
            drawCenteredString(matrix, McIf.mc().font, TextFormatting.RED + "Update download failed", this.width/2, this.height/2, 0xFFFFFFFF);
        } else if (complete) {
            drawCenteredString(matrix, McIf.mc().font, TextFormatting.GREEN + "Update download complete", this.width/2, this.height/2, 0xFFFFFF);
        } else {
            int left = Math.max(this.width/2 - 100, 10);
            int right = Math.min(this.width/2 + 100, this.width - 10);
            int top = this.height/2 - 2 - MathHelper.ceil(McIf.mc().font.lineHeight / 2f);
            int bottom = this.height/2 + 2 + MathHelper.floor(McIf.mc().font.lineHeight / 2f);
            //TODO not sure what drawRect should be replaced with
//            drawRect(left - 1, top - 1, right + 1, bottom + 1, 0xFFC0C0C0);
            int progressPoint = MathHelper.clamp(MathHelper.floor(progress * (right - left) + left), left, right);
//            drawRect(left, top, progressPoint, bottom, 0xFFCB3D35);
//            drawRect(progressPoint, top, right, bottom, 0xFFFFFFFF);

            String label = String.format("%d%%", MathHelper.clamp(MathHelper.floor(progress * 100), 0, 100));
            drawString(matrix, McIf.mc().font, label, (this.width - McIf.mc().font.width(label))/2, top + 3, 0xFF000000);
            int x = (this.width - McIf.mc().font.width(String.format("Downloading %s", DOTS[DOTS.length - 1]))) / 2;
            String title = String.format("Downloading %s", DOTS[((int) (System.currentTimeMillis() % (DOT_TIME * DOTS.length))) / DOT_TIME]);
            drawString(matrix, McIf.mc().font, title, x, top - McIf.mc().font.lineHeight - 2, 0xFFFFFFFF);
        }

        super.render(matrix, mouseX, mouseY, partialTicks);
    }
}
