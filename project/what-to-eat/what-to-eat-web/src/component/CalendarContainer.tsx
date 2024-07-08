import React, { useState } from 'react';
import '../css/CalenderContainer.css'
import { Badge, Button, Calendar, Col, Radio, Row, Select, Typography } from 'antd';
import type { BadgeProps } from 'antd';
import type { Dayjs } from 'dayjs';
import type { CellRenderInfo } from 'rc-picker/lib/interface';

import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import zhCN from 'antd/locale/zh_CN';
import { ConfigProvider } from 'antd/lib';
import MaskContainer from './MaskContainer';
import { CalendarMode, HeaderRender } from 'antd/es/calendar/generateCalendar';
import dayLocaleData from 'dayjs/plugin/localeData';

// 设置日期格式为中文
dayjs.locale('zh-cn');

// 这样dayjs就会有localData属性，抄的文档
dayjs.extend(dayLocaleData);

export default function CalenderContainer() {


    const dateCellRender = (value: Dayjs) => {
        const listData = getListData(value);
        return (
            <ul className="events">
                {listData.map((item) => (
                    <li key={item.content}>
                        <Badge status={item.type as BadgeProps['status']} text={item.content} />
                    </li>
                ))}
            </ul>
        );
    };
    const cellRender = (current: Dayjs, info: CellRenderInfo<Dayjs>) => {
        if (info.type === 'date') return dateCellRender(current);
        // if (info.type === 'month') return monthCellRender(current);
        return info.originNode;
    };


    return (

        <div className='calenderContainer'>
            <Calendar
                headerRender={headerRender}
                cellRender={cellRender} fullscreen={true}
                onPanelChange={(date, mode) => { console.log(date) }}
                onSelect={(date, selecInfo) => { console.log(date) }}
                onChange={(date) => { console.log(date) }}
            />
        </div>

    )
}


function headerRender({ value, type, onChange, onTypeChange }: {
    value: Dayjs;
    type: CalendarMode;
    onChange: (date: Dayjs) => void;
    onTypeChange: (type: CalendarMode) => void;
}) {


    let current = value.clone()
    let localeData = current.localeData()
    const monthOptions = []
    for (let i = 0; i < 12; i++) {
        monthOptions.push(
            <Select.Option key={i} value={i} className="month-item">
                {/* 抄的抄的 */}
                {localeData.monthsShort(current.month(i))}
            </Select.Option>
        )
    }

    let yearOptions = []
    for (let i = current.year() - 10; i < current.year() + 10; i++) {
        yearOptions.push(
            <Select.Option key={i} value={i} className="year-item">
                {i}
            </Select.Option>
        )
    }


    return (
        <div style={{ padding: 8 }}>
            <Row gutter={8} justify="end">
            <Typography.Title level={4}>Custom header</Typography.Title>
            </Row>
            <Row gutter={8} justify="end">
                <Col>
                    <Button> 呱呱</Button>
                </Col>
                <Col>
                    <Radio.Group
                        onChange={(e) => onTypeChange(e.target.value)}
                        value={type}
                    >
                        <Radio.Button value="month">Month</Radio.Button>
                        <Radio.Button value="year">Year</Radio.Button>
                    </Radio.Group>
                </Col>
                <Col>
                    <Select
                        dropdownMatchSelectWidth={false}
                        // className="my-year-select"
                        value={value.year()}
                        onChange={(newYear) => {
                            const now = value.clone().year(newYear);
                            onChange(now);
                        }}
                    >
                        {yearOptions}
                    </Select>
                </Col>
                <Col>
                    <Select
                        dropdownMatchSelectWidth={false}
                        value={value.month()}
                        onChange={(newMonth) => {
                            const now = value.clone().month(newMonth);
                            onChange(now);
                        }}
                    >
                        {monthOptions}
                    </Select>
                </Col>
            </Row>
        </div>
    )

}

const getListData = (value: Dayjs) => {
    // console.log(value.date(),value.month());
    let listData;
    switch (value.date()) {
        case 8:
            listData = [
                { type: 'warning', content: 'This is warning event.' },
                { type: 'success', content: 'This is usual event.' },
            ];
            break;
        case 10:
            listData = [
                { type: 'warning', content: 'This is warning event.' },
                { type: 'success', content: 'This is usual event.' },
                { type: 'error', content: 'This is error event.' },
            ];
            break;
        case 15:
            listData = [
                { type: 'warning', content: 'This is warning event' },
                { type: 'success', content: 'This is very long usual event。。....' },
                { type: 'error', content: 'This is error event 1.' },
                { type: 'error', content: 'This is error event 2.' },
                { type: 'error', content: 'This is error event 3.' },
                { type: 'error', content: 'This is error event 4.' },
            ];
            break;
        default:
    }
    return listData || [];
};