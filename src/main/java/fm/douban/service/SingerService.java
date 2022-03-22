package fm.douban.service;

import fm.douban.model.Singer;

import java.util.List;

public interface SingerService {

    /**
     * 增加一个歌手
     *
     * @param singer
     * @return
     */
    Singer addSinger(Singer singer);

    /**
     * 根据歌手 id 查询歌手
     *
     * @param singerId
     * @return
     */
    Singer get(String singerId);

    /**
     * 查询全部歌手
     *
     * @return
     */
    List<Singer> getAll();

    /**
     * 修改歌手
     *
     * @param singer
     * @return
     */
    boolean modify(Singer singer);

    /**
     * 删除歌手
     *
     * @param singer
     * @return
     */
    boolean delete(String singerId);
}
