import React, { useState } from 'react';
import { Button, Card, Checkbox, ConfigProvider, Form } from 'antd';
import { Input } from 'antd/lib';
import "../css/BusinessForm.css"
import TextArea from 'antd/es/input/TextArea';
import axios from 'axios';
import AxiosUtil from '../config/AxiosUtil';
import CreateBusinessReq from '../domain/message/CreateBusinessReq';
import CreateBusinessResp from '../domain/message/CreateBusinessResp';
import BusinessBO from "../domain/bo/BusinessBO"
import BusinessDTO from '../domain/dto/BusinessDTO';
import { LoadingOutlined } from '@ant-design/icons';

interface Props {
    businessBO?: BusinessBO,
    defaultSubmitValue?: string,
    onFinish: (fieldType: BusinessFormFieldType) => void
    onCancel: () => void
    onFinishFailed: (errorInfo: any) => void
}

export type BusinessFormFieldType = {
    name: string;
    description?: string;
};

export default function BusinessForm(props: Props) {
    const [form] = Form.useForm()
    console.log(props)

    const onFinish = (values: BusinessFormFieldType) => {

        console.log('form:', form.getFieldValue("name"));
        props.onFinish(values);

    };

    const onFinishFailed = (errorInfo: any) => {
        console.log('Failed:', errorInfo);
    };

    const [showLoading, setShowLoading] = useState<boolean>(false)



    return (
        <Card className='businessFormContainer' onClick={e => e.stopPropagation()}>
            <Form
                className='businessForm'
                name="basic"
                labelCol={{ span: 4 }}
                wrapperCol={{ span: 18 }}
                initialValues={{ remember: true }}
                onFinish={props.onFinish}
                onFinishFailed={props.onFinishFailed}
                autoComplete="off"
                form={form}
            >
                <Form.Item<BusinessFormFieldType>
                    label="店名"
                    name="name"
                    rules={[{ required: true, message: '店叫啥名啊？' }]}
                    initialValue={props.businessBO?.name}
                >
                    <Input maxLength={32} placeholder="店叫啥名啊？"
                        value={props.businessBO?.name} />
                </Form.Item>

                <Form.Item<BusinessFormFieldType>
                    label="简介"
                    name="description"
                    rules={[{ required: false, message: '介绍两句啊！' }]}
                    initialValue={props.businessBO?.description}
                >
                    <TextArea rows={4} placeholder="介绍两句啊！" maxLength={60}
                    />
                </Form.Item>

                <Form.Item wrapperCol={{ offset: 8, span: 16 }}>

                    <ConfigProvider autoInsertSpaceInButton={false}>
                        <Button type="primary" htmlType="submit" onClick={e => setShowLoading(true)}
                            style={{ width: '70px', }}
                        >
                            {props.defaultSubmitValue ? props.defaultSubmitValue : "创建"}
                            {showLoading ? <LoadingOutlined /> : ""}
                        </Button>
                        <Button htmlType="button" style={{ margin: '0 8px', width: '70px' }} onClick={e => {
                            props.onCancel()
                        }}
                        >
                            取消
                        </Button>
                    </ConfigProvider>


                </Form.Item>
            </Form>
        </Card>


    )
}