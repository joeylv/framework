/**
 * Created by jolv on 2017/5/11.
 */

package com.ehsure.framework;

import com.ehsure.framework.generate.AutoGenerate;

public class Generate {
    public static void main(String[] args) {
        AutoGenerate autoGenerate = new AutoGenerate("10.1.12.149", "3306", "platform", "root", "Zxas,/12");
        autoGenerate.setBasePackage("com.ehsure");
        autoGenerate.generateCode(new String[]{"sys_person"});
    }
}