package fm.douban.app.control;

import com.alibaba.fastjson.JSON;
import fm.douban.model.*;
import fm.douban.param.SongQueryParam;
import fm.douban.service.*;
import fm.douban.util.FavoriteUtil;
import fm.douban.util.SubjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MainControl {
    private static final Logger LOG = LoggerFactory.getLogger(MainControl.class);

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private SingerService singerService;

    @Autowired
    private SongService songService;

    @Autowired
    private UserService userService;

    @Autowired
    private FavoriteService favoriteService;

    @GetMapping(path = "/index")
    public String index(Model model) {
        // 设置首屏歌曲数据
        setSongData(model);
        // 设置赫兹数据
        setMhzData(model);
        return "index";
    }

    @GetMapping(path = "/search")
    public String search(Model model) {
        return "search";
    }

    @GetMapping(path = "/searchContent")
    @ResponseBody
    public Map searchContent(@RequestParam(name = "keyword") String keyword) {
        SongQueryParam songParam = new SongQueryParam();
        songParam.setName(keyword);
        Page<Song> songs = songService.list(songParam);

        Map result = new HashMap<>();
        result.put("songs", songs);

        return result;
    }

    @GetMapping(path = "/my")
    public String myPage(Model model, HttpServletRequest request, HttpServletResponse response) {
        // 取得 HttpSession 对象
        HttpSession session = request.getSession();
        UserLoginInfo userLoginInfo = (UserLoginInfo)session.getAttribute("userLoginInfo");

        String userId = userLoginInfo.getUserId();

        Favorite fav = new Favorite();
        fav.setUserId(userId);
        fav.setType(FavoriteUtil.TYPE_RED_HEART);
        List<Favorite> favs = favoriteService.list(fav);

        model.addAttribute("favorites", favs);

        List<Song> favedSongs = new ArrayList<>();
        if (favs != null && !favs.isEmpty()) {
            favs.forEach(favorite -> {
                if (FavoriteUtil.TYPE_RED_HEART.equals(favorite.getType()) && FavoriteUtil.ITEM_TYPE_SONG.equals(
                    favorite.getItemType())) {
                    Song song = songService.get(favorite.getItemId());
                    if (song != null) {
                        favedSongs.add(song);
                    }
                }
            });
        }
        model.addAttribute("songs", favedSongs);

        return "my";
    }

    // 喜欢或不喜欢操作。对前端比较简单，不必判断状态
    // 已经喜欢，则删除，表示执行不喜欢操作
    // 还没有喜欢记录，则新增，表示执行喜欢操作
    @GetMapping(path = "/fav")
    @ResponseBody
    public Map doFav(@RequestParam(name = "itemType") String itemType, @RequestParam(name = "itemId") String itemId,
                     HttpServletRequest request, HttpServletResponse response) {
        Map resultData = new HashMap();
        // 取得 HttpSession 对象
        HttpSession session = request.getSession();
        UserLoginInfo userLoginInfo = (UserLoginInfo)session.getAttribute("userLoginInfo");
        String userId = userLoginInfo.getUserId();

        Favorite fav = new Favorite();
        fav.setUserId(userId);
        fav.setType(FavoriteUtil.TYPE_RED_HEART);
        fav.setItemType(itemType);
        fav.setItemId(itemId);
        List<Favorite> favs = favoriteService.list(fav);
        if (favs == null || favs.isEmpty()) {
            favoriteService.add(fav);
        } else {
            for (Favorite f : favs) {
                favoriteService.delete(f);
            }
        }

        resultData.put("message", "successful");

        return resultData;
    }

    @GetMapping(path = "/share")
    public String share(Model model) {
        return "share";
    }

    @GetMapping(path = "/error")
    public String error(Model model) {
        return "error";
    }

    private void setSongData(Model model) {
        SongQueryParam songParam = new SongQueryParam();
        songParam.setPageNum(1);
        songParam.setPageSize(1);
        Page<Song> songs = songService.list(songParam);

        if (songs != null && !songs.isEmpty()) {
            Song resultSong = songs.getContent().get(0);
            model.addAttribute("song", resultSong);

            List<String> singerIds = resultSong.getSingerIds();

            List<Singer> singers = new ArrayList<>();
            if (singerIds != null && !singerIds.isEmpty()) {
                singerIds.forEach(singerId -> {
                    Singer singer = singerService.get(singerId);
                    singers.add(singer);
                });
            }

            model.addAttribute("singers", singers);
        }
    }

    private void setMhzData(Model model) {
        // 查询所有的mhz数据
        List<Subject> subjectDatas = subjectService.getSubjects(SubjectUtil.TYPE_MHZ);

        // 在内存中分类，避免查询四次。
        // 查询数据库由于有网络请求，效率比用程序分类低
        List<Subject> artistDatas = new ArrayList<>();
        List<Subject> moodDatas = new ArrayList<>();
        List<Subject> ageDatas = new ArrayList<>();
        List<Subject> styleDatas = new ArrayList<>();

        if (subjectDatas != null && !subjectDatas.isEmpty()) {
            subjectDatas.forEach(subject -> {
                if (SubjectUtil.TYPE_SUB_ARTIST.equals(subject.getSubjectSubType())) {
                    artistDatas.add(subject);
                } else if (SubjectUtil.TYPE_SUB_MOOD.equals(subject.getSubjectSubType())) {
                    moodDatas.add(subject);
                } else if (SubjectUtil.TYPE_SUB_AGE.equals(subject.getSubjectSubType())) {
                    ageDatas.add(subject);
                } else if (SubjectUtil.TYPE_SUB_STYLE.equals(subject.getSubjectSubType())) {
                    styleDatas.add(subject);
                } else {
                    // 防止数据错误
                    LOG.error("subject data error. unknown subtype. subject=" + JSON.toJSONString(subject));
                }
            });
        }

        // 按照页面视觉组织数据：艺术家mhz，由于是独立的区块，不放一起
        model.addAttribute("artistDatas", artistDatas);

        // 按照页面视觉组织数据：按顺序填入三个赫兹数据
        List<MhzViewModel> mhzViewModels = new ArrayList<>();
        buildMhzViewModel(moodDatas, "心情 / 场景", mhzViewModels);
        buildMhzViewModel(ageDatas, "语言 / 年代", mhzViewModels);
        buildMhzViewModel(styleDatas, "风格 / 流派", mhzViewModels);
        model.addAttribute("mhzViewModels", mhzViewModels);
    }

    private void buildMhzViewModel(List<Subject> subjects, String title, List<MhzViewModel> mhzViewModels) {
        MhzViewModel mhzVO = new MhzViewModel();
        mhzVO.setSubjects(subjects);
        mhzVO.setTitle(title);
        mhzViewModels.add(mhzVO);
    }
}
