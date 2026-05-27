package com.ruinet.ragflow.exception;

/**
 * 业务逻辑异常类。
 *
 * @author 中锐网络
 */
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Integer code;

    public ServiceException(String message) {
        super(message);
        this.code = 500;
    }

    public ServiceException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 使用格式化字符串构建异常信息，符合规约要求的 String.format 规范。
     *
     * @param format 格式化串
     * @param args   参数
     */
    public ServiceException(String format, Object... args) {
        super(String.format(format, args));
        this.code = 500;
    }

    public Integer getCode() {
        return code;
    }
}
