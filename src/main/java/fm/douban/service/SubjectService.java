package fm.douban.service;

import fm.douban.model.Subject;

import java.util.List;

public interface SubjectService {

    /**
     * 增加一个主题
     *
     * @param subject
     * @return
     */
    Subject addSubject(Subject subject);

    /**
     * 修改一个主题
     */
    boolean modify(Subject subject);

    /**
     * 查询单个主题
     *
     * @param subjectId
     * @return
     */
    Subject get(String subjectId);

    /**
     * 查询一组主题
     *
     * @param type 一级分类
     * @return
     */
    List<Subject> getSubjects(String type);

    /**
     * 查询一组主题
     *
     * @param type    一级分类
     * @param subType 二级分类
     * @return
     */
    List<Subject> getSubjects(String type, String subType);

    /**
     * 查询一组主题
     *
     * @param subjectParam    查询参数
     * @return
     */
    List<Subject> getSubjects(Subject subjectParam);

    /**
     * 删除主题
     *
     * @param subjectId
     * @return
     */
    boolean delete(String subjectId);

}