package mara.mybox.controller;

import java.io.File;
import java.util.Arrays;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControllerTools;
import mara.mybox.fxml.PopTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.value.FileExtensions;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-12-1
 * @License Apache License Version 2.0
 */
public class FFmpegConvertMediaFilesController extends BaseBatchFFmpegController {

    public FFmpegConvertMediaFilesController() {
        baseTitle = Languages.message("FFmpegConvertMediaFiles");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(
                    targetPathController.valid.not()
                            .or(Bindings.isEmpty(tableView.getItems()))
                            .or(ffmpegOptionsController.extensionInput.styleProperty().isEqualTo(UserConfig.badStyle()))
            );

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            String ext = ffmpegOptionsController.extensionInput.getText().trim();
            if (ext.isEmpty() || Languages.message("OriginalFormat").equals(ext)) {
                ext = FileNameTools.suffix(srcFile.getName());
            }
            File target = makeTargetFile(FileNameTools.prefix(srcFile.getName()), "." + ext, targetPath);
            if (target == null) {
                return Languages.message("Skip");
            }
//            convert(srcFile, target, -1);
            targetFileGenerated(target);
            return Languages.message("Successful");
        } catch (Exception e) {
            updateLogs(e.toString(), true);
            return Languages.message("Failed");
        }
    }

    @Override
    public void updateFileProgress(long number, long total) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                double p = (number * 1d) / total;
                String s = DateTools.timeDuration(number) + "/"
                        + DateTools.timeDuration(total);
                progressBar.setProgress(p);
                progressValue.setText(s);
            }
        });
    }

    protected void convert(File sourceFile, File targetFile, long duration)
            throws Exception {
//        convert(UrlInput.fromPath(Paths.get(sourceFile.getAbsolutePath())), targetFile, duration);
    }

