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
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public interface BizException {

    /**
     * 异常的错误信息，可能直接呈现给用户的
     * 如需国际化，请提前处理好
     *
     * @return
     */
    String getUserMessage();

    /**
     * 异常的类别
     * （便于下游处理，如前端）
     *
     * @return
     */
    ErrorCodeEnum getCode();

}
