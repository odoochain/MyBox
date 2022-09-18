# [ReadMe in English](https://github.com/Mararsh/MyBox/tree/master/en)  ![ReadMe](https://mararsh.github.io/MyBox/iconGo.png)

# MyBox：简易工具集
这是利用JavaFx开发的图形化桌面应用，目标是提供简单易用的功能。免费开源。          

## 开发中的试用版            
2022-9-18 a6.6      

* 数学函数：计算、数据集、图      
* 数据的xyz图：三维散点图、曲面图      
* 归一化/百分比/比较条图可以分别设置“要计算的列”和“要复制的列”      
* 除了JavaScript管理界面，JavaScript的计算采用Nashorn作为引擎        
* 性能：图像播放器只预读一帧。      
* 多重线性回归-组合      
* 添加新数据行时可以输入数值        

[MyBox-a6.6-2022-9-18-win10-x64.zip](https://sourceforge.net/projects/mara-mybox/files/alpha/MyBox-a6.6-2022-9-18-win10-x64.zip)          


## 新内容
2022-8-31 版本6.5.9          

* 数据：简单线性回归-组合；多重线性回归；等值分组；按多列排序；示例数据保存为合适的类型；生成数据时可设置数据名；
赋值的新选项：小数位数、附加后缀、添加前缀；计算时可选如何处理非法数字。             
* 代码：分离处理逻辑和数据读写。                         
* 文档：ReadMe不再包含工具功能的介绍，更新各个工具的用户手册。                        
* 解决：运行“播放图像列表”时关闭窗口可能导致无限弹出错误从而只能重启计算机（!!抱歉）；计算整型的统计值时可能出错；
行过滤包含统计值名有的操作可能出错；简单线性回归的数据在编辑时错位了一列；箱线图对全部数据按行统计时没有结果；
输入文件/路径时若为空则应为非法而非保存的值；文件自动备份可能自动取消了。        
 
[此版本关闭的需求/问题列表](http://github.com/Mararsh/MyBox/issues?q=is%3Aissue+is%3Aclosed+milestone%3Av6.5.9)             
   

## 下载与运行
每个版本编译好的包已发布在[Releases](https://github.com/Mararsh/MyBox/releases)目录下（点击此项目主页的`releases`页签）。       

### 源码
[MyBox-6.5.9-src.zip](https://sourceforge.net/projects/mara-mybox/files/v6.5.9/MyBox-6.5.9-src.zip)   52M-       

关于源码的结构、编辑、和构建，请参考[开发指南](https://sourceforge.net/projects/mara-mybox/files/documents/MyBox-DevGuide-2.1-zh.pdf) 和
[打包步骤](https://mararsh.github.io/MyBox/pack_steps.html)       


### 自包含程序包
自包含的程序包无需java环境、无需安装、解包可用。     

| 平台 | 链接 | 大小 | 启动文件 |
| --- | --- | --- |  --- |
| win10 x64 | [MyBox-6.5.9-win10-x64.zip](https://sourceforge.net/projects/mara-mybox/files/v6.5.9/MyBox-6.5.9-win10-x64.zip)  | 220MB- | MyBox.exe |
| CentOS 7 x64 | [MyBox-6.5.9-CentOS7-x64.tar.gz](https://sourceforge.net/projects/mara-mybox/files/v6.5.9/MyBox-6.5.9-CentOS7-x64.tar.gz)  | 240MB-  | bin/MyBox  |
| mac x64| [MyBox-6.5.9-mac.dmg](https://sourceforge.net/projects/mara-mybox/files/v6.5.9/MyBox-6.5.9-mac.dmg)  | 240MB-  |  MyBox-6.5.9.app   |

双击或者用命令行执行包内的启动文件即可运行程序。可以把图片/文本/PDF文件的打开方式关联到MyBox，这样双击文件名就直接是用MyBox打开了。        
  

### Jar包
在已安装JRE或者JDK [Oracle jdk](https://www.oracle.com/technetwork/java/javase/downloads/index.html)或[open jdk](http://jdk.java.net/)均可）的环境下，可以下载jar包。       

| 平台 | 链接 | 大小 | 运行需要 |
| --- | --- | --- |  --- |
| win | [MyBox-6.5.9-win-jar.zip](https://sourceforge.net/projects/mara-mybox/files/v6.5.9/MyBox-6.5.9-win-jar.zip)  | 160MB- | Java 18或更高版本 |
| linux | [MyBox-6.5.9-linux-jar.zip](https://sourceforge.net/projects/mara-mybox/files/v6.5.9/MyBox-6.5.9-linux-jar.zip)  | 180MB-  | Java 18或更高版本 |
| mac | [MyBox-6.5.9-mac-jar.zip](https://sourceforge.net/projects/mara-mybox/files/v6.5.9/MyBox-6.5.9-mac-jar.zip)  |  170MB-  | Java 18或更高版本 |

执行以下命令来启动程序：       
<PRE><CODE>     java   -jar   MyBox-6.5.9.jar</CODE></PRE>       
程序可以跟一个文件名作为参数、以用MyBox直接打开此文件。例如以下命令是打开此图片：       
<PRE><CODE>     java   -jar   MyBox-6.5.9.jar   /tmp/a1.jpg</CODE></PRE>       

### 其它下载地址       
从云盘下载：  [https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F](https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F)       
从sourceforge下载：[https://sourceforge.net/projects/mara-mybox/files/](https://sourceforge.net/projects/mara-mybox/files/)      

### 限制
  
* 在某个输入法运行时，MyBox的窗口经常僵住。解决办法：禁用/卸载此输入法。       

## 版本迁移
1. 每个版本有自己的配置文件，新版本可以复制已安装版本的参数。       
2. 每个版本处理的所有数据都在它指向的“数据目录”下。多个版本可以指向同一数据目录。
3. MyBox向后兼容：新版本可以处理旧版本的数据目录。而不保证向前兼容：旧版本处理新版本的数据目录时可能出错。

## 配置<a id="Config" />
配置文件在"用户目录"下:       

| 平台 | MyBox配置文件的目录 |
| --- | --- |
| win | `C:\用户\用户名\mybox\MyBox_v6.5.9.ini`  |
| linux | `/home/用户名/mybox/MyBox_v6.5.9.ini` |
| mac | `/Users/用户名/mybox/MyBox_v6.5.9.ini` |       

可以临时改变配置文件：在命令行启动jar包时设置参数"config=\"配置文件名\""。       
利用“设置”功能也可以修改配置参数。       

# 资源地址       
| 内容 | 链接 |       
| --- | --- |
| 项目主页 | [https://github.com/Mararsh/MyBoxl](https://github.com/Mararsh/MyBox)   |
| 源代码和编译好的包 |  [https://github.com/Mararsh/MyBox/releases](https://github.com/Mararsh/MyBox/releases)  |
| 在线提交软件需求和问题报告 | [https://github.com/Mararsh/MyBox/issues](https://github.com/Mararsh/MyBox/issues) |
| 数据 | [https://github.com/Mararsh/MyBox_data](https://github.com/Mararsh/MyBox_data) |
| 文档 | [https://github.com/Mararsh/MyBox_documents](https://github.com/Mararsh/MyBox_documents) |
| 镜像 | [https://sourceforge.net/projects/mara-mybox/files/](https://sourceforge.net/projects/mara-mybox/files/) |
| 云盘 | [https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F](https://pan.baidu.com/s/1fWMRzym_jh075OCX0D8y8A#list/path=%2F) |       

# 文档       
|      文档名       | 版本    | 修改时间  |                                                                                                                                            英文                                                                                                                                               |                                                                                                                                            中文                                                                                                                                               |
|-------------------|---------|-----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 开发日志          | 6.5.9   | 2022-8-31 | [html](https://mararsh.github.io/MyBox/mybox_devLogs_en.html)                                                                                                                                                                                                                                 | [html](https://mararsh.github.io/MyBox/mybox_devLogs.html)                                                                                                                                                                                                                                    |
| 快捷键            | 6.5.6   | 2022-6-11 | [html](https://mararsh.github.io/MyBox/mybox_shortcuts_en.html)                                                                                                                                                                                                                               | [html](https://mararsh.github.io/MyBox/mybox_shortcuts.html)                                                                                                                                                                                                                                  |
| 打包步骤          | 6.3.3   | 2020-9-27 | [html](https://mararsh.github.io/MyBox/pack_steps_en.html)                                                                                                                                                                                                                                    | [html](https://mararsh.github.io/MyBox/pack_steps.html)                                                                                                                                                                                                                                       |
| 开发指南          | 2.1     | 2020-8-27 | [PDF](https://mararsh.github.io/MyBox_documents/en/MyBox-DevGuide-2.1-en.pdf)                                                                                                                                                                                                                 | [PDF](https://mararsh.github.io/MyBox_documents/zh/MyBox-DevGuide-2.1-zh.pdf)                                                                                                                                                                                                                 |
| 用户手册-综述     | 6.5.9   | 2022-8-31 | [html](https://mararsh.github.io/MyBox_documents/en/MyBox-6.5.9-Overview-en/MyBox-6.5.9-Overview-en.html) [PDF](https://mararsh.github.io/MyBox_documents/en/MyBox-6.5.9-Overview-en.pdf) [odt](https://mararsh.github.io/MyBox_documents/en/MyBox-6.5.9-Overview-en.odt)                     | [html](https://mararsh.github.io/MyBox_documents/zh/MyBox-6.5.9-Overview-zh/MyBox-6.5.9-Overview-zh.html) [PDF](https://mararsh.github.io/MyBox_documents/zh/MyBox-6.5.9-Overview-zh.pdf) [odt](https://mararsh.github.io/MyBox_documents/zh/MyBox-6.5.9-Overview-zh.odt)                     |
| 用户手册-文档工具 | 6.5.9   | 2022-8-31 | [html](https://mararsh.github.io/MyBox_documents/en/MyBox-6.5.9-DocumentTools-en/MyBox-6.5.9-DocumentTools-en.html) [PDF](https://mararsh.github.io/MyBox_documents/en/MyBox-6.5.9-DocumentTools-en.pdf) [odt](https://mararsh.github.io/MyBox_documents/en/MyBox-6.5.9-DocumentTools-en.odt) | [html](https://mararsh.github.io/MyBox_documents/zh/MyBox-6.5.9-DocumentTools-zh/MyBox-6.5.9-DocumentTools-zh.html) [PDF](https://mararsh.github.io/MyBox_documents/zh/MyBox-6.5.9-DocumentTools-zh.pdf) [odt](https://mararsh.github.io/MyBox_documents/zh/MyBox-6.5.9-DocumentTools-zh.odt) |
| 用户手册-图像工具 | 6.5.9   | 2022-8-31 | [html](https://mararsh.github.io/MyBox_documents/en/MyBox-6.5.9-ImageTools-en/MyBox-6.5.9-ImageTools-en.html) [PDF](https://mararsh.github.io/MyBox_documents/en/MyBox-6.5.9-ImageTools-en.pdf) [odt](https://mararsh.github.io/MyBox_documents/en/MyBox-6.5.9-ImageTools-en.odt)             | [html](https://mararsh.github.io/MyBox_documents/zh/MyBox-6.5.9-ImageTools-zh/MyBox-6.5.9-ImageTools-zh.html) [PDF](https://mararsh.github.io/MyBox_documents/zh/MyBox-6.5.9-ImageTools-zh.pdf) [odt](https://mararsh.github.io/MyBox_documents/zh/MyBox-6.5.9-ImageTools-zh.odt)             |
| 用户手册-文件工具 | 6.5.9   | 2022-8-31 | [html](https://mararsh.github.io/MyBox_documents/en/MyBox-6.5.9-FileTools-en/MyBox-6.5.9-FileTools-en.html) [PDF](https://mararsh.github.io/MyBox_documents/en/MyBox-6.5.9-FileTools-en.pdf) [odt](https://mararsh.github.io/MyBox_documents/en/MyBox-6.5.9-FileTools-en.odt)                 | [html](https://mararsh.github.io/MyBox_documents/zh/MyBox-6.5.9-FileTools-zh/MyBox-6.5.9-FileTools-zh.html) [PDF](https://mararsh.github.io/MyBox_documents/zh/MyBox-6.5.9-FileTools-zh.pdf) [odt](https://mararsh.github.io/MyBox_documents/zh/MyBox-6.5.9-FileTools-zh.odt)                 |
| 用户手册-网络工具 | 6.5.9   | 2022-8-31 | [html](https://mararsh.github.io/MyBox_documents/en/MyBox-6.5.9-NetworkTools-en/MyBox-6.5.9-NetworkTools-en.html) [PDF](https://mararsh.github.io/MyBox_documents/en/MyBox-6.5.9-NetworkTools-en.pdf) [odt](https://mararsh.github.io/MyBox_documents/en/MyBox-6.5.9-NetworkTools-en.odt)     | [html](https://mararsh.github.io/MyBox_documents/zh/MyBox-6.5.9-NetworkTools-zh/MyBox-6.5.9-NetworkTools-zh.html) [PDF](https://mararsh.github.io/MyBox_documents/zh/MyBox-6.5.9-NetworkTools-zh.pdf) [odt](https://mararsh.github.io/MyBox_documents/zh/MyBox-6.5.9-NetworkTools-zh.odt)     |
| 用户手册-数据工具 | 6.5.9   | 2022-8-31 | [html](https://mararsh.github.io/MyBox_documents/en/MyBox-6.5.9-DataTools-en/MyBox-6.5.9-DataTools-en.html) [PDF](https://mararsh.github.io/MyBox_documents/en/MyBox-6.5.9-DataTools-en.pdf) [odt](https://mararsh.github.io/MyBox_documents/en/MyBox-6.5.9-DataTools-en.odt)                 | [html](https://mararsh.github.io/MyBox_documents/zh/MyBox-6.5.9-DataTools-zh/MyBox-6.5.9-DataTools-zh.html) [PDF](https://mararsh.github.io/MyBox_documents/zh/MyBox-6.5.9-DataTools-zh.pdf) [odt](https://mararsh.github.io/MyBox_documents/zh/MyBox-6.5.9-DataTools-zh.odt)                 |
| 用户手册-媒体工具 | 6.5.9   | 2022-8-31 | [html](https://mararsh.github.io/MyBox_documents/en/MyBox-6.5.9-MediaTools-en/MyBox-6.5.9-MediaTools-en.html) [PDF](https://mararsh.github.io/MyBox_documents/en/MyBox-6.5.9-MediaTools-en.pdf) [odt](https://mararsh.github.io/MyBox_documents/en/MyBox-6.5.9-MediaTools-en.odt)             | [html](https://mararsh.github.io/MyBox_documents/zh/MyBox-6.5.9-MediaTools-zh/MyBox-6.5.9-MediaTools-zh.html) [PDF](https://mararsh.github.io/MyBox_documents/zh/MyBox-6.5.9-MediaTools-zh.pdf) [odt](https://mararsh.github.io/MyBox_documents/zh/MyBox-6.5.9-MediaTools-zh.odt)             |
| 用户手册-开发工具 | 6.5.9   | 2022-8-31 | [html](https://mararsh.github.io/MyBox_documents/en/MyBox-6.5.9-DevTools-en/MyBox-6.5.9-DevTools-en.html) [PDF](https://mararsh.github.io/MyBox_documents/en/MyBox-6.5.9-DevTools-en.pdf) [odt](https://mararsh.github.io/MyBox_documents/en/MyBox-6.5.9-DevTools-en.odt)                     | [html](https://mararsh.github.io/MyBox_documents/zh/MyBox-6.5.9-DevTools-zh/MyBox-6.5.9-DevTools-zh.html) [PDF](https://mararsh.github.io/MyBox_documents/zh/MyBox-6.5.9-DevTools-zh.pdf) [odt](https://mararsh.github.io/MyBox_documents/zh/MyBox-6.5.9-DevTools-zh.odt)                     |


# 实现基础       
MyBox基于以下开放资源：       

| 名字 | 角色 | 链接 |
| --- | --- | --- |
| JDK | Java语言 |  [http://jdk.java.net/](http://jdk.java.net/)   |
|   |   | [https://www.oracle.com/technetwork/java/javase/downloads/index.html](https://www.oracle.com/technetwork/java/javase/downloads/index.html)  |
|   |   | [https://docs.oracle.com/en/java/javase/18/docs/api/index.html](https://docs.oracle.com/en/java/javase/18/docs/api/index.html)  |
| JavaFx | 图形化界面 | [https://gluonhq.com/products/javafx/](https://gluonhq.com/products/javafx/)  |
|   |   |  [https://docs.oracle.com/javafx/2/](https://docs.oracle.com/javafx/2/)  |
|   |   |  [https://gluonhq.com/products/scene-builder/](https://gluonhq.com/products/scene-builder/) |
|   |   |  [https://openjfx.io/javadoc/18/](https://openjfx.io/javadoc/18/) |
| NetBeans | 集成开发环境 | [https://netbeans.org/](https://netbeans.org/) |
| jpackage | 自包含包 | [https://docs.oracle.com/en/java/javase/18/docs/specs/man/jpackage.html](https://docs.oracle.com/en/java/javase/18/docs/specs/man/jpackage.html) |
| maven | 代码构建 | [https://maven.apache.org/](https://maven.apache.org/) |
| jai-imageio | 图像处理 | [https://github.com/jai-imageio/jai-imageio-core](https://github.com/jai-imageio/jai-imageio-core) |
| PDFBox | PDF处理 | [https://pdfbox.apache.org/](https://pdfbox.apache.org/) |
| PDF2DOM | PDF转html | [http://cssbox.sourceforge.net/pdf2dom/](http://cssbox.sourceforge.net/pdf2dom/) |
| javazoom | MP3解码 | [https://sourceforge.net/projects/javalayer/](https://sourceforge.net/projects/javalayer/) |
| Derby | 数据库 | [http://db.apache.org/derby/](http://db.apache.org/derby/) |
| GifDecoder | 不规范Gif | [https://github.com/DhyanB/Open-Imaging/](https://github.com/DhyanB/Open-Imaging/) |
| EncodingDetect | 文本编码 | [https://www.cnblogs.com/ChurchYim/p/8427373.html](https://www.cnblogs.com/ChurchYim/p/8427373.html) |
| Free Icons | 图标 | [https://icons8.com/icons/set/home](https://icons8.com/icons/set/home) |
| Lindbloom | 色彩理论 | [http://brucelindbloom.com/index.html](http://brucelindbloom.com/index.html) |
| tess4j | OCR | [https://github.com/nguyenq/tess4j](https://github.com/nguyenq/tess4j) |
| tesseract | OCR | [https://github.com/tesseract-ocr/tesseract](https://github.com/tesseract-ocr/tesseract) |
| barcode4j | 生成条码 | [http://barcode4j.sourceforge.net](http://barcode4j.sourceforge.net) |
| zxing | 生成/解码条码 | [https://github.com/zxing/zxing](https://github.com/zxing/zxing) |
| flexmark-java | 转换Markdown | [https://github.com/vsch/flexmark-java](https://github.com/vsch/flexmark-java) |
| commons-compress | 归档/压缩 | [https://commons.apache.org/proper/commons-compress](https://commons.apache.org/proper/commons-compress) |
| XZ for Java | 归档/压缩 | [https://tukaani.org/xz/java.html](https://tukaani.org/xz/java.html) |
| jaffree | 封装ffmpeg | [https://github.com/kokorin/Jaffree](https://github.com/kokorin/Jaffree) |
| ffmpeg| 媒体转换/生成 | [http://ffmpeg.org](http://ffmpeg.org) |
| image4j | ico格式 | [https://github.com/imcdonagh/image4j](https://github.com/imcdonagh/image4j) |
| AutoCommitCell | 提交修改 | [https://stackoverflow.com/questions/24694616 （Ogmios）](https://stackoverflow.com/questions/24694616) |
| 高德 | 地图 | [https://lbs.amap.com/api/javascript-api/summary](https://lbs.amap.com/api/javascript-api/summary) |
| 高德 | 坐标 | [https://lbs.amap.com/api/webservice/guide/api/georegeo](https://lbs.amap.com/api/webservice/guide/api/georegeo) |
| 微博 | 图片素材 | [https://weibo.com/3876734080/InmB1aPiL?type=comment#_rnd1582211299665](https://weibo.com/3876734080/InmB1aPiL?type=comment#_rnd1582211299665) |
| poi | 微软文档 | [https://poi.apache.org](https://poi.apache.org) |
| LabeledBarChart | JavaFx图 | [https://stackoverflow.com/questions/34286062/how-to-clear-text-added-in-a-javafx-barchart/41494789#41494789 (Roland)](https://stackoverflow.com/questions/34286062/how-to-clear-text-added-in-a-javafx-barchart/41494789#41494789 (Roland)) |
| commons-csv | CSV | [https://commons.apache.org/proper/commons-csv/](https://commons.apache.org/proper/commons-csv/) |
| geonames | 位置数据 | [https://www.geonames.org/countries/](https://www.geonames.org/countries/) |
| world-area | 位置数据 | [https://github.com/wizardcode/world-area](https://github.com/wizardcode/world-area) |
| 中国国家统计局 | 数据 | [http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/](http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/) |
| JHU | COVID-19数据 | [https://github.com/CSSEGISandData/COVID-19](https://github.com/CSSEGISandData/COVID-19) |
| 懒人图库 | 色彩数据 | [https://tool.lanrentuku.com/color/china.html](https://tool.lanrentuku.com/color/china.html) |
| 中国纹样全集 | 素材 | [https://book.douban.com/subject/3894923/](https://book.douban.com/subject/3894923/) |
| 中国国家基础地理信息中心 | 地图 | [http://lbs.tianditu.gov.cn/api/js4.0/guide.html](http://lbs.tianditu.gov.cn/api/js4.0/guide.html) |
| movebank | 位置数据 | [https://www.datarepository.movebank.org](https://www.datarepository.movebank.org) |
| CoordinateConverter | 坐标转换 | [https://www.jianshu.com/p/c39a2c72dc65?from=singlemessage](https://www.jianshu.com/p/c39a2c72dc65?from=singlemessage) |
| JavaMail | email | [https://javaee.github.io/javamail/](https://javaee.github.io/javamail/) |
| Commons IO | 文件读写 | [https://commons.apache.org/proper/commons-io/](https://commons.apache.org/proper/commons-io/) |
| colorhexa | 色彩数据 | [https://www.colorhexa.com/color-names](https://www.colorhexa.com/color-names) |
| 文泉驿 | 开源字体 | [http://wenq.org/wqy2/](http://wenq.org/wqy2/) |
| ttc2ttf | 提取ttf | [https://github.com/fermi1981/TTC_TTF](https://github.com/fermi1981/TTC_TTF) |
| 中国出土壁画全集 | 素材 | [https://book.douban.com/subject/10465940/](https://book.douban.com/subject/10465940/) |
| sfds | 书法 | [http://www.sfds.cn/725B/](http://www.sfds.cn/725B/) |
| PaginatedPdfTable | PDF | [https://github.com/eduardohl/Paginated-PDFBox-Table-Sample](https://github.com/eduardohl/Paginated-PDFBox-Table-Sample) |
| jsoup | DOM | [https://jsoup.org/](https://jsoup.org/) |       
| 微博 | 素材 | [https://weibo.com/2328516855/LhFIHy26O](https://weibo.com/2328516855/LhFIHy26O) |
| 知乎 | 素材 | [https://www.zhihu.com/question/41580677/answer/1300242801](https://www.zhihu.com/question/41580677/answer/1300242801) |
| commons-math | 计算 | [https://commons.apache.org/proper/commons-math/index.html](https://commons.apache.org/proper/commons-math/index.html) |
| JEXL | 计算 | [https://commons.apache.org/proper/commons-jexl](https://commons.apache.org/proper/commons-jexl) |
| OpenOffice | 文档 | [http://www.openoffice.org/](http://www.openoffice.org/) |
| nashorn | JavaScript | [https://openjdk.org/projects/nashorn/](https://openjdk.org/projects/nashorn/) |
| echarts-gl | WebGL | [https://github.com/ecomfe/echarts-gl](https://github.com/ecomfe/echarts-gl) |


# 特点
## 跨平台<a id="cross-platform" />       
MyBox用纯Java实现且只基于开放资源，MyBox可运行于支持Java 18的平台。       
MyBox v5.3以前的版本均基于Java 8。       

## 国际化<a id="international" />       
1. 所有代码均国际化。可实时切换语言。
2. 一种语言对应两个资源文件："Messages_语言名.properties"、"TableMessages_语言名.properties"。
3. 支持在线添加语言。提供表格，对照英语翻译。新语言可实时生效。       
例如，新语言名字为“aa”，则它的资源文件是：Messages_aa.properties和TableMessages_aa.properties。       
4. 新语言可共享给别人：把资源文件复制到数据目录的子目录"mybox_languages"下，则MyBox可即时感知到新语言。       
5. 内置中文和英文， 在目录`MyBox/src/main/resources/bundles/`中：       

| 语言 | 界面的资源文件 | 数据表的资源文件 |       
| --- | --- |  --- |
| 中文 | Messages_zh_CN.properties | TableMessages_zh_CN.properties |
| 英文 | Messages_en.properties | TableMessages_en.properties |       

## 个人的<a id="personal" />
1. 无注册/登录/数据中心/云存储。
2. 如无必要，不访问网络。       
3. 如无必要，不读不写。       

## 数据兼容<a id="dataCompatible" />
1. 导出的数据是通用的文本格式，如txt/csv/xml/json/html。
2. 导入的数据是通用的文本格式，如txt/csv。       
3. 至少有一种导出格式可以被导入。
4. 导入的数据是自包含的，即重建原数据无需辅助数据。       

![截屏-封面](https://mararsh.github.io/MyBox/snap-cover.jpg)       

![截屏-界面](https://mararsh.github.io/MyBox/snap-interface.jpg)       

       