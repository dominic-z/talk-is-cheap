package org.talk.is.cheap.project.what.to.eat.constants;

public class ErrorCode {

    private ErrorCode() {
    }

    // 未捕获的异常
    public static final int ERROR = 8000;
    // 参数非法异常
    public static final int ILLEGAL_PARAMETER_ERROR = 8001;
    // 对应数据未查询到异常
    public static final int DATA_NOT_FOUND_ERROR = 8002;
    // 文件类型不支持异常
    public static final int ILLEGAL_FILE_TYPE_ERROR = 8002;
}
