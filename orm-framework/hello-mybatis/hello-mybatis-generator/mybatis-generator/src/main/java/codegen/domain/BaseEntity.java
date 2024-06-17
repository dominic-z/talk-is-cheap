package codegen.domain;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * @author dominiczhu
 * @version 1.0
 * @title BaseEntity
 * @date 2022/1/13 3:34 下午
 */
public class BaseEntity implements Entity {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    protected long id;

    /**
     * 修订版本
     */
    protected int revision = 1;

    /**
     * 创建时间
     */
    protected Date createdTime;

    /**
     * 更新时间
     */
    protected Date lastModifiedTime;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public int getRevision() {
        return revision;
    }

    @Override
    public void setRevision(int revision) {
        this.revision = revision;
    }

    @Override
    public Date getCreatedTime() {
        return createdTime;
    }

    @Override
    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    @Override
    public Date getLastModifiedTime() {
        return lastModifiedTime;
    }

    @Override
    public void setLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}

