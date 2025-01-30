import axios from "axios"



const AxiosUtil = axios.create({
    baseURL: "http://localhost:8080/api",
    timeout: 10000,
    
  });

export default AxiosUtil;
