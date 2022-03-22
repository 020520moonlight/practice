package fm.douban.spider;

import com.alibaba.fastjson.JSON;
import fm.douban.model.Singer;
import fm.douban.model.Song;
import fm.douban.model.Subject;
import fm.douban.service.SingerService;
import fm.douban.service.SongService;
import fm.douban.service.SubjectService;
import fm.douban.util.HttpUtil;
import fm.douban.util.SubjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class SubjectSpider {
    private static Logger logger = LoggerFactory.getLogger(SubjectSpider.class);

    @Autowired
    private HttpUtil httpUtil;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private SingerService singerService;

    @Autowired
    private SongService songService;

    private static final String MHZ_URL = "https://douban.fm/j/v2/rec_channels?specific=all";
    private static final String MHZ_REFERER = "https://douban.fm/";
    private static final String HOST = "douban.fm";

    private static final String SL_URL =
        "https://douban.fm/j/v2/songlist/explore?type=hot&genre=0&limit=20&sample_cnt=5";
    private static final String SL_REFERER = "https://douban.fm/explore/songlists";

    private static final DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter df2 = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    private static final String SD_URL =
        "https://fm.douban.com/j/v2/playlist?channel={0}&kbps=128&client=s%3Amainsite%7Cy%3A3.0&app_name=radio_website&version=100&type=n";

    @PostConstruct
    public void init() {
        CompletableFuture.supplyAsync(() -> doExcute()).thenAccept(result -> {
            logger.error("spider end ...");
        });
    }

    public boolean doExcute() {
        getSubjectData();
        getCollectionsData();

        return true;
    }

    private void getSubjectData() {
        // 有时候需要设置，有时候不需要设置，很奇怪
        //Map<String, String> headerData = httpUtil.buildHeaderData(MHZ_REFERER, HOST);
        String content = httpUtil.getContent(MHZ_URL, null);

        if (!StringUtils.hasText(content)) {
            return;
        }

        Map dataObj = null;

        try {
            dataObj = JSON.parseObject(content, Map.class);
        } catch (Exception e) {
            // 抛异常表示返回的内容不正确，不是正常的 json 格式，可能是网络或服务器出错了。
            logger.error("parse content to map error. ", e);
        }

        // 可能格式错误
        if (dataObj == null) {
            return;
        }

        Map subDataObj = (Map)dataObj.get("data");

        Map channelsObj = null;
        if (subDataObj != null && !subDataObj.isEmpty()) {
            channelsObj = (Map)subDataObj.get("channels");
        }

        if (channelsObj == null) {
            return;
        }

        // 从艺术家出发 数据
        List artists = (List)channelsObj.get("artist");

        // 语言/年代 数据
        List languages = (List)channelsObj.get("language");

        // 风格/流派 数据
        List genres = (List)channelsObj.get("genre");

        // 心情/场景 数据
        List scenarios = (List)channelsObj.get("scenario");

        System.out.println("artists=" + artists.size());
        System.out.println("languages=" + languages.size());
        System.out.println("genres=" + genres.size());
        System.out.println("scenarios=" + scenarios.size());

        addMHZSubject(artists, SubjectUtil.TYPE_SUB_ARTIST);
        addMHZSubject(languages, SubjectUtil.TYPE_SUB_AGE);
        addMHZSubject(genres, SubjectUtil.TYPE_SUB_STYLE);
        addMHZSubject(scenarios, SubjectUtil.TYPE_SUB_MOOD);
    }

    private void addMHZSubject(List<Map> channels, String subjectSubType) {
        if (channels == null || channels.isEmpty()) {
            return;
        }

        channels.forEach(channelObj -> {
            Subject subject = buildSubject(channelObj, SubjectUtil.TYPE_MHZ, subjectSubType);

            if (SubjectUtil.TYPE_SUB_ARTIST.equals(subjectSubType)) {
                // 记录关联的歌手
                List relatedArtists = (List)channelObj.get("related_artists");
                addSingers(relatedArtists);
            }

            // 保存MHZ数据
            saveSubject(subject);

            getSubjectSongData(subject);
        });

    }

    // 增加歌手
    private void addSingers(List<Map> artists) {
        if (artists == null || artists.isEmpty()) {
            return;
        }

        artists.forEach(artistObj -> {
            Singer singer = buildSinger(artistObj);
            saveSinger(singer);
        });
    }

    private void getSubjectSongData(Subject subject) {
        String subjectId = subject.getId();
        String songDataUrl = MessageFormat.format(SD_URL, subjectId);

        // 有时候需要设置，有时候不需要设置，很奇怪
        //Map<String, String> headerData = httpUtil.buildHeaderData(MHZ_REFERER, HOST);
        String content = httpUtil.getContent(songDataUrl, null);

        if (!StringUtils.hasText(content)) {
            return;
        }

        Map dataObj = null;

        try {
            dataObj = JSON.parseObject(content, Map.class);
        } catch (Exception e) {
            // 抛异常表示返回的内容不正确，不是正常的 json 格式，可能是网络或服务器出错了。
            logger.error("parse content to map error. ", e);
        }

        // 可能格式错误
        if (dataObj == null) {
            return;
        }

        List<Map> songsData = (List<Map>)dataObj.get("song");
        if (songsData == null || songsData.isEmpty()) {
            return;
        }

        // 取得兆赫对应的歌曲 id 列表
        List<String> songIds = subject.getSongIds();
        if (songIds == null) {
            // 如果为 null，则初始化
            songIds = new ArrayList<>();
        }

        final List<String> songIdList = songIds;

        songsData.forEach(songObj -> {
            Song song = buildSong(songObj);
            saveSong(song);

            // 如果歌曲 id 不包含在原来的列表中，则新增
            if (!songIdList.contains(song.getId())) {
                songIdList.add(song.getId());
            }
        });

        // 有元素则进行修改
        if (!songIdList.isEmpty()) {
            subject.setSongIds(songIdList);
            subjectService.modify(subject);
        }
    }

    private void getCollectionsData() {
        // 有时候需要设置，有时候不需要设置，很奇怪
        //Map<String, String> headerData = httpUtil.buildHeaderData(SL_REFERER, HOST);
        String content = httpUtil.getContent(SL_URL, null);

        if (!StringUtils.hasText(content)) {
            return;
        }

        List<Map> dataList = null;

        try {
            dataList = JSON.parseObject(content, List.class);
        } catch (Exception e) {
            // 抛异常表示返回的内容不正确，不是正常的 json 格式，可能是网络或服务器出错了。
            logger.error("parse content to map error. ", e);
        }

        if (dataList == null || dataList.isEmpty()) {
            return;
        }

        dataList.forEach(slObj -> {
            Subject collection = buildSubject(slObj, SubjectUtil.TYPE_COLLECTION, null);

            // 处理歌单的作者，存为 user
            Map creator = (Map)slObj.get("creator");

            if (creator != null && creator.get("id") != null && StringUtils.hasText(creator.get("id").toString())) {
                Singer singer = buildSinger(creator);
                saveSinger(singer);
            }

            // 保存 歌单 数据
            saveSubject(collection);
        });
    }

    private Subject buildSubject(Map sourceData, String mainType, String subType) {
        Subject subject = new Subject();
        subject.setCover((String)sourceData.get("cover"));

        if (sourceData.get("title") != null) {
            subject.setName((String)sourceData.get("title"));
        } else if (sourceData.get("name") != null) {
            subject.setName((String)sourceData.get("name"));
        } else {
            subject.setName("");
        }

        subject.setDescription((String)sourceData.get("intro"));
        subject.setId(sourceData.get("id") == null ? null : sourceData.get("id").toString());

        if (sourceData.get("created_time") != null) {
            subject.setPublishedDate(LocalDate.parse(sourceData.get("created_time").toString(), df));
        } else {
            subject.setPublishedDate(LocalDate.now());
        }

        subject.setSubjectSubType(subType);
        subject.setSubjectType(mainType);

        if (SubjectUtil.TYPE_SUB_ARTIST.equals(subType) && sourceData.get("artist_id") != null) {
            subject.setMaster(sourceData.get("artist_id").toString());
        } else if (SubjectUtil.TYPE_COLLECTION.equals(mainType) && sourceData.get("creator") != null) {
            Map creator = (Map)sourceData.get("creator");
            if (creator != null && creator.get("id") != null) {
                subject.setMaster(creator.get("id").toString());
            }
        }

        return subject;
    }

    private void saveSubject(Subject subject) {
        try {
            // 没有相同的记录才插入
            Subject oldSubject = subjectService.get(subject.getId());
            if (oldSubject == null) {
                subjectService.addSubject(subject);
            }
        } catch (Exception e) {
            logger.error("operate subject error. new subject=" + JSON.toJSONString(subject), e);
        }
    }

    public Singer buildSinger(Map source) {
        Singer singer = new Singer();
        singer.setId(source.get("id") == null ? null : source.get("id").toString());
        singer.setName(source.get("name") == null ? null : source.get("name").toString());

        if (source.get("cover") != null && StringUtils.hasText(source.get("cover").toString())) {
            singer.setAvatar(source.get("cover").toString());
        } else if (source.get("picture") != null && StringUtils.hasText(source.get("picture").toString())) {
            singer.setAvatar(source.get("picture").toString());
        } else if (source.get("avatar") != null && StringUtils.hasText(source.get("avatar").toString())) {
            singer.setAvatar(source.get("avatar").toString());
        }

        if (source.get("create_time") != null && StringUtils.hasText(source.get("create_time").toString())) {
            LocalDate ld = LocalDate.parse(source.get("create_time").toString(), df2);
            LocalTime lt = LocalTime.of(0, 0, 0);
            singer.setGmtCreated(LocalDateTime.of(ld, lt));
            singer.setGmtModified(singer.getGmtCreated());
        }

        if (source.get("url") != null && StringUtils.hasText(source.get("url").toString())) {
            singer.setHomepage(source.get("url").toString());
        }

        return singer;
    }

    public void saveSinger(Singer singer) {
        try {
            // 没有相同的记录才插入
            Singer oldSinger = singerService.get(singer.getId());
            if (oldSinger == null) {
                singerService.addSinger(singer);
            }
        } catch (Exception e) {
            logger.error("operate singer error. new singer=" + JSON.toJSONString(singer), e);
        }
    }

    public Song buildSong(Map source) {
        Song song = new Song();
        song.setUrl((String)source.get("url"));
        song.setCover((String)source.get("picture"));
        song.setId((String)source.get("sid"));
        song.setName((String)source.get("title"));

        List<String> singerIds = new ArrayList<>();
        List<Map> singerSources = (List<Map>)source.get("singers");

        singerSources.forEach(singerObj -> {
            Singer singer = buildSinger(singerObj);
            singerIds.add(singer.getId());
            saveSinger(singer);
        });

        song.setSingerIds(singerIds);
        return song;
    }

    public void saveSong(Song song) {
        try {
            // 没有相同的记录才插入
            Song oldSong = songService.get(song.getId());
            if (oldSong == null) {
                songService.add(song);
            }
        } catch (Exception e) {
            logger.error("operate song error. new song=" + JSON.toJSONString(song), e);
        }
    }
}
