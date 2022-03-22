package fm.douban.app.control;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import fm.douban.model.CollectionViewModel;
import fm.douban.model.Singer;
import fm.douban.model.Song;
import fm.douban.model.Subject;
import fm.douban.service.SingerService;
import fm.douban.service.SongService;
import fm.douban.service.SubjectService;
import fm.douban.util.FileUtil;
import fm.douban.util.SubjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class SubjectControl {

    private static final Logger LOG = LoggerFactory.getLogger(SubjectControl.class);

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private SingerService singerService;

    @Autowired
    private SongService songService;

    @GetMapping(path = "/artist")
    public String mhzDetail(Model model, @RequestParam(name = "subjectId") String subjectId) {

        Subject subject = subjectService.get(subjectId);
        if (subject == null) {
            return "error";
        }

        model.addAttribute("subject", subject);
        List<String> songIds = subject.getSongIds();
        List<Song> songs = new ArrayList<>();

        if (songIds != null && !songIds.isEmpty()) {
            songIds.forEach(songId -> {
                Song song = songService.get(songId);
                if (song != null) {
                    songs.add(song);
                }
            });
        }

        model.addAttribute("songs", songs);

        String singerId = subject.getMaster();
        Singer singer = singerService.get(singerId);
        model.addAttribute("singer", singer);
        List<String> similarSingerIds = singer.getSimilarSingerIds();
        List<Singer> simSingers = new ArrayList<>();

        if (similarSingerIds != null && !similarSingerIds.isEmpty()) {
            similarSingerIds.forEach(simSingerId -> {
                Singer simSinger = singerService.get(simSingerId);
                if (simSinger != null) {
                    simSingers.add(simSinger);
                }
            });
        }

        model.addAttribute("simSingers", simSingers);

        return "mhzdetail";

    }

    @GetMapping(path = "/collection")
    public String collection(Model model) {
        List<Subject> subjects = subjectService.getSubjects(SubjectUtil.TYPE_COLLECTION);

        List<List<CollectionViewModel>> subjectColumns = new ArrayList<>();
        // 最大行数
        int lineCount = (subjects.size() % 5 == 0) ? subjects.size() / 5 : (subjects.size() / 5) + 1;
        // 列数，最多 5 列
        for (int i = 0; i < 5; i++) {
            // 每列的元素
            List<CollectionViewModel> column = new ArrayList<>();
            // 第一列的元素是 0 5 11
            // j 是行数
            for (int j = 0; j < lineCount; j++) {
                int itemIndex = i + j * 5;
                if (itemIndex < subjects.size()) {
                    Subject subject = subjects.get(itemIndex);
                    CollectionViewModel xvm = new CollectionViewModel();
                    xvm.setSubject(subject);

                    if (subject.getMaster() != null) {
                        Singer singer = singerService.get(subject.getMaster());
                        xvm.setSinger(singer);
                    }

                    if (subject.getSongIds() != null && !subject.getSongIds().isEmpty()) {
                        List<Song> songs = new ArrayList<>();
                        subject.getSongIds().forEach(songId -> {
                            Song song = songService.get(songId);
                            songs.add(song);
                        });
                        xvm.setSongs(songs);
                    }

                    column.add(xvm);
                }
            }
            subjectColumns.add(column);
        }

        if (subjectColumns == null || subjectColumns.isEmpty()) {
            subjectColumns = mockSubjects();
        }

        model.addAttribute("subjectColumns", subjectColumns);
        return "collection";
    }

    @GetMapping(path = "/collectiondetail")
    public String collectionDetail(Model model, @RequestParam(name = "subjectId") String subjectId) {

        Subject subject = subjectService.get(subjectId);
        if (subject == null) {
            return "error";
        }

        model.addAttribute("subject", subject);
        List<String> songIds = subject.getSongIds();
        List<Song> songs = new ArrayList<>();

        if (songIds != null && !songIds.isEmpty()) {
            songIds.forEach(songId -> {
                Song song = songService.get(songId);
                if (song != null) {
                    songs.add(song);
                }
            });
        }

        model.addAttribute("songs", songs);

        String singerId = subject.getMaster();
        Singer singer = singerService.get(singerId);
        model.addAttribute("singer", singer);

        // 查询其它歌单
        Subject subjectParam = new Subject();
        subjectParam.setSubjectType(SubjectUtil.TYPE_COLLECTION);
        subjectParam.setMaster(singerId);
        List<Subject> otherSubjects = subjectService.getSubjects(subjectParam);
        model.addAttribute("otherSubjects", otherSubjects);

        return "collectiondetail";
    }

    private List<List<CollectionViewModel>> mockSubjects() {
        String subjectsString = FileUtil.readFileContent("sample/subjects-collection.json");
        List<List<CollectionViewModel>> subjects;
        try {
            TypeReference<List<List<CollectionViewModel>>> tr = new TypeReference<List<List<CollectionViewModel>>>() {
            };

            subjects = JSON.parseObject(subjectsString, tr, Feature.InitStringFieldAsEmpty);
        } catch (Exception e) {
            subjects = new ArrayList<>();
        }

        return subjects;
    }
}
