package fm.douban.app.control;

import fm.douban.model.Song;
import fm.douban.param.SongQueryParam;
import fm.douban.service.SongService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/test/song")
public class SongTestControl {
    private static final Logger LOG = LoggerFactory.getLogger(SongTestControl.class);

    @Autowired
    private SongService songService;

    @GetMapping(path = "/add")
    public Song testAdd() {
        Song song = new Song();
        song.setId("0");
        song.setGmtModified(LocalDateTime.now());
        song.setGmtCreated(LocalDateTime.now());
        song.setLyrics("歌词");
        song.setCover("https://5b0988e595225.cdn.sohucs.com/images/20170724/ad28da0d658b43aba84ce91df9cacdad.jpeg");
        song.setName("测试歌曲");
        song.setUrl("https://5b0988e595225.cdn.sohucs.com/images/20170724/aaa.mp3");

        List<String> singerIds = new ArrayList<>();
        singerIds.add("0");
        song.setSingerIds(singerIds);

        Song addedSong = songService.add(song);

        return addedSong;
    }

    @GetMapping(path = "/get")
    public Song testGet() {
        return songService.get("0");
    }

    @GetMapping(path = "/list")
    public Page<Song> testList() {
        SongQueryParam query = new SongQueryParam();
        query.setName("测试歌曲");
        query.setLyrics("歌词");
        return songService.list(query);
    }

    @GetMapping(path = "/modify")
    public boolean testModify() {
        Song song = new Song();
        song.setId("0");
        song.setName("测试歌曲2");
        return songService.modify(song);
    }

    @GetMapping(path = "/del")
    public boolean testDelete() {
        return songService.delete("0");
    }
}
