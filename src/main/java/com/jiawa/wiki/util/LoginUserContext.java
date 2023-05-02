package com.jiawa.wiki.util;


import com.jiawa.wiki.resp.AdminLoginResp;

import java.io.Serializable;

public class LoginUserContext implements Serializable {

    private static ThreadLocal<AdminLoginResp> user = new ThreadLocal<>();

    public static AdminLoginResp getUser() {
        return user.get();
    }

    public static void setUser(AdminLoginResp user) {
        LoginUserContext.user.set(user);
    }

}
