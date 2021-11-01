package mara.mybox.fxml;

import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-7-30
 * @License Apache License Version 2.0
 */
public class StyleCheckBox {

    public static StyleData checkBoxStyle(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        if (id.startsWith("coordinate")) {
            return new StyleData(id, "", message("Coordinate"), "", "iconLocation.png");
        }
        if (id.startsWith("refreshSwitch")) {
            return new StyleData(id, "", message("RefreshWhenSwitch"), "", "iconRefreshSwitch.png");
        }
        if (id.startsWith("refreshChange")) {
            return new StyleData(id, "", message("RefreshWhenChange"), "", "iconRefresh.png");
        }
        if (id.startsWith("wrap")) {
            return new StyleData(id, "", message("Wrap"), "", "iconWrap.png");
        }
        switch (id) {
            case "tableSubdirCheck":
                return new StyleData("tableSubdirCheck", "", message("HandleSubDirectories"), "", "iconFolder.png");
            case "tableExpandDirCheck":
                return new StyleData("tableExpandDirCheck", "", message("ExpandDirectories"), "", "iconUnfold.png");
            case "tableCreateDirCheck":
                return new StyleData("tableCreateDirCheck", "", message("CreateDirectories"), "", "iconFolderLink.png");
            case "countDirCheck":
                return new StyleData("countDirCheck", "", message("CountFilesUnderFolders"), "", "iconFolderBrowse.png");
            case "tableThumbCheck":
                return new StyleData("tableThumbCheck", "", message("Thumbnail"), "", "iconThumbsList.png");
            case "miaoCheck":
                return new StyleData("miaoCheck", message("Meow"), message("MiaoPrompt"), "", "iconCat.png");
            case "pdfMemBox":
                return new StyleData("pdfMemBox", "", message("PdfMemComments"), "", "");
            case "openCheck":
                return new StyleData("openCheck", "", message("OpenWhenComplete"), "", "iconOpen2.png");
            case "selectAreaCheck":
                return new StyleData("selectAreaCheck", "", message("SelectArea"), "CTRL+t / ALT+t", "iconTarget.png");
            case "bookmarksCheck":
                return new StyleData("bookmarksCheck", "", message("Bookmarks"), "", "iconTree.png");
            case "thumbCheck":
                return new StyleData("thumbCheck", "", message("Thumbnails"), "", "iconBrowse.png");
            case "rulerXCheck":
                return new StyleData("rulerXCheck", "", message("RulerX"), "", "iconXRuler.png");
            case "rulerYCheck":
                return new StyleData("rulerYCheck", "", message("RulerY"), "", "iconYRuler.png");
            case "statisticCheck":
                return new StyleData("statisticCheck", "", message("Statistic"), "", "iconStatistic.png");
            case "transparentBackgroundCheck":
                return new StyleData(id, "", message("TransparentBackground"), "", "iconOpacity.png");
            case "transparentCheck":
                return new StyleData(id, "", message("CountTransparent"), "", "iconOpacity.png");
            case "displaySizeCheck":
                return new StyleData("displaySizeCheck", "", message("DisplaySize"), "", "iconIdea.png");
            case "topCheck":
                return new StyleData("topCheck", "", message("AlwayOnTop"), "", "iconTop.png");
            case "saveCloseCheck":
                return new StyleData("saveCloseCheck", "", message("CloseAfterHandled"), "", "iconFlower.png");
            case "deskewCheck":
                return new StyleData("deskewCheck", "", message("Deskew"), "", "iconShear.png");
            case "invertCheck":
                return new StyleData("invertCheck", "", message("Invert"), "", "iconInvert.png");
            case "popCheck":
                return new StyleData(id, message("Pop"), "", "iconPop.png");
            case "pickColorCheck":
                return new StyleData(id, message("PickColor"), message("ColorPickerComments"), "CTRL+k / ALT+k", "iconPickColor.png");
            case "ditherCheck":
                return new StyleData(id, message("DitherComments"), "", "");
            case "withNamesCheck":
            case "sourceWithNamesCheck":
            case "targetWithNamesCheck":
                return new StyleData(id, "", message("FirstLineAsNamesComments"), "", "");
            default:
                return null;
        }
    }

}
