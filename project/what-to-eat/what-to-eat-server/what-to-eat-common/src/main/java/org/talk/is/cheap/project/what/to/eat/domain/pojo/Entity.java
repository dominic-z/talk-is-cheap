package org.talk.is.cheap.project.what.to.eat.domain.pojo;

import java.io.Serializable;
import java.util.Date;

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
