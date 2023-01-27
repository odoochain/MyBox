/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import com.sun.management.OperatingSystemMXBean;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Map;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import mara.mybox.db.DerbyBase;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-6-20
 * @Description
 * @License Apache License Version 2.0
 */
public class MyBoxPropertiesController extends HtmlTableController {

    public MyBoxPropertiesController() {
        baseTitle = message("JvmProperties");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            initTable(null);

            addData(message("UserName"), System.getProperty("user.name"));
            addData(message("UserHome"), System.getProperty("user.home"));
            addData(message("UserDir"), System.getProperty("user.dir"));
            addData(message("MyBoxDataPath"), AppVariables.MyboxDataPath);
            addData(message("MyBoxDatabase"),
                    DerbyBase.protocol + "<BR>" + DerbyBase.dbHome() + "<BR>" + DerbyBase.login);
            addData(message("JavaVersion"), System.getProperty("java.version"));
            addData(message("JavaVendor"), System.getProperty("java.vendor"));
            addData(message("JvmName"), System.getProperty("java.vm.name"));
            addData(message("JvmInfo"), System.getProperty("java.vm.info"));
            addData(message("JavaHome"), System.getProperty("java.home"));
            addData(message("JavaIOTmpdir"), System.getProperty("java.io.tmpdir"));
            addData(message("JavafxRuntimeVersion"), System.getProperty("javafx.runtime.version"));

            int mb = 1024 * 1024;
            OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            addData(message("PhysicalMemory"), osmxb.getTotalMemorySize() / mb + "MB");
            Runtime r = Runtime.getRuntime();
            addData(message("JvmXmx"), r.maxMemory() / mb + "MB");

            addData(message("SunJnuEncoding"), System.getProperty("sun.jnu.encoding"));
            addData(message("IOUnicodeEncoding"), System.getProperty("sun.io.unicode.encoding"));
            addData(message("CPUEndian"), System.getProperty("sun.cpu.endian"));
            addData(message("SunDesktop"), System.getProperty("sun.desktop"));

            // https://stackoverflow.com/questions/48915229/how-to-make-javafx-tell-if-im-on-a-4k-or-1080p-screen?r=SearchResults
            DisplayMode dm = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
            addData(message("PhysicalScreen"),
                    MessageFormat.format(message("PhysicalScreenValue"), NodeTools.screenResolution(),
                            dm.getWidth(), dm.getHeight(), dm.getRefreshRate(), dm.getBitDepth()));

            // https://stackoverflow.com/questions/28817460/how-do-i-find-out-whether-my-program-is-running-on-a-retina-screen?r=SearchResults
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getBounds();
            addData(message("JavaFxScreen"),
                    MessageFormat.format(message("JavaFxScreenValue"), screen.getDpi(),
                            bounds.getWidth(), bounds.getHeight(),
                            screen.getOutputScaleX(), screen.getOutputScaleY()));

            addData("WebView", WebViewTools.userAgent());

            addData(message("FileEncoding"), System.getProperty("file.encoding"));
            addData(message("NativeEncoding"), System.getProperty("native.encoding"));
            addData(message("DefaultCharset"), Charset.defaultCharset().name());
            addData(message("UserCountry"), System.getProperty("user.country"));
            addData(message("UserLanguage"), System.getProperty("user.language"));
            addData(message("FileSeparator"), System.getProperty("file.separator"));
            addData(message("OSName"), System.getProperty("os.name"));
            addData(message("OSVersion"), System.getProperty("os.version"));
            addData(message("OSArch"), System.getProperty("os.arch"));

            addData(message("ClassPaths"), System.getProperty("java.class.path"));
            addData(message("LibraryPaths"), System.getProperty("java.library.path"));

            String envString = "";
            Map<String, String> env = System.getenv();
            for (String k : env.keySet()) {
                envString += k + "=" + env.get(k) + "</BR>\n";
            }
            addData(message("EnvrionmentVariables"), envString);

            displayHtml();

        } catch (Exception e) {

        }
    }

}
