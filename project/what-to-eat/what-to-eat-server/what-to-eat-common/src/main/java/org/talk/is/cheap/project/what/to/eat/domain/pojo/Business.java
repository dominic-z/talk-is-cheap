package org.talk.is.cheap.project.what.to.eat.domain.pojo;

import java.io.Serializable;
import java.util.Date;

/**
 * Database Table Remarks:
 *   商户表
 *
 * This class was generated by MyBatis Generator.
 * This class corresponds to the database table business
 */
/**
 * do not modify. if you want to add or delete some column, please re-run codegen
*/
public class Business implements Serializable {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column business.id
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    private Long id;

    /**
     * Database Column Remarks:
     *   商户名称
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column business.name
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    private String name;

    /**
     * Database Column Remarks:
     *   商家状态
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column business.status
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    private Integer status;

    /**
     * Database Column Remarks:
     *   头像路径
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column business.avatar_path
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    private String avatarPath;

    /**
     * Database Column Remarks:
     *   商家简介
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column business.description
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    private String description;

    /**
     * Database Column Remarks:
     *   并发控制编号
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column business.revision
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    private Long revision;

    /**
     * Database Column Remarks:
     *   创建日期
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column business.create_time
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    private Date createTime;

    /**
     * Database Column Remarks:
     *   更新日期
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column business.update_time
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    private Date updateTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table business
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    private static final long serialVersionUID = 1L;

    public static final String ID = "id";

    public static final String NAME = "name";

    public static final String STATUS = "status";

    public static final String AVATAR_PATH = "avatar_path";

    public static final String DESCRIPTION = "description";

    public static final String REVISION = "revision";

    public static final String CREATE_TIME = "create_time";

    public static final String UPDATE_TIME = "update_time";

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column business.id
     *
     * @return the value of business.id
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    public Long getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table business
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    public Business withId(Long id) {
        this.setId(id);
        return this;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column business.id
     *
     * @param id the value for business.id
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column business.name
     *
     * @return the value of business.name
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    public String getName() {
        return name;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table business
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    public Business withName(String name) {
        this.setName(name);
        return this;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column business.name
     *
     * @param name the value for business.name
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column business.status
     *
     * @return the value of business.status
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table business
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    public Business withStatus(Integer status) {
        this.setStatus(status);
        return this;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column business.status
     *
     * @param status the value for business.status
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column business.avatar_path
     *
     * @return the value of business.avatar_path
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    public String getAvatarPath() {
        return avatarPath;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table business
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    public Business withAvatarPath(String avatarPath) {
        this.setAvatarPath(avatarPath);
        return this;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column business.avatar_path
     *
     * @param avatarPath the value for business.avatar_path
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column business.description
     *
     * @return the value of business.description
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    public String getDescription() {
        return description;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table business
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    public Business withDescription(String description) {
        this.setDescription(description);
        return this;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column business.description
     *
     * @param description the value for business.description
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column business.revision
     *
     * @return the value of business.revision
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    public Long getRevision() {
        return revision;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table business
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    public Business withRevision(Long revision) {
        this.setRevision(revision);
        return this;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column business.revision
     *
     * @param revision the value for business.revision
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    public void setRevision(Long revision) {
        this.revision = revision;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column business.create_time
     *
     * @return the value of business.create_time
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table business
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    public Business withCreateTime(Date createTime) {
        this.setCreateTime(createTime);
        return this;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column business.create_time
     *
     * @param createTime the value for business.create_time
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column business.update_time
     *
     * @return the value of business.update_time
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table business
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    public Business withUpdateTime(Date updateTime) {
        this.setUpdateTime(updateTime);
        return this;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column business.update_time
     *
     * @param updateTime the value for business.update_time
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table business
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", name=").append(name);
        sb.append(", status=").append(status);
        sb.append(", avatarPath=").append(avatarPath);
        sb.append(", description=").append(description);
        sb.append(", revision=").append(revision);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append("]");
        return sb.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table business
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Business other = (Business) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getAvatarPath() == null ? other.getAvatarPath() == null : this.getAvatarPath().equals(other.getAvatarPath()))
            && (this.getDescription() == null ? other.getDescription() == null : this.getDescription().equals(other.getDescription()))
            && (this.getRevision() == null ? other.getRevision() == null : this.getRevision().equals(other.getRevision()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table business
     *
     * @mbg.generated Thu Nov 23 22:49:50 CST 2023
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getAvatarPath() == null) ? 0 : getAvatarPath().hashCode());
        result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
        result = prime * result + ((getRevision() == null) ? 0 : getRevision().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }
}