package com.mybharat.utils;

/**
 * ELPTestContext - Shared context to pass data between ELP test classes.
 * 
 * Since all ELP tests run in the same test suite (single thread, preserve-order),
 * a static field is safe for sharing state like the created ELP title.
 */
public class ELPTestContext {

    /** The title of the ELP created by ELPCreateTest, used by ELPYouthApplyTest to find and apply. */
    private static String createdELPTitle;

    /** The admin email used for ELP login, needed for re-login after youth applies. */
    private static String adminEmail;

    /** The youth email used for ELP apply, needed for re-login in completion step. */
    private static String youthEmail;

    public static String getCreatedELPTitle() {
        return createdELPTitle;
    }

    public static void setCreatedELPTitle(String title) {
        createdELPTitle = title;
    }

    public static String getAdminEmail() {
        return adminEmail;
    }

    public static void setAdminEmail(String email) {
        adminEmail = email;
    }

    public static String getYouthEmail() {
        return youthEmail;
    }

    public static void setYouthEmail(String email) {
        youthEmail = email;
    }
}
