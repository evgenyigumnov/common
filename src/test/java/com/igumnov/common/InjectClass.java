package com.igumnov.common;

import com.igumnov.common.dependency.Inject;
import com.igumnov.common.dependency.Named;

@Named("inject")
public class InjectClass {

    @Inject("inject")
    private InjectClass injectTest;


    private String retVal = "1";

    private static InjectClass singleton;

    InjectClass() {
        singleton = this;
    }


    public String ret () {
        return retVal;
    }




    public static String  test() {
        return singleton.injectTest.ret();
    }

}
