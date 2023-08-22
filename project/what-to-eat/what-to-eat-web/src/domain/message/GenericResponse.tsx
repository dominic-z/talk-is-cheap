export default interface GenericResponse<T>{
    code:number;
    message:string;
    data:T;
}