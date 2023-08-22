import BusinessBO from "../bo/BusinessBO";
import GenericResponse from "./GenericResponse";

export default interface GetBusinessListResp extends GenericResponse<GetBusinessListRespData>{
    
    
    
}


interface GetBusinessListRespData{
    page:number;
    pageSize:number;
    total:number;
    businessBOs:BusinessBO[]
}

const a:GetBusinessListResp = {
    code:1,
    message:"null",
    data:{
        page:1,
        pageSize:1,
        total:20,
        businessBOs:[]
    }

}