//    protected void convert(UrlInput input, File targetFile, long inDuration)
//            throws Exception {
//        final long duration;
//        if (inDuration < 0) {
//            final AtomicLong countedDuration = new AtomicLong();
//            updateLogs(Languages.message("CountingMediaDuration"), true);
//            FFmpeg.atPath(ffmpegOptionsController.executable.toPath().getParent())
//                    .addInput(input)
//                    .addOutput(new NullOutput())
//                    .setOverwriteOutput(true)
//                    .setProgressListener((FFmpegProgress progress) -> {
//                        countedDuration.set(progress.getTimeMillis());
//                    })
//                    .execute();
//            duration = countedDuration.get();
//            updateLogs(Languages.message("Duration") + ": " + DateTools.timeMsDuration(duration), true);
//        } else {
//            duration = inDuration;
//        }
//
//        ProgressListener listener = new ProgressListener() {
//            private long lastProgress = System.currentTimeMillis();
//            private long lastStatus = System.currentTimeMillis();
//
//            @Override
//            public void onProgress(FFmpegProgress progress) {
//                Platform.runLater(() -> {
//                    long now = System.currentTimeMillis();
//                    if (now > lastProgress + 500) {
//                        if (duration > 0) {
//                            updateFileProgress(progress.getTimeMillis(), duration);
//                        } else {
//                            updateLogs(Languages.message("Handled") + ":"
//                                    + DateTools.timeDuration(progress.getTimeMillis()), true);
//                            progressValue.setText(DateTools.timeDuration(progress.getTimeMillis()));
//                        }
//                        lastProgress = now;
//                    }
//                    if (now > lastStatus + 3000) {
//                        long cost = now - ffmpegOptionsController.mediaStart;
//                        if (duration > 0) {
//                            double p = DoubleTools.scale2(progress.getTimeMillis() * 100.0d / duration);
//                            long left = Math.round(cost * (100 - p) / p);
//                            String s = Languages.message("Percentage") + ": " + p + "%  "
//                                    + Languages.message("Cost") + ": " + DateTools.datetimeMsDuration(cost) + "   "
//                                    + Languages.message("EstimatedLeft") + ": " + DateTools.datetimeMsDuration(left);
//                            statusLabel.setText(s);
//                        } else {
//                            String s = Languages.message("Cost") + ": " + DateTools.datetimeMsDuration(cost);
//                            statusLabel.setText(s);
//                        }
//                        lastStatus = now;
//                    }
//                });
//
//            }
//        };
//        ffmpegOptionsController.mediaStart = System.currentTimeMillis();
//        FFmpeg ffmpeg = FFmpeg.atPath(ffmpegOptionsController.executable.toPath().getParent())
//                .addInput(input)
//                .addOutput(UrlOutput.toPath(targetFile.toPath()))
//                .setProgressListener(listener)
//                .setOverwriteOutput(true);
//        if (ffmpegOptionsController.disableVideo) {
//            ffmpeg.addArgument("-vn");
//        } else if (ffmpegOptionsController.videoCodec != null) {
//            ffmpeg.addArguments("-vcodec", ffmpegOptionsController.videoCodec);
//        }
//        if (ffmpegOptionsController.videoBitrate > 0) {
//            ffmpeg.addArguments("-b:v", ffmpegOptionsController.videoBitrate + "k");
//        } else {
//            ffmpeg.addArguments("-b:v", "5000k");
//        }
//        if (ffmpegOptionsController.aspect != null) {
//            ffmpeg.addArguments("-aspect", ffmpegOptionsController.aspect);
//        }
//        if (ffmpegOptionsController.videoFrameRate > 0) {
//            ffmpeg.addArguments("-r", ffmpegOptionsController.videoFrameRate + "");
//        } else {
//            ffmpeg.addArguments("-r", "30");
//        }
//        if (ffmpegOptionsController.width > 0 && ffmpegOptionsController.height > 0) {
//            ffmpeg.addArguments("-s", ffmpegOptionsController.width + "x" + ffmpegOptionsController.height);
//        }
//
//        if (ffmpegOptionsController.disbaleAudio) {
//            ffmpeg.addArgument("-an");
//        } else if (ffmpegOptionsController.audioCodec != null) {
//            ffmpeg.addArguments("-acodec", ffmpegOptionsController.audioCodec);
//        }
//        if (ffmpegOptionsController.audioBitrate > 0) {
//            ffmpeg.addArguments("-b:a", ffmpegOptionsController.audioBitrate + "k");
//        } else {
//            ffmpeg.addArguments("-b:a", "192k");
//        }
//        if (ffmpegOptionsController.audioSampleRate > 0) {
//            ffmpeg.addArguments("-ar", ffmpegOptionsController.audioSampleRate + "");
//        } else {
//            ffmpeg.addArguments("-ar", "44100");
//        }
//        if (ffmpegOptionsController.volumn != null) {
//            ffmpeg.addArguments("-af", "volume=" + ffmpegOptionsController.volumn);
//        }
//        if (ffmpegOptionsController.stereoCheck != null) {
//            ffmpeg.addArguments("-ac", ffmpegOptionsController.stereoCheck.isSelected() ? "2" : "1");
//        }
//
//        if (ffmpegOptionsController.disbaleSubtitle) {
//            ffmpeg.addArgument("-sn");
//        } else if (ffmpegOptionsController.subtitleCodec != null) {
//            ffmpeg.addArguments("-scodec", ffmpegOptionsController.subtitleCodec);
//        }
//
//        String more = ffmpegOptionsController.moreInput.getText().trim();
//        if (!more.isBlank()) {
//            String[] args = StringTools.splitBySpace(more);
//            if (args != null && args.length > 0) {
//                for (String arg : args) {
//                    ffmpeg.addArgument(arg);
//                }
//            }
//        }
//
//        updateLogs(Languages.message("ConvertingMedia") + "  " + Languages.message("TargetFile") + ":" + targetFile, true);
//        FFmpegResult result = ffmpeg.execute();
//    }
    @Override
    public void viewTarget(File file) {
        if (file == null) {
            return;
        }
        if (Arrays.asList(FileExtensions.MediaPlayerSupports).contains(FileNameTools.suffix(file.getName()))) {
            ControllerTools.openMediaPlayer(null, file);

        } else {
            openTarget(null);
        }

    }

    @Override
    public boolean checkBeforeNextAction() {
        if ((ffmpegOptionsController.encoderTask != null && !ffmpegOptionsController.encoderTask.isQuit())
                || (ffmpegOptionsController.muxerTask != null && !ffmpegOptionsController.muxerTask.isQuit())
                || (ffmpegOptionsController.queryTask != null && !ffmpegOptionsController.queryTask.isQuit())) {
            if (!PopTools.askSure(this, getMyStage().getTitle(), Languages.message("TaskRunning"))) {
                return false;
            }
            if (ffmpegOptionsController.encoderTask != null) {
                ffmpegOptionsController.encoderTask.cancel();
                ffmpegOptionsController.encoderTask = null;
            }
            if (ffmpegOptionsController.muxerTask != null) {
                ffmpegOptionsController.muxerTask.cancel();
                ffmpegOptionsController.muxerTask = null;
            }
            if (ffmpegOptionsController.queryTask != null) {
                ffmpegOptionsController.queryTask.cancel();
                ffmpegOptionsController.queryTask = null;
            }
        }
        return true;
    }

}
