package fm.douban.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.nio.charset.Charset;

public class FileUtil {
    private static Logger logger = LoggerFactory.getLogger(FileUtil.class);

    /**
     * 读取文件内容。
     *
     * @param subPath 相对于 src/main/resources 的路径
     * @return
     */
    public static String readFileContent(String subPath) {
        String content = null;
        try {
            FileInputStream fis = new FileInputStream(ResourceUtils.getFile("classpath:" + subPath));
            content = IOUtils.toString(fis, Charset.forName("utf-8"));
        } catch (Exception e) {
            logger.error("", e);
        }

        return content;
    }
}
