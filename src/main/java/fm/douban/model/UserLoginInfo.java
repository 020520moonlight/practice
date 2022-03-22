package fm.douban.model;

import java.io.Serializable;

/**
 * 演示用的用户登录信息
 */
public class UserLoginInfo implements Serializable {
    private String userId;
    private String userName;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
