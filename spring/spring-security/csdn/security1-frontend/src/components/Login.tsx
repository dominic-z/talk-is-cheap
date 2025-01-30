

import React, { FormEvent, useState } from "react";
import AxiosUtil from "../utils/AxioUtil"
import { useCookies } from 'react-cookie'


export default function LoginForm() {

    const [username, setUsername] = useState("")
    const [password, setPassword] = useState("")
    const [cookies, setCookie, removeCookie] = useCookies(['user']);
    const [someContent, setContent] = useState("")


    function onSubmit(e: FormEvent<HTMLFormElement>) {
        e.preventDefault()
        const data = {
            "username": username,
            "password": password
        }
        AxiosUtil.post("/user/login", data)
            .then((resp => {
                console.log(resp)
                setCookie('user', resp.data.data.token, { path: '/' });
            }))
            .catch((r => {
                console.log(r)
            }))
    }

    function clearCookie(){
        removeCookie('user');
    }

    function viewSometingAfterLogin() {
        AxiosUtil.get("/someContent", {
            // withCredentials: true, // 跨域请求时携带 Cookie
            // headers:{
            //     'Access-Control-Allow-Origin':'*'
            // }
        })
            .then((resp => {
                console.log(resp)
                setContent(resp.data.data)
            }))
            .catch((r => {
                console.log(r)

            }))
    }

    return <>
        <form onSubmit={onSubmit}>
            <div>
                <label htmlFor="username">
                    username: <input id="username" onChange={e => setUsername(e.target.value)} />
                </label>
            </div>

            <div>
                <label htmlFor="password">
                    password: <input id="password" type="password" onChange={e => setPassword(e.target.value)} />
                </label>
            </div>
            <button >login</button>
        </form>

        <div>
        <button onClick={clearCookie}>clearCookie</button>

        </div>
        <div>
            {cookies.user || '' ? <p>你的 Cookie 值是: {cookies.user || ''}</p> : <p>cookie is empty</p>}
        </div>


        <div>
            <button onClick={viewSometingAfterLogin}>say hello</button>
            <div>hello! {someContent}</div>
        </div>
    </>

}