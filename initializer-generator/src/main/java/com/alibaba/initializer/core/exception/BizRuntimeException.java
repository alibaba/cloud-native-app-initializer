/*
 * Copyright 2022-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.initializer.core.exception;


import com.alibaba.initializer.core.constants.ErrorCodeEnum;

/**
 * 业务异常基类
 */
public class BizRuntimeException extends RuntimeException implements BizException {

    private static final long serialVersionUID = -4414353662837365450L;

    protected ErrorCodeEnum errorCodeEnum;
    protected String userMessage;

    /**
     * @param errorCodeEnum
     * @param message
     */
    public BizRuntimeException(ErrorCodeEnum errorCodeEnum, String message) {
        super(message);
        this.userMessage = message;
        this.errorCodeEnum = errorCodeEnum;
    }

    /**
     * @param errorCodeEnum
     * @param message
     * @param cause
     */
    public BizRuntimeException(ErrorCodeEnum errorCodeEnum, String message,
                               Throwable cause) {
        super(message, cause);
        this.userMessage = message;
        this.errorCodeEnum = errorCodeEnum;
    }

    @Override
    public ErrorCodeEnum getCode() {
        return errorCodeEnum;
    }

    @Override
    public String getUserMessage() {
        return userMessage;
    }

}
