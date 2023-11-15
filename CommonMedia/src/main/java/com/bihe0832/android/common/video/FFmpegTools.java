package com.bihe0832.android.common.video;

import android.media.MediaMetadataRetriever;
import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.bihe0832.android.common.media.MediaTools;
import com.bihe0832.android.framework.file.AAFFileWrapper;
import com.bihe0832.android.lib.aaf.tools.AAFDataCallback;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.thread.ThreadManager;
import com.bihe0832.android.lib.utils.ConvertUtils;
import java.util.List;

/**
 * Summary
 *
 * @author code@bihe0832.com
 *         Created on 2023/9/19.
 *         Description:
 */
public class FFmpegTools {

    public static int executeFFmpegCommand(final String[] command) {
        // 执行 FFmpeg 命令
        int result = FFmpeg.execute(command);
        System.gc();
        if (result == Config.RETURN_CODE_SUCCESS) {
            ZLog.i(MediaTools.TAG, "FFmpeg  execute 成功");
        } else {
            ZLog.i(MediaTools.TAG, "FFmpeg  execute 失败");
        }
        return result;
    }

    public static int executeFFmpegCommand(final String command) {
        // 执行 FFmpeg 命令
        int result = FFmpeg.execute(command);
        System.gc();
        if (result == Config.RETURN_CODE_SUCCESS) {
            ZLog.i(MediaTools.TAG, "FFmpeg  execute 成功");
        } else {
            ZLog.i(MediaTools.TAG, "FFmpeg  execute 失败");
        }
        return result;
    }

    public static void convertAudioWithImageToVideo(int width, int height, String audioPath, String imagePath,
            AAFDataCallback<String> callback) {
        ThreadManager.getInstance().start(() -> {
            try {
                String videoPath = AAFFileWrapper.INSTANCE.getCacheVideoPath(".mp4");
                String[] combineCommand = {"-y", "-loop", "1", "-i", imagePath, "-i", audioPath, "-vf",
                        "scale=" + width + ":" + height, "-b:v", "2000k", "-c:a", "aac", "-b:a", "192k", "-pix_fmt",
                        "yuv420p", "-shortest", videoPath};
                int result = FFmpegTools.executeFFmpegCommand(combineCommand);
                if (result == Config.RETURN_CODE_SUCCESS) {
                    callback.onSuccess(videoPath);
                } else {
                    callback.onError(result, "executeFFmpegCommand failed");
                }
            } catch (Exception e) {
                callback.onError(-1, "executeFFmpegCommand exception:" + e);
            }
        });
    }

    public static void convertAudioWithImageToVideo(int width, int height, String audioPath, long coverDuration,
            List<String> imagePaths,
            AAFDataCallback<String> callback) {
        ThreadManager.getInstance().start(() -> {
            try {
                String videoPath = AAFFileWrapper.INSTANCE.getCacheVideoPath(".mp4");
                // 获取音频时长
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(audioPath);
                long audioDuration = ConvertUtils.parseLong(
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION), 0L);
                long coverImageDuration = coverDuration;
                if (audioDuration < coverDuration) {
                    coverImageDuration = audioDuration;
                }

                long remainingImageDuration =
                        (audioDuration - coverImageDuration) / (imagePaths.size() - 1); // 其余图片持续时间（毫秒）

                // 生成输入文件参数
                StringBuilder inputArgsBuilder = new StringBuilder();
                inputArgsBuilder.append(String.format("-loop 1 -i %s ", imagePaths.get(0))); // 封面图
                for (int i = 1; i < imagePaths.size(); i++) {
                    inputArgsBuilder.append(String.format("-loop 1 -i %s ", imagePaths.get(i)));
                }
                String inputArgs = inputArgsBuilder.toString();

                // 设置滤镜
                StringBuilder filterArgsBuilder = new StringBuilder();
                for (int i = 0; i < imagePaths.size(); i++) {
                    filterArgsBuilder.append(String.format(
                            "[%d:v]scale=iw*min(%d/iw\\,%d/ih):ih*min(%d/iw\\,%d/ih),pad=%d:%d:(%d-iw)/2:(%d-ih)/2,trim=duration=%.2f[v%d];",
                            i, width, height, width, height, width, height, width,
                            height, (i == 0 ? coverImageDuration : remainingImageDuration) / 1000.0, i));
                }
                for (int i = 0; i < imagePaths.size(); i++) {
                    filterArgsBuilder.append(String.format("[v%d]", i));
                }
                filterArgsBuilder.append(String.format("concat=n=%d:v=1:a=0[v]", imagePaths.size()));
                String filterComplex = filterArgsBuilder.toString();

                // -b:v 2000k（视频比特率为 2000 kbps）、-c:a aac（音频编码器为 AAC）、-b:a 192k（音频比特率为 192 kbps）和 -pix_fmt yuv420p（像素格式为 yuv420p）
                String command = String.format(
                        "-y %s -i %s -filter_complex \"%s\" -map \"[v]\" -map %d:a -shortest -b:v 2000k -c:a aac -b:a 192k -pix_fmt yuv420p %s",
                        inputArgs, audioPath, filterComplex, imagePaths.size(), videoPath);
                int result = FFmpegTools.executeFFmpegCommand(command);
                if (result == Config.RETURN_CODE_SUCCESS) {
                    callback.onSuccess(videoPath);
                } else {
                    callback.onError(result, "executeFFmpegCommand failed");
                }
            } catch (Exception e) {
                callback.onError(-1, "executeFFmpegCommand exception:" + e);
            }
        });
    }
}