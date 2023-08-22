import React from 'react';
import '../css/CalenderContainer.css'
import { Badge, Calendar } from 'antd';
import type { BadgeProps } from 'antd';
import type { Dayjs } from 'dayjs';
import type { CellRenderInfo } from 'rc-picker/lib/interface';

import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import zhCN from 'antd/locale/zh_CN';
import { ConfigProvider } from 'antd/lib';
// dayjs.locale('zh-cn');

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
            <Calendar cellRender={cellRender} />
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