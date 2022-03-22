package fm.douban.service;

import fm.douban.model.Favorite;

import java.util.List;

public interface FavoriteService {

    /**
     * 新增一个喜欢
     *
     * @param fav 对象
     */
    Favorite add(Favorite fav);

    /**
     * 计算喜欢数。如果喜欢数大于 0 ，表示已经喜欢
     *
     * @param favParam 参数
     * @return
     */
    List<Favorite> list(Favorite favParam);

    /**
     * 删除一个喜欢
     *
     * @param favParam 参数
     */
    boolean delete(Favorite favParam);
}
