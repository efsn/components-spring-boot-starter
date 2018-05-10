/**
 * Copyright (c) 2018 Arthur Chan (codeyn@163.com).
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the “Software”), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package cn.elmi.component.lang.text.utils;

import java.util.regex.Pattern;

/**
 * @author Arthur
 * @since 1.0
 */
public enum Validator {

    /**
     * 邮箱
     */
    EMAIL("^[\\u4e00-\\u9fa5\\w]+([-+.][\\u4e00-\\u9fa5\\w]+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$"),

    /**
     * 字母和数字，以字母开头，长度6-20位
     */
    USER_NAME("^[A-Za-z]{1}[0-9A-Za-z]{5,19}$"),

    /**
     * 数字字母组合，不能少于6位
     */
    PASSWD("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9a-zA-Z]{8,20}$"),

    /**
     * 手机号
     */
    MOBILE("^1[3|4|5|7|8]\\d{9}$"),

    /**
     * 固话
     */
    TELEPHONE("^0([1-9]\\d{1,2}\\-{0,1}\\d{7,8})$"),

    /**
     * 中文
     */
    CN_ZH("^[\\u4e00-\\u9fa5]{1,50}$"),

    /**
     * 中英文
     */
    EN_CN("^[a-zA-Z\\u4e00-\\u9fa5]{1,50}$"),

    /**
     * 身份证
     */
    ID_CARD("^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9]|X|x)$|^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$"),

    /**
     * 邮政编码
     */
    POST_CODE("^\\d{6}$"),

    /**
     * 航班号
     */
    FLIGHT_NO("^([a-zA-Z\\\\d]){0,10}$"),

    HTML_TAG("");

    private String regEx;
    private Pattern pattern;

    Validator(String regEx) {
        this.regEx = regEx;
        pattern = Pattern.compile(this.regEx);
    }

}
