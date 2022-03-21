package codegen.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * @author dominiczhu
 * @version 1.0
 * @title Entity
 * @date 2022/1/13 3:35 下午
 */
public interface Entity extends Serializable {

    long getId();

    void setId(long id);

    void setRevision(int revision);

    int getRevision();

    Date getCreatedTime();

    void setCreatedTime(Date ctime);

    Date getLastModifiedTime();

    void setLastModifiedTime(Date utime);

}
