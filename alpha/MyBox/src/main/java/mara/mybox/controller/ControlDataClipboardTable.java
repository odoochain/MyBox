package mara.mybox.controller;

import java.io.File;
import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.value.AppPaths;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-3-11
 * @License Apache License Version 2.0
 */
public class ControlDataClipboardTable extends ControlData2DList {

    public ControlDataClipboardTable() {
        baseTitle = message("DataClipboard");
        TipsLabelKey = "Data2DTips";
    }

    @Override
    protected int deleteData(List<Data2DDefinition> data) {
        if (data == null || data.isEmpty()) {
            return 0;
        }
        for (Data2DDefinition d : data) {
            FileDeleteTools.delete(d.getFile());
        }
        return data2D.getTableData2DDefinition().deleteData(data);
    }

    @Override
    protected void afterDeletion() {
        refreshAction();
        if (data2D.getFile() != null && !data2D.getFile().exists()) {
            data2DController.dataController.loadNull();
        }
    }

    @Override
    protected void afterClear() {
        super.afterClear();
        FileDeleteTools.clearDir(new File(AppPaths.getDataClipboardPath()));
        data2DController.dataController.loadNull();
    }

    @FXML
    public void openAction() {
        try {
            browseURI(new File(AppPaths.getDataClipboardPath() + File.separator).toURI());
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
