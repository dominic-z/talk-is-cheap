import axios from "axios";
import config from "./config";
const AxiosUtil = axios.create({
    baseURL: config.getBaseUrl(),
    timeout: 1000,
    
  });

export default AxiosUtil;


