import { PlusOutlined } from '@ant-design/icons';
import { FloatButton } from 'antd';
import React, { useEffect, useState } from 'react';
import AxiosUtil from '../config/AxiosUtil';
import '../css/BusinessList.css'
import BusinessBO from '../domain/bo/BusinessBO';
import GetBusinessListResp from '../domain/message/GetBusinessListResp';
import Business from './Business';



const url = "/business"
export default function BusinessList() {

    const [businessBOs, setBusinessBOs] = useState<BusinessBO[]>([])

    useEffect(
        () => {
            AxiosUtil.get<GetBusinessListResp>(url,
                {
                    params: {
                        page: 1,
                        pageSize: 10
                    }
                }
            )
                .then(resp => {
                    console.log(resp.data.data)
                    setBusinessBOs(resp.data.data.businessBOs)
                }).catch(e => {
                    console.log(e);
                })
        }
        , [])



    const businessComponents = businessBOs.map(
        b => {
            return <Business key={b.id} name={b.name} description={b.description} imgUrl={""}></Business>
        }
    )
    return (
        <div className='businessList'>
            {businessComponents}
            {businessComponents}
            {businessComponents}
            {businessComponents}
            {businessComponents}
            {businessComponents}
            {businessComponents}
            {businessComponents}
            {businessComponents}
            {businessComponents}
            {businessComponents}
            {businessComponents}

            <div className='addBusinessButtonBox'>
            <FloatButton
                shape="square"
                type="primary"
                className='addBusinessButton'
                icon={<PlusOutlined />}
            />
            </div>

            
        </div>

    )

}