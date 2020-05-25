package mara.mybox.data;

import java.util.ArrayList;
import java.util.List;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2020-3-21
 * @License Apache License Version 2.0
 */
public class GeographyCodeLevel {

    private static List<GeographyCodeLevel> Levels;

    protected int level;
    protected String chineseName, englishName, name, key;

    public GeographyCodeLevel() {
    }

    public GeographyCodeLevel(int value) {
        level = value;
        switch (value) {
            case 1:
                chineseName = "全球";
                englishName = "Global";
                break;
            case 2:
                chineseName = "洲";
                englishName = "Continent";
                break;
            case 3:
                chineseName = "国家";
                englishName = "Country";
                break;
            case 4:
                chineseName = "省";
                englishName = "Province";
                break;
            case 5:
                chineseName = "市";
                englishName = "City";
                break;
            case 6:
                chineseName = "县";
                englishName = "County";
                break;
            case 7:
                chineseName = "镇";
                englishName = "Town";
                break;
            case 8:
                chineseName = "村";
                englishName = "Village";
                break;
            case 9:
                chineseName = "建筑";
                englishName = "Building";
                break;
            case 10:
                chineseName = "兴趣点";
                englishName = "Point Of Interest";
                break;
        }
        key = getKey(level);
    }

    public GeographyCodeLevel(String name) {
        switch (name) {
            case "全球":
                englishName = "Global";
                chineseName = name;
                level = 1;
                break;
            case "Global":
                englishName = name;
                chineseName = "全球";
                level = 1;
                break;
            case "洲":
                englishName = "Continent";
                chineseName = name;
                level = 2;
                break;
            case "Continent":
                englishName = name;
                chineseName = "洲";
                level = 2;
                break;
            case "国家":
                englishName = "Country";
                chineseName = name;
                level = 3;
                break;
            case "Country":
                englishName = name;
                chineseName = "国家";
                level = 3;
                break;
            case "省":
                englishName = "Province";
                chineseName = name;
                level = 4;
                break;
            case "Province":
                englishName = name;
                chineseName = "省";
                level = 4;
                break;
            case "市":
                englishName = "City";
                chineseName = name;
                level = 5;
                break;
            case "City":
                englishName = name;
                chineseName = "市";
                level = 5;
                break;
            case "县":
                englishName = "County";
                chineseName = name;
                level = 6;
                break;
            case "County":
                englishName = name;
                chineseName = "县";
                level = 6;
                break;
            case "镇":
                englishName = "Town";
                chineseName = name;
                level = 7;
                break;
            case "Town":
                englishName = name;
                chineseName = "镇";
                level = 7;
                break;
            case "村":
                englishName = "Village";
                chineseName = name;
                level = 8;
                break;
            case "Village":
                englishName = name;
                chineseName = "村";
                level = 8;
                break;
            case "建筑":
                englishName = "Building";
                chineseName = name;
                level = 9;
                break;
            case "Building":
                englishName = name;
                chineseName = "建筑";
                level = 9;
                break;
            case "兴趣点":
                englishName = "Point Of Interest";
                chineseName = name;
                level = 10;
                break;
            case "Point Of Interest":
                englishName = name;
                chineseName = "兴趣点";
                level = 10;
                break;
            default:
                chineseName = name;
                level = -1;
        }
        key = getKey(level);
    }

    public static GeographyCodeLevel create(int value,
            String chineseName, String englishName) {
        GeographyCodeLevel level = new GeographyCodeLevel();
        level.setLevel(value);
        level.setChineseName(chineseName);
        level.setEnglishName(englishName);
        level.getKey();
        return level;
    }

    public static List<GeographyCodeLevel> levels() {
        if (Levels != null) {
            return Levels;
        }
        Levels = new ArrayList<>();
        Levels.add(new GeographyCodeLevel(1));
        Levels.add(new GeographyCodeLevel(2));
        Levels.add(new GeographyCodeLevel(3));
        Levels.add(new GeographyCodeLevel(4));
        Levels.add(new GeographyCodeLevel(5));
        Levels.add(new GeographyCodeLevel(6));
        Levels.add(new GeographyCodeLevel(7));
        Levels.add(new GeographyCodeLevel(8));
        Levels.add(new GeographyCodeLevel(9));
        Levels.add(new GeographyCodeLevel(10));
        return Levels;
    }

    public static int level(String name) {
        GeographyCodeLevel geolevel = new GeographyCodeLevel(name);
        return geolevel.level;
    }

    public static String level(int level) {
        GeographyCodeLevel geolevel = new GeographyCodeLevel(level);
        return geolevel.getName();
    }

    public static String getKey(int level) {
        String key;
        switch (level) {
            case 1:
                key = null;
                break;
            case 2:
                key = "continent";
                break;
            case 3:
                key = "country";
                break;
            case 4:
                key = "province";
                break;
            case 5:
                key = "city";
                break;
            case 6:
                key = "county";
                break;
            case 7:
                key = "town";
                break;
            case 8:
                key = "village";
                break;
            case 9:
                key = "building";
                break;
            default:
                key = null;
                break;
        }
        return key;
    }

    /*
        customzied get/set
     */
    public String getKey() {
        key = getKey(level);
        return key;
    }

    /*
        get/set
     */
    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getChineseName() {
        return chineseName;
    }

    public void setChineseName(String chineseName) {
        this.chineseName = chineseName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public String getName() {
        return AppVariables.isChinese() ? chineseName : englishName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static List<GeographyCodeLevel> getLevels() {
        return Levels;
    }

    public static void setLevels(List<GeographyCodeLevel> Levels) {
        GeographyCodeLevel.Levels = Levels;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
