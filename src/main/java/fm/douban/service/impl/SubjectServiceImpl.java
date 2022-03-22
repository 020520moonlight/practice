package fm.douban.service.impl;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import fm.douban.model.Subject;
import fm.douban.service.SongService;
import fm.douban.service.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SubjectServiceImpl implements SubjectService {

    private static final Logger LOG = LoggerFactory.getLogger(SubjectServiceImpl.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private SongService songService;

    @Override
    public Subject addSubject(Subject subject) {
        // 作为服务，要对入参进行判断，不能假设被调用时，传入的一定是真正的对象
        if (subject == null) {
            LOG.error("input subject data is null.");
            return null;
        }

        if (subject.getGmtCreated() == null) {
            subject.setGmtCreated(LocalDateTime.now());
        }
        if (subject.getGmtModified() == null) {
            subject.setGmtModified(LocalDateTime.now());
        }

        return mongoTemplate.insert(subject);
    }

    @Override
    public boolean modify(Subject subject) {
        // 作为服务，要对入参进行判断，不能假设被调用时，入参一定正确
        if (subject == null || !StringUtils.hasText(subject.getId())) {
            LOG.error("input song data is not correct.");
            return false;
        }

        // 主键不能修改，作为查询条件
        Query query = new Query(Criteria.where("id").is(subject.getId()));

        Update updateData = new Update();
        // 每次修改都更新 gmtModified 为当前时间
        updateData.set("gmtModified", LocalDateTime.now());
        // 值为 null 表示不修改。值为长度为 0 的字符串 "" 表示清空此字段
        if (subject.getSongIds() != null) {
            updateData.set("songIds", subject.getSongIds());
        }

        // 把一条符合条件的记录，修改其字段
        UpdateResult result = mongoTemplate.updateFirst(query, updateData, Subject.class);
        return result != null && result.getModifiedCount() > 0;
    }

    @Override
    public Subject get(String subjectId) {
        // 输入的主键 id 必须有文本，不能为空或全空格
        if (!StringUtils.hasText(subjectId)) {
            LOG.error("input subjectId is blank.");
            return null;
        }

        Subject subject = mongoTemplate.findById(subjectId, Subject.class);
        return subject;
    }

    @Override
    public List<Subject> getSubjects(String type) {
        return getSubjects(type, null);
    }

    @Override
    public List<Subject> getSubjects(String type, String subType) {
        Subject subjectParam = new Subject();
        subjectParam.setSubjectType(type);
        subjectParam.setSubjectSubType(subType);

        return getSubjects(subjectParam);
    }

    @Override
    public List<Subject> getSubjects(Subject subjectParam) {
        // 作为服务，要对入参进行判断，不能假设被调用时，入参一定正确
        if (subjectParam == null) {
            LOG.error("input subjectParam is not correct.");
            return null;
        }

        String type = subjectParam.getSubjectType();
        String subType = subjectParam.getSubjectSubType();
        String master = subjectParam.getMaster();

        // 作为服务，要对入参进行判断，不能假设被调用时，入参一定正确
        if (!StringUtils.hasText(type)) {
            LOG.error("input type is not correct.");
            return null;
        }

        // 总条件
        Criteria criteria = new Criteria();
        // 可能有多个子条件
        List<Criteria> subCris = new ArrayList();
        subCris.add(Criteria.where("subjectType").is(type));

        if (StringUtils.hasText(subType)) {
            subCris.add(Criteria.where("subjectSubType").is(subType));
        }

        if (StringUtils.hasText(master)) {
            subCris.add(Criteria.where("master").is(master));
        }

        // 三个子条件以 and 关键词连接成总条件对象，相当于 name='' and lyrics='' and subjectId=''
        criteria.andOperator(subCris.toArray(new Criteria[] {}));

        // 条件对象构建查询对象
        Query query = new Query(criteria);

        // 查询结果
        List<Subject> subjects = mongoTemplate.find(query, Subject.class);

        return subjects;
    }

    @Override
    public boolean delete(String subjectId) {
        // 输入的主键 id 必须有文本，不能为空或全空格
        if (!StringUtils.hasText(subjectId)) {
            LOG.error("input subjectId is blank.");
            return false;
        }

        Subject subject = new Subject();
        subject.setId(subjectId);

        DeleteResult result = mongoTemplate.remove(subject);
        return result != null && result.getDeletedCount() > 0;
    }

}
