package com.gzqylc.docker_admin.agent;

import com.github.dockerjava.api.model.ResponseItem;
import com.github.kevinsawicki.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;

import java.nio.charset.StandardCharsets;

@Slf4j
public class RemoteLogger {
    String logUrl;

    private StringBuffer buffer = new StringBuffer();


    private RemoteLogger(String logUrl) {
        this.logUrl = logUrl;
    }

    public static RemoteLogger getLogger(String logUrl) {
        return new RemoteLogger(logUrl);
    }

    public void info(ResponseItem item) {
        // 打印比较好的日志
        String stream = item.getStream();
        if (stream != null) {
            buffer.append(stream);

            if (stream.endsWith("\n")) {
                String info = buffer.toString();
                String[] lines = info.split("\\n|\\r");
                for (String line : lines) {
                    if (line.startsWith("Step ")) {
                        this.info(line);
                    } else {
                        this.infoTab(line);
                    }
                }
                buffer.setLength(0);
            }

        } else if (item.getStatus() != null) {
            infoTab("{} {}", item.getStatus(), item.getProgress());
        } else {
            infoTab(item.toString());
        }

    }

    public void info(String format, Object... arguments) {
        String result = format;
        if (arguments.length != 0) {
            for (Object arg : arguments) {
                result = result.replaceFirst("\\{\\}", arg == null ? "" : String.valueOf(arg));
            }
        }
        writeLine(result);
    }

    /**
     * 缩进一行
     *
     * @param format
     * @param arguments
     */
    public void infoTab(String format, Object... arguments) {
        this.info("    " + format, arguments);
    }


    private void writeLine(String msg) {
        if (StringUtils.isBlank(msg)) {
            return;
        }


        String time = DateFormatUtils.format(System.currentTimeMillis(), "HH:mm:ss");
        msg = time + "  " + msg + "\r\n";

        log.info(msg);

        // TODO 优化
        try {
            HttpRequest.post(logUrl).send(msg.getBytes(StandardCharsets.UTF_8)).body();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
