package org.talk.is.cheap.project.what.to.eat.dao.mbg;

import java.util.Collection;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.talk.is.cheap.project.what.to.eat.domain.pojo.BusinessMarketingPlan;
import org.talk.is.cheap.project.what.to.eat.domain.query.example.BusinessMarketingPlanExample;

public interface BusinessMarketingPlanMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table business_marketing_plan
     *
     * @mbg.generated Mon Aug 07 22:24:00 CST 2023
     */
    long countByExample(BusinessMarketingPlanExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table business_marketing_plan
     *
     * @mbg.generated Mon Aug 07 22:24:00 CST 2023
     */
    int deleteByExample(BusinessMarketingPlanExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table business_marketing_plan
     *
     * @mbg.generated Mon Aug 07 22:24:00 CST 2023
     */
    int insertSelective(BusinessMarketingPlan record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table business_marketing_plan
     *
     * @mbg.generated Mon Aug 07 22:24:00 CST 2023
     */
    List<BusinessMarketingPlan> selectByExampleWithRowbounds(BusinessMarketingPlanExample example, RowBounds rowBounds);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table business_marketing_plan
     *
     * @mbg.generated Mon Aug 07 22:24:00 CST 2023
     */
    List<BusinessMarketingPlan> selectByExample(BusinessMarketingPlanExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table business_marketing_plan
     *
     * @mbg.generated Mon Aug 07 22:24:00 CST 2023
     */
    int updateByExampleSelective(@Param("record") BusinessMarketingPlan record, @Param("example") BusinessMarketingPlanExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table business_marketing_plan
     *
     * @mbg.generated Mon Aug 07 22:24:00 CST 2023
     */
    int insertBatch(Collection<BusinessMarketingPlan> records);
}